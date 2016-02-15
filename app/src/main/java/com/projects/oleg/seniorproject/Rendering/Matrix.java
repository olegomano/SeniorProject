package com.projects.oleg.seniorproject.Rendering;

import com.projects.oleg.seniorproject.Utils;

/**
 * Created by Oleg Tolstov on 10:58 PM, 10/11/15. SeniorProject
 */
public class Matrix {
    private float[] matrix = new float[16];
    private float[] scale = {1,1,1};
    private float[] posBuffer = new float[4];
    private float[] forwardBuffer = new float[4];


    public  Matrix(){
        android.opengl.Matrix.setIdentityM(matrix,0);
    }

    public void rotate(float a, float x, float y, float z){
        android.opengl.Matrix.rotateM(matrix, 0, a, x, y, z);
    }

    public void translate(float x, float y,float z){
        android.opengl.Matrix.translateM(matrix, 0, x, y, z);
    }

    public void setPosition(float x, float y, float z){
        matrix[12] = x;
        matrix[13] = y;
        matrix[14] = z;
    }

    public float[] getForward(){
        System.arraycopy(matrix,8,forwardBuffer,0,4);
        return forwardBuffer;
    }

    public void getInverse(float[] out){
        android.opengl.Matrix.invertM(out,0,matrix,0);
    }

    public float[] getPosBuffer(){
        System.arraycopy(matrix,12,posBuffer,0,4);
        return posBuffer;
    }

    public void transpose(float[] out){
        android.opengl.Matrix.transposeM(out, 0, matrix, 0);
    }

    public void mMul(Matrix right, Matrix out){
        android.opengl.Matrix.multiplyMM(out.matrix,0,matrix,0,right.matrix,0);
    }

    public void lookAt(float x, float y, float z){
        float[] forward = new float[4];
        forward[0] = x - matrix[12];
        forward[1] = y - matrix[13];
        forward[2] = z - matrix[14];
        Utils.normalizeVec(forward);
    }

    public void create(float[] right, float[] up, float[] forward){
        System.arraycopy(right,0,matrix,0,4);
        System.arraycopy(up,0,matrix,4,4);
        System.arraycopy(forward,0,matrix,8,4);
    }


    public float[] getMatrix(){
        return matrix;
    }

    public float[] getScale(){
        return scale;
    }

    public void mulScale(float x, float y, float z){
        scale[0]*=x;
        scale[1]*=y;
        scale[2]*=z;
    }

    public void setScale(float x, float y, float z){
        scale[0] = x;
        scale[1] = y;
        scale[2] = z;
    }

    public String toString(){
        return Utils.matToString(matrix);
    }

}


