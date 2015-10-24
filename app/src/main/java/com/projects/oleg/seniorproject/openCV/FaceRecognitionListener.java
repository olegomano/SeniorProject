package com.projects.oleg.seniorproject.openCV;

import android.graphics.Bitmap;

/**
 * Created by Oleg Tolstov on 6:59 PM, 10/23/15. SeniorProject
 */
public interface FaceRecognitionListener {
    public void onRecognized(Face f, Bitmap frame);
}
