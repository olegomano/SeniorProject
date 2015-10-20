LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := detection_based_tracker
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	/home/momo-chan/AndroidStudioProjects/SeniorProject/openCVSamplefacedetection/src/main/jni/DetectionBasedTracker_jni.cpp \
	/home/momo-chan/AndroidStudioProjects/SeniorProject/openCVSamplefacedetection/src/main/jni/Android.mk \
	/home/momo-chan/AndroidStudioProjects/SeniorProject/openCVSamplefacedetection/src/main/jni/Application.mk \

LOCAL_C_INCLUDES += /home/momo-chan/AndroidStudioProjects/SeniorProject/openCVSamplefacedetection/src/main/jni
LOCAL_C_INCLUDES += /home/momo-chan/AndroidStudioProjects/SeniorProject/openCVSamplefacedetection/src/debug/jni

include $(BUILD_SHARED_LIBRARY)
