package com.projects.oleg.seniorproject.Camera;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import com.projects.oleg.seniorproject.Utils;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Oleg Tolstov on 9:35 PM, 10/10/15. SeniorProject
 * Manages the camera feed
 */

public class CameraTexture extends CameraCaptureSession.StateCallback{
    private final float FACE_WIDTH_MM = 122;
    private final float FACE_HEIGHT_MM = 122;

    private int textureID;
    private ArrayList<SurfaceTexture> sTextureList = new ArrayList<>(1);
    private ArrayList<Surface> surfaceList = new ArrayList<>(1);
    private CaptureRequest mRequest;

    private volatile FaceResult[] mResult = { new FaceResult(), new FaceResult()};
    private volatile int retRes = 1;
    private volatile boolean haveResult =false;

    public CameraTexture(){
        textureID = createTexture();
        SurfaceTexture surfaceTexture = new SurfaceTexture(textureID);
        Surface surface = new Surface(surfaceTexture);
        sTextureList.add(surfaceTexture);
        surfaceList.add(surface);
    }

    public ArrayList<Surface> getSurfaceList(){
        return surfaceList;
    }

    public void configureSurface(int width, int height){ //sets the dimentions of the surface
        for(int i = 0; i < sTextureList.size(); i++){
            sTextureList.get(i).setDefaultBufferSize(width,height);
        }
    }

    public void setCaptureRequest(CaptureRequest c){
        mRequest =c;
    }

    public int getTexture(){
        return textureID;
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

    public FaceResult getFaceResult(){
        synchronized (this){
            if(haveResult) {
                return mResult[1- retRes];
            }else{
                return null;
            }
        }

    }

    @Override
    public void onConfigured(CameraCaptureSession session) {
        try {
            session.setRepeatingRequest(mRequest, new CaptureCallback(), new Handler(Looper.getMainLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {

    }

    private class CaptureCallback extends CameraCaptureSession.CaptureCallback{
        public synchronized void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result){
           super.onCaptureCompleted(session, request, result);
           if(result.get(TotalCaptureResult.STATISTICS_FACES).length != 0){
               float focus = request.get(CaptureRequest.LENS_FOCAL_LENGTH);
               Face mFace = result.get(TotalCaptureResult.STATISTICS_FACES)[0];

               float faceOnSensorWmm = mFace.getBounds().width()*FrontCamera.pixelToMM;
               float faceOnSensorHmm = mFace.getBounds().height()*FrontCamera.pixelToMM;

               float sensorWmm = FrontCamera.sensorSizeMM.getWidth();
               float sensorHmm = FrontCamera.sensorSizeMM.getHeight();

               float h1 = FrontCamera.sensorSizeMM.getWidth() - faceOnSensorWmm;
               float h2 = (((h1 + faceOnSensorWmm ) * FACE_WIDTH_MM) / faceOnSensorWmm) - FACE_WIDTH_MM;
               float distance = ( (focus*(h2 + FACE_WIDTH_MM)) /  (h1 + faceOnSensorWmm) ) - focus;
               float distnace2 = ( (focus*h2) / h1 ) -focus;


               Utils.print("H1,H2,distance: " + h1 + ", " + h2 + ", " + distance + ", " + distnace2);

               float xOffset  = ((mFace.getBounds().centerX()*FrontCamera.pixelToMM - (sensorWmm/2.0f))*(focus + distance) )/ focus;
               float yOffset  = ((mFace.getBounds().centerY()*FrontCamera.pixelToMM - (sensorHmm/2.0f))*(focus + distance) )/ focus;

               float xPositionGL = Utils.rerange(0,FrontCamera.sensorActivePixels.width(),-1,1,mFace.getBounds().centerX());
               float yPositionGL = Utils.rerange(0,FrontCamera.sensorActivePixels.height(),-1,1,mFace.getBounds().centerY());

               FaceResult mResult = CameraTexture.this.mResult[retRes];
               mResult = new FaceResult();
               mResult.distanceInMM = distance;
               mResult.xOffsetInMM = xOffset;
               mResult.yOffsetInMM = yOffset;
               mResult.xPositionGL = xPositionGL;
               mResult.yPositionGL = yPositionGL;
               mResult.scaleXGL = (float) mFace.getBounds().width() / (float) FrontCamera.sensorActivePixels.width();
               mResult.scaleYGL = (float) mFace.getBounds().height() / (float) FrontCamera.sensorActivePixels.height();
               synchronized (this){
                   haveResult = true;
                   retRes = (retRes + 1) % 2;
                   Utils.print(mResult.toString());
                   Utils.print("On Thread " + Thread.currentThread().getName());
               }
           };
        }

        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            synchronized (this){
                haveResult = false;
            }
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