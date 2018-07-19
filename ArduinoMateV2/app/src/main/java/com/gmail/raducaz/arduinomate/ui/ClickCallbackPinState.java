package com.gmail.raducaz.arduinomate.ui;

import android.support.v7.widget.CardView;
import android.view.View;

import com.gmail.raducaz.arduinomate.model.PinState;

public interface ClickCallbackPinState {
    void onClick(View v, PinState pinState);
}
