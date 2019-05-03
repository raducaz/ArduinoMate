package com.gmail.raducaz.arduinomate.commands;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class Parser {

    JSONArray jsonArray;
    public Parser(String result)
    {
        try {
            jsonArray = new JSONArray(result);
        }
        catch (Exception exc)
        {
            Log.e("Parser", exc.getMessage());
        }
    }
    public int getInt(String key)
    {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.has(key))
                    return obj.getInt(key);
            }
            return -1;
        }
        catch (Exception exc) {
            Log.e("Parser", exc.getMessage());
            return -1;
        }

    }
    public double getDouble(String key)
    {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.has(key))
                    return obj.getDouble(key);
            }
            return -1;
        }
        catch (Exception exc) {
            Log.e("Parser", exc.getMessage());
            return -1;
        }

    }
}
