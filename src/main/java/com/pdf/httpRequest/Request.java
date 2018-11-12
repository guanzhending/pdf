package com.pdf.httpRequest;

import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class Request {

    public static String postRequest(JSONObject jsonObject, String requestURL) throws Exception{
        URL url = null;
        PrintWriter send_out = null;
        BufferedReader send_in = null;
        String result = "";

        url = new URL(requestURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www/form-urlencoded");
        httpURLConnection.connect();
        OutputStreamWriter out = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");

        Iterator iterator = jsonObject.keys();
        String param = "";
        while (iterator.hasNext()){
            String key = (String) iterator.next();
            String value = (String) jsonObject.get(key);
            param += key + "=" + value + "&";
        }
        param = param.substring(0, param.length() - 1);
        out.append(param);

        out.flush();

        send_in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line;
        while ((line = send_in.readLine()) != null){
            result += line;
        }
        return result;
    }
}