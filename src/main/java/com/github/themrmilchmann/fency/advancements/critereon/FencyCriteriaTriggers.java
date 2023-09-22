/*
 * Copyright (c) 2021-2023 Leon Linhart
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
package com.github.themrmilchmann.fency.advancements.critereon;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class FencyCriteriaTriggers {

    @Nullable
    public static EnterFenceGateTrigger ENTER_FENCE_GATE;

    public static void init() {
        try {
            Method registerMethod = CriteriaTriggers.class.getDeclaredMethod("register", String.class, CriterionTrigger.class);
            registerMethod.setAccessible(true);

            try {
                registerMethod.invoke(null, "fency:enter_fence_gate", ENTER_FENCE_GATE = new EnterFenceGateTrigger());
            } finally {
                registerMethod.setAccessible(false);
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to register custom advancement trigger.", e);
        }
    }

}