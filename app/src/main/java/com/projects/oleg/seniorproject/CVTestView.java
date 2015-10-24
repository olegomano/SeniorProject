package com.projects.oleg.seniorproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.media.ImageReader;
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
        cvThread = new OpenCVThread();
        cvThread.setRecognitionListener(this);
        reader = new CameraImage(cvThread);

        cvThread.start();
        try {
            while(!camera.start(reader)){}
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    private volatile Bitmap bmp;
    private Paint mPaint = new Paint();
    @Override
    public synchronized void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(bmp != null){
            canvas.drawBitmap(bmp,0,0,mPaint);
            canvas.drawText("HAVE VIDEO BITMAP",0,0,mPaint);
        }
        canvas.drawText("NO VIDEO BITMAP",0,0,mPaint);
    }

    @Override
    public synchronized void onRecognized(Face f, Bitmap frame) {
        bmp = frame;
        postInvalidate();
    }
}
