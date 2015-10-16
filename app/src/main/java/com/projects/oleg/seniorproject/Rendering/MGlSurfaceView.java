package com.projects.oleg.seniorproject.Rendering;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.projects.oleg.seniorproject.Camera.CameraTexture;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.R;
import com.projects.oleg.seniorproject.Rendering.Geometry.Quad;
import com.projects.oleg.seniorproject.Rendering.Geometry.Renderable;
import com.projects.oleg.seniorproject.Rendering.Shader.Shader2D;
import com.projects.oleg.seniorproject.Utils;

import java.io.PrintStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Oleg Tolstov on 3:18 PM, 10/12/15. SeniorProject
 */
public class MGlSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer{
    private Shader2D shader2D = new Shader2D();

    private float screenRatio = 1;

    private Quad videoFeed = new Quad();
    private Quad faceTracker = new Quad();

    private CameraTexture videoTexture;
    private FrontCamera fCamera;
    private Texture faceBoundTxt;

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
        GLES20.glClearColor(1, 1, 0, 1);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc (GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        shader2D.compile();
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
        screenRatio = (float) width / (float) height;
    }


    private long lastFrame = -1;
    private int frame = -1;
    private long fpsTime = 0;
    @Override
    public void onDrawFrame(GL10 gl) {
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



        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        videoTexture.updateTexture();
        draw2D(videoFeed);
        synchronized (videoTexture) {
            CameraTexture.FaceResult face = videoTexture.getFaceResult();
            if (face != null) {
                faceTracker.getModelMatrix().setPosition(-face.yPositionGL, face.xPositionGL, 0);
                faceTracker.getModelMatrix().setScale(face.scaleXGL, face.scaleYGL, 1);
                draw2D(faceTracker);
            }
        }
    }

    private void draw2D(Renderable renderable){

        Utils.print("Drawing " + renderable.getTexture().getTexture() + " " + renderable.getTexture().getType());

        GLES20.glUseProgram(shader2D.getProgramHandle());

        GLES20.glEnableVertexAttribArray(shader2D.getVertexHandle());
        GLES20.glEnableVertexAttribArray(shader2D.getUvHandle());

        GLES20.glVertexAttribPointer(shader2D.getVertexHandle(), 4, GLES20.GL_FLOAT, false, 0, renderable.getVertexBuffer());
        Utils.checkGlError("glVertexAttrib vertex");
        GLES20.glVertexAttribPointer(shader2D.getUvHandle(), 2, GLES20.GL_FLOAT, false, 0, renderable.getUVBuffer());
        Utils.checkGlError("glVertexAttrib uv");

        if(renderable.getTexture().getType() == GLES20.GL_TEXTURE_2D){
            Utils.print("Binding regular texture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(renderable.getTexture().getType(), renderable.getTexture().getTexture());
            Utils.checkGlError("Bind texture");
            GLES20.glUniform1i(shader2D.getRegSamperHandle(),0);
            GLES20.glUniform1i(shader2D.getTextureTypeHandle(),1);
        }else{
            Utils.print("Binding oes texture");
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
    }
}
