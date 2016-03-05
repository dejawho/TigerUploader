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

import android.os.AsyncTask;

import org.acra.ACRA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
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
        sendFile(Utility.convertFileToByteData(file), file.getName());
    }

    public void setConnectionTimeout(int connectionTimeout){
        this.connectionTimeout = connectionTimeout;
    }

    public void onException(Exception ex){
        ACRA.getErrorReporter().handleException(ex);
    }

    public void onPostExecute(String response, int responseCode){

    }

}