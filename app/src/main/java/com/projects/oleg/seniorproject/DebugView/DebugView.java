package com.projects.oleg.seniorproject.DebugView;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.projects.oleg.seniorproject.R;

/**
 * Created by Oleg Tolstov on 6:26 PM, 11/14/15. SeniorProject
 */
public class DebugView {
    private static boolean initialized = false;
    private static DebugView mThis;
    private static Handler mThreadHandler;


    private View dbugView;
    private TextView recogFPS;
    private TextView renderFPS;
    private TextView recogStatus;

    private TextViewPoster recogPoster;
    private TextViewPoster renderPoster;
    private TextViewPoster statusPoster;

    public static void initDebug(View v){
        mThis = new DebugView(v);
        mThreadHandler = new Handler(Looper.getMainLooper());
        initialized = true;
    }

    public static void putRecogFPS(String s){
        if(initialized){
            mThis.postFPSrecog(s,mThreadHandler);
        }
    }

    public static void putRenderFPS(String s){
        if(initialized){
            mThis.postFPSrender(s,mThreadHandler);
        }
    }

    public static void putRecogStatus(String s){
        if(initialized){
            mThis.postStatusRecog(s,mThreadHandler);
        }
    }

    public static boolean isInitialized(String s){
        return initialized;
    }

    private DebugView(View v){
        dbugView = v;
        recogFPS = (TextView) v.findViewById(R.id.recog_fps);
        renderFPS = (TextView) v.findViewById(R.id.rendering_fps);
        recogStatus = (TextView) v.findViewById(R.id.face_status);
        renderPoster = new TextViewPoster(renderFPS);
        recogPoster = new TextViewPoster(recogFPS);
        statusPoster = new TextViewPoster(recogStatus);
    }

    private void postFPSrecog(String s, Handler h){
        recogPoster.post(s,h);
    }

    private void postFPSrender(String s, Handler h){
        renderPoster.post(s,h);
    }

    private void postStatusRecog(String s, Handler h){
        statusPoster.post(s,h);
    }

    private class TextViewPoster implements Runnable{
        private TextView targetView;
        private String toPost = "";

        public TextViewPoster(TextView tg){
            targetView = tg;
        }

        public void post(String s, Handler h){
            toPost =s;
            h.post(this);
        }

        @Override
        public void run() {
            targetView.setText(toPost);
        }
    };

}
