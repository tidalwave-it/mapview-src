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

import java.lang.ref.SoftReference;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import javafx.scene.image.Image;
import it.tidalwave.mapview.OpenStreetMapTileSource;
import it.tidalwave.mapview.javafx.MapView;
import org.assertj.core.api.Condition;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.lang.Integer.toHexString;
import static org.mockito.Mockito.*;

/***************************************************************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **************************************************************************************************************************************************************/
public class TileCacheTest
  {
    static record MockImage (URI uri) {}

    private static final URI TILE_URI = URI.create("https://tile.openstreetmap.org/17/68647/47546.png");

    private static final Path CACHE_FOLDER = Path.of("target/cache");

    private static final Path CACHED_TILE_PATH = CACHE_FOLDER.resolve("OpenStreetMap/43/57/tile.openstreetmap.org/17/68647/47546.png");

    private TileCache underTest;

    private AbstractTile tile;

    private MapView.Options options;

    /**********************************************************************************************************************************************************/
    @BeforeMethod
    public void setup()
            throws IOException
      {
        final var tileSource = new OpenStreetMapTileSource();
        final var executorService = mock(ExecutorService.class);
        final var waitingImage = mock(Image.class);
        options = MapView.options()
                         .withCacheFolder(CACHE_FOLDER)
                         .withWaitingImage(() -> waitingImage)
                         .withExecutorService(ignored -> executorService);
        underTest = new TileCache(options);
        tile = mock(AbstractTile.class);
        when(tile.getSource()).thenReturn(tileSource);
        when(tile.getUri()).thenReturn(TILE_URI);
        when(tile.setImageByPath(any(Path.class))).thenReturn(Optional.of(new MockImage(TILE_URI)));
        Files.deleteIfExists(CACHED_TILE_PATH);
      }

    /**********************************************************************************************************************************************************/
    @Test
    public void test_tile_not_present()
      {
        // when
        underTest.loadTileInBackground(tile);
        // then
        assertThat(underTest.tileQueue).containsExactly(tile);
        assertThat(underTest.memoryImageCache).isEmpty();
        assertThat(underTest.getPendingTileCount()).isEqualTo(1);
        verify(tile).setImageByBitmap(same(options.waitingImage().get()));
      }

    /**********************************************************************************************************************************************************/
    @Test
    public void test_tile_was_in_memory_cache()
      {
        // given
        final var image = new MockImage(tile.getUri());
        underTest.memoryImageCache.put(TILE_URI, new SoftReference<>(image));
        // when
        underTest.loadTileInBackground(tile);
        // then
        assertThat(underTest.tileQueue).isEmpty();
        assertThat(underTest.getPendingTileCount()).isZero();
        verify(tile).setImageByBitmap(same(image));
      }

    /**********************************************************************************************************************************************************/
    @Test
    public void test_tile_was_in_disk_cache()
            throws IOException
      {
        // given
        Files.createDirectories(CACHED_TILE_PATH.getParent());
        Files.writeString(CACHED_TILE_PATH, "mock tile content");
        // when
        underTest.loadTileInBackground(tile);
        // then
        assertThat(underTest.tileQueue).isEmpty();
        assertThat(underTest.getPendingTileCount()).isZero();
        assertThat(underTest.memoryImageCache).hasEntrySatisfying(TILE_URI,
                                                                  new Condition<>(c -> (c instanceof final SoftReference<?> sr)
                                                                        && (sr.get() instanceof MockImage(final var uri))
                                                                        && uri.equals(tile.getUri()), "SoftRererence to " + new MockImage(tile.getUri())));
        verify(tile).setImageByPath(CACHED_TILE_PATH);
      }

    /**********************************************************************************************************************************************************/
    @Test
    public void downloadTile_from_valid_uri_must_store_file_in_cache()
            throws NoSuchAlgorithmException, IOException
      {
        // when
        TileCache.downloadTile(CACHED_TILE_PATH, TILE_URI);
        // then
        assertThat(Files.exists(CACHED_TILE_PATH)).isTrue();
        assertThat(sha256Of(CACHED_TILE_PATH)).isEqualTo("1c77b348765c66299f86929a49254e3e6d7893d3930322ff7879dda6d9071899");
      }

    /**********************************************************************************************************************************************************/
    @Test
    public void downloadTile_from_invalid_url_must_not_store_anything()
      {
        // when
        TileCache.downloadTile(CACHED_TILE_PATH, URI.create("https://tile.openstreetmap.org/17/68647/this-tile-does-not-exist.png"));
        // then
        assertThat(Files.exists(CACHED_TILE_PATH)).isFalse();
      }

    /**********************************************************************************************************************************************************/
    @Test
    public void downloadTile_from_broken_url_must_not_store_anything()
      {
        // when
        TileCache.downloadTile(CACHED_TILE_PATH, URI.create("https://this.uri.does.not/exist"));
        // then
        assertThat(Files.exists(CACHED_TILE_PATH)).isFalse();
      }

    /**********************************************************************************************************************************************************/
    @Test
    public void test_dispose()
            throws InterruptedException
      {
        // given
        final var tileSource = new OpenStreetMapTileSource();

        for (int i = 0; i < 20; i++)
          {
            final var tile = mock(AbstractTile.class);
            when(tile.getSource()).thenReturn(tileSource);
            // TODO: would be better tested by connecting to a mock server that make incoming calls stuck
            when(tile.getUri()).thenReturn(URI.create(String.format("https://tile.openstreetmap.org/17/68647/%d.png", 47000 + i)));
            when(tile.getZoom()).thenReturn(17);
            underTest.loadTileInBackground(tile);
          }
        // when
        underTest.dispose();
        Thread.sleep(1000);
        // then
        final var unterminatedRunnables = underTest.unterminatedRunnables;
        assertThat(unterminatedRunnables).withFailMessage(unterminatedRunnables.toString()).isEmpty();
      }

    /**********************************************************************************************************************************************************/
    @Nonnull
    private static String sha256Of (@Nonnull final Path path)
            throws NoSuchAlgorithmException, IOException
      {
        final var digestComputer = MessageDigest.getInstance("SHA256");

        try (final var raf = new RandomAccessFile(path.toFile(), "r"))
          {
            final var byteBuffer = raf.getChannel().map(READ_ONLY, 0, Files.size(path));
            digestComputer.update(byteBuffer);
          }

        return toString(digestComputer.digest());
      }

    /**********************************************************************************************************************************************************/
    @Nonnull
    private static String toString (@Nonnull final byte[] bytes)
      {
        final var builder = new StringBuilder();

        for (final byte b : bytes)
          {
            final int value = b & 0xff;
            builder.append(toHexString(value >>> 4)).append(toHexString(value & 0x0f));
          }

        return builder.toString();
      }
  }