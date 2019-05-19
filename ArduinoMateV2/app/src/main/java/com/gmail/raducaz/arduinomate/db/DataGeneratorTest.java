package com.gmail.raducaz.arduinomate.db;

import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.model.Device;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Generates data to pre-populate the database
 */
public class DataGeneratorTest {

    public static List<DeviceEntity> generateDevices() {

        List<DeviceEntity> devices = new ArrayList<>(3);
        DeviceEntity device = new DeviceEntity();
        device.setIp("192.168.100.12");
        device.setPort(8080);
        device.setName("Generator");
        device.setDescription("Generator, pompa, senzor curent si presiune");
        device.setId(0);
        devices.add(device);

        device = new DeviceEntity();
        device.setIp("192.168.100.12");
        device.setPort(8081);
        device.setName("Tap");
        device.setDescription("Robineti");
        device.setId(1);
        devices.add(device);

        device = new DeviceEntity();
        device.setIp("192.168.100.12");
        device.setPort(8082);
        device.setName("Boiler");
        device.setDescription("Boiler si prize casa");
        device.setId(2);
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
        FunctionEntity function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("GeneratorOnOff");
        function.setDescription("Start/Stop Generator " + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("PowerOnOff");
        function.setDescription("Start/Stop 220V supply" + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("PumpOnOff");
        function.setDescription("Start/Stop generator and 220V supply to " + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        device = devices.get(1);

        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("WaterSupplyTapOnOff");
        function.setDescription("WaterSupplyTapOnOff" + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("HouseWaterOnOff");
        function.setDescription("HouseWaterOnOff" + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("GardenWaterOnOff");
        function.setDescription("GardenWaterOnOff" + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        device = devices.get(2);
        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("BoilerOnOff");
        function.setDescription("Start/Stop generator for Boiler circuit " + device.getName());
        function.setDateSample(new Date(System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
        functions.add(function);

        return functions;
    }

}