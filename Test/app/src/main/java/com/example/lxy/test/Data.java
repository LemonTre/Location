package com.example.lxy.test;

import org.litepal.crud.DataSupport;

/**
 * Created by lxy on 2017/7/20.
 */

public class Data extends DataSupport{
    private int flag ;
    //private String time;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

/*
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }*/
}
