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
import jakarta.annotation.Nullable;
import java.util.Optional;
import java.nio.file.Path;
import java.net.URI;
import it.tidalwave.mapview.TileSource;

/***************************************************************************************************************************************************************
 *
 * This abstraction for a tile allows to have more code independent of JavaFX and this more easily testable.
 *
 * @author Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
public interface AbstractTile
  {
    /***********************************************************************************************************************************************************
     * Sets the image loading it from a given path.
     * @param     imagePath         the path of the image
     * @return                      the image
     **********************************************************************************************************************************************************/
    @Nonnull
    public Optional<Object> setImageByPath (@Nullable Path imagePath);

    /***********************************************************************************************************************************************************
     * Sets the image from a given bitmap.
     * @param     bitmap            the bitmap (can be {@code null}
     **********************************************************************************************************************************************************/
    public void setImageByBitmap (@Nullable Object bitmap);

    /***********************************************************************************************************************************************************
     * {@return the URI of this tile}.
     **********************************************************************************************************************************************************/
    @Nonnull
    public URI getUri();

    /***********************************************************************************************************************************************************
     * {@return the source of this tile}.
     **********************************************************************************************************************************************************/
    @Nonnull
    public TileSource getSource();

    /***********************************************************************************************************************************************************
     * {@return the zoom level of this tile}.
     **********************************************************************************************************************************************************/
    public int getZoom();
  }
