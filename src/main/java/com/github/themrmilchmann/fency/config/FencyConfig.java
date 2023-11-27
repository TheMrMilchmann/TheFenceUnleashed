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
package com.github.themrmilchmann.fency.config;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FencyConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Behavior> defaultBehavior;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> blocklist;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> allowlist;

    static {
        ModConfigSpec.Builder bConfigSpec = new ModConfigSpec.Builder();

        defaultBehavior = bConfigSpec
            .comment(
                """
                The default behavior for entities trying to pass through fence gates.
                
                Possible values:
                    ALLOW - Entities may pass through fence gates by default. Exceptions may be added to the blocklist.
                    BLOCK - Entities are blocked from passing through fence gates by default. Exceptions may be added to the allowlist.
                    CHECK - An algorithm is used to determine if an entity should be able to pass through fence gates on a best-effort basis.
                            Exceptions can be added to the allow- or blocklist to respectively allow entities to or block them from passing.
                """
            )
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
                    "minecolonies:citizen",
                    "minecolonies:visitor"
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
            StringBuilder sb = new StringBuilder("[The Fence Unleashed] Duplicate entries found in block- and allowlist:");
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