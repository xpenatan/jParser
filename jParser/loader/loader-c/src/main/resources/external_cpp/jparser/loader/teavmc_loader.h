#pragma once

#include <stdint.h>

#if defined(_WIN32)
#define JPARSER_TEAVMC_LOADER_EXPORT __declspec(dllexport)
#elif defined(__GNUC__) || defined(__clang__)
#define JPARSER_TEAVMC_LOADER_EXPORT __attribute__((visibility("default")))
#else
#define JPARSER_TEAVMC_LOADER_EXPORT
#endif

#define JPARSER_TEAVMC_PLUGIN_API_MAGIC UINT64_C(0x4A50544341504931)
#define JPARSER_TEAVMC_PLUGIN_ABI_MAJOR UINT16_C(1)
#define JPARSER_TEAVMC_PLUGIN_ABI_MINOR UINT16_C(0)
#define JPARSER_TEAVMC_DESCRIPTOR_ABI_MAJOR UINT16_C(1)
#define JPARSER_TEAVMC_DESCRIPTOR_ABI_MINOR UINT16_C(0)

#ifdef __cplusplus
extern "C" {
#endif

typedef enum JParserTeaVMCLinkageMode {
    JPARSER_TEAVMC_LINKAGE_STATIC = 0,
    JPARSER_TEAVMC_LINKAGE_SHARED_LINKED = 1,
    JPARSER_TEAVMC_LINKAGE_RUNTIME_LOADED = 2
} JParserTeaVMCLinkageMode;

typedef enum JParserTeaVMCLoaderResult {
    JPARSER_TEAVMC_LOADER_SUCCESS = 0,
    JPARSER_TEAVMC_LOADER_INVALID_ARGUMENT = 1,
    JPARSER_TEAVMC_LOADER_DESCRIPTOR_NOT_FOUND = 2,
    JPARSER_TEAVMC_LOADER_DESCRIPTOR_INVALID = 3,
    JPARSER_TEAVMC_LOADER_DESCRIPTOR_CONFLICT = 4,
    JPARSER_TEAVMC_LOADER_ALREADY_BOUND = 5,
    JPARSER_TEAVMC_LOADER_OPEN_FAILED = 6,
    JPARSER_TEAVMC_LOADER_PROVIDER_NOT_FOUND = 7,
    JPARSER_TEAVMC_LOADER_PLUGIN_API_INVALID = 8,
    JPARSER_TEAVMC_LOADER_PLUGIN_ABI_MISMATCH = 9,
    JPARSER_TEAVMC_LOADER_PLUGIN_SYMBOL_MISMATCH = 10,
    JPARSER_TEAVMC_LOADER_BIND_FAILED = 11,
    JPARSER_TEAVMC_LOADER_UNSUPPORTED = 12
} JParserTeaVMCLoaderResult;

/** Common prefix of every generated per-library plugin API table. */
typedef struct JParserTeaVMCPluginApiHeader {
    uint64_t magic;
    uint16_t abi_major;
    uint16_t abi_minor;
    uint32_t header_size;
    uint32_t api_size;
    uint32_t symbol_count;
    uint64_t fingerprint_hi;
    uint64_t fingerprint_lo;
    const char* logical_name;
    const char* backend_name;
} JParserTeaVMCPluginApiHeader;

typedef const JParserTeaVMCPluginApiHeader* (*JParserTeaVMCGetApiFn)(void);

/**
 * Generated shim callback. It must validate/cast the complete table, populate
 * temporary function pointers, and publish them only when returning success.
 */
typedef int32_t (*JParserTeaVMCBindApiFn)(
        const JParserTeaVMCPluginApiHeader* api,
        char* error_message,
        uint32_t error_message_capacity);

/** Registered by each generated per-library application shim in every mode. */
typedef struct JParserTeaVMCLibraryDescriptor {
    uint32_t struct_size;
    uint16_t descriptor_major;
    uint16_t descriptor_minor;
    uint32_t linkage_mode;
    const char* logical_name;
    const char* provider_symbol;
    uint16_t expected_abi_major;
    uint16_t expected_abi_minor;
    uint32_t expected_api_size;
    uint32_t expected_symbol_count;
    uint64_t expected_fingerprint_hi;
    uint64_t expected_fingerprint_lo;
    JParserTeaVMCBindApiFn bind_api;
} JParserTeaVMCLibraryDescriptor;

JPARSER_TEAVMC_LOADER_EXPORT int32_t jparser_teavmc_loader_register(
        const JParserTeaVMCLibraryDescriptor* descriptor);

JPARSER_TEAVMC_LOADER_EXPORT int32_t jparser_teavmc_loader_load(
        const char* logical_name,
        const char* path,
        const char* exact_file_name,
        int32_t auto_add_prefix,
        int32_t auto_add_suffix);

JPARSER_TEAVMC_LOADER_EXPORT int32_t jparser_teavmc_loader_error_code(void);
JPARSER_TEAVMC_LOADER_EXPORT int32_t jparser_teavmc_loader_error_size(void);
JPARSER_TEAVMC_LOADER_EXPORT int32_t jparser_teavmc_loader_error_copy(
        char* destination,
        int32_t capacity);

#ifdef __cplusplus
}

namespace jparser_teavmc {
class LibraryDescriptorRegistration {
public:
    explicit LibraryDescriptorRegistration(const JParserTeaVMCLibraryDescriptor* descriptor) {
        (void)jparser_teavmc_loader_register(descriptor);
    }
};
}

#define JPARSER_TEAVMC_DETAIL_JOIN_INNER(a, b) a##b
#define JPARSER_TEAVMC_DETAIL_JOIN(a, b) JPARSER_TEAVMC_DETAIL_JOIN_INNER(a, b)
#define JPARSER_TEAVMC_REGISTER_LIBRARY_DESCRIPTOR(descriptor) \
    static const ::jparser_teavmc::LibraryDescriptorRegistration \
            JPARSER_TEAVMC_DETAIL_JOIN(jparser_teavmc_descriptor_registration_, __LINE__)(&(descriptor))
#endif
