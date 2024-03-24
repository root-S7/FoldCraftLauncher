package com.tungsten.fcl.fragment;

import android.annotation.*;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;
import androidx.annotation.*;
import com.tungsten.fcl.*;
import com.tungsten.fcl.activity.SplashActivity;
import com.tungsten.fcl.util.*;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fcllibrary.component.FCLFragment;
import com.tungsten.fcllibrary.component.view.*;
import com.tungsten.fcllibrary.util.LocaleUtils;
import java.io.*;
import java.util.*;

public class RuntimeFragment extends FCLFragment implements View.OnClickListener {

    boolean lwjgl = false;
    boolean cacio = false;
    boolean cacio11 = false;
    boolean cacio17 = false;
    boolean java8 = false;
    boolean java11 = false;
    boolean java17 = false;
    boolean java21 = false;
    boolean gamePackages = false;
    boolean others = false;

    private FCLProgressBar lwjglProgress;
    private FCLProgressBar cacioProgress;
    private FCLProgressBar cacio11Progress;
    private FCLProgressBar cacio17Progress;
    private FCLProgressBar java8Progress;
    private FCLProgressBar java11Progress;
    private FCLProgressBar java17Progress;
    private FCLProgressBar java21Progress;
    private ProgressBar gamePackagesProgress;
    private ProgressBar othersProgress;

    private FCLImageView lwjglState;
    private FCLImageView cacioState;
    private FCLImageView cacio11State;
    private FCLImageView cacio17State;
    private FCLImageView java8State;
    private FCLImageView java11State;
    private FCLImageView java17State;
    private FCLImageView java21State;
    private FCLImageView gamePackagesState;
    private FCLImageView othersState;

    private FCLButton install;

    private SharedPreferences.Editor edit = FCLApplication.appDataSave.edit();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_runtime, container, false);

        lwjglProgress = findViewById(view, R.id.lwjgl_progress);
        cacioProgress = findViewById(view, R.id.cacio_progress);
        cacio11Progress = findViewById(view, R.id.cacio11_progress);
        cacio17Progress = findViewById(view, R.id.cacio17_progress);
        java8Progress = findViewById(view, R.id.java8_progress);
        java11Progress = findViewById(view, R.id.java11_progress);
        java17Progress = findViewById(view, R.id.java17_progress);
        java21Progress = findViewById(view, R.id.java21_progress);
        gamePackagesProgress = findViewById(view, R.id.game_packages_progress);
        othersProgress = findViewById(view, R.id.others_progress);

        lwjglState = findViewById(view, R.id.lwjgl_state);
        cacioState = findViewById(view, R.id.cacio_state);
        cacio11State = findViewById(view, R.id.cacio11_state);
        cacio17State = findViewById(view, R.id.cacio17_state);
        java8State = findViewById(view, R.id.java8_state);
        java11State = findViewById(view, R.id.java11_state);
        java17State = findViewById(view, R.id.java17_state);
        java21State = findViewById(view, R.id.java21_state);
        gamePackagesState = findViewById(view, R.id.game_packages_state);
        othersState = findViewById(view, R.id.others_state);

        initState();

        refreshDrawables();

        check();

        install = findViewById(view, R.id.install);
        install.setOnClickListener(this);

        return view;
    }

    private void initState() {
        try {
            lwjgl = RuntimeUtils.isLatest(FCLPath.LWJGL_DIR, "/assets/app_runtime/lwjgl");
            cacio = RuntimeUtils.isLatest(FCLPath.CACIOCAVALLO_8_DIR, "/assets/app_runtime/caciocavallo");
            cacio11 = RuntimeUtils.isLatest(FCLPath.CACIOCAVALLO_11_DIR, "/assets/app_runtime/caciocavallo11");
            cacio17 = RuntimeUtils.isLatest(FCLPath.CACIOCAVALLO_17_DIR, "/assets/app_runtime/caciocavallo17");
            java8 = RuntimeUtils.isLatest(FCLPath.JAVA_8_PATH, "/assets/app_runtime/java/jre8");
            java11 = RuntimeUtils.isLatest(FCLPath.JAVA_11_PATH, "/assets/app_runtime/java/jre11");
            java17 = RuntimeUtils.isLatest(FCLPath.JAVA_17_PATH, "/assets/app_runtime/java/jre17");
            java21 = RuntimeUtils.isLatest(FCLPath.JAVA_21_PATH, "/assets/app_runtime/java/jre21");
            gamePackages = RuntimeUtils.isLatest(FCLPath.SHARED_COMMON_DIR, "/assets/.minecraft") && FCLApplication.appDataSave.getBoolean("gameDataExportSuccessful",false);
            others = RuntimeUtils.isLatest(FCLPath.OTHERS_DIR, "/assets/others") && gamePackages;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshDrawables() {
        if (getContext() != null) {
            @SuppressLint("UseCompatLoadingForDrawables") Drawable stateUpdate = getContext().getDrawable(R.drawable.ic_baseline_update_24);
            @SuppressLint("UseCompatLoadingForDrawables") Drawable stateDone = getContext().getDrawable(R.drawable.ic_baseline_done_24);

            lwjglState.setBackgroundDrawable(lwjgl ? stateDone : stateUpdate);
            cacioState.setBackgroundDrawable(cacio ? stateDone : stateUpdate);
            cacio11State.setBackgroundDrawable(cacio11 ? stateDone : stateUpdate);
            cacio17State.setBackgroundDrawable(cacio17 ? stateDone : stateUpdate);
            java8State.setBackgroundDrawable(java8 ? stateDone : stateUpdate);
            java11State.setBackgroundDrawable(java11 ? stateDone : stateUpdate);
            java17State.setBackgroundDrawable(java17 ? stateDone : stateUpdate);
            java21State.setBackgroundDrawable(java21 ? stateDone : stateUpdate);
            gamePackagesState.setBackgroundDrawable(gamePackages ? stateDone : stateUpdate);
            othersState.setBackgroundDrawable(others ? stateDone : stateUpdate);
        }
    }

    private boolean isLatest() {
        return lwjgl && cacio && cacio11 && cacio17 && java8 && java11 && java17 && java21 && gamePackages && others;
    }

    private void check() {
        if (isLatest()) {
            if (getActivity() != null) {
                ((SplashActivity) getActivity()).enterLauncher();
            }
        }
    }

    private boolean installing = false;

    private void install() {
        if (installing)
            return;

        installing = true;
        if (!lwjgl) {
            lwjglState.setVisibility(View.GONE);
            lwjglProgress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    RuntimeUtils.install(getContext(), FCLPath.LWJGL_DIR, "app_runtime/lwjgl");
                    lwjgl = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        lwjglState.setVisibility(View.VISIBLE);
                        lwjglProgress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!cacio) {
            cacioState.setVisibility(View.GONE);
            cacioProgress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    RuntimeUtils.install(getContext(), FCLPath.CACIOCAVALLO_8_DIR, "app_runtime/caciocavallo");
                    cacio = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        cacioState.setVisibility(View.VISIBLE);
                        cacioProgress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!cacio11) {
            cacio11State.setVisibility(View.GONE);
            cacio11Progress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    RuntimeUtils.install(getContext(), FCLPath.CACIOCAVALLO_11_DIR, "app_runtime/caciocavallo11");
                    cacio11 = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        cacio11State.setVisibility(View.VISIBLE);
                        cacio11Progress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!cacio17) {
            cacio17State.setVisibility(View.GONE);
            cacio17Progress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    RuntimeUtils.install(getContext(), FCLPath.CACIOCAVALLO_17_DIR, "app_runtime/caciocavallo17");
                    cacio17 = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        cacio17State.setVisibility(View.VISIBLE);
                        cacio17Progress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!java8) {
            java8State.setVisibility(View.GONE);
            java8Progress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    RuntimeUtils.installJava(getContext(), FCLPath.JAVA_8_PATH, "app_runtime/java/jre8");
                    java8 = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        java8State.setVisibility(View.VISIBLE);
                        java8Progress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!java11) {
            java11State.setVisibility(View.GONE);
            java11Progress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    RuntimeUtils.installJava(getContext(), FCLPath.JAVA_11_PATH, "app_runtime/java/jre11");
                    FileUtils.writeText(new File(FCLPath.JAVA_11_PATH + "/resolv.conf"), "nameserver " + FCLApplication.appConfig.getProperty("primary-nameserver","223.5.5.5") + "\nnameserver " + FCLApplication.appConfig.getProperty("secondary-nameserver","8.8.8.8"));
                    java11 = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        java11State.setVisibility(View.VISIBLE);
                        java11Progress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!java17) {
            java17State.setVisibility(View.GONE);
            java17Progress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    RuntimeUtils.installJava(getContext(), FCLPath.JAVA_17_PATH, "app_runtime/java/jre17");
                    FileUtils.writeText(new File(FCLPath.JAVA_17_PATH + "/resolv.conf"), "nameserver " + FCLApplication.appConfig.getProperty("primary-nameserver","223.5.5.5") + "\nnameserver " + FCLApplication.appConfig.getProperty("secondary-nameserver","8.8.8.8"));
                    java17 = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        java17State.setVisibility(View.VISIBLE);
                        java17Progress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!java21) {
            java21State.setVisibility(View.GONE);
            java21Progress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    RuntimeUtils.installJava(getContext(), FCLPath.JAVA_21_PATH, "app_runtime/java/jre21");
                    FileUtils.writeText(new File(FCLPath.JAVA_21_PATH + "/resolv.conf"), "nameserver " + FCLApplication.appConfig.getProperty("primary-nameserver","223.5.5.5") + "\nnameserver " + FCLApplication.appConfig.getProperty("secondary-nameserver","8.8.8.8"));
                    java21 = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        java21State.setVisibility(View.VISIBLE);
                        java21Progress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!gamePackages) {
            gamePackagesState.setVisibility(View.GONE);
            gamePackagesProgress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                RuntimeUtils.delete(FCLPath.SHARED_COMMON_DIR);
                RuntimeUtils.copyAssetsDirToLocalDir(getContext(), ".minecraft", FCLPath.SHARED_COMMON_DIR);
                gamePackages = true;
                edit.putBoolean("gameDataExportSuccessful",true);
                edit.apply();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        gamePackagesState.setVisibility(View.VISIBLE);
                        gamePackagesProgress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
        if (!others) {
            othersState.setVisibility(View.GONE);
            othersProgress.setVisibility(View.VISIBLE);
            new Thread(() -> {
                RuntimeUtils.delete(getActivity().getFilesDir().getAbsolutePath() + "/menu_setting.json");
                RuntimeUtils.delete(getActivity().getFilesDir().getAbsolutePath() + "/config.json");

                new ParseAuthlibInjectorServerFile(this,"authlib-injector-server.json").parseFileAndConvert();
                if("false".equals(FCLApplication.appConfig.getProperty("download-authlib-injector-online","true"))){
                    RuntimeUtils.copyAssetsFileToLocalDir(getContext(), "others", FCLPath.PLUGIN_DIR);
                }

                RuntimeUtils.copyAssetsFileToLocalDir(getContext(), "settings", FCLPath.FILES_DIR + "/..");

                RuntimeUtils.copyAssetsFileToLocalDir(getContext(), "others/version", FCLPath.OTHERS_DIR + "/version");
                others = true;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        othersState.setVisibility(View.VISIBLE);
                        othersProgress.setVisibility(View.GONE);
                        refreshDrawables();
                        check();
                    });
                }
            }).start();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == install) {
            install();
        }
    }
}