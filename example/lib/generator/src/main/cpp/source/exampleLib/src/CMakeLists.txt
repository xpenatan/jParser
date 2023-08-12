
INCLUDE_DIRECTORIES(
    ${EXAMPLELIB_SOURCE_DIR}/src
)

SET(CPP_SRCS
    NormalClass.cpp
    subpackage/ParamData.cpp
    ParentClass.cpp
    ReturnClass.cpp
)

SET(CPP_HDRS
    NormalClass.h
    subpackage/ParamData.h
    ParentClass.h
    ReturnClass.h
    InterfaceClass.h
    DefaultInterface.h
)

ADD_LIBRARY(ExampleLib ${CPP_SRCS} ${CPP_HDRS})