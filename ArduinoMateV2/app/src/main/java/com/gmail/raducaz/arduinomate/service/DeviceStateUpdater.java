package com.gmail.raducaz.arduinomate.service;

import android.os.OperationCanceledException;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.model.Device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceStateUpdater {
    DataRepository dataRepository;
    DeviceStateInfo deviceStateInfo;

    DeviceEntity deviceEntity;

    public DeviceStateUpdater(DataRepository dataRepository, String msg) {
        this.dataRepository = dataRepository;
        deviceStateInfo = new DeviceStateInfo(msg);

        this.deviceEntity = getDeviceEntity();
    }
    public DeviceStateUpdater(DataRepository dataRepository, String msg, long deviceId) {
        this.dataRepository = dataRepository;
        deviceStateInfo = new DeviceStateInfo(msg);

        this.deviceEntity = getDeviceEntity(deviceId);
    }

    public DeviceStateUpdater(DataRepository dataRepository, DeviceStateInfo deviceStateInfo) {
        this.dataRepository = dataRepository;
        this.deviceStateInfo = deviceStateInfo;

        this.deviceEntity = getDeviceEntity();
    }

    public DeviceEntity getDeviceEntity() {
        String ip = deviceStateInfo.getDeviceIp();
        if (ip == null)
            return null;

        return dataRepository.loadDeviceSync(ip);
    }
    public DeviceEntity getDeviceEntity(long id) {

        return dataRepository.loadDeviceSync(id);
    }

    public Map<String, PinStateEntity> getCurrentPinStates() throws Exception {
        Map<String, PinStateEntity> currentPinsState = new HashMap<String, PinStateEntity>();

        if (deviceEntity != null) {
            List<PinStateEntity> list = dataRepository.loadDeviceCurrentPinsStateSync(deviceEntity.getId());
            for (PinStateEntity p : list) {
                currentPinsState.put(p.getName(), p);
            }
        }
        return currentPinsState;
    }

    public void insertPinStateHistory(String pName, Double pState)
    {
        if(deviceEntity != null) {
            // Insert a new History for this pin with the initial state
            PinStateEntity newPinState = new PinStateEntity();
            newPinState.setDeviceId(deviceEntity.getId());
            newPinState.setName(pName);
            newPinState.setFromDate(DateConverter.toDate(System.currentTimeMillis()));
            newPinState.setState(pState);
            dataRepository.insertPinState(newPinState);
        }
    }
    public void updatePinStates() throws Exception
    {
        if(deviceEntity != null) {

            Map<String, Double> pinStates = deviceStateInfo.getPinStates();
            Map<String, PinStateEntity> currentPinsState = getCurrentPinStates();
            //        currentPinsState.stream().filter(p->p.getName().equals("p1")).findAny(); - unsupported by API22

            for (String pName : pinStates.keySet()) {
                Double pState = pinStates.get(pName).doubleValue();
                if (currentPinsState.containsKey(pName) && currentPinsState.get(pName).getState() == pState) {
                    // Do nothing as the state is the same as the current pin State in the History
                } else {
                    if (currentPinsState.containsKey(pName) && currentPinsState.get(pName).getState() != pState) {
                        // Update history with the date until the state was unchanged
                        dataRepository.updatePinStateToDate(currentPinsState.get(pName).getId());
                    }
                    insertPinStateHistory(pName,pState);
                }
            }
        }
    }

}
