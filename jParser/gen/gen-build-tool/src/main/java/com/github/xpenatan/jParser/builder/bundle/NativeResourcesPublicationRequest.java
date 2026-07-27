package com.github.xpenatan.jParser.builder.bundle;

import java.nio.file.Path;
import java.util.ArrayList;

public final class NativeResourcesPublicationRequest {
    public String componentId;
    public String componentVersion;
    public final ArrayList<Path> resourceJars = new ArrayList<>();
    public final ArrayList<String> declaredClassifiers = new ArrayList<>();
    public boolean requireCompleteMatrix;
}
