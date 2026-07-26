package com.github.xpenatan.jParser.idl.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IDLFinalClassGenerationTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void defaultPolicyFinalizesOnlySafeLeafBindings() throws Exception {
        Generation generation = generate(
                "interface Parent {\n" +
                "};\n" +
                "interface Child {\n" +
                "};\n" +
                "Child implements Parent;\n" +
                "interface Leaf {\n" +
                "};\n" +
                "interface DisabledLeaf {\n" +
                "};\n" +
                "interface Callback {\n" +
                "};\n" +
                "[JSImplementation=\"Callback\"]\n" +
                "interface CallbackImpl {\n" +
                "    void CallbackImpl();\n" +
                "};\n" +
                "CallbackImpl implements Callback;\n",
                true,
                parser -> parser.setFinalClass("DisabledLeaf", false));

        assertFinal(generation, "Leaf", true);
        assertFinal(generation, "Child", true);
        assertFinal(generation, "DisabledLeaf", false);
        assertFinal(generation, "Parent", false);
        assertFinal(generation, "Callback", false);
    }

    @Test
    public void classOverrideCanEnableOneSafeLeafWhenGlobalDefaultIsOff() throws Exception {
        Generation generation = generate(
                "interface Parent {\n" +
                "};\n" +
                "interface Child {\n" +
                "};\n" +
                "Child implements Parent;\n" +
                "interface EnabledLeaf {\n" +
                "};\n" +
                "interface DefaultLeaf {\n" +
                "};\n" +
                "interface Callback {\n" +
                "};\n" +
                "[JSImplementation=\"Callback\"]\n" +
                "interface CallbackImpl {\n" +
                "    void CallbackImpl();\n" +
                "};\n" +
                "CallbackImpl implements Callback;\n",
                false,
                parser -> {
                    parser.setFinalClass("EnabledLeaf", true);
                    parser.setFinalClass("Parent", true);
                    parser.setFinalClass("Callback", true);
                });

        assertFinal(generation, "EnabledLeaf", true);
        assertFinal(generation, "DefaultLeaf", false);
        assertFinal(generation, "Child", false);
        assertFinal(generation, "Parent", false);
        assertFinal(generation, "Callback", false);
    }

    @Test
    public void completedJavaGraphProtectsParentsAndOverridesAuthoredModifier() throws Exception {
        Path sourceRoot = temporaryFolder.newFolder("source-java-graph").toPath();
        writeSource(sourceRoot, "com/example/JavaParent.java",
                "package com.example;\n" +
                "public class JavaParent {\n" +
                "}\n");
        writeSource(sourceRoot, "com/example/JavaHelper.java",
                "package com.example;\n" +
                "public class JavaHelper extends JavaParent {\n" +
                "}\n");
        writeSource(sourceRoot, "com/example/AnonymousParent.java",
                "package com.example;\n" +
                "public class AnonymousParent {\n" +
                "}\n");
        writeSource(sourceRoot, "com/example/JavaAnonymousFactory.java",
                "package com.example;\n" +
                "public class JavaAnonymousFactory {\n" +
                "    public AnonymousParent create() {\n" +
                "        return new AnonymousParent() { };\n" +
                "    }\n" +
                "}\n");
        writeSource(sourceRoot, "com/example/AuthoredFinal.java",
                "package com.example;\n" +
                "public final class AuthoredFinal {\n" +
                "}\n");

        Generation generation = generate(
                sourceRoot,
                "interface JavaParent {\n" +
                "};\n" +
                "interface AnonymousParent {\n" +
                "};\n" +
                "interface AuthoredFinal {\n" +
                "};\n",
                true,
                parser -> parser.setFinalClass("AuthoredFinal", false));

        assertFinal(generation, "JavaParent", false);
        assertFinal(generation, "AnonymousParent", false);
        assertFinal(generation, "AuthoredFinal", false);

        String helper = generation.read("JavaHelper");
        assertTrue(helper.contains("public class JavaHelper extends JavaParent"));
        assertFalse(helper.contains("public final class JavaHelper"));
    }

    @Test
    public void referenceIDLChildProtectsGeneratedParent() throws Exception {
        Path sourceRoot = temporaryFolder.newFolder("source-reference-child").toPath();
        Path generatedRoot = temporaryFolder.newFolder("generated-reference-child").toPath();
        Path mainIDL = writeIDL(
                "interface ReferencedParent {\n" +
                "};\n" +
                "interface LocalLeaf {\n" +
                "};\n");
        Path referenceIDL = writeIDL(
                "interface ReferenceChild {\n" +
                "};\n" +
                "ReferenceChild implements ReferencedParent;\n");

        IDLReader idlReader = IDLReader.readIDL(IDLReader.parseFile(mainIDL.toString()));
        IDLReader.addIDLRef(idlReader, IDLReader.parseFile(referenceIDL.toString()));
        IDLReader.setupClasses(idlReader);

        Generation generation = generate(
                sourceRoot,
                generatedRoot,
                idlReader,
                true,
                parser -> {
                });

        assertFinal(generation, "ReferencedParent", false);
        assertFinal(generation, "LocalLeaf", true);
        assertFalse(Files.exists(generatedRoot.resolve(
                "com" + File.separator + "example" + File.separator + "ReferenceChild.java")));
    }

    private Generation generate(
            String idl,
            boolean finalClass,
            ParserConfiguration configuration) throws Exception {
        Path sourceRoot = temporaryFolder.newFolder("source-" + System.nanoTime()).toPath();
        return generate(sourceRoot, idl, finalClass, configuration);
    }

    private Generation generate(
            Path sourceRoot,
            String idl,
            boolean finalClass,
            ParserConfiguration configuration) throws Exception {
        Path generatedRoot = temporaryFolder.newFolder("generated-" + System.nanoTime()).toPath();
        Path idlFile = writeIDL(idl);

        IDLReader idlReader = IDLReader.readIDL(IDLReader.parseFile(idlFile.toString()));
        IDLReader.setupClasses(idlReader);
        return generate(sourceRoot, generatedRoot, idlReader, finalClass, configuration);
    }

    private Generation generate(
            Path sourceRoot,
            Path generatedRoot,
            IDLReader idlReader,
            boolean finalClass,
            ParserConfiguration configuration) throws Exception {
        IDLDefaultCodeParser parser = new IDLDefaultCodeParser(
                "com.example",
                "CORE",
                idlReader,
                sourceRoot.toString());
        parser.generateClass = true;
        parser.generateNativeBindings = false;
        parser.finalClass = finalClass;
        configuration.configure(parser);

        JParser.generate(parser, sourceRoot.toString(), generatedRoot.toString());
        return new Generation(generatedRoot);
    }

    private Path writeIDL(String idl) throws Exception {
        Path idlFile = temporaryFolder.newFile("FinalClass-" + System.nanoTime() + ".idl").toPath();
        Files.write(idlFile, idl.getBytes(StandardCharsets.UTF_8));
        return idlFile;
    }

    private static void assertFinal(Generation generation, String className, boolean expected) throws Exception {
        String generated = generation.read(className);
        String declaration = "public final class " + className;
        if(expected) {
            assertTrue(generated, generated.contains(declaration));
        }
        else {
            assertFalse(generated, generated.contains(declaration));
            assertTrue(generated, generated.contains("public class " + className));
        }
    }

    private static void writeSource(Path sourceRoot, String relativePath, String content) throws Exception {
        Path source = sourceRoot.resolve(relativePath.replace('/', File.separatorChar));
        Files.createDirectories(source.getParent());
        Files.write(source, content.getBytes(StandardCharsets.UTF_8));
    }

    private interface ParserConfiguration {
        void configure(IDLDefaultCodeParser parser);
    }

    private static final class Generation {
        private final Path generatedRoot;

        private Generation(Path generatedRoot) {
            this.generatedRoot = generatedRoot;
        }

        private String read(String className) throws Exception {
            Path source = generatedRoot.resolve(
                    "com" + File.separator + "example" + File.separator + className + ".java");
            assertTrue("Missing generated class " + source, Files.isRegularFile(source));
            return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        }
    }
}
