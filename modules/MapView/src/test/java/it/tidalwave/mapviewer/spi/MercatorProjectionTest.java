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
    public void test_coordinatesToMapPoint (final int tileSize, final int zoom, final MapCoordinates coordinates, final MapPoint point)
      {
        // given
        final var underTest = new MercatorProjection(tileSize);
        // when
        final var result = underTest.coordinatesToMapPoint(coordinates, zoom);
        // then
        assertThat(distance(result, point)).isLessThan(1E-2);
      }

    /**********************************************************************************************************************************************************/
    @Test(dataProvider = "coordinates")
    public void test_maPointToCoordinates (final int tileSize, final int zoom, final MapCoordinates coordinates, final MapPoint point)
      {
        // given
        final var underTest = new MercatorProjection(tileSize);
        // when
        final var result = underTest.mapPointToCoordinates(point, zoom);
        // then
        assertThat(distance(result, coordinates)).isLessThan(1E-2);
      }

    /**********************************************************************************************************************************************************/
    @DataProvider
    public Object[][] coordinates()
      {
        // +------------------+------------------+
        // |[0,57]            |          [511,57]|
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
            {256, 1, MapCoordinates.of(  0,    0), MapPoint.of(256.000000, 256.000000) },
            {256, 1, MapCoordinates.of( 45,   45), MapPoint.of(320.000000, 184.179219) },
            {256, 1, MapCoordinates.of( 80,    0), MapPoint.of(256.000000,  57.476812) },
            {256, 1, MapCoordinates.of( 80,  180), MapPoint.of(512.000000,  57.476812) },
            {256, 1, MapCoordinates.of(  0,  180), MapPoint.of(512.000000, 256.000000) },
            {256, 1, MapCoordinates.of(  0, -180), MapPoint.of(  0.000000, 256.000000) },
            {256, 1, MapCoordinates.of( 80, -180), MapPoint.of(  0.000000,  57.476812) },
            {256, 1, MapCoordinates.of(-80, -180), MapPoint.of(  0.000000, 454.523188) },
          };
      }
  }
