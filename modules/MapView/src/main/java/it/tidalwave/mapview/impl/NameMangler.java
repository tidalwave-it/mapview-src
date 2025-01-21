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
package it.tidalwave.mapview.impl;

import jakarta.annotation.Nonnull;
import lombok.experimental.UtilityClass;

/***************************************************************************************************************************************************************
 *
 * A mangler for storing files in the tile cache.
 * 
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@UtilityClass
public class NameMangler 
  {
    /***********************************************************************************************************************************************************
     * Mangles the given URL returning the local path to be used on the  filesystem. The returned path is the original parameter prefixed with two directories
     * such as <code>ab/cd/</code> where abcd is the CRC16 of the original URL.
     * @param   url  the url to mangle
     * @return       the mangled path
     **********************************************************************************************************************************************************/
    @Nonnull
    public static String mangle (@Nonnull final String url)
      {
        var s = url.substring("http://".length());
        int i = s.indexOf('/');
        
        if (i > 0)
          {
            s = s.substring(i + 1);
          }
        
        i = s.lastIndexOf('?');
        
        if (i >= 0)
          {
            s = s.substring(0, i);
          }
        
        var crc16 = "0000" + Integer.toHexString(CRC16.crc16(s));
        crc16 = crc16.substring(crc16.length() - 4);
        return crc16.substring(0, 2) + "/" + crc16.substring(2, 4) + "/" + s;
      }
  }
