package com.github.xpenatan.jparser.builder.tool;

import com.github.xpenatan.jparser.builder.BuildMultiTarget;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.util.ArrayList;

public interface BuildToolListener {
    void onAddTarget(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets);
}
