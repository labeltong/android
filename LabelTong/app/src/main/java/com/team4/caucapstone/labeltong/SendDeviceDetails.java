package com.team4.caucapstone.labeltong;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendDeviceDetails extends AsyncTask<String, Void, String> {
    public static final String POST_METHOD = "POST";
    public static final String GET_METHOD = "GET";
    private boolean isConnFinish = false;
    private boolean isErrorHappen = false;
    private String data;
    private int responseCode = 0;

    @Override
    protected String doInBackground(String... params) {

        data = "";
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
            Log.d("SERVER TEST", "CONNECTION START");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);


            if (params[2].equals(POST_METHOD)) {
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                httpURLConnection.setRequestProperty("Accept","application/json");

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
                responseCode = httpURLConnection.getResponseCode();
            }
            else {
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setUseCaches(true);
                httpURLConnection.setDefaultUseCaches(false);
                String strCookie = httpURLConnection.getHeaderField("Set-Cookie");
                InputStream in = httpURLConnection.getInputStream();

                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line+ "\n");
                }
                data = builder.toString();
                responseCode = httpURLConnection.getResponseCode();
            }
        } catch (Exception e) {

            isErrorHappen = true;
            if (params[2].equals(GET_METHOD)) {
                try{
                    responseCode = httpURLConnection.getResponseCode();
                    InputStream in = httpURLConnection.getErrorStream();
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line+ "\n");
                    }
                    data = builder.toString();
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                isConnFinish = true;
                Log.d("SERVER END", String.valueOf(responseCode));
            }
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    public boolean isConnFinish() {
        return isConnFinish;
    }
    public boolean isErrorHappen() {
        return isErrorHappen;
    }
    public String getResultData() {
        return data;
    }
    public int getResponseCode() {
        return responseCode;
    }
}
