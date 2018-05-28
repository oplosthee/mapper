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
package nl.utwente.mapper;

import nl.utwente.mapper.metrics.AdaptiveLocalAlignment;
import nl.utwente.mapper.metrics.functions.AdaptiveSubstitution;
import nl.utwente.mapper.util.JarLoader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {

    /**
     * Sample usage of the Adaptive Local Alignment algorithm.
     */
    public static void main(String[] args) {
        File source = new File(Main.class.getClassLoader().getResource("PATH_TO_SOURCE_JAR").getFile());
        File target = new File(Main.class.getClassLoader().getResource("PATH_TO_TARGET_JAR").getFile());

        try {
            Map<String, ClassNode> sourceMap = JarLoader.readJar(source);
            Map<String, ClassNode> targetMap = JarLoader.readJar(target);

            MethodNode sourceMethod = sourceMap.get("SOURCE_CLASS_NAME").methods.get(1);
            MethodNode targetMethod = targetMap.get("TARGET_CLASS_NAME").methods.get(1);

            Set<ClassNode> classes = new HashSet<>(sourceMap.values());
            classes.addAll(targetMap.values());

            AdaptiveLocalAlignment localAlignment = new AdaptiveLocalAlignment(-0.5f, new AdaptiveSubstitution(classes));
            System.out.println("Similarity score: " + localAlignment.compare(sourceMethod, targetMethod));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
