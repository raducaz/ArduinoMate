package com.gmail.raducaz.arduinomate.commands;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;

import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceGeneratorFunctions {
    ArduinoCommander arduinoCommander;
    DataRepository dataRepository;
    DeviceEntity deviceEntity;

    public DeviceGeneratorFunctions(DataRepository dataRepository, String deviceIp) {
        this.deviceEntity = dataRepository.loadDeviceSync(deviceIp);
        arduinoCommander = new ArduinoCommander(deviceEntity.getIp(), deviceEntity.getPort());
    }

    public boolean isCurrentAbove(double threshold)
    {
        // Check AC current - if high then it's on
        String command = "[{\"?A1\":0}]";
        String result = arduinoCommander.SendCommand(command);
        if(new Parser(result).getDouble("?A1")>=threshold)
            return true;
        else
            return false;
    }

    // Returns true if probe proves the pressure sensor is activated
    public boolean isPressureLow()
    {
        String TAG = "isPressureLow";
        try {
            // Generate command - set A3 to 0 to see if A4 becomes 0 too
            // If becomes 0 => A3 is connected to A4 => pressure sensor is activated => pressure low
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(new JSONObject().put("~A3", 0));
            jsonArray.put(new JSONObject().put("#A4", 0));
            jsonArray.put(new JSONObject().put("~A3", 1));

            //String command = "[{\"~A3\":0},{\"#A4\":0},{\"~A3\":1}]";
            String command = jsonArray.toString();
            String result = arduinoCommander.SendCommand(command);

            // Read command results
            if(new Parser(result).getInt("#A4")==0)
                return true;
            else
                return false;
        }
        catch (Exception exc)
        {
            Log.e(TAG,exc.getMessage());
        }
        finally {
            return false;
        }
    }

    // Start Generator sequence
    public boolean generatorON()
    {
        String TAG = "generatorON";
        try {
            // Soc ON for .5s, Wait 1s, Contact ON, Demarare ON for 2s, Soc OFF for .5s,
            String command = "[{\"=6\":0,\"@\":500},{\"!\":1000},{\"=2\":0},{\"!\":1000},{\"=8\":1,\"@\":2000},{\"=7\":0,\"@\":500}]";
            String result = arduinoCommander.SendCommand(command);

            return true;
        }
        catch (Exception exc)
        {
            Log.e(TAG,exc.getMessage());
        }
        finally {
            return false;
        }
    }

    // Stop Generator sequence
    public boolean generatorOFF()
    {
        String TAG = "generatorOFF";
        try {
            // Priza OFF, Wait 2s, Contact OFF
            String command = "[{\"=3\":1},{\"!\":2000},{\"=2\":1}]";
            arduinoCommander.SendCommand(command);

            // Check AC current - if low then it stopped
            return !isCurrentAbove(0.18);
        }
        catch (Exception exc)
        {
            Log.e(TAG,exc.getMessage());
        }
        finally {
            return false;
        }
    }

    // AC ON
    public boolean powerON()
    {
        String TAG = "powerON";
        try {
            // Priza ON
            String command = "[{\"=3\":0}]";
            arduinoCommander.SendCommand(command);

            return isCurrentAbove(0.18);
        }
        catch (Exception exc)
        {
            Log.e(TAG,exc.getMessage());
        }
        finally {
            return false;
        }
    }

    // AC OFF
    public boolean powerOFF()
    {
        String TAG = "powerON";
        try {
            // Priza OFF
            String command = "[{\"=3\":1}]";
            arduinoCommander.SendCommand(command);

            // Check AC current - if low then it's off
            return !isCurrentAbove(0.18);
        }
        catch (Exception exc)
        {
            Log.e(TAG,exc.getMessage());
        }
        finally {
            return false;
        }
    }
}
