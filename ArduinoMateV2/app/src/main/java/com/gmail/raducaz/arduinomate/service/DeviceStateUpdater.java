package com.gmail.raducaz.arduinomate.service;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.events.DeviceStateChangeEvent;
import com.gmail.raducaz.arduinomate.events.PinStateChangeEvent;
import com.gmail.raducaz.arduinomate.events.PinStateChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceStateUpdater {
    DataRepository dataRepository;
    public DeviceStateInfo deviceStateInfo;

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
        String name = deviceStateInfo.getDeviceName();
        if (name == null)
            return null;

        return dataRepository.loadDeviceByNameSync(name);
    }
    public DeviceEntity getDeviceEntity(long id) {

        return dataRepository.loadDeviceSync(id);
    }

    public Map<String, PinStateEntity> getCurrentPinStates() {
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
            newPinState.setLastUpdate(DateConverter.toDate(System.currentTimeMillis()));
            newPinState.setState(pState);
            dataRepository.insertPinState(newPinState);
        }
    }
    private ArrayList<PinStateChangeListener> pinStateChangeListenerList = new ArrayList<PinStateChangeListener>();
    // Register an event listener
    public synchronized void addPinStateListener(PinStateChangeListener listener) {
        if (!pinStateChangeListenerList.contains(listener)) {
            pinStateChangeListenerList.add(listener);
        }
    }
    private void processPinStateChangeEvent(PinStateChangeEvent pinStateChangeEvent) {
        ArrayList<PinStateChangeListener> tempPinStateChangeListenerList;

        synchronized (this) {
            if (pinStateChangeListenerList.size() == 0) {
                return;
            }
            tempPinStateChangeListenerList = (ArrayList<PinStateChangeListener>) pinStateChangeListenerList.clone();
        }

        for (PinStateChangeListener listener : tempPinStateChangeListenerList) {
            listener.pinStateChanged(pinStateChangeEvent);
        }
    }
    public void updatePinStates() throws Exception
    {
        if(deviceEntity != null) {

            Map<String, Double> pinStates = deviceStateInfo.getPinStates();
            Map<String, PinStateEntity> currentPinsState = getCurrentPinStates();
            Map<String, Double> newPinStates = new HashMap<>();
            Map<String, Double> oldPinStates = new HashMap<>();
            //        currentPinsState.stream().filter(p->p.getName().equals("p1")).findAny(); - unsupported by API22

            for (String pName : pinStates.keySet()) {
                Double pState = pinStates.get(pName).doubleValue();
                if (currentPinsState.containsKey(pName) && currentPinsState.get(pName).getState() == pState) {
                    // Do nothing as the state is the same as the current pin State in the History
                    dataRepository.updatePinStateLastUpdate(currentPinsState.get(pName).getId());
                } else {
                    // Prepare the Device pin state change event
                    newPinStates.put(pName, pState);
                    if(currentPinsState.containsKey(pName))
                        oldPinStates.put(pName, currentPinsState.get(pName).getState());

                    // Trigger here the custom event PinChanged
//                    processPinStateChangeEvent(new PinStateChangeEvent(this, pName, pState));

//                    //TODO: Add custom listener
//                    PinStateChangeListener listener = new MyPinStateChangeListener();
//                    this.addPinStateListener(listener);

                    if (currentPinsState.containsKey(pName) && currentPinsState.get(pName).getState() != pState) {
                        // Update history with the date until the state was unchanged
                        dataRepository.updatePinStateToDate(currentPinsState.get(pName).getId());
                    }

                    insertPinStateHistory(pName,pState);
                }
            }

            //TODO: Delete from history what has toDate < yesterday

            // Trigger the change event containing all the changes
            //TODO: Test if need to start this on a separate thread to avoid blocking the Server thread -
            // should not be the case as this is executed on a Handler
            if(newPinStates.size()>0) { // There is at least one change
                DeviceStateChangeEvent deviceStateChangeEvent = new DeviceStateChangeEvent(dataRepository, deviceEntity);
                deviceStateChangeEvent.trigger(oldPinStates, newPinStates);
            }
        }
    }

    //TODO: this can be anywhere along with the add listener code
//    DeviceStateUpdater deviceStateUpdater = new DeviceStateUpdater();
//    PinStateChangeListener listener = new MyPinStateChangeListener();
//    deviceStateUpdater.addSpeedListener(listener);
    private static class MyPinStateChangeListener implements PinStateChangeListener {

        @Override
        public void pinStateChanged(PinStateChangeEvent e) {
            // TODO: Call here logic if pin state changes

        }
    }
}
