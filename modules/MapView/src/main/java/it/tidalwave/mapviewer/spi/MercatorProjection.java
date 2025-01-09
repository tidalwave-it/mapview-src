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
package it.tidalwave.mapviewer.spi;

import jakarta.annotation.Nonnull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.tidalwave.mapviewer.MapCoordinates;
import it.tidalwave.mapviewer.MapPoint;
import it.tidalwave.mapviewer.Projection;
import org.apiguardian.api.API;
import lombok.RequiredArgsConstructor;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static java.lang.Math.*;

/***************************************************************************************************************************************************************
 *
 * An implementation of the Mercator Projection.
 * 
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@API(status = EXPERIMENTAL)
@RequiredArgsConstructor
public class MercatorProjection implements Projection
  {
    private static final double EARTH_RADIUS = 6378137;

    private static final double EARTH_CIRCUMFERENCE = EARTH_RADIUS * 2.0 * PI;

    private final int tileSize;

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull
    public MapPoint coordinatesToMapPoint (@Nonnull final MapCoordinates coordinates, final double zoomLevel)
      {
        final double arc = arc(zoomLevel);
        final double sinLat = sin(toRadians(coordinates.latitude()));
        final double metersX = EARTH_RADIUS * toRadians(coordinates.longitude());
        final double metersY = EARTH_RADIUS / 2 * log((1 + sinLat) / (1 - sinLat));
        final double x = (EARTH_CIRCUMFERENCE / 2 + metersX) / arc;
        final double y = (EARTH_CIRCUMFERENCE / 2 - metersY) / arc;
        return MapPoint.of(x, y);
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull @SuppressFBWarnings("FL_FLOATS_AS_LOOP_COUNTERS")
    public MapCoordinates mapPointToCoordinates (@Nonnull final MapPoint mapPoint, final double zoomLevel)
      {
        final double arc = arc(zoomLevel);
        final double metersX = mapPoint.x() * arc - EARTH_CIRCUMFERENCE / 2;
        final double metersY = EARTH_CIRCUMFERENCE / 2 - mapPoint.y() * arc;
        final double exp = exp(metersY / (EARTH_RADIUS / 2));
        double lon = toDegrees(metersX / EARTH_RADIUS);
        final double lat = toDegrees(asin((exp - 1) / (exp + 1)));

        while (lon <= -180)
          {
            lon += 360;
          }

        while (lon > 180)
          {
            lon -= 360;
          }

        return MapCoordinates.of(lat, lon);
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override
    public final double metersPerPixel (@Nonnull final MapCoordinates coordinates, final double zoomLevel)
      {
        return arc(zoomLevel) * cos(toRadians(coordinates.latitude()));
      }
    
    /***********************************************************************************************************************************************************
     * {@return the arc length, in meters, that corresponds to a tile}.
     * @param   zoomLevel   the zoom level
     **********************************************************************************************************************************************************/
    private double arc (final double zoomLevel)
      {
        return EARTH_CIRCUMFERENCE / (pow(2, zoomLevel) * tileSize); // was EARTH_CIRCUMFERENCE / ((1 << (maxZoomLevel - zoomLevel)) * tileSize);
      }
  }
