package com.gmail.raducaz.arduinomate.ui;

import android.os.AsyncTask;

import com.gmail.raducaz.arduinomate.processes.TaskInterface;


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
