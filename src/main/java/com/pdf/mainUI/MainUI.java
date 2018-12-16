package com.pdf.mainUI;

import com.pdf.Print.PDFPrint;
import com.pdf.common.Contains;
import com.pdf.httpRequest.Request;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class MainUI extends JFrame implements ActionListener{

    public static Logger logger = LoggerFactory.getLogger(MainUI.class);

    static JButton jb1 = null;
    JPanel jp1, jp4 = null;
    JLabel j1b1,j1b2 = null;
    JPasswordField jpf = null;

    static String bianhao = null;

    private static final String print = "开始打印";

    public void actionPerformed(ActionEvent e) {
        try {
            if (print.equals(e.getActionCommand())){
                logger.info(print);
                jb1.setText("打印中···");
                jb1.setEnabled(false);
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        try {
                            while (true){
                                request();
                                Thread.sleep(1000);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            logger.error(e1.getMessage());
            jb1.setText("打印失败。请重新启动");
            jb1.setEnabled(false);
        }
    }

    public static void request() throws Exception{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Contains.PRINTER_NUM, bianhao);
        String result = Request.postRequest(jsonObject, Contains.PRINTER_URL);
        logger.info(result);
        JSONObject jsonObject1 = JSONObject.fromObject(result);
        String code = jsonObject1.get("code").toString();
        if ("200".equals(code)){
            JSONObject data = jsonObject1.getJSONObject("data");
            Iterator iterator = data.keys();
            while (iterator.hasNext()){
                String key = (String) iterator.next();
                String url = (String) data.get(key);
                String result2 = PDFPrint.print(url);
                if (Contains.SUCCESS.equals(result2)){
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put(Contains.PRINTER_NUM, bianhao);
                    jsonObject2.put(Contains.ID, key);
                    Request.postRequest(jsonObject2, Contains.CALLBACK_URL);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            logger.info("123");
            getBianhao();
            MainUI mainUI = new MainUI();
        }catch(Exception e){
            e.printStackTrace();
            jb1.setText(e.getMessage());
            jb1.setEnabled(false);
        }
    }

    public static  void getBianhao()  throws Exception{
        String encoding = "UTF-8";
        File file = new File("");
        String a = file.getCanonicalPath() + "\\bianhao.txt";
        file = new File(a);
        if (file.isFile() && file.exists()){
            InputStreamReader in = new InputStreamReader(new FileInputStream(file), encoding);
            BufferedReader bufferedReader = new BufferedReader(in);
            String lineText = null;
            StringBuffer s = new StringBuffer();
            while ((lineText = bufferedReader.readLine())!= null){
                s.append(lineText);
            }
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] b = decoder.decodeBuffer(s.toString());
            bianhao = new String(b, encoding);
        }else {
            throw new Exception("找不到编号文件夹");
        }
        if (bianhao == null){
            throw new Exception("编号为空");
        }
    }

    public MainUI(){
        Font font = new Font("宋体", Font.BOLD, 30);
        jb1 = new JButton(print);
        jb1.setFont(font);
        jb1.addActionListener(this);

        jp4 = new JPanel();
        jp4.add(jb1);

        jp1 = new JPanel();
        j1b1 = new JLabel("打印机编号：");
        j1b1.setFont(font);
        j1b2 = new JLabel(bianhao);
        j1b2.setFont(font);
        jpf = new JPasswordField(10);

        jp1.add(j1b1);
        jp1.add(j1b2);

        this.add(jp1);
        this.add(jp4);

        this.setLayout(new GridLayout(4,1));
        this.setTitle("打印机设置");
        this.setSize(1000,500);
        this.setLocation(400, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(true);
    }
}
