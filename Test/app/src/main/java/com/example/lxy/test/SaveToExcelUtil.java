package com.example.lxy.test;

import android.app.Activity;

import java.io.File;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Created by lxy on 2017/7/18.
 */

public class SaveToExcelUtil {

    private WritableWorkbook wwb ;
    private String excelPath ;
    private File excelFile ;
    private Activity activity ;
    private int row;

    public SaveToExcelUtil(Activity activity , String excelPath ){
        this.excelPath = excelPath ;
        this.activity = activity;

        excelFile = new File(excelPath);
        createExcel(excelFile);
    }

    public void createExcel(File file){
        WritableSheet ws = null ;

        try{
            if(!file.exists()){

                    wwb = Workbook.createWorkbook(file);
                    ws = wwb.createSheet("sheet",0);
                    Label lbl1 = new Label(0,0,"纬度");
                    Label lbl2 = new Label(1,0,"经度");
                    Label lbl3 = new Label(2,0,"定位方式");
                    Label lbl4 = new Label(3,0,"精度");
                    Label lbl5 = new Label(4,0,"时间戳");

                    ws.addCell(lbl1);
                    ws.addCell(lbl2);
                    ws.addCell(lbl3);
                    ws.addCell(lbl4);
                    ws.addCell(lbl5);

                    wwb.write();
                    wwb.close();

            }

            }catch (Exception e){
            e.printStackTrace();
        }
    }

public void writeToExcel(Object...args){
    try{

        Workbook oldWwb = Workbook.getWorkbook(excelFile);
        wwb = Workbook.createWorkbook(excelFile,oldWwb);
        WritableSheet ws = wwb.getSheet(0);
        row = ws.getRows() ;
        Label lab1 = new Label(0,row,args[0]+"");
        Label lab2 = new Label(1,row,args[1]+"");
        Label lab3 = new Label(2,row,args[2]+"");
        Label lab4 = new Label(3,row,args[3]+"");
        Label lab5 = new Label(4,row,args[4]+"");
        ws.addCell(lab1);
        ws.addCell(lab2);
        ws.addCell(lab3);
        ws.addCell(lab4);
        ws.addCell(lab5);

        wwb.write();
        wwb.close();

    }catch (Exception e){
        e.printStackTrace();
    }
    //Log.d("MainActivity","row is " + row );
}



}
