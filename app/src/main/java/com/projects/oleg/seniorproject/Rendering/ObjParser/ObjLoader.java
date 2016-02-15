package com.projects.oleg.seniorproject.Rendering.ObjParser;

import android.content.Context;

import com.projects.oleg.seniorproject.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Oleg Tolstov on 9:07 PM, 11/15/15. SeniorProject
 */
public class ObjLoader extends Thread{
    private static String MAT_LIB = "mtllib";
    private static String MAT_REF = "usemtl";
    private static String VERTEX = "v";
    private static String NORMAL = "vn";
    private static String UV = "vt";
    private static String FACE = "f";
    private static String COMMENT = "#";
    private static String SMOOTHING ="s";
    private static String OBJECT = "o";
    private static String GROUP = "g";

    public static void LoadObject(Context context, String name, OnLoadingComplete listener){
        new ObjLoader(listener,name,context).start();
    }


    private volatile OnLoadingComplete listener;
    private volatile String filePath;
    private volatile Context loadContext;

    ObjLoader(OnLoadingComplete l, String path, Context context){
        listener = l;
        filePath = path;
        loadContext = context;

    }

    @Override
    public void run() {
        try {
            listener.onLoaded(loadObj(loadContext, filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.print("Obj has Loaded");
    }

    public Obj loadObj(Context context, String name) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader( context.getAssets().open(name)));
        String line = reader.readLine();
        ArrayList<Float> normalList = new ArrayList<>();
        ArrayList<Float> vertexList = new ArrayList<>();
        ArrayList<Float> uvList = new ArrayList<>();

        float xScale = 0;
        float yScale = 0;
        float zScale = 0;

        Obj obj = new Obj();

        while(line != null){
            Utils.print("Processing line: " + line);
            String[] lineSplit = line.split("\\s+");

            if(lineSplit.length > 0){
                if(lineSplit[0].compareTo(COMMENT)==0){
                    Utils.print("Comment, IGNORING LINE ");
                }else if(lineSplit[0].compareTo(MAT_REF)==0){
                    Utils.print("Material Reference: " + lineSplit[1]);
                    if(obj.getCurrGroup()==null){
                        Utils.print("Malformed file, no object declared");
                        return null;
                    }
                    if(obj.getMatLib() == null){
                        Utils.print("Malformed file, no materialLib declared");
                        return null;
                    }
                    obj.setGroupMat(lineSplit[1]);
                }else if(lineSplit[0].compareTo(SMOOTHING) == 0){
                    Utils.print("Smoothing");
                }else if(lineSplit[0].compareTo(MAT_LIB) == 0){
                    Utils.print("Material Library: " + lineSplit[1]);
                    obj.setMatLib( new MaterialLib(context,lineSplit[1]) );
                }else if(lineSplit[0].compareTo(OBJECT)==0){
                    Utils.print("Object name: " + lineSplit[1]);
                    obj.name = lineSplit[1];
                }else if(lineSplit[0].compareTo(FACE)==0){
                    float[] faceTris = parseFace(lineSplit,vertexList,normalList,uvList);
                    for(int i = 0; i < faceTris.length;i++){
                        obj.getCurrGroup().floatBuffer.add(faceTris[i]);
                    }
                }else if(lineSplit[0].compareTo(GROUP)==0){
                    Utils.print("Group: " + lineSplit[1]);
                    obj.createGroup(lineSplit[1]);
                }else {
                    float x = Float.parseFloat(lineSplit[1]);
                    float y = Float.parseFloat( lineSplit[2] );
                    if(lineSplit[0].compareTo(VERTEX)==0){
                        float z = Float.parseFloat( lineSplit[3] );
                        vertexList.add(x);
                        vertexList.add(y);
                        vertexList.add(z);
                        if(Math.abs(x) > xScale){
                            xScale = Math.abs(x);
                        }
                        if(Math.abs(y) > yScale){
                            yScale = Math.abs(y);
                        }
                        if(Math.abs(z) > zScale){
                            zScale = Math.abs(z);
                        }
                    }else if(lineSplit[0].compareTo(NORMAL)==0){
                        float z = Float.parseFloat( lineSplit[3] );
                        normalList.add(x);
                        normalList.add(y);
                        normalList.add(z);
                    }else if(lineSplit[0].compareTo(UV)==0){
                        uvList.add(x);
                        uvList.add(y);
                    }
                }
                line = reader.readLine();
            }
        };
        //gUtils.print("Created Object: " + obj.toString());
        return obj;
    }

    private int[] vertINDX = new int[3];
    private int[] uvINDX = new int[3];
    private int[] normINDX = new int[3];
    private float[] returnVal = new float[30];//10 floats per vert, 4v/4norm/2uv
    private float[] parseFace(String[] face, ArrayList<Float> verts, ArrayList<Float> normals, ArrayList<Float> uv){//returns array of format vx,vy,vz,vw,nx,ny,nz,nw,ux,uy
        boolean hasUV = false;
        boolean hasNorm = false;
        for(int i = 0; i < face.length; i++){
            Utils.print("   " + face[i]);
        }
        for(int i = 0; i < vertINDX.length;i++){
            String[] faceNode = face[i+1].split("/");
            switch (faceNode.length){
                case 1:
                    vertINDX[i] = Integer.parseInt(faceNode[0]);
                    break;
                case 2:
                    vertINDX[i] = Integer.parseInt(faceNode[0]);
                    uvINDX[i] = Integer.parseInt(faceNode[1]);
                    hasUV = true;
                    break;
                case 3:
                    vertINDX[i] = Integer.parseInt(faceNode[0]);
                    normINDX[i] = Integer.parseInt(faceNode[2]);
                    if(faceNode[1].compareTo("")!=0) {
                        uvINDX[i] = Integer.parseInt(faceNode[1]);
                        hasUV = true;
                        hasNorm = true;
                    }else{
                        hasNorm = true;
                    }
                    break;
            }
        }

       int vertCount = 0;
        for(int i = 0; i < vertINDX.length-2;i++){ //take in groups of 3 at a time
            for(int b = 0; b < 3; b++){//makes a triangle
                int mIndex = i+b;
                float vertX = verts.get((vertINDX[mIndex]-1)*3); //vert x,y,z
                float vertY = verts.get((vertINDX[mIndex]-1)*3 + 1);
                float vertZ = verts.get((vertINDX[mIndex]-1)*3 + 2);

                float normX = normals.get((normINDX[mIndex]-1)*3);//norm x,y,z
                float normY = normals.get((normINDX[mIndex]-1)*3 + 1);
                float normZ = normals.get((normINDX[mIndex]-1)*3 + 2);

                float uvX = 0;
                float uvY = 0;
                if(hasUV) {
                    uvX = uv.get((uvINDX[mIndex] - 1) * 2); //uvX
                    uvY = uv.get((uvINDX[mIndex] - 1) * 2 + 1); //uvX
                }
                /*
                Utils.print(
                            "Created Vertex((vx,vy,vz),(nx,ny,nz),(uvX,uvY)): \n"
                            + "   (" +vertX + ", " + vertY +"," + vertZ +")"  +"\n"
                            + "   (" + normX +", " + normY + "," + normZ +")" +"\n"
                            + "   (" + uvX + "," + uvY + ")"
                            );
                */
                returnVal[vertCount*10] = vertX;
                returnVal[vertCount*10 + 1] = vertY;
                returnVal[vertCount*10 + 2] = vertZ;
                returnVal[vertCount*10 + 3] = 1;

                returnVal[vertCount*10 + 4] = normX;
                returnVal[vertCount*10 + 5] = normY;
                returnVal[vertCount*10 + 6] = normZ;
                returnVal[vertCount*10 + 7] = 1;

                returnVal[vertCount*10 + 8] = uvX;
                returnVal[vertCount*10 + 9] = uvY;

                vertCount++;
            }
        }
        return returnVal;
    }

    public interface OnLoadingComplete{
        public void onLoaded(Obj o);
    }

}
