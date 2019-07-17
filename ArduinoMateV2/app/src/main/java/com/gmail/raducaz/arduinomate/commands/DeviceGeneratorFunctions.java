package com.gmail.raducaz.arduinomate.commands;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceGeneratorFunctions {
    ArduinoCommander arduinoCommander;
    DataRepository dataRepository;
    DeviceEntity deviceEntity;

    public DeviceGeneratorFunctions(DataRepository dataRepository, String deviceName) {
        this.deviceEntity = dataRepository.loadDeviceByNameSync(deviceName);
        arduinoCommander = new ArduinoCommander(deviceEntity.getIp(), deviceEntity.getPort());
    }

    //TODO: This needs to be rewritten to get the value from the pin state history - the custom current value will not be the actual value of the pin
    public boolean isCurrentAbove(double threshold)
    {
        try {
            // Check AC current - if high then it's on
            String command = "[F1]";
            String result = arduinoCommander.SendCommand(command);
            if (new Parser(result).getDouble("F1") >= threshold)
                return true;
            else
                return false;
        }
        catch (Exception exc)
        {
            Log.e("isCurrentAbove", exc.getMessage());
            return false;
        }
    }
    public double getCurrentTemperature()
    {
        try {
            // Check AC current - if high then it's on
            String command = "[F2]";
            String result = arduinoCommander.SendCommand(command);
            double temp = 0;
            temp = new Parser(result).getDouble("F2");

            return temp;
        }
        catch (Exception exc)
        {
            Log.e("isCurrentAbove", exc.getMessage());
            return -50;
        }
    }

    // Returns true if probe proves the pressure sensor is activated
    public boolean testLongRun() {
        String TAG = "testLongRun";

        String command = "[=6:0:500|!1000|=2:0|!1000|=8:1:2000|=7:0:500]";
        String result = arduinoCommander.SendCommand(command);

        try {
            // Read command results
            if (new Parser(result).getInt("#A4") == 0)
                return true;
            else
                return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    // Returns true if probe proves the pressure sensor is activated
    public boolean isPressureLow() throws Exception {

        // Generate command - set A3 to 0 to see if A4 becomes 0 too
        // If becomes 0 => A3 is connected to A4 => pressure sensor is activated => pressure low
        String command = "[~A3:0|#A4|~A3:1]";
        String result = arduinoCommander.SendCommand(command);

        // Read command results
        if (new Parser(result).getInt("#A4") == 0)
            return true;
        else
            return false;

    }

    // Set Soc On sequence
    public boolean socON() {
        String TAG = "socON";

        // Soc ON
        String command = "[=6:0:500]";
        String result = arduinoCommander.SendCommand(command);

        return true;
    }
    // Set Soc On sequence
    public boolean socOFF() {
        String TAG = "socOFF";

        // Soc OFF
        String command = "[=7:0:500]";
        String result = arduinoCommander.SendCommand(command);

        return true;
    }

    // Set Soc On sequence
    public boolean contactON() {
        String TAG = "contactON";

        // Soc ON
        String command = "[=2:0]";
        String result = arduinoCommander.SendCommand(command);

        return true;
    }
    // Set Soc On sequence
    public boolean contactOFF() {
        String TAG = "contactOFF";

        // Soc ON
        String command = "[=2:1]";
        String result = arduinoCommander.SendCommand(command);

        return true;
    }
    // Set Soc On sequence
    public boolean ignition() {
        String TAG = "ignitionON";

        // ignition ON
        String command = "[=8:1:3500]";
        String result = arduinoCommander.SendCommand(command);

        return true;
    }

    // Start Generator sequence
    public boolean generatorON() {
        String TAG = "generatorON";

        // Soc ON , Wait 1s, Contact ON, Demarare ON for 2s, Soc OFF for .5s,
        String command = "[=6:0:500|!1000|=2:0|!1000|=8:1:2000|=7:0:500]";
        String result = arduinoCommander.SendCommand(command);

        return true;

    }

        // Stop Generator sequence
        public boolean generatorOFF()
        {
            String TAG = "generatorOFF";

            // Priza OFF, Wait 2s, Contact OFF
            String command = "[=3:1|!2000|=2:1]";
            arduinoCommander.SendCommand(command);

            // Check AC current - if low then it stopped
            return true; // TODO: Use when implemented correctly !isCurrentAbove(0.18);

    }

    public FunctionResultStateEnum getGeneratorState()
    {
        String TAG = "generatorState";

        // query State - weak validation but base on contact only
        // TODO: find a stronger method to determine if generator is OFF
        String command = "[?2]";
        String result = arduinoCommander.SendCommand(command);

        try {
            // Read command results
            if (new Parser(result).getInt("?2") == 0)
                return FunctionResultStateEnum.ON;
            else
                return FunctionResultStateEnum.OFF;
        }
        catch (Exception e) {
            return FunctionResultStateEnum.ERROR;
        }
    }

    // AC ON
    public boolean powerON()
    {
        String TAG = "powerON";

            // Priza ON
            String command = "[=3:0]";
            arduinoCommander.SendCommand(command);

            return true; //TODO: Use when is correct isCurrentAbove(0.18);

    }

    // AC OFF
    public boolean powerOFF()
    {
        String TAG = "powerON";

            // Priza OFF
            String command = "[=3:1]";
            arduinoCommander.SendCommand(command);

            // Check AC current - if low then it's off
            return !isCurrentAbove(0.18);

    }

    public FunctionResultStateEnum getPowerState()
    {
        String TAG = "powerState";

        // query Power State
        String command = "[?3]";
        String result = arduinoCommander.SendCommand(command);

        try {
            // Read command results
            if (new Parser(result).getInt("?3") == 0)
                return FunctionResultStateEnum.ON;
            else
                return FunctionResultStateEnum.OFF;
        }
        catch (Exception e) {
            return FunctionResultStateEnum.ERROR;
        }
    }
}
