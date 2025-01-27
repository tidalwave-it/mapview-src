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
package it.tidalwave.mapview.spi;

import it.tidalwave.mapview.MapCoordinates;
import it.tidalwave.mapview.MapPoint;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static it.tidalwave.mapview.impl.Distances.distance;
import static org.assertj.core.api.Assertions.assertThat;

/***************************************************************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
public class WGS84PseudoMercatorProjectionTest
  {
    @Test(dataProvider = "coordinates")
    public void test_coordinatesToMapPoint (final int tileSize, final int zoom, final MapCoordinates coordinates, final MapPoint point)
      {
        // given
        final var underTest = new WGS84PseudoMercatorProjection(tileSize);
        // when
        final var actual = underTest.coordinatesToMapPoint(coordinates, zoom);
        // then
        assertThat(distance(actual, point)).withFailMessage("actual: %s, expected: %s", actual, point).isLessThan(1E-2);
      }

    /**********************************************************************************************************************************************************/
    @Test(dataProvider = "coordinates")
    public void test_maPointToCoordinates (final int tileSize, final int zoom, final MapCoordinates coordinates, final MapPoint point)
      {
        // given
        final var underTest = new WGS84PseudoMercatorProjection(tileSize);
        // when
        final var actual = underTest.mapPointToCoordinates(point, zoom);
        // then
        assertThat(distance(actual, coordinates)).withFailMessage("actual: %s, expected: %s", actual, coordinates).isLessThan(1E-2);
      }

    /**********************************************************************************************************************************************************/
    @DataProvider
    public Object[][] coordinates()
      {
        // +------------------+------------------+
        // |[0,0]             |           [512,0]|
        // |                  |                  |
        // |                  |                  |
        // |                  |                  |
        // |[0,256]           |[256,256]         |
        // +------------------+------------------+
        // |                  |                  |
        // |                  |                  |
        // |                  |                  |
        // |                  |                  |
        // |[0,512]           |         [512,512]|
        // +------------------+------------------+
        final var ML = 85.05112877980655; // maximum latitude allowable by this model

        //@formatter:off
        return new Object[][]
          {
            { 256, 1, MapCoordinates.of(  0,    0), MapPoint.of( 256.000000,  256.000000) },
            { 256, 1, MapCoordinates.of( 45,   45), MapPoint.of( 320.000000,  184.179219) },
            { 256, 1, MapCoordinates.of( ML,    0), MapPoint.of( 256.000000,    0.000000) },
            { 256, 1, MapCoordinates.of( ML,  180), MapPoint.of( 512.000000,    0.000000) },
            { 256, 1, MapCoordinates.of(  0,  180), MapPoint.of( 512.000000,  256.000000) },
            { 256, 1, MapCoordinates.of(  0, -180), MapPoint.of(   0.000000,  256.000000) },
            { 256, 1, MapCoordinates.of( ML, -180), MapPoint.of(   0.000000,    0.000000) },
            { 256, 1, MapCoordinates.of(-ML, -180), MapPoint.of(   0.000000,  512.000000) },

            { 256, 2, MapCoordinates.of(  0,    0), MapPoint.of( 512.000000,  512.000000) },
            { 256, 2, MapCoordinates.of( 45,   45), MapPoint.of( 640.000000,  368.358438) },
            { 256, 2, MapCoordinates.of( ML,    0), MapPoint.of( 512.000000,    0.000000) },
            { 256, 2, MapCoordinates.of( ML,  180), MapPoint.of(1024.000000,    0.000000) },
            { 256, 2, MapCoordinates.of(  0,  180), MapPoint.of(1024.000000,  512.000000) },
            { 256, 2, MapCoordinates.of(  0, -180), MapPoint.of(   0.000000,  512.000000) },
            { 256, 2, MapCoordinates.of( ML, -180), MapPoint.of(   0.000000,    0.000000) },
            { 256, 2, MapCoordinates.of(-ML, -180), MapPoint.of(   0.000000, 1024.000000) },
          };
        //@formatter:on
      }
  }
