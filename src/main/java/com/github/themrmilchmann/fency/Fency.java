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
package com.github.themrmilchmann.fency;

import com.github.themrmilchmann.fency.advancements.critereon.FencyCriteriaTriggers;
import com.github.themrmilchmann.fency.config.FencyConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

@Mod(Fency.MOD_ID)
public final class Fency {

    public static final String MOD_ID = "fency";

    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final Set<ResourceLocation> imcAllowlist = new HashSet<>();
    private static final Set<ResourceLocation> imcBlocklist = new HashSet<>();

    public Fency() {
        ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerExtensionPoint(
            IExtensionPoint.DisplayTest.class,
            () -> new IExtensionPoint.DisplayTest(
                () -> NetworkConstants.IGNORESERVERONLY,
                (a, b) -> true
            )
        );
        ctx.registerConfig(ModConfig.Type.COMMON, FencyConfig.SPEC, "the-fence-unleashed.toml");

        FencyCriteriaTriggers.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onIMCProcessEvent);
    }

    public static boolean isAllowed(ResourceLocation rl) {
        Set<? extends ResourceLocation> cfgAllowlist = FencyConfig.getAllowlist();
        Set<? extends ResourceLocation> cfgBlocklist = FencyConfig.getBlocklist();

        return cfgAllowlist.contains(rl) || (!cfgBlocklist.contains(rl) && imcAllowlist.contains(rl));
    }

    public static boolean isBlocked(ResourceLocation rl) {
        Set<? extends ResourceLocation> cfgAllowlist = FencyConfig.getAllowlist();
        Set<? extends ResourceLocation> cfgBlocklist = FencyConfig.getBlocklist();

        return cfgBlocklist.contains(rl) || (!cfgAllowlist.contains(rl) && imcBlocklist.contains(rl));
    }

    private void onIMCProcessEvent(InterModProcessEvent event) {
        event.getIMCStream().forEach(message -> {
            String method = message.method();

            switch (method) {
                case "addToAllowlist" -> {
                    ResourceLocation rl = (ResourceLocation) message.messageSupplier().get();
                    if (imcBlocklist.contains(rl))
                        throw new IllegalArgumentException("[The Fence Unleashed] Entry cannot be added to allowlist as it's already explicitly blocked: " + rl);

                    imcAllowlist.add(rl);
                }
                case "addToBlocklist" -> {
                    ResourceLocation rl = (ResourceLocation) message.messageSupplier().get();
                    if (imcAllowlist.contains(rl))
                        throw new IllegalArgumentException("[The Fence Unleashed] Entry cannot be added to blocklist as it's already explicitly allowed: " + rl);

                    imcBlocklist.add(rl);
                }
                default -> LOGGER.warn("Received IMC message for unknown method '" + message.method() + "' from mod: " + message.senderModId());
            }
        });
    }

}