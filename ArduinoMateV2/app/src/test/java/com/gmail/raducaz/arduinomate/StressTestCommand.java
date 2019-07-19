package com.gmail.raducaz.arduinomate;

import android.util.Log;

import com.gmail.raducaz.arduinomate.commands.ArduinoCommander;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class StressTestCommand {
    @Test
    public void TestSendMessage() {

        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });

        String command = "[=2:0]";
        ArduinoCommander arduinoCommander = new ArduinoCommander("192.168.100.100", 8080);
        String result = "";

        int i = 0;
        while(i<20)
        {
            if(i%2==0)
            {
                command = "[=2:0]";
            }
            else
            {
                command = "[=2:1]";
            }
            result = arduinoCommander.SendCommand(command);
            Logger.d(result);
            Log.d("", "aaa");

            i++;
        }
    }
}
