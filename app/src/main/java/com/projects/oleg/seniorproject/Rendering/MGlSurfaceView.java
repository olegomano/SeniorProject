package com.projects.oleg.seniorproject.Rendering;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.projects.oleg.seniorproject.Camera.CameraTexture;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.Rendering.Geometry.Quad;
import com.projects.oleg.seniorproject.Rendering.Geometry.Renderable;
import com.projects.oleg.seniorproject.Rendering.Shader.Shader2DOES;
import com.projects.oleg.seniorproject.Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11Ext;

/**
 * Created by Oleg Tolstov on 3:18 PM, 10/12/15. SeniorProject
 */
public class MGlSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer{
    private Shader2DOES oesShader = new Shader2DOES();

    private float screenRatio = 1;

    private Quad videoFeed = new Quad();
    private Quad faceTracker = new Quad();

    private CameraTexture videoTexture;
    private FrontCamera fCamera;

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
        oesShader.compile();
        videoTexture = new CameraTexture();
        try {
            while(!fCamera.start(videoTexture)){

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
        videoFeed.setTexture(videoTexture.getTexture());
//        videoFeed.getModelMatrix().rotate(90,0,1,0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        screenRatio = (float) width / (float) height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        videoTexture.updateTexture();
        videoFeed.getModelMatrix().rotate(1, 0, 0, 1);
        if(videoTexture.getFaceResult() != null){

        }

        draw2DOES(videoFeed);
    }

    private void draw2DOES(Renderable renderable){
        GLES20.glUseProgram(oesShader.getProgramHandle());

        GLES20.glEnableVertexAttribArray(oesShader.getVertexHandle());
        GLES20.glEnableVertexAttribArray(oesShader.getUvHandle());

        GLES20.glVertexAttribPointer(oesShader.getVertexHandle(), 4, GLES20.GL_FLOAT, false, 0, renderable.getVertexBuffer());
        Utils.checkGlError("glVertexAttrib vertex");
        GLES20.glVertexAttribPointer(oesShader.getUvHandle(), 2, GLES20.GL_FLOAT, false, 0, renderable.getUVBuffer());
        Utils.checkGlError("glVertexAttrib uv");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, renderable.getTexture().getTexture());
        Utils.checkGlError("Bind texture");
        GLES20.glUniform1i(oesShader.getGetSamplerHandle(), 0);
        Utils.checkGlError("glUniform sampler");

        GLES20.glUniformMatrix4fv(oesShader.getModelMatrixHandle(),1,false,renderable.getModelMatrix().getMatrix(),0);

        GLES20.glUniform1f(oesShader.getScreenRatioHandle(), screenRatio);
        Utils.checkGlError("glUniform1f screenRatio");

        GLES20.glUniform3fv(oesShader.getScaleHandle(), 1, renderable.getModelMatrix().getScale(), 0);
        Utils.checkGlError("glUniform3fv scale");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, renderable.getVertexCount());
        Utils.checkGlError("Draw");
    }

    private void draw2D(Renderable renderable){
        GLES20.glUseProgram(oesShader.getProgramHandle());

        GLES20.glEnableVertexAttribArray(oesShader.getVertexHandle());
        GLES20.glEnableVertexAttribArray(oesShader.getUvHandle());

        GLES20.glVertexAttribPointer(oesShader.getVertexHandle(), 4, GLES20.GL_FLOAT, false, 0, renderable.getVertexBuffer());
        Utils.checkGlError("glVertexAttrib vertex");
        GLES20.glVertexAttribPointer(oesShader.getUvHandle(), 2, GLES20.GL_FLOAT, false, 0, renderable.getUVBuffer());
        Utils.checkGlError("glVertexAttrib uv");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, renderable.getTexture().getTexture());
        Utils.checkGlError("Bind texture");
        GLES20.glUniform1i(oesShader.getGetSamplerHandle(), 0);
        Utils.checkGlError("glUniform sampler");

        GLES20.glUniformMatrix4fv(oesShader.getModelMatrixHandle(),1,false,renderable.getModelMatrix().getMatrix(),0);

        GLES20.glUniform1f(oesShader.getScreenRatioHandle(), screenRatio);
        Utils.checkGlError("glUniform1f screenRatio");

        GLES20.glUniform3fv(oesShader.getScaleHandle(), 1, renderable.getModelMatrix().getScale(), 0);
        Utils.checkGlError("glUniform3fv scale");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, renderable.getVertexCount());
        Utils.checkGlError("Draw");
    }

}
