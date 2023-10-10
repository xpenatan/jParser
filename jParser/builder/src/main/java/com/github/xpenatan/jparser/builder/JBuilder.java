package com.github.xpenatan.jparser.builder;

import java.util.ArrayList;

public class JBuilder {

    public static void build(BuildConfig config, ArrayList<BuildTarget> targets) {
        BuildTarget [] targetsArray = new BuildTarget[targets.size()];
        for(int i = 0; i < targets.size(); i++) {
            targetsArray[i] = targets.get(i);
        }
        build(config, targetsArray);
    }

    public static void build(BuildConfig config, BuildTarget ... targets) {
        for(int i = 0; i < targets.length; i++) {
            BuildTarget target = targets[i];
            if(target != null) {
                String targetName = target.getClass().getSimpleName();
                System.out.println("##### Building: " + targetName + " #####");
                if(!target.build(config)) {
                    throw new RuntimeException();
                }
            }
        }
    }

    public static void build(BuildConfig config, BuildMultiTarget ... targets) {
        for(int i = 0; i < targets.length; i++) {
            BuildMultiTarget target = targets[i];
            if(target != null) {
                for(BuildTarget buildTarget : target.multiTarget) {
                    build(config, buildTarget);
                }
            }
        }
    }
}
