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
package it.tidalwave.mapviewer.impl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Objects;
import javafx.beans.property.SimpleDoubleProperty;

/***************************************************************************************************************************************************************
 *
 * A specialisation of {@link javafx.beans.property.DoubleProperty} that supports limits.
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
public class RangeLimitedDoubleProperty extends SimpleDoubleProperty
  {
    private double min;

    private double max;

    /***********************************************************************************************************************************************************
     * Creates a new instance.
     * @param   bean            the owner bean
     * @param   name            the property name
     * @param   initialValue    the initial value
     * @param   min             the minimum value
     * @param   max             the maximum value
     * @see                     #setLimits(double, double)
     **********************************************************************************************************************************************************/
    public RangeLimitedDoubleProperty (@Nonnull final Object bean, @Nonnull final String name, final double initialValue, final double min, final double max)
      {
        super(bean, name, Math.min(Math.max(min, initialValue), max));
        this.min = min;
        this.max = max;
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override
    public void set (final double value)
      {
        super.set(Math.min(Math.max(min, value), max));
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override
    public void setValue (@Nullable final Number value)
      {
        Objects.requireNonNull(value, "value");
        super.setValue(Math.min(Math.max(min, value.doubleValue()), max));
      }

    /***********************************************************************************************************************************************************
     * Changes the limit. The current property value will be eventually changed to fit the limits, and a change event fired.
     * @param   min             the minimum value
     * @param   max             the maximum value
     **********************************************************************************************************************************************************/
    public void setLimits (final double min, final double max)
      {
        this.min = min;
        this.max = max;
        set(get());
      }
  }
