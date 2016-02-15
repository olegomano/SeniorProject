package com.projects.oleg.seniorproject.Rendering;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.opengl.*;
import android.util.AttributeSet;

import com.projects.oleg.seniorproject.Camera.CameraTexture;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.DebugView.DebugView;
import com.projects.oleg.seniorproject.MainActivity;
import com.projects.oleg.seniorproject.R;
import com.projects.oleg.seniorproject.Rendering.Geometry.Box;
import com.projects.oleg.seniorproject.Rendering.Geometry.Cube;
import com.projects.oleg.seniorproject.Rendering.Geometry.Mesh;
import com.projects.oleg.seniorproject.Rendering.Geometry.Quad;
import com.projects.oleg.seniorproject.Rendering.Geometry.Renderable;
import com.projects.oleg.seniorproject.Rendering.Geometry.RenderableObj;
import com.projects.oleg.seniorproject.Rendering.Shader.Shader2D;
import com.projects.oleg.seniorproject.Rendering.Shader.Shader3D;
import com.projects.oleg.seniorproject.Rendering.Shader.Shader3DObj;
import com.projects.oleg.seniorproject.Rendering.Texture.TextureLoader;
import com.projects.oleg.seniorproject.Utils;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.opencv.android.*;
/**
 * Created by Oleg Tolstov on 3:18 PM, 10/12/15. SeniorProject
 */
public class MGlSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer{
    protected Camera camera = new Camera();
    protected float screenWInches;
    protected float screenHInches;
    protected float screenRatio;

    private Shader2D shader2D = new Shader2D();
    private Shader3D shader3D = new Shader3D();
    private Shader3DObj objShader = new Shader3DObj();


    public MGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MGlSurfaceView(Context context) {
        super(context);
    }

    public void onInit(){ TextureLoader.initTextureLoader(getContext());};

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        shader2D.compile();
        shader3D.compile();
        objShader.compile();
        camera.createFrustrum(1, 100, -1, 1, -1, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        screenRatio = 1;
        screenHInches = (float)height/ MainActivity.PPI_Y;
        screenWInches = (float)width/ MainActivity.PPI_X;
        //screenRatio = screenWInches/screenHInches;
        Utils.print("Screen size is(in) " + screenWInches + ", " + screenHInches);
        onInit();
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
            DebugView.putRenderFPS("" + frame);
            frame = 0;
            fpsTime = 0;
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        getFps();
   }

    protected void draw2D(Renderable renderable){
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
    protected void draw3D(Renderable renderable){
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

        android.opengl.Matrix.multiplyMM(mvpMat, 0, camera.getProjectionCamera(), 0, renderable.getModelMatrix().getMatrix(), 0);
        GLES20.glUniformMatrix4fv(shader3D.getMvpMatHandle(), 1, false, mvpMat, 0);
        Utils.checkGlError("Passed uniform matrix");
        GLES20.glUniform3fv(shader3D.getScaleHandle(), 1, renderable.getModelMatrix().getScale(), 0);
        Utils.checkGlError("passed scale");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderable.getTexture().getTexture());
        GLES20.glUniform1i(shader3D.getSamplerHandle(), 0);
        Utils.checkGlError("Passed texture");

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, renderable.getVertexCount(), GLES20.GL_UNSIGNED_SHORT, renderable.getIndexBuffer());

        Utils.checkGlError("Draw 3d");
        GLES20.glDisableVertexAttribArray(shader3D.getVertexHandle());
        GLES20.glDisableVertexAttribArray(shader3D.getUvHandle());

    }

    public void draw3DVBO(Renderable toRender, Matrix light){
        GLES20.glUseProgram(objShader.getProgramHandle());
        Utils.checkGlError("Use Program");

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, toRender.getVBO());
        GLES20.glEnableVertexAttribArray(objShader.getVertexHandle());
        GLES20.glEnableVertexAttribArray(objShader.getUvHandle());
        GLES20.glEnableVertexAttribArray(objShader.getVertNormalHandle());
        Utils.checkGlError("Enabled Attrib Arrays");

        GLES20.glUniform1f(objShader.getScreenRatioHandle(), screenRatio);
        Utils.checkGlError("glUniform1f screenRatio");


        GLES20.glVertexAttribPointer(objShader.getVertexHandle(), 4, GLES20.GL_FLOAT, false, toRender.getStride() * 4, toRender.getVertOffset() * 4);
        Utils.checkGlError("Passed vertex");
        GLES20.glVertexAttribPointer(objShader.getVertNormalHandle(), 4, GLES20.GL_FLOAT, false, toRender.getStride() * 4, toRender.getNormOffset() * 4);
        Utils.checkGlError("Passed Normal");
        GLES20.glVertexAttribPointer(objShader.getUvHandle(), 2, GLES20.GL_FLOAT, false, toRender.getStride() * 4, toRender.getUVOffset() * 4);
        Utils.checkGlError("Passed UV");

        GLES20.glUniform3fv(objShader.getDirLightHandle(), 1, new float[]{0, 0, 1}, 0);
        Utils.checkGlError("Passed Dir Light");

        Matrix unitMatrix = new Matrix();
        GLES20.glUniformMatrix4fv(objShader.getModelMatHandle(), 1, false, toRender.getModelMatrix().getMatrix(), 0);
        Utils.checkGlError("Passed model mat");

        android.opengl.Matrix.multiplyMM(mvpMat, 0, camera.getProjectionCamera(), 0, toRender.getModelMatrix().getMatrix(), 0);
        GLES20.glUniformMatrix4fv(objShader.getMvpMatHandle(), 1, false, mvpMat, 0);
        Utils.checkGlError("Passed mvp matrix");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, toRender.getTexture().getTexture());
        GLES20.glUniform1i(objShader.getSamplerHandle(), 0);
        Utils.checkGlError("Passed Texture");

        GLES20.glUniform3fv(objShader.getScaleHandle(), 1, toRender.getModelMatrix().getScale(), 0);
        Utils.checkGlError("passed scale");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, toRender.getVertexCount());

        GLES20.glDisableVertexAttribArray(objShader.getUvHandle());
        GLES20.glDisableVertexAttribArray(objShader.getVertexHandle());
        GLES20.glDisableVertexAttribArray(objShader.getVertNormalHandle());
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        Utils.checkGlError("Draw VBO 3D");

    }

    public void drawRenderableObj(RenderableObj toRender, Matrix light){
        Mesh[] meshes = toRender.getMeshes();
        for(int i = 0; i < meshes.length;i++){
            if(meshes[i].usesTexture()) {
                draw3DVBO(meshes[i],light);
            }
        }
    }
}
