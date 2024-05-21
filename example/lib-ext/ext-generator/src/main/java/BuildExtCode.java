import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildMultiTarget;
import com.github.xpenatan.jparser.builder.BuildTarget;
import com.github.xpenatan.jparser.builder.JBuilder;
import com.github.xpenatan.jparser.builder.targets.WindowsTarget;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.util.FileHelper;
import com.github.xpenatan.jparser.cpp.CppCodeParser;
import com.github.xpenatan.jparser.cpp.CppGenerator;
import com.github.xpenatan.jparser.cpp.NativeCPPGenerator;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class BuildExtCode {

    public static void build() throws Exception {
        String libName = "extlib";
        String basePackage = "com.lib.ext";

        JParser.CREATE_IDL_HELPER = false;

        String libPath = new File("../../lib").getCanonicalPath().replace("\\", "/");
        String idlPath = new File("src/main/cpp/ExtLib.idl").getCanonicalPath();
        IDLReader idlReader = IDLReader.readIDL(idlPath);
        String baseJavaDir = new File(".").getAbsolutePath() + "./ext-base/src/main/java";

        String cppSourceDir = new File("./src/main/cpp/cpp-source//source/").getCanonicalPath();
        String libsDir = new File("./build/c++/libs/").getCanonicalPath();
        String libBuildPath = new File("./build/c++/").getCanonicalPath();
        String cppDestinationPath = libBuildPath + "/src";
        String libDestinationPath = cppDestinationPath + "/extlib";

        FileHelper.copyDir(cppSourceDir, libDestinationPath);

        Path copyOut = new File(libDestinationPath).toPath();
        FileHelper.copyDir(new File("src/main/cpp/cpp-source/custom").toPath(), copyOut);

        CppGenerator cppGenerator = new NativeCPPGenerator(libDestinationPath);
        CppCodeParser cppParser = new CppCodeParser(cppGenerator, idlReader, basePackage, cppSourceDir);
        cppParser.generateClass = true;
        JParser.generate(cppParser, baseJavaDir, "../ext-core/src/main/java");

        BuildConfig buildConfig = new BuildConfig(cppDestinationPath, libBuildPath, libsDir, libName);

        ArrayList<BuildMultiTarget> targets = new ArrayList<>();
        if(BuildTarget.isWindows() || BuildTarget.isUnix()) {
//            targets.add(getWindowBuildTarget(libPath));
        }

        JBuilder.build(buildConfig, targets);
    }

    private static BuildMultiTarget getWindowBuildTarget(String libPath) throws IOException {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        String libCppPath = libPath + "/generator/build/c++";

        WindowsTarget windowsTarget = new WindowsTarget();
        windowsTarget.headerDirs.add("-I" + libCppPath + "/src/imgui");
        windowsTarget.isStatic = true;
        windowsTarget.headerDirs.add("-Isrc/extlib/");
        windowsTarget.cppInclude.add("**/extlib/*.cpp");
        multiTarget.add(windowsTarget);

        return multiTarget;
    }

}