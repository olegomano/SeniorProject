package com.projects.oleg.seniorproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.util.AttributeSet;
import android.view.View;

import com.projects.oleg.seniorproject.Camera.CameraImage;
import com.projects.oleg.seniorproject.Camera.FrontCamera;
import com.projects.oleg.seniorproject.openCV.Face;
import com.projects.oleg.seniorproject.openCV.FaceRecognitionListener;
import com.projects.oleg.seniorproject.openCV.OpenCVThread;

/**
 * Created by Oleg Tolstov on 6:00 PM, 10/23/15. SeniorProject
 */
public class CVTestView extends View implements FaceRecognitionListener{
    private FrontCamera camera;
    private CameraImage reader;
    private OpenCVThread cvThread;

    public CVTestView(Context context) {
        super(context);
        init();
    }

    public CVTestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CVTestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CVTestView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        try {
            camera = new FrontCamera(getContext());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        cvThread = new OpenCVThread(getContext());
        cvThread.setRecognitionListener(this);
        reader = new CameraImage(cvThread);
        try {
            while(!camera.start(reader)){}
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    private volatile Bitmap bmp;
    private Paint mPaint = new Paint();
    private long frameTime;
    private int fps = 0;
    private int frameCount = 0;
    @Override
    public synchronized void onDraw(Canvas canvas){
        super.onDraw(canvas);
        frameCount++;
        if(bmp != null){
            canvas.drawBitmap(bmp,0,0,null);
        }
        if(System.nanoTime() - frameTime > 1000000000){
            mPaint.setTextSize(45);
            mPaint.setColor(Color.RED);
            fps = frameCount;
            frameCount = 0;
            frameTime = System.nanoTime();
        }
        if(detectedFace!=null){
            canvas.drawRect(detectedFace.getLeftTop()[0],detectedFace.getLeftTop()[1],detectedFace.getRightBottom()[0],detectedFace.getRightBottom()[1],mPaint);
            canvas.drawText("Distance to face: " + detectedFace.getPosition()[0] + ", " + detectedFace.getPosition()[1] + ", " + detectedFace.getPosition()[2],0,450,mPaint);
        }
        canvas.drawText("FPS: " + fps,0,250,mPaint);
        detectedFace = null;
    }
    private Face detectedFace = null;
    @Override
    public synchronized void onRecognized(Face f, Bitmap frame) {
        bmp = frame;
        detectedFace = f;
        postInvalidate();
    }
}
