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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/***************************************************************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
public class MapCoordinatesTest
  {
    private MapCoordinates underTest;

    @BeforeMethod
    public void setup()
      {
        underTest = MapCoordinates.of(1.0/3.0, 1.0/3.0);
      }

    @Test
    public void testTestToString()
      {
        assertThat(underTest).hasToString("(0.3333333333333333, 0.3333333333333333)");
      }

    @Test
    public void testToFormattedString()
      {
        assertThat(underTest.toFormattedString()).isEqualTo("(0.333333, 0.333333)");
      }
  }