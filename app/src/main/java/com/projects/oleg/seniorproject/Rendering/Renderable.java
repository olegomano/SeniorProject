package com.projects.oleg.seniorproject.Rendering;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Oleg Tolstov on 10:56 PM, 10/11/15. SeniorProject
 */
public abstract class Renderable {
    protected int texture;
    protected Matrix modelMatrix = new Matrix();

    public abstract FloatBuffer getVertexBuffer();
    public abstract FloatBuffer getUVBuffer();
    public abstract ShortBuffer getIndexBuffer();

    public int getVertexVBO(){return -1;}
    public int getUvVBO(){return -1;}
    public int getIndexVBO(){return -1;}



    public boolean usesVertexVBO(){
        return false;
    }

    public boolean usesUvVBO(){
        return false;
    }

    public boolean usesIndexList(){
        return false;
    }

    public boolean usesIndexVBO(){
        return false;
    }



    public int getTexture(){
        return texture;
    }

    public void setTexture(int t){
        texture = t;
    }

    public Matrix getModelMatrix(){
        return modelMatrix;
    }

}
