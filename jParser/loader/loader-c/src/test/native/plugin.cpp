#include "teavmc_loader.h"

namespace {
const JParserTeaVMCPluginApiHeader api = {
        JPARSER_TEAVMC_PLUGIN_API_MAGIC,
        JPARSER_TEAVMC_PLUGIN_ABI_MAJOR,
        JPARSER_TEAVMC_PLUGIN_ABI_MINOR,
        static_cast<uint32_t>(sizeof(JParserTeaVMCPluginApiHeader)),
        static_cast<uint32_t>(sizeof(JParserTeaVMCPluginApiHeader)),
        0,
        UINT64_C(0x0123456789ABCDEF),
        UINT64_C(0xFEDCBA9876543210),
        "runtime-test",
        "smoke"
};
}

extern "C" JPARSER_TEAVMC_LOADER_EXPORT const JParserTeaVMCPluginApiHeader*
jparser_teavmc_test_get_api_v1(void) {
    return &api;
}
