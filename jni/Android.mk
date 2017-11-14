LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS    :=-llog
LOCAL_MODULE    := CALLC
LOCAL_SRC_FILES := CALLC.cpp filter.c myfilter_emxAPI.c myfilter_emxutil.c myfilter_initialize.c myfilter_terminate.c myfilter.c rt_nonfinite.c rtGetInf.c rtGetNaN.c

include $(BUILD_SHARED_LIBRARY)
