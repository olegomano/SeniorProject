package com.projects.oleg.seniorproject.Rendering.Geometry;

import com.projects.oleg.seniorproject.Rendering.Matrix;
import com.projects.oleg.seniorproject.Rendering.Texture.Texture;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Oleg Tolstov on 10:56 PM, 10/11/15. SeniorProject
 */
public abstract class Renderable {
    protected Texture texture;
    protected Matrix modelMatrix = new Matrix();

    public abstract FloatBuffer getVertexBuffer();
    public abstract FloatBuffer getUVBuffer();
    public abstract ShortBuffer getIndexBuffer();

    public void initVBO(){};
    public int getVBO(){return -1;}

    public int getStride(){return 4;}

    public int getVertOffset(){return 0;}
    public int getNormOffset(){return 0;}
    public int getUVOffset(){return 0;}

    public abstract int getVertexCount();
    public boolean usesVBO(){return false;}

    public boolean usesIndexList(){
        return false;
    }
    public boolean usesIndexVBO(){
        return false;
    }
    public Texture getTexture(){
        return texture;
    }
    public void setTexture(Texture t){
        texture = t;
    }
    public Matrix getModelMatrix(){
        return modelMatrix;
    }

}
