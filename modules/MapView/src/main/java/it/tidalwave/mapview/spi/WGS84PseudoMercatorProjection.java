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

import jakarta.annotation.Nonnull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.tidalwave.mapview.MapCoordinates;
import it.tidalwave.mapview.MapPoint;
import it.tidalwave.mapview.Projection;
import org.apiguardian.api.API;
import lombok.RequiredArgsConstructor;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static java.lang.Math.*;

/***************************************************************************************************************************************************************
 *
 * An implementation of the <a href="https://epsg.io/3857">WGS84 Pseudo Mercator Projection (EPSG:3857)</a>.
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@API(status = EXPERIMENTAL)
@RequiredArgsConstructor
public class WGS84PseudoMercatorProjection implements Projection
  {
    private static final double EARTH_RADIUS = 6378137;

    private final int tileSize;

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull
    public MapPoint coordinatesToMapPoint (@Nonnull final MapCoordinates coordinates, final double zoomLevel)
      {
        final double pixelPerRadians = 1.0 / radiansPerPixel(zoomLevel);
        final double sinLat = sin(toRadians(coordinates.latitude()));
        final double y = (PI - 0.5 * log((1 + sinLat) / (1 - sinLat))) * pixelPerRadians;
        final double x = (PI + toRadians(coordinates.longitude())) * pixelPerRadians;
        return MapPoint.of(x, y);
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull @SuppressFBWarnings("FL_FLOATS_AS_LOOP_COUNTERS")
    public MapCoordinates mapPointToCoordinates (@Nonnull final MapPoint mapPoint, final double zoomLevel)
      {
        final double radiansPerPixel = radiansPerPixel(zoomLevel);
        final double exp = exp(2 * (PI - mapPoint.y() * radiansPerPixel));
        final double lat = toDegrees(asin((exp - 1) / (exp + 1)));
        double lon = toDegrees(mapPoint.x() * radiansPerPixel - PI);

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
        return EARTH_RADIUS * radiansPerPixel(zoomLevel) * cos(toRadians(coordinates.latitude()));
      }
    
    /***********************************************************************************************************************************************************
     * {@return the angle corresponding to a pixel}.
     * @param   zoomLevel   the zoom level
     **********************************************************************************************************************************************************/
    private double radiansPerPixel (final double zoomLevel)
      {
        return 2.0 * PI / (pow(2, zoomLevel) * tileSize);
      }
  }
