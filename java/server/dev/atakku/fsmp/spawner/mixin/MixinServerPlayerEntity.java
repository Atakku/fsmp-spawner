// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.spawner.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import dev.atakku.fsmp.spawner.SpawnCache;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
  @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"), method = "moveToSpawn")
  public BlockPos getSpawnData(ServerWorld world) {
    return SpawnCache.getSpawnData(world, ((ServerPlayerEntity)(Object) this).getUuid()).toBlockPos();
  }
}