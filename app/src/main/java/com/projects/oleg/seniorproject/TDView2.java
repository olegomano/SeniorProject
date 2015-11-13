package com.projects.oleg.seniorproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.opengl.GLES20;
import android.util.AttributeSet;

import com.projects.oleg.seniorproject.Camera.CameraImage;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.Rendering.Geometry.Box;
import com.projects.oleg.seniorproject.Rendering.Geometry.Cube;
import com.projects.oleg.seniorproject.Rendering.MGlSurfaceView;
import com.projects.oleg.seniorproject.Rendering.Texture;
import com.projects.oleg.seniorproject.openCV.Face;
import com.projects.oleg.seniorproject.openCV.FaceRecognitionListener;
import com.projects.oleg.seniorproject.openCV.OpenCVThread;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Oleg Tolstov on 9:47 PM, 11/11/15. SeniorProject
 */
public class TDView2 extends MGlSurfaceView implements FaceRecognitionListener{
    private Cube closeCube;
    private Cube farCube;
    private Box box;
    private Texture woodTexture;

    private volatile Object drawLock = new Object();

    public TDView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    public TDView2(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    private FrontCamera fCamera;
    private CameraImage camerFeed;
    private OpenCVThread cvThread;
    private void initCamera(){
        try {
            Utils.print("Initializing camera");
            cvThread = new OpenCVThread(getContext());
            cvThread.setRecognitionListener(this);
            fCamera = new FrontCamera(getContext());
            camerFeed = new CameraImage(cvThread);
            while(!fCamera.start(camerFeed)){};
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(){
        super.onInit();
        initCamera();
        Utils.print("Initializing scene");
        closeCube = new Cube();
        farCube = new Cube();
        box = new Box();
        closeCube.getModelMatrix().setPosition(0, 0, 1);
        farCube.getModelMatrix().setPosition(0, 0, -2);
        camera.createFrustrum(1, 100, -1, 1, -1, 1);

        closeCube.getModelMatrix().setScale(screenWInches*.35f, screenWInches*.35f, 1);
        farCube.getModelMatrix().setScale(screenWInches*.35f, screenWInches*.35f, 1);
        box.getModelMatrix().setScale(1.15f*screenWInches, 1.15f*screenHInches, 12.5f);

        camera.getMatrix().setPosition(0, 0, 0);
        woodTexture = Texture.loadTexture(getContext(),R.drawable.cube_texture);
        Texture boxTexture = Texture.loadTexture(getContext(),R.drawable.cube_wood_texture);
        closeCube.setTexture(woodTexture);
        farCube.setTexture(woodTexture);
        box.setTexture(boxTexture);
        Utils.print("Camera: " + camera);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        synchronized (drawLock) {
            draw3D(farCube);
            draw3D(closeCube);
            draw3D(box);
        }
    }

    private float threashHold = .03f;
    private float lastSigX = 0;
    private float lastSigY = 0;
    private float lastSigZ = 0;
    @Override
    public void onRecognized(Face f, Bitmap frame) {
        if(f==null)return;
        synchronized (drawLock){
            float[] headPosition = f.getPosition();
            if(Math.abs( headPosition[0] - lastSigX) < threashHold && Math.abs( headPosition[1] - lastSigY ) < threashHold && Math.abs( headPosition[2] - lastSigZ )< threashHold){
                return;
            }
            lastSigX = headPosition[0];
            lastSigY = headPosition[1];
            lastSigZ = headPosition[2];
            Utils.print("Face detected: " + headPosition[0] + ", " + headPosition[1] + ", " + headPosition[2]);
            //camera.getMatrix().setPosition(0, 0, headPosition[2]);
            camera.getMatrix().setPosition( headPosition[0]*.6f,headPosition[1]*.6f,-headPosition[2]);
            float dCoeff    =  1.0f / headPosition[2];
            float frustrumL =  (headPosition[0]*.6f - screenWInches/2.0f)*dCoeff;
            float frustrumR =  (headPosition[0]*.6f + screenWInches/2.0f)*dCoeff;
            float frustrumT =  (headPosition[1]*.6f - screenHInches/2.0f)*dCoeff;
            float frustrumB =  (headPosition[1]*.6f + screenHInches/2.0f)*dCoeff;
            camera.createFrustrum(1,100,frustrumL,frustrumR,frustrumB,frustrumT);
        }
    }
}
