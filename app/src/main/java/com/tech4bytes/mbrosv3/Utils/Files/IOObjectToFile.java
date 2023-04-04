package com.tech4bytes.mbrosv3.Utils.Files;

import android.content.Context;

import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IOObjectToFile {
    public static void deleteObjectFromFile(String fileName) {
        try {
//            File file = new File(fileName);
//            System.out.println("Delete Detaisl: ");
//            System.out.println(file.getAbsolutePath());
//            System.out.println(file.exists());
//            file.delete();
//            System.err.println("Delete Success: ");

            fileName = "/" + fileName;
            System.out.println(fileName);

            File fdelete = new File(fileName);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    System.out.println("file Deleted :" + fileName);
                } else {
                    System.out.println("file not Deleted :" + fileName);
                }
            } else {
                System.out.println("file Deleted not exist:");
            }
        } catch (Exception e) {
            System.err.println("Delete error: ");
            e.printStackTrace();
        }
    }

    public void WriteObjectToFile(Context context, String fileName, Object object) throws IOException {
        LogMe.log("Writing to file: " + fileName);
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
            fos.close();
        } catch (Exception e) {
            System.out.println("Error while writing object to file: " + fileName);
            System.out.println(e);
            throw e;
        }
    }

    public Object ReadObjectFromFile(Context context, String fileName) {
        LogMe.log("Reading from file: " + fileName);
        try {
            FileInputStream fis = context.openFileInput(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            Object object = is.readObject();
            is.close();
            fis.close();
            return object;
        } catch (Exception e) {
            System.out.println("Error while reading object from file");
            System.out.println(e);
        }

        return null;
    }
}
