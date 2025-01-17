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
package it.tidalwave.mapviewer.javafx;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.nio.file.Path;
import javafx.animation.Interpolatable;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import javafx.application.Platform;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.tidalwave.mapviewer.MapArea;
import it.tidalwave.mapviewer.MapCoordinates;
import it.tidalwave.mapviewer.MapViewPoint;
import it.tidalwave.mapviewer.OpenStreetMapTileSource;
import it.tidalwave.mapviewer.TileSource;
import it.tidalwave.mapviewer.impl.MapViewModel;
import it.tidalwave.mapviewer.impl.RangeLimitedDoubleProperty;
import it.tidalwave.mapviewer.impl.TileCache;
import it.tidalwave.mapviewer.javafx.impl.TileGrid;
import it.tidalwave.mapviewer.javafx.impl.Translation;
import org.apiguardian.api.API;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import static org.apiguardian.api.API.Status.STABLE;
import static java.lang.Double.doubleToLongBits;
import static javafx.collections.FXCollections.observableList;
import static javafx.util.Duration.ZERO;

/***************************************************************************************************************************************************************
 *
 * A JavaFX control capable to render a map based on tiles. It must be associated to a {@link TileSource} that provides the tile bitmaps; two instances are
 * provided, {@link OpenStreetMapTileSource} and {@link it.tidalwave.mapviewer.OpenTopoMapTileSource}. Further sources can be easily implemented by overriding
 * {@link it.tidalwave.mapviewer.spi.TileSourceSupport}.
 * The basic properties of a {@code MapView} are:
 *
 * <ul>
 *   <li>{@link #tileSourceProperty()}: the tile source (that can be changed during the life of {@code MapView});</li>
 *   <li>{@link #centerProperty()}: the coordinates that are rendered at the center of the screen;</li>
 *   <li>{@link #zoomProperty()}: the detail level for the map, going from 1 (the lowest) to a value depending on the tile source.</li>
 * </ul>
 *
 * Other properties are:
 *
 * <ul>
 *   <li>{@link #minZoomProperty()} (read only): the minimum zoom level allowed;</li>
 *   <li>{@link #maxZoomProperty()} (read only): the maximum zoom level allowed;</li>
 *   <li>{@link #mouseCoordinatesProperty()} (read only): the coordinates corresponding to the point where the mouse is;</li>
 *   <li>{@link #areaProperty()} (read only): the rectangular area delimited by north, east, south, west coordinates that is currently rendered.</li>
 * </ul>
 *
 * The method {@link #fitArea(MapArea)} can be used to adapt rendering parameters so that the given area is rendered; this is useful e.g. when one wants to
 * render a GPS track.
 *
 * Maps can be scrolled by dragging and re-centered by double-clicking on a point (use {@link #setRecenterOnDoubleClick(boolean)} to enable this behaviour).
 *
 * It is possible to add and remove overlays that move in solid with the map:
 *
 * <ul>
 *   <li>{@link #addOverlay(String, Consumer)}</li>
 *   <li>{@link #removeOverlay(String)}</li>
 *   <li>{@link #removeAllOverlays()}</li>
 * </ul>
 *
 * @see     OpenStreetMapTileSource
 * @see     it.tidalwave.mapviewer.OpenTopoMapTileSource
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@API(status = STABLE)
@Slf4j
public class MapView extends Region
  {
    private static final int DEFAULT_TILE_POOL_SIZE = 10;
    private static final int DEFAULT_TILE_QUEUE_CAPACITY = 1000;
    private static final OpenStreetMapTileSource DEFAULT_TILE_SOURCE = new OpenStreetMapTileSource();

    /** The placeholder used while the tile image has not been loaded yet. */
    private static final Supplier<Image> WAITING_IMAGE = () -> new Image(MapView.class.getResource("/hold-on.gif").toExternalForm());

    /***********************************************************************************************************************************************************
     * This helper class provides methods useful for creating map overlays.
     **********************************************************************************************************************************************************/
    @API(status = STABLE)
    @RequiredArgsConstructor(staticName = "of") @Accessors(fluent = true)
    public static class OverlayHelper
      {
        @Nonnull
        private final MapViewModel model;

        @Nonnull
        private final ObservableList<Node> children;

        /*******************************************************************************************************************************************************
         * Adds a {@link Node} to the overlay.
         * @param   node          the {@code Node}
         ******************************************************************************************************************************************************/
        public void add (@Nonnull final Node node)
          {
            children.add(node);
          }

        /*******************************************************************************************************************************************************
         * Adds multiple {@link Node}s to the overlay.
         * @param   nodes         the {@code Node}s
         ******************************************************************************************************************************************************/
        public void addAll (@Nonnull final Collection<? extends Node> nodes)
          {
            children.addAll(nodes);
          }

        /*******************************************************************************************************************************************************
         * {@return a map view point corresponding to the given coordinates}. This view point must be used to draw to the overlay.
         * @param   coordinates   the coordinates
         ******************************************************************************************************************************************************/
        @Nonnull
        public MapViewPoint toMapViewPoint (@Nonnull final MapCoordinates coordinates)
          {
            final var gridOffset = model.gridOffset();
            final var point = model.coordinatesToMapViewPoint(coordinates);
            return MapViewPoint.of(point.x() - gridOffset.x(), point.y() - gridOffset.y());
          }

        /*******************************************************************************************************************************************************
         * {@return the coordinates corresponding at the center of the map view}.
         ******************************************************************************************************************************************************/
        @Nonnull
        public MapCoordinates getCenter()
          {
            return model.center();
          }

        /*******************************************************************************************************************************************************
         * {@return the zoom level}.
         ******************************************************************************************************************************************************/
        public double getZoom()
          {
            return model.zoom();
          }

        /*******************************************************************************************************************************************************
         * {@return the area rendered in the map view}.
         ******************************************************************************************************************************************************/
        @Nonnull
        public MapArea getArea()
          {
            return model.getArea();
          }
      }

    /***********************************************************************************************************************************************************
     * Options for creating a {@code MapView}. Don't directly create an instance of this class, but use {@link MapView#options()} and then set the desired
     * attributes with a {@code with*()} method.
     * @param   cacheFolder         the {@link Path} of the folder where cached tiles are stored
     * @param   downloadAllowed     whether downloading tiles is allowed
     * @param   poolSize            the number of parallel thread of the tile downloader
     * @param   tileQueueCapacity   the capacity of the tile queue
     * @param   waitingImage        a {@link Supplier} of the image to be rendered while the tile bitmap has not been downloaded yet
     * @param   executorService     the {@link ExecutorService} to load tiles in backgrounds
     **********************************************************************************************************************************************************/
    @API(status = STABLE)
    @With
    public record Options(@Nonnull Path cacheFolder,
                          boolean downloadAllowed,
                          int poolSize,
                          int tileQueueCapacity,
                          @Nonnull Supplier<Image> waitingImage,
                          @Nonnull Function<Integer, ExecutorService> executorService) {}

    /** The tile source. */
    @Nonnull
    private final SimpleObjectProperty<TileSource> tileSource;

    /** The coordinates at the center of the map view. */
    @Nonnull
    private final SimpleObjectProperty<MapCoordinates> center;

    /** The zoom level. */
    @Nonnull
    private final RangeLimitedDoubleProperty zoom;

    /** The minimum zoom level. */
    @Nonnull
    private final SimpleDoubleProperty minZoom;

    /** The maximum zoom level. */
    @Nonnull
    private final SimpleDoubleProperty maxZoom;

    /** The coordinates corresponding to the mouse position on the map. */
    @Nonnull
    private final SimpleObjectProperty<MapCoordinates> mouseCoordinates;

    /** The rectangular area in the view. */
    @Nonnull
    private final SimpleObjectProperty<MapArea> area;

    /** The list of names of overlays. */
    private final SimpleListProperty<String> overlayNamesProperty = new SimpleListProperty<>(observableList(new ArrayList<>()));

    /** The model for this control. */
    @Nonnull
    private final MapViewModel model;

    /** The tile grid that the rendering relies upon. */
    @Nonnull
    private final TileGrid tileGrid;

    /** A cache for tiles. */
    @Nonnull
    private final TileCache tileCache;

    /** Whether double click re-centers the map to the clicked point. */
    @Getter @Setter
    private boolean recenterOnDoubleClick = true;

    /** Whether the vertical scroll gesture should zoom. */
    @Getter @Setter
    private boolean scrollToZoom = false;

    /** The duration of the re-centering animation. */
    @Getter @Setter
    private Duration recenterDuration = Duration.millis(200);

    /** True if a zoom operation is in progress. */
    private boolean zooming;

    /** True if a drag operation is in progress. */
    private boolean dragging;

    /** The latest x coordinate in drag. */
    private double dragX;

    /** The latest y coordinate in drag. */
    private double dragY;

    /** The latest value in scroll. */
    private double scroll;

    /** A guard to manage reentrant calls to {@link #setCenterAndZoom(MapCoordinates, double)}. */
    private boolean reentrantGuard;

    /***********************************************************************************************************************************************************
     * Creates a new instance.
     * @param   options         options for the control
     **********************************************************************************************************************************************************/
    @SuppressWarnings("this-escape") @SuppressFBWarnings({"MALICIOUS_CODE", "CT_CONSTRUCTOR_THROW"})
    public MapView (@Nonnull final Options options)
      {
        if (!Platform.isFxApplicationThread())
          {
            throw new IllegalStateException("Must be instantiated on JavaFX thread");
          }

        tileSource = new SimpleObjectProperty<>(this, "tileSource", DEFAULT_TILE_SOURCE);
        model = new MapViewModel(tileSource.get());
        tileCache = new TileCache(options);
        tileGrid = new TileGrid(this, model, tileSource, tileCache);
        center = new SimpleObjectProperty<>(this, "center", tileGrid.getCenter());
        zoom = new RangeLimitedDoubleProperty(this, "zoom", model.zoom(), tileSource.get().getMinZoomLevel(), tileSource.get().getMaxZoomLevel());
        minZoom = new SimpleDoubleProperty(this, "minZoom", tileSource.get().getMinZoomLevel());
        maxZoom = new SimpleDoubleProperty(this, "maxZoom", tileSource.get().getMaxZoomLevel());
        mouseCoordinates = new SimpleObjectProperty<>(this, "mouseCoordinates", MapCoordinates.of(0, 0));
        area = new SimpleObjectProperty<>(this, "area", MapArea.of(0, 0, 0, 0));
        tileSource.addListener((_1, _2, _3) -> onTileSourceChanged());
        center.addListener((_1, _2, newValue) -> setCenterAndZoom(newValue, zoom.get()));
        zoom.addListener((_1, _2, newValue) -> setCenterAndZoom(center.get(), newValue.intValue()));
        getChildren().add(tileGrid);
        AnchorPane.setLeftAnchor(tileGrid, 0d);
        AnchorPane.setRightAnchor(tileGrid, 0d);
        AnchorPane.setTopAnchor(tileGrid, 0d);
        AnchorPane.setBottomAnchor(tileGrid, 0d);
        setOnMouseClicked(this::onMouseClicked);
        setOnZoom(this::onZoom);
        setOnZoomStarted(this::onZoomStarted);
        setOnZoomFinished(this::onZoomFinished);
        setOnMouseMoved(this::onMouseMoved);
        setOnScroll(this::onScroll);
        tileGrid.setOnMousePressed(this::onMousePressed);
        tileGrid.setOnMouseReleased(this::onMouseReleased);
        tileGrid.setOnMouseDragged(this::onMouseDragged);
      }

    /***********************************************************************************************************************************************************
     * {@return a new set of default options}.
     * @see                   Options
     **********************************************************************************************************************************************************/
    @Nonnull
    public static Options options()
      {
        return new Options(Path.of(System.getProperty("java.io.tmpdir")),
                           true,
                           DEFAULT_TILE_POOL_SIZE,
                           DEFAULT_TILE_QUEUE_CAPACITY,
                           WAITING_IMAGE,
                           Executors::newFixedThreadPool);
      }

    /***********************************************************************************************************************************************************
     * {@return the tile source}.
     * @see                   #setTileSource(TileSource)
     * @see                   #tileSourceProperty()
     **********************************************************************************************************************************************************/
    @Nonnull
    public final TileSource getTileSource()
      {
        return tileSource.get();
      }

    /***********************************************************************************************************************************************************
     * Sets the tile source. Changing the tile source might change the zoom level to make sure it is within the limits of the new source.
     * @param   tileSource    the tile source
     * @see                   #getTileSource()
     * @see                   #tileSourceProperty()
     **********************************************************************************************************************************************************/
    public final void setTileSource (@Nonnull final TileSource tileSource)
      {
        this.tileSource.set(tileSource);
      }

    /***********************************************************************************************************************************************************
     * {@return the tile source property}.
     * @see                   #setTileSource(TileSource)
     * @see                   #getTileSource()
     **********************************************************************************************************************************************************/
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP")
    public final ObjectProperty<TileSource> tileSourceProperty()
      {
        return tileSource;
      }

    /***********************************************************************************************************************************************************
     * {@return the center coordinates}.
     * @see                   #setCenter(MapCoordinates)
     * @see                   #centerProperty()
     **********************************************************************************************************************************************************/
    @Nonnull
    public final MapCoordinates getCenter()
      {
        return center.get();
      }

    /***********************************************************************************************************************************************************
     * Sets the coordinates to show at the center of the map.
     * @param   center        the center coordinates
     * @see                   #getCenter()
     * @see                   #centerProperty()
     **********************************************************************************************************************************************************/
    public final void setCenter (@Nonnull final MapCoordinates center)
      {
        this.center.set(center);
      }

    /***********************************************************************************************************************************************************
     * {@return the center property}.
     * @see                   #getCenter()
     * @see                   #setCenter(MapCoordinates)
     **********************************************************************************************************************************************************/
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP")
    public final ObjectProperty<MapCoordinates> centerProperty()
      {
        return center;
      }

    /***********************************************************************************************************************************************************
     * {@return the zoom level}.
     * @see                   #setZoom(double)
     * @see                   #zoomProperty()
     **********************************************************************************************************************************************************/
    public final double getZoom()
      {
        return zoom.get();
      }

    /***********************************************************************************************************************************************************
     * Sets the zoom level.
     * @param   zoom    the zoom level
     * @see                   #getZoom()
     * @see                   #zoomProperty()
     **********************************************************************************************************************************************************/
    public final void setZoom (final double zoom)
      {
        this.zoom.set(zoom);
      }

    /***********************************************************************************************************************************************************
     * {@return the zoom level property}.
     * @see                   #getZoom()
     * @see                   #setZoom(double)
     **********************************************************************************************************************************************************/
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP")
    public final DoubleProperty zoomProperty()
      {
        return zoom;
      }

    /***********************************************************************************************************************************************************
     * {@return the min zoom level}.
     * @see                   #minZoomProperty()
     **********************************************************************************************************************************************************/
    public final double getMinZoom()
      {
        return minZoom.get();
      }

    /***********************************************************************************************************************************************************
     * {@return the min zoom level property}.
     * @see                   #getMinZoom()
     **********************************************************************************************************************************************************/
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP")
    public final ReadOnlyDoubleProperty minZoomProperty()
      {
        return minZoom;
      }

    /***********************************************************************************************************************************************************
     * {@return the max zoom level}.
     * @see                   #maxZoomProperty()
     **********************************************************************************************************************************************************/
    public final double getMaxZoom()
      {
        return maxZoom.get();
      }

    /***********************************************************************************************************************************************************
     * {@return the max zoom level property}.
     * @see                   #getMaxZoom()
     **********************************************************************************************************************************************************/
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP")
    public final ReadOnlyDoubleProperty maxZoomProperty()
      {
        return maxZoom;
      }

    /***********************************************************************************************************************************************************
     * {@return the coordinates corresponding to the point where the mouse is}.
     **********************************************************************************************************************************************************/
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP")
    public final ObjectProperty<MapCoordinates> mouseCoordinatesProperty ()
      {
        return mouseCoordinates;
      }

    /***********************************************************************************************************************************************************
     * {@return the area rendered on the map}.
     * @see                   #areaProperty()
     * @see                   #fitArea(MapArea)
     **********************************************************************************************************************************************************/
    @Nonnull
    public final MapArea getArea()
      {
        return area.get();
      }

    /***********************************************************************************************************************************************************
     * {@return the area rendered on the map}.
     * @see                   #getArea()
     * @see                   #fitArea(MapArea)
     **********************************************************************************************************************************************************/
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP")
    public final ObjectProperty<MapArea> areaProperty()
      {
        return area;
      }

    /***********************************************************************************************************************************************************
     * Fits the zoom level and centers the map so that the two corners are visible.
     * @param   area          the area to fit
     * @see                   #getArea()
     * @see                   #areaProperty()
     **********************************************************************************************************************************************************/
    public void fitArea (@Nonnull final MapArea area)
      {
        log.debug("fitArea({})", area);
        setCenterAndZoom(area.getCenter(), model.computeFittingZoom(area));
      }

    /***********************************************************************************************************************************************************
     * {@return the scale of the map in meters per pixel}.
     **********************************************************************************************************************************************************/
    // @Nonnegative
    public double getMetersPerPixel()
      {
        return tileSource.get().metersPerPixel(tileGrid.getCenter(), zoom.get());
      }

    /***********************************************************************************************************************************************************
     * {@return a point on the map corresponding to the given coordinates}.
     * @param  coordinates    the coordinates
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapViewPoint coordinatesToPoint (@Nonnull final MapCoordinates coordinates)
      {
        return model.coordinatesToMapViewPoint(coordinates);
      }

    /***********************************************************************************************************************************************************
     * {@return the coordinates corresponding to a given point on the map}.
     * @param   point         the point on the map
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapCoordinates pointToCoordinates (@Nonnull final MapViewPoint point)
      {
        return model.mapViewPointToCoordinates(point);
      }

    /***********************************************************************************************************************************************************
     * Adds an overlay, passing a callback that will be responsible for rendering the overlay, when needed.
     * @param   name          the name of the overlay
     * @param   creator       the overlay creator
     * @see                   OverlayHelper
     **********************************************************************************************************************************************************/
    public void addOverlay (@Nonnull final String name, @Nonnull final Consumer<OverlayHelper> creator)
      {
        tileGrid.addOverlay(name, creator);
        overlayNamesProperty.add(name);
      }

    /***********************************************************************************************************************************************************
     * Removes an overlay.
     * @param   name          the name of the overlay to remove
     **********************************************************************************************************************************************************/
    public void removeOverlay (@Nonnull final String name)
      {
        tileGrid.removeOverlay(name);
        overlayNamesProperty.remove(name);
      }

    /***********************************************************************************************************************************************************
     * Removes all overlays.
     **********************************************************************************************************************************************************/
    public void removeAllOverlays()
      {
        tileGrid.removeAllOverlays();
        overlayNamesProperty.clear();
      }

    /***********************************************************************************************************************************************************
     * {@return a list of overlay names}.
     * @since   1.0-ALPHA-3
     **********************************************************************************************************************************************************/
    @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP")
    public final ReadOnlyListProperty<String> getOverlayNamesProperty()
      {
        return overlayNamesProperty;
      }

    /***********************************************************************************************************************************************************
     * {@return a list of overlay names}.
     * @since   1.0-ALPHA-3
     **********************************************************************************************************************************************************/
    @Nonnull
    public final List<String> getOverlayNames()
      {
        return overlayNamesProperty.get();
      }

    /***********************************************************************************************************************************************************
     * Sets both the center and the zoom level. This method has got a reentrant protection since it touches the {@link #center} and {@link #zoom} properties,
     * that in turn will fire events that call back this method, the first time with the previous zoom level. There's no way to change them atomically.
     * @param   center        the center
     * @param   zoom          the zoom level
     **********************************************************************************************************************************************************/
    private void setCenterAndZoom (@Nonnull final MapCoordinates center, final double zoom)
      {
        if (!reentrantGuard)
          {
            try
              {
                reentrantGuard = true;
                log.trace("setCenterAndZoom({}, {})", center, zoom);

                if (!center.equals(tileGrid.getCenter()) || doubleToLongBits(zoom) != doubleToLongBits(model.zoom()))
                  {
                    tileCache.retainPendingTiles((int)zoom);
                    tileGrid.setCenterAndZoom(center, zoom);
                    this.center.set(center);
                    this.zoom.set(zoom);
                    area.set(model.getArea());
                  }
              }
            finally // defensive
              {
                reentrantGuard = false;
              }
          }
      }

    /***********************************************************************************************************************************************************
     * Translate the map center by the specified amount.
     * @param   dx            the horizontal amount
     * @param   dy            the vertical amount
     **********************************************************************************************************************************************************/
    private void translateCenter (final double dx, final double dy)
      {
        tileGrid.translate(dx, dy);
        center.set(model.center());
        area.set(model.getArea());
      }

    /***********************************************************************************************************************************************************
     * This method is called when the tile source has been changed.
     **********************************************************************************************************************************************************/
    private void onTileSourceChanged()
      {
        final var minZoom = tileSourceProperty().get().getMinZoomLevel();
        final var maxZoom = tileSourceProperty().get().getMaxZoomLevel();
        zoom.setLimits(minZoom, maxZoom);
        this.minZoom.set(minZoom);
        this.maxZoom.set(maxZoom);
        setNeedsLayout(true);
      }

    /***********************************************************************************************************************************************************
     * Mouse callback.
     **********************************************************************************************************************************************************/
    private void onMousePressed (@Nonnull final MouseEvent event)
      {
        if (!zooming)
          {
            dragging = true;
            dragX = event.getSceneX();
            dragY = event.getSceneY();
            log.trace("onMousePressed: {} {}", dragX, dragY);
          }
      }

    /***********************************************************************************************************************************************************
     * Mouse callback.
     **********************************************************************************************************************************************************/
    private void onMouseReleased (@Nonnull final MouseEvent ignored)
      {
        log.trace("onMouseReleased");
        dragging = false;
      }

    /***********************************************************************************************************************************************************
     * Mouse callback.
     **********************************************************************************************************************************************************/
    private void onMouseDragged (@Nonnull final MouseEvent event)
      {
        if (!zooming && dragging)
          {
            translateCenter(event.getSceneX() - dragX, event.getSceneY() - dragY);
            dragX = event.getSceneX();
            dragY = event.getSceneY();
          }
      }

    /***********************************************************************************************************************************************************
     * Mouse callback.
     **********************************************************************************************************************************************************/
    private void onMouseClicked (@Nonnull final MouseEvent event)
      {
        if (recenterOnDoubleClick && (event.getClickCount() == 2))
          {
            final var delta = Translation.of(getWidth() / 2 - event.getX(), getHeight() / 2 - event.getY());
            final var target = new SimpleObjectProperty<>(Translation.of(0, 0));
            target.addListener((__, oldValue, newValue) ->
                                       translateCenter(newValue.x() - oldValue.x(), newValue.y() - oldValue.y()));
            animate(target, Translation.of(0, 0), delta, recenterDuration);
          }
      }

    /***********************************************************************************************************************************************************
     * Mouse callback.
     **********************************************************************************************************************************************************/
    private void onMouseMoved (@Nonnull final MouseEvent event)
      {
        mouseCoordinates.set(pointToCoordinates(MapViewPoint.of(event)));
      }

    /***********************************************************************************************************************************************************
     * Gesture callback.
     **********************************************************************************************************************************************************/
    private void onZoomStarted (@Nonnull final ZoomEvent event)
      {
        log.trace("onZoomStarted({})", event);
        zooming = true;
        dragging = false;
      }

    /***********************************************************************************************************************************************************
     * Gesture callback.
     **********************************************************************************************************************************************************/
    private void onZoomFinished (@Nonnull final ZoomEvent event)
      {
        log.trace("onZoomFinished({})", event);
        zooming = false;
      }

    /***********************************************************************************************************************************************************
     * Gesture callback.
     **********************************************************************************************************************************************************/
    private void onZoom (@Nonnull final ZoomEvent event)
      {
        log.trace("onZoom({})", event);
      }

    /***********************************************************************************************************************************************************
     * Mouse callback.
     **********************************************************************************************************************************************************/
    private void onScroll (@Nonnull final ScrollEvent event)
      {
        if (scrollToZoom)
          {
            log.info("onScroll({})", event);
            final var amount = -Math.signum(Math.floor(event.getDeltaY() - scroll));
            scroll = event.getDeltaY();
            log.debug("zoom change for scroll: {}", amount);
            zoom.set(Math.round(zoom.get() + amount));
          }
      }

    /***********************************************************************************************************************************************************
     * Animates a property. If the duration is zero, the property is immediately set.
     * @param   <T>           the static type of the property to animate
     * @param   target        the property to animate
     * @param   startValue    the start value of the property
     * @param   endValue      the end value of the property
     * @param   duration      the duration of the animation
     **********************************************************************************************************************************************************/
    private static <T extends Interpolatable<T>> void animate (@Nonnull final ObjectProperty<T> target,
                                                               @Nonnull final T startValue,
                                                               @Nonnull final T endValue,
                                                               @Nonnull final Duration duration)
      {
        if (duration.equals(ZERO))
          {
            target.set(endValue);
          }
        else
          {
            final var start = new KeyFrame(ZERO, new KeyValue(target, startValue));
            final var end = new KeyFrame(duration, new KeyValue(target, endValue, Interpolator.EASE_OUT));
            new Timeline(start, end).play();
          }
      }

    // FIXME: on close shut down the tile cache executor service.
  }
