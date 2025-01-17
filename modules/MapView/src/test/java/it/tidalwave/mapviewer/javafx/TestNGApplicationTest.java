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
package it.tidalwave.mapviewer.javafx;

import javafx.stage.Stage;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Preloader;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationAdapter;
import org.testfx.framework.junit5.ApplicationFixture;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/***************************************************************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@Slf4j
public abstract class TestNGApplicationTest extends FxRobot implements ApplicationFixture
  {
    public static Application launch(Class<? extends Application> appClass, String... appArgs)
            throws Exception
      {
        FxToolkit.registerPrimaryStage();
        return FxToolkit.setupApplication(appClass, appArgs);
      }

    @BeforeMethod
    public final void internalBefore()
            throws Exception
      {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(() -> new ApplicationAdapter(this));
      }

    @AfterMethod
    public final void internalAfter()
            throws Exception
      {
        FxToolkit.cleanupAfterTest(this, new ApplicationAdapter(this));
      }

    public void init()
      {
      }

    public void start (Stage stage)
      {
      }

    public void stop()
      {
      }

    /** @deprecated */
    @Deprecated
    public final HostServices getHostServices() {
      throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final Application.Parameters getParameters() {
      throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void notifyPreloader(Preloader.PreloaderNotification notification) {
      throw new UnsupportedOperationException();
    }
  }