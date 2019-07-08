package com.gmail.raducaz.arduinomate.mocks;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.MockPinStateEntity;
import com.gmail.raducaz.arduinomate.model.MockPinState;
import com.gmail.raducaz.arduinomate.telnet.TelnetClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockArduinoClient {
    String host;
    int port;
    DataRepository dataRepository;

    public MockArduinoClient(DataRepository dataRepository, String host, int port)
    {
        this.dataRepository = dataRepository;
        this.host = host;
        this.port = port;
    }
    private String SendCommand(String command)
    {
        final TelnetClient telnetClient = new TelnetClient(host, port,
                "test", "test");
        Map<String, Object> map = new HashMap<>();
        telnetClient.executeCommand(command, map);

        Object result = map.get(command);
        if(result!=null)
            return result.toString();
        else
            return "";
    }

    public void SendMockPinStates(String deviceName)
    {
        SendCommand(ConstructDigitalPinStates(deviceName));
        //SendCommand("END");

        SendCommand(ConstructAnalogPinStates(deviceName));
        SendCommand("END");
    }
    private double getPinState(String deviceName, int pinNo)
    {
        MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(deviceName, pinNo);
        double state = pin.getState();
        return state;
    }
    private void setPinState(String deviceName, int pinNo, double state)
    {
        MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(deviceName, pinNo);
        pin.setState(state);
        dataRepository.updateMockPinState(pin);
    }
    private String ConstructDigitalPinStates(String deviceName)
    {
        //return "{\"name\":\""+deviceName+"\",\"state\":0,\"digitalPins\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0]}";

        try {
            JSONObject root = new JSONObject();
            root.put("name", deviceName);
            double state = getPinState(deviceName, 20);
            root.put("state", state);

            if(state==3)
                setPinState(deviceName, 20, 0);

            JSONArray jsonArray = new JSONArray();
            List<MockPinStateEntity> pins = dataRepository.loadMockDevicePinsStateSync(deviceName);

            for (MockPinStateEntity pin : pins)
            {
                if(pin.getNumber() < 14)
                    jsonArray.put(pin.getState());
                else
                    break;
            }

            root.put("digitalPins", jsonArray);

            return root.toString();
        }
        catch (Exception exc)
        {
            return new JSONObject().toString();
        }

//SAMPLE SAMPLE SAMPLE SAMPLE SAMPLE SAMPLE
//        JsonObject& _root = _buffer.createObject();
//        if(strcmp(msg,"") != 0)
//            _root[MSG] = msg;
//        _root[IP] = "192.168.2.200";//ipToString(deviceIp);
//        _root[DEVICESTATE] = deviceState;
//
//        JsonArray& psArr = _root.createNestedArray(pinType==0?DIGITAL:ANALOG);
//        for(byte i=0;i<size;i++)
//        {
//            psArr.add(pinStates[i]);
//        }

    }
    private String ConstructAnalogPinStates(String deviceName) {
        //return "{\"name\":\""+deviceName+"\",\"state\":0,\"analogPins\":[1023,1023,1023,1023,1023,1023]}";

        try {
            JSONObject root = new JSONObject();
            root.put("name", deviceName);
            root.put("state", 0);

            JSONArray jsonArray = new JSONArray();
            List<MockPinStateEntity> pins = dataRepository.loadMockDevicePinsStateSync(deviceName);

            for (MockPinStateEntity pin : pins)
            {
                if(pin.getNumber() < 14)
                    continue;
                else
                    jsonArray.put(pin.getState());
            }

            root.put("analogPins", jsonArray);

            return root.toString();
        }
        catch (Exception exc)
        {
            return new JSONObject().toString();
        }
    }
}
