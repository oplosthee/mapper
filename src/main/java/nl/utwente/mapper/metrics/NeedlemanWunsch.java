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

import nl.utwente.mapper.metrics.functions.BytecodeSubstitution;
import org.objectweb.asm.tree.MethodNode;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static nl.utwente.mapper.metrics.Math.min;

public final class NeedlemanWunsch {

    private final BytecodeSubstitution substitution;

    private final float gapValue;

    /**
     * Constructs a new Needleman-Wunsch metric.
     *
     * @param gapValue
     *            a non-positive penalty for gaps
     * @param substitution
     *            a substitution function for mismatched characters
     */
    public NeedlemanWunsch(float gapValue, BytecodeSubstitution substitution) {
        checkArgument(gapValue <= 0.0f);
        checkNotNull(substitution);
        this.gapValue = gapValue;
        this.substitution = substitution;
    }

    public float compare(MethodNode a, MethodNode b) {
        if (a.instructions.size() == 0 && b.instructions.size() == 0) {
            return 1.0f;
        }

        if (a.equals(b)) {
            return 1.0f;
        }

        float maxDistance = max(
                a.instructions.size(),
                b.instructions.size()) * max(substitution.max(),
                gapValue);
        float minDistance = max(
                a.instructions.size(),
                b.instructions.size()) * min(substitution.min(),
                gapValue);
        return (-needlemanWunsch(a, b) - minDistance) / (maxDistance - minDistance);

    }

    private float needlemanWunsch(final MethodNode s, final MethodNode t) {
        if (s.instructions.size() == 0) {
            return -gapValue * t.instructions.size();
        }

        if (t.instructions.size() == 0) {
            return -gapValue * s.instructions.size();
        }

        final int n = s.instructions.size();
        final int m = t.instructions.size();

        // We're only interested in the alignment penalty between s and t
        // and not their actual alignment. This means we don't have to backtrack
        // through the n-by-m matrix and can safe some space by reusing v0 for
        // row i-1.
        float[] v0 = new float[m + 1];
        float[] v1 = new float[m + 1];

        for (int j = 0; j <= m; j++) {
            v0[j] = j;
        }

        for (int i = 1; i <= n; i++) {
            v1[0] = i;

            for (int j = 1; j <= m; j++) {
                v1[j] = min(
                        v0[j]     - gapValue,
                        v1[j - 1] - gapValue,
                        v0[j - 1] - substitution.compare(s, i - 1, t, j - 1));
            }

            final float[] swap = v0; v0 = v1; v1 = swap;

        }

        // Because we swapped the results are in v0.
        return v0[m];
    }

    @Override
    public String toString() {
        return "NeedlemanWunsch [costFunction=" + substitution + ", gapCost=" + gapValue + "]";
    }
}