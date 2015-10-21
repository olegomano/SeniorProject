package com.projects.oleg.seniorproject;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.opengl.GLES20;
import android.util.AttributeSet;

import com.projects.oleg.seniorproject.Camera.CameraTexture;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.Rendering.Camera;
import com.projects.oleg.seniorproject.Rendering.Geometry.Box;
import com.projects.oleg.seniorproject.Rendering.Geometry.Cube;
import com.projects.oleg.seniorproject.Rendering.Geometry.Quad;
import com.projects.oleg.seniorproject.Rendering.MGlSurfaceView;
import com.projects.oleg.seniorproject.Rendering.Texture;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Oleg Tolstov on 2:29 PM, 10/21/15. SeniorProject
 */
public class TDView extends MGlSurfaceView {
    private Quad videoFeed = new Quad();
    private Quad faceTracker = new Quad();
    private Cube cube = new Cube();
    private Cube cube2 = new Cube();
    private Box box = new Box();

    private CameraTexture videoTexture;
    private FrontCamera fCamera;
    private Texture faceBoundTxt;
    private Texture cubeTxt;
    private Texture woodTxt;

    public TDView(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            fCamera = new FrontCamera(context);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        setEGLContextClientVersion(2);
        setRenderer(this);

    }

    public TDView(Context context) {
        super(context);
        try {
            fCamera = new FrontCamera(context);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl,config);
        cubeTxt = Texture.loadTexture(getContext(),R.drawable.cube_texture);
        faceBoundTxt = Texture.loadTexture(getContext(), R.drawable.face_boundbox);
        woodTxt = Texture.loadTexture(getContext(),R.drawable.box_dark_texture);
        camera.getMatrix().setPosition(0, 0, -5f);
        camera.createFrustrum(1, 100, -1, 1, -1, 1);
        cube.setTexture(cubeTxt);
        cube2.setTexture(cubeTxt);
        box.setTexture(woodTxt);
        videoTexture = new CameraTexture();
        Utils.print("Created face_boundbox " + faceBoundTxt.getTexture() + " " + faceBoundTxt.getType());
        try {
            while(!fCamera.start(videoTexture)){
                Utils.print("Requested Capture Session");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
        videoFeed.setTexture(videoTexture.getTexture());
        videoFeed.getModelMatrix().rotate(90, 0, 0, 1);
        faceTracker.setTexture(faceBoundTxt);
        positionBoxes();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl,width,height);
        box.getModelMatrix().mulScale(screenWInches,screenHInches,1);
    }

    private void positionBoxes(){
        box.getModelMatrix().setScale(1.5f,1.1f,12.5f);
        cube.getModelMatrix().setPosition(0, 0, 1);
        cube2.getModelMatrix().setPosition(.5f,.5f,-2);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        videoTexture.updateTexture();
        synchronized (videoTexture) {
            CameraTexture.FaceResult face = videoTexture.getFaceResult();
            if (face != null) {
                faceTracker.getModelMatrix().setPosition(face.xPositionGL,face.yPositionGL,0);
                float camDistance = face.distanceInMM*Utils.MM_TO_INCH;
                float camX = face.xOffsetInMM*Utils.MM_TO_INCH*.65f;
                float camY = face.yOffsetInMM*Utils.MM_TO_INCH*.65f;
                camera.getMatrix().setPosition(-camX, -camY, -camDistance);
                float screenX = -camX;
                float screenY = -camY;

                float screenL = screenX - screenWInches/2;
                float screenR = screenX + screenWInches/2;

                float screenT = screenY + screenHInches/2;
                float screenB = screenY - screenHInches/2;
                camera.createFrustrum(camDistance * .5f, camDistance + 25, screenR * .5f, screenL * .5f, screenT * .5f, screenB * .5f);
            }
        }

        draw3D(cube);
        draw3D(box);
        draw3D(cube2);
    }
}
