// THIS FILE WAS DOWNLOADED FROM EMSCRIPTEN WEBIDL TEST (09/AUG/2024)

#include "test.h"

Parent::Parent(int val) : value(val), attr(6), immutableAttr(8) {
  printf("Parent:%d\n", val);
}

Parent::Parent(Parent* p, Parent* q)
  : value(p->value + q->value), attr(6), immutableAttr(8) {
  printf("Parent:%d\n", value);
}

void Parent::mulVal(int mul) { value *= mul; }

