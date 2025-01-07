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
import java.util.function.Consumer;
import javafx.scene.layout.Region;
import it.tidalwave.mapviewer.impl.MapViewModel;
import it.tidalwave.mapviewer.javafx.MapView;
import lombok.RequiredArgsConstructor;

/***************************************************************************************************************************************************************
 *
 * A control used to draw an overlay over the map.
 *
 * @author      Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@RequiredArgsConstructor
public class MapOverlay extends Region
  {
    @Nonnull
    private final MapViewModel model;

    @Nonnull
    private final Consumer<MapView.OverlayHelper> creator;

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    public void create()
      {
        getChildren().clear();
        creator.accept(MapView.OverlayHelper.of(model, getChildren()));
      }
  }
