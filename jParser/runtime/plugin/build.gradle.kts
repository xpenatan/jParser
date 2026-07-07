import com.github.xpenatan.jParser.builder.tool.JParserSymbolNameMode

plugins {
    id("com.github.xpenatan.jparser")
}

jParser {
    libName.set("runtime")
    modulePrefix.set("")
    modulePath.set(file("..").absolutePath)
    packageName.set("com.github.xpenatan.jparser.runtime")
    webModuleName.set("runtime")

    runtimeHelper()
    addRuntimeHelperIDL.set(false)
    ffmDefaultCritical.set(true)
    ffmLogMethod.set(true)
    jniSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)
    ffmSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)
    teaVMCSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)

    moduleBaseSuffix.set("base")
    moduleBuildSuffix.set("builder")
    moduleCoreSuffix.set("core")
    moduleJNISuffix.set("shared/runtime-jni")
    moduleWebSuffix.set("web/runtime-web")
    moduleFFMSuffix.set("desktop/runtime-desktop-ffm")
    moduleCSuffix.set("shared/runtime-c")

    jniCppStandard.set("c++17")
    ffmCppStandard.set("c++17")
    teaVMCCppStandard.set("c++17")
    webCppStandard.set("c++17")
}
