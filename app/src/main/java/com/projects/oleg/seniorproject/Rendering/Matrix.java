package com.projects.oleg.seniorproject.Rendering;

/**
 * Created by Oleg Tolstov on 10:58 PM, 10/11/15. SeniorProject
 */
public class Matrix {
    private float[] matrix = new float[16];
    private float[] scale = {1,1,1};

    public  Matrix(){
        android.opengl.Matrix.setIdentityM(matrix,0);
    }

    public void rotate(float a, float x, float y, float z){
        android.opengl.Matrix.rotateM(matrix,0,a,x,y,z);
    }

    public void translate(float x, float y,float z){
        android.opengl.Matrix.translateM(matrix, 0, x, y, z);
    }

    public void setPosition(float x, float y, float z){
        matrix[12] = x;
        matrix[13] = y;
        matrix[14] = z;
    }

    public void getInverse(float[] out){
        android.opengl.Matrix.invertM(out,0,matrix,0);
    }

    public void lookAt(float x, float y, float z){

    }

    public void create(float[] right, float[] up, float[] forward){

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

}


