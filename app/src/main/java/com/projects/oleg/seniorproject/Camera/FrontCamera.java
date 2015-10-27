package com.projects.oleg.seniorproject.Camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SizeF;

import com.projects.oleg.seniorproject.Utils;

/**
 * Created by Oleg Tolstov on 8:35 PM, 10/9/15. SeniorProject
 */
public class FrontCamera extends CameraDevice.StateCallback{
    public static SizeF sensorSizeMM; //size of the physical sensor in mm
    public static Rect sensorActivePixels; //the pixels on the sensor that are used when taking pictures, Face recog is in this space
    public static Size sensorAllPixels; //all the pixels in the sensor

    public static float pixelToMM; //totalPixelsInSensor / sizeOfSensor

    private Context mContext;
    private CameraManager cameraManager;
    private String mId;
    private CameraDevice camera;
    private boolean opened = false;

    private volatile HandlerThread workerThread = new HandlerThread("MWorker");

    public FrontCamera(Context context) throws CameraAccessException {
        workerThread.start();
        mContext = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mId = getFontCamera();
        cameraManager.openCamera(mId,this,new Handler(workerThread.getLooper()));
        Utils.print("Requested to open Camera");
        sensorSizeMM = cameraManager.getCameraCharacteristics(mId).get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        sensorAllPixels = cameraManager.getCameraCharacteristics(mId).get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        sensorActivePixels = cameraManager.getCameraCharacteristics(mId).get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        pixelToMM = (float)sensorSizeMM.getWidth()/ (float)sensorAllPixels.getWidth() ;

    }

    public synchronized boolean start(CameraListener out) throws CameraAccessException {
        if(!opened){
            return false;
        }
        StreamConfigurationMap streamMap = cameraManager.getCameraCharacteristics(mId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = streamMap.getOutputSizes(ImageFormat.YUV_420_888); //todo: get maximum available size instead of taking first one
        out.configureBufferSize(sizes[((int) (sizes.length -1))].getWidth(), sizes[((int) (sizes.length -1))].getHeight());
        Utils.print("Set camera image size to: " + sizes[((int) (sizes.length - 1))].getWidth() + ", " + sizes[((int) (sizes.length - 1))].getHeight());
        CaptureRequest.Builder request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW); //this ones gives highest framerate
        request.set(CaptureRequest.TONEMAP_MODE,CaptureRequest.TONEMAP_MODE_HIGH_QUALITY);
        for(int i = 0; i < out.getSurfaceList().size(); i++){
            request.addTarget(out.getSurfaceList().get(i));
        }
        request.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE); //my phone only supports this one
        out.setCaptureRequest(request.build());

        camera.createCaptureSession(out.getSurfaceList(), out,new Handler(workerThread.getLooper()));
        return true;
    }

    private String getFontCamera(){
        try {
            String cameraID[] = cameraManager.getCameraIdList();
            for(int i = 0; i < cameraID.length; i++){
                CameraCharacteristics cam = cameraManager.getCameraCharacteristics(cameraID[i]);
                int camPosition = cam.get(CameraCharacteristics.LENS_FACING);
                if(camPosition == CameraCharacteristics.LENS_FACING_FRONT){
                    return cameraID[i];
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public synchronized void onOpened(CameraDevice camera) {
        Utils.print("opened camera");
        opened = true;
        this.camera = camera;
    }

    @Override
    public void onDisconnected(CameraDevice camera) {

    }

    @Override
    public void onError(CameraDevice camera, int error) {

    }


}
