// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.spawner;

import java.lang.reflect.Type;

import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SpawnData {
  public int x, z;
  
  public SpawnData(int x, int z) {
    this.x = x;
    this.z = z;
  }

  public final BlockPos toBlockPos() {
    return new BlockPos(x, 128, z);
  }

  public static final class Serde implements JsonSerializer<SpawnData>, JsonDeserializer<SpawnData> {
    @Override
    public SpawnData deserialize(JsonElement json, Type t, JsonDeserializationContext ctx)
        throws JsonParseException {
      JsonObject obj = json.getAsJsonObject();
      int x = JsonHelper.getInt(obj, "x", JsonHelper.getInt(obj, "spawnX", 0));
      int z = JsonHelper.getInt(obj, "z", JsonHelper.getInt(obj, "spawnZ", 0));
      return new SpawnData(x, z);
    }

    @Override
    public JsonElement serialize(SpawnData self, Type t, JsonSerializationContext ctx) {
      JsonObject json = new JsonObject();
      json.addProperty("x", self.x);
      json.addProperty("z", self.z);
      return json;
    }
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof SpawnData other) {
      return this.x == other.x && this.z == other.z;
    }
    return false;
  }

}