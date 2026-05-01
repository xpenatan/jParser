package com.github.xpenatan.jParser.example.services;

import org.teavm.classlib.ResourceSupplier;
import org.teavm.classlib.ResourceSupplierContext;

public class TestResourcesSupplier implements ResourceSupplier {
    @Override
    public String[] supplyResources(ResourceSupplierContext context) {
        String[] result = { "exampleLib.js" };
        for (int i = 0; i < result.length; ++i) {
            result[i] = "resources-for-test/" + result[i];
        }
        return result;
    }
}
