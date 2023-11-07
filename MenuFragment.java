package com.ostaxi.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.ostaxi.app.AuthActivity;
import com.ostaxi.app.R;

import org.jetbrains.annotations.NotNull;


public class MenuFragment extends Fragment implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {


    String name, email;
    String idToken;
    private FirebaseAuth firebaseAuth;
    private View view;
    Button getStarted;

    TextView login2;
    private boolean started = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_menu, container, false);


            getStarted = view.findViewById(R.id.getStarted);
            login2 = view.findViewById(R.id.login2);

        }
        else{

            container.removeView(view);}


        firebaseAuth = FirebaseAuth.getInstance();



        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AuthActivity) getActivity()).registrationClick();


            }
        });
        login2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AuthActivity) getActivity()).loginClick();


            }
        });

        started = true;

        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button mLogin = view.findViewById(R.id.login);
        Button mRegistration = view.findViewById(R.id.registration);

        mRegistration.setOnClickListener(this);
        mLogin.setOnClickListener(this);
    }




    private void firebaseAuthWithGoogle(AuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                    } else {
                        task.getException().printStackTrace();
                        Toast.makeText(getActivity(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registration:
                ((AuthActivity) getActivity()).registrationClick();
                break;
            case R.id.login:
                ((AuthActivity) getActivity()).loginClick();
                break;
        }
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}