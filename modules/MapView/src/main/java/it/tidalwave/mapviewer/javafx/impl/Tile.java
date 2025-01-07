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
package it.tidalwave.mapviewer.javafx.impl;

import jakarta.annotation.Nonnull;
import java.net.URI;
import javafx.scene.image.ImageView;
import it.tidalwave.mapviewer.TileSource;
import lombok.Getter;

/***************************************************************************************************************************************************************
 *
 * This class represents a single tile for rendering a map. It basically wraps a bitmap that is available on the internet at a specific URL, and it has the
 * capability of being loaded in background and rendering some temporary icons while the process has not terminated.
 * 
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@Getter
public class Tile extends ImageView
  {
    /** The source of tile bitmaps. */
    @Nonnull
    private final TileSource source;

    /** The URL of this tile. */
    private final URI uri;

    /** The zoom level this tile belongs to. */
    private final int zoom;

    /***********************************************************************************************************************************************************
     * Creates a new tile and submits it to the cache for downloading.
     * @param   tileCache       the tile cache
     * @param   source          the tile source
     * @param   uri             the URL of the tile
     * @param   size            the size of the tile
     * @param   zoom            the zoom level of this tile
     **********************************************************************************************************************************************************/
    @SuppressWarnings("this-escape")
    protected Tile (@Nonnull final TileCache tileCache, @Nonnull final TileSource source, @Nonnull final URI uri, final int size, final int zoom)
      {
        this.source = source;
        this.uri = uri;
        this.zoom = zoom;
        setFitWidth(size);
        setFitHeight(size);
        tileCache.loadTileInBackground(this);
      }
  }
