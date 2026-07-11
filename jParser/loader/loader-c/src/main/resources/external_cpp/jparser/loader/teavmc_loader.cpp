#include "teavmc_loader.h"

#include <algorithm>
#include <cstring>
#include <limits>
#include <mutex>
#include <sstream>
#include <string>
#include <unordered_map>

#if defined(_WIN32)
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#ifndef NOMINMAX
#define NOMINMAX
#endif
#include <windows.h>
#ifndef LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR
#define LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR 0x00000100
#endif
#ifndef LOAD_LIBRARY_SEARCH_DEFAULT_DIRS
#define LOAD_LIBRARY_SEARCH_DEFAULT_DIRS 0x00001000
#endif
#else
#include <dlfcn.h>
#endif

namespace {

struct LibraryRecord {
    const JParserTeaVMCLibraryDescriptor* descriptor = nullptr;
    void* handle = nullptr;
    bool bound = false;
    std::string physical_path;
};

thread_local int32_t last_error_code = JPARSER_TEAVMC_LOADER_SUCCESS;
thread_local std::string last_error_message;

std::unordered_map<std::string, LibraryRecord>& registry() {
    static std::unordered_map<std::string, LibraryRecord> value;
    return value;
}

std::mutex& registryMutex() {
    static std::mutex value;
    return value;
}

int32_t setError(int32_t code, const std::string& message) {
    last_error_code = code;
    last_error_message = message;
    return code;
}

void clearError() {
    last_error_code = JPARSER_TEAVMC_LOADER_SUCCESS;
    last_error_message.clear();
}

bool hasText(const char* value) {
    return value != nullptr && value[0] != '\0';
}

bool isSupportedLinkage(uint32_t linkage) {
    return linkage == JPARSER_TEAVMC_LINKAGE_STATIC
            || linkage == JPARSER_TEAVMC_LINKAGE_SHARED_LINKED
            || linkage == JPARSER_TEAVMC_LINKAGE_RUNTIME_LOADED;
}

int32_t validateDescriptor(const JParserTeaVMCLibraryDescriptor* descriptor) {
    if(descriptor == nullptr || descriptor->struct_size < sizeof(JParserTeaVMCLibraryDescriptor)) {
        return setError(JPARSER_TEAVMC_LOADER_DESCRIPTOR_INVALID,
                "TeaVM C library descriptor is null or smaller than the loader ABI requires");
    }
    if(descriptor->descriptor_major != JPARSER_TEAVMC_DESCRIPTOR_ABI_MAJOR
            || descriptor->descriptor_minor > JPARSER_TEAVMC_DESCRIPTOR_ABI_MINOR) {
        return setError(JPARSER_TEAVMC_LOADER_DESCRIPTOR_INVALID,
                "TeaVM C library descriptor ABI is not supported by this loader");
    }
    if(!hasText(descriptor->logical_name)) {
        return setError(JPARSER_TEAVMC_LOADER_DESCRIPTOR_INVALID,
                "TeaVM C library descriptor has no logical library name");
    }
    if(!isSupportedLinkage(descriptor->linkage_mode)) {
        return setError(JPARSER_TEAVMC_LOADER_DESCRIPTOR_INVALID,
                "TeaVM C library descriptor has an unknown linkage mode");
    }
    if(descriptor->linkage_mode == JPARSER_TEAVMC_LINKAGE_RUNTIME_LOADED) {
        if(!hasText(descriptor->provider_symbol) || descriptor->bind_api == nullptr) {
            return setError(JPARSER_TEAVMC_LOADER_DESCRIPTOR_INVALID,
                    "Runtime-loaded TeaVM C descriptor requires a provider symbol and bind callback");
        }
        if(descriptor->expected_abi_major == 0
                || descriptor->expected_api_size < sizeof(JParserTeaVMCPluginApiHeader)) {
            return setError(JPARSER_TEAVMC_LOADER_DESCRIPTOR_INVALID,
                    "Runtime-loaded TeaVM C descriptor has invalid expected ABI metadata");
        }
    }
    return JPARSER_TEAVMC_LOADER_SUCCESS;
}

bool isAbsolutePath(const std::string& value) {
    if(value.empty()) {
        return false;
    }
#if defined(_WIN32)
    return value[0] == '/' || value[0] == '\\'
            || (value.size() > 2 && value[1] == ':'
                    && (value[2] == '/' || value[2] == '\\'));
#else
    return value[0] == '/';
#endif
}

std::string architectureSuffix() {
#if defined(__ANDROID__)
    return "";
#elif defined(_M_X64) || defined(__x86_64__) || defined(__amd64__)
    return "64";
#elif defined(_M_ARM64) || defined(__aarch64__) || defined(__arm64__)
    return "arm64";
#elif defined(_M_ARM) || defined(__arm__)
    return "arm";
#elif defined(__riscv) && __riscv_xlen == 64
    return "riscv64";
#elif defined(__riscv)
    return "riscv";
#elif defined(__loongarch64)
    return "loongarch64";
#else
    return "";
#endif
}

const char* platformPrefix() {
#if defined(_WIN32)
    return "";
#else
    return "lib";
#endif
}

const char* platformExtension() {
#if defined(_WIN32)
    return ".dll";
#elif defined(__APPLE__)
    return ".dylib";
#else
    return ".so";
#endif
}

std::string joinPath(const std::string& directory, const std::string& file) {
    if(directory.empty() || isAbsolutePath(file)) {
        return file;
    }
    char last = directory[directory.size() - 1];
    if(last == '/' || last == '\\') {
        return directory + file;
    }
    return directory + "/" + file;
}

std::string physicalLibraryPath(const char* logical_name, const char* path,
                                const char* exact_file_name, bool auto_add_prefix,
                                bool auto_add_suffix) {
    std::string file;
    if(hasText(exact_file_name)) {
        file = exact_file_name;
    }
    else {
        if(auto_add_prefix) {
            file += platformPrefix();
        }
        file += logical_name;
        if(auto_add_suffix) {
            file += architectureSuffix();
        }
        file += platformExtension();
    }
#if defined(__APPLE__)
    // Darwin does not resolve a bare dlopen leaf through LC_RPATH. Dynamic
    // payloads are staged beside the target, so anchor only the implicit leaf
    // case there. Explicit directories, absolute paths, and dyld-token paths
    // embedded in exact_file_name remain untouched.
    if(!hasText(path) && !isAbsolutePath(file)
            && file.find('/') == std::string::npos) {
        return joinPath("@loader_path", file);
    }
#endif
    return joinPath(hasText(path) ? path : "", file);
}

#if defined(_WIN32)

std::wstring utf8ToWide(const std::string& value) {
    if(value.empty()) {
        return std::wstring();
    }
    int count = MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, value.c_str(),
            static_cast<int>(value.size()), nullptr, 0);
    if(count <= 0) {
        return std::wstring();
    }
    std::wstring result(static_cast<size_t>(count), L'\0');
    MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, value.c_str(),
            static_cast<int>(value.size()), &result[0], count);
    return result;
}

std::string wideToUtf8(const wchar_t* value, int length) {
    if(value == nullptr || length <= 0) {
        return std::string();
    }
    int count = WideCharToMultiByte(CP_UTF8, 0, value, length, nullptr, 0, nullptr, nullptr);
    if(count <= 0) {
        return std::string();
    }
    std::string result(static_cast<size_t>(count), '\0');
    WideCharToMultiByte(CP_UTF8, 0, value, length, &result[0], count, nullptr, nullptr);
    return result;
}

std::string windowsError(DWORD error) {
    wchar_t* buffer = nullptr;
    DWORD length = FormatMessageW(FORMAT_MESSAGE_ALLOCATE_BUFFER
                    | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
            nullptr, error, 0, reinterpret_cast<wchar_t*>(&buffer), 0, nullptr);
    std::string message = wideToUtf8(buffer, static_cast<int>(length));
    if(buffer != nullptr) {
        LocalFree(buffer);
    }
    while(!message.empty() && (message.back() == '\r' || message.back() == '\n')) {
        message.pop_back();
    }
    if(message.empty()) {
        message = "Windows error " + std::to_string(static_cast<unsigned long>(error));
    }
    return message;
}

void* openLibrary(const std::string& path, std::string& error) {
    std::wstring wide_path = utf8ToWide(path);
    if(wide_path.empty()) {
        error = "Native library path is not valid UTF-8";
        return nullptr;
    }
    DWORD flags = LOAD_LIBRARY_SEARCH_DEFAULT_DIRS;
    if(path.find('/') != std::string::npos || path.find('\\') != std::string::npos) {
        flags |= LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR;
    }
    HMODULE handle = LoadLibraryExW(wide_path.c_str(), nullptr, flags);
    if(handle == nullptr && GetLastError() == ERROR_INVALID_PARAMETER) {
        handle = LoadLibraryExW(wide_path.c_str(), nullptr, 0);
    }
    if(handle == nullptr) {
        error = windowsError(GetLastError());
    }
    return reinterpret_cast<void*>(handle);
}

void* resolveSymbol(void* handle, const char* symbol, std::string& error) {
    FARPROC address = GetProcAddress(reinterpret_cast<HMODULE>(handle), symbol);
    if(address == nullptr) {
        error = windowsError(GetLastError());
        return nullptr;
    }
    return reinterpret_cast<void*>(address);
}

void closeLibrary(void* handle) {
    if(handle != nullptr) {
        FreeLibrary(reinterpret_cast<HMODULE>(handle));
    }
}

#else

void* openLibrary(const std::string& path, std::string& error) {
    dlerror();
    void* handle = dlopen(path.c_str(), RTLD_NOW | RTLD_LOCAL);
    if(handle == nullptr) {
        const char* detail = dlerror();
        error = detail != nullptr ? detail : "dlopen failed without an error message";
    }
    return handle;
}

void* resolveSymbol(void* handle, const char* symbol, std::string& error) {
    dlerror();
    void* address = dlsym(handle, symbol);
    const char* detail = dlerror();
    if(detail != nullptr) {
        error = detail;
        return nullptr;
    }
    return address;
}

void closeLibrary(void* handle) {
    if(handle != nullptr) {
        dlclose(handle);
    }
}

#endif

int32_t validatePluginApi(const JParserTeaVMCLibraryDescriptor* descriptor,
                          const JParserTeaVMCPluginApiHeader* api) {
    if(api == nullptr || api->magic != JPARSER_TEAVMC_PLUGIN_API_MAGIC
            || api->header_size < sizeof(JParserTeaVMCPluginApiHeader)
            || api->api_size < api->header_size || !hasText(api->logical_name)) {
        return setError(JPARSER_TEAVMC_LOADER_PLUGIN_API_INVALID,
                "Native plugin returned an invalid TeaVM C API header");
    }
    if(std::strcmp(api->logical_name, descriptor->logical_name) != 0) {
        return setError(JPARSER_TEAVMC_LOADER_PLUGIN_API_INVALID,
                "Native plugin logical library name does not match the registered descriptor");
    }
    if(api->abi_major != descriptor->expected_abi_major
            || api->abi_minor != descriptor->expected_abi_minor) {
        return setError(JPARSER_TEAVMC_LOADER_PLUGIN_ABI_MISMATCH,
                "Native plugin TeaVM C ABI version does not match the registered descriptor");
    }
    if(api->api_size != descriptor->expected_api_size
            || api->symbol_count != descriptor->expected_symbol_count
            || api->fingerprint_hi != descriptor->expected_fingerprint_hi
            || api->fingerprint_lo != descriptor->expected_fingerprint_lo) {
        return setError(JPARSER_TEAVMC_LOADER_PLUGIN_SYMBOL_MISMATCH,
                "Native plugin TeaVM C API size, symbol count, or fingerprint does not match");
    }
    return JPARSER_TEAVMC_LOADER_SUCCESS;
}

std::string loadFailure(const std::string& action, const std::string& library,
                        const std::string& detail) {
    std::ostringstream message;
    message << action << " '" << library << "'";
    if(!detail.empty()) {
        message << ": " << detail;
    }
    return message.str();
}

} // namespace

extern "C" {

int32_t jparser_teavmc_loader_register(const JParserTeaVMCLibraryDescriptor* descriptor) {
    clearError();
    int32_t validation = validateDescriptor(descriptor);
    if(validation != JPARSER_TEAVMC_LOADER_SUCCESS) {
        return validation;
    }

    std::lock_guard<std::mutex> lock(registryMutex());
    auto found = registry().find(descriptor->logical_name);
    if(found != registry().end()) {
        if(found->second.descriptor == descriptor) {
            return JPARSER_TEAVMC_LOADER_SUCCESS;
        }
        return setError(JPARSER_TEAVMC_LOADER_DESCRIPTOR_CONFLICT,
                "More than one TeaVM C descriptor registered for logical library '"
                        + std::string(descriptor->logical_name) + "'");
    }

    LibraryRecord record;
    record.descriptor = descriptor;
    registry().emplace(descriptor->logical_name, record);
    return JPARSER_TEAVMC_LOADER_SUCCESS;
}

int32_t jparser_teavmc_loader_load(const char* logical_name, const char* path,
                                   const char* exact_file_name, int32_t auto_add_prefix,
                                   int32_t auto_add_suffix) {
    clearError();
    if(!hasText(logical_name)) {
        return setError(JPARSER_TEAVMC_LOADER_INVALID_ARGUMENT,
                "TeaVM C loader requires a logical library name");
    }

    std::lock_guard<std::mutex> lock(registryMutex());
    auto found = registry().find(logical_name);
    if(found == registry().end()) {
        return setError(JPARSER_TEAVMC_LOADER_DESCRIPTOR_NOT_FOUND,
                "No TeaVM C descriptor registered for logical library '"
                        + std::string(logical_name) + "'");
    }

    LibraryRecord& record = found->second;
    const JParserTeaVMCLibraryDescriptor* descriptor = record.descriptor;
    if(descriptor->linkage_mode == JPARSER_TEAVMC_LINKAGE_STATIC
            || descriptor->linkage_mode == JPARSER_TEAVMC_LINKAGE_SHARED_LINKED) {
        record.bound = true;
        return JPARSER_TEAVMC_LOADER_SUCCESS;
    }

    std::string physical_path = physicalLibraryPath(logical_name, path, exact_file_name,
            auto_add_prefix != 0, auto_add_suffix != 0);
    if(record.bound) {
        if(record.physical_path == physical_path) {
            return JPARSER_TEAVMC_LOADER_SUCCESS;
        }
        return setError(JPARSER_TEAVMC_LOADER_ALREADY_BOUND,
                "Logical TeaVM C library '" + std::string(logical_name)
                        + "' is already bound to '" + record.physical_path
                        + "' and cannot be rebound to '" + physical_path + "'");
    }

    std::string os_error;
    void* handle = openLibrary(physical_path, os_error);
    if(handle == nullptr) {
        return setError(JPARSER_TEAVMC_LOADER_OPEN_FAILED,
                loadFailure("Unable to open native library", physical_path, os_error));
    }

    void* provider_address = resolveSymbol(handle, descriptor->provider_symbol, os_error);
    if(provider_address == nullptr) {
        closeLibrary(handle);
        return setError(JPARSER_TEAVMC_LOADER_PROVIDER_NOT_FOUND,
                loadFailure("Unable to resolve TeaVM C provider '"
                        + std::string(descriptor->provider_symbol) + "' from", physical_path, os_error));
    }

    JParserTeaVMCGetApiFn provider = reinterpret_cast<JParserTeaVMCGetApiFn>(provider_address);
    const JParserTeaVMCPluginApiHeader* api = provider();
    int32_t validation = validatePluginApi(descriptor, api);
    if(validation != JPARSER_TEAVMC_LOADER_SUCCESS) {
        closeLibrary(handle);
        return validation;
    }

    char bind_error[1024] = { 0 };
    int32_t bind_result = descriptor->bind_api(api, bind_error,
            static_cast<uint32_t>(sizeof(bind_error)));
    if(bind_result != 0) {
        closeLibrary(handle);
        std::string detail = bind_error[0] != '\0' ? bind_error : "generated shim rejected the API table";
        return setError(JPARSER_TEAVMC_LOADER_BIND_FAILED,
                loadFailure("Unable to bind native library", physical_path, detail));
    }

    record.handle = handle;
    record.bound = true;
    record.physical_path = physical_path;
    return JPARSER_TEAVMC_LOADER_SUCCESS;
}

int32_t jparser_teavmc_loader_error_code(void) {
    return last_error_code;
}

int32_t jparser_teavmc_loader_error_size(void) {
    size_t size = last_error_message.size();
    size_t maximum = static_cast<size_t>(std::numeric_limits<int32_t>::max());
    return static_cast<int32_t>(std::min(size, maximum));
}

int32_t jparser_teavmc_loader_error_copy(char* destination, int32_t capacity) {
    if(destination == nullptr || capacity <= 0) {
        return 0;
    }
    size_t maximum = static_cast<size_t>(capacity - 1);
    size_t count = std::min(last_error_message.size(), maximum);
    if(count > 0) {
        std::memcpy(destination, last_error_message.data(), count);
    }
    destination[count] = '\0';
    return static_cast<int32_t>(count);
}

} // extern "C"
