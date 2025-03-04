/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.googolplex_theater.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.WatchServiceConfiguration;
import com.jyuzawa.googolplex_theater.client.GoogolplexController;
import com.jyuzawa.googolplex_theater.config.DeviceConfig.DeviceInfo;
import io.netty.util.CharsetUtil;
import io.vertx.core.json.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.jmdns.ServiceEvent;
import org.junit.jupiter.api.Test;

class DeviceConfigLoaderTest {

    private static final String VALUE1 =
            "devices:\n  - name: NameOfYourDevice2\n    settings:\n      url: https://example2.com/\n      refreshSeconds: 9600";

    private static final String VALUE2 =
            "devices:\n  - name: NameOfYourDevice2\n    settings:\n      url: https://example2.com/updated\n      refreshSeconds: 600";

    @Test
    void loaderTest() throws IOException, InterruptedException {
        // For a simple file system with Unix-style paths and behavior:
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix().toBuilder()
                .setWatchServiceConfiguration(WatchServiceConfiguration.polling(10, TimeUnit.MILLISECONDS))
                .build());
        Path conf = fs.getPath("/conf");
        Files.createDirectory(conf);
        Path path = conf.resolve("devices.yml");
        try (BufferedWriter bufferedWriter =
                Files.newBufferedWriter(path, CharsetUtil.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            bufferedWriter.write(VALUE1);
        }
        BlockingQueue<DeviceConfig> queue = new ArrayBlockingQueue<>(10);
        GoogolplexController controller = new GoogolplexController() {

            @Override
            public void register(ServiceEvent event) {}

            @Override
            public void refresh(String name) {}

            @Override
            public List<JsonObject> getDeviceInfo() {
                return null;
            }

            @Override
            public void processDeviceConfig(DeviceConfig config) {
                queue.add(config);
            }
        };
        DeviceConfigLoader loader = new DeviceConfigLoader(controller, path);
        try {
            DeviceConfig config = queue.take();
            assertEquals(1, config.getDevices().size());
            DeviceInfo device = config.getDevices().get(0);
            assertEquals("NameOfYourDevice2", device.getName());
            assertEquals(
                    "https://example2.com/", device.getSettings().get("url").asText());
            assertEquals(9600, device.getSettings().get("refreshSeconds").asInt());

            // see if an update is detected
            try (BufferedWriter bufferedWriter =
                    Files.newBufferedWriter(path, CharsetUtil.UTF_8, StandardOpenOption.WRITE)) {
                bufferedWriter.write(VALUE2);
            }
            config = queue.poll(1, TimeUnit.MINUTES);
            assertNotNull(config);
            assertEquals(1, config.getDevices().size());
            device = config.getDevices().get(0);
            assertEquals("NameOfYourDevice2", device.getName());
            assertEquals(
                    "https://example2.com/updated",
                    device.getSettings().get("url").asText());
            assertEquals(600, device.getSettings().get("refreshSeconds").asInt());
        } finally {
            loader.close();
        }
    }
}
