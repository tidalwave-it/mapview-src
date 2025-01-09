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
import java.net.URI;
import it.tidalwave.mapviewer.javafx.impl.Tile;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.STABLE;

/***************************************************************************************************************************************************************
 *
 * This class represent a source for {@link Tile}s. A source is able to convert coordinates expressed as (latitude, longitude) into rectified (x,y)
 * cartesian coordinates for a map, as well as to provide the URL for a map tile that contains a given pair of rectified coordinates.
 * 
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@API(status = STABLE)
public interface TileSource extends Projection
  {
    /***********************************************************************************************************************************************************
     * {@return the display name of this object}.
     **********************************************************************************************************************************************************/
    public String getDisplayName();

    /***********************************************************************************************************************************************************
     * {@return the URI for the tile at the given position with the given zoom level}.
     * @param   column    the tile column
     * @param   row       the tile row
     * @param   zoom      the zoom level
     **********************************************************************************************************************************************************/
    @Nonnull
    public URI getTileUri (int column, int row, int zoom);

    /***********************************************************************************************************************************************************
     * {@return the maximum zoom level of this source}.
     **********************************************************************************************************************************************************/
    public int getMaxZoomLevel();

    /***********************************************************************************************************************************************************
     * {@return the minimum zoom level of this source}.
     **********************************************************************************************************************************************************/
    public int getMinZoomLevel();

    /***********************************************************************************************************************************************************
     * {@return the default zoom level of this source}.
     **********************************************************************************************************************************************************/
    public int getDefaultZoomLevel();

    /***********************************************************************************************************************************************************
     * {@return a prefix unique to this source to be used by the local tile cache}.
     **********************************************************************************************************************************************************/
    @Nonnull
    public String getCachePrefix();

    /***********************************************************************************************************************************************************
     * {@return the size of the tiles created by this source}.
     **********************************************************************************************************************************************************/
    public int getTileSize();
}
