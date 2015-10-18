package com.projects.oleg.seniorproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.projects.oleg.seniorproject.Rendering.MGlSurfaceView;

public class MainActivity extends AppCompatActivity {
    private MGlSurfaceView surfaceView;
    public static TextView output;
    public static float PPI_X;
    public static float PPI_Y;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view_layout);
        output = (TextView) findViewById(R.id.text_view);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        PPI_X = metrics.xdpi;
        PPI_Y = metrics.ydpi;
    }
}
