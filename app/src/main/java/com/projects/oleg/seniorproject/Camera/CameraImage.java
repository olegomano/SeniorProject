package com.projects.oleg.seniorproject.Camera;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.view.Surface;

import com.projects.oleg.seniorproject.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg Tolstov on 6:12 PM, 10/23/15. SeniorProject
 */
public class CameraImage extends CameraListener implements ImageReader.OnImageAvailableListener {
    private CaptureRequest request;
    private ImageReader imgReader;
    private OnImageReadyListener listener;
    private HandlerThread callbackThread = new HandlerThread("ImageCaptureCallback");
    private HandlerThread nThread = new HandlerThread("ThreadCallback");

    private int imgW;
    private int imgH;
    private int imageBuffSize = 60;

    public CameraImage(OnImageReadyListener listener){
        this.listener = listener;
        callbackThread.start();
    }

    @Override
    public List<Surface> getSurfaceList() {
        ArrayList<Surface> surfList = new ArrayList<>(1);
        surfList.add(imgReader.getSurface());
        return surfList;
    }

    @Override
    public void configureBufferSize(int w, int h) {
        imgReader = ImageReader.newInstance(w,h,ImageFormat.YUV_420_888,imageBuffSize);
        imgReader.setOnImageAvailableListener(this, new Handler(callbackThread.getLooper()));
        imgW = w;
        imgH = h;
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
        nThread.start();
        try {
            Utils.print("Session Created, sending request");
            session.setRepeatingRequest(request,new CaptureCallback(),new Handler(nThread.getLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
            nThread.interrupt();
        }
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {

    }
    private long lastPicTime = System.nanoTime();
    private int fps = 0;
    private volatile Face detectedFace;
    private volatile Object faceLock = new Object();
    @Override
    public void onImageAvailable(ImageReader reader) {
        Image newImage;
        if(listener != null){
            fps++;
            if(System.nanoTime() - lastPicTime > 1000000000){
                Utils.print("Camera fps: " + fps);
                fps =0;
                lastPicTime=System.nanoTime();
            }
            newImage = imgReader.acquireLatestImage();
            if(newImage == null) return;
            Image.Plane[] imgPlanes = newImage.getPlanes();
            long time = System.nanoTime();
            byte[] y = new byte[imgPlanes[0].getBuffer().capacity()];
            byte[] u = new byte[imgPlanes[1].getBuffer().capacity()];
            byte[] v = new byte[imgPlanes[2].getBuffer().capacity()];
            imgPlanes[0].getBuffer().get(y,0,y.length);
            imgPlanes[1].getBuffer().get(u,0,u.length);
            imgPlanes[2].getBuffer().get(v, 0, v.length);
            newImage.close();

            synchronized (faceLock) {
                listener.onImageReady(y, u, v, detectedFace);
            }
        }
    }


    private class CaptureCallback extends CameraCaptureSession.CaptureCallback{

        public  void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            synchronized (faceLock){
                Face[] face = result.get(TotalCaptureResult.STATISTICS_FACES);
                if(face != null && face.length != 0){
                    detectedFace = face[0];
                }
            }
        }
    }

    public interface OnImageReadyListener{
        public void onImageReady(byte[] y, byte[] u, byte[] v, Face face);
        public void configBuffers(int w, int h);
    }
}
