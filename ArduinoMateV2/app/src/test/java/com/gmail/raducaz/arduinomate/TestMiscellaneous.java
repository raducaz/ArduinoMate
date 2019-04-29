package com.gmail.raducaz.arduinomate;

import com.gmail.raducaz.arduinomate.telnet.TelnetClient;

import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestMiscellaneous {
    @Test
    public void addition_isCorrect() {
        //assertEquals(4, 2 + 2);

        byte repl_3[] = {(byte) 0xff, (byte) 0xfc, 0x01};
        System.out.println("Result =" + repl_3);

    }
}