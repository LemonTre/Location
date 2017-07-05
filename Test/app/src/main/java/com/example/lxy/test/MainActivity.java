package com.example.lxy.test;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.location.LocationManager.NETWORK_PROVIDER;

public class MainActivity extends AppCompatActivity{

    private int gpsTonet = 0 ;
    private int flag1 = 0 ;
    private int flag2 = 0 ;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private Timer mTimer = new Timer();
    private Timer mTimer1,mTimer2;
    private TimerTask mTimerTask,mTimerTask1,mTimerTask2;
    private Location currentBestLocation;
    private LocationManager locationManager ;
    //private WifiManager wifi = null;
    //private TelephonyManager tele = null;
    private String locationProvider;
    private EditText editText;
    private long minTime = 2000;
    private NetworkInfo activeNetInfo = null;
    private BroadcastReceiver mReceiver = null;
    private IntentFilter filter;

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            locationManager.removeUpdates(networkListener);
            locationManager.removeUpdates(gpsListener);
        }catch (Exception e){
            e.printStackTrace();
        }
        unregisterReceiver(mReceiver);
        if(mTimer1 != null){
            mTimer1.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //注册网络监听
        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                ConnectivityManager connManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                activeNetInfo = connManager.getActiveNetworkInfo();

                if( activeNetInfo != null && activeNetInfo.isAvailable()){
                    //if(activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI){
                    //}else if(activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                    //}
                    //Toast.makeText(MainActivity.this,"请开启网络连接",Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(MainActivity.this,"网络无连接，请开启网络连接",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            }
        };

        registerReceiver(mReceiver,filter);

        final Button addData = (Button) findViewById(R.id.add_data);
        Button deleteData = (Button) findViewById(R.id.delete_data);
        Button queryData = (Button) findViewById(R.id.query_data);
        Button timerStart = (Button) findViewById(R.id.timer_start);
        Button timerEnd = (Button) findViewById(R.id.timer_end);
        final Button startLoc = (Button) findViewById(R.id.start_loc);

        editText = (EditText) findViewById(R.id.edit_text);
        Button send = (Button) findViewById(R.id.finish);



        /* 运行前数据库自动创建
       createDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Connector.getDatabase();
                Toast.makeText(MainActivity.this,"Create database",Toast.LENGTH_SHORT).show();
            }
        });
        */

        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocationData location1 = new LocationData();
                location1.setLatitude("20.23423");
                location1.setLongitude("121.23123");
                location1.setLoctype("GPS定位");
                location1.setTimestamp(System.currentTimeMillis());
                location1.save();

                LocationData location2 = new LocationData();
                location2.setLatitude("21.12332");
                location2.setLongitude("120.47532");
                location2.setLoctype("基站定位");
                location2.setTimestamp(System.currentTimeMillis());
                location2.save();

              //  Log.d("MainActivity11","the current time is " + System.currentTimeMillis());
                Toast.makeText(MainActivity.this,"Add Data",Toast.LENGTH_SHORT).show();
            }
        });

        deleteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DataSupport.findBySQL("drop database LocationStore");
                DataSupport.deleteAll(LocationData.class);
                Toast.makeText(MainActivity.this,"Delete Data",Toast.LENGTH_SHORT).show();
            }
        });

        queryData.setOnClickListener(new View.OnClickListener() {




            @Override
            public void onClick(View v) {
                ArrayList<LocationData> location_list = (ArrayList<LocationData>) DataSupport.findAll(LocationData.class);
                Intent intent = new Intent(MainActivity.this, QueryActivity.class);
                intent.putParcelableArrayListExtra("locations_list", location_list);
                MainActivity.this.startActivity(intent);

            }
        });

        timerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {

                        LocationData location1 = new LocationData();
                        location1.setLatitude("22.23343");
                        location1.setLongitude("121.22123");
                        location1.setLoctype("Wifi定位");
                        location1.setTimestamp(System.currentTimeMillis());
                        location1.save();
                    }
                };
                mTimer.schedule(mTimerTask, 2000, 2000);
                Toast.makeText(MainActivity.this,"Start Add Data",Toast.LENGTH_SHORT).show();
            }
        });

        timerEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimer.cancel();
                Toast.makeText(MainActivity.this,"End Add Data",Toast.LENGTH_SHORT).show();
            }
        });

        startLoc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                //获得位置管理器的实例
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                List<String> providers = locationManager.getProviders(true);

                if(providers.contains(LocationManager.NETWORK_PROVIDER)){

                }else if(providers.contains(LocationManager.GPS_PROVIDER)){

                }else {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent,0);
                }

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                        .ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                        .ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                if(activeNetInfo != null && activeNetInfo.isAvailable()){
                    currentBestLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);

                    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, networkListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0,
                            gpsListener);
                    Log.d("MainActivity","time is " + "1");
                    Toast.makeText(MainActivity.this,"监听",Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this,"网络无连接",Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this,"网络无连接",Toast.LENGTH_SHORT).show();
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String inputText = editText.getText().toString();
                minTime = Long.parseLong(inputText) * 1000;
                Toast.makeText(MainActivity.this,"时间间隔：" + inputText + "秒",Toast.LENGTH_SHORT).show();
                editText.setText(null);
            }
        });
    }


    //onCreate()结束


    private boolean isSameProvider(String provider1 , String provider2){
        if(provider1 == null){
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private boolean isBetterLocation(Location location , Location currentBestLocation){
        Toast.makeText(MainActivity.this,"比较。。",Toast.LENGTH_SHORT).show();
        if(currentBestLocation == null){
            return true;
        }
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < - TWO_MINUTES;
        boolean isNewer = timeDelta > 0;
        if(isSignificantlyNewer){
            return true;
        }else if(isSignificantlyOlder){
            return false;
        }
        int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean ifFromSameProvider = isSameProvider(location.getProvider(),currentBestLocation.getProvider());
        if(isMoreAccurate){
            return true;
        }else if(isNewer && !isLessAccurate){
            return true;
        }else if(isNewer && !isSignificantlyLessAccurate && ifFromSameProvider){
            return true;
        }
        return false;
    }

    private void updateLocation(final Location location){
        //添加位置信息到数据库
                if(location != null){
                    LocationData location2 = new LocationData();
                    location2.setLatitude(location.getLatitude() + "");
                    location2.setLongitude(location.getLongitude() + "");
                    location2.setLoctype(locationProvider);
                    location2.setAccuracy(location.getAccuracy());
                    location2.setTimestamp(System.currentTimeMillis());
                    location2.save();
                    //Toast.makeText(MainActivity.this,"Start Locate Your Location!",Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(MainActivity.this,"Your location is NULL! Please open the GPS or network!",Toast
                            //.LENGTH_SHORT).show();
                }
    }
    //network监听
     LocationListener networkListener =
            new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    //if(flag1 == 1){
                       // mTimer1.cancel();
                       // flag1 = 0 ;
                    //}else
                    if(flag1 ==1 || flag2 == 2 ){
                        if(mTimer1 != null){
                            mTimer1.cancel();
                        }

                    }
                    flag1 = 1 ;
 //                   Log.d("MainActivity","is :" + System.currentTimeMillis());
                    boolean flag = isBetterLocation(location,currentBestLocation);
                        if(flag || gpsTonet == 1){
                            currentBestLocation = location;
                            if(activeNetInfo != null){
                                Toast.makeText(MainActivity.this,"网络连接",Toast.LENGTH_SHORT).show();
                                locationProvider = activeNetInfo.getTypeName();
                                //if(activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI){
                                 //locationProvider = WIFI_SERVICE;
                                 //}else if(activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                                    //locationProvider =
                                //}
                            } else{
                                Toast.makeText(MainActivity.this,"网络无连接，网络连接",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        gpsTonet = 0 ;
                        }

                        mTimer1 = new Timer();
                        mTimerTask1 = new TimerTask() {
                            @Override
                            public void run() {

                                Log.d("MainActivity","time is " + "2");
                                updateLocation(currentBestLocation);
                            }
                        };

                        mTimer1.schedule(mTimerTask1,0,minTime);




                    //Log.d("MainActivity","is :" + currentBestLocation.getAccuracy());

                    //Toast.makeText(MainActivity.this,"net",Toast.LENGTH_SHORT).show();
                    //Log.d("MainActivity11","net: " + networkListener + "");
      }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    /*
                    if(LocationProvider.OUT_OF_SERVICE == status){
                        Toast.makeText(MainActivity.this,"网络服务丢失，切换至GPS定位",Toast.LENGTH_SHORT).show();
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, gpsListener);
                    }
                    */
                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {
                    /*
                        Toast.makeText(MainActivity.this,"网络服务丢失1，切换至GPS定位",Toast.LENGTH_SHORT).show();
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, gpsListener);
                        */
                }
            };
    //GPS监听
    LocationListener gpsListener =
            new LocationListener() {
                private boolean isRemove = false;
                @Override
                public void onLocationChanged(Location location) {

                    //if(flag2 == 1){
                     //   mTimer1.cancel();
                      //  flag2 = 0;
                   // }else
                    if(flag2 == 2 || flag1 == 1){
                        if(mTimer1 != null){
                            mTimer1.cancel();
                        }
                    }
                    flag2 = 2 ;
                    boolean flag = isBetterLocation(location,currentBestLocation);
                        if(flag){
                            currentBestLocation = location;
                            locationProvider = LocationManager.GPS_PROVIDER;
                        }
                        if(location != null && !isRemove){
                            //flag1 = 2;
                            locationManager.removeUpdates(networkListener);
                            Toast.makeText(MainActivity.this,"网络到GPS",Toast.LENGTH_SHORT).show();
                            isRemove = true;
                        }
                        mTimer1 = new Timer();
                        mTimerTask1 = new TimerTask() {
                            @Override
                            public void run() {
                                Log.d("MainActivity","time is " + "3");
                                updateLocation(currentBestLocation);
                            }
                        };
                        mTimer1.schedule(mTimerTask1,0,minTime);

                    Toast.makeText(MainActivity.this,"GPS",Toast.LENGTH_SHORT).show();
                    //Log.d("MainActivity11","GPS: " + gpsListener + "");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    /*
                   if(LocationProvider.OUT_OF_SERVICE == status){
                        Toast.makeText(MainActivity.this,"GPS服务丢失，切换至网络定位",Toast.LENGTH_SHORT).show();
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        gpsTonet = 1;
                       //Toast.makeText(MainActivity.this,"GPS服务丢失1，切换至网络定位",Toast.LENGTH_SHORT).show();
                        locationManager.requestLocationUpdates(NETWORK_PROVIDER,0,0,networkListener);*/
                    }



                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {


                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                            .ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                            .ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    gpsTonet = 1;
                    //flag2 = 1 ;
                    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, networkListener);
                    Toast.makeText(MainActivity.this,"GPS服务丢失1，切换至网络定位",Toast.LENGTH_SHORT).show();
                }
            };
}

