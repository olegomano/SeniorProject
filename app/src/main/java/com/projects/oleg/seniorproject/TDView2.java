package com.projects.oleg.seniorproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.opengl.GLES20;
import android.util.AttributeSet;

import com.projects.oleg.seniorproject.Camera.CameraImage;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.DebugView.DebugView;
import com.projects.oleg.seniorproject.Rendering.Geometry.Box;
import com.projects.oleg.seniorproject.Rendering.Geometry.Cube;
import com.projects.oleg.seniorproject.Rendering.Geometry.RenderableObj;
import com.projects.oleg.seniorproject.Rendering.MGlSurfaceView;
import com.projects.oleg.seniorproject.Rendering.ObjParser.ObjLoader;
import com.projects.oleg.seniorproject.Rendering.Texture.Texture;
import com.projects.oleg.seniorproject.Rendering.Texture.TextureLoader;
import com.projects.oleg.seniorproject.openCV.Face;
import com.projects.oleg.seniorproject.openCV.FaceRecognitionListener;
import com.projects.oleg.seniorproject.openCV.OpenCVThread;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Oleg Tolstov on 9:47 PM, 11/11/15. SeniorProject
 */
public class TDView2 extends MGlSurfaceView implements FaceRecognitionListener{
    private Cube[] cubes = new Cube[12];
    private Box box;
    private Texture woodTexture;
    private RenderableObj mModel;

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
        box = new Box();
        camera.createFrustrum(1, 100, -1, 1, -1, 1);
        box.getModelMatrix().setScale(screenWInches,screenHInches, 8);
        positionCubes();
        camera.getMatrix().setPosition(0, 0, 0);
        Texture boxTexture = TextureLoader.loadTexture(R.drawable.cube_wood_texture);
        box.setTexture(boxTexture);
        Utils.print("Camera: " + camera);
        try {
            mModel = new RenderableObj( ObjLoader.loadObj(getContext(),"cube.obj") );
            mModel.getModelMatrix().setPosition(0,0,-3);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void positionCubes(){
        woodTexture = TextureLoader.loadTexture(R.drawable.cube_texture);
        float closest = 2;
        float farthest = -10;
        float leftMost = -screenWInches/2.0f;
        float rightMost = screenWInches/2.0f;
        float maxYOffset = screenHInches/2.0f;

        for(int i = 0; i < cubes.length; i++){
            cubes[i] = new Cube();
            cubes[i].setTexture(woodTexture);

            cubes[i].getModelMatrix().setPosition( leftMost + (((rightMost - leftMost)/cubes.length)*i), Utils.genRand(-maxYOffset,maxYOffset),Utils.genRand(farthest,closest));

            cubes[i].getModelMatrix().setScale( screenHInches*.45f,screenHInches*.45f,1.0f);
        }

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        synchronized (drawLock) {
            for(int i = 0; i < cubes.length; i++){
            //    draw3D(cubes[i]);
            }
            draw3D(box);
            drawRenderableObj(mModel);

        }
    }

    private float threashHold = .1f;
    private float lastSigX = 0;
    private float lastSigY = 0;
    private float lastSigZ = 0;

    private int newDetections = 0;
    private long lastPost = System.nanoTime();
    @Override
    public void onRecognized(Face f, Bitmap frame) {
        newDetections++;
        long frameTime = System.nanoTime();
        if(frameTime - lastPost > Utils.SECOND_TO_NANO){
            DebugView.putRecogFPS(""+newDetections);
            newDetections=0;
            lastPost = frameTime;
        }

        if(f==null){
            DebugView.putRecogStatus("FALSE (NEW VER)");
            return;
        }
        DebugView.putRecogStatus("TRUE (NEW VER)");
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
            camera.getMatrix().setPosition( headPosition[0]*1f,headPosition[1]*1f,-headPosition[2]);
            float dCoeff    =  1.0f / headPosition[2];
            dCoeff = 1f;
            float frustrumL =  (headPosition[0]*1f - screenWInches/2.0f)*dCoeff;
            float frustrumR =  (headPosition[0]*1f + screenWInches/2.0f)*dCoeff;
            float frustrumT =  (headPosition[1]*1f - screenHInches/2.0f)*dCoeff;
            float frustrumB =  (headPosition[1]*1f + screenHInches/2.0f)*dCoeff;
            camera.createFrustrum(headPosition[2]*dCoeff,100,frustrumL,frustrumR,frustrumB,frustrumT);
        }
    }
}
