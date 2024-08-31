package com.tungsten.fcl.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.tungsten.fcl.FCLApplication;
import com.tungsten.fcl.R;
import com.tungsten.fcl.game.TexturesLoader;
import com.tungsten.fcl.setting.Accounts;
import com.tungsten.fcl.util.AndroidUtils;
import com.tungsten.fclcore.auth.Account;
import com.tungsten.fclcore.fakefx.beans.property.ObjectProperty;
import com.tungsten.fclcore.fakefx.beans.property.SimpleObjectProperty;
import com.tungsten.fclcore.task.Schedulers;
import com.tungsten.fclcore.task.Task;
import com.tungsten.fclcore.util.Logging;
import com.tungsten.fclcore.util.io.HttpRequest;
import com.tungsten.fclcore.util.io.NetworkUtils;
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog;
import com.tungsten.fcllibrary.component.theme.ThemeEngine;
import com.tungsten.fcllibrary.component.ui.FCLCommonUI;
import com.tungsten.fcllibrary.component.view.FCLButton;
import com.tungsten.fcllibrary.component.view.FCLTextView;
import com.tungsten.fcllibrary.component.view.FCLUILayout;
import com.tungsten.fcllibrary.skin.SkinCanvas;
import com.tungsten.fcllibrary.skin.SkinRenderer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MainUI extends FCLCommonUI implements View.OnClickListener {
  
    public static final String ANNOUNCEMENT_URL = FCLApplication.appConfig.getProperty("announcement-url","https://raw.githubusercontent.com/hyplant/FoldCraftLauncher/doc/announcement/latest.json");

    private LinearLayoutCompat announcementContainer;
    private LinearLayoutCompat announcementLayout;
    private FCLTextView title;
    private FCLTextView announcementView;
    private FCLTextView date;
    private FCLButton hide;
    private Announcement announcement = null;

    private RelativeLayout skinContainer;
    private SkinCanvas skinCanvas;
    private SkinRenderer renderer;

    private ObjectProperty<Account> currentAccount;

    public MainUI(Context context, FCLUILayout parent, int id) {
        super(context, parent, id);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        announcementContainer = findViewById(R.id.announcement_container);
        announcementLayout = findViewById(R.id.announcement_layout);
        title = findViewById(R.id.title);
        announcementView = findViewById(R.id.announcement);
        date = findViewById(R.id.date);
        hide = findViewById(R.id.hide);
        ThemeEngine.getInstance().registerEvent(announcementLayout, () -> announcementLayout.getBackground().setTint(ThemeEngine.getInstance().getTheme().getColor()));
        hide.setOnClickListener(this);

        skinContainer = findViewById(R.id.skin_container);
        renderer = new SkinRenderer(getContext());
        ViewGroup.LayoutParams layoutParamsSkin = skinContainer.getLayoutParams();
        layoutParamsSkin.width = (int) (((View) skinContainer.getParent().getParent()).getMeasuredWidth() * 0.5f);
        layoutParamsSkin.height = (int) Math.min(((View) skinContainer.getParent().getParent()).getMeasuredWidth() * 0.5f, ((View) skinContainer.getParent().getParent()).getMeasuredHeight());
        skinContainer.setLayoutParams(layoutParamsSkin);

        checkAnnouncement();

        setupSkinDisplay();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (skinCanvas == null) {
            skinCanvas = new SkinCanvas(getContext());
            skinCanvas.setRenderer(renderer, 5f);
        } else {
            skinCanvas.onResume();
            renderer.updateTexture(renderer.getTexture()[0], renderer.getTexture()[1]);
        }
        skinContainer.addView(skinCanvas);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (skinCanvas != null) {
            skinCanvas.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShowing() && skinCanvas != null) {
            skinCanvas.onResume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (skinCanvas != null) {
            skinCanvas.onPause();
        }
        skinContainer.removeView(skinCanvas);
    }

    @Override
    public Task<?> refresh(Object... param) {
        return Task.runAsync(() -> {

        });
    }

    private void checkAnnouncement() {
        announcementContainer.setVisibility(View.INVISIBLE);
        if(FCLApplication.appConfig.getProperty("enable-announcement","true").equals("true")){
            @SuppressLint("SimpleDateFormat") CompletableFuture<Announcement> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return new Gson().fromJson(NetworkUtils.doGet(NetworkUtils.toURL(ANNOUNCEMENT_URL), FCLApplication.deviceInfoUtils.toString()), Announcement.class);
                }catch (Exception e) {
                    return new Announcement(
                            -1,
                            true,
                            false,
                            -1,
                            -1,
                            new ArrayList<>(Collections.singletonList("")),
                            new ArrayList<>(Collections.singletonList(new Announcement.Content("en", getContext().getString(R.string.announcement_error)))),
                            new SimpleDateFormat("yyyy.MM.dd").format(new Date()),
                            new ArrayList<>(Collections.singletonList(new Announcement.Content("en", getContext().getString(R.string.announcement_failure))))
                    );
                }
            });
            future.thenAccept(announcement -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    this.announcement = announcement;
                    if (!announcement.shouldDisplay(getContext()))
                        return;
                    title.setText(this.announcement.getDisplayTitle(getContext()));
                    announcementView.setText(this.announcement.getDisplayContent(getContext()));
                    date.setText(this.announcement.getDate());
                    announcementContainer.setVisibility(View.VISIBLE);
                });
            });
        }
    }

    private void hideAnnouncement() {
        announcementContainer.setVisibility(View.GONE);
        if (announcement != null) {
            announcement.hide(getContext());
        }
    }

    private void setupSkinDisplay() {
        currentAccount = new SimpleObjectProperty<Account>() {

            @Override
            protected void invalidated() {
                Account account = get();
                renderer.textureProperty().unbind();
                if (account == null) {
                    renderer.updateTexture(BitmapFactory.decodeStream(MainUI.class.getResourceAsStream("/assets/img/alex.png")), null);
                } else {
                    renderer.textureProperty().bind(TexturesLoader.textureBinding(account));
                }
            }
        };
        currentAccount.bind(Accounts.selectedAccountProperty());
    }

    public void refreshSkin(Account account) {
        Schedulers.androidUIThread().execute(() -> {
            if (currentAccount.get() == account) {
                renderer.textureProperty().unbind();
                renderer.textureProperty().bind(TexturesLoader.textureBinding(currentAccount.get()));
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == hide) {
            if (announcement != null && announcement.isSignificant()) {
                FCLAlertDialog.Builder builder = new FCLAlertDialog.Builder(getContext());
                builder.setAlertLevel(FCLAlertDialog.AlertLevel.ALERT);
                builder.setCancelable(false);
                builder.setMessage(getContext().getString(R.string.announcement_significant));
                builder.setPositiveButton(null);
                builder.setNegativeButton(null);
                builder.create().show();
            } else {
                hideAnnouncement();
            }
        }
    }
}
