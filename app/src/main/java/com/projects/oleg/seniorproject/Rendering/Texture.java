package com.projects.oleg.seniorproject.Rendering;

/**
 * Created by Oleg Tolstov on 9:50 PM, 10/14/15. SeniorProject
 */
public class Texture {
    private int type = -1;
    private int id = -1;

    public Texture(int text, int typ){
        type = typ;
        id = text;
    }

    public int getType() {
        return type;
    }

    public int getTexture(){
        return id;
    }
}
