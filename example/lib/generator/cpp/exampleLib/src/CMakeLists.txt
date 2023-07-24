
INCLUDE_DIRECTORIES(
    ${EXAMPLELIB_SOURCE_DIR}/src
)

SET(CPP_SRCS
    NormalClass.cpp
    ParamClass.cpp
    ParentClass.cpp
    ReturnClass.cpp
)

SET(CPP_HDRS
    NormalClass.h
    ParamClass.h
    ParentClass.h
    ReturnClass.h
)

ADD_LIBRARY(ExampleLib ${CPP_SRCS} ${CPP_HDRS})