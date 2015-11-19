package com.projects.oleg.seniorproject.Camera;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Surface;

import com.projects.oleg.seniorproject.Rendering.Texture.Texture;
import com.projects.oleg.seniorproject.Utils;

import java.util.ArrayList;

/**
 * Created by Oleg Tolstov on 9:35 PM, 10/10/15. SeniorProject
 * Manages the camera feed
 */

public class CameraTexture extends CameraListener{
    private final float FACE_HEIGHT_MM = 145;
    private final float FACE_WIDTH_MM = 145;

    private Texture texture;
    private ArrayList<SurfaceTexture> sTextureList = new ArrayList<>(1);
    private ArrayList<Surface> surfaceList = new ArrayList<>(1);
    private CaptureRequest mRequest;

    private volatile FaceResult mResult = new FaceResult();
    private volatile int retRes = 1;
    private volatile boolean haveResult =false;

    private volatile HandlerThread callbackThread = new HandlerThread("ImageCallbackThread");

    public CameraTexture(){
        texture = new Texture(createTexture(),GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        SurfaceTexture surfaceTexture = new SurfaceTexture(texture.getTexture());
        Surface surface = new Surface(surfaceTexture);
        sTextureList.add(surfaceTexture);
        surfaceList.add(surface);
        callbackThread.start();
    }

    public ArrayList<Surface> getSurfaceList(){
        return surfaceList;
    }

    public void configureImageSize(int width, int height){ //sets the dimentions of the surface
        for(int i = 0; i < sTextureList.size(); i++){
            sTextureList.get(i).setDefaultBufferSize(width,height);
        }
    }

    public void setCaptureRequest(CaptureRequest c){
        mRequest =c;
    }

    @Override
    public Class getType() {
        return SurfaceTexture.class;
    }

    public Texture getTexture(){
        return texture;
    }

    private int createTexture(){
        int[] textureID = new int[1];
        GLES20.glGenTextures(1, textureID, 0);
        if(textureID[0]<0){
            Utils.print("Failed creating texture");
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        return textureID[0];
    }

    public void updateTexture(){
        for(int i = 0; i < sTextureList.size(); i++){
            sTextureList.get(i).updateTexImage();
        }
    }

    public FaceResult getFaceResult() {
        if (haveResult) {
            return mResult;
        } else {
            return null;
        }
    }

    @Override
    public void onConfigured(CameraCaptureSession session) {
        try {
            Utils.print("Session created, sending request");
            session.setRepeatingRequest(mRequest, new CaptureCallback(), new Handler(callbackThread.getLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {

    }

    private class CaptureCallback extends CameraCaptureSession.CaptureCallback{
        private long frameTime = System.nanoTime();
        public  void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            long fTime = System.nanoTime();
            Utils.print("time between camera frames: " + (fTime - frameTime)*Utils.NANO_TO_SECOND);
            frameTime = fTime;
            if (result.get(TotalCaptureResult.STATISTICS_FACES).length != 0) {
                float focus = request.get(CaptureRequest.LENS_FOCAL_LENGTH);
                Face mFace = result.get(TotalCaptureResult.STATISTICS_FACES)[0];

                float faceOnSensorWmm = mFace.getBounds().width() * FrontCamera.pixelToMM;
                float faceOnSensorHmm = mFace.getBounds().height() * FrontCamera.pixelToMM;

                float sensorWmm = FrontCamera.sensorSizeMM.getWidth();
                float sensorHmm = FrontCamera.sensorSizeMM.getHeight();

                float w1 = FrontCamera.sensorSizeMM.getWidth() - mFace.getBounds().right * FrontCamera.pixelToMM;
                float w2 = (((w1 + faceOnSensorWmm) * FACE_HEIGHT_MM) / faceOnSensorWmm) - FACE_HEIGHT_MM;
                final float distance = ((focus * w2) / w1) - focus;

                float h1 = FrontCamera.sensorSizeMM.getHeight() - mFace.getBounds().bottom * FrontCamera.pixelToMM;
                float h2 = (((h1 + faceOnSensorHmm) * FACE_WIDTH_MM) / faceOnSensorHmm) - FACE_WIDTH_MM;
                final float distnaceh = ((focus * h2) / h1) - focus;


                //Utils.print("H1,H2,distance: " + w1 + ", " + w2 + ", " + distance );

                float xOffset = ((mFace.getBounds().centerX() * FrontCamera.pixelToMM - (sensorWmm / 2.0f)) * (focus + distance)) / focus;
                float yOffset = ((mFace.getBounds().centerY() * FrontCamera.pixelToMM - (sensorHmm / 2.0f)) * (focus + distance)) / focus;

                float xPositionGL = Utils.rerange(0, FrontCamera.sensorActivePixels.width(), -1, 1, mFace.getBounds().centerX());
                float yPositionGL = Utils.rerange(0, FrontCamera.sensorActivePixels.height(), -1, 1, mFace.getBounds().centerY());
                synchronized (this) {
                    mResult.distanceInMM = distance;
                    mResult.xOffsetInMM = xOffset;
                    mResult.yOffsetInMM = yOffset;
                    mResult.xPositionGL = xPositionGL;
                    mResult.yPositionGL = yPositionGL;
                    mResult.scaleXGL = (float) mFace.getBounds().width() / (float) FrontCamera.sensorActivePixels.width();
                    mResult.scaleYGL = (float) mFace.getBounds().height() / (float) FrontCamera.sensorActivePixels.height();
                    haveResult = true;
                    retRes = (retRes + 1) % 2;
                    //Utils.print(mResult.toString());
                    //Utils.print("On Thread " + Thread.currentThread().getName());
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                     //   MainActivity.output.setText("Distance: " + distance * Utils.MM_TO_INCH + ", " + distnaceh * Utils.MM_TO_INCH);
                    }
                });

            } else {
                synchronized (this) {
                    haveResult = false;
                }
            }

        }

        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {

        }

    }

    public class FaceResult{
        public float distanceInMM;
        public float xOffsetInMM;
        public float yOffsetInMM;

        public float xPositionGL;
        public float yPositionGL;

        public float scaleXGL;
        public float scaleYGL;

        public String toString(){
            return "Face Pose(x,y,z): " + xOffsetInMM + " , " + yOffsetInMM + " ," + distanceInMM + " \n GL: (x,y) " + xPositionGL + " " + yPositionGL + " scale: " + scaleXGL + " , " + scaleYGL ;
        }
    }
}