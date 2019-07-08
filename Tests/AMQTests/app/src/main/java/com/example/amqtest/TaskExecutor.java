package com.example.amqtest;

import android.os.AsyncTask;


public class TaskExecutor extends AsyncTask<TaskInterface, Void, String>
{
    protected String doInBackground(TaskInterface...tasks)
    {
        // Execute tasks
        for(TaskInterface task : tasks) {
            task.execute();
        }

        return "";
    }
}
