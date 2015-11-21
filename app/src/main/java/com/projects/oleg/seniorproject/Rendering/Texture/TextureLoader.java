package com.projects.oleg.seniorproject.Rendering.Texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.projects.oleg.seniorproject.Rendering.ObjParser.Material;
import com.projects.oleg.seniorproject.Rendering.ObjParser.MaterialLib;
import com.projects.oleg.seniorproject.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Oleg Tolstov on 11:17 PM, 11/16/15. SeniorProject
 */
public class TextureLoader {
    private static final String FALLBACK_TEXTURE_KEY = "FALLBACK_TEXTURE";

    private static HashMap<String,Texture> textureTable = new HashMap<>();
    private static Context context;

    public static void initTextureLoader(Context c){
        context = c;
    }

    public static void createFallbackTexture(){

    }

    public static void loadMatLib(MaterialLib lib){

    }

    public static Texture getTexture(String name){
        return textureTable.get(name);
    }

    public static Texture loadTexture(String name) throws IOException {
        if(textureTable.containsKey(name)){
            return textureTable.get(name);
        }
        InputStream textureFile = context.getAssets().open(name);
        Bitmap textBit = BitmapFactory.decodeStream(textureFile);
        Texture mText = loadTexture(textBit);
        textBit.recycle();
        textureTable.put(name,mText);
        return mText;
    }


    public static Texture loadTexture(final int resourceId) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            Texture t =  loadTexture(bitmap);
            bitmap.recycle();
            return t;
    }

    public static Texture getFallbackTexture(){
        return textureTable.get(FALLBACK_TEXTURE_KEY);
    }


    private static Texture loadTexture(Bitmap bmp){
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        Utils.checkGlError("Checking GL error");
        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        Utils.checkGlError("Created Texture: " + textureHandle[0]);
        return new Texture(textureHandle[0],GLES20.GL_TEXTURE_2D);
    }
}
