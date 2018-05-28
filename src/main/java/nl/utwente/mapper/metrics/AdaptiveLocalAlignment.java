/*
 * License header of the original version of this file:
 * #%L
 * Simmetrics Core
 * %%
 * Copyright (C) 2014 - 2016 Simmetrics Authors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 * This file has been modified to work with MethodNodes from the ASM framework.
 */
package nl.utwente.mapper.metrics;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static nl.utwente.mapper.metrics.Math.max;

import nl.utwente.mapper.metrics.functions.BytecodeSubstitution;
import org.objectweb.asm.tree.MethodNode;

/**
 * Applies the Smith-Waterman algorithm to calculate the similarity between two
 * ASM MethodNodes. This implementation uses optimizations described in Osamu Gotoh
 * (1982). "An improved algorithm for matching biological sequences". Journal of
 * molecular biology 162: 705" and uses constant space and quadratic time.
 *
 * <p>
 * This class is immutable and thread-safe if its substitution functions are.
 *
 * @see <a
 * href="https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm">Wikipedia - Smith-Waterman algorithm</a>
 */
public final class AdaptiveLocalAlignment {

    private final float gapValue;
    private final BytecodeSubstitution substitution;

    /**
     * Constructs a new Smith Waterman metric.
     *
     * @param gapValue
     *            a non-positive gap penalty
     * @param substitution
     *            a substitution function
     */
    public AdaptiveLocalAlignment(float gapValue, BytecodeSubstitution substitution) {
        checkArgument(gapValue <= 0.0f);
        checkNotNull(substitution);
        this.gapValue = gapValue;
        this.substitution = substitution;
    }

    public float compare(final MethodNode source, final MethodNode target) {
        if (source.instructions.size() == 0 && target.instructions.size() == 0) {
            return 1.0f;
        }

        if (source.instructions.size() == 0 || target.instructions.size() == 0) {
            return 0.0f;
        }

        float maxDistance = min(source.instructions.size(), target.instructions.size())
                * max(substitution.max(), gapValue);
        return calculate(source, target) / maxDistance;
    }

    private float calculate(final MethodNode source, final MethodNode target) {
        float[] v0 = new float[target.instructions.size()];
        float[] v1 = new float[target.instructions.size()];

        float max = v0[0] = max(0, gapValue, substitution.compare(source, 0, target, 0));

        for (int j = 1; j < v0.length; j++) {
            v0[j] = max(0, v0[j - 1] + gapValue,
                    substitution.compare(source, 0, target, j));

            max = max(max, v0[j]);
        }

        // Find max
        for (int i = 1; i < source.instructions.size(); i++) {
            v1[0] = max(0, v0[0] + gapValue, substitution.compare(source, i, target, 0));

            max = max(max, v1[0]);

            for (int j = 1; j < v0.length; j++) {
                v1[j] = max(0, v0[j] + gapValue, v1[j - 1] + gapValue,
                        v0[j - 1] + substitution.compare(source, i, target, j));

                max = max(max, v1[j]);
            }

            for (int j = 0; j < v0.length; j++) {
                v0[j] = v1[j];
            }
        }

        return max;
    }

    @Override
    public String toString() {
        return "AdaptiveLocalAlignment [substitution=" + substitution + ", gapValue=" + gapValue + "]";
    }
}
