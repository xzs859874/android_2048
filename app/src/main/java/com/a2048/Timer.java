package com.a2048;

import android.os.Handler;

import java.sql.Time;
import java.util.TimerTask;

public class Timer {

    public static String getFormatHMS(Long time){
        time = time/1000;
        int s = (int) (time%60);//秒
        int m = (int) (time/60)%60;//分
        return String.format("%02d:%02d",m,s);
    }
}