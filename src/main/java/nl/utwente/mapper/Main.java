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

    public static void main(String[] args) {
        File source = new File(Main.class.getClassLoader().getResource("samples/gamepack/123.jar").getFile());
        File target = new File(Main.class.getClassLoader().getResource("samples/gamepack/170.jar").getFile());

        try {
            Map<String, ClassNode> sourceMap = JarLoader.readJar(source);
            Map<String, ClassNode> targetMap = JarLoader.readJar(target);

            MethodNode sourceMethod1 = sourceMap.get("ChatPlayer").methods.get(1);
            MethodNode targetMethod1 = targetMap.get("defpackage/ChatPlayer").methods.get(2);

            MethodNode sourceMethod2 = sourceMap.get("WorldMapManager").methods.get(1);
            MethodNode targetMethod2 = targetMap.get("defpackage/WorldMapManager").methods.get(4);

            // Manual inspection shows these methods actually look very different:
            MethodNode sourceMethod3 = sourceMap.get("NPCComposition").methods.get(4);
            MethodNode targetMethod3 = targetMap.get("defpackage/NPCComposition").methods.get(4);

            MethodNode sourceMethod4 = sourceMap.get("Client").methods.get(11);
            MethodNode targetMethod4 = targetMap.get("defpackage/Client").methods.get(3);

            MethodNode unrelatedMethod1 = targetMap.get("defpackage/ChatPlayer").methods.get(3);
            MethodNode unrelatedMethod2 = targetMap.get("defpackage/WorldMapManager").methods.get(5);

            Set<ClassNode> classes = new HashSet<>(sourceMap.values());
            classes.addAll(targetMap.values());

            AdaptiveLocalAlignment localAlignment = new AdaptiveLocalAlignment(-0.5f, new AdaptiveSubstitution(classes));
            System.out.println("[1] Score with same method of different revision: " + localAlignment.compare(sourceMethod1, targetMethod1));
            System.out.println("[2] Score with same method of different revision: " + localAlignment.compare(sourceMethod2, targetMethod2));
            System.out.println("[3] Score with same method of different (obfuscated) revision: " + localAlignment.compare(sourceMethod3, targetMethod3));
            System.out.println("[4] Score with same method of different (obfuscated) revision: " + localAlignment.compare(sourceMethod4, targetMethod4));
            System.out.println("-------------------------------------------------");
            System.out.println("[5] Score with random method of different revision: " + localAlignment.compare(sourceMethod1, unrelatedMethod1));
            System.out.println("[6] Score with random method of different revision: " + localAlignment.compare(sourceMethod2, unrelatedMethod2));
            System.out.println("[7] Score with random method of different revision: " + localAlignment.compare(targetMethod1, unrelatedMethod1));
            System.out.println("[8] Score with random method of different revision: " + localAlignment.compare(targetMethod2, sourceMethod1));
            System.out.println("-------------------------------------------------");
            System.out.println("[9] Comparing a method to itself: " + localAlignment.compare(sourceMethod1, sourceMethod1));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}