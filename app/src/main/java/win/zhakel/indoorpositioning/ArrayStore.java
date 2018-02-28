package win.zhakel.indoorpositioning;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by zdk93 on 2018/2/28.
 */

class ArrayStore {
    private ArrayList<Float> arrays;
    private ArrayList<float[]> arrayLists;
//    private int elementsNumber;
    private String File_Name;

    ArrayStore() {
        arrays = new ArrayList<>();
        arrayLists = new ArrayList<>();
//        elementsNumber = 10;
        File_Name = "wifi.txt";
    }

//    ArrayStore(int num, String name) {
//        if (num > 0 && !name.isEmpty()) {
//            arrays = new ArrayList<>();
////            elementsNumber = num;
//            File_Name = name + ".txt";
//        } else {
//            Log.e(TAG, "changeElementNumbers: we need a num bigger than 0 or name is not empty.\n");
//        }
//    }

//    public void changeElementNumbers(int num){
//
//    }

    void addListElements(float[] list){
        arrayLists.add(list);
    }

    void storePdr(){
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/pdr.txt");

            FileOutputStream fos = new FileOutputStream(file,true);

            for(float[] f : arrayLists){
                for(float ff:f){
                    fos.write(String.valueOf(ff).getBytes());
                    fos.write("\t".getBytes());
                }
            }
//            fos.write(arrayLists.toString().getBytes());
            arrayLists.clear();
            fos.close();
            Log.i(TAG, "storeElements file path: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addElements(float element) {
        arrays.add(element);
//        if (arrays.size() >= elementsNumber)
//            storeElements();
    }

//    void clearArray() {
//        arrays.clear();
//    }
//
//    void delElement(float ele) {
//        if (arrays.remove(ele))
//            Log.i(TAG, "delElement: " + ele + " success.\n");
//        else
//            Log.i(TAG, "delElement: failed. No such element.\n");
//    }
//
//    void delElementIndex(int index) {
//        if (index < arrays.size())
//            Log.i(TAG, "delElementIndex " + index + ": " + arrays.remove(index) + " success.\n");
//        else
//            Log.i(TAG, "delElementIndex " + index + "over the range.\n");
//    }

    void storeElements() {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+File_Name);

            FileOutputStream fos = new FileOutputStream(file,true);

            fos.write(arrays.toString().getBytes());
            arrays.clear();
            fos.close();
            Log.i(TAG, "storeElements file path: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void delFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+File_Name);
        if(file.exists()){
            if(file.delete())
                Log.i(TAG, "delFile: file delete.\n");
            else
                Log.i(TAG, "delFile: no file.\n");
        }

    }

}
