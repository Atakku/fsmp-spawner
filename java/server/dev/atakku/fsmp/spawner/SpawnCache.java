// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.spawner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.imageio.ImageIO;

import dev.atakku.fsmp.spawner.mixin.AccessorSpawnLocating;

public class SpawnCache {
  private static final Random random = new Random();

  public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
      .registerTypeAdapter(SpawnData.class, new SpawnData.Serde()).create();
  private static final SerdeHelper<Object2ObjectOpenHashMap<UUID, SpawnData>> SPAWN_DATA = new SerdeHelper<Object2ObjectOpenHashMap<UUID, SpawnData>>("fsmp-spawner.json", new Object2ObjectOpenHashMap<UUID, SpawnData>(), GSON);
  private static final SerdeHelper<ObjectArrayList<SpawnData>> INVALID_CACHE = new SerdeHelper<ObjectArrayList<SpawnData>>("fsmp-spawner-invalid-cache.json", new ObjectArrayList<SpawnData>(), GSON);

  private static ObjectArrayList<SpawnData> available = null;

  public static ObjectArrayList<SpawnData> getAvailable() {
    if (available == null) {
      available = new ObjectArrayList<SpawnData>();
    }

    // every step yields step ^ 2 * 4 results (starts with 64)
    int step = 4;
    while (available.isEmpty()) {
      Spawner.LOGGER.info("Populating with step " + step);
      populate(available, step);
      available.removeAll(SPAWN_DATA.getData().values());
      available.removeAll(INVALID_CACHE.getData());
      step *= 2;
    }

    return available;
  }

  private static SpawnData takeRandomAvailable() {
    int size = SpawnCache.getAvailable().size();
    return SpawnCache.getAvailable().remove(random.nextInt(size));
  }

  private static void populate(ObjectArrayList<SpawnData> avail, int step) {
    try {
      BufferedImage img = ImageIO.read(new File("map.png"));
      Spawner.LOGGER.info("Loaded sample map");
      for (int x = -Spawner.R; x <= Spawner.R; x += Spawner.R / step) {
        for (int z = -Spawner.R; z <= Spawner.R; z += Spawner.R / step) {
          int px = Math.max(Math.min(x + Spawner.R, Spawner.R * 2 - 1), 0) / 4;
          int pz = Math.max(Math.min(z + Spawner.R, Spawner.R * 2 - 1), 0) / 4;
          if ((img.getRGB(px, pz) & 0x000000ff) >= 200) {
            avail.add(new SpawnData(x, z));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static SpawnData getSpawnData(ServerWorld world, UUID uuid) {
    if (!SPAWN_DATA.getData().containsKey(uuid)) {
      setSpawnPoint(world, uuid, null);
    }
    return SPAWN_DATA.getData().get(uuid);
  }
  
  public static int setSpawnPoint(ServerWorld world, UUID uuid, SpawnData start) {
    SpawnData initial = start != null ? start : SpawnCache.takeRandomAvailable();
    SpawnData d = initial;
    int tries = 0;
    for (tries = 0; tries < 512; tries++) {
      if (tries > 0)
        d = SpawnCache.takeRandomAvailable();
      if (AccessorSpawnLocating.invokeFindOverworldSpawn(world, d.x, d.z) != null) {
        Spawner.LOGGER.info("Found a valid spawn location at x: {} z: {}, took {} attempts", d.x, d.z, tries);
        break;
      }
      Spawner.LOGGER.info("Spawn location x: {} z: {} is invalid, skipping", d.x, d.z);
      INVALID_CACHE.getData().push(d);
    }
    if (tries > 512) {
      d = initial;
      Spawner.LOGGER.error("Failed to find a proper spawn location for {}, setting x: {} z: {}", uuid, d.x, d.z);
    }
    SPAWN_DATA.getData().put(uuid, d);
    SPAWN_DATA.saveData();
    INVALID_CACHE.saveData();
    return tries;
  }
}
