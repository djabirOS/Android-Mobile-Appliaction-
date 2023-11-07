package com.ostaxi.app;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.ostaxi.app.Model.CardModel;
import com.ostaxi.app.R;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StripeInfo {

    public interface CardListCallback {
        void onResult(ArrayList<CardModel> cardArrayList);
    }

    public interface ResponseCallback {
        void onResult(int result);
    }




    void fetchCardsList(CardListCallback callback, Context context) {
        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(context.getResources().getString(R.string.firebase_functions_base_url) + "/listCustomerCards?uid=" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String jsonDataString = null;
                try {
                    jsonDataString = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String defaultCard;
                try {
                    JSONObject jsonData = null;
                    if (jsonDataString != null) {
                        jsonData = new JSONObject(jsonDataString);
                    }
                    else{
                        return;
                    }


                    defaultCard = jsonData.getString("default_payment_method");

                    JSONArray cardJsonArray = new JSONArray(jsonData.getString("cards"));

                    ArrayList<CardModel> cardArrayList = new ArrayList<>();

                    for (int i = 0; i < cardJsonArray.length(); i++) {
                        JSONObject cardJson = cardJsonArray.getJSONObject(i);
                        CardModel mCard = new CardModel(cardJson.getString("id"));
                        JSONObject cardDetailsJson = cardJson.getJSONObject("card");
                        mCard.setBrand(cardDetailsJson.getString("brand"));
                        mCard.setExpMonth(cardDetailsJson.getInt("exp_month"));
                        mCard.setExpYear(cardDetailsJson.getInt("exp_year"));
                        mCard.setLastDigits(cardDetailsJson.getInt("last4"));
                        if(mCard.getId().equals(defaultCard)){
                            mCard.setDefaultCard(true);
                        }
                        cardArrayList.add(mCard);
                    }

                    if(callback != null){
                        callback.onResult(cardArrayList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }





}
