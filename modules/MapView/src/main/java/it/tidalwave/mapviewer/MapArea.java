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
package it.tidalwave.mapviewer;

import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import static lombok.AccessLevel.PRIVATE;

/***************************************************************************************************************************************************************
 *
 * A rectangular area on the map.
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@RequiredArgsConstructor(access = PRIVATE) @Getter @EqualsAndHashCode
public class MapArea
  {
    private final double north;

    private final double east;

    private final double south;

    private final double west;

    /***********************************************************************************************************************************************************
     * {@return a new area}.
     * @param   north     the north limit
     * @param   east      the east limit
     * @param   south     the south limit
     * @param   west      the west limit
     * @throws            IllegalArgumentException when the values are unfeasible
     **********************************************************************************************************************************************************/
    public static MapArea of (final double north, final double east, final double south, final double west)
      {
        checkLatitude(north, "north");
        checkLatitude(south, "south");
        checkLongitude(east, "east");
        checkLongitude(west, "west");

        if (north < south)
          {
            throw new IllegalArgumentException(String.format("north (%f) must be greater on equal than south (%f)", north, south));
          }

        return new MapArea(north, east, south, west);
      }

    /***********************************************************************************************************************************************************
     * {@return the center of this area}.
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapCoordinates getCenter()
      {
        var longitudeCenter = (west + east) / 2;

        if (isAcrossGreenwichAntimeridian())
          {
            longitudeCenter = longitudeCenter - 180;

            if (longitudeCenter <= -180)
              {
                longitudeCenter = 360 + longitudeCenter;
              }
          }

        return MapCoordinates.of((north + south) / 2, longitudeCenter);
      }

    /***********************************************************************************************************************************************************
     * {@return {@code true} if this area spans across the Greenwich antimeridian (180° W)}.
     **********************************************************************************************************************************************************/
    public boolean isAcrossGreenwichAntimeridian()
      {
        return east < west;
      }

    /***********************************************************************************************************************************************************
     * {@return {@code true} if this area contains the given coordinates}.
     * @param   coordinates   the coordinates
     **********************************************************************************************************************************************************/
    public boolean contains (@Nonnull final MapCoordinates coordinates)
      {
        return coordinates.latitude() <= north && coordinates.latitude() >= south
               && isAcrossGreenwichAntimeridian() ? coordinates.longitude() >= west && coordinates.longitude() <= east
                                                  :  coordinates.longitude() >= east && coordinates.longitude() <= west;
      }

    /***********************************************************************************************************************************************************
     * {@return {@code true} if this area contains the given area}.
     * @param   that   the area to compare
     **********************************************************************************************************************************************************/
    public boolean contains (@Nonnull final MapArea that)
      {
        return north >= that.north && south <= that.south && west <= that.west && east >= that.east;
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull
    public String toString()
      {
        return String.format("(n=%.6f, e=%.6f, s=%.6f, w=%.6f)", north, east, south, west);
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    private static void checkLatitude (final double latitude, @Nonnull final String name)
      {
        if (latitude < -90 || latitude > 90)
          {
            throw new IllegalStateException("Latitude must be in range [-90, 90]: " + name + "=" + latitude);
          }
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    private static void checkLongitude (final double longitude, @Nonnull final String name)
      {
        if (longitude <= -180 || longitude > 180)
          {
            throw new IllegalStateException("Longitude must be in range (-180, 180]: " + name + "=" + longitude);
          }
      }
  }
