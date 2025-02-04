// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.spawner.mixin;

import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnLocating.class)
public interface AccessorSpawnLocating {
  @Invoker
  public static BlockPos invokeFindOverworldSpawn(ServerWorld world, int x, int z) {
    throw new AssertionError();
  }
}