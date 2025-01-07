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
package it.tidalwave.mapviewer;

import jakarta.annotation.Nonnull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

/***************************************************************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
public class MapAreaTest
  {
    @Test(dataProvider = "areas")
    public void test_getCenter (@Nonnull final MapArea underTest, @Nonnull final MapCoordinates expectedCenter)
      {
        final var center = underTest.getCenter();
        assertThat(center).isEqualTo(expectedCenter);
      }

    @DataProvider
    private static Object[][] areas()
      {
        return new Object[][]
          {
            { MapArea.of(50,    8, 40,   4), MapCoordinates.of(45,    6) },
            { MapArea.of(60, -150, 50, 150), MapCoordinates.of(55,  180) },
            { MapArea.of(60, -150, 50, 160), MapCoordinates.of(55, -175) },
            { MapArea.of(60, -160, 50, 150), MapCoordinates.of(55,  175) },
          };
      }
  }