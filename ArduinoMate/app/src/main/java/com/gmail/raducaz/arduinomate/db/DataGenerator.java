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

    private static final String[] FIRST = new String[]{
            "Special edition", "New", "Cheap", "Quality", "Used"};
    private static final String[] SECOND = new String[]{
            "Three-headed Monkey", "Rubber Chicken", "Pint of Grog", "Monocle"};
    private static final String[] DESCRIPTION = new String[]{
            "is finally here", "is recommended by Stan S. Stanman",
            "is the best sold product on Mêlée Island", "is \uD83D\uDCAF", "is ❤️", "is fine"};
    private static final String[] FUNCTIONS = new String[]{
            "Function 1", "Function 2", "Function 3", "Function 4", "Function 5", "Function 6",
    };

    public static List<DeviceEntity> generateDevices() {

        List<DeviceEntity> devices = new ArrayList<>(3);
        DeviceEntity device = new DeviceEntity();
        device.setName("My Arduino Dev 1");
        device.setDescription("Desc");
        device.setId(1);
        devices.add(device);
        device = new DeviceEntity();
        device.setName("My Arduino Dev 2");
        device.setDescription("Desc");
        device.setId(2);
        devices.add(device);
        device = new DeviceEntity();
        device.setName("My Arduino Dev 3");
        device.setDescription("Desc");
        device.setId(3);
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

        for (Device device : devices) {
            FunctionEntity function = new FunctionEntity();
            function.setDeviceId(device.getId());
            function.setText("StateFct");
            function.setPostedAt(new Date(System.currentTimeMillis()
                    - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
            functions.add(function);

            function = new FunctionEntity();
            function.setDeviceId(device.getId());
            function.setText("ProgressFct");
            function.setPostedAt(new Date(System.currentTimeMillis()
                    - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
            functions.add(function);

            function = new FunctionEntity();
            function.setDeviceId(device.getId());
            function.setText("MonitorFct");
            function.setPostedAt(new Date(System.currentTimeMillis()
                    - TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(0)));
            functions.add(function);
        }


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