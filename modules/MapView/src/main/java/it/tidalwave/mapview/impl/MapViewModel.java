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
package it.tidalwave.mapview.impl;

import jakarta.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.net.URI;
import it.tidalwave.mapview.MapArea;
import it.tidalwave.mapview.MapCoordinates;
import it.tidalwave.mapview.MapPoint;
import it.tidalwave.mapview.MapViewPoint;
import it.tidalwave.mapview.TileSource;
import it.tidalwave.mapview.javafx.MapView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/***************************************************************************************************************************************************************
 *
 * A model (independent of UI technology) that provides parameters for implementing a tile grid based map renderer.
 *
 * To understand how this class works, three coordinate systems are to be understood:
 *
 * <ul>
 *   <li>classic <b>coordinates</b> composed of latitude and longitude, modelled by the class {@link MapCoordinates}</li>;
 *   <li><b>map coordinates</b> that are pixel coordinates in the huge, untiled bitmap that represents a map at a given zoom level; they are
 *      modelled by the class {@link MapPoint}.
 *   <li><b>map view coordinates</b> that are pixel coordinates in the map view, modelled by the class {@link MapViewPoint}</li>;
 * </ul>
 *
 * The tile grid is always created large enough to cover the whole clip area, plus a "margin" frame of tiles of {@code MARGIN} tiles. This allows to drag
 * the map at least by the tile size amount before being forced to reload tiles.
 *
 * The rendering component must:
 *
 * <ul>
 * <li>call the {@link #updateGridSize(double, double)} method specifying the size of the rendering region; this method must be called again every time
 * the rendering region changes size. It returns {@code true} when the grid has been recomputed (because it has been moved so far that a new row or
 * column of tiles must be downloaded).</li>
 * <li>call the {@link #setCenterAndZoom(MapCoordinates, double)} or {@link #setCenterAndZoom(MapPoint, double)} methods to set point that the center of
 * the rendered area, using either coordinates or map points, and the zoom level.
 * </ul>
 *
 * After doing that, this class computes:
 *
 * <ul>
 *   <li>the center point in the coordinate system ({@link #center()};</li>
 *   <li>the center point in the map coordinate system ({@link #pointCenter()})</li>
 *   <li>the coordinates (colum and row) of the tile that is rendered at the center ({@link #tileCenter())</li>
 *   <li>the offset in pixels that the center tile must be applied to ({#{@link #tileOffset()}}</li>
 *   <li>the offset in pixels that the grid must be applied to ({#{@link #gridOffset()}}</li>
 *   <li>the number of columns in the grid ({@link #columns()})</li>
 *   <li>the number of rows in the grid ({@link #rows()} ()})</li>
 * </ul>
 *
 * At this point the implementor must invoke {@link #iterateOnGrid(BiConsumer)} that will call back passing the URL and the grid position for each tile.
 *
 * Two further methods are available:
 *
 * <ul>
 *   <li>{@link #getArea()} returns the coordinates of the rendered area;</li>
 *   <li>{@link #computeFittingZoom(MapArea)} returns the maximum zoom level that allows to fully render the given area.</li>
 * </ul>
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@Accessors(fluent = true) @Getter @Slf4j
public class MapViewModel
  {
    @RequiredArgsConstructor(staticName = "of")
    static class TileInfo
      {
        @Delegate @Nonnull
        private final TilePos pos;

        @Getter @Nonnull
        private final URI uri;

        @Override @Nonnull
        public String toString()
          {
            return String.format("(%d, %d) - %s", pos.column, pos.row, uri);
          }
      }

    private static final int MARGIN = 1;

    /** The source of tiles. */
    @Nonnull
    private TileSource tileSource;

    /** The zoom level. */
    private double zoom = 1;

    /** The coordinates rendered at the center of the map — note: this is _not_ the center of the TileGrid, since there is an offset. */
    private MapCoordinates center = MapCoordinates.of(0.0, 0.0);

    /** The same of above, but expressed in terms of pixel coordinates relative to the map as a huge, untiled image. */
    private MapPoint pointCenter = MapPoint.of(0.0, 0.0);

    /** The position of the tile that corresponds to the coordinates. */
    private TilePos tileCenter;

    /** The offset inside the tile that corresponds to the coordinates. */
    private TileOffset tileOffset = TileOffset.of(0.0, 0.0);

    private TileOffset gridOffset = TileOffset.of(0.0, 0.0);

    /** How many columns in the TileGrid. */
    private int columns;

    /** How many rows in the TileGrid. */
    private int rows;

    /** The width of the MapView. */
    private double mapViewWidth;

    /** The height of the MapView. */
    private double mapViewHeight;

    /***********************************************************************************************************************************************************
     * @param   tileSource        the tile source
     **********************************************************************************************************************************************************/
    public MapViewModel (@Nonnull final TileSource tileSource)
      {
        this.tileSource = tileSource;
      }

    /***********************************************************************************************************************************************************
     * Changes the tile source.
     * @param   tileSource        the new tile source
     **********************************************************************************************************************************************************/
    public void setTileSource (@Nonnull final TileSource tileSource)
      {
        this.tileSource = tileSource;
        recompute();
      }

    /***********************************************************************************************************************************************************
     * Set the center coordinates and the zoom level.
     * @param   coordinates       the coordinates
     * @param   zoom              the zoom level
     **********************************************************************************************************************************************************/
    public void setCenterAndZoom (@Nonnull final MapCoordinates coordinates, final double zoom)
      {
        this.center = coordinates;
        this.zoom = Math.floor(zoom);
        pointCenter = tileSource.coordinatesToMapPoint(coordinates, zoom);
        recompute();
      }

    /***********************************************************************************************************************************************************
     * Set the center point and the zoom level.
     * @param   mapPoint          the mapPoint
     * @param   zoom              the zoom level
     **********************************************************************************************************************************************************/
    public void setCenterAndZoom (@Nonnull final MapPoint mapPoint, final double zoom)
      {
        this.pointCenter = mapPoint;
        this.zoom = Math.floor(zoom);
        center = tileSource.mapPointToCoordinates(pointCenter, zoom);
        recompute();
      }

    /***********************************************************************************************************************************************************
     * Updates the size of the grid given the size of the {@link MapView}.
     * @param   mapViewWidth      the mapViewWidth of the {@code MapView}
     * @param   mapViewHeight     the mapViewHeight of the {@code MapView}
     * @return                    {@code true} if the grid size has changed
     **********************************************************************************************************************************************************/
    public boolean updateGridSize (final double mapViewWidth, final double mapViewHeight)
      {
        this.mapViewWidth = mapViewWidth;
        this.mapViewHeight = mapViewHeight;
        final var prevColumns = columns;
        final var prevRows = rows;
        final var tileSize = tileSource.getTileSize();
        columns = greaterOdd((int)((mapViewWidth + tileSize - 1) / tileSize)) + MARGIN * 2;
        rows = greaterOdd((int)((mapViewHeight + tileSize - 1) / tileSize)) + MARGIN * 2;
        return (prevColumns != columns) || (prevRows != rows);
      }

    /***********************************************************************************************************************************************************
     * Iterates over all the tiles providing the URL of the image for each tile.
     * @param   consumer    the call back
     **********************************************************************************************************************************************************/
    public void iterateOnGrid (@Nonnull final BiConsumer<? super TilePos, ? super URI> consumer)
      {
        final var grid = getGrid();

        for (int r = 0; r < grid.length; r++)
          {
            for (int c = 0; c < grid[r].length; c++)
              {
                consumer.accept(TilePos.of(c, r), grid[r][c].uri);
              }
          }
      }

    /***********************************************************************************************************************************************************
     * {@return the zoom level to apply in order to accomodate the given area to the rendered region}.
     * @param   area      the area to fit
     **********************************************************************************************************************************************************/
    public int computeFittingZoom (@Nonnull final MapArea area)
      {
        log.info("computeFittingZoom({})", area);
        final var center = area.getCenter();
        final var otherModel = new MapViewModel(tileSource); // a temporary model to compute various attempts
        otherModel.updateGridSize(mapViewWidth, mapViewHeight);

        for (int zoomAttempt = tileSource.getMaxZoomLevel(); zoomAttempt >= tileSource.getMinZoomLevel(); zoomAttempt--)
          {
            otherModel.setCenterAndZoom(center, zoomAttempt);
            final var mapArea = otherModel.getArea();

            if (mapArea.contains(area))
              {
                return zoomAttempt;
              }
          }

        return 1;
      }

    /***********************************************************************************************************************************************************
     * {@return the smallest rectangular area which encloses the area rendered in the map view}.
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapArea getArea()
      {
        final var nw = mapViewPointToCoordinates(MapViewPoint.of(0, 0));
        final var se = mapViewPointToCoordinates(MapViewPoint.of(mapViewWidth, mapViewHeight));
        return MapArea.of(nw.latitude(), se.longitude(), se.latitude(), nw.longitude());
      }

    /***********************************************************************************************************************************************************
     * Recomputes the tile center, offset and the grid offset.
     **********************************************************************************************************************************************************/
    public void recompute()
      {
        // both pixel and tile h-axis goes left -> right, v-axis top -> bottom
        final var tileSize = tileSource.getTileSize();
        tileCenter = TilePos.of((int)(pointCenter.x() / tileSize), (int)(pointCenter.y() / tileSize));
        tileOffset = TileOffset.of(pointCenter.x() % tileSize, pointCenter.y() % tileSize);
        gridOffset = TileOffset.of(-tileOffset.x() - tileSize * columns / 2.0 + mapViewWidth / 2.0 + tileSize / 2.0,
                                   -tileOffset.y() - tileSize * rows / 2.0 + mapViewHeight / 2.0 + tileSize / 2.0);
        log.trace("center: {}, {} - tile center: {} - tile offset: {} - grid offset: {}", center, pointCenter, tileCenter, tileOffset, gridOffset);
      }

    /***********************************************************************************************************************************************************
     * {@return the point relative to the map view corresponding to the given coordinates}.
     * @param   coordinates         the coordinates
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapViewPoint coordinatesToMapViewPoint (@Nonnull final MapCoordinates coordinates)
      {
        return toMapViewPoint(tileSource.coordinatesToMapPoint(coordinates, zoom));
      }

    /***********************************************************************************************************************************************************
     * {@return the coordinates corresponding to the given mapViewPoint on the map viewer}.
     * @param   mapViewPoint        the mapViewPoint relative to the map view: (0,0) is the top left and (w,h) is the bottom right
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapCoordinates mapViewPointToCoordinates (@Nonnull final MapViewPoint mapViewPoint)
      {
        return tileSource.mapPointToCoordinates(toMapPoint(mapViewPoint), zoom);
      }

    /***********************************************************************************************************************************************************
     * {@return the current grid of tile info}.
     **********************************************************************************************************************************************************/
    @Nonnull
    private TileInfo[][] getGrid()
      {
        final int max = (int)Math.pow(2, zoom);
        // (left, top) tile must be adjusted for half the tile array size
        final int left = tileCenter.column() - columns / 2;
        final int top = tileCenter.row() - rows / 2;        // rows go top to bottom
        final var grid = new TileInfo[rows][columns];

        for (int r = 0; r < rows; r++)
          {
            for (int c = 0; c < columns; c++)
              {
                final var column = Math.floorMod(left + c, max);
                final var row = Math.floorMod(top + r, max);
                final var uri = tileSource.getTileUri(column, row, (int)zoom);
                grid[r][c] = TileInfo.of(TilePos.of(column, row), uri);
              }
          }

        return grid;
      }

    /***********************************************************************************************************************************************************
     * {@return a point in map view coordinates corresponding to a point in map coordinates}.
     * @param   mapPoint            the point
     **********************************************************************************************************************************************************/
    @Nonnull
    private MapViewPoint toMapViewPoint (@Nonnull final MapPoint mapPoint)
      {
        return MapViewPoint.of(mapPoint.translated(mapViewWidth / 2.0 - pointCenter.x(), mapViewHeight / 2.0 - pointCenter.y()));
      }

    /***********************************************************************************************************************************************************
     * {@return a point in map coordinates corresponding to a point in map view coordinates}.
     * @param   mapViewPoint        the point
     **********************************************************************************************************************************************************/
    @Nonnull
    private MapPoint toMapPoint (@Nonnull final MapViewPoint mapViewPoint)
      {
        return mapViewPoint.translated(pointCenter.x() - mapViewWidth / 2, pointCenter.y() - mapViewHeight / 2);
      }

    /***********************************************************************************************************************************************************
     * {@return the first greater odd integer of the given number}.
     * @param   n   the number
     **********************************************************************************************************************************************************/
    /* visible for testing */ static int greaterOdd (final int n)
      {
        return n + ((n % 2 == 0) ? 1 : 0);
      }
  }

