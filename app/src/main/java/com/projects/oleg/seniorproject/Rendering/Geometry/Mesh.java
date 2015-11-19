package com.projects.oleg.seniorproject.Rendering.Geometry;

import android.opengl.GLES20;

import com.projects.oleg.seniorproject.Rendering.Matrix;
import com.projects.oleg.seniorproject.Rendering.ObjParser.Material;
import com.projects.oleg.seniorproject.Rendering.ObjParser.Obj;
import com.projects.oleg.seniorproject.Rendering.Texture.Texture;
import com.projects.oleg.seniorproject.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Oleg Tolstov on 2:50 PM, 11/18/15. SeniorProject
 */
public class Mesh extends Renderable{
    private Material mat;
    private int vboID;
    private RenderableObj parent;
    private Matrix mulledMat = new Matrix();
    private int vertCount = 0;

    public Mesh(Obj obj, RenderableObj p){
        ByteBuffer verts = ByteBuffer.allocateDirect(obj.getData().size()*4);
        verts.order(ByteOrder.nativeOrder());
        vertCount = obj.getData().size()/10;
        Utils.print("Created Mesh with " + vertCount + " vertecies");
        FloatBuffer mData = verts.asFloatBuffer();
        for(int i = 0; i < obj.getData().size(); i++){
            mData.put(i,obj.getData().get(i));
        }
        mData.position(0);
        vboID = createVBO(mData);
        mat = obj.getMat();
        mat.loadTexture();
        parent = p;
    }

    private int createVBO(FloatBuffer data){
        int[] vboID = new int[1];
        GLES20.glGenBuffers(1, vboID, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboID[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, data.capacity() * 4, data, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        return vboID[0];
    }

    @Override
    public int getVBO(){
        return vboID;
    }

    @Override
    public boolean usesVBO(){
        return true;
    }
    @Override
    public int getStride(){
        return 10;
    }

    public int getVertOffset(){return 0;}
    public int getNormOffset(){return 4;}
    public int getUVOffset(){return 8;}

    @Override
    public FloatBuffer getVertexBuffer() {
        return null;
    }

    @Override
    public FloatBuffer getUVBuffer() {
        return null;
    }

    @Override
    public ShortBuffer getIndexBuffer() {
        return null;
    }

    @Override
    public int getVertexCount() {
        return vertCount;
    }

    public Texture getTexture(){
        return mat.getTexture();
    }

    public Matrix getModelMatrix(){
        return parent.getModelMatrix();
    }
}