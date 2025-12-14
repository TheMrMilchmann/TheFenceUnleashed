/*
 * Copyright (c) 2021-2025 Leon Linhart,
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
package com.github.themrmilchmann.fency;

import com.github.themrmilchmann.fency.advancements.critereon.FencyCriteriaTriggers;
import com.github.themrmilchmann.fency.config.FencyConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

@Mod(Fency.MOD_ID)
public final class Fency {

    public static final String MOD_ID = "fency";

    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final Set<Identifier> imcAllowlist = new HashSet<>();
    private static final Set<Identifier> imcBlocklist = new HashSet<>();

    public Fency(ModContainer container, IEventBus eventBus) {
        FencyCriteriaTriggers.TRIGGER_TYPES.register(eventBus);

        container.registerConfig(ModConfig.Type.COMMON, FencyConfig.SPEC, "the-fence-unleashed.toml");

        FencyCriteriaTriggers.init();
        eventBus.addListener(this::onIMCProcessEvent);
    }

    public static boolean isAllowed(Identifier id) {
        Set<? extends Identifier> cfgAllowlist = FencyConfig.getAllowlist();
        Set<? extends Identifier> cfgBlocklist = FencyConfig.getBlocklist();

        return cfgAllowlist.contains(id) || (!cfgBlocklist.contains(id) && imcAllowlist.contains(id));
    }

    public static boolean isBlocked(Identifier id) {
        Set<? extends Identifier> cfgAllowlist = FencyConfig.getAllowlist();
        Set<? extends Identifier> cfgBlocklist = FencyConfig.getBlocklist();

        return cfgBlocklist.contains(id) || (!cfgAllowlist.contains(id) && imcBlocklist.contains(id));
    }

    private void onIMCProcessEvent(InterModProcessEvent event) {
        event.getIMCStream().forEach(message -> {
            String method = message.method();

            switch (method) {
                case "addToAllowlist" -> {
                    Identifier rl = (Identifier) message.messageSupplier().get();
                    if (imcBlocklist.contains(rl))
                        throw new IllegalArgumentException("[The Fence Unleashed] Entry cannot be added to allowlist as it's already explicitly blocked: " + rl);

                    imcAllowlist.add(rl);
                }
                case "addToBlocklist" -> {
                    Identifier rl = (Identifier) message.messageSupplier().get();
                    if (imcAllowlist.contains(rl))
                        throw new IllegalArgumentException("[The Fence Unleashed] Entry cannot be added to blocklist as it's already explicitly allowed: " + rl);

                    imcBlocklist.add(rl);
                }
                default -> LOGGER.warn("Received IMC message for unknown method '" + message.method() + "' from mod: " + message.senderModId());
            }
        });
    }

}