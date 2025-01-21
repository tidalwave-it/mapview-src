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
package it.tidalwave.mapview.javafx.impl;

import jakarta.annotation.Nonnull;
import javafx.animation.Interpolatable;
import javafx.beans.value.WritableValue;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/***************************************************************************************************************************************************************
 *
 * This class represents an interpolatable translation.
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@AllArgsConstructor(staticName = "of") @Accessors(fluent = true) @Getter @EqualsAndHashCode @ToString
public class Translation implements WritableValue<Translation>, Interpolatable<Translation>
  {
    private double x;

    private double y;

    /** {@inheritDoc} */
    @Override @Nonnull
    public Translation getValue()
      {
        return this;
      }

    /** {@inheritDoc} */
    @Override
    public void setValue (@Nonnull final Translation value)
      {
        x = value.x;
        y = value.y;
      }

    /** {@inheritDoc} */
    @Override
    public Translation interpolate (@Nonnull final Translation endValue, final double progress)
      {
        return new Translation(x + (endValue.x - x) * progress, y + (endValue.y - y) * progress);
      }
  }
