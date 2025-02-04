// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.spawner;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spawner implements DedicatedServerModInitializer {
  public static final String MOD_ID = "fsmp-spawner";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final int R = 8192;

  @Override
  public void onInitializeServer() {
    LOGGER.info("Initializing FSMP Spawner");
    // Populte the cache
    SpawnCache.getAvailable();
    CommandRegistrationCallback.EVENT.register((d, r, e) -> {
      IntegerArgumentType coord = IntegerArgumentType.integer(-R, +R);

      d.register(literal("fsmp-spawner").requires(s -> s.hasPermissionLevel(2))
          .then(literal("get").then(argument("player", EntityArgumentType.player()).executes(Spawner::get)))
          .then(literal("set").then(argument("player", EntityArgumentType.player())
              .then(argument("x", coord).then(argument("z", coord).executes(Spawner::set))))));
    });
  }

  private static final int get(CommandContext<ServerCommandSource> ctx) {
    try {
      ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
      SpawnData d = SpawnCache.getSpawnData(ctx.getSource().getWorld(), p.getUuid());
      ctx.getSource().sendMessage(Text.literal("UUID " + p.getUuidAsString() + " X: " + d.x + " Y: " + d.z));
    } catch (Exception e) {
      ctx.getSource().sendError(Text.literal(e.getMessage()));
      e.printStackTrace();
      return 0;
    }
    return 1;
  }

  private static final int set(CommandContext<ServerCommandSource> ctx) {
    try {
      ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
      int x = IntegerArgumentType.getInteger(ctx, "x");
      int z = IntegerArgumentType.getInteger(ctx, "z");
      SpawnCache.setSpawnPoint(ctx.getSource().getWorld(), p.getUuid(), new SpawnData(x, z));
    } catch (Exception e) {
      ctx.getSource().sendError(Text.literal(e.getMessage()));
      e.printStackTrace();
      return 0;
    }
    return 1;
  }
}
