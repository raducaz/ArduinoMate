package com.gmail.raducaz.arduinomate.events;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateChangeEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.model.PinState;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.ui.TaskExecutor;
import com.orhanobut.logger.Logger;

import java.util.Date;
import java.util.List;

public class PinStateChangeEvent {

    final String TAG = "PinStateChangeEvent";
    Context context;
    long lastTrigger = 0;

    public PinStateChangeEvent(Context context)
    {
        this.context = context;
    }

    public void makeCall(String phoneNumber)
    {
        long now = System.currentTimeMillis();

        if((now - lastTrigger) > 120000) // longer than 2 minutes
        {
            lastTrigger = now;

            // Start call here
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.setData(Uri.parse("tel:"+phoneNumber));
            context.startActivity(callIntent);
        }

    }
    public void trigger(List<PinStateChangeEntity> changedPinStates)
    {
        for (PinStateChangeEntity changedPin : changedPinStates) {
            try {
                switch (changedPin.getDeviceName()) {
                    case "Generator":
                        // Add logic for specific pin change
                        break;
                    case "Tap":
                        // Add logic for specific pin change
                        break;
                }

            } catch (Exception exc) {
                Logger.e(TAG + exc.getMessage());
            }
        }


    }

}
