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
import static org.apiguardian.api.API.Status.STABLE;

/***************************************************************************************************************************************************************
 *
 * This class is responsible for converting (latitude, longitude) into rectilinear (x,y) coordinates (to be used by a map).
 * 
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@API(status = STABLE)
public interface Projection
  {
    /***********************************************************************************************************************************************************
     * {@return a {@link MapPoint} converted from the given coordinates and zoom level}.
     * @param  coordinates   the coordinates to convert
     * @param  zoom          the zoom level
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapPoint coordinatesToMapPoint (@Nonnull MapCoordinates coordinates, double zoom);

    /***********************************************************************************************************************************************************
     * {@return {@link MapCoordinates} converted from the point coordinates and zoom level}.
     * @param  mapPoint      the point
     * @param  zoom          the zoom level
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapCoordinates mapPointToCoordinates (@Nonnull MapPoint mapPoint, double zoom);

    /***********************************************************************************************************************************************************
     * {@return the map scale (expressed in meters per pixel) at the given zoom level}.
     * @param  coordinates   coordinates
     * @param  zoom          the zoom level
     **********************************************************************************************************************************************************/
    public double metersPerPixel (@Nonnull MapCoordinates coordinates, double zoom);
  }
