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
package it.tidalwave.mapview.javafx;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.nio.file.Path;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import it.tidalwave.mapview.MapArea;
import it.tidalwave.mapview.MapCoordinates;
import it.tidalwave.mapview.OpenStreetMapTileSource;
import it.tidalwave.mapview.OpenTopoMapTileSource;
import org.assertj.core.api.Condition;
import org.testfx.api.FxRobot;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.*;
import static javafx.scene.paint.Color.RED;

/***************************************************************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@Slf4j
public class MapViewTest extends TestNGApplicationTest
  {
    private static final Path CACHE_FOLDER = Path.of("target/tile-cache");

    private MapView underTest;

    private FxRobot robot;

    /**********************************************************************************************************************************************************/
    @Override
    public void start (@Nonnull final Stage stage)
      {
        underTest = new MapView(MapView.options().withCacheFolder(CACHE_FOLDER));
        underTest.setId("underTest");
        stage.setScene(new Scene(new StackPane(underTest), 800, 600));
        stage.show();
        stage.toFront();
        stage.setAlwaysOnTop(true);
        robot = new FxRobot();
      }

    /**********************************************************************************************************************************************************/
    @AfterMethod(groups = "display")
    public void waitForTiles()
      {
        sleep(2, TimeUnit.SECONDS);
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_default_properties()
      {
        assertThat(underTest.getZoom()).isEqualTo(1);
        assertThat(underTest.getCenter()).isEqualTo(MapCoordinates.of(0, 0));
        assertThat(underTest.getTileSource()).isExactlyInstanceOf(OpenStreetMapTileSource.class);
        assertThat(underTest.getArea()).isEqualTo(MapArea.of(0, 0, 0, 0));
        assertThat(underTest.getMinZoom()).isEqualTo(1);
        assertThat(underTest.getMaxZoom()).isEqualTo(19);
        assertThat(underTest.getMetersPerPixel()).isEqualTo(78271.51696402048);
        assertThat(underTest.getOverlayNames()).isEmpty();
        assertThat(underTest.getSingleClickBehaviour()).isEqualTo(MapView.DO_NOTHING);
        assertThat(underTest.getDoubleClickBehaviour()).isEqualTo(MapView.DO_NOTHING);
        assertThat(underTest.getDragBehaviour()).isEqualTo(MapView.TRANSLATE);
        assertThat(underTest.getScrollBehaviour()).isEqualTo(MapView.DO_NOTHING);
        // TODO: assertThat(underTest.getRecenterDuration()).isEqualTo(Duration.ofMillis(200));
      }

    /**********************************************************************************************************************************************************
     * The coordinates are already tested by {@link it.tidalwave.mapview.impl.MapViewModel}'s test. This is mainly to visually check the smoothness.
     **********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_drag()
      {
        // given
        runLaterAndWait(() ->
          {
            underTest.setCenter(MapCoordinates.of(44.5, 9));
            underTest.setZoom(11);
          });

        robot.clickOn("#underTest");
        // when
        for (int i = 0; i < 50; i++)
          {
            robot.drag("#underTest", MouseButton.PRIMARY).moveBy(-200, -200).drop();
          }
        // then
        assertThat(underTest.getCenter()).has(practicallyEqualsTo(MapCoordinates.of(39.397541, 15.866455)));
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void double_click_with_DO_NOTHING_should_do_nothing()
      {
        // given
        runLaterAndWait(() ->
          {
            underTest.setDoubleClickBehaviour(MapView.DO_NOTHING);
            underTest.setCenter(MapCoordinates.of(44, 7));
            underTest.setZoom(15);
          });
        robot.moveTo("#underTest").moveBy(-100, -100).interact(() -> {});
        // when
        robot.doubleClickOn(MouseButton.PRIMARY);
        sleep((long)underTest.getRecenterDuration().add(Duration.seconds(1)).toMillis(), TimeUnit.MILLISECONDS);
        // then
        assertThat(underTest.getCenter()).has(practicallyEqualsTo(MapCoordinates.of(44, 7)));
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void double_click_with_RECENTER_should_recenter()
      {
        // given
        runLaterAndWait(() ->
          {
            underTest.setDoubleClickBehaviour(MapView.RECENTER);
            underTest.setCenter(MapCoordinates.of(44, 7));
            underTest.setZoom(15);
          });
        robot.moveTo("#underTest").moveBy(-100, -100).interact(() -> {});
        // when
        robot.doubleClickOn(MouseButton.PRIMARY);
        sleep((long)underTest.getRecenterDuration().add(Duration.seconds(1)).toMillis(), TimeUnit.MILLISECONDS);
        // then
        assertThat(underTest.getCenter()).has(practicallyEqualsTo(MapCoordinates.of(44.003087, 6.995708)));
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_setTileSource()
      {
        // given
        runLaterAndWait(() ->
          {
            underTest.setCenter(MapCoordinates.of(44.5, 9));
            underTest.setZoom(11);
          });
        // when
        runLaterAndWait(() -> underTest.setTileSource(new OpenTopoMapTileSource()));
        // then
        // TODO: what could be tested here?
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_addOverlay()
      {
        // when
        runLaterAndWait(() ->
          {
            underTest.addOverlay("overlay1", ignored -> {});
            underTest.addOverlay("overlay2", ignored -> {});
          });
        // then
        assertThat(underTest.getOverlayNames()).contains("overlay1", "overlay2");
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_removeOverlay()
      {
        // given
        runLaterAndWait(() ->
          {
            underTest.addOverlay("overlay1", ignored -> {});
            underTest.addOverlay("overlay2", ignored -> {});
          });
        // when
        runLaterAndWait(() -> underTest.removeOverlay("overlay1"));
        // then
        assertThat(underTest.getOverlayNames()).contains("overlay2");
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_removeAllOverlays()
      {
        // given
        runLaterAndWait(() ->
          {
            underTest.addOverlay("overlay1", ignored -> {});
            underTest.addOverlay("overlay2", ignored -> {});
          });
        // when
        runLaterAndWait(() -> underTest.removeAllOverlays());
        // then
        assertThat(underTest.getOverlayNames()).isEmpty();
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_overlay()
      {
        // given
        runLaterAndWait(() ->
          {
            underTest.setCenter(MapCoordinates.of(44.5, 11));
            underTest.setZoom(7);
          });
        // when
        runLaterAndWait(() -> underTest.addOverlay("test", MapViewTest::createOverlay));
        // then
        assertThat(underTest.getOverlayNames()).contains("test");
      }

    /**********************************************************************************************************************************************************/
    @Test(groups = "display")
    public void test_play_around()
      {
        // given
        runLaterAndWait(() ->
          {
            underTest.setCenter(MapCoordinates.of(44.5, 11));
            underTest.setZoom(7);
            underTest.addOverlay("test", MapViewTest::createOverlay);
            underTest.setDoubleClickBehaviour(MapView.RECENTER);
          });

        final var delay = 100;
        robot.clickOn("#underTest");

        for (final var coordinates : testCoordinates())
          {
            final var point = underTest.coordinatesToPoint(coordinates);
            final var p = underTest.localToScreen(point.toPoint2D());
            robot.moveTo(p).doubleClickOn(MouseButton.PRIMARY);
            //robot.drag("#underTest", MouseButton.PRIMARY).moveTo(-p.getX(), -p.getY()).doubleClickOn(MouseButton.PRIMARY);
            sleep((long)underTest.getRecenterDuration().add(Duration.millis(100)).toMillis(), TimeUnit.MILLISECONDS);
            // when
            robot.doubleClickOn(MouseButton.PRIMARY);
            // runLaterAndWait(() -> underTest.setCenter(coordinates));
            sleep(delay);
            final var z1 = (int)underTest.getZoom();
            final var z2 = (int)underTest.getMaxZoom();
            final var zoomIn = IntStream.rangeClosed(z1 + 1, z2).boxed();
            final var zoomOut = IntStream.rangeClosed(z1, z2 - 1).boxed().toList().reversed().stream();
            Stream.concat(zoomIn, zoomOut).forEach(z ->
              {
                runLaterAndWait(() -> underTest.setZoom(z));
                sleep(delay);
              });
          }
      }

    /**********************************************************************************************************************************************************/
    public static void createOverlay (@Nonnull final MapView.OverlayHelper helper)
      {
        helper.addAll(testCoordinates().stream().map(helper::toMapViewPoint).map(p -> new Circle(p.x(), p.y(), 5, RED)).toList());
      }

    /**********************************************************************************************************************************************************/
    @Nonnull
    public static List<MapCoordinates> testCoordinates()
      {
        return List.of(MapCoordinates.of(45.7369,  7.321),
                       MapCoordinates.of(44.4926, 11.342),
                       MapCoordinates.of(44.4063,  8.9333),
                       MapCoordinates.of(45.4659,  9.1887),
                       MapCoordinates.of(45.0774,  7.6753),
                       MapCoordinates.of(46.0656, 11.1168),
                       MapCoordinates.of(45.4389, 12.3308),
                       MapCoordinates.of(45.6486, 13.77));
      }

    /**********************************************************************************************************************************************************/
    @Nonnull
    private static Condition<MapCoordinates> practicallyEqualsTo (@Nonnull final MapCoordinates expected)
      {
        return new Condition<>(a -> Math.abs(a.latitude() - expected.latitude()) + Math.abs(a.longitude() - expected.longitude()) < 1E-5,
                               "practically equals to " + expected);
      }

    /**********************************************************************************************************************************************************/
    private void runLaterAndWait (@Nonnull final Runnable runnable)
      {
        asyncFx(runnable);
        waitForFxEvents();
      }
  }