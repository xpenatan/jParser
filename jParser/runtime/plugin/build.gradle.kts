import com.github.xpenatan.jParser.builder.tool.JParserSymbolNameMode

plugins {
    id("com.github.xpenatan.jparser")
}

jParser {
    libName.set("runtime")
    modulePrefix.set("runtime")
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

    moduleJNISuffix.set("jvm/jni")
    moduleWebSuffix.set("jvm/web")
    moduleFFMSuffix.set("jvm/ffm")
    moduleCSuffix.set("c/core")

    jniCppStandard.set("c++17")
    ffmCppStandard.set("c++17")
    teaVMCCppStandard.set("c++17")
    webCppStandard.set("c++17")
}
