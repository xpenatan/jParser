package com.github.xpenatan.jparser.builder;

public class JBuilder {

    public static void build(BuildConfig config, BuildTarget ... targets) {
        for(int i = 0; i < targets.length; i++) {
            BuildTarget target = targets[i];
            if(!target.build(config)) {
                throw new RuntimeException();
            }
        }
    }
}
