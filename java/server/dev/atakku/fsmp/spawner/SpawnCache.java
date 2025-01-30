// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.spawner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;

import net.minecraft.util.JsonHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;

public class SpawnCache {
  private static final Random random = new Random();
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
      .registerTypeAdapter(SpawnData.class, new SpawnData.Serde()).create();

  private static Object2ObjectOpenHashMap<UUID, SpawnData> data = null;

  public static Object2ObjectOpenHashMap<UUID, SpawnData> getData() {
    if (data == null) {
      loadData();
      saveData();
    }
    return data;
  }

  private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fsmp-spawner.json");

  public static void loadData() {
    String json = "{}";
    try {
      json = Files.readString(PATH);
      data = JsonHelper.deserialize(GSON, json, new TypeToken<Object2ObjectOpenHashMap<UUID, SpawnData>>() {
      });
    } catch (Exception ex) {
      Spawner.LOGGER.warn("Failed to load json from {}: {}", PATH, ex);
      data = new Object2ObjectOpenHashMap<UUID, SpawnData>();
    }
  }

  public static void saveData() {
    try {
      Files.writeString(PATH, GSON.toJson(data));
    } catch (Exception ex) {
      Spawner.LOGGER.warn("Failed to save json to {}: {}", PATH, ex);
    }
  }

  private static ObjectArrayList<SpawnData> available = null;

  public static ObjectArrayList<SpawnData> getAvailable() {
    if (available == null) {
      available = new ObjectArrayList<SpawnData>();
    }

    int step = 1;
    while (available.isEmpty()) {
      Spawner.LOGGER.info("Populating with step " + step);
      populate(available, step);
      available.removeAll(getData().values());
      step *= 2;
    }

    return available;
  }

  private static void populate(ObjectArrayList<SpawnData> avail, int step) {
    try {
      BufferedImage img = ImageIO.read(new File("map.png"));
      Spawner.LOGGER.info("Loaded sample map");
      for (int x = -8192; x <= 8192; x += 2048 / step) {
        for (int z = -8192; z <= 8192; z += 2048 / step) {
          if ((img.getRGB(x%4, z/4) & 0x000000ff) >= 200) {
            SpawnData data = new SpawnData();
            data.spawnX = x;
            data.spawnZ = z;
            avail.add(data);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static SpawnData getSpawnData(UUID uuid) {
    if (!getData().containsKey(uuid)) {
      ObjectArrayList<SpawnData> avail = SpawnCache.getAvailable();
      getData().put(uuid, avail.remove(random.nextInt(avail.size())));
      saveData();
    }
    return getData().get(uuid);
  }
}
