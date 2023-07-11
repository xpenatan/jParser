#include "NormalClass.h"

NormalClass::NormalClass()
{
    int a = 1;
    int b = 2;
    int c = a + b;
    c++;
}

int NormalClass::addIntValue(int a, int b)
{
    return (a + b) * hiddenInt * hiddenParentInt;
}