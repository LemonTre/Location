package com.example.lxy.test;

import org.litepal.crud.DataSupport;

/**
 * Created by lxy on 2017/8/5.
 */

public class Time extends DataSupport {
    private String time ;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
