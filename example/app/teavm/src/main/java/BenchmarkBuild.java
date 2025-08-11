import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.config.TeaTargetType;
import java.io.File;
import java.io.IOException;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

public class BenchmarkBuild {

    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new AssetFileHandle("../desktop/assets"));
        teaBuildConfiguration.webappPath = new File("build/dist").getCanonicalPath();
        teaBuildConfiguration.targetType = TeaTargetType.WEBASSEMBLY;
        TeaBuilder.config(teaBuildConfiguration);
        TeaVMTool tool = new TeaVMTool();
        tool.setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED);
        tool.setMainClass(BenchmarkLauncher.class.getName());
        tool.setObfuscated(false);
        TeaBuilder.build(tool);
    }
}
