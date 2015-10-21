
package com.projects.oleg.seniorproject.openCV;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

/**
 * Created by Oleg Tolstov on 5:06 PM, 10/20/15. SeniorProject
 */
public class OpenCVThread extends Thread {
    private volatile Object imageBufferLock = new Object();
    private volatile boolean run = true;
    private volatile int bmpW;
    private volatile int bmpH;
    private volatile boolean loaded = false;
    private volatile byte[] imgData;

    private Mat cvMat = new Mat();
    private CascadeClassifier eyeClassifier;
    private CascadeClassifier mouthClassifier;
    private CascadeClassifier noseClassifier;
    private CascadeClassifier earClassifier;

    public OpenCVThread(int w, int h){
        super();
        bmpH = h;
        bmpW = w;
        imgData = new byte[w*h*4];
    }

    @Override
    public void run(){
        onThreadStarted();
        while(run){
            if(this.isInterrupted()){
                run = false;
                onThreadFinished();
                return;
            }
            onTick();
        }
        onThreadFinished();
    }

    public void newImageReady(byte[] imageData){
        synchronized (imageBufferLock){
            if(!loaded) return;
            System.arraycopy(imageData, 0, imgData, 0, imageData.length);
        }
    }

    private void onTick(){
        Bitmap bmp;
        synchronized (imageBufferLock){
            bmp = BitmapFactory.decodeByteArray(imgData,0,bmpW*bmpH*4);
            Utils.bitmapToMat(bmp,cvMat);
        }
    }

    private void onThreadStarted(){
        synchronized (imageBufferLock) {
            loaded = OpenCVLoader.initDebug();
        }
        if(!loaded){
            com.projects.oleg.seniorproject.Utils.print("ERROR LOADING CV");
            interrupt();
        }

        earClassifier = new CascadeClassifier();
        noseClassifier = new CascadeClassifier();
        mouthClassifier = new CascadeClassifier();
        eyeClassifier = new CascadeClassifier();

        com.projects.oleg.seniorproject.Utils.print("Created CV and loaded classifiers");
    }

    private void onThreadFinished(){

    }
}
