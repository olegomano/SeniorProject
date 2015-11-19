package com.projects.oleg.seniorproject.Rendering.ObjParser;

import java.util.ArrayList;

/**
 * Created by Oleg Tolstov on 11:36 PM, 11/16/15. SeniorProject
 */
public class Obj {
    String name;
    ArrayList<Float> data = new ArrayList<>();
    Material mMat;

    public String toString(){
        String retS = "Object; \n";
        retS+=" Name: " +name + "\n";
        retS+=" Material: " + mMat.toString() +"\n";
        retS+=" Data: \n";
        for(int i = 0; i < data.size();i+=10){
            retS+="V(" + data.get(i) + "," + data.get(i+1) + "," + data.get(i+2) + "," + data.get(i+3) + ")" +
                    "N(" + data.get(i+4) + "," + data.get(i+5) +"," + data.get(i+6) + "," + data.get(i+7) + ")" +
                    "UV(" + data.get(i+8) + "," + data.get(i+9) + ")\n";
        }
        return retS;
    }

    public ArrayList<Float> getData(){
        return data;
    }

    public Material getMat(){
        return mMat;
    }

}