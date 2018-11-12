package com.pdf.mainUI;

import com.pdf.common.Contains;
import com.pdf.httpRequest.Request;
import net.sf.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

public class MainUI extends JFrame implements ActionListener{

    static JButton jb1 = null;
    JPanel jp1, jp4 = null;
    JLabel JLB1,JLB2 = null;
    JPasswordField jpf = null;

    static String bianhao = null;

    private static final String print = "开始打印";

    public void actionPerformed(ActionEvent e) {
        try {
            if (print.equals(e.getActionCommand())){
                jb1.setText("打印中···");
                jb1.setEnabled(false);
                while (true){
                }
            }
                request();
                Thread.sleep(1000);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void request() throws Exception{
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
