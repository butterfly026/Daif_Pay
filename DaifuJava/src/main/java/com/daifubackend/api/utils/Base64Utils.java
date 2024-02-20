package com.daifubackend.api.utils;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;

public class Base64Utils {

    public static String imageBase64(String imgUrl) throws IOException {
        URL url = null;
        InputStream is = null;
        ByteArrayOutputStream outStream = null;
        HttpURLConnection httpUrl = null;
        try {
            url = new URL(imgUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            httpUrl.getInputStream();
            is = httpUrl.getInputStream();
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            return Base64Utils.encodeImage(outStream.toByteArray());
        } catch (Exception e) {
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw e;
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    throw e;
                }
            }
            if (httpUrl != null) {
                httpUrl.disconnect();
            }
        }
    }

    /**
     * 本地图片转换Base64的方法
     *
     * @param imgPath
     * @return
     */
    public static String localImageToBase64(String imgPath) {
        InputStream in = null;
        byte[] data = null;
         // 读取图片字节数组
        try {
            in = new FileInputStream(imgPath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(data);
    }


    /**
     * 图片转字符串
     *
     * @param image
     * @return
     */
    public static String encodeImage(byte[] image) {
        Base64.Encoder encoder = Base64.getEncoder();
        return replaceEnter(encoder.encodeToString(image));
    }

    public static String replaceEnter(String str) {
        String reg = "[\n-\r]";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }
}
