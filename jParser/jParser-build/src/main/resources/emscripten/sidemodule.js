var [SIDE_MODULE_NAME] = (() => {
    return async function(moduleArg = {}) {
        var Module = moduleArg;

        function assert(condition, text) {
          if (!condition) {
            abort('Assertion failed' + (text ? ': ' + text : ''));
          }
        }

        function decodeBase64(base64) {
            var binary_string = atob(base64);
            var len = binary_string.length;
            var bytes = new Uint8Array(len);
            for (var i = 0; i < len; i++) {
                bytes[i] = binary_string.charCodeAt(i);
            }
            return bytes;
        }
        var wasmBinaryBase64 = "[WASM_BIN]";
        var wasmBinary = decodeBase64(wasmBinaryBase64);
        var libName = "[SIDE_MODULE_WASM]";
        var rawExports = window.[MAIN_MODULE_NAME].loadWebAssemblyModule(wasmBinary, { loadAsync: false, global: true, nodelete: true }, libName);

//        var libName = "[SIDE_MODULE_WASM]";
//        var isSuccess = await window.[MAIN_MODULE_NAME].loadDynamicLibrary(libName, { loadAsync: true, global: true, nodelete: true});
//        var rawExports = window.[MAIN_MODULE_NAME].LDSO.loadedLibsByName[libName].exports;

        const modifiedExports = {};
        for (const [key, value] of Object.entries(rawExports)) {
            modifiedExports['_' + key] = value;  // Add '_' prefix only here
        }

         Object.assign(Module, modifiedExports);

        let evalCode = '';
        Object.keys(modifiedExports).forEach(key => {
          evalCode += `var ${key} = modifiedExports.${key}; `;
        });
        eval(evalCode);

[GLUE_CODE]

        return Module;
    };
})();

[SIDE_MODULE_NAME]().then(function(r){
    window.[SIDE_MODULE_NAME] = r;
    window.[SIDE_MODULE_NAME]OnInit();
});