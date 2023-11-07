package com.ostaxi.app;

import android.app.Activity;


import com.ostaxi.app.Model.TypeModel;
import com.ostaxi.app.R;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Utils {


    public BigDecimal round(float amount, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(amount));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }



    public static ArrayList<TypeModel> getTypeList(Activity activity){
        ArrayList<TypeModel> typeArrayList = new ArrayList<>();


        typeArrayList.add(new TypeModel("type_1", activity.getResources().getString(R.string.type_1), activity.getResources().getDrawable(R.drawable.ic_type_1), 4,"Affordable trips,all to yourself"));
        typeArrayList.add(new TypeModel("type_2", activity.getResources().getString(R.string.type_2), activity.getResources().getDrawable(R.drawable.ic_type_2), 6,"Affordable trips for groups of up to 6"));
        typeArrayList.add(new TypeModel("type_3", activity.getResources().getString(R.string.type_3), activity.getResources().getDrawable(R.drawable.ic_type_3), 4,"Luxury trips with professional drivers"));
        typeArrayList.add(new TypeModel("type_4", activity.getResources().getString(R.string.type_4), activity.getResources().getDrawable(R.drawable.ic_type_4), 1,"Bike for 1 person"));

        return  typeArrayList;
    }



    public static int rideCostEstimate(double distance, double duration){
        double price;
        price = 36 + distance * 26 + duration * 0.001;
        return (int) price;
    }

    public static TypeModel getTypeById(Activity activity, String id){
        ArrayList<TypeModel> typeArrayList = getTypeList(activity);
        for(TypeModel mType : typeArrayList){
            if(mType.getId().equals(id)){
                return mType;
            }
        }
        return null;
    }
}
