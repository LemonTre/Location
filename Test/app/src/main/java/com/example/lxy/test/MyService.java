package com.example.lxy.test;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

import static com.example.lxy.test.MainActivity.SERVICE_RECEIVER;

public class MyService extends Service {
    
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null ;
    }

    @Override
    public int onStartCommand(final Intent intent , int flags , int startId){

        String temp = "4000";
        //long time = 0 ;

        ArrayList<Time> datalist = (ArrayList<Time>) DataSupport.findAll(Time.class);

        if(datalist.size() != 0){
            for(Time dalist : datalist){
                temp = dalist.getTime();
            }
        }
        //Log.d("My","this is ");
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Log.d("My","this is + ");
                Intent intent = new Intent(SERVICE_RECEIVER);

                //data2[0].getLong("time");
                //minTime[0] = intent.getLongExtra("time",0);

                getApplicationContext().sendBroadcast(intent);

            }
        }).start();

        //long time = minTime[0];
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime ;
        if(temp != null){
            triggerAtTime = System.currentTimeMillis() + Long.valueOf(temp) ;
        }else{
            triggerAtTime = System.currentTimeMillis() + 4000 ;
        }

       //Log.d("My","this is A  " + temp);
        Intent i ;
        i = new Intent(this,MyService.class);
        PendingIntent pi = PendingIntent.getService(this , 0 , i , 0 );

        manager.set(AlarmManager.RTC,triggerAtTime,pi);

        //manager.setExact(AlarmManager.RTC ,triggerAtTime , pi);
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            manager.setExact(AlarmManager.RTC ,triggerAtTime , pi);
            Toast.makeText(MyService.this,"手机版本1： " + Build.VERSION.SDK_INT ,Toast.LENGTH_SHORT).show();

        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Log.d("My","this is + 1");
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC,triggerAtTime,pi);
            Toast.makeText(MyService.this,"手机版本2： " + Build.VERSION.SDK_INT ,Toast.LENGTH_SHORT).show();
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Log.d("My","this is + 2");
            manager.setExact(AlarmManager.RTC ,triggerAtTime , pi);
            Toast.makeText(MyService.this,"手机版本3： " + Build.VERSION.SDK_INT ,Toast.LENGTH_SHORT).show();
        }else{
            Log.d("My","this is + 3");
            if(temp != null){
                manager.setRepeating(AlarmManager.RTC,System.currentTimeMillis(),Long.valueOf(temp),pi);
            }else{
                manager.setRepeating(AlarmManager.RTC,System.currentTimeMillis(),4000,pi);
            }
            Toast.makeText(MyService.this,"手机版本4： " + Build.VERSION.SDK_INT ,Toast.LENGTH_SHORT).show();
        }*/

        return super.onStartCommand(intent , flags , startId);
    }

}
















