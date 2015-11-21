package com.projects.oleg.seniorproject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Oleg Tolstov on 9:36 PM, 10/10/15. SeniorProject
 */
public class Utils {
    public static final float MM_TO_INCH = 0.0393701f;
    public static final float NANO_TO_SECOND = .000000001f;
    public static final float SECOND_TO_NANO = 1000000000;
    private static Random rand = new Random(System.nanoTime());
    public static void print(String s){
        if(s!=null){
            Log.d("Project", s);
        }
    }

    public static float genRand(float min, float max){
        return rand.nextFloat()*(max-min) + min;
    }

    public static boolean yuvTbmp(int w, int h,byte[] y, byte[] u,byte[] v,Bitmap bmp){
        if(bmp.getWidth()*bmp.getHeight() != y.length){
            print("Error converting, size mismatch");
            return false;
        }
        for(int xp = 0; xp < w; xp++){
            for(int yp = 0; yp < h; yp++){
                int bmpBit = (yp*w + xp);
                int my = toInt( y[bmpBit] );
                int mu = toInt( u[bmpBit/4]);
                int mv = toInt( v[bmpBit/4]);
                my = clamp((int) (1.164*(my)));

                int r = my;
                int g = my;
                int b = my;

                r = ( clamp(r) << 16 )& 0x00FF0000;
                g = ( clamp(g) << 8  )& 0x0000FF00;
                b = clamp(b) & 0x000000FF;
                int color = 0xFF000000|r|g|b;
                bmp.setPixel(xp,yp, color);
            }
        }
        return true;
    }

    public static float dotProduct(float[] v1,float[] v2){
        float product = 0;
        for(int i = 0; i < 3;i++){
            product+=(v1[i]*v2[i]);
        }
        return product;
    }

    public static void normalizeVec(float[] vec){
        float magnitude = getMagnitude(vec);
        for(int i = 0; i < 3; i++){
            vec[i]/=magnitude;
        }
    }


    public static void crossProduct(float[] v1,int v1Offset, float[] v2, int v2Offset, float[] out, int outOffset){

    }

    public static float getMagnitude(float[] vec){
        float sum = 0;
        for(int i = 0; i < 3; i++){
            sum+=( vec[i] * vec[i]);
        }
        return (float) Math.sqrt(sum);
    }

    public static int toInt(byte b){
        return ((int) b) & 0xff;
    }


    public static int clamp(int val){
        if(val <= 0){
            return 0;
        }
        if(val >= 255){
            return 255;
        }
        return val;
    }

    public static float rerange(float oMin, float oMax, float nMin, float nMax, float val){
        float oMag = (val-oMin)/(oMax - oMin);
        float nVal = (nMax - nMin)*oMag + nMin;
        return nVal;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                print( "Could not link program: ");
                print(GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        print(GLES20.glGetProgramInfoLog(program));
        return program;
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                print("Could not compile shader " + shaderType + ":");
                print(GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            print(op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
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

    public static String matToString(float[] matrix){
        String rets = "[ " + matrix[0] + ", " + matrix[1] + ", " + matrix[2] + ", "+matrix[3] + "] \n";
        rets += "[ " +       matrix[4] + ", " + matrix[5] + ", " + matrix[6] + ", "+matrix[7] + "] \n";
        rets += "[ " +       matrix[8] + ", " + matrix[9] + ", " + matrix[10] + ", "+matrix[11] + "] \n";
        rets += "[ " +       matrix[12] + ", " + matrix[12] + ", " + matrix[14] + ", "+matrix[15] + "] \n";
        return rets;
    }
}
