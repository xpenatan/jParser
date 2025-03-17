#include "IDLHelper.h"
#include "TestLib.h"

using TestEnumWithinClass = TestEnumClass::TestEnumWithinClass;
using TestEnumClassWithinClass = TestEnumClass::TestEnumClassWithinClass;
using TestEnumInNamespace = TestEnumNamespace::TestEnumInNamespace;

using TestObjectClassArray = IDLArray<TestObjectClass*>;