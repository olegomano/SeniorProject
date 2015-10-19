package com.projects.oleg.seniorproject.Rendering.Geometry;

import com.projects.oleg.seniorproject.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Oleg Tolstov on 8:25 PM, 10/18/15. SeniorProject
 */
public class Box extends Renderable {
    private FloatBuffer uvBuffer;
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private int vertCount = 0;

    public Box(){
        float[] verts = {
                 .5f, .5f,-.5f,1, //rt f 4
                .5f,-.5f,-.5f,1, //rb f 5
                -.5f, .5f,-.5f,1, //lt f 6
                -.5f,-.5f,-.5f,1, //lb f 7

                .5f, .5f,.5f,1, //rt f 0
                .5f, .5f,-.5f,1, //rt f 4
                .5f,-.5f,-.5f,1, //rb f 5
                .5f,-.5f,.5f,1, //rb f 1

                -.5f, .5f,.5f,1, //lt f 2
                -.5f,-.5f,.5f,1, //lb f 3
                -.5f, .5f,-.5f,1, //lt f 6
                -.5f,-.5f,-.5f,1, //lb f 7

                .5f, .5f,.5f,1, //rt f 0
                -.5f, .5f,.5f,1, //lt f 2
                -.5f, .5f,-.5f,1, //lt f 6
                .5f, .5f,-.5f,1, //rt f 4

                .5f,-.5f,.5f,1, //rb f 1
                -.5f,-.5f,.5f,1, //lb f 3
                .5f,-.5f,-.5f,1, //rb f 5
                -.5f,-.5f,-.5f,1, //lb f 7

        };

        float[] uv = {
                1,1,
                1,0,
                0,1,
                0,0,

                1,1,
                1,0,
                0,1,
                0,0,

                1,1,
                1,0,
                0,1,
                0,0,

                1,1,
                1,0,
                0,1,
                0,0,

                1,1,
                1,0,
                0,1,
                0,0,

        };

        short[] indecies = {
                0,1,2, //BACK FACE
                1,2,3,

                4,5,6,
                4,6,7,

                8,9,10,
                9,10,11,

                12,13,14,
                12,14,15,

                16,17,18,
                17,18,19

        };
        vertCount = indecies.length;
        uvBuffer = Utils.allocateFloatBuffer(uv);
        vertexBuffer = Utils.allocateFloatBuffer(verts);
        indexBuffer = Utils.allocateShortBuffer(indecies);
    }

    @Override
    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    @Override
    public FloatBuffer getUVBuffer() {
        return uvBuffer;
    }

    @Override
    public ShortBuffer getIndexBuffer() {
        return indexBuffer;
    }

    @Override
    public int getVertexCount() {
        return vertCount;
    }
}
