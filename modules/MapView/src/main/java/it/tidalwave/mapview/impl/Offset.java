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
import it.tidalwave.mapview.Cartesian;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/***************************************************************************************************************************************************************
 *
 * This class represents an offset.
 * 
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@RequiredArgsConstructor(staticName = "of") @Accessors(fluent = true) @Getter @EqualsAndHashCode
public final class Offset implements Cartesian
  {
    private final double x;

    private final double y;

    /***********************************************************************************************************************************************************
     * {@return an updated offset}.
     * @param   dx    the horizontal translation
     * @param   dy    the vertical translation
     **********************************************************************************************************************************************************/
    @Nonnull
    public Offset translated (final double dx, final double dy)
      {
        return new Offset(x + dx, y + dy);
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull
    public String toString()
      {
        return "(%f, %f)".formatted(x, y);
      }
  }
