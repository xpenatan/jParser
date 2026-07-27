import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.targets.WindowsMSVCTarget;
import com.github.xpenatan.jParser.builder.tool.BuildToolListener;
import com.github.xpenatan.jParser.builder.tool.BuildToolOptions;
import com.github.xpenatan.jParser.builder.tool.BuilderTool;
import com.github.xpenatan.jParser.builder.tool.DefaultBuildTargetConfig;
import com.github.xpenatan.jParser.builder.tool.DefaultBuildTargetFactory;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.cpp.JNIClassData;
import com.github.xpenatan.jParser.ffm.FFMClassData;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;

public class BuildRuntimeHelper {

    private static final String WINDOWS_COMPILE_FLAG_PROPERTY = "jparser.teaVMCWindowsCompileFlag";
    private static final String WINDOWS_OUTPUT_PREFIX_PROPERTY = "jparser.teaVMCWindowsOutputPrefix";

    public static void main(String[] args) throws Exception {
        WindowsMSVCTarget.DEBUG_BUILD = false;
        JParser.CREATE_RUNTIME_HELPER = true;

        BuildToolOptions.BuildToolParams data = new BuildToolOptions.BuildToolParams();
        data.libName = "runtime";
        data.idlName = null;
        data.webModuleName = "runtime";
        data.packageName = "com.github.xpenatan.jparser.runtime";
        data.cppSourcePath = null;
        data.modulePrefix = "";
        data.moduleBaseSuffix = "base";
        data.moduleBuildSuffix = "builder";
        data.moduleCoreSuffix = "core";
        data.moduleJNISuffix = "shared/runtime-jni";
        data.moduleWebSuffix = "web/runtime-web";
        data.moduleFFMSuffix = "desktop/runtime-desktop-ffm";
        data.moduleCSuffix = "shared/runtime-c";

        BuildToolOptions options = new BuildToolOptions(data, args);
        options.ffmClassData.defaultCritical = true;
        options.ffmClassData.symbolNameMode = FFMClassData.SymbolNameMode.OBFUSCATED;
        options.teaVMCClassData.symbolNameMode = FFMClassData.SymbolNameMode.OBFUSCATED;
        options.jniClassData.symbolNameMode = JNIClassData.SymbolNameMode.OBFUSCATED;
        options.ffmClassData.logMethod = true;

        DefaultBuildTargetConfig config = DefaultBuildTargetConfig.fromBuildToolOptions(options);
        config.runtimeHelperMode = true;
        config.jniCppStandard = "c++17";
        config.ffmCppStandard = "c++17";
        config.teaVMCCppStandard = "c++17";
        config.webCppStandard = "c++17";
        configureWindowsTeaVMCVariant(config);

        DefaultBuildTargetFactory targetFactory = new DefaultBuildTargetFactory();
        BuilderTool.build(options, new BuildToolListener() {
            @Override
            public void onAddTarget(
                    BuildToolOptions buildOptions,
                    IDLReader idlReader,
                    ArrayList<BuildMultiTarget> targets) {
                targetFactory.addTargets(buildOptions, idlReader, targets, config);
            }
        });
    }

    private static void configureWindowsTeaVMCVariant(DefaultBuildTargetConfig config) {
        String compileFlag = System.getProperty(WINDOWS_COMPILE_FLAG_PROPERTY, "").trim();
        String outputPrefix = normalizeOutputPrefix(
                System.getProperty(WINDOWS_OUTPUT_PREFIX_PROPERTY, ""));
        DefaultBuildTargetConfig.TargetHooks hooks = config.target("windows64_teavm_c");
        if(!compileFlag.isEmpty()) {
            hooks.compileFlags.add(compileFlag);
        }
        if(!outputPrefix.isEmpty()) {
            hooks.outputDirectoryPrefix = outputPrefix;
        }
    }

    private static String normalizeOutputPrefix(String value) {
        String normalized = value == null ? "" : value.trim().replace('\\', '/');
        while(normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while(normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
