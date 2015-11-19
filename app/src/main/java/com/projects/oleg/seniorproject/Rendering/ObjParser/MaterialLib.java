package com.projects.oleg.seniorproject.Rendering.ObjParser;

import android.content.Context;

import com.projects.oleg.seniorproject.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;


/**
 * Created by Oleg Tolstov on 9:07 PM, 11/15/15. SeniorProject
 */
public class MaterialLib {
    private final static String MAT_DECLR = "newmtl";
    private final static String TEXT_NAME = "map_Kd";

    private HashMap<String,Material> materials = new HashMap<>();
    private String mName = "";

    MaterialLib(Context context, String name) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader( context.getAssets().open(name)));
        mName = name;
        String line = reader.readLine();
        Material currMat = null;
        while(line != null){
            String[] lineSplit = line.split("\\s+");
            if(lineSplit[0].compareTo(MAT_DECLR)==0){
                Utils.print("Created new Mat: " + lineSplit[1]);
                Material mat = new Material();
                mat.name = lineSplit[1];
                currMat = mat;
                materials.put(mat.name, mat);
            }else if(lineSplit[0].compareTo(TEXT_NAME)==0){
                if(currMat==null){
                    Utils.print("ERRROR PARSING MATLIB, NO MATERIAL DECLARED");
                    return;
                }
                currMat.texturePath = lineSplit[1];
            }
            line = reader.readLine();
        }
    }

    public Material getMaterial(String name){
        return materials.get(name);
    }
    public String  getLibName(){
        return mName;
    }


}
