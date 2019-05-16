package com.gmail.raducaz.arduinomate.commands;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class DeviceTapFunctions {
    ArduinoCommander arduinoCommander;
    DataRepository dataRepository;
    DeviceEntity deviceEntity;

    public DeviceTapFunctions(DataRepository dataRepository, String deviceName) {
        this.deviceEntity = dataRepository.loadDeviceByNameSync(deviceName);
        arduinoCommander = new ArduinoCommander(deviceEntity.getIp(), deviceEntity.getPort());
    }

    // tap open sequence - should be simple :)
    public boolean tapOPEN() {
        String TAG = "tapOpen";

        // Open selected pin -- cannot send the wait command now, because tap will open in 45s
        String command = "[{\"=2\":0}]";
        String result = arduinoCommander.SendCommand(command);

        if(isContactOn()) {
            try {
                TimeUnit.SECONDS.sleep(50);

                return tapOpenState() == FunctionResultStateEnum.ON;

            } catch (Exception exc) {
                return false;
            }
        }

        return false;
    }

    // tap open sequence - should be simple :)
    public boolean tapCLOSE() {
        String TAG = "tapClose";

        if(tapOpenState() == FunctionResultStateEnum.OFF)
            return true;
        else {
            // Open selected pin -- cannot send the wait command now, because tap will open in 45s
            String command = "[{\"=2\":1}]";
            String result = arduinoCommander.SendCommand(command);

            if (!isContactOn()) {
                try {
                    TimeUnit.SECONDS.sleep(10);

                    return tapOpenState() == FunctionResultStateEnum.OFF;

                } catch (Exception exc) {
                    return false;
                }
            }

            return false;
        }
    }

    public boolean isContactOn()
    {
        try{
            return !isPinOn("2");
        }
        catch (Exception exc)
        {
            return  false;
        }
    }
    // Returns true if pin = 1
    private boolean isPinOn(String pin) throws Exception {
        String TAG = "isPinOn";

        String command = "[{\"?" + pin + "\":0}]";
        String result = arduinoCommander.SendCommand(command);

        // Read command results
        if (new Parser(result).getInt("?" + pin) == 0)
            return false;
        else
            return true;
    }

    public FunctionResultStateEnum tapOpenState()
    {
        String TAG = "tapOpenState";

            // Send probe and see if contact is ON => tap is OPEN
            String command = "[{\"=7\":1},{\"!\":500},{\"?6\":0},{\"=7\":0}]"; // 6 is receiver
            String result = arduinoCommander.SendCommand(command);

            try {
                if (new Parser(result).getInt("?6") == 0)
                    return FunctionResultStateEnum.ON;
                else
                    return FunctionResultStateEnum.OFF;
            }
            catch (Exception exc){
                return FunctionResultStateEnum.ERROR;
            }

    }


}