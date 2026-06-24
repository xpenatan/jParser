import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import org.teavm.diagnostics.Problem;
import org.teavm.diagnostics.ProblemProvider;
import org.teavm.diagnostics.ProblemTextConsumer;
import org.teavm.model.FieldReference;
import org.teavm.model.MethodReference;
import org.teavm.model.TextLocation;
import org.teavm.model.ValueType;
import org.teavm.tooling.ConsoleTeaVMToolLog;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

public class BuildTeaVMC {

    public static void main(String[] args) throws Exception {
        File outputDir = new File("build/teavm-c");
        deleteDirectory(outputDir);

        TeaVMTool tool = new TeaVMTool();
        tool.setTargetType(TeaVMTargetType.C);
        tool.setTargetDirectory(outputDir);
        tool.setTargetFileName("TestLibTeaVMC.c");
        tool.setMainClass("com.github.xpenatan.jParser.example.app.desktopc.TeaVMCHeadlessMain");
        tool.setEntryPointName("main");
        tool.setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE);
        tool.setObfuscated(false);
        tool.setLog(new ConsoleTeaVMToolLog(false));
        tool.setClassPath(currentClassPath());
        tool.generate();

        ProblemProvider problemProvider = tool.getProblemProvider();
        if(problemProvider != null && !problemProvider.getSevereProblems().isEmpty()) {
            throw new IllegalStateException(renderProblems(problemProvider.getSevereProblems()));
        }

        File mainFile = new File(outputDir, "main.c");
        File allFile = new File(outputDir, "all.c");
        if(!mainFile.exists() || !allFile.exists()) {
            throw new IllegalStateException("TeaVM C output was not generated in: " + outputDir.getAbsolutePath());
        }
    }

    private static void deleteDirectory(File directory) throws IOException {
        if(!directory.exists()) {
            return;
        }
        try(var paths = Files.walk(directory.toPath())) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        }
                        catch(IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private static List<File> currentClassPath() {
        String classPath = System.getProperty("java.class.path");
        String[] entries = classPath.split(File.pathSeparator);
        ArrayList<File> files = new ArrayList<>();
        for(String entry : entries) {
            if(!entry.isEmpty()) {
                files.add(new File(entry));
            }
        }
        return files;
    }

    private static String renderProblems(List<Problem> problems) {
        StringBuilder builder = new StringBuilder("TeaVM C build failed:");
        for(Problem problem : problems) {
            builder.append(System.lineSeparator()).append("- ");
            problem.render(new StringProblemConsumer(builder));
            if(problem.getLocation() != null) {
                builder.append(" at ").append(problem.getLocation());
            }
        }
        return builder.toString();
    }

    private static class StringProblemConsumer implements ProblemTextConsumer {
        private final StringBuilder builder;

        private StringProblemConsumer(StringBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void append(String text) {
            builder.append(text);
        }

        @Override
        public void appendClass(String className) {
            builder.append(className);
        }

        @Override
        public void appendType(ValueType type) {
            builder.append(type);
        }

        @Override
        public void appendMethod(MethodReference method) {
            builder.append(method);
        }

        @Override
        public void appendField(FieldReference field) {
            builder.append(field);
        }

        @Override
        public void appendLocation(TextLocation location) {
            builder.append(location);
        }
    }
}
