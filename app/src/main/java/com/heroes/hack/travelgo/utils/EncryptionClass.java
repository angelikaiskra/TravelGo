package com.heroes.hack.travelgo.utils;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


public final class EncryptionClass {
    // Hash algorithm
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("iso-8859-1");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }


    public static boolean validateToken(String token) {

        String decodedToken = getDecodedToken(token);

        try {
            JSONObject decodedTokenJson = new JSONObject(decodedToken);
            long currentDate = new Date().getTime();
            long expirationDate = Long.parseLong(decodedTokenJson.get("exp").toString().concat("000"));

            if (expirationDate > currentDate) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getDecodedToken(String fullToken) {

        if (fullToken == null) {
            return null;
        }

        if (fullToken.length() == 0)
            return null;

        String[] splitedToken = fullToken.split("\\.", 3);
        byte[] byteArray = android.util.Base64.decode(splitedToken[1], Base64.DEFAULT);
        String decodedToken = new String(byteArray);

        return decodedToken;
    }
}