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
import it.tidalwave.mapviewer.Projection;
import it.tidalwave.mapviewer.TileSource;
import org.apiguardian.api.API;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/***************************************************************************************************************************************************************
 *
 * A generic, basic implementation for {@link TileSource}. This class basically acts as a holder for immutable properties (such as zoom ranges etc...)
 * and also contains an instance of {@link Projection} to which it delegates the coordinate conversion tasks.
 * 
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@API(status = EXPERIMENTAL)
@RequiredArgsConstructor @Getter
public abstract class TileSourceSupport implements TileSource
  {
    /** The projection that converts (latitude,longitude) into rectified map coordinates. */
    @Delegate @Nonnull
    private final Projection projection;
    
    /** The minimum zoom level. */
    private final int minZoomLevel;

    /** The maximum zoom level. */
    private final int maxZoomLevel;
    
    /** The default zoom level. */
    private final int defaultZoomLevel;
    
    /** The size of the generated tiles. */
    private final int tileSize;
    
    /** The display name. */
    private final String displayName;
    
    /** The prefix for the local cache. */
    private final String cachePrefix;
  }
