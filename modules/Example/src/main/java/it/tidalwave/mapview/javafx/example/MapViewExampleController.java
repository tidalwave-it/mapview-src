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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import it.tidalwave.mapview.MapArea;
import it.tidalwave.mapview.MapCoordinates;
import it.tidalwave.mapview.OpenStreetMapTileSource;
import it.tidalwave.mapview.OpenTopoMapTileSource;
import it.tidalwave.mapview.TileSource;
import it.tidalwave.mapview.javafx.MapView;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static lombok.AccessLevel.PROTECTED;

/***************************************************************************************************************************************************************
 *
 * @author      Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@Getter(PROTECTED) @Slf4j
public class MapViewExampleController
  {
    private static final Path CACHE_FOLDER = Path.of("target/tile-cache");

    private static final String TRACK_OVERLAY_NAME = "track";
    private static final MapCoordinates START = MapCoordinates.of(44.4072, 8.9340);
    private static final double START_ZOOM = 8;
    private static final MapArea ITALY = MapArea.of(47.115, 18.480, 36.6199, 6.749);
    private static final MapArea FRANCE = MapArea.of(51.148, 9.560, 2.053, -54.524);
    private static final MapArea SWITZERLAND = MapArea.of(47.830, 10.442, 45.776, 6.022);
    private static final MapArea ALEUTIAN = MapArea.of(62.67, -147.38, 46.32, 161.64 );
    private static final TileSource osm = new OpenStreetMapTileSource();
    private static final TileSource otm = new OpenTopoMapTileSource();

    @FXML
    private AnchorPane apAnchorPane;

    @FXML
    private Slider slZoom;

    @FXML
    private Button btZoomIn;

    @FXML
    private Button btZoomOut;

    @FXML
    private Button btReset;

    @FXML
    private Button btShowItaly;

    @FXML
    private Button btShowFrance;

    @FXML
    private Button btShowSwitzerland;

    @FXML
    private Button btShowAleutian;

    @FXML
    private Button btZeroZero;

    @FXML
    private Button btTrack;

    @FXML
    private Button btOSM;

    @FXML
    private Button btOTM;

    @FXML
    private Label lbCoordinates;

    @FXML
    private Label lbArea;

    @FXML
    private Label lbCenterCoordinates;

    @FXML
    private Label lbZoom;

    private MapView mapView;

    @Nonnull
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    public void initialize()
      {
        mapView = new MapView(MapView.options().withCacheFolder(CACHE_FOLDER));
        mapView.setZoom(START_ZOOM);
        mapView.setCenter(START);
        mapView.setDoubleClickBehaviour(MapView.RECENTER);
        AnchorPane.setLeftAnchor(mapView, 0.0);
        AnchorPane.setRightAnchor(mapView, 0.0);
        AnchorPane.setTopAnchor(mapView, 0.0);
        AnchorPane.setBottomAnchor(mapView, 0.0);
        apAnchorPane.getChildren().add(mapView);
        slZoom.minProperty().bind(mapView.minZoomProperty());
        slZoom.maxProperty().bind(mapView.maxZoomProperty());
        slZoom.valueProperty().bindBidirectional(mapView.zoomProperty());
        mapView.centerProperty().addListener((_1, _2, coordinates) -> lbCenterCoordinates.setText(coordinates.toString()));
        mapView.zoomProperty().addListener((_1, _2, zoom) -> lbZoom.setText(Integer.toString(zoom.intValue())));
        mapView.areaProperty().addListener((_1, _2, area) -> lbArea.setText(area.toString()));
        mapView.mouseCoordinatesProperty().addListener((_1, _2, coordinates) -> lbCoordinates.setText(coordinates.toString()));
        btZoomIn.setOnAction(event -> mapView.setZoom(mapView.getZoom() + 1));
        btZoomOut.setOnAction(event -> mapView.setZoom(mapView.getZoom() - 1));
        btReset.setOnAction(event -> { mapView.setCenter(START); mapView.setZoom(START_ZOOM); });
        btShowItaly.setOnAction(event -> mapView.fitArea(ITALY));
        btShowFrance.setOnAction(event -> mapView.fitArea(FRANCE));
        btShowSwitzerland.setOnAction(event -> mapView.fitArea(SWITZERLAND));
        btShowAleutian.setOnAction(event -> mapView.fitArea(ALEUTIAN));
        btZeroZero.setOnAction(event -> mapView.setCenter(MapCoordinates.of(0, 0)));
        btOSM.setOnAction(event -> mapView.setTileSource(osm));
        btOTM.setOnAction(event -> mapView.setTileSource(otm));
        btTrack.setOnAction(event -> renderTrack());
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void renderTrack()
      {
        record PointsAndArea(List<WayPoint> points, MapArea area) {}

        final var task = new Task<PointsAndArea>()
          {
            @Override @Nonnull
            protected PointsAndArea call()
              {
                final var track = loadTrack();
                final var points = track.segments().flatMap(TrackSegment::points).toList();
                final var area = computeFitArea(points);
                log.info("track with {} points, fit area: {}", points.size(), area);
                return new PointsAndArea(points, area);
              }
          };

        task.setOnSucceeded(event ->
          {
            try
              {
                final var pointsAndArea = task.get();
                mapView.removeOverlay(TRACK_OVERLAY_NAME);
                mapView.addOverlay(TRACK_OVERLAY_NAME, helper ->
                        helper.addAll(pointsAndArea.points.stream().map(wp -> createPoint(helper, wp)).toList()));
                mapView.fitArea(pointsAndArea.area);
              }
            catch (InterruptedException e)
              {
                log.error("", e);
                Thread.currentThread().interrupt();
              }
            catch (ExecutionException e)
              {
                log.error("", e);
              }
          });

        executorService.submit(task);
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    @Nonnull
    private static Node createPoint (@Nonnull final MapView.OverlayHelper helper, @Nonnull final WayPoint wp)
      {
        final var mapPoint = helper.toMapViewPoint(MapCoordinates.of(wp.getLatitude().doubleValue(), wp.getLongitude().doubleValue()));
        final var node = new Circle(2.5, Color.RED);
        node.setVisible(true);
        node.setTranslateX(mapPoint.x());
        node.setTranslateY(mapPoint.y());
        return node;
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    @Nonnull
    private static MapArea computeFitArea (@Nonnull final Collection<WayPoint> points)
      {
        // quick and dirty, just for this example
        var north = -999d;
        var south = 999d;
        var east = -999d;
        var west = 999d;

        for (final var point : points)
          {
            north = Math.max(north, point.getLatitude().doubleValue());
            south = Math.min(south, point.getLatitude().doubleValue());
            east = Math.max(east, point.getLongitude().doubleValue());
            west = Math.min(west, point.getLongitude().doubleValue());
          }

        return MapArea.of(north, east, south, west);
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    @Nonnull
    private Track loadTrack()
      {
        try (final var is = MapViewExampleController.class.getResourceAsStream("/2014-04-10 1800__20140410_1800.gpx"))
          {
            return GPX.Reader.of(GPX.Reader.Mode.LENIENT).read(is).getTracks().getFirst();
          }
        catch (IOException e)
          {
            log.error(e.toString());
            throw new UncheckedIOException(e);
          }
      }
  }