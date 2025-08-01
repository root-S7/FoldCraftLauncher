package com.tungsten.fcl.ui.manage;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.mio.data.Renderer;
import com.mio.manager.RendererManager;
import com.mio.ui.dialog.DriverSelectDialog;
import com.mio.ui.dialog.JavaManageDialog;
import com.mio.ui.dialog.RendererSelectDialog;
import com.tungsten.fcl.R;
import com.tungsten.fcl.activity.MainActivity;
import com.tungsten.fcl.control.SelectControllerDialog;
import com.tungsten.fcl.game.FCLGameRepository;
import com.tungsten.fcl.setting.Controllers;
import com.tungsten.fcl.setting.Profile;
import com.tungsten.fcl.setting.Profiles;
import com.tungsten.fcl.setting.VersionSetting;
import com.tungsten.fcl.ui.UIManager;
import com.tungsten.fcl.ui.controller.ControllerPageManager;
import com.tungsten.fcl.util.AndroidUtils;
import com.tungsten.fcl.util.FXUtils;
import com.tungsten.fcl.util.RequestCodes;
import com.tungsten.fcl.util.WeakListenerHolder;
import com.tungsten.fclauncher.FCLConfig;
import com.tungsten.fclauncher.plugins.DriverPlugin;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.event.Event;
import com.tungsten.fclcore.fakefx.beans.InvalidationListener;
import com.tungsten.fclcore.fakefx.beans.binding.Bindings;
import com.tungsten.fclcore.fakefx.beans.property.BooleanProperty;
import com.tungsten.fclcore.fakefx.beans.property.IntegerProperty;
import com.tungsten.fclcore.fakefx.beans.property.SimpleBooleanProperty;
import com.tungsten.fclcore.fakefx.beans.property.SimpleIntegerProperty;
import com.tungsten.fclcore.fakefx.beans.property.SimpleStringProperty;
import com.tungsten.fclcore.fakefx.beans.property.StringProperty;
import com.tungsten.fclcore.task.Schedulers;
import com.tungsten.fclcore.task.Task;
import com.tungsten.fclcore.util.Lang;
import com.tungsten.fclcore.util.Logging;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fclcore.util.platform.MemoryUtils;
import com.tungsten.fcllibrary.browser.FileBrowser;
import com.tungsten.fcllibrary.browser.options.LibMode;
import com.tungsten.fcllibrary.browser.options.SelectionMode;
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog;
import com.tungsten.fcllibrary.component.ui.FCLCommonPage;
import com.tungsten.fcllibrary.component.view.FCLCheckBox;
import com.tungsten.fcllibrary.component.view.FCLEditText;
import com.tungsten.fcllibrary.component.view.FCLImageButton;
import com.tungsten.fcllibrary.component.view.FCLImageView;
import com.tungsten.fcllibrary.component.view.FCLLinearLayout;
import com.tungsten.fcllibrary.component.view.FCLNumberSeekBar;
import com.tungsten.fcllibrary.component.view.FCLProgressBar;
import com.tungsten.fcllibrary.component.view.FCLSwitch;
import com.tungsten.fcllibrary.component.view.FCLTextView;
import com.tungsten.fcllibrary.component.view.FCLUILayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

public class VersionSettingPage extends FCLCommonPage implements ManageUI.VersionLoadable, View.OnClickListener {

    private final boolean globalSetting;

    private VersionSetting lastVersionSetting = null;
    private Profile profile;
    private WeakListenerHolder listenerHolder;
    private String versionId;

    private FCLEditText txtJVMArgs;
    private FCLEditText txtGameArgs;
    private FCLEditText txtMetaspace;
    private FCLEditText txtServerIP;

    private FCLCheckBox chkAutoAllocate;

    private FCLImageView iconView;

    private FCLNumberSeekBar allocateSeekbar;
    private FCLNumberSeekBar scaleFactorSeekbar;

    private FCLSwitch isolateWorkingDirSwitch;
    private FCLSwitch beGestureSwitch;
    private FCLSwitch vulkanDriverSystemSwitch;
    private FCLSwitch pojavBigCoreSwitch;
    private FCLSwitch noGameCheckSwitch;
    private FCLSwitch noJVMCheckSwitch;

    private FCLImageButton javaButton;
    private FCLImageButton javaInstallButton;
    private FCLImageButton editIconButton;
    private FCLImageButton deleteIconButton;
    private FCLImageButton controllerButton;
    private FCLImageButton controllerInstallButton;
    private FCLImageButton rendererButton;
    private FCLImageButton rendererInstallButton;
    private FCLImageButton driverButton;
    private FCLImageButton driverInstallButton;

    private FCLTextView javaText;
    private FCLTextView controllerText;
    private FCLTextView rendererText;
    private FCLTextView driverText;

    private final InvalidationListener specificSettingsListener;
    private final StringProperty selectedVersion = new SimpleStringProperty();
    private final BooleanProperty enableSpecificSettings = new SimpleBooleanProperty(false);
    private final IntegerProperty maxMemory = new SimpleIntegerProperty();
    private final IntegerProperty usedMemory = new SimpleIntegerProperty(0);
    private final BooleanProperty modpack = new SimpleBooleanProperty();

    public VersionSettingPage(Context context, int id, FCLUILayout parent, int resId, boolean globalSetting) {
        super(context, id, parent, resId);
        this.globalSetting = globalSetting;
        create();
        specificSettingsListener = any -> enableSpecificSettings.set(!lastVersionSetting.isUsesGlobal());
    }

    private void create() {
        FCLLinearLayout settingTypeLayout = findViewById(R.id.special_setting_layout);
        FCLLinearLayout settingLayout = findViewById(R.id.setting_layout);

        txtJVMArgs = findViewById(R.id.edit_jvm_args);
        txtGameArgs = findViewById(R.id.edit_minecraft_args);
        txtMetaspace = findViewById(R.id.edit_permgen_space);
        txtServerIP = findViewById(R.id.edit_server);

        chkAutoAllocate = findViewById(R.id.edit_auto_allocate);

        iconView = findViewById(R.id.icon);

        allocateSeekbar = findViewById(R.id.edit_memory);
        scaleFactorSeekbar = findViewById(R.id.edit_scale_factor);

        FCLSwitch specialSettingSwitch = findViewById(R.id.enable_per_instance_setting);
        specialSettingSwitch.addCheckedChangeListener();
        isolateWorkingDirSwitch = findViewById(R.id.edit_game_dir);
        beGestureSwitch = findViewById(R.id.edit_controller_injector);
        vulkanDriverSystemSwitch = findViewById(R.id.vulkan_driver_system);
        pojavBigCoreSwitch = findViewById(R.id.pojav_big_core);
        noGameCheckSwitch = findViewById(R.id.edit_not_check_game);
        noJVMCheckSwitch = findViewById(R.id.edit_not_check_java);

        isolateWorkingDirSwitch.disableProperty().bind(modpack);
        scaleFactorSeekbar.addProgressListener();

        javaButton = findViewById(R.id.edit_java);
        javaInstallButton = findViewById(R.id.install_java);
        editIconButton = findViewById(R.id.edit_icon);
        deleteIconButton = findViewById(R.id.delete_icon);
        controllerButton = findViewById(R.id.edit_controller);
        controllerInstallButton = findViewById(R.id.install_controller);
        rendererButton = findViewById(R.id.edit_renderer);
        rendererInstallButton = findViewById(R.id.install_renderer);
        driverButton = findViewById(R.id.edit_driver);
        driverInstallButton = findViewById(R.id.install_driver);

        javaButton.setOnClickListener(this);
        javaInstallButton.setOnClickListener(this);
        editIconButton.setOnClickListener(this);
        deleteIconButton.setOnClickListener(this);
        controllerButton.setOnClickListener(this);
        controllerInstallButton.setOnClickListener(this);
        rendererButton.setOnClickListener(this);
        rendererInstallButton.setOnClickListener(this);
        driverButton.setOnClickListener(this);
        driverInstallButton.setOnClickListener(this);

        javaText = findViewById(R.id.java);
        controllerText = findViewById(R.id.controller);
        rendererText = findViewById(R.id.renderer);
        driverText = findViewById(R.id.driver);

        FCLProgressBar memoryBar = findViewById(R.id.memory_bar);

        FCLTextView memoryStateText = findViewById(R.id.memory_state);
        FCLTextView memoryInfoText = findViewById(R.id.memory_info_text);
        FCLTextView memoryAllocateText = findViewById(R.id.memory_allocate_text);

        memoryStateText.stringProperty().bind(Bindings.createStringBinding(() -> {
            if (chkAutoAllocate.isChecked()) {
                return getContext().getString(R.string.settings_memory_lower_bound);
            } else {
                return getContext().getString(R.string.settings_memory);
            }
        }, chkAutoAllocate.checkProperty()));

        allocateSeekbar.setMax(MemoryUtils.getTotalDeviceMemory(getContext()));
        memoryBar.setMax(MemoryUtils.getTotalDeviceMemory(getContext()));

        allocateSeekbar.addProgressListener();
        allocateSeekbar.progressProperty().bindBidirectional(maxMemory);

        memoryBar.firstProgressProperty().bind(usedMemory);
        memoryBar.secondProgressProperty().bind(Bindings.createIntegerBinding(() -> {
            int allocate = (int) (FCLGameRepository.getAllocatedMemory(maxMemory.intValue() * 1024L * 1024L, MemoryUtils.getFreeDeviceMemory(getContext()) * 1024L * 1024L, chkAutoAllocate.isChecked()) / 1024. / 1024);
            return usedMemory.intValue() + (chkAutoAllocate.isChecked() ? allocate : maxMemory.intValue());
        }, usedMemory, maxMemory, chkAutoAllocate.checkProperty()));

        memoryInfoText.stringProperty().bind(Bindings.createStringBinding(() -> AndroidUtils.getLocalizedText(getContext(), "settings_memory_used_per_total", MemoryUtils.getUsedDeviceMemory(getContext()) / 1024., MemoryUtils.getTotalDeviceMemory(getContext()) / 1024.), usedMemory));

        memoryAllocateText.stringProperty().bind(Bindings.createStringBinding(() -> {
            long maxMemory = Lang.parseInt(this.maxMemory.get(), 0) * 1024L * 1024L;
            return AndroidUtils.getLocalizedText(getContext(), maxMemory / 1024. / 1024. > MemoryUtils.getFreeDeviceMemory(getContext())
                            ? (chkAutoAllocate.isChecked() ? "settings_memory_allocate_auto_exceeded" : "settings_memory_allocate_manual_exceeded")
                            : (chkAutoAllocate.isChecked() ? "settings_memory_allocate_auto" : "settings_memory_allocate_manual"),
                    maxMemory / 1024. / 1024. / 1024.,
                    FCLGameRepository.getAllocatedMemory(maxMemory, MemoryUtils.getFreeDeviceMemory(getContext()) * 1024L * 1024L, chkAutoAllocate.isChecked()) / 1024. / 1024. / 1024.,
                    MemoryUtils.getFreeDeviceMemory(getContext()) / 1024.);
        }, usedMemory, maxMemory, chkAutoAllocate.checkProperty()));

        settingTypeLayout.setVisibility(globalSetting ? View.GONE : View.VISIBLE);

        if (!globalSetting) {
            specialSettingSwitch.disableProperty().bind(modpack);
            specialSettingSwitch.checkProperty().bindBidirectional(enableSpecificSettings);
            settingLayout.visibilityProperty().bind(enableSpecificSettings);
        }

        enableSpecificSettings.addListener((a, b, newValue) -> {
            if (versionId == null) return;

            // do not call versionSettings.setUsesGlobal(true/false)
            // because versionSettings can be the global one.
            // global versionSettings.usesGlobal is always true.
            if (newValue)
                profile.getRepository().specializeVersionSetting(versionId);
            else
                profile.getRepository().globalizeVersionSetting(versionId);

            Schedulers.androidUIThread().execute(() -> loadVersion(profile, versionId));
        });
        vulkanDriverSystemSwitch.setOnClickListener(v -> {
            if (vulkanDriverSystemSwitch.checkProperty().get() && AndroidUtils.isAdrenoGPU()) {
                FCLAlertDialog.Builder builder = new FCLAlertDialog.Builder(getContext());
                builder.setAlertLevel(FCLAlertDialog.AlertLevel.INFO);
                builder.setMessage(getContext().getString(R.string.message_vulkan_driver_system));
                builder.setNegativeButton(getContext().getString(com.tungsten.fcllibrary.R.string.dialog_positive), null);
                builder.create().show();
            }
        });
    }

    @Override
    public Task<?> refresh(Object... param) {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        usedMemory.set(MemoryUtils.getUsedDeviceMemory(getContext()));
    }

    @Override
    public void loadVersion(Profile profile, String versionId) {
        this.profile = profile;
        this.versionId = versionId;
        this.listenerHolder = new WeakListenerHolder();

        if (versionId == null) {
            enableSpecificSettings.set(true);
            listenerHolder.add(FXUtils.onWeakChangeAndOperate(profile.selectedVersionProperty(), this.selectedVersion::setValue));
        }

        VersionSetting versionSetting = profile.getVersionSetting(versionId);
        versionSetting.checkController();

        modpack.set(versionId != null && profile.getRepository().isModpack(versionId));
        usedMemory.set(MemoryUtils.getUsedDeviceMemory(getContext()));

        InvalidationListener listener = observable -> ManagePageManager.getInstance().onRunDirectoryChange(profile, versionId);

        // unbind data fields
        if (lastVersionSetting != null) {
            lastVersionSetting.getIsolateGameDirProperty().removeListener(listener);
            FXUtils.unbind(txtJVMArgs, lastVersionSetting.getJavaArgsProperty());
            FXUtils.unbind(txtGameArgs, lastVersionSetting.getMinecraftArgsProperty());
            FXUtils.unbind(txtMetaspace, lastVersionSetting.getPermSizeProperty());
            FXUtils.unbind(txtServerIP, lastVersionSetting.getServerIpProperty());
            FXUtils.unbindBoolean(chkAutoAllocate, lastVersionSetting.getAutoMemoryProperty());
            FXUtils.unbindBoolean(isolateWorkingDirSwitch, lastVersionSetting.getIsolateGameDirProperty());
            FXUtils.unbindBoolean(pojavBigCoreSwitch, lastVersionSetting.getPojavBigCoreProperty());
            FXUtils.unbindBoolean(noGameCheckSwitch, lastVersionSetting.getNotCheckGameProperty());
            FXUtils.unbindBoolean(noJVMCheckSwitch, lastVersionSetting.getNotCheckJVMProperty());
            FXUtils.unbindBoolean(beGestureSwitch, lastVersionSetting.getBeGestureProperty());
            FXUtils.unbindBoolean(vulkanDriverSystemSwitch, lastVersionSetting.getVkDriverSystemProperty());
            scaleFactorSeekbar.progressProperty().unbindBidirectional(lastVersionSetting.getScaleFactorProperty());
            maxMemory.unbindBidirectional(lastVersionSetting.getMaxMemoryProperty());

            lastVersionSetting.getUsesGlobalProperty().removeListener(specificSettingsListener);
        }

        // bind new data fields
        if (getId() == ManagePageManager.PAGE_ID_MANAGE_SETTING) {
            versionSetting.getIsolateGameDirProperty().addListener(listener);
        }
        FXUtils.bindString(txtJVMArgs, versionSetting.getJavaArgsProperty());
        FXUtils.bindString(txtGameArgs, versionSetting.getMinecraftArgsProperty());
        FXUtils.bindString(txtMetaspace, versionSetting.getPermSizeProperty());
        FXUtils.bindString(txtServerIP, versionSetting.getServerIpProperty());
        FXUtils.bindBoolean(chkAutoAllocate, versionSetting.getAutoMemoryProperty());
        FXUtils.bindBoolean(isolateWorkingDirSwitch, versionSetting.getIsolateGameDirProperty());
        FXUtils.bindBoolean(pojavBigCoreSwitch, versionSetting.getPojavBigCoreProperty());
        FXUtils.bindBoolean(noGameCheckSwitch, versionSetting.getNotCheckGameProperty());
        FXUtils.bindBoolean(noJVMCheckSwitch, versionSetting.getNotCheckJVMProperty());
        FXUtils.bindBoolean(beGestureSwitch, versionSetting.getBeGestureProperty());
        FXUtils.bindBoolean(vulkanDriverSystemSwitch, versionSetting.getVkDriverSystemProperty());
        scaleFactorSeekbar.progressProperty().bindBidirectional(versionSetting.getScaleFactorProperty());
        maxMemory.bindBidirectional(versionSetting.getMaxMemoryProperty());

        javaText.setText(versionSetting.getJava().equals("Auto") ? getContext().getString(R.string.settings_game_java_version_auto) : versionSetting.getJava());
        Controllers.addCallback(() -> {
            controllerText.setText(Controllers.findControllerById(versionSetting.getController()).getName());
        });
        Renderer renderer = RendererManager.getRenderer(versionSetting.getRenderer());
        rendererText.setText(renderer.getDes());
        if (!versionSetting.getDriver().equals("Turnip")) {
            boolean isSelected = false;
            for (DriverPlugin.Driver driver : DriverPlugin.getDriverList()) {
                if (driver.getDriver().equals(versionSetting.getDriver())) {
                    DriverPlugin.setSelected(driver);
                    versionSetting.setDriver(driver.getDriver());
                    isSelected = true;
                }
            }
            if (!isSelected) {
                versionSetting.setDriver("Turnip");
            }
        }
        driverText.setText(versionSetting.getDriver());

        versionSetting.getUsesGlobalProperty().addListener(specificSettingsListener);
        if (versionId != null)
            enableSpecificSettings.set(!versionSetting.isUsesGlobal());

        lastVersionSetting = versionSetting;

        loadIcon();
    }

    private void onExploreIcon() {
        if (versionId == null)
            return;

        FileBrowser.Builder builder = new FileBrowser.Builder(getContext());
        ArrayList<String> suffix = new ArrayList<>();
        suffix.add(".png");
        builder.setLibMode(LibMode.FILE_CHOOSER);
        builder.setSelectionMode(SelectionMode.SINGLE_SELECTION);
        builder.setTitle(getContext().getString(R.string.settings_icon));
        builder.setSuffix(suffix);
        builder.create().browse(getActivity(), RequestCodes.SELECT_VERSION_ICON_CODE, (requestCode, resultCode, data) -> {
            if (requestCode == RequestCodes.SELECT_VERSION_ICON_CODE && resultCode == Activity.RESULT_OK && data != null) {
                if (FileBrowser.getSelectedFiles(data).size() == 0)
                    return;

                String path = FileBrowser.getSelectedFiles(data).get(0);
                Uri uri = Uri.parse(path);
                if (AndroidUtils.isDocUri(uri)) {
                    path = AndroidUtils.copyFileToDir(getActivity(), uri, new File(FCLPath.CACHE_DIR));
                }
                if (path == null)
                    return;

                File selectedFile = new File(path);
                File iconFile = profile.getRepository().getVersionIconFile(versionId);
                try {
                    FileUtils.copyFile(selectedFile, iconFile);

                    profile.getRepository().onVersionIconChanged.fireEvent(new Event(this));
                    loadIcon();
                } catch (IOException e) {
                    Logging.LOG.log(Level.SEVERE, "Failed to copy icon file from " + selectedFile + " to " + iconFile, e);
                }
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void onDeleteIcon() {
        if (versionId == null)
            return;

        File iconFile = profile.getRepository().getVersionIconFile(versionId);
        if (iconFile.exists())
            iconFile.delete();
        profile.getRepository().onVersionIconChanged.fireEvent(new Event(this));
        loadIcon();
    }

    private void loadIcon() {
        if (versionId == null) {
            return;
        }

        iconView.setImageDrawable(profile.getRepository().getVersionIconImage(versionId));
    }

    @Override
    public void onClick(View view) {
        if (view == editIconButton) {
            onExploreIcon();
        }
        if (view == deleteIconButton) {
            onDeleteIcon();
        }
        if (view == controllerButton) {
            SelectControllerDialog dialog = new SelectControllerDialog(getContext(), lastVersionSetting.getController(), controller -> {
                lastVersionSetting.setController(controller.getId());
                controllerText.setText(controller.getName());
            });
            dialog.show();
        }
        if (view == controllerInstallButton) {
            UIManager uiManager = MainActivity.getInstance().getUiManager();
            MainActivity.getInstance().binding.controller.setSelected(true);
            uiManager.getControllerUI().checkPageManager(() -> uiManager.getControllerUI().getPageManager().switchPage(ControllerPageManager.PAGE_ID_CONTROLLER_REPO));
        }
        if (view == javaButton) {
            new JavaManageDialog(getContext(), java -> {
                lastVersionSetting.setJava(java);
                if (java.equals("Auto")) {
                    javaText.setText(R.string.settings_game_java_version_auto);
                } else {
                    javaText.setText(java);
                }
                return null;
            }).show();
        }
        if (view == javaInstallButton) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.message_install_java)
                    .setItems(new String[]{"Github", getContext().getString(R.string.update_netdisk)}, (d, w) -> {
                        String url = null;
                        switch (w) {
                            case 0:
                                url = "https://github.com/FCL-Team/FoldCraftLauncher/releases/tag/java";
                                break;
                            case 1:
                                url = "https://pan.quark.cn/s/d1c9894545f9";
                                break;
                        }
                        if (url != null) {
                            AndroidUtils.openLink(getContext(), url);
                        }
                    })
                    .setPositiveButton(R.string.button_cancel, null)
                    .create()
                    .show();
        }
        if (view == rendererButton) {
            new RendererSelectDialog(getContext(), globalSetting, name -> {
                if (globalSetting && Profiles.getSelectedProfile().getVersionSetting() != null && !Profiles.getSelectedProfile().getVersionSetting().isGlobal()) {
                    FCLAlertDialog.Builder builder = new FCLAlertDialog.Builder(getContext());
                    builder.setAlertLevel(FCLAlertDialog.AlertLevel.INFO);
                    builder.setMessage(getContext().getString(R.string.message_warn_renderer_global_setting));
                    builder.setNegativeButton(getContext().getString(com.tungsten.fcllibrary.R.string.dialog_positive), null);
                    builder.create().show();
                }
                rendererText.setText(name);
            }).show();
        }
        if (view == driverButton) {
            new DriverSelectDialog(getContext(), globalSetting, name -> driverText.setText(name)).show();
        }
        if (view == rendererInstallButton) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.message_install_plugin)
                    .setItems(new String[]{"Github", getContext().getString(R.string.update_netdisk)}, (d, w) -> {
                        String url = null;
                        switch (w) {
                            case 0:
                                url = "https://github.com/ShirosakiMio/FCLRendererPlugin/releases/tag/Renderer";
                                break;
                            case 1:
                                url = "https://pan.quark.cn/s/a9f6e9d860d9";
                                break;
                        }
                        if (url != null) {
                            AndroidUtils.openLink(getContext(), url);
                        }
                    })
                    .setPositiveButton(R.string.button_cancel, null)
                    .create()
                    .show();
        }
        if (view == driverInstallButton) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.message_install_plugin)
                    .setItems(new String[]{"Github", getContext().getString(R.string.update_netdisk)}, (d, w) -> {
                        String url = null;
                        switch (w) {
                            case 0:
                                url = "https://github.com/FCL-Team/FCLDriverPlugin/releases/tag/Turnip";
                                break;
                            case 1:
                                url = "https://pan.quark.cn/s/d87c59695250";
                                break;
                        }
                        if (url != null) {
                            AndroidUtils.openLink(getContext(), url);
                        }
                    })
                    .setPositiveButton(R.string.button_cancel, null)
                    .create()
                    .show();
        }
    }
}
