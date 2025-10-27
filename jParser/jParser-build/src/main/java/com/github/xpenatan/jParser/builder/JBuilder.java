package com.github.xpenatan.jParser.builder;

import java.util.ArrayList;

public class JBuilder {

    public static void build(BuildConfig config, ArrayList<BuildMultiTarget> targets) {
        BuildMultiTarget [] targetsArray = new BuildMultiTarget[targets.size()];
        targets.toArray(targetsArray);
        build(config, targetsArray);
    }

    public static void build(BuildConfig config, BuildMultiTarget ... targets) {
        for(int i = 0; i < targets.length; i++) {
            BuildMultiTarget target = targets[i];
            if(target != null) {
                for(BuildTarget buildTarget : target.multiTarget) {
                    String targetName = buildTarget.getClass().getSimpleName();
                    System.out.println("##### Building: " + targetName + " #####");
                    if(!buildTarget.buildInternal(config)) {
                        throw new RuntimeException();
                    }
                }
            }
        }
    }
}
