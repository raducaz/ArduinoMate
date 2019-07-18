package com.gmail.raducaz.arduinomate.commands;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class Parser {

    String result;
    public Parser(String result) throws Exception
    {
        try {
            this.result = result;

            if(!result.endsWith("]"))
                throw new Exception("Result is incomplete");
        }
        catch (Exception exc)
        {
            Log.e("Parser", exc.getMessage());
            throw exc;
        }
    }
    public String getString(String key)
    {
        // [F2:-100|F1:1|?2:0|#2:0|#A4:1019]
        // key = #2, F1 etc.
        int keyLen = key.length();
        int start = result.indexOf(key);
        if(start == -1)
            return "";

        if(start+keyLen<result.length())
        {
            String partRes = result.substring(start+keyLen);
            partRes = partRes.replace(":","");
            int end = partRes.indexOf("|");
            if(end == -1) end = partRes.indexOf("]");

            if(end>=0)
                return partRes.substring(0, end);
            else
                return "";
        }

        return "";

    }
    public int getInt(String key)
    {
        String s = getString(key);
        try
        {
            return Integer.parseInt(s);
        }
        catch (Exception exc) {
            Log.e("Parser", exc.getMessage());
            return -1;
        }
    }
    public double getDouble(String key)
    {
        String s = getString(key);
        try
        {
            return Double.parseDouble(s);
        }
        catch (Exception exc) {
            Log.e("Parser", exc.getMessage());
            return -1;
        }
    }

//    JSONArray jsonArray;
//    public Parser(String result) throws Exception
//    {
//        try {
//            jsonArray = new JSONArray(result);
//        }
//        catch (Exception exc)
//        {
//            Log.e("Parser", exc.getMessage());
//            throw exc;
//        }
//    }
//    public int getIntFromJSON(String key)
//    {
//        try {
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject obj = jsonArray.getJSONObject(i);
//                if (obj.has(key))
//                    return obj.getInt(key);
//            }
//            return -1;
//        }
//        catch (Exception exc) {
//            Log.e("Parser", exc.getMessage());
//            return -1;
//        }
//
//    }
//    public double getDoubleFromJSON(String key)
//    {
//        try {
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject obj = jsonArray.getJSONObject(i);
//                if (obj.has(key))
//                    return obj.getDouble(key);
//            }
//            return -1;
//        }
//        catch (Exception exc) {
//            Log.e("Parser", exc.getMessage());
//            return -1;
//        }
//
//    }
}
