package com.projects.oleg.seniorproject.Rendering.Shader;

import android.opengl.GLES20;

import com.projects.oleg.seniorproject.Utils;

/**
 * Created by Oleg Tolstov on 7:39 PM, 11/20/15. SeniorProject
 */
public class Shader3DObj {
    private static final String vertexShader =
                    "precision mediump float;"+
                    "uniform   mat4  modelMat;"+
                    "uniform   mat4  mvpMat;"+
                    "uniform   vec3  dirLight;"+
                    "uniform   float screenRatio;"+
                    "uniform   vec3 scale;"+

                    "attribute vec4 aVertex;" +
                    "attribute vec4 aNormal;"+
                    "attribute vec2 aUV;" +

                    "varying   vec2  vUV;" +
                    "varying   float vLight;"+

                    "void main() {" +
                    "  vec4 scaledVert = aVertex;"+
                    "  scaledVert.x*=scale.x;"+
                    "  scaledVert.y*=scale.y;"+
                    "  scaledVert.z*=scale.z;"+
                    "  gl_Position = mvpMat*scaledVert;"+
                    "  gl_Position.y*=screenRatio;"+
                    "  vec3 vertNorm = vec3(aNormal);"+
                    "  vLight = abs(dot(vertNorm,dirLight));"+
                    "  vUV = aUV;" +
                    "}\n";

    private static final String fragmentShader =
                    "precision  mediump float;" +
                    "varying    vec2 vUV;" +
                    "varying    float vLight;"+

                    "uniform    sampler2D texture;"+
                    "void main() {" +
                    "   vec4 sampledColor = texture2D(texture,vUV);"+
                    "   vec3 lightConstants = vec3(.35,.65,1);"+
                    "   gl_FragColor = sampledColor * lightConstants.x + sampledColor*vLight*lightConstants.y;"+
                    "   gl_FragColor.a = lightConstants.z;"+
                    "}";

    private int programHandle = -1;

    private int mvpMatHandle = -1;
    private int modelMatHandle = -1;

    private int vertexHandle = -1;
    private int uvHandle = -1;
    private int scaleHandle =-1;

    private int screenRatioHandle = -1;

    private int regSamperHandle = -1;

    private int dirLightHandle = -1;
    private int vertNormalHandle = -1;
    public void compile(){
        programHandle = Utils.createProgram(vertexShader, fragmentShader);
        vertexHandle  = GLES20.glGetAttribLocation(programHandle, "aVertex");
        Utils.checkGlError("get vertex handle");
        uvHandle = GLES20.glGetAttribLocation(programHandle, "aUV");
        Utils.checkGlError("get uv handle");
        scaleHandle = GLES20.glGetUniformLocation(programHandle, "scale");
        Utils.checkGlError("get scale handle");
        screenRatioHandle = GLES20.glGetUniformLocation(programHandle,"screenRatio");
        Utils.checkGlError("get screenRatio handle");
        mvpMatHandle = GLES20.glGetUniformLocation(programHandle,"mvpMat");
        Utils.checkGlError("got camera mat");
        dirLightHandle = GLES20.glGetUniformLocation(programHandle, "dirLight");
        Utils.checkGlError("Got light handle");
        vertNormalHandle = GLES20.glGetAttribLocation(programHandle, "aNormal");
        Utils.checkGlError("Got normal handle");
        modelMatHandle = GLES20.glGetAttribLocation(programHandle,"modelMat");
        Utils.checkGlError("Got modelMat");
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

    public int getVertNormalHandle(){
        return vertNormalHandle;
    }

    public int getDirLightHandle(){
        return dirLightHandle;
    }

    public int getModelMatHandle(){
        return modelMatHandle;
    }
}
