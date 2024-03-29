package com.gmail.raducaz.arduinomate.db;

import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.model.Device;
import com.gmail.raducaz.arduinomate.model.MockPinState;
import com.gmail.raducaz.arduinomate.model.Settings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Generates data to pre-populate the database
 */
public class DataGenerator {

    public static SettingsEntity generateSettings() {
        SettingsEntity settings = new SettingsEntity();
        settings.setIsController(true);
        settings.setPermitRemoteControl(false);
        settings.setIsTestingMode(false);
        settings.setAmqUri("amqps://ypwemwny:52DF1s5SaLsW4dBqTQAAonJWNQCQIC3B@cow.rmq2.cloudamqp.com/ypwemwny");

        return settings;
    }

    public static List<DeviceEntity> generateDevices() {

        List<DeviceEntity> devices = new ArrayList<>(3);
        DeviceEntity device = new DeviceEntity();
        device.setIp("192.168.1.100");
        device.setPort(8080);
        device.setName("Generator");
        device.setDescription("Generator, pompa, senzor curent si presiune");
        device.setId(0);
        devices.add(device);

        device = new DeviceEntity();
        device.setIp("192.168.1.101");
        device.setPort(8081);
        device.setName("Tap");
        device.setDescription("Robineti");
        device.setId(1);
        devices.add(device);

        device = new DeviceEntity();
        device.setIp("192.168.1.102");
        device.setPort(8082);
        device.setName("Boiler");
        device.setDescription("Boiler si prize casa");
        device.setId(2);
        devices.add(device);

        device = new DeviceEntity();
        device.setIp("192.168.1.110");
        device.setPort(8083);
        device.setName("SocketX4");
        device.setDescription("4 prize si temp senzor");
        device.setId(3);
        devices.add(device);

        device = new DeviceEntity();
        device.setIp("192.168.1.10");
        device.setMac("00:15:B7:4B:1D:56");
        device.setPort(80);
        device.setName("DVR");
        device.setDescription("DVR");
        device.setId(4);
        devices.add(device);

//        List<ProductEntity> products = new ArrayList<>(FIRST.length * SECOND.length);
//        Random rnd = new Random();
//        for (int i = 0; i < FIRST.length; i++) {
//            for (int j = 0; j < SECOND.length; j++) {
//                ProductEntity product = new ProductEntity();
//                product.setName(FIRST[i] + " " + SECOND[j]);
//                product.setDescription(product.getName() + " " + DESCRIPTION[j]);
//                product.setPrice(rnd.nextInt(240));
//                product.setId(FIRST.length * i + j + 1);
//                products.add(product);
//            }
//        }
        return devices;
    }

    public static List<FunctionEntity> generateFunctionsForDevices(
            final List<DeviceEntity> devices) {

        List<FunctionEntity> functions = new ArrayList<>();

        Device device = devices.get(0);
//        FunctionEntity function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("GeneratorOnOff");
//        function.setDescription("Start/Stop Generator " + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);
//
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("PowerOnOff");
//        function.setDescription("Start/Stop 220V supply" + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);
//
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("PumpOnOff");
//        function.setDescription("Start/Stop generator and 220V supply to " + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);
//
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("SocOnOff");
//        function.setDescription("Used for initial generator start");
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);
//
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("IgnitionOnOff");
//        function.setDescription("Used for initial generator start");
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);

        device = devices.get(1);

//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("WaterSupplyTapOnOff");
//        function.setDescription("WaterSupplyTapOnOff" + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);
//
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("RightIrrigationOnOff");
//        function.setDescription("RightIrrigationOnOff" + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);
//
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("LeftIrrigationOnOff");
//        function.setDescription("LeftIrrigationOnOff" + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);
//
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("HouseWaterOnOff");
//        function.setDescription("HouseWaterOnOff" + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);
//
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("GardenWaterOnOff");
//        function.setDescription("GardenWaterOnOff" + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);

        device = devices.get(2);
//        function = new FunctionEntity();
//        function.setDeviceId(device.getId());
//        function.setName("BoilerOnOff");
//        function.setDescription("Start/Stop generator for Boiler circuit " + device.getName());
//        function.setDateSample(new Date(System.currentTimeMillis()
//                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
//        functions.add(function);

        device = devices.get(3);
        FunctionEntity function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("TempRefresh");
        function.setDescription("Temp sensor refresh " + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("Socket6");
        function.setDescription("Socket6 " + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        device = devices.get(4);
        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("DvrOnOff");
        function.setDescription("Sends the magic package to wake on LAN " + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        return functions;
    }

}