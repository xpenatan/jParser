#if defined(__APPLE__)
#include <signal.h>
#include <string.h>

static int jparser_ios_sigwaitinfo(const sigset_t* signals, siginfo_t* info) {
    int signal = 0;
    int result = sigwait(signals, &signal);
    if(result != 0) {
        return -1;
    }
    if(info != NULL) {
        memset(info, 0, sizeof(siginfo_t));
        info->si_signo = signal;
    }
    return signal;
}

#define sigwaitinfo jparser_ios_sigwaitinfo
#endif

#define main jparser_ios_teavm_main
#include "all.c"
#undef main
