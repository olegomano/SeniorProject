package com.projects.oleg.seniorproject.Rendering.Geometry;


import android.opengl.GLES20;

import com.projects.oleg.seniorproject.Rendering.Matrix;
import com.projects.oleg.seniorproject.Rendering.ObjParser.*;
import com.projects.oleg.seniorproject.Rendering.ObjParser.Obj;
import com.projects.oleg.seniorproject.Rendering.Texture.Texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by Oleg Tolstov on 3:39 PM, 11/15/15. SeniorProject
 */
public class RenderableObj {
    private Matrix modelMatrix = new Matrix();
    private Mesh[] meshes;

    public RenderableObj(Obj o){
        meshes = new Mesh[o.getGroups().size()];
        for(int i = 0; i < meshes.length;i++){
            meshes[i] = new Mesh(o.getGroups().get(i),this);
        }
    }

    public Mesh[] getMeshes(){
        return meshes;
    }

    public Matrix getModelMatrix(){
        return modelMatrix;
    }




}
