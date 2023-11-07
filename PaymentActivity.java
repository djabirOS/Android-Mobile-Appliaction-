package com.ostaxi.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ostaxi.app.Adapters.Card;

public class PaymentActivity extends AppCompatActivity {

    ImageView mAddCard;
    RecyclerView recyclerView;
    StripeInfo.CardListCallback cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);



        recyclerView = findViewById(R.id.recyclerView);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        setupToolbar();

        mAddCard = findViewById(R.id.add_card_image);
        mAddCard.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, AddPaymentActivity.class);
            startActivity(intent);
        });





    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth auth = FirebaseAuth.getInstance();




        Query query = FirebaseDatabase.getInstance().getReference().child("customer_payout").child(auth.getCurrentUser().getUid());


        FirebaseRecyclerOptions<Card> options =
                new FirebaseRecyclerOptions.Builder<Card>()
                        .setQuery(query, Card.class)
                        .build();

        FirebaseRecyclerAdapter friendsConvAdapter = new FirebaseRecyclerAdapter<Card, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_card2, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder viewHolder, int position, Card model) {



                viewHolder.cardName.setText(model.getName());
                viewHolder.cardNumber.setText(model.getCard());
                viewHolder.expire.setText(model.getExpire());





            }
        };


        friendsConvAdapter.startListening();
        recyclerView.setAdapter(friendsConvAdapter);
        friendsConvAdapter.startListening();



    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView cardName,cardNumber,expire;

        public ViewHolder(View itemView) {
            super(itemView);
            mView =itemView;
            cardName = itemView.findViewById(R.id.cardName);
            cardNumber = itemView.findViewById(R.id.cardNumber);
            expire = itemView.findViewById(R.id.expire);

        }





    }

    private void setupToolbar() {
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.payment));
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(v -> finish());
    }




    @Override
    protected void onResume() {
        super.onResume();

        new StripeInfo().fetchCardsList(cb, getApplicationContext());
    }
}
