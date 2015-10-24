
package com.projects.oleg.seniorproject.openCV;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.projects.oleg.seniorproject.Camera.CameraImage;
import com.projects.oleg.seniorproject.Camera.FrontCamera;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;

/**
 * Created by Oleg Tolstov on 5:06 PM, 10/20/15. SeniorProject
 */
public class OpenCVThread extends Thread implements CameraImage.OnImageReadyListener{
    private final String CASCADE_PATH = "haarcascades/haarcascade_";
    private volatile Object imageBufferLock = new Object();
    private volatile boolean run = true;
    private volatile int bmpW;
    private volatile int bmpH;
    private volatile boolean loaded = false;
    private volatile byte[] imgData;
    private volatile boolean newImage = false;

    private Mat cvMat;
    private CascadeClassifier eyeClassifier;
    private CascadeClassifier mouthClassifier;
    private CascadeClassifier noseClassifier;

    private File eyeCascade   = new File(CASCADE_PATH+"eye.xml");
    private File mouthCascade = new File(CASCADE_PATH+"mouth.xml");
    private File noseCascade  = new File(CASCADE_PATH+"nose.xml");

    private volatile FaceRecognitionListener listener;


    public OpenCVThread(){
        super();
    }

    public void setRecognitionListener(FaceRecognitionListener l){
        listener = l;
    }


    public void configBuffers(int w, int h){
        synchronized (imageBufferLock) {
            bmpH = h;
            bmpW = w;
        }
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

    public void onImageReady(byte[] imageData){
        com.projects.oleg.seniorproject.Utils.print("Image is ready");
        long time = System.nanoTime();
        synchronized (imageBufferLock){
            if(!loaded) return;
            imgData = new byte[imageData.length];
            System.arraycopy(imageData, 0, imgData, 0, imageData.length);
        }
        time = System.nanoTime() - time;
        newImage = true;
        com.projects.oleg.seniorproject.Utils.print("array to array copy time: " + time * com.projects.oleg.seniorproject.Utils.NANO_TO_SECOND);

    }

    private void onTick(){
      //  com.projects.oleg.seniorproject.Utils.print("CV THREAD TICK");
        if(!newImage){
            return;
        }

        Bitmap bmp;
        synchronized (imageBufferLock){
            if(imgData==null) return;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            long time = System.nanoTime();
            bmp = BitmapFactory.decodeByteArray(imgData,0,imgData.length,options);
            time = System.nanoTime() - time;
            com.projects.oleg.seniorproject.Utils.print("array to bmp time: " + time* com.projects.oleg.seniorproject.Utils.NANO_TO_SECOND);
            if(bmp==null){
                //com.projects.oleg.seniorproject.Utils.print("Could not read bitmap");
                return;
            }
            time = System.nanoTime();
            Utils.bitmapToMat(bmp, cvMat);
            time = System.nanoTime() - time;
            com.projects.oleg.seniorproject.Utils.print("Bitmap to mat time: " + time* com.projects.oleg.seniorproject.Utils.NANO_TO_SECOND);

        }
        listener.onRecognized(null,Bitmap.createBitmap(bmp));
        newImage = false;
    }

    private void onThreadStarted(){
        synchronized (imageBufferLock) {
            loaded = OpenCVLoader.initDebug();
        }
        if(!loaded){
            com.projects.oleg.seniorproject.Utils.print("ERROR LOADING CV");
            interrupt();
        }
        cvMat = new Mat();
        noseClassifier  = new CascadeClassifier();
        mouthClassifier = new CascadeClassifier();
        eyeClassifier   = new CascadeClassifier();
        loadClassifier(noseClassifier,noseCascade);
        loadClassifier(mouthClassifier, mouthCascade);
        loadClassifier(eyeClassifier,eyeCascade);

        com.projects.oleg.seniorproject.Utils.print("Created CV and loaded classifiers");
    }

    private void loadClassifier(CascadeClassifier cc, File xml){
        if(xml.exists()){
            if(!cc.load(xml.getAbsolutePath())){
                com.projects.oleg.seniorproject.Utils.print("Failed to load " + xml.getName());
            }else{
                com.projects.oleg.seniorproject.Utils.print("Loaded " + xml.getName() + " succesfully");
            }
        }else{
            com.projects.oleg.seniorproject.Utils.print(xml.getName() + " Does not exist");
        }
    }

    private void onThreadFinished(){
        cvMat.release();
    }


}
