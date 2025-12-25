#ifdef _WIN32
    #ifdef LIBA_EXPORTS
        #define LIBA_API __declspec(dllexport)
    #else
        #define LIBA_API __declspec(dllimport)
    #endif
#else
    #define LIBA_API __attribute__((visibility("default")))
#endif