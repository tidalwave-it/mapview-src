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
package it.tidalwave.mapviewer.impl;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import it.tidalwave.mapviewer.MapArea;
import it.tidalwave.mapviewer.MapCoordinates;
import it.tidalwave.mapviewer.MapPoint;
import it.tidalwave.mapviewer.OpenStreetMapTileSource;
import it.tidalwave.mapviewer.TileSource;
import it.tidalwave.mapviewer.javafx.impl.TilePos;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static it.tidalwave.mapviewer.impl.Distances.distance;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class MapViewModelTest
  {
    private final TileSource tileSource = new OpenStreetMapTileSource();

    private MapViewModel underTest;

    /**********************************************************************************************************************************************************/
    @BeforeTest
    public void setup()
      {
        underTest = new MapViewModel(tileSource);
      }

    /**********************************************************************************************************************************************************/
    @Test(dataProvider = "sizes")
    public void test_updateGridSize (final double width, final double height, final int expectedColumns, final int expectedRows)
      {
        // when
        underTest.updateGridSize(width, height);
        // then
        assertThat(underTest.columns()).isEqualTo(expectedColumns);
        assertThat(underTest.rows()).isEqualTo(expectedRows);
      }

    /**********************************************************************************************************************************************************/
    @DataProvider
    private static Object[][] sizes()
      {
        return new Object[][]
          {
            {  256, 256, 3, 3 },
            {  257, 257, 5, 5 },
            {  512, 512, 5, 5 },
            {  800, 600, 7, 5 },
            { 1000, 300, 7, 5 },
            { 1500, 820, 9, 7 }
          };
      }

    /**********************************************************************************************************************************************************/
    @Test(dataProvider = "coordinates")
    public void test_setCenterAndZoomCoordinates (final int zoomLevel,
                                                  final MapCoordinates center,
                                                  final TilePos tileCenter,
                                                  final MapPoint pixelCenter,
                                                  final TileOffset tileOffset)
      {
        // when
        underTest.setCenterAndZoom(center, zoomLevel);
        // then
        assertThat(underTest.zoom()).isEqualTo(zoomLevel);
        assertThat(underTest.center()).isEqualTo(center);
        assertThat(distance(underTest.pointCenter(), pixelCenter)).isLessThan(1);
        assertThat(distance(underTest.tileOffset(), tileOffset)).isLessThan(1);
        assertThat(underTest.tileCenter()).isEqualTo(tileCenter);
      }

    /**********************************************************************************************************************************************************/
    @Test(dataProvider = "coordinates")
    public void test_setCenterAndZoomPixels (final int zoomLevel,
                                             final MapCoordinates center,
                                             final TilePos tileCenter,
                                             final MapPoint pixelCenter,
                                             final TileOffset tileOffset)
      {
        // when
        underTest.setCenterAndZoom(pixelCenter, zoomLevel);
        // then
        assertThat(underTest.zoom()).isEqualTo(zoomLevel);
        assertThat(distance(underTest.center(), center)).isLessThan(5);
        assertThat(distance(underTest.pointCenter(), pixelCenter)).isLessThan(1);
        assertThat(distance(underTest.tileOffset(), tileOffset)).isLessThan(1);
        assertThat(underTest.tileCenter()).isEqualTo(tileCenter);
      }

    /**********************************************************************************************************************************************************/
    @DataProvider
    private static Object[][] coordinates()
      {
        return new Object[][]
          {
            // (0,0) (1,0)
            // (0,1) (1,1)
            // z  coordinates                                 tile coordinates                   pixel coordinates                   tile offset
            {  1, MapCoordinates.of( 0,  0), TilePos.of(   1,    1), MapPoint.of(   256,    256), TileOffset.of( 0,   0) },
            {  2, MapCoordinates.of( 0,  0), TilePos.of(   2,    2), MapPoint.of(   512,    512), TileOffset.of( 0,   0) },
            {  7, MapCoordinates.of( 0,  0), TilePos.of(  64,   64), MapPoint.of( 16384,  16384), TileOffset.of( 0,   0) },
            { 12, MapCoordinates.of( 0,  0), TilePos.of(2048, 2048), MapPoint.of(524288, 524288), TileOffset.of( 0,   0) },

            { 12, MapCoordinates.of(45, 11), TilePos.of(2173, 1473), MapPoint.of(556328, 377199), TileOffset.of(40, 111) },
            { 12, MapCoordinates.of(45, 45), TilePos.of(2560, 1473), MapPoint.of(655360, 377199), TileOffset.of( 0, 111) },

            { 18, MapCoordinates.of(44.4, 8.95), TilePos.of(137589, 94914), MapPoint.of(35222832.924444, 24298096.066372), 
              TileOffset.of(48.924444, 112.066372) },
          };
      }

    /**********************************************************************************************************************************************************/
    @Test(dataProvider = "grids")
    public void test_iterateOnGrid (final int zoomLevel, final MapCoordinates center, final String expectedGrid)
      {
        // given
        underTest.updateGridSize(256, 256); // 3x3 grid
        underTest.setCenterAndZoom(center, zoomLevel);
        final var list = new ArrayList<String>();
        // when
        underTest.iterateOnGrid((pos, url) -> list.add(String.format("(%d, %d) - %s", pos.column(), pos.row(), url)));
        // then
        assertThat(String.join("\n", list) + "\n").isEqualTo(expectedGrid);
      }

    /**********************************************************************************************************************************************************/
    @DataProvider
    private static Object[][] grids()
      {
        return new Object[][]
          {
            // [0,1] [1,1] [0,1]  should be this, it's not the same
            // (0,0) (1,0) [0,0]
            // (0,1) (1,1) [0,1]
            {1, MapCoordinates.of(0, 0),
             """
              (0, 0) - https://tile.openstreetmap.org/1/0/0.png
              (1, 0) - https://tile.openstreetmap.org/1/1/0.png
              (2, 0) - https://tile.openstreetmap.org/1/0/0.png
              (0, 1) - https://tile.openstreetmap.org/1/0/1.png
              (1, 1) - https://tile.openstreetmap.org/1/1/1.png
              (2, 1) - https://tile.openstreetmap.org/1/0/1.png
              (0, 2) - https://tile.openstreetmap.org/1/0/0.png
              (1, 2) - https://tile.openstreetmap.org/1/1/0.png
              (2, 2) - https://tile.openstreetmap.org/1/0/0.png
              """ },
            {18, MapCoordinates.of(44.4, 8.95),   // https://www.openstreetmap.org/#map=18/44.4/8.95
             """
              (0, 0) - https://tile.openstreetmap.org/18/137588/94913.png
              (1, 0) - https://tile.openstreetmap.org/18/137589/94913.png
              (2, 0) - https://tile.openstreetmap.org/18/137590/94913.png
              (0, 1) - https://tile.openstreetmap.org/18/137588/94914.png
              (1, 1) - https://tile.openstreetmap.org/18/137589/94914.png
              (2, 1) - https://tile.openstreetmap.org/18/137590/94914.png
              (0, 2) - https://tile.openstreetmap.org/18/137588/94915.png
              (1, 2) - https://tile.openstreetmap.org/18/137589/94915.png
              (2, 2) - https://tile.openstreetmap.org/18/137590/94915.png
              """ }
         };
      }

    /**********************************************************************************************************************************************************/
    @Test(dataProvider = "grids2")
    public void test_iterateOnGrid2 (final int zoomLevel, final MapPoint center, final String expectedGrid)
      {
        // given
        underTest.updateGridSize(256, 256); // 3x3 grid
        underTest.setCenterAndZoom(center, zoomLevel);
        final var list = new ArrayList<String>();
        // when
        underTest.iterateOnGrid((pos, url) -> list.add(String.format("(%d, %d) - %s", pos.column(), pos.row(), url)));
        // then
        assertThat(String.join("\n", list) + "\n").isEqualTo(expectedGrid);
      }

    /**********************************************************************************************************************************************************/
    @DataProvider
    private static Object[][] grids2()
      {
        return new Object[][]
          {
            { 18, MapPoint.of(35222833, 24298096),   // https://www.openstreetmap.org/#map=18/44.4/8.95
                          """
              (0, 0) - https://tile.openstreetmap.org/18/137588/94913.png
              (1, 0) - https://tile.openstreetmap.org/18/137589/94913.png
              (2, 0) - https://tile.openstreetmap.org/18/137590/94913.png
              (0, 1) - https://tile.openstreetmap.org/18/137588/94914.png
              (1, 1) - https://tile.openstreetmap.org/18/137589/94914.png
              (2, 1) - https://tile.openstreetmap.org/18/137590/94914.png
              (0, 2) - https://tile.openstreetmap.org/18/137588/94915.png
              (1, 2) - https://tile.openstreetmap.org/18/137589/94915.png
              (2, 2) - https://tile.openstreetmap.org/18/137590/94915.png
              """ },
            { 18, MapPoint.of(35222833, 24298096 + 256),   // dragged 256 pixels southbound
              """
              (0, 0) - https://tile.openstreetmap.org/18/137588/94914.png
              (1, 0) - https://tile.openstreetmap.org/18/137589/94914.png
              (2, 0) - https://tile.openstreetmap.org/18/137590/94914.png
              (0, 1) - https://tile.openstreetmap.org/18/137588/94915.png
              (1, 1) - https://tile.openstreetmap.org/18/137589/94915.png
              (2, 1) - https://tile.openstreetmap.org/18/137590/94915.png
              (0, 2) - https://tile.openstreetmap.org/18/137588/94916.png
              (1, 2) - https://tile.openstreetmap.org/18/137589/94916.png
              (2, 2) - https://tile.openstreetmap.org/18/137590/94916.png
              """ }
          };
      }

    /**********************************************************************************************************************************************************/
    @Test(dataProvider = "areas")
    public void test_computeFittingZoom (@Nonnull final MapArea area, final int mapViewWidth, final int mapViewHeight, final int fitZoom)
      {
        // given
        underTest.updateGridSize(mapViewWidth, mapViewHeight);
        // when
        final var fit = underTest.computeFittingZoom(area);
        // then
        assertThat(fit).isEqualTo(fitZoom);
      }

    /**********************************************************************************************************************************************************/
    @DataProvider
    private static Object[][] areas()
      {
        return new Object[][]
          {
            { MapArea.of(44.4012,    8.9540, 44.3992,   8.9522), 800, 600, 18 },
            { MapArea.of(47.1150,   18.4800, 36.6199,   6.7490), 800, 600,  5 },
            { MapArea.of(60.0000, -148.0000, 46.0000, 160.0000), 800, 600,  4 }  // across Greenwich antimeridian
          };
      }

    /**********************************************************************************************************************************************************/
    @Test
    public void test_greatestOdd()
      {
        assertThat(MapViewModel.greaterOdd(4)).isEqualTo(5);
        assertThat(MapViewModel.greaterOdd(7)).isEqualTo(7);
      }
  }
