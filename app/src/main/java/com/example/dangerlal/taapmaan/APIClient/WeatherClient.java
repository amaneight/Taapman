// File: WeatherClient.java
//
// Author: Aman Sehgal
//
// Purpose: Communicate with Openweathermap.org using API and fetch current Temperature

package com.example.dangerlal.taapmaan.APIClient;

import android.util.Log;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class WeatherClient {
    private static final String TAG = "WeatherClient";

    public static JSONObject fetchCurrentTemperature(String latitude, String longitude){

        final JSONObject responseObject = new JSONObject();

        //openweathermap api id
        final String API_ID= "#################"; // Obtain your API ID from https://openweathermap.org/
        
        final String BASE_URI = "http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&APPID=";
        Double currentTemperature = 0.0;
        URL url = null;
        HttpURLConnection conn = null;

        //Make HTTP Request
        try{
            url = new URL(BASE_URI+API_ID);

            //open connection
            conn = (HttpURLConnection)url.openConnection();

            //set timeout
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);

            //add http header to set your response type to json

            conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            conn.setRequestProperty("Accept", "*/*");

            //Set request type
            conn.setRequestMethod("GET");

            //Read output and fetch token
            Scanner inStream = new Scanner(conn.getInputStream());
            String line = "";
            while(inStream.hasNextLine()){
                line += inStream.nextLine();
            }

            //Store retrieved result in JSON object
            JSONObject j =  new JSONObject(line);

            //Parse JSON Object to get temperature
            String strTemperature = j.getJSONObject("main").getString("temp");
            String strLocation = j.getString("name")+", "+j.getJSONObject("sys").getString("country");
            //Convert obtained temperature from Kelvin to Celcius
            currentTemperature = Double.parseDouble(strTemperature) - 273.15;

            responseObject.put("strTemperature",currentTemperature);
            responseObject.put("strLocation",strLocation);
            conn.disconnect();
            return responseObject;
        }catch(SocketTimeoutException ste){
            Log.e(TAG,ste.getMessage());
        }
        catch(Exception e){
            Log.e(TAG,e.getMessage());
        }
        return responseObject;
    }
}
