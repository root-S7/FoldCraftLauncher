package com.tungsten.fcl.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tungsten.fcl.FCLApplication;
import com.tungsten.fcl.R;
import com.tungsten.fcl.activity.SplashActivity;
import com.tungsten.fcl.util.AndroidUtils;
import com.tungsten.fclcore.util.io.NetworkUtils;
import com.tungsten.fcllibrary.component.FCLFragment;
import com.tungsten.fcllibrary.component.view.FCLButton;
import com.tungsten.fcllibrary.component.view.FCLProgressBar;
import com.tungsten.fcllibrary.component.view.FCLTextView;

import java.io.IOException;

public class EulaFragment extends FCLFragment implements View.OnClickListener {

    public static final String EULA_URL = FCLApplication.appConfig.getProperty("eula-url","https://mirror.ghproxy.com/https://raw.githubusercontent.com/hyplant/FoldCraftLauncher/doc/eula/latest.txt");

    private FCLProgressBar progressBar;
    private FCLTextView eula;

    private FCLButton next;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eula, container, false);

        progressBar = findViewById(view, R.id.progress);
        eula = findViewById(view, R.id.eula);

        next = findViewById(view, R.id.next);
        next.setOnClickListener(this);

        loadEula();
        
        return view;
    }

    private void loadEula() {
        new Thread(() -> {
            String str;
            try {
                str = NetworkUtils.doGet(NetworkUtils.toURL(EULA_URL),FCLApplication.deviceInfoUtils.toString());
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                str = getString(R.string.splash_eula_error);
            }
            final String s = str;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    eula.setText(s);
                    eula.setTextSize(16.5F);
                    eula.setTextColor(Color.BLACK);
                });
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                next.setEnabled(true); // 启用按钮
            });
        }).start();
    }

    @Override
    public void onClick(View view) {
        if (view == next) {
            if (getActivity() != null) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("launcher", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_first_launch", false);
                editor.apply();
                ((SplashActivity) getActivity()).start();
            }
        }
    }
}
