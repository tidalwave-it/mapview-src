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
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import org.apiguardian.api.API;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import static org.apiguardian.api.API.Status.STABLE;

/***************************************************************************************************************************************************************
 *
 * This class represents a point on the map. (0, 0) corresponds to the center of the map view.
 *
 * @see     MapCoordinates
 * @see     MapViewPoint
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@API(status = STABLE)
@RequiredArgsConstructor(staticName = "of") @Accessors(fluent = true) @Getter @EqualsAndHashCode
public final class MapViewPoint implements Cartesian
  {
    private final double x;

    private final double y;

    /***********************************************************************************************************************************************************
     * {@return a new point given a (x,y) source}.
     * @param   point   the source
     **********************************************************************************************************************************************************/
    @Nonnull
    public static MapViewPoint of (@Nonnull final Cartesian point)
      {
        return of(point.x(), point.y());
      }

    /***********************************************************************************************************************************************************
     * {@return a new point copying from a {@link MouseEvent}}.
     * @param   event   the event
     **********************************************************************************************************************************************************/
    @Nonnull
    public static MapViewPoint of (@Nonnull final MouseEvent event)
      {
        return of(event.getX(), event.getY());
      }

    /***********************************************************************************************************************************************************
     * {@return a point translated of the specified amount}.
     * @param   dx    the horizontal translation
     * @param   dy    the vertical translation
     **********************************************************************************************************************************************************/
    @Nonnull
    public MapPoint translated (final double dx, final double dy)
      {
        return MapPoint.of(x + dx, x + dy);
      }

    /***********************************************************************************************************************************************************
     * {@return a {@link Point2D} equivalent}.
     * @since   1.0-ALPHA-3
     **********************************************************************************************************************************************************/
    @Nonnull
    public Point2D toPoint2D()
      {
        return new Point2D(x, y);
      }

    /***********************************************************************************************************************************************************
     * {@inheritDoc}
     **********************************************************************************************************************************************************/
    @Override @Nonnull
    public String toString()
      {
        return String.format("MapViewPoint(%f, %f)", x, y);
      }
  }
