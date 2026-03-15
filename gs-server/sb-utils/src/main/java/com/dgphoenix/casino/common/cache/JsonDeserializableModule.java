package com.abs.casino.common.cache;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.abs.casino.common.cache.JsonDeserializable;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class JsonDeserializableModule extends SimpleModule {
    public JsonDeserializableModule(String... packageNames) {
        super("JsonDeserializableModule");

        // Automatically register all classes implementing JsonDeserializable
        try {
            // Scan all classes in the package
            for (Class clazz : getClassesFromPackage(packageNames)) {
                if (JsonDeserializable.class.isAssignableFrom(clazz)) {
                    // Register the deserializer for each class that implements JsonDeserializable
                    JsonDeserializer deserializer = new JsonDeserializableDeserializer(clazz);
                    addDeserializer(clazz, deserializer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<Class> getClassesFromPackage(String... packageNames) throws IOException {
        String[] normalizedPackages = Arrays.stream(packageNames == null ? new String[0] : packageNames)
                .filter(name -> name != null && !name.trim().isEmpty())
                .flatMap(name -> Arrays.stream(name.split(",")))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> name.endsWith(".*") ? name.substring(0, name.length() - 2) : name)
                .toArray(String[]::new);

        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(normalizedPackages)
                .enableClassInfo()
                .scan()) {

            Set<Class> classesFromPackage = scanResult
                    .getAllClasses()
                    .stream()
                    .map(ClassInfo::loadClass)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            return classesFromPackage;
        }
    }
}
