package com.gmail.raducaz.arduinomate.service;

import android.os.OperationCanceledException;

import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeviceStateInfo {
    JSONObject _messageData = null;
    String _messageRaw = null;

    public DeviceStateInfo(String msg)
    {
        try {
            if(msg!=null) {
            /* samples
            {"ip":"192.168.1.100","state":0,"pinStates":[1,1,1,1,0,0,1,1,1,1,1,1,1,0]}
            {"pinState":[1,0.234],"msg":"Contact off"}
            {"fctState":1,"msg":"Generator ON"}
             */
                _messageData = new JSONObject(msg);
            }
        }
        catch (Exception e)
        {
            // store the message as raw string
            _messageRaw = msg;
        }
    }

    public String getDeviceName()
    {
        try {
            if (_messageData != null && _messageData.has("name")) {
                return _messageData.getString("name");
            } else
                return null;
        }
        catch (Exception exc){
            return null;
        }
    }
    public DeviceStateEnum getDeviceState()
    {
        try {
            if (_messageData != null && _messageData.has("state")) {
                return DeviceStateEnum.forInt(_messageData.getInt("state"));
            } else
                return null;
        }
        catch (Exception e){return null;}
    }
    public FunctionResultStateEnum getFunctionState()
    {
        try {
            if (_messageData != null && _messageData.has("fctState")) {
                return FunctionResultStateEnum.forInt(_messageData.getInt("fctState"));
            } else
                return FunctionResultStateEnum.NA;
        }
        catch (Exception e){return FunctionResultStateEnum.NA;}
    }
    public String getFunctionName()
    {
        try {
            if (_messageData != null && _messageData.has("fctName")) {
                return _messageData.getString("fctName");
            } else
                return null;
        }
        catch (Exception e){return null;}
    }
    public String getMessage()
    {
        try {
            if (_messageData != null && _messageData.has("msg")) {
                return _messageData.getString("msg");
            } else if (_messageData == null) {
                // If this is not structured most probably this is the message itself
                return _messageRaw;
            } else {
                return null;
            }
        }
        catch (Exception e){return null;}
    }

    public Map<String, Double> getPinStates()
    {
        try {
            if (_messageData != null) {
                if (_messageData.has("digitalPins")) {
                    return convertJSONArrayListToMap("digitalPins", "");
                } else if (_messageData.has("analogPins")) {
                    return convertJSONArrayListToMap("analogPins", "A");
                } else if (_messageData.has("pin")) {
                    return convertJSONPinStatePairToMap();
                }
            }
        }
        catch (Exception e){}

        return new HashMap<String, Double>();
    }

    private Map<String, Double> convertJSONArrayListToMap(String arrayName, String keyPrefix) {
        Map<String, Double> map = new HashMap<String, Double>();

        try {
            if (_messageData != null && _messageData.has(arrayName)) {
                JSONArray jsonArray = _messageData.getJSONArray(arrayName);

                for (int i = 0; i < jsonArray.length(); i++) {
                    Double value = jsonArray.getDouble(i);
                    map.put(keyPrefix+String.valueOf(i), value);
                }
            }
        }
        catch (Exception e){return null;}

        return map;
    }
    private Map<String, Double> convertJSONPinStatePairToMap() {
        Map<String, Double> map = new HashMap<String, Double>();

        try {
            if (_messageData != null && _messageData.has("pin") && _messageData.has("value")) {
                    String name = String.valueOf(_messageData.getInt("pin"));
                    Double value = _messageData.getDouble("value");
                    map.put(name, value);
            }
        }
        catch (Exception e){return null;}

        return map;
    }
}
