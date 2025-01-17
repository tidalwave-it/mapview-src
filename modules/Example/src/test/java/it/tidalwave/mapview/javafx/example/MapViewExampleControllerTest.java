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
import java.io.UncheckedIOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import it.tidalwave.mapviewer.javafx.TestNGApplicationTest;
import org.testfx.api.FxRobot;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/***************************************************************************************************************************************************************
 *
 * @author      Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
public class MapViewExampleControllerTest extends TestNGApplicationTest
  {
    private MapViewExampleController underTest;

    /**********************************************************************************************************************************************************/
    @Override
    public void start (@Nonnull final Stage stage)
      {
        try
          {
            final var loader = new FXMLLoader(MapViewExampleApplication.class.getResource("/MapViewExample.fxml"));
            loader.load();
            underTest = loader.getController();
            final var root = (Node)loader.getRoot();
            root.setId("underTest");
            stage.setScene(new Scene(new StackPane(root), 1400, 1000));
            stage.show();
            stage.toFront();
            stage.setAlwaysOnTop(true);
          }
        catch (IOException e)
          {
            throw new UncheckedIOException(e);
          }
      }

    /**********************************************************************************************************************************************************/
    @AfterMethod
    public void teardowm()
      {
        sleep(2000);
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_show_Italy()
      {
        clickOn(underTest.getBtShowItaly());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_show_Italy")
    public void test_show_France()
      {
        clickOn(underTest.getBtShowFrance());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_show_France")
    public void test_show_Switzerland()
      {
        clickOn(underTest.getBtShowSwitzerland());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_show_Switzerland")
    public void test_show_Aleutian()
      {
        clickOn(underTest.getBtShowAleutian());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_show_Aleutian")
    public void test_show_track()
      {
        clickOn(underTest.getBtTrack());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_show_track")
    public void test_zoom_in()
      {
        clickOn(underTest.getBtZoomIn());
        clickOn(underTest.getBtZoomIn());
        clickOn(underTest.getBtZoomIn());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_zoom_in")
    public void test_zoom_out()
      {
        clickOn(underTest.getBtZoomOut());
        clickOn(underTest.getBtZoomOut());
        clickOn(underTest.getBtZoomOut());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_zoom_out")
    public void test_zero_zero()
      {
        clickOn(underTest.getBtZeroZero());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_zero_zero")
    public void test_drag()
      {
        final var robot = new FxRobot();
        robot.clickOn("#underTest");

        for (int i = 0; i < 8; i++)
          {
            robot.drag("#underTest", MouseButton.PRIMARY).moveBy(-200, -200).drop();
          }
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_drag")
    public void test_set_OpenTopoMap()
      {
        clickOn(underTest.getBtOTM());
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display", dependsOnMethods = "test_set_OpenTopoMap")
    public void test_set_OpenStreetMap()
      {
        clickOn(underTest.getBtOSM());
      }
  }