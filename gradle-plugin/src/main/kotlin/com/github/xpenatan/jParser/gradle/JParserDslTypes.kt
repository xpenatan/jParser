package com.github.xpenatan.jParser.gradle

/** Symbol naming policies supported by jParser binding generators. */
enum class JParserSymbolNameMode {
    DEFAULT,
    OBFUSCATED
}

/** Native-library linkage modes supported by jParser's TeaVM C runtime. */
enum class TeaVMCLinkage {
    STATIC,
    SHARED_LINKED,
    RUNTIME_LOADED
}

/** Native source languages supported by jParser build targets. */
enum class SourceLanguage {
    C,
    CPP
}

/**
 * Android values exposed by the Gradle DSL.
 *
 * These intentionally mirror the generator values by name without making the
 * included plugin build depend on generator implementation classes.
 */
object AndroidTarget {
    enum class Target {
        arm64_v8a,
        armeabi_v7a,
        x86_64,
        x86
    }

    enum class ApiLevel {
        Android_16_36,
        Android_15_35,
        Android_14_34,
        Android_13_33,
        Android_12_32,
        Android_12_31,
        Android_11_30,
        Android_10_29,
        Android_09_28
    }
}

/** Minimal IDL type description passed to Gradle-side renaming callbacks. */
data class IDLClassOrEnum(
    val name: String,
    val subPackage: String?,
    private val classType: Boolean,
    private val enumType: Boolean
) {
    fun isClass(): Boolean = classType

    fun isEnum(): Boolean = enumType
}

/**
 * Gradle-side IDL renaming callback.
 *
 * The generator invokes this through a JDK-only callback bridge so the Gradle
 * plugin remains independent of generator implementation classes.
 */
interface IDLRenaming {
    fun obtainNewPackage(idlClassOrEnum: IDLClassOrEnum, classPackage: String): String {
        return classPackage
    }

    fun getIDLMethodName(methodName: String): String {
        return methodName
    }

    fun getIDLEnumName(enumName: String): String {
        return enumName
    }
}
