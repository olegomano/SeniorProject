package com.projects.oleg.seniorproject.openCV;

/**
 * Created by Oleg Tolstov on 6:59 PM, 10/23/15. SeniorProject
 */
public class Face {
    float[] leftEye = {0,0};
    float[] rightEye = {0,0};
    float[] mouth = {0,0};
    float[] leftEar = {0,0};
    float[] rightEar = {0,0};


    public float[] getLeftEye(){
        return leftEye;
    }

    public float[] getRightEye(){
        return rightEye;
    }

    public float[] getMouth(){
        return mouth;
    }

    public float[] getLeftEar(){
        return leftEar;
    }

    public float[] getRightEar(){
        return rightEar;
    }
}
