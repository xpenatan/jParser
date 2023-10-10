package com.github.xpenatan.jparser.builder;

import java.util.ArrayList;

public class BuildMultiTarget {

    public ArrayList<BuildTarget> multiTarget = new ArrayList<>();

    public void add(BuildTarget target) {
        multiTarget.add(target);
    }
}
