/*
 * Copyright (c) 2021-2023 Leon Linhart,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * 4. Redistributions of the software or derivative works, in any form, for
 *    commercial purposes, are strictly prohibited without the express written
 *    consent of the copyright holder. For the purposes of this license,
 *    "commercial purposes" includes, but is not limited to, redistributions
 *    through applications or services that generate revenue through
 *    advertising. This clause does neither not apply to "community
 *    contributions", as defined by the act of submitting changes or additions
 *    to the software, provided that the contributors are not copyright
 *    holders of the software, nor does it apply to derivatives of the software.
 *
 * 5. Any modifications or derivatives of the software, whether in source or
 *    binary form, must be made publicly available under the same license terms
 *    as this original license. This includes providing access to the modified
 *    source code.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.themrmilchmann.fency.mixins;

import java.lang.ref.WeakReference;
import java.util.*;

import javax.annotation.Nullable;

import com.github.themrmilchmann.fency.Fency;
import com.github.themrmilchmann.fency.advancements.critereon.FencyCriteriaTriggers;
import com.github.themrmilchmann.fency.config.FencyConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.FakePlayer;
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

        Entity entity = entityCollisionContext.getEntity();
        if (entity == null) return;

        if (entity instanceof ServerPlayer player) {
            assert (FencyCriteriaTriggers.ENTER_FENCE_GATE != null);
            FencyCriteriaTriggers.ENTER_FENCE_GATE.trigger(player);
        }

        ResourceLocation entityTypeID = EntityType.getKey(entity.getType());

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

        @SuppressWarnings("resource")
        MinecraftServer server = entity.level().getServer();
        Player player;

        if (server != null) {
            if (playerRef == null || (player = playerRef.get()) == null) {
                playerRef = new WeakReference<>(player = new FakePlayer(server.overworld(), PROFILE));
            }
        } else {
            player = ClientPlayerRetriever.getPlayer();
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