package com.projects.oleg.seniorproject.Rendering;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.opengl.*;
import android.test.UiThreadTest;
import android.util.AttributeSet;

import com.projects.oleg.seniorproject.Camera.CameraTexture;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.MainActivity;
import com.projects.oleg.seniorproject.R;
import com.projects.oleg.seniorproject.Rendering.Geometry.Cube;
import com.projects.oleg.seniorproject.Rendering.Geometry.Quad;
import com.projects.oleg.seniorproject.Rendering.Geometry.Renderable;
import com.projects.oleg.seniorproject.Rendering.Shader.Shader2D;
import com.projects.oleg.seniorproject.Rendering.Shader.Shader3D;
import com.projects.oleg.seniorproject.Utils;

import java.io.PrintStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Oleg Tolstov on 3:18 PM, 10/12/15. SeniorProject
 */
public class MGlSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer{
    private float screenWInches;
    private float screenHInches;

    private Shader2D shader2D = new Shader2D();
    private Shader3D shader3D = new Shader3D();

    private float screenRatio = 1;

    private Camera camera = new Camera();
    private Quad videoFeed = new Quad();
    private Quad faceTracker = new Quad();
    private Cube cube = new Cube();
    private Cube cube2 = new Cube();

    private CameraTexture videoTexture;
    private FrontCamera fCamera;
    private Texture faceBoundTxt;



    public MGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            fCamera = new FrontCamera(context);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    public MGlSurfaceView(Context context) {
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
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        shader2D.compile();
        shader3D.compile();
        camera.getMatrix().setPosition(0, 0, -5);
        camera.createFrustrum(1, 100, -1, 1, -1, 1);
        cube2.getModelMatrix().setPosition(0, 0, -15);
        cube.getModelMatrix().setPosition(0, 0, -1);
        cube.getModelMatrix().mulScale(1,1,1);
        cube2.getModelMatrix().mulScale(4,4,4);


        videoTexture = new CameraTexture();
        faceBoundTxt = Texture.loadTexture(getContext(), R.drawable.face_boundbox);
        Utils.print("Created face_boundbox " + faceBoundTxt.getTexture() + " " + faceBoundTxt.getType());
        try {
            while(!fCamera.start(videoTexture)){
                Utils.print("Requested Capture Session");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
        videoFeed.setTexture(videoTexture.getTexture());
        videoFeed.getModelMatrix().rotate(90,0,0,1);
        faceTracker.setTexture(faceBoundTxt);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        screenRatio = 1;
        screenHInches = (float)height/ MainActivity.PPI_Y;
        screenWInches = (float)width/ MainActivity.PPI_X;
        Utils.print("Screen size is(in) " + screenWInches + ", " + screenHInches);
    }


    private long lastFrame = -1;
    private int frame = -1;
    private long fpsTime = 0;
    private void getFps(){
        frame++;
        long frameTime = System.nanoTime();
        long dt = frameTime - lastFrame;
        fpsTime+=dt;
        lastFrame = frameTime;
        if(fpsTime > 1000000000){
            Utils.print("FPS: " + frame);
            frame = 0;
            fpsTime = 0;
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        getFps();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        videoTexture.updateTexture();
       // draw2D(videoFeed);
        synchronized (videoTexture) {
            CameraTexture.FaceResult face = videoTexture.getFaceResult();
            if (face != null) {
                faceTracker.getModelMatrix().setPosition(face.xPositionGL,face.yPositionGL,0);
                float camDistance = face.distanceInMM*Utils.MM_TO_INCH;
                float camX = face.xOffsetInMM*Utils.MM_TO_INCH;
                float camY = face.yOffsetInMM*Utils.MM_TO_INCH;
                camera.getMatrix().setPosition(-camY, camX, -camDistance);
                Utils.print("Camera pos: " + camX + ", " + camY + " " + camDistance );
                float screenX = -camY;
                float screenY = camX;

                float screenL = screenX - screenWInches;
                float screenR = screenX + screenWInches;

                float screenT = screenY + screenHInches;
                float screenB = screenY - screenHInches;

                camera.createFrustrum(camDistance,camDistance + 25,screenL,screenR,screenB,screenT);

                draw3D(cube);
                draw3D(cube2);
                //draw2D(faceTracker);
            }
        }
        //cube2.getModelMatrix().rotate(1,.3f,.3f,0);
   }

    private void draw2D(Renderable renderable){
        GLES20.glUseProgram(shader2D.getProgramHandle());
        GLES20.glEnableVertexAttribArray(shader2D.getVertexHandle());
        GLES20.glEnableVertexAttribArray(shader2D.getUvHandle());
        GLES20.glVertexAttribPointer(shader2D.getVertexHandle(), 4, GLES20.GL_FLOAT, false, 0, renderable.getVertexBuffer());
        Utils.checkGlError("glVertexAttrib vertex");
        GLES20.glVertexAttribPointer(shader2D.getUvHandle(), 2, GLES20.GL_FLOAT, false, 0, renderable.getUVBuffer());
        Utils.checkGlError("glVertexAttrib uv");

        if(renderable.getTexture().getType() == GLES20.GL_TEXTURE_2D){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(renderable.getTexture().getType(), renderable.getTexture().getTexture());
            Utils.checkGlError("Bind texture");
            GLES20.glUniform1i(shader2D.getRegSamperHandle(),0);
            GLES20.glUniform1i(shader2D.getTextureTypeHandle(),1);
        }else{
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(renderable.getTexture().getType(), renderable.getTexture().getTexture());
            Utils.checkGlError("Bind texture, oes");
            GLES20.glUniform1i(shader2D.getExtSamplerHandle(),1);
            GLES20.glUniform1i(shader2D.getTextureTypeHandle(),0);
        }
        Utils.checkGlError("passed texture");

        GLES20.glUniformMatrix4fv(shader2D.getModelMatrixHandle(), 1, false, renderable.getModelMatrix().getMatrix(), 0);
        GLES20.glUniform1f(shader2D.getScreenRatioHandle(), screenRatio);
        Utils.checkGlError("glUniform1f screenRatio");

        GLES20.glUniform3fv(shader2D.getScaleHandle(), 1, renderable.getModelMatrix().getScale(), 0);
        Utils.checkGlError("glUniform3fv scale");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, renderable.getVertexCount());
        Utils.checkGlError("Draw");

        GLES20.glDisableVertexAttribArray(shader2D.getUvHandle());
        GLES20.glDisableVertexAttribArray(shader2D.getVertexHandle());
    }

    private float[] mvpMat = new float[16];
    public void draw3D(Renderable renderable){
        GLES20.glUseProgram(shader3D.getProgramHandle());

        GLES20.glEnableVertexAttribArray(shader3D.getVertexHandle());
        GLES20.glEnableVertexAttribArray(shader3D.getUvHandle());
        Utils.checkGlError("Enabled arrays");
        GLES20.glVertexAttribPointer(shader3D.getVertexHandle(), 4, GLES20.GL_FLOAT, false, 0, renderable.getVertexBuffer());
        Utils.checkGlError("Passed vertex");
        GLES20.glVertexAttribPointer(shader3D.getUvHandle(), 2, GLES20.GL_FLOAT, false, 0, renderable.getUVBuffer());
        Utils.checkGlError("passed attribute arrays");

        GLES20.glUniform1f(shader3D.getScreenRatioHandle(), screenRatio);
        Utils.checkGlError("glUniform1f screenRatio");


        android.opengl.Matrix.multiplyMM(mvpMat,0,camera.getProjectionCamera(),0,renderable.getModelMatrix().getMatrix(),0);
        GLES20.glUniformMatrix4fv(shader3D.getMvpMatHandle(), 1, false, mvpMat, 0);
        Utils.checkGlError("Passed uniform matrix");
        GLES20.glUniform3fv(shader3D.getScaleHandle(), 1, renderable.getModelMatrix().getScale(), 0);
        Utils.checkGlError("passed scale");

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, renderable.getVertexCount(), GLES20.GL_UNSIGNED_SHORT, renderable.getIndexBuffer());

        Utils.checkGlError("Draw 3d");
        GLES20.glDisableVertexAttribArray(shader3D.getVertexHandle());
        GLES20.glDisableVertexAttribArray(shader3D.getUvHandle());

    }
}
