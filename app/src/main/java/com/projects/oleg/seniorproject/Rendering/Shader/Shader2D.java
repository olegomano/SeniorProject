package com.projects.oleg.seniorproject.Rendering.Shader;

import android.opengl.GLES20;

import com.projects.oleg.seniorproject.Utils;

/**
 * Created by Oleg Tolstov on 12:19 AM, 10/12/15. SeniorProject
 */
public class Shader2D {
    private static final String vertexShader =
                    "precision mediump float;"+
                    "uniform   mat4  modelMat;"+
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
                    "  gl_Position = scaledVert;" +
                    "  gl_Position = modelMat*gl_Position;"+
                    "  gl_Position.y*=screenRatio;"+
                    "  vUV = aUV;" +
                    "}\n";

    private static final String fragmentShader =
                    "#extension GL_OES_EGL_image_external : require\n" +
                    "precision  mediump float;" +
                    "varying    vec2 vUV;" +
                    "uniform    samplerExternalOES extTexture;" +
                    "uniform    sampler2D texture;"+
                    "uniform    int textureType;"+
                    "void main() {" +
                    "    if(textureType == 0){"+
                    "        gl_FragColor = texture2D(extTexture,vUV);"+
                    "    }"+
                    "    if(textureType == 1){"+
                    "        gl_FragColor = texture2D(texture,vUV);"+
                    "    }"+
                    "}";

    private int programHandle = -1;

    private int modelMatrixHandle = -1;

    private int vertexHandle = -1;
    private int uvHandle = -1;
    private int scaleHandle =-1;

    private int screenRatioHandle = -1;

    private int textureTypeHandle = -1;
    private int extSamplerHandle = -1;
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
        modelMatrixHandle = GLES20.glGetUniformLocation(programHandle,"modelMat");
        Utils.checkGlError("get modelMatrix handle");
        extSamplerHandle = GLES20.glGetUniformLocation(programHandle,"extTexture");
        Utils.checkGlError("get ext sampler");
        regSamperHandle = GLES20.glGetUniformLocation(programHandle,"texture");
        Utils.checkGlError("got regular sampler");
        textureTypeHandle = GLES20.glGetUniformLocation(programHandle,"textureType");
        Utils.checkGlError("got text type handle");
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

    public int getExtSamplerHandle(){
        return extSamplerHandle;
    }

    public int getRegSamperHandle(){
        return regSamperHandle;
    }

    public int getTextureTypeHandle(){
        return textureTypeHandle;
    }

    public int getModelMatrixHandle(){
        return modelMatrixHandle;
    }

}
