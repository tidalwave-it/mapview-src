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
import java.net.URISyntaxException;
import it.tidalwave.mapviewer.spi.MercatorProjection;
import it.tidalwave.mapviewer.spi.TileSourceSupport;
import lombok.extern.slf4j.Slf4j;

/***************************************************************************************************************************************************************
 *
 * The OpenStreetMap source of map tiles.
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@Slf4j
public class OpenStreetMapTileSource extends TileSourceSupport
  {
    private static final int TILE_SIZE = 256;

    private static final int TOP_ZOOM_LEVEL = 19;
    
    private static final int DEFAULT_ZOOM_LEVEL = 9;

    /** The pattern, in {@link String#format(String, Object...)} syntax, of the URI. */
    @Nonnull
    protected final String pattern;

    /***********************************************************************************************************************************************************
     * Creates a new instance.
     **********************************************************************************************************************************************************/
    public OpenStreetMapTileSource()
      {
        this(TOP_ZOOM_LEVEL, "https://tile.openstreetmap.org/%d/%d/%d.png", "OpenStreetMap", "OpenStreetMap");
      }

    /***********************************************************************************************************************************************************
     * Constructor for subclasses.
     * @param   maxZoom         the maximum allowed zoom level
     * @param   pattern         the URI pattern
     * @param   displayName     the display name of the provider
     * @param   cachePrefix     the prefix for the cache
     **********************************************************************************************************************************************************/
    protected OpenStreetMapTileSource (final int maxZoom,
                                       @Nonnull final String pattern,
                                       @Nonnull final String displayName,
                                       @Nonnull final String cachePrefix)
      {
        super(new MercatorProjection(TILE_SIZE), 1, maxZoom, DEFAULT_ZOOM_LEVEL, TILE_SIZE, displayName, cachePrefix);
        this.pattern = pattern;
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull
    public final URI getTileUri (final int column, final int row, final int zoom)
      {
        try
          {
            return new URI(String.format(pattern, zoom, column, row));
          }
        catch (URISyntaxException e)
          {
            throw new RuntimeException(e);
          }
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull
    public String toString()
      {
        return getClass().getSimpleName();
      }
  }
