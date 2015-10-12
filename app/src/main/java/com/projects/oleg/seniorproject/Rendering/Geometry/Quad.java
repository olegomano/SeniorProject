package com.projects.oleg.seniorproject.Rendering.Geometry;

import com.projects.oleg.seniorproject.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Oleg Tolstov on 11:00 PM, 10/11/15. SeniorProject
 */
public class Quad extends Renderable {
    protected FloatBuffer vertBuffer;
    protected FloatBuffer uvBuffer;
    public Quad(){

    }

    private void init(){
        float[] vertex = {
            -1,-1,0,1, //lb
            -1, 1,0,1, //lt
             1, 1,0,1, //rt

             -1,-1,0,1, //lb
              1, 1,0,1, //rt
              1, -1,0,1 //rb
        };

        float[] uv = {

        };
        vertBuffer = Utils.allocateFloatBuffer(vertex);
        uvBuffer = Utils.allocateFloatBuffer(uv);

    }

    @Override
    public FloatBuffer getVertexBuffer() {
        return vertBuffer;
    }

    @Override
    public FloatBuffer getUVBuffer() {
        return uvBuffer;
    }

    @Override
    public ShortBuffer getIndexBuffer() {
        return null;
    }

    @Override
    public int getVertexCount() {
        return 6;
    }
}
