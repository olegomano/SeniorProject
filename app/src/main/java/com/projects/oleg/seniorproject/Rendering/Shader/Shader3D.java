package com.projects.oleg.seniorproject.Rendering.Shader;

import android.opengl.GLES20;

import com.projects.oleg.seniorproject.Utils;

/**
 * Created by Oleg Tolstov on 7:45 PM, 10/16/15. SeniorProject
 */
public class Shader3D {
    private static final String vertexShader =
                    "precision mediump float;"+
                    "uniform   mat4  mvpMat;"+
                    "uniform   float screenRatio;"+
                    "uniform   vec3 scale;"+
                    "attribute vec4 aVertex;" +
                    "attribute vec2 aUV;" +
                    "varying   vec2 vUV;" +
                    "void main() {" +
                    "  vec4 scaledVert = aVertex;"+
                    "  scaledVert.x*=scale.x;"+
                    "  scaledVert.y*=scale.y;"+
                    "  scaledVert.z*=scale.z;"+
                    "  gl_Position = mvpMat*scaledVert;"+
                    "  gl_Position.y*=screenRatio;"+
                    "  vUV = aUV;" +
                    "}\n";

    private static final String fragmentShader =
                    "precision  mediump float;" +
                    "varying    vec2 vUV;" +
                    "uniform    sampler2D texture;"+
                    "void main() {" +
                    "   gl_FragColor = texture2D(texture,vUV);"+
                    "}";

    private int programHandle = -1;

    private int mvpMatHandle = -1;

    private int vertexHandle = -1;
    private int uvHandle = -1;
    private int scaleHandle =-1;

    private int screenRatioHandle = -1;

    private int regSamperHandle = -1;

    public void compile(){
        programHandle = Utils.createProgram(vertexShader, fragmentShader);
        vertexHandle  = GLES20.glGetAttribLocation(programHandle, "aVertex");
        Utils.checkGlError("get vertex handle");
        uvHandle = GLES20.glGetAttribLocation(programHandle, "aUV");
        Utils.checkGlError("get uv handle");
        scaleHandle   = GLES20.glGetUniformLocation(programHandle, "scale");
        Utils.checkGlError("get scale handle");
        screenRatioHandle = GLES20.glGetUniformLocation(programHandle,"screenRatio");
        Utils.checkGlError("get screenRatio handle");
        mvpMatHandle = GLES20.glGetUniformLocation(programHandle,"mvpMat");
        Utils.checkGlError("got camera mat");
    }

    public int getVertexHandle(){
        return vertexHandle;
    }

    public int getUvHandle(){
        return uvHandle;
    }

    public int getScreenRatioHandle(){
        return screenRatioHandle;
    }

    public int getProgramHandle(){
        return programHandle;
    }

    public int getScaleHandle(){
        return scaleHandle;
    }

    public int getSamplerHandle(){
        return regSamperHandle;
    }

    public int getMvpMatHandle(){
        return mvpMatHandle;
    }
}
