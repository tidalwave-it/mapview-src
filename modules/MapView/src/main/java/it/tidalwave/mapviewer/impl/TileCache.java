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
package it.tidalwave.mapviewer.impl;

import java.lang.ref.SoftReference;
import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.tidalwave.mapviewer.javafx.MapView;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.mapviewer.impl.NameMangler.mangle;
import static java.net.http.HttpClient.Redirect.ALWAYS;

/***************************************************************************************************************************************************************
 *
 * A cache for tiles.
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
@Slf4j
public class TileCache
  {
    /** The queue of tiles to be downloaded. */
    @Nonnull
    /* visible for testing */ final BlockingQueue<AbstractTile> tileQueue;

    /** Options of the map view. */
    @Nonnull
    private final MapView.Options options;

    /** The thread pool for downloading tiles. */
    @Nonnull
    private final ExecutorService executorService;

    /** This is important to avoid flickering then the TileGrid recreates tiles. */
    /* visible for testing */ final Map<URI, SoftReference<Object>> memoryImageCache = new ConcurrentHashMap<>();

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public TileCache (@Nonnull final MapView.Options options)
      {
        this.options = options;
        tileQueue = new LinkedBlockingQueue<>(options.tileQueueCapacity());
        final var poolSize = options.poolSize();
        executorService = options.executorService().apply(poolSize);
        IntStream.range(0, poolSize).forEach(i -> executorService.execute(this::tileLoader));
      }

    /***********************************************************************************************************************************************************
     * {@return the number of tiles in the download queue}.
     **********************************************************************************************************************************************************/
    public int getPendingTileCount()
      {
        return tileQueue.size();
      }

    /***********************************************************************************************************************************************************
     * Loads a tile in background.
     * @param   tile      the tile to download
     **********************************************************************************************************************************************************/
    public final void loadTileInBackground (@Nonnull final AbstractTile tile)
      {
        log.debug("loadTileInBackground({})", tile);
        final var imageRef = memoryImageCache.get(tile.getUri());
        final var image = (imageRef == null) ? null : imageRef.get();

        if (image != null)
          {
            log.debug("loading tile from memory cache...");
            tile.setImageByBitmap(image);
          }
        else
          {
            final var localPath = resolveCachedTilePath(tile);
            log.debug("looking in disk cache {} ...", localPath);

            if (Files.exists(localPath))
              {
                loadImageFromCache(tile, localPath);
              }
            else
              {
                tile.setImageByBitmap(options.waitingImage().get());

                if (tileQueue.offer(tile))
                  {
                    log.debug("added tile {} to download queue - tiles in queue: {}", tile.getUri(), tileQueue.size());
                  }
                else
                  {
                    log.warn("download queue full, discarding: {}", tile);
                  }
              }
          }
      }

    /***********************************************************************************************************************************************************
     * Clears the queue of pending tiles, retaining only those for the given zoom level.
     * @param   zoom    the zoom level to retain
     **********************************************************************************************************************************************************/
    public void retainPendingTiles (final int zoom)
      {
        log.debug("retainPendingTiles({})", zoom);
        tileQueue.removeIf(tile -> tile.getZoom() != zoom);
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    public void dispose()
            throws InterruptedException
      {
        log.debug("dispose()");
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
      }

    /***********************************************************************************************************************************************************
     * The main loop that downloads the tiles.
     **********************************************************************************************************************************************************/
    private void tileLoader()
      {
        while (!Thread.interrupted())
          {
            try
              {
                log.debug("waiting for next tile to load... queue size = {}", tileQueue.size());
                final var tile = tileQueue.take();
                final var uri = tile.getUri();
                final var localPath = resolveCachedTilePath(tile);

                if (!Files.exists(localPath) && options.downloadAllowed())
                  {
                    downloadTile(localPath, uri);
                  }

                if (!Files.exists(localPath))
                  {
                    tile.setImageByPath(null);
                  }
                else
                  {
                    loadImageFromCache(tile, localPath);
                  }
              }
            catch (InterruptedException ignored)
              {
                log.info("tileLoader interrupted");
                Thread.currentThread().interrupt();
                break;
              }
            catch (Exception e) // defensive
              {
                log.error("", e);
              }
          }

        log.info("tileLoader terminated");
      }

    /***********************************************************************************************************************************************************
     * Loads an image from the cache.
     * @param     tile          the tile
     * @param     path          the path of the cache file
     **********************************************************************************************************************************************************/
    private void loadImageFromCache (@Nonnull final AbstractTile tile, @Nonnull final Path path)
      {
        log.debug("loadImageFromCache({}, {})", tile, path);
        tile.setImageByPath(path).ifPresent(image -> memoryImageCache.put(tile.getUri(), new SoftReference<>(image)));
      }

    /***********************************************************************************************************************************************************
     * {@return the path of the cached tile}.
     * @param     tile          the tile
     **********************************************************************************************************************************************************/
    @Nonnull
    private Path resolveCachedTilePath (@Nonnull final AbstractTile tile)
      {
        return options.cacheFolder().resolve(tile.getSource().getCachePrefix()).resolve(mangle(tile.getUri().toString()));
      }

    /***********************************************************************************************************************************************************
     * Downloads a tile and stores it.
     * @param     localPath     the file to store the tile into
     * @param     uri           the uri of the tile
     **********************************************************************************************************************************************************/
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    /* visible for testing */ static void downloadTile (@Nonnull final Path localPath, @Nonnull final URI uri)
      {
        try (final var client = HttpClient.newBuilder().followRedirects(ALWAYS).build())
          {
            Files.createDirectories(localPath.getParent());
            final var request = HttpRequest.newBuilder()
                                           .GET()
                                           .header("User-Agent", "curl/8.7.1")
                                           .header("Accept", "*/*")
                                           .uri(uri)
                                           .build();
            final var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            switch (response.statusCode())
              {
                case 200:
                  final var bytes = response.body();
                  Files.write(localPath, bytes);
                  log.debug("written {} bytes to {}", bytes.length, localPath);
                  break;
                case 503:
                  log.warn("status code 503 for {}, should re-schedule; {}", uri, response.headers().map());
                  getErrorBody(response).ifPresent(log::warn);
                  // TODO: should reschedule, but not immediately, and also count for a max number of attempts
                  // TOOD: could use a different placeholder image?
                  break;
                default:
                  log.error("status code {} for {}; {}", response.statusCode(), uri, response.headers().map());
                  getErrorBody(response).ifPresent(log::error);
              }
          }
        catch (InterruptedException e)
          {
            log.error("", e);
            Thread.currentThread().interrupt();
          }
        catch (Exception e) // defensive
          {
            log.error("", e);
          }
      }

    /***********************************************************************************************************************************************************
     *
     **********************************************************************************************************************************************************/
    @Nonnull
    private static Optional<String> getErrorBody (@Nonnull final HttpResponse<byte[]> response)
      {
        return response.headers()
                       .firstValue("Content-type")
                       .filter(ct -> ct.startsWith("text/"))
                       .map(r -> new String(response.body(), StandardCharsets.UTF_8)); // TODO: charset should be get from response
      }
  }
