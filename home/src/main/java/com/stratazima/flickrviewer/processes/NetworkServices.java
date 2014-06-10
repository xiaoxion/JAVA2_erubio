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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.processes
 * Author  Esau
 * Date    6/3/2014
 */

public class NetworkServices extends IntentService {
    public static final String MESSAGE = "messenger";

    public NetworkServices() {
        super("NetworkServices");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extra = intent.getExtras();
        Messenger messenger = (Messenger) extra.get(MESSAGE);
        String url = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=48e35c7d6af648edda8f9de6cc9bdca2&format=json&nojsoncallback=1";
        String result = "";

        try {
            InputStream is = null;
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
                    sb.append(line);
                }
                is.close();
                result = sb.toString();
            } catch(Exception e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject daObj = null;
        try {
            daObj = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject[] objects = null;
        if (daObj != null) {
            objects = new JSONObject[20];
            for (int i = 0; i < 20; i++) {
                objects[i] = new JSONObject();
                JSONObject temp = null;

                try {
                    temp = (JSONObject) daObj.getJSONObject("photos").getJSONArray("photo").get(i);
                    objects[i].put("title", temp.getString("title"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (temp != null) {
                    try {
                        InputStream is = null;
                        // HTTP
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpGet httpGet = new HttpGet("https://api.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key=48e35c7d6af648edda8f9de6cc9bdca2&photo_id=" + temp.getString("id") + "&format=json&nojsoncallback=1");
                            HttpResponse response = httpclient.execute(httpGet);
                            HttpEntity entity = response.getEntity();
                            is = entity.getContent();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Read response to string
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            is.close();
                            result = sb.toString();
                            JSONObject temporary = new JSONObject(result);
                            objects[i].put("username", temporary.getJSONObject("photo").getJSONObject("owner").getString("username"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (temp != null) {
                    try {
                        InputStream is = null;
                        // HTTP
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpGet httpGet = new HttpGet("https://api.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=48e35c7d6af648edda8f9de6cc9bdca2&photo_id=" + temp.getString("id") + "&format=json&nojsoncallback=1");
                            HttpResponse response = httpclient.execute(httpGet);
                            HttpEntity entity = response.getEntity();
                            is = entity.getContent();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Read response to string
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            is.close();
                            result = sb.toString();
                            JSONObject temporary = new JSONObject(result);

                            JSONObject temporary2 = (JSONObject) temporary.getJSONObject("sizes").getJSONArray("size").get(1);
                            objects[i].put("imageURL", temporary2.getString("source"));

                            // Checks for the largest possible photo and assigns it to largeURL
                            JSONArray temporary3 = temporary.getJSONObject("sizes").getJSONArray("size");

                            int daInt = temporary3.length() - 1;
                            if (daInt > 8) {
                                temporary2 = (JSONObject) temporary3.get(8);
                            } else {
                                temporary2 = (JSONObject) temporary3.get(daInt);
                            }

                            objects[i].put("largeURL", temporary2.getString("source"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        objects[i].put("rating", 0);
                        objects[i].put("id", temp.get("id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Message message = Message.obtain();
        message.arg1 = Activity.RESULT_OK;
        message.obj = Arrays.toString(objects);

        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
