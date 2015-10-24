package com.projects.oleg.seniorproject.Camera;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import com.projects.oleg.seniorproject.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg Tolstov on 6:12 PM, 10/23/15. SeniorProject
 */
public class CameraImage extends CameraListener {
    private CaptureRequest request;
    private ImageReader imgReader;
    private OnImageReadyListener listener;
    private HandlerThread callbackThread = new HandlerThread("ImageCaptureCallback");

    public CameraImage(OnImageReadyListener listener){
        this.listener = listener;
    }

    @Override
    public List<Surface> getSurfaceList() {
        ArrayList<Surface> surfList = new ArrayList<>(1);
        surfList.add(imgReader.getSurface());
        return surfList;
    }

    @Override
    public void configureBufferSize(int w, int h) {
        imgReader = ImageReader.newInstance(w,h,ImageFormat.JPEG,5);
        if(listener!=null){
            listener.configBuffers(w,h);
        }
    }

    @Override
    public void setCaptureRequest(CaptureRequest request) {
        this.request = request;
    }

    @Override
    public Class getType() {
        return ImageReader.class;
    }

    @Override
    public void onConfigured(CameraCaptureSession session) {
        callbackThread.start();
        try {
            Utils.print("Session Created, sending request");
            session.setRepeatingRequest(request,new CaptureCallback(),new Handler(callbackThread.getLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {

    }


    private class CaptureCallback extends CameraCaptureSession.CaptureCallback{
        public  void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            if(listener != null){
                Image newImage = imgReader.acquireLatestImage();
                if(newImage == null) return;

                Image.Plane[] imgPlanes = newImage.getPlanes();
                long time = System.nanoTime();
                byte[] imgData = new byte[imgPlanes[0].getBuffer().capacity()];
                for(int i = 0; i < imgData.length;i++){
                    imgData[i] = imgPlanes[0].getBuffer().get(i);
                }
                time = System.nanoTime() - time;

                Utils.print("Buffer to byteArray time: " + time*Utils.NANO_TO_SECOND);
                listener.onImageReady(imgData);
            }
        }
    }

    public interface OnImageReadyListener{
        public void onImageReady(byte[] data);
        public void configBuffers(int w, int h);
    }
}
