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
package com.github.themrmilchmann.fency.config;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FencyConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ConfigValue<Behavior> defaultBehavior;

    private static final ConfigValue<List<? extends String>> blocklist;
    private static final ConfigValue<List<? extends String>> allowlist;

    static {
        ForgeConfigSpec.Builder bConfigSpec = new ForgeConfigSpec.Builder();

        defaultBehavior = bConfigSpec
            .comment("The default behavior for entities trying to pass through fence gates.")
            .defineEnum(
                "defaultBehavior",
                Behavior.CHECK
            );

        blocklist = bConfigSpec
            .comment("Entities that are always blocked from passing through fence gates.")
            .defineList(
                "blocklist",
                Collections::emptyList,
                it -> it instanceof String && (ResourceLocation.isValidResourceLocation((String) it))
            );

        allowlist = bConfigSpec
            .comment("Entities that are always allowed to pass through fence gates.")
            .defineList(
                "allowlist",
                () -> List.of(
                    "minecolonies:citizen"
                ),
                it -> it instanceof String && (ResourceLocation.isValidResourceLocation((String) it))
            );

        SPEC = bConfigSpec.build();
    }

    @Nullable
    private static Set<? extends ResourceLocation> _blocklist, _allowlist;

    public static Set<? extends ResourceLocation> getBlocklist() { return Objects.requireNonNull(_blocklist, "Requested blocklist before config was loaded"); }
    public static Set<? extends ResourceLocation> getAllowlist() { return Objects.requireNonNull(_allowlist, "Requested allowlist before config was loaded"); }

    private static void processConfig() {
        List<? extends String> blocklist = FencyConfig.blocklist.get();
        List<? extends String> allowlist = FencyConfig.allowlist.get();

        _blocklist = blocklist.stream().map(ResourceLocation::new).collect(Collectors.toUnmodifiableSet());
        _allowlist = allowlist.stream().map(ResourceLocation::new).collect(Collectors.toUnmodifiableSet());

        Set<? extends ResourceLocation> duplicates = _blocklist.stream()
            .filter(it -> _allowlist.contains(it))
            .collect(Collectors.toSet());

        if (!duplicates.isEmpty()) {
            StringBuilder sb = new StringBuilder("[The Fence Unleashed] Duplicate entries found in black- and allowlist:");
            for (ResourceLocation duplicate : duplicates) sb.append("\n\t - ").append(duplicate);
            sb.append("\nPlease resolve these configuration issues before restarting.");

            throw new IllegalArgumentException(sb.toString());
        }
    }

    @SubscribeEvent
    public static void onConfigLoaded(ModConfigEvent.Loading event) {
        processConfig();
    }

    @SubscribeEvent
    public static void onConfigReloaded(ModConfigEvent.Reloading event) {
        processConfig();
    }

    public enum Behavior {
        ALLOW,
        BLOCK,
        CHECK
    }

}