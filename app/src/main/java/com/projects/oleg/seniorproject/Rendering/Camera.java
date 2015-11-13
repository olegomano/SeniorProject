package com.projects.oleg.seniorproject.Rendering;

import com.projects.oleg.seniorproject.Utils;

/**
 * Created by Oleg Tolstov on 7:59 PM, 10/16/15. SeniorProject
 */
public class Camera {
    private Matrix mMatrix = new Matrix();
    private float[] projectionCamera = new float[16];
    private float[] buff = new float[16];
    private float[] projection =  new float[16];

    public Camera(){
        android.opengl.Matrix.setIdentityM(projection,0);
    }

    public void createFrustrum(float near, float far, float left, float right, float bottom, float top){
        android.opengl.Matrix.frustumM(projection, 0, left, right, bottom, top, near, far);
    }

    public void createFrustrum(float angle){
        android.opengl.Matrix.perspectiveM(projection,0,angle,1,1,100);
    }

    public float[] getProjectionCamera(){
        mMatrix.getInverse(buff);
        android.opengl.Matrix.multiplyMM(projectionCamera, 0, projection, 0, mMatrix.getMatrix(), 0);
        return projectionCamera;
    }

    public Matrix getMatrix(){
        return mMatrix;
    }

    public String toString(){
        String rets = mMatrix.toString() + "\n Projection \n";
        rets+= Utils.matToString(projection) + "\n Mul \n ";
        rets+= Utils.matToString(projectionCamera);
        return rets;
    }

}
