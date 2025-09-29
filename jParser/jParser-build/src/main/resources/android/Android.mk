LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := %libName%
LOCAL_C_INCLUDES := %headerDirs%
 
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) %cppFlags%
LOCAL_LDLIBS := %linkerFlags%
LOCAL_ARM_MODE  := arm

FILE_LIST :=
%srcFiles%
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)
include $(BUILD_SHARED_LIBRARY)
LOCAL_DISABLE_FORMAT_STRING_CHECKS := true