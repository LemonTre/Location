package com.example.lxy.test;

import org.litepal.crud.DataSupport;

/**
 * Created by lxy on 2017/7/20.
 */

public class Data extends DataSupport{
    private int flag ;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
