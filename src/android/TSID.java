package com.teamsystem.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;


public class TSID extends CordovaPlugin {

    public static String md5(String input) {
    try {
      MessageDigest md =  MessageDigest.getInstance("MD5");

      StringBuilder hexString = new StringBuilder();
      for (byte digestByte : md.digest(input.getBytes()))
        hexString.append(String.format("%02X", digestByte));

      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
  }
  public String TSID_API_REQ(final HashMap<String,String> params) throws IOException {
    String risultato = "KO";
    String AppCode = "App code with escape key (\ -> \\)]";
    String Email = params.get("EMAIL");
    String Password = params.get("PWD");
    long time= System.currentTimeMillis();
    String ReqDate = Long.toString(time);
    String AppPwd ="[App password as string]";
    String MAC = AppCode + Email + Password + ReqDate + AppPwd;
    String MAC_MD5 = md5(MAC);

    JSONObject jsonObj = new JSONObject();
    try {
      jsonObj.put("AppCode",AppCode);
      jsonObj.put("Email",Email);
      jsonObj.put("Password",Password);
      jsonObj.put("RequestDate",ReqDate);
      jsonObj.put("MAC",MAC_MD5);

    }catch (JSONException e) {

    }

    String json = jsonObj.toString();
    TSIDCrypt tsidcrypt = new TSIDCrypt();
    // Encrypting our dummy String
    String encryptedStr="KO";
    try {
      encryptedStr = TSIDCrypt.byteArrayToB64String(tsidcrypt.encrypt(json));
    } catch (Exception e) {

    }

    return risultato = encryptedStr;
  }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("encrypt")){
            String email = data.getString(0);
            String password = data.getString(1);
            HashMap<String, String> params=new HashMap<String, String>();
            params.put("EMAIL",email);
            params.put("PWD",password);

            String result = "KO";
            String err = "";
            try {
              result = TSID_API_REQ(params);
            } catch (IOException e) {
              err = e.getMessage();
            }

            if (result.equals("KO"))
                callbackContext.error("Unable to encrypt.\n"+ err);
            else
                callbackContext.success(result);

            return true;

        } else if (action.equals("decrypt")) {
            String encryptedString = data.getString(0);
            String result = "KO";
            TSIDCrypt tsidcrypt = new TSIDCrypt();
            String err ="";
            try {
                result = new String(tsidcrypt.decrypt(encryptedString), "UTF-8"); // for UTF-8 encoding 
            } catch (Exception e) {
                err = e.getMessage();
            }

            if (result.equals("KO"))
                callbackContext.error("Unable to decrypt.\n"+err);
            else
                callbackContext.success(result);

            return true;

        } else {
            
            return false;

        }
    }
}
