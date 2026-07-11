/* The CMake hook must force-include teavmc_loader.h for this declaration. */
int loader_import_declaration_smoke(void) {
    return jparser_teavmc_loader_error_code();
}
