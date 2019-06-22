package com.example.amqtest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskCaller implements TaskInterface {

    private String TAG = "TaskCaller";

    public TaskCaller()
    {}

    public void execute() {
        RemoteStateSender sender = new RemoteStateSender();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String message = "Msg " + dateFormat.format(date);

        sender.SendState(message);
    }

}
