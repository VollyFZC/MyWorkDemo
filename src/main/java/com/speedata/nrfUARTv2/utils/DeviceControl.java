package com.speedata.nrfUARTv2.utils;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DeviceControl {
    private BufferedWriter CtrlFile;
    private Context mContext;
    public static String _433 = "63";
    public static String uhf = "64";

    public DeviceControl(String path, Context context) throws IOException {
        File DeviceName = new File(path);
        mContext = context;
        CtrlFile = new BufferedWriter(new FileWriter(DeviceName, false)); // open
    }

    public void PowerOnDevice(String power_on) throws IOException // poweron
    // barcode
    // device
    {
        CtrlFile.write(power_on);
        CtrlFile.flush();
    }

    public void PowerOffDevice(String power_off) throws IOException // poweroff
    // barcode
    // device
    {
        CtrlFile.write(power_off);
        CtrlFile.flush();
    }

    public void TriggerOnDevice() throws IOException // make barcode begin to
    // scan
    {
        CtrlFile.write("trig");
        CtrlFile.flush();
    }

    public void TriggerOffDevice() throws IOException // make barcode stop scan
    {
        CtrlFile.write("trigoff");
        CtrlFile.flush();
    }

    public void DeviceClose() throws IOException // close file
    {
        CtrlFile.close();
    }

    public void MTGpioOn() {
        try {
            CtrlFile.write("-wdout" + _433 + " 1");
            CtrlFile.flush();
//			Toast.makeText(mContext, "open mtgpio driver success",
//					Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void MTGpioOff() {
        try {
            CtrlFile.write("-wdout" + _433 + " 0");
            CtrlFile.flush();
//			Toast.makeText(mContext, "close mtgpio driver success",
//					Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void MTGpioUHFOn() {
        try {
            CtrlFile.write("-wdout" + uhf + " 1");
            CtrlFile.flush();
//			Toast.makeText(mContext, "open mtgpio driver success",
//					Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void MTGpioUHFOff() {
        try {
            CtrlFile.write("-wdout" + uhf + " 0");
            CtrlFile.flush();
//			Toast.makeText(mContext, "close mtgpio driver success",
//					Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}