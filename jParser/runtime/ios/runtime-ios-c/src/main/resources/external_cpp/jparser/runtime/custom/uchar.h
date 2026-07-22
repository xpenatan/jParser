#pragma once

/*
 * TeaVM's C runtime expects the C11 <uchar.h> API, which Apple SDKs do not
 * provide. runtime-ios-c adds this header to the runtime custom include root,
 * which the existing jParser TeaVM C runtime hook already exposes.
 */

#include <errno.h>
#include <limits.h>
#include <stdint.h>
#include <wchar.h>

#if !defined(__cplusplus)
typedef uint16_t char16_t;
typedef uint32_t char32_t;
#endif

static inline size_t c16rtomb(char* destination, char16_t character, mbstate_t* state) {
    char resetBuffer[MB_LEN_MAX];
    if(destination == NULL) {
        destination = resetBuffer;
        character = 0;
    }
    return wcrtomb(destination, (wchar_t)character, state);
}

static inline size_t mbrtoc16(char16_t* destination, const char* source, size_t length, mbstate_t* state) {
    static const char resetCharacter = '\0';
    if(source == NULL) {
        source = &resetCharacter;
        length = 1;
    }

    wchar_t character = 0;
    size_t result = mbrtowc(&character, source, length, state);
    if(result == (size_t)-1 || result == (size_t)-2) {
        return result;
    }
#if WCHAR_MAX > UINT16_MAX
    if((uint32_t)character > UINT16_MAX) {
        errno = EILSEQ;
        return (size_t)-1;
    }
#endif
    if(destination != NULL) {
        *destination = (char16_t)character;
    }
    return result;
}
