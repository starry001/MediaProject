//
// Created by Administrator on 2018/7/23.
//

#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_example_surfaceview_util_JNIUtils_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}