package com.pdf.Thread;

import com.pdf.common.Contains;
import com.pdf.httpRequest.Request;
import net.sf.json.JSONObject;

import java.util.Iterator;

public class PrintThread extends Thread{

    String bianhao;

    public PrintThread(String bianhao){

        this.bianhao = bianhao;
    }
    @Override
    public void run() {
        try {
            while (true){
                request(bianhao);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void request(String bianhao) throws Exception{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Contains.PRINTER_NUM, bianhao);
        String result = Request.postRequest(jsonObject, Contains.PRINTER_URL);
        System.out.println(result);
        JSONObject jsonObject1 = JSONObject.fromObject(result);
        String code = jsonObject1.get("code").toString();
        if ("200".equals(code)){
            JSONObject data = jsonObject1.getJSONObject("data");
            Iterator iterator = data.keys();
            while (iterator.hasNext()){
                String key = (String) iterator.next();
                String url = (String) jsonObject.get(key);
                System.out.println(url);
                String result2 = "";
                System.out.println(result2);
                if (Contains.SUCCESS.equals(result2)){
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put(Contains.PRINTER_NUM, bianhao);
                    jsonObject2.put(Contains.ID, key);
                    Request.postRequest(jsonObject2, Contains.CALLBACK_URL);
                }
            }
        }
    }
}
