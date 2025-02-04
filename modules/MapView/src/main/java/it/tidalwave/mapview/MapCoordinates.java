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
package it.tidalwave.mapview;

import jakarta.annotation.Nonnull;
import org.apiguardian.api.API;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import static org.apiguardian.api.API.Status.STABLE;

/***************************************************************************************************************************************************************
 *
 * This class models a pair of coordinates.
 *
 * @see     MapPoint
 * @see     MapViewPoint
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@API(status = STABLE)
@RequiredArgsConstructor(staticName = "of") @Accessors(fluent = true) @Getter @EqualsAndHashCode
public class MapCoordinates
  {
    private final double latitude;

    private final double longitude;

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull
    public String toString()
      {
        return "(%s, %s)".formatted(latitude, longitude);
      }

    /***********************************************************************************************************************************************************
     * {@return a formatted string with 6 decimals}.
     * @since     1.0-ALPHA-4
     **********************************************************************************************************************************************************/
    @Nonnull
    public String toFormattedString()
      {
        return "(%.6f, %.6f)".formatted(latitude, longitude);
      }
  }
