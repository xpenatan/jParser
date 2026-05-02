package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;

public class IOSTarget extends DefaultBuildTarget {

    public String xcframeworkBundleIdentifier = "";
    public String platform = "iphonesimulator";
    public String minIOSVersion = "11.0";

    public static String iphoneosSdk =  "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk/";
    public static String iphoneSimulatorSdk = "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk";

    public IOSTarget() {
        this(SourceLanguage.CPP);
    }

    public IOSTarget(SourceLanguage language) {
        this.libDirSuffix = "ios/";
        this.tempBuildDir = "target/ios/";

        if(language == SourceLanguage.C) {
            String cppCompilerr = "clang";
            cppCompiler.add(cppCompilerr);
            linkerCompiler.add(cppCompilerr);
        }
        else if(language == SourceLanguage.CPP) {
            String cppCompilerr = "clang++";
            cppCompiler.add(cppCompilerr);
            linkerCompiler.add(cppCompilerr);
        }

        cppFlags.add("-isysroot" + iphoneSimulatorSdk);
        cppFlags.add("-arch x86_64");
        cppFlags.add("-mios-simulator-version-min=" + minIOSVersion);
        cppFlags.add("-d");
        cppFlags.add("-c");

        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        cppFlags.add("-stdlib=libc++");
    }

    @Override
    protected void setup(BuildConfig config) {
        CustomFileDescriptor iosDir = config.buildRootPath;
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
            libSuffix = "64_.a";
            linkerOutputCommand = "";
        }
        else {
            linkerFlags.add("-isysroot" + iphoneSimulatorSdk);
            linkerFlags.add("-arch x86_64");
            linkerFlags.add("-mios-simulator-version-min=" + minIOSVersion);
            linkerFlags.add("-shared");
            linkerFlags.add("-stdlib=libc++");
            libSuffix = "";
            linkerOutputCommand = "-o";
        }
    }
}