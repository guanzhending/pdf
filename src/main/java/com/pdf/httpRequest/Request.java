package com.pdf.httpRequest;

import com.pdf.common.Contains;
import com.pdf.mainUI.MainUI;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class Request {

    public static Logger logger = LoggerFactory.getLogger(Request.class);

    public static String postRequest(JSONObject jsonObject, String requestURL) throws Exception{
        logger.info("开始请求，入参：{}，{}",jsonObject, requestURL);
        String result = "";
        BufferedReader send_in = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(requestURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
            out.close();

            send_in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = send_in.readLine()) != null){
                result += line;
            }
            logger.info("result:{}", result);
        }catch (Exception e){
            logger.error("postRequest 异常：{}", e);
            throw new Exception(e);
        }finally {
            try {
                if (send_in != null) {
                    send_in.close();
                }
                if (httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put(Contains.PRINTER_NUM, "001");
        postRequest(jsonObject2,"http://hqyp.fenith.com/api/getPrintFiles");
    }
}
