/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * MACHINE GENERATED FILE, DO NOT EDIT
 */
#include "lwjgl/common_tools.h"
#include "opengl.h"

typedef void (APIENTRY *glClientActiveTexturePROC) (jint);
typedef void (APIENTRY *glMultiTexCoord1fPROC) (jint, jfloat);
typedef void (APIENTRY *glMultiTexCoord1sPROC) (jint, jshort);
typedef void (APIENTRY *glMultiTexCoord1iPROC) (jint, jint);
typedef void (APIENTRY *glMultiTexCoord1dPROC) (jint, jdouble);
typedef void (APIENTRY *glMultiTexCoord1fvPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord1svPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord1ivPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord1dvPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord2fPROC) (jint, jfloat, jfloat);
typedef void (APIENTRY *glMultiTexCoord2sPROC) (jint, jshort, jshort);
typedef void (APIENTRY *glMultiTexCoord2iPROC) (jint, jint, jint);
typedef void (APIENTRY *glMultiTexCoord2dPROC) (jint, jdouble, jdouble);
typedef void (APIENTRY *glMultiTexCoord2fvPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord2svPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord2ivPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord2dvPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord3fPROC) (jint, jfloat, jfloat, jfloat);
typedef void (APIENTRY *glMultiTexCoord3sPROC) (jint, jshort, jshort, jshort);
typedef void (APIENTRY *glMultiTexCoord3iPROC) (jint, jint, jint, jint);
typedef void (APIENTRY *glMultiTexCoord3dPROC) (jint, jdouble, jdouble, jdouble);
typedef void (APIENTRY *glMultiTexCoord3fvPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord3svPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord3ivPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord3dvPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord4fPROC) (jint, jfloat, jfloat, jfloat, jfloat);
typedef void (APIENTRY *glMultiTexCoord4sPROC) (jint, jshort, jshort, jshort, jshort);
typedef void (APIENTRY *glMultiTexCoord4iPROC) (jint, jint, jint, jint, jint);
typedef void (APIENTRY *glMultiTexCoord4dPROC) (jint, jdouble, jdouble, jdouble, jdouble);
typedef void (APIENTRY *glMultiTexCoord4fvPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord4svPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord4ivPROC) (jint, uintptr_t);
typedef void (APIENTRY *glMultiTexCoord4dvPROC) (jint, uintptr_t);
typedef void (APIENTRY *glLoadTransposeMatrixfPROC) (uintptr_t);
typedef void (APIENTRY *glLoadTransposeMatrixdPROC) (uintptr_t);
typedef void (APIENTRY *glMultTransposeMatrixfPROC) (uintptr_t);
typedef void (APIENTRY *glMultTransposeMatrixdPROC) (uintptr_t);

EXTERN_C_ENTER

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glClientActiveTexture(JNIEnv *__env, jclass clazz, jint texture) {
    glClientActiveTexturePROC glClientActiveTexture = (glClientActiveTexturePROC)tlsGetFunction(349);
    UNUSED_PARAM(clazz)
    glClientActiveTexture(texture);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord1f(JNIEnv *__env, jclass clazz, jint texture, jfloat s) {
    glMultiTexCoord1fPROC glMultiTexCoord1f = (glMultiTexCoord1fPROC)tlsGetFunction(350);
    UNUSED_PARAM(clazz)
    glMultiTexCoord1f(texture, s);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord1s(JNIEnv *__env, jclass clazz, jint texture, jshort s) {
    glMultiTexCoord1sPROC glMultiTexCoord1s = (glMultiTexCoord1sPROC)tlsGetFunction(351);
    UNUSED_PARAM(clazz)
    glMultiTexCoord1s(texture, s);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord1i(JNIEnv *__env, jclass clazz, jint texture, jint s) {
    glMultiTexCoord1iPROC glMultiTexCoord1i = (glMultiTexCoord1iPROC)tlsGetFunction(352);
    UNUSED_PARAM(clazz)
    glMultiTexCoord1i(texture, s);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord1d(JNIEnv *__env, jclass clazz, jint texture, jdouble s) {
    glMultiTexCoord1dPROC glMultiTexCoord1d = (glMultiTexCoord1dPROC)tlsGetFunction(353);
    UNUSED_PARAM(clazz)
    glMultiTexCoord1d(texture, s);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord1fv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord1fvPROC glMultiTexCoord1fv = (glMultiTexCoord1fvPROC)tlsGetFunction(354);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord1fv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord1sv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord1svPROC glMultiTexCoord1sv = (glMultiTexCoord1svPROC)tlsGetFunction(355);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord1sv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord1iv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord1ivPROC glMultiTexCoord1iv = (glMultiTexCoord1ivPROC)tlsGetFunction(356);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord1iv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord1dv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord1dvPROC glMultiTexCoord1dv = (glMultiTexCoord1dvPROC)tlsGetFunction(357);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord1dv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord2f(JNIEnv *__env, jclass clazz, jint texture, jfloat s, jfloat t) {
    glMultiTexCoord2fPROC glMultiTexCoord2f = (glMultiTexCoord2fPROC)tlsGetFunction(358);
    UNUSED_PARAM(clazz)
    glMultiTexCoord2f(texture, s, t);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord2s(JNIEnv *__env, jclass clazz, jint texture, jshort s, jshort t) {
    glMultiTexCoord2sPROC glMultiTexCoord2s = (glMultiTexCoord2sPROC)tlsGetFunction(359);
    UNUSED_PARAM(clazz)
    glMultiTexCoord2s(texture, s, t);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord2i(JNIEnv *__env, jclass clazz, jint texture, jint s, jint t) {
    glMultiTexCoord2iPROC glMultiTexCoord2i = (glMultiTexCoord2iPROC)tlsGetFunction(360);
    UNUSED_PARAM(clazz)
    glMultiTexCoord2i(texture, s, t);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord2d(JNIEnv *__env, jclass clazz, jint texture, jdouble s, jdouble t) {
    glMultiTexCoord2dPROC glMultiTexCoord2d = (glMultiTexCoord2dPROC)tlsGetFunction(361);
    UNUSED_PARAM(clazz)
    glMultiTexCoord2d(texture, s, t);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord2fv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord2fvPROC glMultiTexCoord2fv = (glMultiTexCoord2fvPROC)tlsGetFunction(362);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord2fv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord2sv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord2svPROC glMultiTexCoord2sv = (glMultiTexCoord2svPROC)tlsGetFunction(363);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord2sv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord2iv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord2ivPROC glMultiTexCoord2iv = (glMultiTexCoord2ivPROC)tlsGetFunction(364);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord2iv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord2dv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord2dvPROC glMultiTexCoord2dv = (glMultiTexCoord2dvPROC)tlsGetFunction(365);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord2dv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord3f(JNIEnv *__env, jclass clazz, jint texture, jfloat s, jfloat t, jfloat r) {
    glMultiTexCoord3fPROC glMultiTexCoord3f = (glMultiTexCoord3fPROC)tlsGetFunction(366);
    UNUSED_PARAM(clazz)
    glMultiTexCoord3f(texture, s, t, r);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord3s(JNIEnv *__env, jclass clazz, jint texture, jshort s, jshort t, jshort r) {
    glMultiTexCoord3sPROC glMultiTexCoord3s = (glMultiTexCoord3sPROC)tlsGetFunction(367);
    UNUSED_PARAM(clazz)
    glMultiTexCoord3s(texture, s, t, r);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord3i(JNIEnv *__env, jclass clazz, jint texture, jint s, jint t, jint r) {
    glMultiTexCoord3iPROC glMultiTexCoord3i = (glMultiTexCoord3iPROC)tlsGetFunction(368);
    UNUSED_PARAM(clazz)
    glMultiTexCoord3i(texture, s, t, r);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord3d(JNIEnv *__env, jclass clazz, jint texture, jdouble s, jdouble t, jdouble r) {
    glMultiTexCoord3dPROC glMultiTexCoord3d = (glMultiTexCoord3dPROC)tlsGetFunction(369);
    UNUSED_PARAM(clazz)
    glMultiTexCoord3d(texture, s, t, r);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord3fv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord3fvPROC glMultiTexCoord3fv = (glMultiTexCoord3fvPROC)tlsGetFunction(370);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord3fv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord3sv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord3svPROC glMultiTexCoord3sv = (glMultiTexCoord3svPROC)tlsGetFunction(371);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord3sv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord3iv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord3ivPROC glMultiTexCoord3iv = (glMultiTexCoord3ivPROC)tlsGetFunction(372);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord3iv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord3dv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord3dvPROC glMultiTexCoord3dv = (glMultiTexCoord3dvPROC)tlsGetFunction(373);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord3dv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord4f(JNIEnv *__env, jclass clazz, jint texture, jfloat s, jfloat t, jfloat r, jfloat q) {
    glMultiTexCoord4fPROC glMultiTexCoord4f = (glMultiTexCoord4fPROC)tlsGetFunction(374);
    UNUSED_PARAM(clazz)
    glMultiTexCoord4f(texture, s, t, r, q);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord4s(JNIEnv *__env, jclass clazz, jint texture, jshort s, jshort t, jshort r, jshort q) {
    glMultiTexCoord4sPROC glMultiTexCoord4s = (glMultiTexCoord4sPROC)tlsGetFunction(375);
    UNUSED_PARAM(clazz)
    glMultiTexCoord4s(texture, s, t, r, q);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord4i(JNIEnv *__env, jclass clazz, jint texture, jint s, jint t, jint r, jint q) {
    glMultiTexCoord4iPROC glMultiTexCoord4i = (glMultiTexCoord4iPROC)tlsGetFunction(376);
    UNUSED_PARAM(clazz)
    glMultiTexCoord4i(texture, s, t, r, q);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_glMultiTexCoord4d(JNIEnv *__env, jclass clazz, jint texture, jdouble s, jdouble t, jdouble r, jdouble q) {
    glMultiTexCoord4dPROC glMultiTexCoord4d = (glMultiTexCoord4dPROC)tlsGetFunction(377);
    UNUSED_PARAM(clazz)
    glMultiTexCoord4d(texture, s, t, r, q);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord4fv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord4fvPROC glMultiTexCoord4fv = (glMultiTexCoord4fvPROC)tlsGetFunction(378);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord4fv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord4sv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord4svPROC glMultiTexCoord4sv = (glMultiTexCoord4svPROC)tlsGetFunction(379);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord4sv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord4iv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord4ivPROC glMultiTexCoord4iv = (glMultiTexCoord4ivPROC)tlsGetFunction(380);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord4iv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultiTexCoord4dv__IJ(JNIEnv *__env, jclass clazz, jint texture, jlong vAddress) {
    glMultiTexCoord4dvPROC glMultiTexCoord4dv = (glMultiTexCoord4dvPROC)tlsGetFunction(381);
    uintptr_t v = (uintptr_t)vAddress;
    UNUSED_PARAM(clazz)
    glMultiTexCoord4dv(texture, v);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglLoadTransposeMatrixf__J(JNIEnv *__env, jclass clazz, jlong mAddress) {
    glLoadTransposeMatrixfPROC glLoadTransposeMatrixf = (glLoadTransposeMatrixfPROC)tlsGetFunction(382);
    uintptr_t m = (uintptr_t)mAddress;
    UNUSED_PARAM(clazz)
    glLoadTransposeMatrixf(m);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglLoadTransposeMatrixd__J(JNIEnv *__env, jclass clazz, jlong mAddress) {
    glLoadTransposeMatrixdPROC glLoadTransposeMatrixd = (glLoadTransposeMatrixdPROC)tlsGetFunction(383);
    uintptr_t m = (uintptr_t)mAddress;
    UNUSED_PARAM(clazz)
    glLoadTransposeMatrixd(m);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultTransposeMatrixf__J(JNIEnv *__env, jclass clazz, jlong mAddress) {
    glMultTransposeMatrixfPROC glMultTransposeMatrixf = (glMultTransposeMatrixfPROC)tlsGetFunction(384);
    uintptr_t m = (uintptr_t)mAddress;
    UNUSED_PARAM(clazz)
    glMultTransposeMatrixf(m);
}

JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL13_nglMultTransposeMatrixd__J(JNIEnv *__env, jclass clazz, jlong mAddress) {
    glMultTransposeMatrixdPROC glMultTransposeMatrixd = (glMultTransposeMatrixdPROC)tlsGetFunction(385);
    uintptr_t m = (uintptr_t)mAddress;
    UNUSED_PARAM(clazz)
    glMultTransposeMatrixd(m);
}

EXTERN_C_EXIT
