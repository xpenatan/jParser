package com.github.xpenatan.jParser.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author xpenatan
 */
public class JParserItem {
    public CompilationUnit unit;
    public final String destinationBaseDir;
    public String packagePathName;
    public String className = "";
    public boolean notAllowed;
    public boolean isIDL;

    public JParserItem(CompilationUnit unit, String destinationBaseDir) {
        this.unit = unit;
        this.destinationBaseDir = destinationBaseDir;
        if(unit != null) {
            List<EnumDeclaration> allEnum = unit.findAll(EnumDeclaration.class);
            List<ClassOrInterfaceDeclaration> all = unit.findAll(ClassOrInterfaceDeclaration.class);

            if(all.size() > 0) {
                className = all.get(0).getNameAsString();
            }
            else if(allEnum.size() > 0) {
                className = allEnum.get(0).getNameAsString();
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
    }

    public String getFullDestinationPath() {
        String packagePathName = "";
        Optional<PackageDeclaration> packageDeclaration = unit.getPackageDeclaration();
        if(packageDeclaration.isPresent()) {
            PackageDeclaration pkg = packageDeclaration.get();
            String pkgPath = pkg.getNameAsString().replace(".", File.separator);
            packagePathName = File.separator + pkgPath + File.separator + className + ".java";
        } else {
            packagePathName = "";
        }
        String path = destinationBaseDir + packagePathName;
        return path;
    }

    public PackageDeclaration getPackage() {
        if(unit == null) {
            return null;
        }
        Optional<PackageDeclaration> optionalPackageDeclaration = unit.getPackageDeclaration();
        return optionalPackageDeclaration.orElse(null);
    }

    public ClassOrInterfaceDeclaration getClassDeclaration() {
        if(unit == null) {
            return null;
        }
        return JParserHelper.getClassDeclaration(unit);
    }

    public EnumDeclaration getEnumDeclaration() {
        if(unit == null) {
            return null;
        }
        return JParserHelper.getEnumDeclaration(unit);
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