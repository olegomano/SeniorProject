package com.projects.oleg.seniorproject.Rendering.Geometry;

import com.projects.oleg.seniorproject.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**vertCount
 * Created by Oleg Tolstov on 6:57 PM, 10/16/15. SeniorProject
 */
public class Cube extends Renderable {
    protected FloatBuffer vertBuffer;
    protected FloatBuffer uvBuffer;
    protected ShortBuffer indexBuffer;
    protected int vertCount = 0;
    public Cube(){
        float[] verts = {
            .5f, .5f,.5f,1, //rt f
            .5f,-.5f,.5f,1, //rb f
           -.5f, .5f,.5f,1, //lt f
           -.5f,-.5f,.5f,1, //lb f

           .5f, .5f,-.5f,1, //rt f
           .5f,-.5f,-.5f,1, //rb f
          -.5f, .5f,-.5f,1, //lt f
          -.5f,-.5f,-.5f,1, //lb f

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
        };

        short[] indecies = {
            2,0,1, //front
            2,3,1,

            6,5,4, //back
            6,5,7,

            0,4,5, //r
            0,1,5,

            2,6,7,//l
            2,3,7,

            2,6,4,//t
            2,0,4,

            3,7,5,
            3,1,5

        };
        vertCount = indecies.length;
        uvBuffer = Utils.allocateFloatBuffer(uv);
        vertBuffer = Utils.allocateFloatBuffer(verts);
        indexBuffer = Utils.allocateShortBuffer(indecies);
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
        indexBuffer.position(0);
        return indexBuffer;
    }

    @Override
    public int getVertexCount() {
        return vertCount;
    }

    @Override
    public boolean usesIndexList(){
        return true;
    }

}
