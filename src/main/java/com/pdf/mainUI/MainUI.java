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
import java.awt.event.*;
import java.io.*;
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
                    while (true){
                        try {
                            request();
                        }catch (Exception e){
                            logger.error("网络出现异常，正在重连···");
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            logger.error("线程出错···");
                        }
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
        logger.info("请求结果：{}",result);
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
                    logger.info("请求成功：{}",result2);
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
            logger.error("程序运行错误：{}", e);
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
            logger.info("获取编号为：{}", bianhao);
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
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.setTray();
        this.setVisible(true);

    }

    //添加托盘显示：1.先判断当前平台是否支持托盘显示
    public void setTray() {

        if(SystemTray.isSupported()){//判断当前平台是否支持托盘功能
            //创建托盘实例
            SystemTray tray = SystemTray.getSystemTray();
            //创建托盘图标：1.显示图标Image 2.停留提示text 3.弹出菜单popupMenu 4.创建托盘图标实例
            //1.创建Image图像
            File file = new File("");
            String courseFile = null;
            try {
                courseFile = file.getCanonicalPath() + "\\print.jpg";
            } catch (IOException e) {
                logger.error("获取图片资源失败");
                e.printStackTrace();
            }
            Image image = Toolkit.getDefaultToolkit().getImage(courseFile);
            //2.停留提示text
            String text = "PDF Print";
            //3.弹出菜单popupMenu
            PopupMenu popMenu = new PopupMenu();
            MenuItem itmOpen = new MenuItem("open");
            itmOpen.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    Show();
                }
            });
            MenuItem itmExit = new MenuItem("close");
            itmExit.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    Exit();
                }
            });
            popMenu.add(itmOpen);
            popMenu.add(itmExit);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int option = JOptionPane.showConfirmDialog(MainUI.this,"是否最小化到托盘?",
                            "提示:", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION){
                        UnVisible();
                    }else {
                        Exit();
                    }
                }
            });

            //创建托盘图标
            TrayIcon trayIcon = new TrayIcon(image,text,popMenu);
            //设置自动调整图标大小以适应当前平台的托盘图标显示
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2){
                        Show();
                    }
                }
            });
            //将托盘图标加到托盘上
            try {
                tray.add(trayIcon);
            } catch (AWTException e1) {
                e1.printStackTrace();
            }
        }
    }

    //内部类中不能直接调用外部类（this不能指向）
    public void UnVisible() {
        this.setVisible(false);
    }
    public void Show() {
        this.setVisible(true);
    }
    public void Exit() {
        System.exit(0);
    }
}
