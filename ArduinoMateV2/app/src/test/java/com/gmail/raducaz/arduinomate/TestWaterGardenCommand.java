package com.gmail.raducaz.arduinomate;

import android.app.Application;
import android.util.Log;

import com.gmail.raducaz.arduinomate.commands.ArduinoCommander;
import com.gmail.raducaz.arduinomate.commands.DeviceTapFunctions;
import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.processes.ProcessGardenWaterOnOff;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestWaterGardenCommand extends Application {
    @Test
    public void TestSendMessage() {

        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });

//        AppDatabase db = AppDatabase.getInstance(this, new AppExecutors());
//        DataRepository dataRepository = DataRepository.getInstance(db);
//
//        ProcessGardenWaterOnOff p = new ProcessGardenWaterOnOff(dataRepository, "Tap");
//        p.execute(false, true, FunctionResultStateEnum.ON, "Test");

        int i=0;
        while(i<100) {
            ArduinoCommander arduino = new ArduinoCommander("192.168.100.100", 8080);

            //tap open
            String command = "[=7:1]";
            String result = arduino.SendCommand(command);

            // get gen state
            command = "[?2]";
            result = arduino.SendCommand(command);

            // current value
            command = "[F1]";
            result = arduino.SendCommand(command);

            // start
            command = "[=6:0:500|!1000|=2:0|!1000|=8:1:2000|=7:0:500]";
            result = arduino.SendCommand(command);

            // start pump
            command = "[=3:0]";
            result = arduino.SendCommand(command);

            i++;
        }

    }
}
