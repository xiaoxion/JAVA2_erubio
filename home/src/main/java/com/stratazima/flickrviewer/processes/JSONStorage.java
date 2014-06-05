package com.stratazima.flickrviewer.processes;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.processes
 * Author  Esau
 * Date    6/4/2014
 */
public final class JSONStorage implements Cloneable {
    private static JSONStorage mInstance;
    private static Context mContext;

    private JSONStorage() {}

    public static JSONStorage getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new JSONStorage();
        }
        mContext = context.getApplicationContext();
        return mInstance;
    }


    public boolean onWriteFile(String jsonObject) {
        boolean results = false;
        try {
            FileOutputStream fos = mContext.openFileOutput("flickr.json", Context.MODE_PRIVATE);
            fos.write(jsonObject.getBytes());
            fos.close();
            results = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    public JSONObject onReadFile() {
        JSONObject daObject = null;
        String content;
        FileInputStream fis = null;
        try {
            fis = mContext.openFileInput("flickr.json");
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] contentBytes = new byte[1024];
            int bytesRead = 0;
            StringBuffer contentBuffer = new StringBuffer();

            while ((bytesRead - bis.read(contentBytes) != -1)) {
                content = new String(contentBytes, 0, bytesRead);
                contentBuffer.append(content);
            }

            content = contentBuffer.toString();
            daObject = new JSONObject(content);
            fis.close();
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
