package com.pdf.Print;

import com.pdf.common.Contains;
import com.pdf.mainUI.MainUI;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.printing.PDFPageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.Pageable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * 如果报找不到 msatc.ttf，那么删除c:\windows\Fonts目录下的msatc.ttf
 * 删除 msatc.ttf 的参考地址：http://www.xitongcheng.com/jiaocheng/win7_article_35712.html
 * PDF打印主要类
 *
 */
public class PDFPrint {

    public static Logger logger = LoggerFactory.getLogger(MainUI.class);

    public static String print(String path){
        PDDocument document = null;
        PDDocument doc = null;
        String result = Contains.FAIL;
        InputStream inputStream = null;
        URLConnection urlConnection = null;

        try {

            logger.info("begin to print");
            URL url = new URL(path);

            urlConnection = url.openConnection();
            inputStream = urlConnection.getInputStream();

            String pdfFile = "123";
            document = PDDocument.load(inputStream);
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);
            PDPage  page = document.getPage(0);
            COSDictionary diction = page.getCOSObject();
            diction.setNeedToBeUpdated(true);

            COSBase cosBase = diction.getItem(COSName.RESOURCES);
            COSDictionary dic = getCOSDictionary(cosBase);
//            COSObject cosBase2 = (COSObject)dic.getItem(COSName.FONT);
//            COSDictionary dic1 = (COSDictionary)cosBase2.getObject();
            COSBase cosBase2 = dic.getItem(COSName.FONT);
            COSDictionary dic1 = getCOSDictionary(cosBase2);
            for (int i = 0; i < dic1.size(); i++) {
//                COSObject cosObject = (COSObject)dic1.getItem(COSName.getPDFName("F" + i));
//                COSDictionary a = (COSDictionary)cosObject.getObject();
                COSBase cosObject = dic1.getItem(COSName.getPDFName("F" + i));
                if (cosObject ==  null){
                    continue;
                }
                COSDictionary a = getCOSDictionary(cosObject);
                String aaaa= a.getCOSName(COSName.BASE_FONT).toString();
                logger.info("aaa = {}", aaaa);

                COSArray array = (COSArray) a.getItem(COSName.DESCENDANT_FONTS);
//                COSObject cosObject1 = (COSObject)array.get(0);
//                COSDictionary b = (COSDictionary)cosObject1.getObject();
                if(array == null || array.size() <= 0){
                    continue;
                }
                COSBase cosObject1 = array.get(0);
                COSDictionary b = getCOSDictionary(cosObject1);

//                COSDictionary c = (COSDictionary)b.getItem(COSName.FONT_DESC);
//                COSObject bb = (COSObject)b.getItem(COSName.FONT_DESC);
//                COSDictionary c = (COSDictionary)bb.getObject();
                COSBase bb = b.getItem(COSName.FONT_DESC);
                COSDictionary c = getCOSDictionary(bb);
                b.setItem(COSName.ENCODING, COSName.getPDFName("GBK"));
                if (a != null && aaaa.endsWith(",Bold}")){
                    a.setItem(COSName.BASE_FONT, COSName.getPDFName("SimSunBold"));
                    b.setItem(COSName.BASE_FONT, COSName.getPDFName("SimSunBold"));
                    c.setItem(COSName.FONT_NAME, COSName.getPDFName("SimSunBold"));
                }else if ("COSName{����}" .equals(aaaa)){
                    a.setItem(COSName.BASE_FONT, COSName.getPDFName("SimSun"));
                    b.setItem(COSName.BASE_FONT, COSName.getPDFName("SimSun"));
                    c.setItem(COSName.FONT_NAME, COSName.getPDFName("SimSun"));
                }


//                cosObject.setObject(a);
//                dic1.setItem(COSName.getPDFName("F" + i), cosObject);
            }
            dic.setItem(COSName.FONT, dic1);
            diction.setItem(COSName.RESOURCES, dic);
            page = new PDPage(diction);
            doc = new PDDocument();
            doc.addPage(page);
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setJobName(new File(pdfFile).getName());

            PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            printJob.setPrintService(defaultService);
            Pageable pageable = new PDFPageable(doc);

            printJob.setPageable(pageable);
            logger.info("begin to print···");
            printJob.print();
            logger.info("print end···");
            result = Contains.SUCCESS;
        } catch (Exception e){
            e.printStackTrace();
            logger.error("文件打出错：{}", e);
            logger.error("文件打印出错，直接返回！");
            result = Contains.SUCCESS;
        } finally {
            try {
                if (document != null) {
                    document.close();
                }
                if (doc != null) {
                    doc.close();
                }
                if (inputStream != null){
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("关闭流出错:{}",e);
                e.printStackTrace();
            }
        }
        return result;
    }

    private static COSDictionary getCOSDictionary(COSBase cosBase){
        COSDictionary dictionary = null;
        if (cosBase instanceof COSObject){
            COSObject cosObject = (COSObject) cosBase;
            dictionary = (COSDictionary) cosObject.getObject();
        }else if (cosBase instanceof COSDictionary){
            dictionary = (COSDictionary) cosBase;
        }
        return dictionary;
    }

    public static void main(String[] args) {
        print("http://hqyp.fenith.com/temp_pdf/5c89aadc03790314.pdf");
    }
}
