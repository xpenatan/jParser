import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.config.TeaTargetType;
import java.io.File;
import java.io.IOException;
import org.teavm.tooling.TeaVMSourceFilePolicy;
import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.sources.DirectorySourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;

public class Build {

    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new AssetFileHandle("../desktop/assets"));
        teaBuildConfiguration.webappPath = new File("build/dist").getCanonicalPath();
        teaBuildConfiguration.targetType = TeaTargetType.JAVASCRIPT;
        TeaBuilder.config(teaBuildConfiguration);

        TeaVMTool tool = new TeaVMTool();
        tool.setObfuscated(false);
        tool.setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE);
        tool.setMainClass(TeaVMLauncher.class.getName());

//        tool.setDebugInformationGenerated(true);
//        tool.setSourceMapsFileGenerated(true);
//        tool.setSourceFilePolicy(TeaVMSourceFilePolicy.COPY);

        TeaBuilder.build(tool);
    }
}
