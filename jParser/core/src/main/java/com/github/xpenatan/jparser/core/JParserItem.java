package com.github.xpenatan.jparser.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author xpenatan
 */
public class JParserItem {
    public CompilationUnit unit;
    public String inputPath;
    public final String destinationBaseDir;
    public String packagePathName;
    public String className = "";
    public boolean notAllowed;

    public JParserItem(CompilationUnit unit, String inputPath, String destinationBaseDir) {
        this.unit = unit;
        this.inputPath = inputPath;
        this.destinationBaseDir = destinationBaseDir;

        List<ClassOrInterfaceDeclaration> all = unit.findAll(ClassOrInterfaceDeclaration.class);
        if(all.size() > 0) {
            className = all.get(0).getNameAsString();
            String packageName = unit.getPackageDeclaration().get().getNameAsString();
            this.packagePathName = packageName.replace(".", File.separator);
        }
        else {
            notAllowed = true;
            List<CompilationUnit> compiAll = unit.findAll(CompilationUnit.class);
            if(compiAll.size() > 0) {
                CompilationUnit compilationUnit = compiAll.get(0);
                NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
                if(types.size() > 0) {
                    className = types.get(0).getNameAsString();
                }
            }
        }
    }

    public String getFullDestinationPath() {
        String path = destinationBaseDir + File.separator + packagePathName + File.separator + className + ".java";
        return path;
    }

    public PackageDeclaration getPackage() {
        Optional<PackageDeclaration> optionalPackageDeclaration = unit.getPackageDeclaration();
        return optionalPackageDeclaration.orElse(null);
    }

    public ClassOrInterfaceDeclaration getClassDeclaration() {
        return JParserHelper.getClassDeclaration(unit);
    }

    @Override
    public String toString() {
        String name = "";
        if(!className.isEmpty()) {
            name = " " + className;
        }
        return super.toString() + name;
    }
}