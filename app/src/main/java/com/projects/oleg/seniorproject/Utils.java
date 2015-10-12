package com.projects.oleg.seniorproject;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Oleg Tolstov on 9:36 PM, 10/10/15. SeniorProject
 */
public class Utils {

    public static void print(String s){
        if(s!=null){
            Log.d("Project",s);
        }
    }

    public static float rerange(float oMin, float oMax, float nMin, float nMax, float val){
        float oMag = (val-oMin)/(oMax - oMin);
        float nVal = (nMax - nMin)*oMag + nMin;
        return nVal;
    }

    public static FloatBuffer allocateFloatBuffer(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    public static ShortBuffer allocateShortBuffer(short[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer fb = bb.asShortBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }



}
