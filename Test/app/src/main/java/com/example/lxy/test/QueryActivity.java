package com.example.lxy.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class QueryActivity extends AppCompatActivity {

    private static String[] str ;
    private ArrayList<LocationData> list;
    private String[] string = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        Intent intent = getIntent();
        if(list != null){
            list.clear();
        }
        list = intent.getParcelableArrayListExtra("locations_list");
        str = new String[list.size()];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr;
        int j = 0 ;

        if(list != null && list.size() > 0){

            for(int i = list.size() - 1 ; i >= 0 ; --i){

                LocationData location = list.get(i);
                dateStr = dateFormat.format(location.getTimestamp());
                str[j++] = "纬度：" + location.getLatitude() + "\n"  +
                        "经度： " + location.getLongitude() + "\n" +
                        "定位方式： " + location.getLoctype() + "\n" +
                        "精度(米)： " + location.getAccuracy() + "\n" +
                        "时间戳:  " + dateStr + "\n" +
                "------------------------------------------\n";
            }
        }else{
           string[0] = "The database is null . Please add the data!";
        }

        if(list != null && list.size() > 0){
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    str);
            ListView listView = (ListView)findViewById(R.id.Location_Text);
            listView.setAdapter(adapter);
        }else{
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    string);
            ListView listView = (ListView)findViewById(R.id.Location_Text);
            listView.setAdapter(adapter);
        }
    }
}
