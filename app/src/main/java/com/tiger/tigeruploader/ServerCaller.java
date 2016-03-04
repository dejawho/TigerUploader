package com.tiger.tigeruploader;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ServerCaller {

    private String URL;

    private String response;

    private int responseCode;

    private int connectionTimeout = 0;

    private static final String crlf = "\r\n";

    private static final String twoHyphens = "--";

    private static final String boundary =  "*****";

    public ServerCaller(String URL){
        this.URL = URL;
    }

    public void sendCall(final String query){
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    java.net.URL url = new URL(ServerCaller.this.URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(0);
                    conn.setConnectTimeout(connectionTimeout);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);


                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    conn.connect();

                    responseCode=conn.getResponseCode();
                    conn.disconnect();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        response = "";
                        String line;
                        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line=br.readLine()) != null) {
                            response+=line;
                        }

                    }

                } catch (Exception ex) {
                    onException(ex);
                }
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                ServerCaller.this.onPostExecute(response, responseCode);
            }
        }.execute(null, null, null);

    }


    public void sendFile(final byte[] byteData, final String fileName){
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    java.net.URL url = new URL(ServerCaller.this.URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(connectionTimeout);
                    conn.setUseCaches(false);
                    conn.setDoOutput(true);

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    DataOutputStream request = new DataOutputStream(conn.getOutputStream());

                    request.writeBytes(twoHyphens + boundary + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\"" + fileName + "\"" + crlf);
                    request.writeBytes(crlf);

                    request.write(byteData);
                    request.writeBytes(crlf);
                    request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                    request.flush();
                    request.close();

                    responseCode=conn.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                        response = "";
                        String line;
                        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line=br.readLine()) != null) {
                            response+=line;
                        }
                        br.close();
                    }
                    conn.disconnect();

                } catch (Exception ex) {
                    onException(ex);
                }
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                ServerCaller.this.onPostExecute(response, responseCode);
            }
        }.execute(null, null, null);

    }


    public void sendFile(File file){
        sendFile(convertToByte(file), file.getName());
    }

    protected byte[] convertToByte(File file){
        byte[] b = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            b = new byte[(int) file.length()];
            fileInputStream.read(b);
        } catch (Exception ex) {
            b = null;
        } finally {
            if (fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (Exception ex){

                }
            }
        }
        return b;
    }


    public void setConnectionTimeout(int connectionTimeout){
        this.connectionTimeout = connectionTimeout;
    }

    public void onException(Exception ex){

    }

    public void onPostExecute(String response, int responseCode){

    }

}