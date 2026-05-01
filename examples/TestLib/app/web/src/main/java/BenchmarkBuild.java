import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import java.io.File;
import java.io.IOException;
import org.teavm.vm.TeaVMOptimizationLevel;

public class BenchmarkBuild {

    public static void main(String[] args) throws IOException {
        AssetFileHandle assetsPath = new AssetFileHandle("../assets");
        WebBackend webBackend = new WebBackend();
        webBackend.setStartJettyAfterBuild(true);
        new TeaCompiler(webBackend)
                .addAssets(assetsPath)
                .setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED)
                .setMainClass(BenchmarkLauncher.class.getName())
                .setObfuscated(false)
                .build(new File("build/dist"));
    }
}
