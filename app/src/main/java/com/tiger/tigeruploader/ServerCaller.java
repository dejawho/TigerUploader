/** This file is part of TigerUploader, located at
 * https://github.com/dejawho/TigerUploader

 TigerUploader is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Foobar is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Foobar.  If not, see <http://www.gnu.org/licenses/>.**/
package com.tiger.tigeruploader;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class ServerCaller {

    private String URL;

    private String response;

    private int responseCode;

    private int connectionTimeout = 0;

    private static final String twoHyphens = "--";

    private static final String boundary =  "*****";

    private  static final String lineEnd = "\r\n";

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


    public void sendChunkedFile(final byte[] byteData, final String fileName, final NotificationManager notificationManager, final NotificationCompat.Builder notificatonBuilder){
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String uuid = UUID.randomUUID().toString();
                int chunkSize = 1024 * 512; //1MB
                int chunkNumber = byteData.length / chunkSize;
                if (byteData.length % chunkSize != 0) chunkNumber++;

                notificatonBuilder.setProgress(chunkNumber, 1, false);
                Notification buildNotification = notificatonBuilder.build();
                buildNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(PermissionHandler.APP_ID, buildNotification);

                boolean allChunksSent = true;
                for (int i = 0; i < chunkNumber; i++) {
                    byte[] chunk = null;
                    if (i * chunkSize + chunkSize < byteData.length) {
                        chunk = Arrays.copyOfRange(byteData, i * chunkSize, i * chunkSize + chunkSize);
                    } else {
                        chunk = Arrays.copyOfRange(byteData, i * chunkSize, byteData.length);
                    }

                    DataOutputStream dos = null;
                    try {
                        URL url = new URL(ServerCaller.this.URL);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.disconnect();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", fileName);


                        dos = new DataOutputStream(conn.getOutputStream());
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //Adding first parameter
                        dos.writeBytes("Content-Disposition: form-data; name=\"qqtotalparts\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(String.valueOf(chunkNumber));
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //adding second parameter
                        dos.writeBytes("Content-Disposition: form-data; name=\"qquuid\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(uuid);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //Adding third parameter
                        dos.writeBytes("Content-Disposition: form-data; name=\"qqtotalfilesize\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(String.valueOf(byteData.length));
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //Adding fourth parameter
                        dos.writeBytes("Content-Disposition: form-data; name=\"qqfilename\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(fileName);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //Adding the part index parameter
                        dos.writeBytes("Content-Disposition: form-data; name=\"qqpartindex\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(String.valueOf(i));
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //Adding the current chunk
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"qqfile\";filename=\"" + fileName + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.write(chunk);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                        dos.flush();
                        dos.close();

                        int chunkResponseCode = conn.getResponseCode();
                        if (chunkResponseCode == HttpURLConnection.HTTP_OK){
                            response = "";
                            String line;
                            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            while ((line=br.readLine()) != null) {
                                response+=line;
                            }
                        } else {
                            response = "";
                            String line;
                            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            while ((line=br.readLine()) != null) {
                                response+=line;
                            }
                            allChunksSent = false;
                            break;
                        }
                        conn.disconnect();

                        notificatonBuilder.setProgress(chunkNumber, i + 1, false);
                        buildNotification = notificatonBuilder.build();
                        buildNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(PermissionHandler.APP_ID, buildNotification);
                    } catch (Exception ex) {
                        allChunksSent = false;
                        onException(ex);
                        break;
                    }
                }
                if (allChunksSent){
                    DataOutputStream dos = null;
                    try {
                        URL url = new URL(ServerCaller.this.URL + "?done");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.disconnect();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", fileName);


                        dos = new DataOutputStream(conn.getOutputStream());
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //Adding first parameter
                        dos.writeBytes("Content-Disposition: form-data; name=\"qqtotalparts\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(String.valueOf(chunkNumber));
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //adding second parameter
                        dos.writeBytes("Content-Disposition: form-data; name=\"qquuid\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(uuid);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //Adding third parameter
                        dos.writeBytes("Content-Disposition: form-data; name=\"qqfilename\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(fileName);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        dos.flush();
                        dos.close();

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

                    } catch (Exception ex){
                        onException(ex);
                    }
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
                    conn.connect();
                    conn.setConnectTimeout(connectionTimeout);
                    conn.setUseCaches(false);
                    conn.setDoOutput(true);

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    DataOutputStream request = new DataOutputStream(conn.getOutputStream());

                    request.writeBytes(twoHyphens + boundary + lineEnd);
                    request.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\"" + fileName + "\"" + lineEnd);
                    request.writeBytes(lineEnd);

                    request.write(byteData);
                    request.writeBytes(lineEnd);
                    request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
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
        sendFile(Utility.convertFileToByteData(file), file.getName());
    }

    public static byte[] downloadFile(String address) {
        InputStream input = null;
        ByteArrayOutputStream output = null;
        HttpURLConnection connection = null;
        byte[] result = null;
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return  null;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new ByteArrayOutputStream();

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
            result = output.toByteArray();
        } catch (Exception e) {
            TigerApplication.ShowException(e);
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }


    public void setConnectionTimeout(int connectionTimeout){
        this.connectionTimeout = connectionTimeout;
    }

    public void onException(Exception ex){
        TigerApplication.ShowException(ex);
    }

    public void onPostExecute(String response, int responseCode){

    }

}