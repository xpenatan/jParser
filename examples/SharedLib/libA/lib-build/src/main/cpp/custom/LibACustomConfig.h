#ifdef LIBA_EXPORTS
#define LIBA_API __declspec(dllexport)
#else
#define LIBA_API __declspec(dllimport)
#endif