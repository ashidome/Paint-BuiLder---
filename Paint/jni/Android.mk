LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := jniimage
LOCAL_SRC_FILES := jniimage.c

include $(BUILD_SHARED_LIBRARY)