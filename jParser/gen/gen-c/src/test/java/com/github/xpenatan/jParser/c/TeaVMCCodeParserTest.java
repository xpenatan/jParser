package com.github.xpenatan.jParser.c;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserItem;
import com.github.xpenatan.jParser.ffm.FFMNativeCodeGenerator;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TeaVMCCodeParserTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void generatesAlongsidePublicApiInGenCPackages() throws Exception {
        Path sourceRoot = temporaryFolder.newFolder("source").toPath();
        Path generatedRoot = temporaryFolder.newFolder("generated").toPath();
        Path cppRoot = temporaryFolder.newFolder("cpp").toPath();

        Path dependencySource = writeSource(sourceRoot, "com/example/types/Dependency.java",
                "package com.example.types;\n" +
                "public class Dependency {\n" +
                "}\n");
        Path apiSource = writeSource(sourceRoot, "com/example/api/PublicApi.java",
                "package com.example.api;\n" +
                "import com.example.types.Dependency;\n" +
                "public class PublicApi {\n" +
                "    public Dependency dependency;\n" +
                "}\n");

        RecordingTeaVMCCodeParser parser = generate(sourceRoot, generatedRoot, cppRoot);

        Path generatedDependency = generatedRoot.resolve("gen/c/com/example/types/Dependency.java");
        Path generatedApi = generatedRoot.resolve("gen/c/com/example/api/PublicApi.java");
        assertTrue(Files.isRegularFile(generatedDependency));
        assertTrue(Files.isRegularFile(generatedApi));
        assertFalse(Files.exists(generatedRoot.resolve("com/example/api/PublicApi.java")));
        assertTrue(parser.packagePaths.contains(path("gen/c/com/example/api")));
        assertTrue(parser.packagePaths.contains(path("gen/c/com/example/types")));

        String generatedApiText = readString(generatedApi);
        assertTrue(generatedApiText.contains("package gen.c.com.example.api;"));
        assertTrue(generatedApiText.contains("import gen.c.com.example.types.Dependency;"));

        Path classes = temporaryFolder.newFolder("classes").toPath();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue("Tests require a JDK", compiler != null);
        int result = compiler.run(null, null, null,
                "-d", classes.toString(),
                dependencySource.toString(), apiSource.toString(),
                generatedDependency.toString(), generatedApi.toString());
        assertTrue("Public and generated classes must compile together", result == 0);
    }

    @Test
    public void keepsNativeSymbolsBasedOnPublicPackage() throws Exception {
        Path sourceRoot = temporaryFolder.newFolder("native-source").toPath();
        Path generatedRoot = temporaryFolder.newFolder("native-generated").toPath();
        Path cppRoot = temporaryFolder.newFolder("native-cpp").toPath();

        writeSource(sourceRoot, "com/example/api/NativeApi.java",
                "package com.example.api;\n" +
                "public class NativeApi {\n" +
                "    /*[-TEAVM_C;-NATIVE]\n" +
                "        return 7;\n" +
                "    */\n" +
                "    public static native int internal_native_value();\n" +
                "}\n");

        generate(sourceRoot, generatedRoot, cppRoot);

        Path generatedApi = generatedRoot.resolve("gen/c/com/example/api/NativeApi.java");
        String generatedApiText = readString(generatedApi);
        String glueText = readString(cppRoot.resolve("teavmcglue/TeaVMCGlue.h"));
        String publicSymbol = "com_example_api_nativeapi_value";

        assertTrue(generatedApiText.contains("package gen.c.com.example.api;"));
        assertTrue(generatedApiText, generatedApiText.contains(publicSymbol));
        assertTrue(glueText.contains(publicSymbol));
        assertFalse(generatedApiText.contains("gen_c_com_example_api_nativeapi_value"));
        assertFalse(glueText.contains("gen_c_com_example_api_nativeapi_value"));
    }

    @Test
    public void keepsApiOnlyImportsInThePublicNamespace() throws Exception {
        Path sourceRoot = temporaryFolder.newFolder("api-source").toPath();
        Path generatedRoot = temporaryFolder.newFolder("api-generated").toPath();
        Path cppRoot = temporaryFolder.newFolder("api-cpp").toPath();

        writeSource(sourceRoot, "com/example/api/ApiBacked.java",
                "package com.example.api;\n" +
                "import com.github.xpenatan.jParser.api.NativeObject;\n" +
                "public class ApiBacked extends NativeObject {\n" +
                "}\n");

        generate(sourceRoot, generatedRoot, cppRoot);

        Path generatedApi = generatedRoot.resolve("gen/c/com/example/api/ApiBacked.java");
        String generatedApiText = readString(generatedApi);
        assertTrue(generatedApiText.contains("import com.github.xpenatan.jParser.api.NativeObject;"));
        assertFalse(generatedApiText.contains("import gen.c.com.github.xpenatan.jParser.api.NativeObject;"));

        Path classes = temporaryFolder.newFolder("api-classes").toPath();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue("Tests require a JDK", compiler != null);
        int result = compiler.run(null, null, null,
                "-classpath", System.getProperty("java.class.path"),
                "-d", classes.toString(),
                generatedApi.toString());
        assertTrue("Generated implementations must compile against public API-only types", result == 0);
    }

    private static RecordingTeaVMCCodeParser generate(Path sourceRoot, Path generatedRoot, Path cppRoot) throws Exception {
        TeaVMCGenerator generator = new TeaVMCGenerator(cppRoot.toString());
        RecordingTeaVMCCodeParser parser = new RecordingTeaVMCCodeParser(generator, new IDLReader(), "com.example", cppRoot.toString());
        parser.generateClass = true;
        JParser.generate(parser, sourceRoot.toString(), generatedRoot.toString());
        return parser;
    }

    private static Path writeSource(Path sourceRoot, String relativePath, String content) throws Exception {
        Path source = sourceRoot.resolve(relativePath.replace('/', File.separatorChar));
        Files.createDirectories(source.getParent());
        Files.write(source, content.getBytes(StandardCharsets.UTF_8));
        return source;
    }

    private static String readString(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static String path(String value) {
        return value.replace('/', File.separatorChar);
    }

    private static final class RecordingTeaVMCCodeParser extends TeaVMCCodeParser {
        private final List<String> packagePaths = new ArrayList<>();

        private RecordingTeaVMCCodeParser(FFMNativeCodeGenerator cppGenerator, IDLReader idlReader, String basePackage, String cppDir) {
            super(cppGenerator, idlReader, basePackage, cppDir);
        }

        @Override
        public void onParserComplete(JParser jParser, ArrayList<JParserItem> parserItems) {
            super.onParserComplete(jParser, parserItems);
            for(JParserItem parserItem : parserItems) {
                packagePaths.add(parserItem.packagePathName);
            }
        }
    }
}
