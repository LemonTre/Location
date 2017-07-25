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
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.location.LocationManager.NETWORK_PROVIDER;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private int gpsTonet = 0 ;
    private int flag1 = 0 ;
    private int flag2 = 0 ;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private Timer mTimer;
    private Timer mTimer1;
    private TimerTask mTimerTask,mTimerTask1;
    private Location currentBestLocation;
    private LocationManager locationManager ;

    private String locationProvider;
    private EditText editText;
    private long minTime = 2000;
    private ConnectivityManager connManager;
    private NetworkInfo activeNetInfo = null;
    private BroadcastReceiver mReceiver = null;
    private IntentFilter filter;
    private String fileName ;
    private String excelPath ;
    private SaveToExcelUtil st ;
    private String latitude;
    private String longitude;
    private String loctype;
    private float accuracy;
    private String dateStr;
    private String fName;
    //private int isFirst = 1 ;

    private SimpleDateFormat date = new SimpleDateFormat("HHmm");

    private int count = 0 ;
    private Data data ;

    private static final int MY_PERMISSIONS_REQUEST = 1;
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

        //startLoc();

        //注册网络监听
        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                connManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                activeNetInfo = connManager.getActiveNetworkInfo();
                if( activeNetInfo != null && activeNetInfo.isAvailable()){
                    startLoc();
                    //Toast.makeText(MainActivity.this,"连接",Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(MainActivity.this,"网络无连接，请开启网络连接",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            }
        };

        registerReceiver(mReceiver,filter);

        Button addData = (Button) findViewById(R.id.add_data);
        Button deleteData = (Button) findViewById(R.id.delete_data);
        Button queryData = (Button) findViewById(R.id.query_data);
        Button timerStart = (Button) findViewById(R.id.timer_start);
        Button timerEnd = (Button) findViewById(R.id.timer_end);
        Button startLoc = (Button) findViewById(R.id.start_loc);
        Button send = (Button) findViewById(R.id.finish);
        Button exportData = (Button) findViewById(R.id.export_data);

        editText = (EditText) findViewById(R.id.edit_text);


        addData.setOnClickListener(this);
        deleteData.setOnClickListener(this);
        queryData.setOnClickListener(this);
        timerStart.setOnClickListener(this);
        timerEnd.setOnClickListener(this);
        startLoc.setOnClickListener(this);
        send.setOnClickListener(this);
        exportData.setOnClickListener(this);


        /* 运行前数据库自动创建
       createDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Connector.getDatabase();
                Toast.makeText(MainActivity.this,"Create database",Toast.LENGTH_SHORT).show();
            }
        });*/
    }



    //onCreate()结束

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.add_data:
                addData();
                break;
            case R.id.delete_data:
                deleteData();
                break;
            case R.id.query_data:
                queryData();
                break;
            case R.id.timer_start:
                timerStart();
                break;
            case R.id.timer_end:
                timerEnd();
                break;
            case R.id.start_loc:
                //startLoc();
                break;
            case R.id.finish:
                send();
                break;
            case R.id.export_data:
                fName = date.format(System.currentTimeMillis());
                excelPath = getSDPath() + File.separator+ "demo" + fName + ".xls"  ;
                st = new SaveToExcelUtil(this,excelPath);

                ArrayList<LocationData> list = (ArrayList<LocationData>) DataSupport.findAll(LocationData.class);

                try{
                    saveData(list);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                break;

        }
    }

    private void addData(){
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

    private void deleteData(){
        DataSupport.deleteAll(LocationData.class);
        data = new Data();
        data.setFlag(0);
        data.save();

        Toast.makeText(MainActivity.this,"Delete Data",Toast.LENGTH_SHORT).show();
    }

    private void queryData(){
        ArrayList<LocationData> location_list = (ArrayList<LocationData>) DataSupport.findAll(LocationData.class);
        Intent intent = new Intent(MainActivity.this, QueryActivity.class);
        intent.putParcelableArrayListExtra("locations_list", location_list);
        MainActivity.this.startActivity(intent);
    }

    private void timerStart(){
        mTimer = new Timer();
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

    private void timerEnd(){
        mTimer.cancel();
        Toast.makeText(MainActivity.this,"End Add Data",Toast.LENGTH_SHORT).show();
    }

    private boolean isSameProvider(String provider1 , String provider2){
        if(provider1 == null){
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void startLoc(){
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
            Toast.makeText(MainActivity.this,"监听",Toast.LENGTH_SHORT).show();
        }else{

        }

    }

    private void send(){
        String inputText = editText.getText().toString();
        //Toast.makeText(MainActivity.this,"时间间隔：" +inputText  + "秒",Toast.LENGTH_SHORT).show();
        if(editText.length() == 0){
            minTime = 2000;
            Toast.makeText(MainActivity.this,"默认时间间隔：" + 2 + "秒",Toast.LENGTH_SHORT).show();
        }else{
            minTime = Long.parseLong(inputText) * 1000;
            Toast.makeText(MainActivity.this,"时间间隔：" + inputText + "秒",Toast.LENGTH_SHORT).show();
        }

        editText.setText(null);
    }

    public String getSDPath(){

        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getPath();
        File dir = new File(fileName);
        if(dir.exists()){
            //Toast.makeText(MainActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
            return dir.toString();
        }else{
            dir.mkdirs();
            //Toast.makeText(MainActivity.this,"保存路径不存在",Toast.LENGTH_SHORT).show();
            return dir.toString();
        }
    }

    private void saveData (ArrayList<LocationData> mylist){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //File file = new File(excelPath);

        ArrayList<Data> datalist = (ArrayList<Data>) DataSupport.findAll(Data.class);

        for(Data dalist : datalist){
            count = dalist.getFlag();

        }

        ArrayList<LocationData> list = mylist;
        int temp = count ;
        if(list != null && list.size() > 0){

            for(int i = list.size() - 1 ; i >= temp ; --i){
                ++count;
                LocationData location = list.get(i);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                loctype = location.getLoctype();
                accuracy = location.getAccuracy();
                dateStr = dateFormat.format(location.getTimestamp());
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission
                .READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST);
                }else {
                    st.writeToExcel(latitude, longitude, loctype, accuracy, dateStr);
                    Toast.makeText(MainActivity.this,"导出成功",Toast.LENGTH_SHORT).show();
                }
            }

    }else{
        Toast.makeText(MainActivity.this,"The list is Null",Toast.LENGTH_SHORT).show();
    }
    Toast.makeText(MainActivity.this,"demo" + fName,Toast.LENGTH_LONG).show();
    data = new Data();
    data.setFlag(count);
        data.save();

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
                                //Log.d("MainActivity","time is " + "2");
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
                public void onLocationChanged(final Location location) {

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
                                //Log.d("MainActivity","time is " + "3");
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


     public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions , @NonNull int[]
                                           grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                st.writeToExcel(latitude, longitude, loctype, accuracy, dateStr);
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}

