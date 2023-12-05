package com.gmail.raducaz.arduinomate.commands;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
public class DeviceDvrFunctions {
    ArduinoCommander arduinoCommander;
    DataRepository dataRepository;
    DeviceEntity deviceEntity;

    public DeviceDvrFunctions(DataRepository dataRepository, String deviceName) {
        this.deviceEntity = dataRepository.loadDeviceByNameSync(deviceName);
    }

    // send the magic package
    public boolean wakeup() throws Exception {
        String TAG = "wakeup";

        int port = 7; // Change if needed
        // do not use the ip as after a period of time the ip is deleted from router ipTable, use subnet-local broadcast address like 192.168.1.255
        String broadcastIP = deviceEntity.getIp();
        String mac = deviceEntity.getMac();

        if (mac == null) {
            throw new Exception("Mac error at wakeup:mac = null");
        }

        byte[] macBytes = getMacBytes(mac);
        byte[] bytes = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        // Multicasting - https://docs.oracle.com/javase/tutorial/networking/datagrams/broadcasting.html
        // https://stackoverflow.com/questions/2950715/udp-broadcast-in-java
        InetAddress address = InetAddress.getByName("192.168.1.255");// this is a ipGroup
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.send(packet);
        socket.close();

        return true;

    }

    byte[] getMacBytes(String mac) throws IllegalArgumentException {
        mac = mac.replace(":", "");
        byte[] bytes = new byte[6];
        try {
            String hex;
            for (int i = 0; i < 6; i++) {
                hex = mac.substring(i*2, i*2+2);
                bytes[i] = (byte) Integer.parseInt(hex, 16);

            }
        }
        catch (NumberFormatException e) {
            Log.e("GetMacBytes","error");
        }
        return bytes;
    }

}
