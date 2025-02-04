// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.spawner;

import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.util.JsonHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

public class SerdeHelper<T> {
  private final Path path;
  private T data;
  private final Gson gson;

  public SerdeHelper(String name, T init, Gson gson) {
    this.path = FabricLoader.getInstance().getConfigDir().resolve(name);
    this.data = init;
    this.gson = gson;
  }

  public T getData() {
    if (data == null) {
      loadData();
      saveData();
    }
    return data;
  }

  public void loadData() {
    String json = "{}";
    try {
      json = Files.readString(path);
      data = JsonHelper.deserialize(gson, json, new TypeToken<T>() {
      });
    } catch (Exception ex) {
      Spawner.LOGGER.warn("Failed to load json from {}: {}", path, ex);
    }
  }

  public void saveData() {
    try {
      Files.writeString(path, gson.toJson(data));
    } catch (Exception ex) {
      Spawner.LOGGER.warn("Failed to save json to {}: {}", path, ex);
    }
  }
}
