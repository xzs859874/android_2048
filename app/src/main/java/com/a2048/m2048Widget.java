package com.a2048;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class m2048Widget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.m2048_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews view = new RemoteViews(context.getPackageName(),R.layout.m2048_widget);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("游戏提示")
                .setContentText("创建小部件成功")
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.bgp3))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("您有一条新消息")
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        Intent Intent1 = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, Intent1, 0);
        builder.setContentIntent(pendingIntent);
        Notification notify = builder.build();
        notificationManager.notify(0, notify);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Intent clickInt = new Intent(context, MenuActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickInt, 0);
        RemoteViews view = new RemoteViews(context.getPackageName(),R.layout.m2048_widget);
        view.setOnClickPendingIntent(R.id.img_widget, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, view);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

