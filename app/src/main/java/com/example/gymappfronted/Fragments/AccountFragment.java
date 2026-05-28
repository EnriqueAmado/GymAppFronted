package com.example.gymappfronted.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.gymappfronted.R;

public class AccountFragment extends Fragment {

    private TextView tvUser, tvEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvUser = view.findViewById(R.id.tvAccountUser);
        tvEmail = view.findViewById(R.id.tvAccountEmail);

        SharedPreferences prefs = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "No disponible");
        String email = prefs.getString("email", "No disponible");

        tvUser.setText("Nombre de usuario: " + username);
        tvEmail.setText("Email: " + email);

        return view;
    }
}