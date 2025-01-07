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
package it.tidalwave.mapview.javafx.example;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.application.Application;

/***************************************************************************************************************************************************************
 *
 * @author      Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
public class MapViewExampleApplication extends Application
  {
    public static void main (final String[] args)
      {
        launch(args);
      }

    @Override
    public void start (@Nonnull final Stage stage)
            throws IOException
      {
        final Parent parent = FXMLLoader.load(MapViewExampleApplication.class.getResource("/MapViewExample.fxml"));
        stage.setTitle("MapView example");
        final double scale = 0.65;
        final var screenSize = Screen.getPrimary().getBounds();
        stage.setScene(new Scene(parent, scale * screenSize.getWidth(), scale * screenSize.getHeight()));
        stage.show();
        stage.setAlwaysOnTop(true);
      }
  }