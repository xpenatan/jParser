#include "teavmc_loader.h"

#include <cstring>
#include <iostream>
#include <string>

extern "C" int loader_import_declaration_smoke(void);

namespace {
bool runtime_bound = false;

int32_t bindRuntimeApi(const JParserTeaVMCPluginApiHeader* api, char*, uint32_t) {
    runtime_bound = api != nullptr && api->backend_name != nullptr
            && std::strcmp(api->backend_name, "smoke") == 0;
    return runtime_bound ? 0 : 1;
}

const JParserTeaVMCLibraryDescriptor static_descriptor = {
        static_cast<uint32_t>(sizeof(JParserTeaVMCLibraryDescriptor)),
        JPARSER_TEAVMC_DESCRIPTOR_ABI_MAJOR,
        JPARSER_TEAVMC_DESCRIPTOR_ABI_MINOR,
        JPARSER_TEAVMC_LINKAGE_STATIC,
        "static-test",
        nullptr,
        0,
        0,
        0,
        0,
        0,
        0,
        nullptr
};

const JParserTeaVMCLibraryDescriptor shared_descriptor = {
        static_cast<uint32_t>(sizeof(JParserTeaVMCLibraryDescriptor)),
        JPARSER_TEAVMC_DESCRIPTOR_ABI_MAJOR,
        JPARSER_TEAVMC_DESCRIPTOR_ABI_MINOR,
        JPARSER_TEAVMC_LINKAGE_SHARED_LINKED,
        "shared-test",
        nullptr,
        0,
        0,
        0,
        0,
        0,
        0,
        nullptr
};

const JParserTeaVMCLibraryDescriptor runtime_descriptor = {
        static_cast<uint32_t>(sizeof(JParserTeaVMCLibraryDescriptor)),
        JPARSER_TEAVMC_DESCRIPTOR_ABI_MAJOR,
        JPARSER_TEAVMC_DESCRIPTOR_ABI_MINOR,
        JPARSER_TEAVMC_LINKAGE_RUNTIME_LOADED,
        "runtime-test",
        "jparser_teavmc_test_get_api_v1",
        JPARSER_TEAVMC_PLUGIN_ABI_MAJOR,
        JPARSER_TEAVMC_PLUGIN_ABI_MINOR,
        static_cast<uint32_t>(sizeof(JParserTeaVMCPluginApiHeader)),
        0,
        UINT64_C(0x0123456789ABCDEF),
        UINT64_C(0xFEDCBA9876543210),
        bindRuntimeApi
};

bool expect(int32_t actual, int32_t expected, const char* operation) {
    if(actual == expected) {
        return true;
    }
    char error[1024] = { 0 };
    jparser_teavmc_loader_error_copy(error, static_cast<int32_t>(sizeof(error)));
    std::cerr << operation << " returned " << actual << " instead of " << expected
              << ": " << error << std::endl;
    return false;
}
}

int main(int argc, char** argv) {
    if(argc != 3) {
        std::cerr << "Expected plugin directory and filename" << std::endl;
        return 1;
    }
    if(loader_import_declaration_smoke() != JPARSER_TEAVMC_LOADER_SUCCESS) {
        std::cerr << "Forced loader import declaration was not callable" << std::endl;
        return 12;
    }

    if(!expect(jparser_teavmc_loader_register(&static_descriptor),
            JPARSER_TEAVMC_LOADER_SUCCESS, "register static")) return 2;
    if(!expect(jparser_teavmc_loader_register(&shared_descriptor),
            JPARSER_TEAVMC_LOADER_SUCCESS, "register shared")) return 3;
    if(!expect(jparser_teavmc_loader_register(&runtime_descriptor),
            JPARSER_TEAVMC_LOADER_SUCCESS, "register runtime")) return 4;

#if defined(__APPLE__)
    // CTest runs from a directory other than the executable/plugin directory.
    // The implicit filename must therefore resolve through @loader_path.
    const char* runtime_directory = nullptr;
#else
    const char* runtime_directory = argv[1];
#endif

    if(!expect(jparser_teavmc_loader_load("static-test", nullptr, "missing.file", 1, 1),
            JPARSER_TEAVMC_LOADER_SUCCESS, "load static")) return 5;
    if(!expect(jparser_teavmc_loader_load("shared-test", nullptr, "missing.file", 1, 1),
            JPARSER_TEAVMC_LOADER_SUCCESS, "load shared")) return 6;
    if(!expect(jparser_teavmc_loader_load("runtime-test", runtime_directory, argv[2], 1, 1),
            JPARSER_TEAVMC_LOADER_SUCCESS, "load runtime")) return 7;
    if(!runtime_bound) {
        std::cerr << "Runtime descriptor bind callback was not called" << std::endl;
        return 8;
    }
    if(!expect(jparser_teavmc_loader_load("runtime-test", runtime_directory, argv[2], 1, 1),
            JPARSER_TEAVMC_LOADER_SUCCESS, "repeat runtime load")) return 9;
    if(!expect(jparser_teavmc_loader_load("runtime-test", runtime_directory, "different.file", 1, 1),
            JPARSER_TEAVMC_LOADER_ALREADY_BOUND, "conflicting runtime load")) return 10;
    if(jparser_teavmc_loader_error_size() <= 0) {
        std::cerr << "Conflicting load did not preserve an error message" << std::endl;
        return 11;
    }
    return 0;
}
