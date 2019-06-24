package com.example.amqtest;

import com.rabbitmq.client.Channel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.amqtest.MyApplication.AmqConnection;
import static com.example.amqtest.MyApplication.EXCHANGE_NAME;

public class TaskCaller implements TaskInterface {

    private String TAG = "TaskCaller";

    public TaskCaller()
    {}

    public void execute() {
        RemoteStateSender sender = new RemoteStateSender();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String message = "Msg " + dateFormat.format(date);

        try {
            for (int i = 0; i < 100; i++) {
                sender.SendState("Msg:" + i);
            }
        }
        catch (Exception exc){}
    }

}
