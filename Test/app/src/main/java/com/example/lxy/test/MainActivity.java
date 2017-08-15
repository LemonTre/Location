package com.example.lxy.test;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.location.LocationManager.NETWORK_PROVIDER;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private int gpsTonet = 0 ;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private Timer mTimer;
    private  Timer mTimer1 = null;
    private TimerTask mTimerTask;
    private Location currentBestLocation;
    private LocationManager locationManager ;
    private String locationProvider;
    private long minTime ;
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
    private SimpleDateFormat date = new SimpleDateFormat("HHmm");

    private int count = 0 ;
    private Data data ;
    private int isRemoveNet = 0 ;

    //下拉菜单的实现
    private List<String> list = new ArrayList<>();
    private TextView myTextView ;
    private Spinner mySpinner ;
    private ArrayAdapter<String> adapter ;

    private ScheduledExecutorService service ;

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //在退出App时将网络与GPS监听移除
        try{
            locationManager.removeUpdates(networkListener);
            locationManager.removeUpdates(gpsListener);
        }catch (Exception e){
            e.printStackTrace();
        }
        //退出活动时将监听网络状态的广播移除
        unregisterReceiver(mReceiver);
        //是在测试Timer Start按钮时，使用到的
        if(mTimer1 != null){
            mTimer1.cancel();
        }
        //ScheduledExecutorService在活动取消时关闭
        if(service != null){
            service.shutdownNow();
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

                connManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                activeNetInfo = connManager.getActiveNetworkInfo();
                if( activeNetInfo != null && activeNetInfo.isAvailable()){
                    boolean isInter = pingIpAddress();
                    if(isInter){
                        Toast.makeText(MainActivity.this,"请选择时间间隔！",Toast.LENGTH_SHORT).show();
                        menu();
                    }else{

                        Toast.makeText(MainActivity.this,"未连接上网,请检查网络连接",Toast.LENGTH_SHORT).show();
                    }
                    //Toast.makeText(MainActivity.this,"连接",Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(MainActivity.this,"网络无连接，请开启网络连接",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            }
        };

        //运行权限
        List<String> permissionList  = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            registerReceiver(mReceiver,filter);
        }

        //各个按钮的实现
        Button addData = (Button) findViewById(R.id.add_data);
        Button deleteData = (Button) findViewById(R.id.delete_data);
        Button queryData = (Button) findViewById(R.id.query_data);
        Button timerStart = (Button) findViewById(R.id.timer_start);
        Button timerEnd = (Button) findViewById(R.id.timer_end);
        Button startLoc = (Button) findViewById(R.id.start_loc);
        //Button send = (Button) findViewById(R.id.finish);
        Button exportData = (Button) findViewById(R.id.export_data);
        //editText = (EditText) findViewById(R.id.edit_text);

        //按钮的监听
        addData.setOnClickListener(this);
        deleteData.setOnClickListener(this);
        queryData.setOnClickListener(this);
        timerStart.setOnClickListener(this);
        timerEnd.setOnClickListener(this);
        startLoc.setOnClickListener(this);
        //send.setOnClickListener(this);
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

    //下拉菜单的实现
    public void menu(){
        list.add("请选择！");
        list.add("2");
        list.add("5");
        list.add("10");

        myTextView = (TextView) findViewById(R.id.TextView_time);
        mySpinner = (Spinner) findViewById(R.id.Spinner_time);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);

        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String myTime = adapter.getItem(position);
                if(service != null) {
                    service.shutdownNow();
                }
                switch (myTime){
                    case "2":
                        minTime = Long.valueOf(myTime) * 1000;
                        Toast.makeText(MainActivity.this,"时间间隔确定！",Toast.LENGTH_SHORT).show();
                        startLoc();
                        break;
                    case "5":
                        minTime = Long.valueOf(myTime) * 1000;
                        Toast.makeText(MainActivity.this,"时间间隔确定！",Toast.LENGTH_SHORT).show();
                        startLoc();
                        break;
                    case "10":
                        minTime = Long.valueOf(myTime) * 1000;
                        Toast.makeText(MainActivity.this,"时间间隔确定！" ,Toast.LENGTH_SHORT).show();
                        startLoc();
                        break;
                    default:
                }
                myTextView.setText("您选择的时间间隔是(秒)：");
                parent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Toast.makeText(MainActivity.this,"2",Toast.LENGTH_SHORT).show();
                myTextView.setText("NONE");
                parent.setVisibility(View.VISIBLE);
            }
        });

        mySpinner.setOnTouchListener(new Spinner.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Toast.makeText(MainActivity.this,"3",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mySpinner.setOnFocusChangeListener(new Spinner.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Toast.makeText(MainActivity.this,"4",Toast.LENGTH_SHORT).show();
            }
        });

    }

    //各个按钮响应事件
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
            //case R.id.finish:
                //send();
                //break;
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

    //书记添加，直接书写两个数据添加到数据库中
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
        Toast.makeText(MainActivity.this,"Add Data",Toast.LENGTH_SHORT).show();
    }

    //数据删除，完成将数据库中的所有数据进行删除
    private void deleteData(){
        DataSupport.deleteAll(LocationData.class);

        data = new Data();
        data.setFlag(0);
        data.save();

        Toast.makeText(MainActivity.this,"Delete Data",Toast.LENGTH_SHORT).show();
    }

    //数据查询，按照数据产生的时间顺序，将数据查询出来
    private void queryData(){
        ArrayList<LocationData> location_list = (ArrayList<LocationData>) DataSupport.findAll(LocationData.class);
        Intent intent = new Intent(MainActivity.this, QueryActivity.class);
        intent.putParcelableArrayListExtra("locations_list", location_list);
        MainActivity.this.startActivity(intent);
    }

    //测试Tier时使用，Timer开始
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

    //测试Timer时使用，Timer结束
    private void timerEnd(){
        mTimer.cancel();
        Toast.makeText(MainActivity.this,"End Add Data",Toast.LENGTH_SHORT).show();
    }

    //判断定位的两次位置服务提供器是否一样
    private boolean isSameProvider(String provider1 , String provider2){
        if(provider1 == null){
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    //开始定位
    private void startLoc(){
        //获得位置管理器的实例
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        if(providers.contains(LocationManager.NETWORK_PROVIDER)){

        }else if(providers.contains(LocationManager.GPS_PROVIDER)){

        }else {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent1,0);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            return;
        }else{
            if(activeNetInfo != null && activeNetInfo.isAvailable()){

                currentBestLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);

                locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, networkListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 12000 , 0,
                        gpsListener);
                locationManager.addGpsStatusListener(statusListener);

                    service = Executors
                            .newSingleThreadScheduledExecutor();
                    service.scheduleAtFixedRate(getLogginDmsRunner(),0,minTime, TimeUnit.MILLISECONDS);

                Toast.makeText(MainActivity.this,"监听",Toast.LENGTH_SHORT).show();
            }else{
            }
        }
    }

    //ScheduledExecutorService固定时间间隔将数据添加到数据库中
    private Runnable getLogginDmsRunner(){
        return new Runnable() {
            @Override
            public void run() {
                Log.d("My","this is + ");

                updateLocation(currentBestLocation);

            }
        };
    }

    //得到Excel存储的位置
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

    //将数据库中的数据存储到Excel文件中
    private void saveData (ArrayList<LocationData> mylist){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

                st.writeToExcel(latitude, longitude, loctype, accuracy, dateStr);

            }
            Toast.makeText(MainActivity.this,"导出成功",Toast.LENGTH_SHORT).show();

    }else{
        Toast.makeText(MainActivity.this,"The list is Null",Toast.LENGTH_SHORT).show();
    }
    Toast.makeText(MainActivity.this,"demo" + fName,Toast.LENGTH_LONG).show();
    data = new Data();
    data.setFlag(count);
        data.save();

    }

    //判断网络是否连接上网
    private boolean pingIpAddress(){
        try{
            Process process = Runtime.getRuntime().exec("ping -c 1 -w 1 www.baidu.com");
            int status = process.waitFor();
            if(status == 0){
                return true;
            }else{
                return false;
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return false;
    }

    //判断新位置是否比当前位置更加的精确
    private boolean isBetterLocation(Location location , Location currentBestLocation){
        //Toast.makeText(MainActivity.this,"比较。。",Toast.LENGTH_SHORT).show();
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

    //添加位置信息到数据库
    private void updateLocation(final Location location){
        if(location != null && locationProvider != null){
            LocationData location2 = new LocationData();
            location2.setLatitude(location.getLatitude() + "");
            location2.setLongitude(location.getLongitude() + "");
            location2.setLoctype(locationProvider);
            location2.setAccuracy(location.getAccuracy());
            location2.setTimestamp(System.currentTimeMillis());
            location2.save();
            //Toast.makeText(MainActivity.this,"Start Locate Your Location!",Toast.LENGTH_SHORT).show();
            }else{
            }
    }

    //network监听
     LocationListener networkListener =
            new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    isRemoveNet = 0 ;
                    boolean flag = isBetterLocation(location,currentBestLocation);
                        if(flag || gpsTonet == 1){
                            currentBestLocation = location;
                            if(activeNetInfo != null){
                                Toast.makeText(MainActivity.this,"网络连接",Toast.LENGTH_SHORT).show();
                                locationProvider = activeNetInfo.getTypeName();

                            } else{
                                Toast.makeText(MainActivity.this,"网络无连接，网络连接",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        gpsTonet = 0 ;
                        }

      }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

    //GPS监听
    LocationListener gpsListener =
            new LocationListener() {
                private boolean isRemove = false;
                @Override
                public void onLocationChanged(final Location location) {
                    if(!isRemove){
                        isRemoveNet = 0 ;
                    }
                    boolean flag = isBetterLocation(location,currentBestLocation);

                    if(flag){
                            //Toast.makeText(MainActivity.this,"GPS",Toast.LENGTH_SHORT).show();
                            currentBestLocation = location;
                            locationProvider = LocationManager.GPS_PROVIDER;
                        }

                        if(location != null && !isRemove){
                            locationManager.removeUpdates(networkListener);
                            isRemoveNet = 1 ;
                            Toast.makeText(MainActivity.this,"网络到GPS",Toast.LENGTH_SHORT).show();
                            isRemove = true;
                        }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                    if(LocationProvider.AVAILABLE == status || LocationProvider.TEMPORARILY_UNAVAILABLE == status){
                        //Toast.makeText(MainActivity.this,"可见",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this,"GPS状态改变",Toast.LENGTH_SHORT).show();
                        gpsTonet = 1;
                        //flag2 = 1 ;
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, networkListener);
                    }
                    }

                @Override
                public void onProviderEnabled(String provider) {
                    Toast.makeText(MainActivity.this,"GPS位置服务开启",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Toast.makeText(MainActivity.this,"GPS位置服务已关闭，切换到网络定位",Toast.LENGTH_SHORT).show();
                    gpsTonet = 1;
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                            .ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                            .ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, networkListener);
                }
            };

     //权限判断时的回调方法
     public void onRequestPermissionsResult(int requestCode,  String[] permissions ,  int[]
                                           grantResults) {
         switch (requestCode){
             case 1:
                 if(grantResults.length > 0){
                     for (int result : grantResults){
                         if(result != PackageManager.PERMISSION_GRANTED){
                             Toast.makeText(MainActivity.this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                             finish();
                             return;
                         }
                     }
                     registerReceiver(mReceiver,filter);
                 }else{
                     finish();
                 }
                 break;
             default:
         }
    }

    //GPS状态监听，此时定位星的颗数
    private final GpsStatus.Listener statusListener = new GpsStatus.Listener(){
        public void onGpsStatusChanged(int event){
            LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context
            .LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                    .ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                    .ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }

            GpsStatus status = locationManager.getGpsStatus(null);
            updateGpsStatus(event ,status);
        }
    };

    //GPS状态变化时调用的方法
    private void updateGpsStatus(int event , GpsStatus status){
        if(event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            int count = 0 ;
            while(it.hasNext() && count <= maxSatellites){
                count++;
                if(count <= 4){
                    //isStar = 1;
                    gpsTonet = 0 ;
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                            .ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                            .ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    if(isRemoveNet == 1){
                        locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, networkListener);
                    }
                }else{
                    //Toast.makeText(MainActivity.this,"GPS定位 " ,Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(MainActivity.this,"找到星",Toast.LENGTH_SHORT).show();
            }
        }else if(status == null){
            Toast.makeText(MainActivity.this,"星的个数: 0" + count ,Toast.LENGTH_SHORT).show();
        }
    }
}


