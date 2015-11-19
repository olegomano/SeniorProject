package com.projects.oleg.seniorproject.Rendering.ObjParser;

import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by Oleg Tolstov on 4:53 PM, 11/18/15. SeniorProject
 */
public class Group {
    String name = "";
    Material mat;
    ArrayList<Float> floatBuffer = new ArrayList<>();

    public ArrayList<Float> getData(){
        return floatBuffer;
    }

    public Material getMat(){
        return mat;
    }

    public String getName(){
        return name;
    }

    public String toString(){
        String retS = "Group name: " + name;
        retS+="\n   has " + floatBuffer.size()/10 + " verts: ";
        retS+="\n   uses mat:  " + mat.getName() + "at: " + mat.getPath();
        for(int i = 0; i < floatBuffer.size(); i+=10){
            retS+="\n     (";
            for(int b = 0; b < 4; b++){
                retS+=" " + floatBuffer.get(b);
            }
            retS+=")(";
            for(int b = 4; b < 8; b++){
                retS+=" " + floatBuffer.get(b);
            }
            retS+=")(";
            for(int b = 8; b < 10; b++){
                retS+=" " + floatBuffer.get(b);
            }
            retS+=")";
        }
        return retS;
    }
}
