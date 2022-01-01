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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.themrmilchmann.fency.config.FencyConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.LeashKnotEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.FenceGateBlock.*;

@SuppressWarnings("unused")
@Mixin(FenceGateBlock.class)
public final class FenceGateBlockMixin {

    private static final UUID PROFILE_UUID = UUID.randomUUID();
    private static final GameProfile PROFILE = new GameProfile(PROFILE_UUID, "[Fency]");

    @Nullable
    private static WeakReference<FakePlayer> fakePlayerRef;

    @Accessor(value = "X_COLLISION_SHAPE")
    private static VoxelShape X_COLLISION_SHAPE() {
        throw new NotImplementedException("FenceGateBlock#X_COLLISION_SHAPE mixin failed to apply");
    }

    @Accessor(value = "Z_COLLISION_SHAPE")
    private static VoxelShape Z_COLLISION_SHAPE() {
        throw new NotImplementedException("FenceGateBlock#Z_COLLISION_SHAPE mixin failed to apply");
    }

    @Inject(at = @At(value = "HEAD"), method = "getCollisionShape", cancellable = true)
    public void getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext, CallbackInfoReturnable<VoxelShape> ci) {
        Entity entity = selectionContext.getEntity();
        if (!state.getValue(OPEN) || entity == null) return;

        ResourceLocation entityTypeID = Objects.requireNonNull(entity.getType().getRegistryName());

        boolean isBlocked = FencyConfig.getBlocklist().contains(entityTypeID);
        boolean isAllowed = FencyConfig.getAllowlist().contains(entityTypeID);

        if (isBlocked || (!isAllowed && FencyConfig.defaultBehavior.get() == FencyConfig.Behavior.BLOCK)) {
            ci.setReturnValue(state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE() : X_COLLISION_SHAPE());
            return;
        } else if (isAllowed || FencyConfig.defaultBehavior.get() == FencyConfig.Behavior.ALLOW) {
            ci.setReturnValue(VoxelShapes.empty());
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

            if (entity instanceof MobEntity) {
                if ((tmp = ((MobEntity) entity).getLeashHolder()) != null) {
                    if (!(tmp instanceof LeashKnotEntity)) {
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

        FakePlayer fakePlayer;

        if (fakePlayerRef == null || (fakePlayer = fakePlayerRef.get()) == null) {
            fakePlayerRef = new WeakReference<>(fakePlayer = new FakePlayer(ServerLifecycleHooks.getCurrentServer().overworld(), PROFILE));
        }

        if (entity instanceof MobEntity && (((MobEntity) entity).canBeLeashed(fakePlayer) || ((MobEntity) entity).getLeashHolder() instanceof LeashKnotEntity)) {
            ci.setReturnValue(state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE() : X_COLLISION_SHAPE());
        }
    }

}