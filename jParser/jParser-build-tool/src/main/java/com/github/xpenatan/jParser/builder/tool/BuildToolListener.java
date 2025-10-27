package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;

public interface BuildToolListener {
    void onAddTarget(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets);
}
