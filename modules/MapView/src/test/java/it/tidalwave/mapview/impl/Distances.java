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
import it.tidalwave.mapview.MapCoordinates;
import it.tidalwave.mapview.Cartesian;
import lombok.experimental.UtilityClass;
import static java.lang.StrictMath.*;

@UtilityClass
public class Distances
  {
    /***********************************************************************************************************************************************************
     * Calculates the geodetic distance between the two points according to the ellipsoid model of WGS84. Altitude is neglected from calculations.
     * The implementation shall calculate this as exactly as it can. However, it is required that the result is within 0.35% of the correct result.
     * Haversine Formula (from R.W. Sinnott, "Virtues of the Haversine", Sky and Telescope, vol. 68, no. 2, 1984, p. 159):
     * See the following URL for more info on calculating distances: <a href="http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1">here</a>.
     * @param   c1  the former coordinates
     * @param   c2  the latter coordinates
     * @return      the distance in meters
     **********************************************************************************************************************************************************/
    public static double distance (@Nonnull final MapCoordinates c1, @Nonnull final MapCoordinates c2)
      {
        final double earthRadius = 6371000;

        final double lat1 = toRadians(c1.latitude());
        final double lon1 = toRadians(c1.longitude());
        final double lat2 = toRadians(c2.latitude());
        final double lon2 = toRadians(c2.longitude());

        final double dlon = lon2 - lon1;
        final double dlat = lat2 - lat1;

        return earthRadius * 2 * asin(min(1.0, sqrt(pow(sin(dlat / 2), 2) + pow(cos(lat1) * cos(lat2) * sin(dlon / 2), 2))));
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    public static double distance (@Nonnull final Cartesian p1, @Nonnull final Cartesian p2)
      {
        return sqrt(pow(p1.x() - p2.x(), 2) + pow(p1.y() - p2.y(), 2));
      }
  }
