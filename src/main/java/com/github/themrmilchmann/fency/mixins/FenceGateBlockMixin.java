/*
 * Copyright (c) 2021-2022 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.themrmilchmann.fency.mixins;

import java.lang.ref.WeakReference;
import java.util.*;

import javax.annotation.Nullable;

import com.github.themrmilchmann.fency.Fency;
import com.github.themrmilchmann.fency.config.FencyConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.FenceGateBlock.*;

@SuppressWarnings("unused")
@Mixin(FenceGateBlock.class)
public final class FenceGateBlockMixin {

    private static final UUID PROFILE_UUID = UUID.randomUUID();
    private static final GameProfile PROFILE = new GameProfile(PROFILE_UUID, "[Fency]");

    @Nullable
    private static WeakReference<Player> playerRef;

    @Accessor(value = "X_COLLISION_SHAPE")
    private static VoxelShape X_COLLISION_SHAPE() {
        throw new NotImplementedException("FenceGateBlock#X_COLLISION_SHAPE mixin failed to apply");
    }

    @Accessor(value = "Z_COLLISION_SHAPE")
    private static VoxelShape Z_COLLISION_SHAPE() {
        throw new NotImplementedException("FenceGateBlock#Z_COLLISION_SHAPE mixin failed to apply");
    }

    @Inject(at = @At(value = "HEAD"), method = "getCollisionShape", cancellable = true)
    public void getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> ci) {
        if (!state.getValue(OPEN) || !(collisionContext instanceof EntityCollisionContext entityCollisionContext)) return;

        Optional<Entity> optEntity = entityCollisionContext.getEntity();
        if (optEntity.isEmpty()) return;

        Entity entity = optEntity.get();
        ResourceLocation entityTypeID = Objects.requireNonNull(entity.getType().getRegistryName());

        boolean isBlocked = Fency.isBlocked(entityTypeID);
        boolean isAllowed = Fency.isAllowed(entityTypeID);

        if (isBlocked || (!isAllowed && FencyConfig.defaultBehavior.get() == FencyConfig.Behavior.BLOCK)) {
            ci.setReturnValue(state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE() : X_COLLISION_SHAPE());
            return;
        } else if (isAllowed || FencyConfig.defaultBehavior.get() == FencyConfig.Behavior.ALLOW) {
            ci.setReturnValue(Shapes.empty());
            return;
        }

        Set<Entity> visitedEntities = new HashSet<>();
        Entity tmp;

        while (true) {
            if (visitedEntities.contains(entity)) {
                ci.setReturnValue(state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE() : X_COLLISION_SHAPE());
                return; // We track visited entities and return early to not even risk getting stuck in infinite loops.
            }

            visitedEntities.add(entity);

            if (entity instanceof Mob mob) {
                if ((tmp = mob.getLeashHolder()) != null) {
                    if (!(tmp instanceof LeashFenceKnotEntity)) {
                        entity = tmp;
                        continue;
                    }
                }
            }

            if ((tmp = entity.getControllingPassenger()) != null) {
                entity = tmp;
                continue;
            }

            break;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Player player;

        if (server != null) {
            if (playerRef == null || (player = playerRef.get()) == null) {
                playerRef = new WeakReference<>(player = new FakePlayer(ServerLifecycleHooks.getCurrentServer().overworld(), PROFILE));
            }
        } else {
            player = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientPlayerRetriever::getPlayer);
        }

        if (entity instanceof Mob mob && (mob.canBeLeashed(player) || mob.getLeashHolder() instanceof LeashFenceKnotEntity)) {
            ci.setReturnValue(state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE() : X_COLLISION_SHAPE());
        }
    }

    // Required to properly defer the client-only logic to prevent loading of client classes on dedicated servers.
    private static final class ClientPlayerRetriever {

        @OnlyIn(Dist.CLIENT)
        private static Player getPlayer() {
            LocalPlayer player = Minecraft.getInstance().player;
            assert (player != null);

            return player;
        }

    }
}