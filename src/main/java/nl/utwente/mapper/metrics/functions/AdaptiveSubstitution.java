/*
 * MIT License
 *
 * Copyright (c) 2018, Tom Leemreize <t.leemreize@student.utwente.nl>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package nl.utwente.mapper.metrics.functions;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AdaptiveSubstitution implements BytecodeSubstitution {

    private Map<Integer, Float> frequencyScores = new HashMap<>();

    private float maxValue = Float.MIN_VALUE;
    private float minValue = Float.MAX_VALUE;

    public AdaptiveSubstitution(Set<ClassNode> classes) {
        calculateScores(classes);
    }

    private void calculateScores(Set<ClassNode> classes) {
        Map<Integer, Integer> occurrences = new HashMap<>();
        int total = 0;

        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                Iterator iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    total++;
                    AbstractInsnNode node = (AbstractInsnNode) iterator.next();
                    occurrences.merge(node.getOpcode(), 1, Integer::sum);
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry : occurrences.entrySet()) {
            float score = (float) entry.getValue() / total;

            if (score > maxValue) {
                maxValue = score;
            }
            if (score < minValue) {
                minValue = score;
            }

            frequencyScores.put(entry.getKey(), score);
        }
    }

    @Override
    public float compare(MethodNode source, int sourceIndex, MethodNode target, int targetIndex) {
        int sourceOpcode = source.instructions.get(sourceIndex).getOpcode();
        int targetOpcode = target.instructions.get(targetIndex).getOpcode();

        if (sourceOpcode == targetOpcode) {
            return 1 - frequencyScores.get(sourceOpcode);
        } else {
            return -1 + frequencyScores.get(sourceOpcode);
        }
    }

    @Override
    public float max() {
        return 1 - maxValue;
    }

    @Override
    public float min() {
        return -1 + minValue;
    }
}
