import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import java.io.File;
import java.io.IOException;
import org.teavm.vm.TeaVMOptimizationLevel;

public class Build {

    public static void main(String[] args) throws IOException {
        AssetFileHandle assetsPath = new AssetFileHandle("../../assets");
        WebBackend webBackend = new WebBackend();
        boolean startJetty = Boolean.parseBoolean(System.getProperty("jparser.web.startJetty", "true"));
        webBackend.setStartJettyAfterBuild(startJetty);
        new TeaCompiler(webBackend)
                .addAssets(assetsPath)
                .setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE)
                .setMainClass(TeaVMLauncher.class.getName())
                .setObfuscated(false)
                .build(new File("build/dist"));
    }
}
