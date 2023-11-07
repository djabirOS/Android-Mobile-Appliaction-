package com.ostaxi.app.Model;

import android.content.Context;
import android.text.format.DateFormat;

import com.google.firebase.database.DataSnapshot;

import java.math.BigDecimal;
import java.util.Calendar;



public class PayModel {

    String id, amount, date;Long timestamp;
    Context context;


    public PayModel(String id, String amount, String date, Long timestamp, Context context) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.timestamp = timestamp;
        this.context = context;
    }

    public PayModel(Context context){
        this.context = context;
    }




    public String getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }


    public BigDecimal round(int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(Float.parseFloat(amount)));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

}
