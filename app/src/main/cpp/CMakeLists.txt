cmake_minimum_required(VERSION 3.4.1)

#include头文件目录
include_directories(include)

#source directory源文件目录
file(GLOB MTCNN_SRC *.h
        *.cpp)

set(MTCNN_COMPILE_CODE ${MTCNN_SRC})

#添加ncnn库
add_library(libncnn STATIC IMPORTED)
set_target_properties(libncnn
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libncnn.a)

#编译为动态库
add_library(mtcnn SHARED ${MTCNN_COMPILE_CODE})

#添加工程所依赖的库
find_library(  log-lib log )

target_link_libraries( mtcnn
        libncnn
        android
        jnigraphics
        z
        ${log-lib} )
