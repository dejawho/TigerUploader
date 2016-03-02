package com.tiger.tigeruploader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity {

    ProgressDialog prgDialog;

    String encodedString;

    String imgPath, fileName;

    private static int RESULT_LOAD_IMG = 1;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 135;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        checkPermission(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, "Read SD Card");
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
            handleSendImage(intent); // Handle single image being sent
        }
        checkPath();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_gallery:
                checkPermission(new String[] {Manifest.permission.INTERNET}, "Access to Internet");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URLResolver.SERVER_GALLERY_REMOTE));
                startActivity(browserIntent);
                return true;
            case R.id.action_load:
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void checkPath(){
        Button bUpload = (Button)findViewById(R.id.uploadPicture);
        if (imgPath != null && new File(imgPath).exists()){
            bUpload.setEnabled(true);
        } else {
            bUpload.setEnabled(false);
        }
    }

    private void checkPermission(String[] permissionIDs, String permissionName) {
        ArrayList<String> missingPermissions = new ArrayList<String>();
        for(String permissionID : permissionIDs){
            int hasPermission = ContextCompat.checkSelfPermission(MainActivity.this, permissionID);
            if (hasPermission != PackageManager.PERMISSION_GRANTED){
                missingPermissions.add(permissionID);
            }
        }

        if (!missingPermissions.isEmpty()) {
            for (final String missingPermission : missingPermissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, missingPermission)) {
                    try {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{missingPermission}, REQUEST_CODE_ASK_PERMISSIONS);
                    } catch (Exception ex){
                        Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                    return;
                }
            }
        }
    }


    protected void loadImage(Uri selectedImage){
        WebView imgView = (WebView) findViewById(R.id.imgView);
        imgView.getSettings().setBuiltInZoomControls(true);
        imgView.loadUrl(selectedImage.toString());
        if ("content".equals(selectedImage.getScheme())){
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            // Get the cursor
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                // Move to first row
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "Unable to resolve image path", Toast.LENGTH_LONG)
                        .show();
            }
        } else if ("file".equals(selectedImage.getScheme())){
            imgPath = selectedImage.getPath();
            // Get the Image's file name
            String fileNameSegments[] = imgPath.split("/");
            fileName = fileNameSegments[fileNameSegments.length - 1];
        }
    }

    // When Image is selected from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK  && null != data) {
                // Get the Image from data
                Uri selectedImage = data.getData();
                loadImage(selectedImage);
            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
            checkPath();
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    // When Upload button is clicked
    public void uploadImage(View v) {
        // When Image is selected from Gallery
        if (imgPath != null && !imgPath.isEmpty()) {
            Button bUpload = (Button)findViewById(R.id.uploadPicture);
            bUpload.setEnabled(false);
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            // Convert image to String using Base64
            encodeImageToString();
            bUpload.setEnabled(true);
        } else {
            // When Image is not selected from Gallery
            Toast.makeText(
                    getApplicationContext(),
                    "You must select image from gallery before you try to upload",
                    Toast.LENGTH_LONG).show();
        }
    }

    protected byte[] convertToByte(String path){
        File file = new File(path);

        byte[] b = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            b = new byte[(int) file.length()];
            fileInputStream.read(b);
        } catch (Exception ex) {
            b = null;
            Toast.makeText(
                    getApplicationContext(),
                    ex.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
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

    // AsyncTask - To convert Image to String
    public void encodeImageToString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(Void... params) {
                byte[] byte_arr = convertToByte(imgPath);
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.setMessage("Calling Upload");
                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload() {
        checkPermission(new String[] {Manifest.permission.INTERNET}, "Access to Internet");
        makeHTTPCall();
    }

    private String getQuery() throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        result.append(URLEncoder.encode("imageName", "UTF-8"));
        result.append("=");
        result.append(URLEncoder.encode(fileName, "UTF-8"));
        result.append("&");
        result.append(URLEncoder.encode("imageBytes", "UTF-8"));
        result.append("=");
        result.append(URLEncoder.encode(encodedString, "UTF-8"));
        return result.toString();
    }

    int responseCode = 0;
    String response = "";

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        prgDialog.setMessage("Sending File");
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(URLResolver.SERVER_URL_REMOTE);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(0);
                    conn.setConnectTimeout(0);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getQuery());
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
                    Toast.makeText(
                            getApplicationContext(),
                            ex.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                }
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.hide();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("link", response);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(getApplicationContext(), "Link Copied into the Clipboard: " + response, Toast.LENGTH_LONG).show();
                } else  if (responseCode == 404) {
                    Toast.makeText(getApplicationContext(),
                            "Requested resource not found",
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (responseCode == 500) {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong at server end",
                            Toast.LENGTH_LONG).show();
                }  else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "
                                    + responseCode, Toast.LENGTH_LONG)
                            .show();
                }
            }
        }.execute(null, null, null);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            loadImage(imageUri);
        }
    }
}
