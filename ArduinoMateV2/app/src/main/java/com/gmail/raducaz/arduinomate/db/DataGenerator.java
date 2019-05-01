package com.gmail.raducaz.arduinomate.db;

import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.model.Device;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Generates data to pre-populate the database
 */
public class DataGenerator {

    public static List<DeviceEntity> generateDevices() {

        List<DeviceEntity> devices = new ArrayList<>(3);
        DeviceEntity device = new DeviceEntity();
        device.setIp("192.168.1.100");
        device.setPort(8080);
        device.setName("TEST - Generator");
        device.setDescription("Controllerul de pornire/oprire generator si control pompa - TEST");
        device.setId(1);
        devices.add(device);

        device = new DeviceEntity();
        device.setIp("192.168.1.200");
        device.setPort(8080);
        device.setName("PROD - Generator");
        device.setDescription("Controllerul de pornire/oprire generator si control pompa - PROD");
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

        // Device Generator - TEST
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

        // Device Generator - TEST

        // Device Generator - PROD
        device = devices.get(1);
        function = new FunctionEntity();
        function.setDeviceId(device.getId());
        function.setName("GeneratorOnOff");
        function.setDescription("Start/Stop Generator" + device.getName());
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

//        Random rnd = new Random();
//
//        for (Product product : products) {
//            int commentsNumber = rnd.nextInt(5) + 1;
//            for (int i = 0; i < commentsNumber; i++) {
//                CommentEntity comment = new CommentEntity();
//                comment.setProductId(product.getId());
//                comment.setText(COMMENTS[i] + " for " + product.getName());
//                comment.setPostedAt(new Date(System.currentTimeMillis()
//                        - TimeUnit.DAYS.toMillis(commentsNumber - i) + TimeUnit.HOURS.toMillis(i)));
//                comments.add(comment);
//            }
//        }

        return functions;
    }
}