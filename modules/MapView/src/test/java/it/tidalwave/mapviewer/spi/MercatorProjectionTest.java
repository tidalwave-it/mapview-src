package it.tidalwave.mapviewer.spi;

import it.tidalwave.mapviewer.MapCoordinates;
import it.tidalwave.mapviewer.MapPoint;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static it.tidalwave.mapviewer.impl.Distances.distance;
import static org.assertj.core.api.Assertions.assertThat;

public class MercatorProjectionTest
  {
    @Test(dataProvider = "coordinates")
    public void test (final int tileSize, final int zoom, final MapCoordinates coordinates, final MapPoint mapPoint)
      {
        final var underTest = new MercatorProjection(tileSize);
        final var point = underTest.coordinatesToMapPoint(coordinates, zoom);
        assertThat(distance(point, mapPoint)).isLessThan(1);
      }

    @DataProvider
    public Object[][] coordinates()
      {
        // +------------------+------------------+
        // |[0,57]            |          [512,57]|
        // |                  |                  |
        // |                  |                  |
        // |                  |                  |
        // |[0,256]           |[256,256]         |
        // +------------------+------------------+
        // |                  |                  |
        // |                  |                  |
        // |                  |                  |
        // |                  |                  |
        // |[0,454]           |                  |
        // +------------------+------------------+

        return new Object[][]
          {
            {256, 1, MapCoordinates.of(0, 0), MapPoint.of(256.000000, 256.000000) },
            {256, 1, MapCoordinates.of(45, 45), MapPoint.of(320.000000, 184.179219) },
            {256, 1, MapCoordinates.of(80, 0), MapPoint.of(256.000000, 57.476812) },
            {256, 1, MapCoordinates.of(80, 180), MapPoint.of(512.000000, 57.476812) },
            {256, 1, MapCoordinates.of(0, 180), MapPoint.of(512.000000, 256.000000) },
            {256, 1, MapCoordinates.of(0, -180), MapPoint.of(0.000000, 256.000000) },
            {256, 1, MapCoordinates.of(80, -180), MapPoint.of(0.000000, 57.476812) },
            {256, 1, MapCoordinates.of(-80, -180), MapPoint.of(0.000000, 454.523188) },
          };
      }
  }
