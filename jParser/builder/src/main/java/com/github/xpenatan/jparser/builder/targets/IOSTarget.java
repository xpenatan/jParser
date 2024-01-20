package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildTarget;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class IOSTarget extends BuildTarget {

    public String xcframeworkBundleIdentifier = "";
    public String platform = "iphonesimulator";
    public String minIOSVersion = "11.0";

    public static String iphoneosSdk =  "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk/";
    public static String iphoneSimulatorSdk = "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk";

    public IOSTarget() {
        this.libDirSuffix = "ios/";
        this.tempBuildDir = "target/ios";

        cppCompiler.clear();
        linkerCompiler.clear();
        String cppCompilerr = "clang++";
        cppCompiler.add(cppCompilerr);
        linkerCompiler.add(cppCompilerr);

//        cppFlags.add("-isysroot " + iphoneSimulatorSdk + " -arch x86_64 -mios-simulator-version-min=" + minIOSVersion);
        cppFlags.add("-c");
        cppFlags.add("-arch x86_64");
        cppFlags.add("-mios-simulator-version-min=" + minIOSVersion);
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        cppFlags.add("-stdlib=libc++");
        cppFlags.add("-std=c++17");
        
        linkerFlags.add("-shared");
        linkerFlags.add("-stdlib=libc++");
    }

    @Override
    protected void setup(BuildConfig config) {
        CustomFileDescriptor iosDir = config.buildDir;
        if(!iosDir.exists()) {
            iosDir.mkdirs();
        }

        CustomFileDescriptor template = new CustomFileDescriptor("ios/Info.plist", CustomFileDescriptor.FileType.Classpath);
        String templateStr = template.readString();
        templateStr = templateStr.replace("%libName%", libName);
        templateStr = templateStr.replace("%identifier%", xcframeworkBundleIdentifier);
        templateStr = templateStr.replace("%platform%", platform);
        templateStr = templateStr.replace("%minIOSVersion%", minIOSVersion);
        CustomFileDescriptor applicationFile = iosDir.child(template.name());
        applicationFile.writeString(templateStr, false);

        if(isStatic) {
            linkerCompiler.clear();
            linkerCompiler.add("libtool");
            linkerFlags.add("-static");
            linkerFlags.add("-o");
            libSuffix = "64.a";
        }
        else {
            linkerFlags.add("-shared");
            linkerFlags.add("-stdlib=libc++");
            libSuffix = "";
        }
    }

    @Override
    protected void onLink(ArrayList<CustomFileDescriptor> compiledObjects, String objFilePath, String libPath) {
        if(isStatic) {
            linkerCommands.addAll(linkerCompiler);
            linkerCommands.addAll(linkerFlags);
            linkerCommands.add(libPath);
            linkerCommands.add("@" + objFilePath);
        }
        else {
            super.onLink(compiledObjects, objFilePath, libPath);
        }
    }
}