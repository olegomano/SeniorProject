package com.projects.oleg.seniorproject.Camera;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import java.util.List;

/**
 * Created by Oleg Tolstov on 6:02 PM, 10/23/15. SeniorProject
 */
public abstract class CameraListener extends CameraCaptureSession.StateCallback {
    public abstract List<Surface> getSurfaceList();
    public abstract void configureBufferSize(int w, int h);
    public abstract void setCaptureRequest(CaptureRequest request);
    public abstract Class getType();
}
