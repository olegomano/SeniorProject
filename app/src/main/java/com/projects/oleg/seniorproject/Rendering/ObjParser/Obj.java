package com.projects.oleg.seniorproject.Rendering.ObjParser;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Oleg Tolstov on 11:36 PM, 11/16/15. SeniorProject
 */
public class Obj {
    private String name;
    private ArrayList<Group> groups = new ArrayList<>();
    private int curGroup = -1;
    private MaterialLib matLib;

    public Obj(String s){
        name = s;
    }

    public String toString(){
        String retS = "Object name: " + name + "\nmatLib: " + matLib.getLibName() + "\n";
        for(int i = 0; i < groups.size();i++){
            retS+=groups.get(i).toString() + "\n";
        }
        return retS;
    }

    public void setMatLib(MaterialLib m){
       matLib = m;
    }

    public void setGroupMat(String matName){
        groups.get(curGroup).mat = matLib.getMaterial(matName);
    }

    public ArrayList<Group> getGroups(){
        return groups;
    }

    public MaterialLib getMatLib(){
        return matLib;
    }

    public void createGroup(String name){
        Group nGroup = new Group();
        nGroup.name = name;
        groups.add(nGroup);
        curGroup++;
    }

    public Group getCurrGroup(){
        return groups.get(curGroup);
    }


}