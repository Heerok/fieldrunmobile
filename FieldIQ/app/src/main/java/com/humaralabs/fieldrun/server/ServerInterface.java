package com.humaralabs.fieldrun.server;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

public class ServerInterface {
    public static int timeoutConnectionTime=55;
    public static boolean BGcheckserverforgps=false;
    public static boolean BGcheckserverfortask=false;
    public static boolean Imagecheckserver=false;
    public static boolean checkserver=false;

    //this method is to call server api
    public static String CallServerApi(JSONObject data,String url,int timeout,String Token,String Iemi) {
        String result = "";
        try {
            data.put("deviceNo",Iemi);
            BasicHttpParams httpParameters = new BasicHttpParams();
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            int timeoutConnection = timeout * 1000;

            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = timeout * 1000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpPost httppost = new HttpPost(url);
            String json = data.toString();
            StringEntity se = null;
            se = new StringEntity(json);
            httppost.setEntity(se);
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("Authorization","Bearer "+Token);
            checkserver = true;
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String temp = sb.toString();
                result = temp;
                Log.i("Read from server", result);
            }
        }
        catch (SocketTimeoutException e) {
            checkserver = false;
        } catch (ConnectTimeoutException e) {
            checkserver = false;
        }  catch (ClientProtocolException e) {
            checkserver = false;
        } catch (Exception e) {
            checkserver = false;
            e.printStackTrace();
        }
        return result.toString().trim();
    }


    public static String CallServerApiBgForGPS(JSONObject data,String url,String Token,String Iemi) {
        String result = "";
        try {
            data.put("deviceNo",Iemi);
            BasicHttpParams httpParameters = new BasicHttpParams();
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            int timeoutConnection = 55*1000;

            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 55*1000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpPost httppost = new HttpPost(url);
            String json = data.toString();
            StringEntity se = null;

            se = new StringEntity(json);
            httppost.setEntity(se);
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("Authorization","Bearer "+Token);
            BGcheckserverforgps = true;
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String temp = sb.toString();
                result = temp;
                Log.i("Read from server", result);
            }
        }
        catch (SocketTimeoutException e) {
            BGcheckserverforgps = false;
            result="timeout";
        } catch (ConnectTimeoutException e) {
            BGcheckserverforgps = false;
            result="timeout";
        }  catch (ClientProtocolException e) {
            BGcheckserverforgps = false;
            result="";
        } catch (Exception e) {
            BGcheckserverforgps = false;
            result="";
            e.printStackTrace();
        }
        return result.toString().trim();
    }

    public static String CallServerApiBgForTask(JSONObject data,String url,String Token,String Iemi) {
        String result = "";
        try {
            data.put("deviceNo",Iemi);
            BasicHttpParams httpParameters = new BasicHttpParams();
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            int timeoutConnection = timeoutConnectionTime* 1000;

            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = timeoutConnectionTime * 1000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpPost httppost = new HttpPost(url);
            String json = data.toString();
            StringEntity se = null;
            se = new StringEntity(json);
            httppost.setEntity(se);
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("Authorization","Bearer "+Token);
            BGcheckserverfortask = true;
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String temp = sb.toString();
                result = temp;
                Log.i("Read from server", result);
            }
        }
        catch (SocketTimeoutException e) {
            BGcheckserverfortask = false;
            result="timeout";
        } catch (ConnectTimeoutException e) {
            BGcheckserverfortask = false;
            result="timeout";
        }  catch (ClientProtocolException e) {
            BGcheckserverfortask = false;
            result="";
        } catch (Exception e) {
            BGcheckserverfortask = false;
            result="";
            e.printStackTrace();
        }
        return result.toString().trim();
    }

    //upload task image
    public static String UploadImageApi(File imageFile,String url,JSONObject requestBody,String Token) {
        String result = "";
        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        int timeoutConnection = timeoutConnectionTime * 1000;

        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = timeoutConnectionTime * 1000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        try{
            HttpPost httpost = new HttpPost(url);
            String json = requestBody.toString();
            StringEntity se = null;
            se = new StringEntity(json);

            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
            multipartEntity.addPart("file", new FileBody(imageFile));
            multipartEntity.addPart("params", new StringBody(requestBody.toString()));
            multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntity.setLaxMode().setBoundary("xx").setCharset(Charset.forName("UTF-8"));
            httpost.setEntity(multipartEntity.build());
            httpost.setHeader("Accept", "application/json");
            httpost.setHeader("Authorization","Bearer "+Token);

            Imagecheckserver=true;
            HttpResponse response;
            response = httpclient.execute(httpost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String temp = sb.toString();
                result = temp;
                Log.i("Read from server", result);
            }
        } catch (SocketTimeoutException e) {
            Imagecheckserver = false;
        } catch (ConnectTimeoutException e) {
            Imagecheckserver = false;
        } catch (ClientProtocolException e) {
            Imagecheckserver = false;
        } catch (Exception e) {
            Imagecheckserver = false;
            e.printStackTrace();
        }
        return result.toString().trim();
    }
}
