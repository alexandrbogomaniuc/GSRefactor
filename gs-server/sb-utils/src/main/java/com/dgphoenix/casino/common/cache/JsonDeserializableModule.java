package com.abs.casino.common.cache;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import com.abs.casino.common.cache.JsonDeserializable;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class JsonDeserializableModule extends SimpleModule {
    public JsonDeserializableModule(String packageName) {
        super("JsonDeserializableModule");

        // Automatically register all classes implementing JsonDeserializable
        try {
            // Scan all classes in the package
            for (Class clazz : getClassesFromPackage(packageName)) {
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

    public Set<Class> getClassesFromPackage(String packageName) throws IOException {
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(packageName)
                .enableClassInfo()
                .scan()) {

            Set<Class> classesFromPackage = scanResult
                    .getAllClasses()
                    .stream()
                    .map(ClassInfo::loadClass)
                    .collect(Collectors.toSet());

            return classesFromPackage;
        }
    }
}
