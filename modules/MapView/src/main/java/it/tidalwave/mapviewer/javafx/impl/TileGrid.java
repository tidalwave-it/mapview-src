/*
 * *************************************************************************************************************************************************************
 *
 * MapView: a JavaFX map renderer for tile-based servers
 * http://tidalwave.it/projects/mapview
 *
 * Copyright (C) 2024 - 2025 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 * *************************************************************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 *
 * *************************************************************************************************************************************************************
 *
 * git clone https://bitbucket.org/tidalwave/mapview-src
 * git clone https://github.com/tidalwave-it/mapview-src
 *
 * *************************************************************************************************************************************************************
 */
package it.tidalwave.mapviewer.javafx.impl;

import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.net.URI;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.tidalwave.mapviewer.MapCoordinates;
import it.tidalwave.mapviewer.TileSource;
import it.tidalwave.mapviewer.impl.MapViewModel;
import it.tidalwave.mapviewer.impl.TileCache;
import it.tidalwave.mapviewer.javafx.MapView;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import static java.lang.Double.doubleToLongBits;

/***************************************************************************************************************************************************************
 *
 * A grid of tiles, used to completely fill an arbitrary area of a graphic device.
 * 
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@Slf4j @Accessors(fluent = true)
public class TileGrid extends StackPane
  {
    enum Dirty
      {
        /** Not dirty */ NONE,
        /** Only grid needs to be rebuilt. */ GRID,
        /** All need to be rebuilt, */ ALL
      }

    /** The owner component. */
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final MapView parent;

    /** The tile source. */
    @Nonnull
    private final ObjectProperty<TileSource> tileSource;

    /** The model. */
    @Nonnull
    private final MapViewModel model;

    /** The tile cache. */
    @Nonnull
    private final TileCache tileCache;

    /** Whether this control needs to be redrawn. */
    private Dirty dirty = Dirty.NONE;

    /** The map of overlays indexed by name. */
    private final Map<String, MapOverlay> overlayByName = new HashMap<>();

    /** The container of tiles. */
    private final GridPane tilePane = new GridPane();

    /** The container of overlays. */
    private final StackPane overlayPane = new StackPane();

    /***********************************************************************************************************************************************************
     * Creates a grid of tiles.
     * @param   parent      the map view control
     * @param   model       the map model
     * @param   tileSource  the tile source
     * @param   tileCache   the tile cache
     **********************************************************************************************************************************************************/
    @SuppressFBWarnings({"EI_EXPOSE_REP2", "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR"})
    public TileGrid (@Nonnull final MapView parent,
                     @Nonnull final MapViewModel model,
                     @Nonnull final ObjectProperty<TileSource> tileSource,
                     @Nonnull final TileCache tileCache)
      {
        this.parent = parent;
        this.tileSource = tileSource;
        this.model = model;
        this.tileCache = tileCache;
        getChildren().addAll(tilePane, overlayPane);
        parent.layoutBoundsProperty().addListener((_1, _2, _3) -> setDirty(Dirty.GRID));
        model.setCenterAndZoom(MapCoordinates.of(0, 0), 1);
        tileSource.addListener((_1, _2, _3) -> onTileSourceChanged());
      }

    /***********************************************************************************************************************************************************
     * Sets the coordinates at the center of the grid and the zoom level. This method will update the tiles in the grid with the proper URLs for the required
     * setting. If the grid is already populated, existing tiles are recycled if possible (this is useful while moving the coordinates in order to avoid the
     * number of tiles to download for the next position).
     * @param  center       the center at the center of the tile
     * @param  zoom         the zoom level
     **********************************************************************************************************************************************************/
    public void setCenterAndZoom (@Nonnull final MapCoordinates center, final double zoom)
      {
        log.debug("setCenterAndZoom({}, {})", center, zoom);

        if (!center.equals(model.center()) || doubleToLongBits(zoom) != doubleToLongBits(model.zoom())) // defensive
          {
            model.setCenterAndZoom(center, zoom);
            createTiles();
            recreateOverlays();
            setDirty(Dirty.ALL);
          }
      }

    /***********************************************************************************************************************************************************
     * {@return the coordinates of the point at the center of the map}.
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapCoordinates getCenter()
      {
        return model.center();
      }

    /***********************************************************************************************************************************************************
     * Translates the tile grid. If the translation is so large that the tile at the center changes, the grid is recomputed and translated back.
     * @param   deltaX    the drag in screen coordinates
     * @param   deltaY    the drag in screen coordinates
     **********************************************************************************************************************************************************/
    public void translate (final double deltaX, final double deltaY)
      {
        log.trace("translate({}, {})", deltaX, deltaY);
        final var prevTileCenter = model.tileCenter();
        model.setCenterAndZoom(model.pointCenter().translated(-deltaX, -deltaY), model.zoom());
        final var tileCenter = model.tileCenter();

        if (!prevTileCenter.equals(tileCenter))
          {
            createTiles();
            // no need to recreate overlays, just translate them
            final var dX = overlayPane.getTranslateX() -(tileCenter.column - prevTileCenter.column) * tileSource.get().getTileSize();
            final var dY = overlayPane.getTranslateY() -(tileCenter.row - prevTileCenter.row) * tileSource.get().getTileSize();
            log.debug("translate overlays: {}, {}", dX, dY);
            overlayPane.setTranslateX(dX);
            overlayPane.setTranslateY(dY);
            setDirty(Dirty.GRID);
          }
        else
          {
            applyTranslate();
          }
      }

    /***********************************************************************************************************************************************************
     * Adds an overlay.
     * @param   name      the name of the overlay
     * @param   creator   the overlay creator
     **********************************************************************************************************************************************************/
    public void addOverlay (@Nonnull final String name, @Nonnull final Consumer<MapView.OverlayHelper> creator)
      {
        final var overlay = new MapOverlay(model, creator);
        overlayPane.getChildren().add(overlay);
        overlayByName.put(name, overlay);
        overlay.create();
      }

    /***********************************************************************************************************************************************************
     * Removes an overlay.
     * @param   name      the name of the overlay to remove
     **********************************************************************************************************************************************************/
    public void removeOverlay (@Nonnull final String name)
      {
        if (overlayByName.containsKey(name))
          {
            overlayPane.getChildren().remove(overlayByName.remove(name));
          }
      }

    /***********************************************************************************************************************************************************
     * Removes all overlays.
     **********************************************************************************************************************************************************/
    public void removeAllOverlays()
      {
        overlayByName.clear();
        overlayPane.getChildren().clear();
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override
    protected void layoutChildren()
      {
        log.trace("layoutChildren");

        if (dirty != Dirty.NONE && isVisible())
          {
            final var parentWidth = parent.getWidth();
            final var parentHeight = parent.getHeight();
            final var centerTileChanged = model.updateGridSize(parentWidth, parentHeight);
            model.recompute();

            if (centerTileChanged)
              {
                log.debug("new view size: {} x {}, new grid size: {} x {}", parentWidth, parentHeight, model.columns(), model.rows());
                createTiles();

                if (dirty == Dirty.ALL)
                  {
                    recreateOverlays();
                  }
              }
            else
              {
                applyTranslate();
              }
          }

        dirty = Dirty.NONE;
        super.layoutChildren();
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    private void onTileSourceChanged()
      {
        log.debug("onTileSourceChanged()");
        model.setTileSource(tileSource.get());
        createTiles();
        setDirty(Dirty.GRID);
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    private void createTiles()
      {
        log.debug("createTiles()");
        tilePane.getChildren().clear();
        model.iterateOnGrid((pos, url) -> tilePane.add(createTile(url), pos.column(), pos.row(), 1, 1));
        applyTranslate();
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    private void recreateOverlays()
      {
        log.debug("recreateOverlays()");
        overlayPane.setTranslateX(0);
        overlayPane.setTranslateY(0);
        overlayByName.values().forEach(MapOverlay::create);
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    @Nonnull
    private Node createTile (@Nonnull final URI uri)
      {
        return new Tile(tileCache, tileSource.get(), uri, tileSource.get().getTileSize(), (int)model.zoom());
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    private void applyTranslate()
      {
        setTranslateX(model.gridOffset().x());
        setTranslateY(model.gridOffset().y());
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    private void setDirty (@Nonnull final Dirty dirty)
      {
        this.dirty = dirty;
        setNeedsLayout(true);
      }
  }
