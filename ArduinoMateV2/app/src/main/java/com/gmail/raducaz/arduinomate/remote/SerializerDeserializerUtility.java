package com.gmail.raducaz.arduinomate.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializerDeserializerUtility {
    public static byte[] Serialize(Serializable object) throws IOException
    {
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(resultStream);
        out.writeObject(object);
        out.close();
        resultStream.close();

        return resultStream.toByteArray();
    }

    public static Object Deserialize(byte[] object) throws Exception
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(object);
        ObjectInputStream in = new ObjectInputStream(inputStream);
        return in.readObject();
    }
}
