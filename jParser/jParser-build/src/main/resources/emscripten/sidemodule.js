var [SIDE_MODULE_NAME] = (() => {
    return async function(moduleArg = {}) {
        var Module = moduleArg;

        function assert(condition, text) {
          if (!condition) {
            abort('Assertion failed' + (text ? ': ' + text : ''));
          }
        }

        var libName = "[SIDE_MODULE_WASM]";
        var isSuccess = await [MAIN_MODULE_NAME].loadDynamicLibrary(libName, { loadAsync: true, global: true, nodelete: true});
        var rawExports = [MAIN_MODULE_NAME].LDSO.loadedLibsByName[libName].exports;

        const modifiedExports = {};
        for (const [key, value] of Object.entries(rawExports)) {
            modifiedExports['_' + key] = value;  // Add '_' prefix only here
        }

        let evalCode = '';
        Object.keys(modifiedExports).forEach(key => {
          evalCode += `var ${key} = modifiedExports.${key}; `;
        });
        eval(evalCode);

        var runtimeInitialized = true;

[GLUE_CODE]

        Object.assign([MAIN_MODULE_NAME], Module);
        Object.assign(Module, modifiedExports);

        return Module;
    };
})();