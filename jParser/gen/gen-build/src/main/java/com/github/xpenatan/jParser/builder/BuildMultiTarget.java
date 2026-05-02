package com.github.xpenatan.jParser.builder;

import java.util.ArrayList;

public class BuildMultiTarget {

    public ArrayList<BuildTarget> multiTarget = new ArrayList<>();

    public void add(BuildTarget target) {
        multiTarget.add(target);
    }
}
