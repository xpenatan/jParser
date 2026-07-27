package com.github.xpenatan.jParser.builder.bundle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class NativeResourcesPublicationVerifier {
    private NativeResourcesPublicationVerifier() {
    }

    public static void verify(NativeResourcesPublicationRequest request) throws IOException {
        if(request == null) {
            throw new IllegalArgumentException("Native resources publication request is required");
        }
        String componentId = required("componentId", request.componentId);
        String componentVersion = required("componentVersion", request.componentVersion);
        if(request.resourceJars.isEmpty()) {
            throw new IllegalArgumentException("Native resources publication contains no classifier JARs");
        }

        LinkedHashMap<String, Path> classifiers = new LinkedHashMap<>();
        for(Path resourceJar : request.resourceJars) {
            NativeComponentManifest manifest = NativeComponentReader.read(resourceJar);
            if(!componentId.equals(manifest.getComponentId())) {
                throw new IllegalArgumentException("Native resources component ID mismatch in " + resourceJar
                        + ": expected " + componentId + " but found " + manifest.getComponentId());
            }
            if(!componentVersion.equals(manifest.getComponentVersion())) {
                throw new IllegalArgumentException("Native resources component version mismatch in " + resourceJar
                        + ": expected " + componentVersion + " but found " + manifest.getComponentVersion());
            }
            boolean hasLicense = false;
            for(NativeComponentManifest.FileEntry file : manifest.getFiles()) {
                if(file.role() == NativeComponentFileRole.LICENSE) {
                    hasLicense = true;
                    break;
                }
            }
            if(!hasLicense) {
                throw new IllegalArgumentException("Native resources classifier has no license: "
                        + manifest.getClassifier());
            }
            Path previous = classifiers.put(manifest.getClassifier(), resourceJar);
            if(previous != null) {
                throw new IllegalArgumentException("Duplicate native resources classifier "
                        + manifest.getClassifier() + " in " + previous + " and " + resourceJar);
            }
        }

        LinkedHashSet<String> declared = new LinkedHashSet<>();
        for(String classifier : request.declaredClassifiers) {
            String normalized = required("declared classifier", classifier);
            if(!declared.add(normalized)) {
                throw new IllegalArgumentException("Duplicate declared native resources classifier: " + normalized);
            }
        }
        if(request.requireCompleteMatrix) {
            if(declared.isEmpty()) {
                throw new IllegalArgumentException("Release native resources publication must declare its complete "
                        + "classifier matrix");
            }
            Set<String> actual = classifiers.keySet();
            LinkedHashSet<String> missing = new LinkedHashSet<>(declared);
            missing.removeAll(actual);
            LinkedHashSet<String> unexpected = new LinkedHashSet<>(actual);
            unexpected.removeAll(declared);
            if(!missing.isEmpty() || !unexpected.isEmpty()) {
                throw new IllegalArgumentException("Incomplete native resources publication. Missing classifiers: "
                        + missing + "; unexpected classifiers: " + unexpected);
            }
        }
        else if(!declared.isEmpty()) {
            LinkedHashSet<String> unexpected = new LinkedHashSet<>(classifiers.keySet());
            unexpected.removeAll(declared);
            if(!unexpected.isEmpty()) {
                throw new IllegalArgumentException("Native resources publication contains undeclared classifiers: "
                        + unexpected);
            }
        }
    }

    private static String required(String name, String value) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
