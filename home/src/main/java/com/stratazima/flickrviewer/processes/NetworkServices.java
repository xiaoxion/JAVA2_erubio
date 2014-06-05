package com.stratazima.flickrviewer.processes;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.processes
 * Author  Esau
 * Date    6/3/2014
 */

public class NetworkServices extends IntentService {
    public static final String MESSAGE = "messenger";
    public static final String TYPE = "type";
    public static final String USER_ID = "user";

    public NetworkServices() {
        super("NetworkServices");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extra = intent.getExtras();
        Messenger messenger = (Messenger) extra.get(MESSAGE);
        int getType = extra.getInt(TYPE);


        String url = "";
        if (getType == 0){
            url = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=48e35c7d6af648edda8f9de6cc9bdca2&format=json&nojsoncallback=1";
        } else if (getType == 1) {
            String getUserID = extra.getString(USER_ID);
            url = "https://api.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key=48e35c7d6af648edda8f9de6cc9bdca2&photo_id=" + getUserID + "&format=json&nojsoncallback=1";
        }

            try {
                InputStream is = null;
                String result = "";

                // HTTP
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse response = httpclient.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch(Exception e) {
                    e.printStackTrace();
                }

                // Read response to string
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                } catch(Exception e) {
                    e.printStackTrace();
                }

                JSONObject tempObj;
                // Convert string to object
                try {
                    tempObj = new JSONObject(result);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        Message message = Message.obtain();
        message.arg1 = Activity.RESULT_OK;
        message.obj = "Service is Done";

        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
