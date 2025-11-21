/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2020  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tungsten.fcl.game;

import static android.content.Context.MODE_PRIVATE;
import static com.tungsten.fcl.util.AndroidUtils.getLocalizedText;
import static com.tungsten.fcl.util.AndroidUtils.hasStringId;
import static com.tungsten.fcl.util.AndroidUtils.openLink;
import static com.tungsten.fcl.util.RuleCheckState.isNormal;
import static com.tungsten.fclcore.util.Logging.LOG;
import static com.tungsten.fcllibrary.component.dialog.FCLAlertDialog.AlertLevel.ALERT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mio.JavaManager;
import com.mio.data.Renderer;
import com.mio.manager.RendererManager;
import com.mio.minecraft.ModCheckException;
import com.mio.minecraft.ModChecker;
import com.mio.util.ParseUtil;
import com.tungsten.fcl.FCLApplication;
import com.tungsten.fcl.R;
import com.tungsten.fcl.activity.JVMActivity;
import com.tungsten.fcl.activity.MainActivity;
import com.tungsten.fcl.control.MenuType;
import com.tungsten.fcl.setting.Profile;
import com.tungsten.fcl.setting.Profiles;
import com.tungsten.fcl.setting.VersionSetting;
import static com.tungsten.fcl.setting.rules.GameRulesManager.*;
import com.tungsten.fcl.setting.rules.extend.JavaRule;
import com.tungsten.fcl.setting.rules.extend.MemoryRule;
import com.tungsten.fcl.setting.rules.extend.RendererRule;
import com.tungsten.fcl.setting.rules.extend.VersionRule;
import com.tungsten.fcl.ui.TaskDialog;
import com.tungsten.fcl.util.RuleCheckState;
import com.tungsten.fcl.util.TaskCancellationAction;
import com.tungsten.fclauncher.bridge.FCLBridge;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.auth.Account;
import com.tungsten.fclcore.auth.AuthInfo;
import com.tungsten.fclcore.auth.AuthenticationException;
import com.tungsten.fclcore.auth.CharacterDeletedException;
import com.tungsten.fclcore.auth.CredentialExpiredException;
import com.tungsten.fclcore.auth.authlibinjector.AuthlibInjectorDownloadException;
import com.tungsten.fclcore.download.DefaultDependencyManager;
import com.tungsten.fclcore.download.MaintainTask;
import com.tungsten.fclcore.download.game.GameAssetIndexDownloadTask;
import com.tungsten.fclcore.download.game.GameVerificationFixTask;
import com.tungsten.fclcore.download.game.LibraryDownloadException;
import com.tungsten.fclcore.game.JavaVersion;
import com.tungsten.fclcore.game.LaunchOptions;
import com.tungsten.fclcore.game.Version;
import com.tungsten.fclcore.mod.LocalModFile;
import com.tungsten.fclcore.mod.ModpackCompletionException;
import com.tungsten.fclcore.mod.ModpackConfiguration;
import com.tungsten.fclcore.mod.ModpackProvider;
import com.tungsten.fclcore.mod.server.ServerModpackProvider;
import com.tungsten.fclcore.task.DownloadException;
import com.tungsten.fclcore.task.Schedulers;
import com.tungsten.fclcore.task.Task;
import com.tungsten.fclcore.task.TaskExecutor;
import com.tungsten.fclcore.task.TaskListener;
import com.tungsten.fclcore.util.Lang;
import com.tungsten.fclcore.util.LibFilter;
import com.tungsten.fclcore.util.Logging;
import com.tungsten.fclcore.util.StringUtils;
import com.tungsten.fclcore.util.io.ResponseCodeException;
import com.tungsten.fclcore.util.platform.MemoryUtils;
import com.tungsten.fclcore.util.versioning.VersionNumber;
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog;
import com.tungsten.fcllibrary.component.dialog.FCLDialog;
import com.tungsten.fcllibrary.component.view.FCLButton;

import org.lwjgl.glfw.CallbackBridge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public final class LauncherHelper {

    private final Context context;
    private final Profile profile;
    private final Account account;
    private final String selectedVersion;
    private final VersionSetting setting;
    private final TaskDialog launchingStepsPane;
    private final VersionRule rule;

    public LauncherHelper(Context context, Profile profile, Account account, String selectedVersion) {
        this.context = Objects.requireNonNull(context);
        this.profile = Objects.requireNonNull(profile);
        this.account = Objects.requireNonNull(account);
        this.selectedVersion = Objects.requireNonNull(selectedVersion);
        this.setting = profile.getVersionSetting(selectedVersion);
        this.launchingStepsPane = new TaskDialog(context, TaskCancellationAction.NORMAL);
        this.rule = fromJson(context).getVersionRule(selectedVersion);
        this.launchingStepsPane.setTitle(context.getString(R.string.version_launch));
    }

    public void launch() {
        LOG.info("Launching game version: " + selectedVersion);

        launchingStepsPane.show();
        launch0();
    }

    private void launch0() {
        FCLGameRepository repository = profile.getRepository();
        DefaultDependencyManager dependencyManager = profile.getDependency();
        AtomicReference<Version> version = new AtomicReference<>(MaintainTask.maintain(repository, repository.getResolvedVersion(selectedVersion)));
        Optional<String> gameVersion = repository.getGameVersion(version.get());
        boolean integrityCheck = repository.unmarkVersionLaunchedAbnormally(selectedVersion);
        List<String> javaAgents = new ArrayList<>(0);

        AtomicReference<JavaVersion> javaVersionRef = new AtomicReference<>();

        TaskExecutor executor = checkGameState(context, setting, version.get(), rule.getJava())
                .thenComposeAsync(javaVersion -> {
                    javaVersionRef.set(Objects.requireNonNull(javaVersion));
                    version.set(LibFilter.filter(version.get()));
                    if (setting.isNotCheckGame())
                        return null;
                    return Task.allOf(
                            dependencyManager.checkGameCompletionAsync(version.get(), integrityCheck),
                            Task.composeAsync(() -> {
                                try {
                                    ModpackConfiguration<?> configuration = ModpackHelper.readModpackConfiguration(repository.getModpackConfiguration(selectedVersion));
                                    ModpackProvider provider = ModpackHelper.getProviderByType(configuration.getType());
                                    if (provider == null)
                                        return null;
                                    else if (configuration.getType().equals(ServerModpackProvider.INSTANCE.getName()))
                                        return provider.createCompletionTask(dependencyManager, selectedVersion);
                                } catch (IOException ignore) {
                                }
                                return null;
                            }),
                            Task.composeAsync(() -> null)
                    );
                }).withStage("launch.state.dependencies")
                .thenComposeAsync(() -> checkHardware(context)).withStage("launch.state.device")
                .thenComposeAsync(() -> setGameRule(context, setting, rule)).withStage("launch.state.rule")
                .thenComposeAsync(() -> {
                    try (InputStream input = LauncherHelper.class.getResourceAsStream("/assets/game/MioLibPatcher.jar")) {
                        Files.copy(input, new File(FCLPath.LIB_PATCHER_PATH).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        Logging.LOG.log(Level.WARNING, "Unable to unpack MioLibFixer.jar", e);
                    }
                    return null;
                })
                .thenComposeAsync(() -> {
                    try (InputStream input = LauncherHelper.class.getResourceAsStream("/assets/game/MioLaunchWrapper.jar")) {
                        Files.copy(input, new File(FCLPath.MIO_LAUNCH_WRAPPER).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        Logging.LOG.log(Level.WARNING, "Unable to unpack MioLaunchWrapper.jar", e);
                    }
                    return null;
                })
                .thenComposeAsync(() -> gameVersion.map(s -> new GameVerificationFixTask(dependencyManager, s, version.get())).orElse(null))
                .thenComposeAsync(() -> logIn(context, account).withStage("launch.state.logging_in"))
                .thenComposeAsync(authInfo -> Task.supplyAsync(() -> {
                            LaunchOptions launchOptions = repository.getLaunchOptions(selectedVersion, javaVersionRef.get(), profile.getGameDir(), javaAgents);
                            FCLGameLauncher launcher = new FCLGameLauncher(
                                    context,
                                    repository,
                                    version.get(),
                                    authInfo,
                                    launchOptions
                            );
                            version.get().getLibraries().forEach(library -> {
                                if (library.getName().startsWith("net.java.dev.jna:jna:")) {
                                    launcher.setJnaVersion(library.getVersion());
                                }
                            });
                            return launcher;
                        }).thenComposeAsync(launcher -> { // launcher is prev task's result
                            return Task.supplyAsync(launcher::launch);
                        }).thenComposeAsync(fclBridge -> checkPathValid(fclBridge, repository))
                        .thenComposeAsync(fclBridge -> {
                            Renderer renderer = RendererManager.getRenderer(repository.getVersionSetting(selectedVersion).getRenderer());
                            fclBridge.setRenderer(renderer.getName());
                            return checkRenderer(fclBridge, renderer, repository.getGameVersion(selectedVersion).orElse(""));
                        }).thenComposeAsync(fclBridge -> {
                            boolean skip = repository.getVersionSetting(selectedVersion).isNotCheckMod();
                            return checkMod(fclBridge, repository.getGameVersion(selectedVersion).orElse(""), skip);
                        })
                        .thenAcceptAsync(fclBridge -> Schedulers.androidUIThread().execute(() -> {
                            CallbackBridge.nativeSetUseInputStackQueue(version.get().getArguments().isPresent());
                            Intent intent = new Intent(context, JVMActivity.class);
                            fclBridge.setScaleFactor(repository.getVersionSetting(selectedVersion).getScaleFactor() / 100.0);
                            fclBridge.setController(repository.getVersionSetting(selectedVersion).getController());
                            fclBridge.setGameDir(repository.getRunDirectory(selectedVersion).getAbsolutePath());
                            fclBridge.setJava(Integer.toString(javaVersionRef.get().getVersion()));
                            JVMActivity.setFCLBridge(fclBridge, MenuType.GAME);
                            Bundle bundle = new Bundle();
                            bundle.putString("controller", repository.getVersionSetting(selectedVersion).getController());
                            intent.putExtras(bundle);
                            LOG.log(Level.INFO, "Start JVMActivity!");
                            context.startActivity(intent);
                            if (MainActivity.getInstance().shouldPlayVideo()) {
                                MainActivity.getInstance().binding.videoView.stopPlayback();
                            }
                            if (context.getSharedPreferences("launcher", MODE_PRIVATE).getBoolean("autoExitLauncher", false)) {
                                Activity activity = FCLApplication.getCurrentActivity();
                                if (activity != null)
                                    activity.finish();
                            }
                        }))
                        .withStage("launch.state.waiting_launching"))
                .withStagesHint(Lang.immutableListOf(
                        "launch.state.java",
                        "launch.state.dependencies",
                        "launch.state.device",
                        "launch.state.rule",
                        "launch.state.logging_in",
                        "launch.state.waiting_launching"))
                .executor();
        launchingStepsPane.setExecutor(executor, false);
        executor.addTaskListener(new TaskListener() {

            @Override
            public void onStop(boolean success, TaskExecutor executor) {
                launchingStepsPane.dismiss();
                if (!success) {
                    Exception ex = executor.getException();
                    if (ex != null && !(ex instanceof CancellationException)) {
                        Schedulers.androidUIThread().execute(() -> {
                            String message;
                            if (ex instanceof ModpackCompletionException) {
                                if (ex.getCause() instanceof FileNotFoundException)
                                    message = getLocalizedText(context, "modpack_type_curse_not_found");
                                else
                                    message = getLocalizedText(context, "modpack_type_curse_error");
                            } else if (ex instanceof LibraryDownloadException) {
                                message = getLocalizedText(context, "launch_failed_download_library", ((LibraryDownloadException) ex).getLibrary().getName()) + "\n";
                                if (ex.getCause() instanceof ResponseCodeException) {
                                    ResponseCodeException rce = (ResponseCodeException) ex.getCause();
                                    int responseCode = rce.getResponseCode();
                                    URL url = rce.getUrl();
                                    if (responseCode == 404)
                                        message += getLocalizedText(context, "download_code_404", url);
                                    else
                                        message += getLocalizedText(context, "download_failed", url, responseCode);
                                } else {
                                    message += StringUtils.getStackTrace(ex.getCause());
                                }
                            } else if (ex instanceof DownloadException) {
                                URL url = ((DownloadException) ex).getUrl();
                                if (ex.getCause() instanceof SocketTimeoutException) {
                                    message = getLocalizedText(context, "install_failed_downloading_timeout", url);
                                } else if (ex.getCause() instanceof ResponseCodeException) {
                                    ResponseCodeException responseCodeException = (ResponseCodeException) ex.getCause();
                                    if (hasStringId(context, "download_code_" + responseCodeException.getResponseCode())) {
                                        message = getLocalizedText(context, "download_code_" + responseCodeException.getResponseCode(), url);
                                    } else {
                                        message = getLocalizedText(context, "install_failed_downloading_detail", url) + "\n" + StringUtils.getStackTrace(ex.getCause());
                                    }
                                } else {
                                    message = getLocalizedText(context, "install_failed_downloading_detail", url) + "\n" + StringUtils.getStackTrace(ex.getCause());
                                }
                            } else if (ex instanceof GameAssetIndexDownloadTask.GameAssetIndexMalformedException) {
                                message = getLocalizedText(context, "assets_index_malformed");
                            } else if (ex instanceof AuthlibInjectorDownloadException) {
                                message = getLocalizedText(context, "account_failed_injector_download_failure");
                            } else if (ex instanceof CharacterDeletedException) {
                                message = getLocalizedText(context, "account_failed_character_deleted");
                            } else if (ex instanceof ResponseCodeException) {
                                ResponseCodeException rce = (ResponseCodeException) ex;
                                int responseCode = rce.getResponseCode();
                                URL url = rce.getUrl();
                                if (responseCode == 404)
                                    message = getLocalizedText(context, "download_code_404", url);
                                else
                                    message = getLocalizedText(context, "download_failed", url, responseCode);
                            } else if (ex instanceof AccessDeniedException) {
                                message = getLocalizedText(context, "exception_access_denied", ((AccessDeniedException) ex).getFile());

                            } else if (ex instanceof RuntimeException) {
                                message = ex.getMessage();
                            } else if (ex instanceof ModCheckException) {
                                message = ((ModCheckException) ex).getReason();
                            } else {
                                if (ex == null) {
                                    message = "Task failed without exception!";
                                } else {
                                    message = StringUtils.getStackTrace(ex);
                                }
                            }

                            FCLAlertDialog.Builder builder = new FCLAlertDialog.Builder(context);
                            builder.setAlertLevel(ALERT);
                            builder.setCancelable(false);
                            builder.setTitle(context.getString(R.string.launch_failed));
                            builder.setMessage(message);
                            builder.setNegativeButton(context.getString(com.tungsten.fcllibrary.R.string.dialog_positive), null);
                            builder.setPositiveButton("关闭应用", () -> System.exit(0));
                            builder.create().show();
                        });
                    }
                }
            }
        });

        executor.start();
    }

    private Task<FCLBridge> checkPathValid(FCLBridge bridge, FCLGameRepository repository) {
        return Task.composeAsync(() -> {
            try {
                CompletableFuture<Task<FCLBridge>> future = new CompletableFuture<>();
                String path = repository.getVersionJar(selectedVersion).getAbsolutePath();
                if (ParseUtil.isValidCharacters(path)) {
                    return Task.completed(bridge);
                } else {
                    Schedulers.androidUIThread().execute(() -> new FCLAlertDialog.Builder(context)
                            .setCancelable(false)
                            .setMessage(context.getString(R.string.message_check_path_valid, path))
                            .setPositiveButton(context.getString(R.string.button_cancel), () -> future.completeExceptionally(new CancellationException()))
                            .setNegativeButton(context.getString(R.string.mod_check_continue), () -> future.complete(Task.completed(bridge))).create().show());
                    return Task.fromCompletableFuture(future).thenComposeAsync(task -> task);
                }
            } catch (Throwable e) {
                return Task.completed(bridge);
            }
        });
    }

    private Task<FCLBridge> checkRenderer(FCLBridge bridge, Renderer renderer, String version) {
        return Task.composeAsync(() -> {
            try {
                CompletableFuture<Task<FCLBridge>> future = new CompletableFuture<>();
                if (!version.isEmpty()) {
                    if (rule != null && rule.getRenderer() != null) return Task.completed(bridge);
                    if (!renderer.getMinMCver().isEmpty()) {
                        if (VersionNumber.compare(version, renderer.getMinMCver()) < 0) {
                            Schedulers.androidUIThread().execute(() -> new FCLAlertDialog.Builder(context)
                                    .setCancelable(false)
                                    .setMessage(context.getString(R.string.message_check_renderer, renderer.getName()))
                                    .setPositiveButton(context.getString(R.string.button_cancel), () -> future.completeExceptionally(new CancellationException()))
                                    .setNegativeButton(context.getString(R.string.mod_check_continue), () -> future.complete(Task.completed(bridge))).create().show());
                            return Task.fromCompletableFuture(future).thenComposeAsync(task -> task);
                        }
                    }
                    if (!renderer.getMaxMCver().isEmpty()) {
                        if (VersionNumber.compare(version, renderer.getMaxMCver()) > 0) {
                            Schedulers.androidUIThread().execute(() -> new FCLAlertDialog.Builder(context)
                                    .setCancelable(false)
                                    .setMessage(context.getString(R.string.message_check_renderer, renderer.getName()))
                                    .setPositiveButton(context.getString(R.string.button_cancel), () -> future.completeExceptionally(new CancellationException()))
                                    .setNegativeButton(context.getString(R.string.mod_check_continue), () -> future.complete(Task.completed(bridge))).create().show());
                            return Task.fromCompletableFuture(future).thenComposeAsync(task -> task);
                        }
                    }
                }
                return Task.completed(bridge);
            } catch (Throwable e) {
                LOG.log(Level.WARNING, "checkRenderer() failed", e);
                return Task.completed(bridge);
            }
        });
    }

    private Task<FCLBridge> checkMod(FCLBridge bridge, String version, boolean skip) {
        return Task.composeAsync(() -> {
            try {
                StringBuilder modCheckerInfo = new StringBuilder();
                StringBuilder modSummary = new StringBuilder();
                ModChecker modChecker = new ModChecker(context, version);
                int count = 0;
                for (LocalModFile mod : Profiles.getSelectedProfile().getRepository().getModManager(Profiles.getSelectedVersion()).getMods()) {
                    if (!mod.isActive()) {
                        continue;
                    }
                    modSummary.append(mod.getFileName());
                    modSummary.append(" | ");
                    modSummary.append(mod.getId());
                    modSummary.append(" | ");
                    modSummary.append(mod.getVersion());
                    modSummary.append(" | ");
                    modSummary.append(mod.getModLoaderType());
                    modSummary.append("\n");
                    try {
                        modChecker.check(bridge, mod);
                    } catch (ModCheckException e) {
                        count++;
                        modCheckerInfo.append(count).append(".").append(e.getReason()).append("\n\n");
                    }
                }
                bridge.setModSummary(modSummary.toString());
                if (!skip && !modCheckerInfo.toString().trim().isEmpty()) {
                    CompletableFuture<Task<FCLBridge>> future = new CompletableFuture<>();
                    Schedulers.androidUIThread().execute(() -> {
                        FCLAlertDialog.Builder builder = new FCLAlertDialog.Builder(context);
                        builder.setCancelable(false);
                        builder.setMessage(modCheckerInfo.toString());
                        builder.setPositiveButton(context.getString(R.string.button_cancel), () -> future.completeExceptionally(new CancellationException()));
                        builder.setNegativeButton(context.getString(R.string.mod_check_continue), () -> future.complete(Task.completed(bridge)));
                        builder.create().show();
                    });
                    return Task.fromCompletableFuture(future).thenComposeAsync(task -> task);
                }
                return Task.completed(bridge);
            } catch (Throwable e) {
                LOG.log(Level.WARNING, "CheckMod() failed", e);
                return Task.completed(bridge);
            }
        });
    }

    private static Task<JavaVersion> checkGameState(Context context, VersionSetting setting, Version version, JavaRule rule) {
        Task<JavaVersion> task = Task.composeAsync(() -> Task.supplyAsync(Schedulers.androidUIThread(), () -> {
            if (setting.getJava().equals("Auto")) {
                return JavaManager.getSuitableJavaVersion(version);
            } else {
                return JavaManager.getJavaFromVersionName(setting.getJava());
            }
        }));
        if (setting.isNotCheckJVM()) {
            return task.withStage("launch.state.java");
        }

        if (rule != null && rule.canDetectRule()) { // 如果规则可用，则根据规则要求进行设置Java
            return task.thenComposeAsync(javaVersion -> Optional.ofNullable(rule.setRule(setting))
                    .filter(RuleCheckState::isNormal)
                    .map(r -> Task.completed("Auto".equals(setting.getJava()) ? JavaManager.getSuitableJavaVersion(version) : JavaManager.getJavaFromVersionName(setting.getJava())))
                    .orElseGet(() -> {
                        CompletableFuture<JavaVersion> future = new CompletableFuture<>();
                        Schedulers.androidUIThread().execute(() -> errRuleDialog(context, rule.getTip(), rule.getDownloadURL(), future).create().show());
                        return Task.fromCompletableFuture(future);
                    })).withStage("launch.state.java");
        }else return task.thenComposeAsync(javaVersion -> Task.allOf(Task.completed(javaVersion), Task.supplyAsync(() -> JavaVersion.getSuitableJavaVersion(version))))
                .thenComposeAsync(Schedulers.androidUIThread(), javaVersions -> {
                    JavaVersion javaVersion = (JavaVersion) javaVersions.get(0);
                    JavaVersion suggestedJavaVersion = (JavaVersion) javaVersions.get(1);
                    if (setting.getJava().equals("Auto") || javaVersion.getVersion() == suggestedJavaVersion.getVersion()) {
                        return Task.completed(setting.getJava().equals("Auto") ? suggestedJavaVersion : javaVersion);
                    }

                    CompletableFuture<JavaVersion> future = new CompletableFuture<>();
                    Runnable continueAction = () -> future.complete(javaVersion);
                    new FCLAlertDialog.Builder(context)
                            .setCancelable(false)
                            .setMessage(context.getString(R.string.launch_error_java))
                            .setPositiveButton(context.getString(R.string.launch_error_java_auto), () -> {
                                setting.setJava(JavaVersion.JAVA_AUTO.getName());
                                future.complete(suggestedJavaVersion);
                            })
                            .setNegativeButton(context.getString(R.string.launch_error_java_continue), continueAction::run)
                            .setNeutralButton(context.getString(R.string.launch_error_java_continue_disable), () -> {
                                setting.setNotCheckJVM(true);
                                continueAction.run();
                            })
                            .create()
                            .show();
                    return Task.fromCompletableFuture(future);
                }).withStage("launch.state.java");
    }

    private static Task<AuthInfo> logIn(Context context, Account account) {
        return Task.composeAsync(() -> {
            try {
                return Task.completed(account.logIn());
            } catch (CredentialExpiredException e) {
                LOG.log(Level.INFO, "Credential has expired", e);

                CompletableFuture<Task<AuthInfo>> future = new CompletableFuture<>();
                Schedulers.androidUIThread().execute(() -> {
                    TipReLoginLoginDialog dialog = new TipReLoginLoginDialog(context, account, future);
                    dialog.show();
                });
                return Task.fromCompletableFuture(future).thenComposeAsync(task -> task);
            } catch (AuthenticationException e) {
                LOG.log(Level.WARNING, "Authentication failed, try skipping refresh", e);

                CompletableFuture<Task<AuthInfo>> future = new CompletableFuture<>();
                Schedulers.androidUIThread().execute(() -> {
                    SkipLoginDialog dialog = new SkipLoginDialog(context, account, future);
                    dialog.show();
                });
                return Task.fromCompletableFuture(future).thenComposeAsync(task -> task);
            }
        });
    }

    private static Task<Boolean> checkHardware(Context context) {
        return Task.composeAsync(() -> {
            String memoryRequirement = FCLPath.GENERAL_SETTING.getProperty("min-memory-requirement", "4");
            Float thisTotalMemory = (float) Math.ceil((double) MemoryUtils.getTotalDeviceMemory(context) / 1024);

            int comparisonResult;
            try {
                comparisonResult = Float.valueOf(memoryRequirement).compareTo(thisTotalMemory);
            } catch (Exception e) {
                comparisonResult = Float.valueOf(4).compareTo(thisTotalMemory);
            }

            if(comparisonResult <= 0) return Task.completed(true);
            else throw new RuntimeException(String.format(
                    "当前设备硬件配置不符合要求\n\n运行内存至少要%sGB，而你的设备只有%dGB\n\n\n\n注：设备上的虚拟内存不能算作运行内存！",
                    memoryRequirement,
                    thisTotalMemory.intValue()
            ));
        });
    }

    private static Task<Boolean> setGameRule(@NonNull Context context, VersionSetting setting, VersionRule rule) {
        return Task.composeAsync(() -> {
            if (rule == null) return Task.completed(true);

            try {
                MemoryRule memory = rule.getMemory();
                if(memory != null && !isNormal(memory.setRule(setting))) throw new RuleException(memory.getTip(), null);

                RendererRule renderer = rule.getRenderer();
                if(renderer != null && !isNormal(renderer.setRule(setting))) throw new RuleException(renderer.getTip(), renderer.getDownloadURL());

                return Task.completed(true);
            }catch(RuleException ex) {
                CompletableFuture<Task<Boolean>> future = new CompletableFuture<>();
                Schedulers.androidUIThread().execute(() -> errRuleDialog(context, ex.getMessage(), ex.getUrl(), future).create().show());
                return Task.fromCompletableFuture(future).thenComposeAsync(task -> task);
            }
        });
    }

    public static FCLAlertDialog.Builder errRuleDialog(@NonNull Context context, String msg, URL url, @NonNull CompletableFuture<?> future) {
        String tip = msg == null ? "当前设置规则不满足该版本要求，请根据提示修改！" : msg;

        FCLAlertDialog.Builder builder = new FCLAlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage(tip)
                .setAlertLevel(FCLAlertDialog.AlertLevel.ALERT)
                .setTitle("规则异常")
                .setNegativeButton(url != null ? "下载" : "确定", () -> {
                    if(url != null) openLink(context, url.toString());
                    future.completeExceptionally(new CancellationException(url != null ? "由于用户设置不满足规则，取消本次启动" : "用户强行终止了启动"));
                })
                .setPercentageSize(0.8f, 0.7f)
                .setMessageTextStyle(14f, true);

        if (url != null) builder.setPositiveButton("取消", () -> future.completeExceptionally(new CancellationException("用户强行终止了启动")));
        return builder;
    }

    static class SkipLoginDialog extends FCLDialog implements View.OnClickListener {

        private final Account account;
        private final CompletableFuture<Task<AuthInfo>> future;

        private FCLButton retry;
        private FCLButton skip;
        private FCLButton cancel;

        public SkipLoginDialog(@NonNull Context context, Account account, CompletableFuture<Task<AuthInfo>> future) {
            super(context);
            this.account = account;
            this.future = future;
            setContentView(R.layout.dialog_skip_login);
            setCancelable(false);

            retry = findViewById(R.id.retry);
            skip = findViewById(R.id.skip);
            cancel = findViewById(R.id.cancel);
            retry.setOnClickListener(this);
            skip.setOnClickListener(this);
            cancel.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == retry) {
                future.complete(logIn(getContext(), account));
            }
            if (view == skip) {
                try {
                    future.complete(Task.completed(account.playOffline()));
                } catch (AuthenticationException e2) {
                    future.completeExceptionally(e2);
                }
            }
            if (view == cancel) {
                future.completeExceptionally(new CancellationException());
            }
            dismiss();
        }
    }

    static class TipReLoginLoginDialog extends FCLDialog implements View.OnClickListener {

        private final Account account;
        private final CompletableFuture<Task<AuthInfo>> future;

        private FCLButton skip;
        private FCLButton ok;

        public TipReLoginLoginDialog(@NonNull Context context, Account account, CompletableFuture<Task<AuthInfo>> future) {
            super(context);
            this.account = account;
            this.future = future;
            setContentView(R.layout.dialog_tip_relogin);
            setCancelable(false);

            skip = findViewById(R.id.skip);
            ok = findViewById(R.id.ok);
            skip.setOnClickListener(this);
            ok.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == skip) {
                try {
                    future.complete(Task.completed(account.playOffline()));
                } catch (AuthenticationException e2) {
                    future.completeExceptionally(e2);
                }
            }
            if (view == ok) {
                future.completeExceptionally(new CancellationException());
            }
            dismiss();
        }
    }

}
