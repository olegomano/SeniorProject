package com.projects.oleg.seniorproject.Rendering.ObjParser;

import com.projects.oleg.seniorproject.Rendering.Texture.Texture;
import com.projects.oleg.seniorproject.Rendering.Texture.TextureLoader;

import java.io.IOException;

/**
 * Created by Oleg Tolstov on 11:41 PM, 11/16/15. SeniorProject
 */
public class Material{
    String name = "";//name of material in .mat file
    String texturePath = ""; //path to image int assets folder
    Texture glTexture;
    boolean usesTexture = false;
    private boolean textureLoaded = false;


    public String toString(){
        return "Name: " + name + " Texture path: " + texturePath;
    }

    public boolean loadTexture(){
        try {
            glTexture = TextureLoader.loadTexture(texturePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        textureLoaded = true;
        return true;
    }

    public boolean usesTexture(){
        return usesTexture;
    }

    public boolean isLoadedGL(){
        return textureLoaded;
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return texturePath;
    }

    public Texture getTexture(){
        return glTexture;
    }
}
