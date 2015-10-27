
package com.projects.oleg.seniorproject.openCV;

import android.content.Context;
import android.graphics.Bitmap;

import com.projects.oleg.seniorproject.Camera.CameraImage;
import com.projects.oleg.seniorproject.R;
import com.projects.oleg.seniorproject.Utils;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Oleg Tolstov on 5:06 PM, 10/20/15. SeniorProject
 */
public class OpenCVThread extends Thread implements CameraImage.OnImageReadyListener{
    private final String CASCADE_PATH = "raw/haarcascades/haarcascade_";
    private volatile Object imageBufferLock = new Object();
    private volatile boolean run = true;
    private volatile int bmpW;
    private volatile int bmpH;
    private volatile boolean loaded = false;
    private volatile byte[] imgData;
    private volatile boolean newImage = false;
    private volatile Bitmap videoFrameBMP;
    private volatile boolean buffersCreated = false;

    private volatile Mat cvYuvMat;
    private volatile Mat cvRGBAMat;
    private CascadeClassifier eyeClassifier;
    private CascadeClassifier mouthClassifier;
    private CascadeClassifier noseClassifier;
    private CascadeClassifier faceClassifier;

    private volatile FaceRecognitionListener listener;
    private volatile Context mContext;

    public OpenCVThread(Context context){
        super();
        mContext = context;
        start();

    }

    public void setRecognitionListener(FaceRecognitionListener l){
        listener = l;
    }


    public void configBuffers(int w, int h){
        synchronized (imageBufferLock) {
            bmpH = h;
            bmpW = w;
            cvYuvMat =  new Mat((int) (h + (float)h/2.0f),w, CvType.CV_8UC1);
            cvRGBAMat = new Mat(w,h,CvType.CV_8UC4);
            videoFrameBMP = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
            Utils.print("CV thread configed mat: " + cvYuvMat.width() + ", " + cvYuvMat.height() + ", " + cvRGBAMat.width() + ", " + cvRGBAMat.height());
            buffersCreated = true;
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

    public void onImageReady(byte[] y,byte[] u, byte[] v, android.hardware.camera2.params.Face face){
        synchronized (imageBufferLock){
            if(!loaded) return;
            imgData = new byte[y.length];
            System.arraycopy(y, 0, imgData, 0, imgData.length);
        }
        newImage = true;
    }

    private int fps= 0;
    private long fpsTimer;
    private long recogTime = 0;
    private long bmpConvertTime = 0;
    private void onTick(){
        if(!newImage || !buffersCreated){
            return;
        }
        if(System.nanoTime() - fpsTimer > 1000000000){
            Utils.print("Image recog fps: " + fps);
            if(fps!=0) {
                Utils.print("Image recog avg time: " + (recogTime / fps) * Utils.NANO_TO_SECOND);
                Utils.print("Bmp convert avg time: " + (bmpConvertTime / fps) * Utils.NANO_TO_SECOND);
            }
            recogTime =0;
            bmpConvertTime=0;
            fps=0;
            fpsTimer = System.nanoTime();
        }

        synchronized (imageBufferLock){
            if(imgData==null ) return;
            long time = System.nanoTime();
            cvYuvMat.put(0, 0, imgData);
            time = System.nanoTime() - time;
            bmpConvertTime+=time;
        }

        Imgproc.cvtColor(cvYuvMat, cvRGBAMat, Imgproc.COLOR_YUV420sp2RGB, 4);
        org.opencv.android.Utils.matToBitmap(cvRGBAMat, videoFrameBMP);

        Mat grayMat = new Mat();
        Imgproc.cvtColor(cvYuvMat,grayMat,Imgproc.COLOR_YUV420p2GRAY);
        long time = System.nanoTime();
        MatOfRect face = new MatOfRect();
        faceClassifier.detectMultiScale(grayMat, face);
        org.opencv.core.Rect[] faces = face.toArray();
        Utils.print("There were: " + faces.length + " faces detected in the picture");

        Face mFace = null;
        if(faces.length!=0){
            mFace = new Face();
            mFace.leftTop[0] = (float) faces[0].tl().x;
            mFace.leftTop[1] = (float) faces[0].tl().y;
            mFace.rightBottom[0] = (float) faces[0].br().x;
            mFace.rightBottom[1] = (float) faces[0].br().y;
        }

        recogTime+=(System.nanoTime() - time);
        fps++;
        listener.onRecognized(mFace, videoFrameBMP);
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

        noseClassifier  = new CascadeClassifier();
        mouthClassifier = new CascadeClassifier();
        eyeClassifier   = new CascadeClassifier();
        faceClassifier = new CascadeClassifier();
        File noseCascade = loadXMLFile("nose",R.raw.haarcascade_nose);
        File eyeCascade = loadXMLFile("eye",R.raw.haarcascade_eye);
        File mouthCascade = loadXMLFile("mouth",R.raw.haarcascade_mouth);
        File faceCascade = loadXMLFile("frontalface_default",R.raw.haarcascade_frontalface_default);

        loadClassifier(noseClassifier,noseCascade);
        loadClassifier(mouthClassifier, mouthCascade);
        loadClassifier(eyeClassifier, eyeCascade);
        loadClassifier(faceClassifier,faceCascade);
        com.projects.oleg.seniorproject.Utils.print("Created CV and loaded classifiers");
    }

    private File loadXMLFile(String fileName,int resID){
        File mFile = new File(mContext.getFilesDir(),fileName + ".xml");
        if(mFile.exists()){
            mFile.delete();
        }
        try {
            if(mFile.createNewFile()){
                Utils.print("Succesfully created new file");
            }else{
                Utils.print("Failed creating file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = mContext.getResources().openRawResource(resID);
        try {
            OutputStream fileWrite = new FileOutputStream(mFile);
            byte[] buffer = new byte[1024];
            int read = 0;
            read = inputStream.read(buffer);
            while(read  != -1){
                fileWrite.write(buffer,0,read);
                read = inputStream.read(buffer);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mFile;
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
        cvYuvMat.release();
    }


}
