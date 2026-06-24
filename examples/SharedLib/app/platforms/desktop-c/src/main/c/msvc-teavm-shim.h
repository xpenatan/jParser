#pragma once

#if defined(_MSC_VER) && !defined(__cplusplus)
typedef unsigned short char16_t;
typedef unsigned int char32_t;

#define localtime_s _localtime64_s
#define gmtime_s _gmtime64_s
#endif
