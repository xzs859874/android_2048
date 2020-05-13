package com.a2048;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

public class ExitApplication extends Application{

    private List<Activity> activityList = new LinkedList();

    private static ExitApplication instance;

    private ExitApplication(){

    }
    //单例模式中获取唯一的ExitApp实例
    public synchronized static ExitApplication getInstance(){
        if(null == instance){
            instance = new ExitApplication();
        }
        return instance;
    }
    //添加Activity到容器中
    public void addActivity(Activity activity){
        activityList.add(activity);
    }

    public void exit(){
        try {
            for(Activity activity:activityList){
                if(activity != null){
                    activity.finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            System.exit(0);
        }
    }

    public void onLowMemory(){
        super.onLowMemory();
        System.gc();
    }
}
