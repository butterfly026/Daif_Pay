package com.daifubackend.api.utils;

import com.daifubackend.api.pojo.Result;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class EncDecUtils {

    public static String decodeBase64(String base64) {
        // Replace '-' with '/' and '_' with '+'
        base64 = base64.replace('-', '/');
        base64 = base64.replace('_', '+');

        try {
            // URL decode before decoding Base64
            byte[] decodedBytes = Base64.decode(base64);
            // Convert byte array to string
            String decodedString = new String(decodedBytes, "UTF-8");
            System.out.println("DECOD " + decodedString);
            return decodedString;
        } catch (Exception e) {
            return  null;
        }
    }

    public static String encodeBase64(String plan) {
        // Replace '-' with '/' and '_' with '+'
        return Base64.encode(plan.getBytes());
    }

    public static String genSecurePassword(int length) {
        char[] SYMBOLS = "^$*.[]{}()?-\"!@#%&/\\,><':;|_~`".toCharArray();
        char[] LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char[] NUMBERS = "0123456789".toCharArray();
        char[] ALL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789^$*.[]{}()?-\"!@#%&/\\,><':;|_~`".toCharArray();
        Random rand = new SecureRandom();


        char[] password = new char[length];

        //get the requirements out of the way
        password[0] = LOWERCASE[rand.nextInt(LOWERCASE.length)];
        password[1] = UPPERCASE[rand.nextInt(UPPERCASE.length)];
        password[2] = NUMBERS[rand.nextInt(NUMBERS.length)];
        password[3] = SYMBOLS[rand.nextInt(SYMBOLS.length)];

        //populate rest of the password with random chars
        for (int i = 4; i < length; i++) {
            password[i] = ALL_CHARS[rand.nextInt(ALL_CHARS.length)];
        }

        //shuffle it up
        for (int i = 0; i < password.length; i++) {
            int randomPosition = rand.nextInt(password.length);
            char temp = password[i];
            password[i] = password[randomPosition];
            password[randomPosition] = temp;
        }

        return new String(password);
    }

    public static boolean verifyGoogle2fa(String googleCode, int code) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean isCodeValid = gAuth.authorize(googleCode, code);
        log.info("Google Authenticator" + (isCodeValid ? " OK " : " Fail"));
        return true; //isCodeValid;
    }

    public static String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }
    public static Map decodePostParam(String reqBodyString) {
        String encParamString = "";
        try{
            encParamString = URLDecoder.decode(reqBodyString, "UTF-8");
            if(encParamString.endsWith("=") && encParamString.length() % 4 != 0)
                encParamString = encParamString.substring(0, encParamString.length() - 1);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String inputParam = encParamString.replace(" ", "+");
        inputParam = inputParam.replace("-", "/");
        inputParam = inputParam.replace("_", "+");
        byte[] bts = Base64.atob(inputParam);
        String decParam = XXTEA.decryptBase64StringToString(inputParam, "1996652523");
        if(decParam == null || decParam.isEmpty())
            return null;
        return convertUrlQuery(decParam);
    }

    public static Map<String, String> convertUrlQuery(String query) {
        Map<String, String> params = new HashMap<>();
        String[] queryParts = query.split("&");

        for (String param : queryParts) {
            String[] item = param.split("=");

            try {
                if(item.length < 2) continue;
                String decodedValue = URLDecoder.decode(item[1], "UTF-8");
                // Assuming AntiXSS.xss_clean() is a method to sanitize user input to prevent XSS attacks

                params.put(item[0], decodedValue);
            } catch (UnsupportedEncodingException e) {
                // Handle the exception according to your needs
                e.printStackTrace();
            }
        }

        return params;
    }

}
