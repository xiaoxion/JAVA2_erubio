package com.stratazima.flickrviewer.processes;

import android.content.Context;
import android.util.JsonReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.processes
 * Author  Esau
 * Date    6/4/2014
 */
public final class DataStorage implements Cloneable {
    private static DataStorage mInstance;
    private static Context mContext;
    JSONArray daObject = null;

    private DataStorage() {}

    public static DataStorage getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DataStorage();
        }
        mContext = context.getApplicationContext();
        return mInstance;
    }

    public boolean onCheckFile() {
        Boolean fileExist = false;

        File file = new File(mContext.getFilesDir().getPath() + "/flickr.txt");
        if (file.exists()) fileExist = true;

        return fileExist;
    }


    public void onWriteFile(String jsonObject) {
        try {
            FileOutputStream fos = mContext.openFileOutput("flickr.txt", Context.MODE_PRIVATE);
            fos.write(jsonObject.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray onReadFile() {
        String content;
        try {
            InputStream inputStream = mContext.openFileInput("flickr.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder contentBuffer = new StringBuilder();

            while ((content = bufferedReader.readLine()) != null) {
                contentBuffer.append(content);
            }

            content = contentBuffer.toString();
            daObject = new JSONArray(content);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return daObject;
    }
}
