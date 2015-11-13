
package com.projects.oleg.seniorproject.openCV;

import android.content.Context;
import android.graphics.Bitmap;

import com.projects.oleg.seniorproject.Camera.CameraImage;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.R;
import com.projects.oleg.seniorproject.Utils;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
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
   // private static final float FACE_WIDTH_MM;

    private volatile Object imageBufferLock = new Object();
    private volatile boolean run = true;
    private volatile int camPictureW;
    private volatile int camPictureH;
    private volatile boolean loaded = false;
    private volatile byte[] imgData;
    private volatile boolean newImage = false;
    private volatile Bitmap videoFrameBMP;
    private volatile Bitmap videoFrameBMPbuffer;
    private volatile boolean buffersCreated = false;

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


    public void onImageSizeChanged(int w, int h){
        synchronized (imageBufferLock) {
            camPictureH = h;
            camPictureW = w;
            cvRGBAMat = new Mat(w,h,CvType.CV_8UC4);
            videoFrameBMP = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
            videoFrameBMPbuffer = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
            imgData = new byte[w*h*4];
            Utils.print("CV thread configed mat: " + cvRGBAMat.width() + ", " + cvRGBAMat.height());
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

    public void onImageReady(byte[] y,byte[] u, byte[] v){
        synchronized (imageBufferLock){
            if(!loaded) return;
            Utils.yuvTbmp(camPictureW, camPictureH,y,u,v,videoFrameBMPbuffer);
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
            videoFrameBMP = Bitmap.createBitmap(videoFrameBMPbuffer);
            time = System.nanoTime() - time;
            bmpConvertTime+=time;
        }

        long time = System.nanoTime();
        org.opencv.android.Utils.bitmapToMat(videoFrameBMP, cvRGBAMat);
        Mat gray = new Mat();
        org.opencv.imgproc.Imgproc.cvtColor(cvRGBAMat, gray, Imgproc.COLOR_RGBA2GRAY);
        Mat equalized = new Mat();
        org.opencv.imgproc.Imgproc.equalizeHist(gray, equalized);

        Bitmap toDraw = Bitmap.createBitmap(camPictureW, camPictureH, Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(equalized, toDraw);

        Rect detectedFace = detectFace(equalized);
        Face mFace = null;

        if(detectedFace != null){
            mFace = new Face();
            mFace.leftTop[0] = (float) detectedFace.tl().x;
            mFace.leftTop[1] = (float) detectedFace.tl().y;
            mFace.rightBottom[0] = (float) detectedFace.br().x;
            mFace.rightBottom[1] = (float) detectedFace.br().y;
            calculateFacePose(detectedFace, mFace);
        }

        recogTime+=(System.nanoTime() - time);
        fps++;
        listener.onRecognized(mFace, videoFrameBMP);
        newImage = false;
    }

    private void calculateFacePose(Rect face,Face out){
        float sensorWPerc = (float)face.width / (float)camPictureW;
        float sizeOnSensorMM = sensorWPerc* FrontCamera.sensorSizeMM.getWidth()/2.0f;
        Utils.print("Face width pix: " + face.width + ", on sensor mm: " + sizeOnSensorMM);
        float distance = ( (Face.FACE_WIDTH_REAL_MM*FrontCamera.focalLength)/sizeOnSensorMM ) - FrontCamera.focalLength;
        out.position[2] = distance;

        float faceCenterX = (float) ((face.br().x + face.tl().x)/2.0);
        float faceCenterY = (float) ((face.br().y + face.tl().y)/2.0);
        float faceCenterXSensorPix = (faceCenterX/camPictureW)*FrontCamera.sensorActivePixels.width();
        float faceCenterYSensorPix = (faceCenterY/camPictureH)*FrontCamera.sensorActivePixels.width();

        float faceXPos = ( (faceCenterXSensorPix - FrontCamera.sensorActivePixels.width()/2.0f)*(distance + FrontCamera.focalLength)*FrontCamera.pixelToMM ) / FrontCamera.focalLength;
        float faceYPos = ( (faceCenterYSensorPix - FrontCamera.sensorActivePixels.height()/2.0f)*(distance + FrontCamera.focalLength)*FrontCamera.pixelToMM ) / FrontCamera.focalLength;

        out.position[0] =  faceXPos;
        out.position[1] = -faceYPos;

        out.position[0]*=Utils.MM_TO_INCH;
        out.position[1]*=Utils.MM_TO_INCH;
        out.position[2]*=Utils.MM_TO_INCH;
    }

    private Rect prevFaceBounds;
    private Rect detectFace(Mat mat){
        Mat detectionMat;
        float nextRscaleW = 1.6f;
        float nextRscaleH = 1.3f;
        boolean fullFrameDetect = false;
        Size minSize = new Size(10,10);
        Size maxSize = new Size(mat.width(),mat.height());
        double scaleFactor = 1.02;
        if(prevFaceBounds == null){ //detecting in full picture
            detectionMat = mat;
            fullFrameDetect = true;
            scaleFactor = 1.1f;
        }else{ //detecting in previous bounds
            detectionMat = mat.submat(prevFaceBounds);
            minSize.width = (prevFaceBounds.width/nextRscaleW)*.7f;
            minSize.height = (prevFaceBounds.width/nextRscaleH)*.7f;
            maxSize.width = prevFaceBounds.width;
            maxSize.height = prevFaceBounds.height;
        }

        long time = System.nanoTime();
        MatOfRect detectionResultMat = new MatOfRect();
        faceClassifier.detectMultiScale(detectionMat,detectionResultMat,scaleFactor,15,0,minSize,maxSize);
        long detectTime = System.nanoTime() - time;

        Rect[] results = detectionResultMat.toArray();
        Utils.print("Time to detect " + detectTime + " in mat size" + detectionMat.width()*detectionMat.height() + " Per pixel detect time = " + (detectTime/(detectionMat.width()*detectionMat.height())));

        if(results.length != 0){//found results
            if(!fullFrameDetect){ //found results using previous frame
                Utils.print("Found face using previous bounds");
                results[0].x += prevFaceBounds.x;
                results[0].y += prevFaceBounds.y;
            }
            prevFaceBounds = createNewFaceBounds(results[0],nextRscaleW,nextRscaleH, camPictureW, camPictureH);
            return results[0];

        }else{
            prevFaceBounds = null;
            return null;
        }
    }

    private Rect createNewFaceBounds(Rect currFace,float wRatio,float hRatio, int w, int h){
        int newW = (int) (currFace.width*wRatio);
        int newH = (int)(currFace.height*hRatio);
        int offsetX= (int) ((newW - currFace.width)/2.0f) ;
        int offsetY = (int) ((newH - currFace.height)/2.0f) ;;

        int top = (int) (currFace.y - offsetY);
        if(top < 0) top = 0;

        int left = (int) (currFace.x -offsetX);
        if(left < 0) left = 0;

        int bottom = (int) (top + newH);
        if(bottom > h) bottom = h;

        int right = (int) (left + newW);
        if(right > w) right =  w;

        Rect newBounds = new Rect(left,top,right - left, bottom - top);
        Utils.print("Created new Bounds: " + newBounds + ", old bounds " + currFace);
        return newBounds;
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
        File faceCascade = loadXMLFile("frontalface_default",R.raw.haarcascade_frontalface_alt);

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
        cvRGBAMat.release();
    }


}
