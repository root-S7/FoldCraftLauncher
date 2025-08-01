package com.tungsten.fcl.fragment

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.tungsten.fcl.R
import com.tungsten.fcl.activity.SplashActivity
import com.tungsten.fcl.databinding.FragmentRuntimeBinding
import com.tungsten.fcl.setting.ConfigHolder.config
import com.tungsten.fcl.setting.ConfigHolder.getSelectedPath
import com.tungsten.fcl.util.AndroidUtils.*
import com.tungsten.fcl.util.InstallResources
import com.tungsten.fcl.util.RuntimeUtils
import com.tungsten.fclauncher.utils.FCLPath
import com.tungsten.fclauncher.utils.FCLPath.DK_BACKGROUND_PATH
import com.tungsten.fclauncher.utils.FCLPath.LT_BACKGROUND_PATH
import com.tungsten.fclcore.task.Schedulers
import com.tungsten.fcllibrary.component.FCLFragment
import com.tungsten.fcllibrary.component.theme.ThemeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class RuntimeFragment : FCLFragment(), View.OnClickListener {
    private lateinit var bind: FragmentRuntimeBinding
    var gameFiles = false
    var configFiles = false
    var lwjgl = false
    var cacio = false
    var cacio11 = false
    var cacio17 = false
    var java8 = false
    var java11 = false
    var java17 = false
    var java21 = false
    var jna = false

    private val sharedPreferences = FCLPath.CONTEXT.getSharedPreferences("launcher", MODE_PRIVATE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_runtime, container, false)
        bind = FragmentRuntimeBinding.bind(view)
        bind.install.setOnClickListener(this)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { initState() }
            refreshDrawables()
            check()
        }
        return view
    }

    private fun initState() {
        gameFiles = (activity as SplashActivity).gameFiles
        configFiles = (activity as SplashActivity).configFiles
        lwjgl = (activity as SplashActivity).lwjgl
        cacio = (activity as SplashActivity).cacio
        cacio11 = (activity as SplashActivity).cacio11
        cacio17 = (activity as SplashActivity).cacio17
        java8 = (activity as SplashActivity).java8
        java11 = (activity as SplashActivity).java11
        java17 = (activity as SplashActivity).java17
        java21 = (activity as SplashActivity).java21
        jna = (activity as SplashActivity).jna
    }

    private fun refreshDrawables() {
        if (context != null) {
            val stateUpdate =
                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_update_24)
            val stateDone =
                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_done_24)

            bind.apply {
                gameFileState.setBackgroundDrawable(if (gameFiles) stateDone else stateUpdate)
                configFileState.setBackgroundDrawable(if (configFiles) stateDone else stateUpdate)
                lwjglState.setBackgroundDrawable(if (lwjgl) stateDone else stateUpdate)
                cacioState.setBackgroundDrawable(if (cacio) stateDone else stateUpdate)
                cacio11State.setBackgroundDrawable(if (cacio11) stateDone else stateUpdate)
                cacio17State.setBackgroundDrawable(if (cacio17) stateDone else stateUpdate)
                java8State.setBackgroundDrawable(if (java8) stateDone else stateUpdate)
                java11State.setBackgroundDrawable(if (java11) stateDone else stateUpdate)
                java17State.setBackgroundDrawable(if (java17) stateDone else stateUpdate)
                java21State.setBackgroundDrawable(if (java21) stateDone else stateUpdate)
                jnaState.setBackgroundDrawable(if (jna) stateDone else stateUpdate)
            }
        }
    }

    private val isLatest: Boolean
        get() = gameFiles && configFiles && lwjgl && cacio && cacio11 && cacio17 && java8 && java11 && java17 && java21 && jna

    private fun check() {
        if (isLatest) {
            (activity as SplashActivity).enterLauncher()
        }
    }

    private var installing = false
    private val showErrDialog = AtomicBoolean(false)

    private fun install() {
        if (installing) return

        bind.apply {
            val installResources = InstallResources(context)
            installing = true
            showErrDialog.set(false)
            if (!gameFiles) {
                gameFileState.visibility = View.GONE
                gameFilesProgress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            installResources.installGameFiles(getSelectedPath(config()).absolutePath, ".minecraft", sharedPreferences.edit(), context)
                            gameFiles = true
                        }.onFailure {
                            it.printStackTrace()
                            if (showErrDialog.compareAndSet(false, true)) {
                                withContext(Dispatchers.Main) {
                                    showErrorDialog(activity, it.message, true)
                                }
                            }
                        }
                    }
                    gameFileState.visibility = View.VISIBLE
                    gameFilesProgress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }

            if (!configFiles) {
                configFileState.visibility = View.GONE
                configFilesProgress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            installResources.installConfigFiles(FCLPath.CONFIG_DIR, "app_config", context)
                            configFiles = true
                        }.onFailure {
                            it.printStackTrace()
                            if (showErrDialog.compareAndSet(false, true)) {
                                withContext(Dispatchers.Main) {
                                    showErrorDialog(activity, it.message, true)
                                }
                            }
                        }
                    }
                    if(!installResources.isSuccess) ThemeEngine.getInstance().applyAndSave(activity, bind.backgroundInstallView, LT_BACKGROUND_PATH, DK_BACKGROUND_PATH)
                    configFileState.visibility = View.VISIBLE
                    configFilesProgress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!lwjgl) {
                lwjglState.visibility = View.GONE
                lwjglProgress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.install(context, FCLPath.LWJGL_DIR, "app_runtime/lwjgl")
                            RuntimeUtils.install(
                                context,
                                FCLPath.LWJGL_DIR + "-boat",
                                "app_runtime/lwjgl-boat"
                            )
                            lwjgl = true
                        }
                    }
                    lwjglState.visibility = View.VISIBLE
                    lwjglProgress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!cacio) {
                cacioState.visibility = View.GONE
                cacioProgress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.install(
                                context,
                                FCLPath.CACIOCAVALLO_8_DIR,
                                "app_runtime/caciocavallo"
                            )
                            cacio = true
                        }
                    }
                    cacioState.visibility = View.VISIBLE
                    cacioProgress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!cacio11) {
                cacio11State.visibility = View.GONE
                cacio11Progress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.install(
                                context,
                                FCLPath.CACIOCAVALLO_11_DIR,
                                "app_runtime/caciocavallo11"
                            )
                            cacio11 = true
                        }
                    }
                    cacio11State.visibility = View.VISIBLE
                    cacio11Progress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!cacio17) {
                cacio17State.visibility = View.GONE
                cacio17Progress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.install(
                                context,
                                FCLPath.CACIOCAVALLO_17_DIR,
                                "app_runtime/caciocavallo17"
                            )
                            cacio17 = true
                        }
                    }
                    cacio17State.visibility = View.VISIBLE
                    cacio17Progress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!java8) {
                java8State.visibility = View.GONE
                java8Progress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.installJava(
                                context,
                                FCLPath.JAVA_8_PATH,
                                "app_runtime/java/jre8"
                            )
                            java8 = true
                        }
                    }
                    java8State.visibility = View.VISIBLE
                    java8Progress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!java11) {
                java11State.visibility = View.GONE
                java11Progress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.installJava(
                                context,
                                FCLPath.JAVA_11_PATH,
                                "app_runtime/java/jre11"
                            )
                            java11 = true
                        }
                    }
                    java11State.visibility = View.VISIBLE
                    java11Progress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!java17) {
                java17State.visibility = View.GONE
                java17Progress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.installJava(
                                context,
                                FCLPath.JAVA_17_PATH,
                                "app_runtime/java/jre17"
                            )
                            java17 = true
                        }
                    }
                    java17State.visibility = View.VISIBLE
                    java17Progress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!java21) {
                java21State.visibility = View.GONE
                java21Progress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.installJava(
                                context,
                                FCLPath.JAVA_21_PATH,
                                "app_runtime/java/jre21"
                            )
                            java21 = true
                        }
                    }
                    java21State.visibility = View.VISIBLE
                    java21Progress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
            if (!jna) {
                jnaState.visibility = View.GONE
                jnaProgress.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RuntimeUtils.installJna(
                                context,
                                FCLPath.JNA_PATH,
                                "app_runtime/jna"
                            )
                            jna = true
                        }
                    }
                    jnaState.visibility = View.VISIBLE
                    jnaProgress.visibility = View.GONE
                    refreshDrawables()
                    check()
                }
            }
        }
    }

    override fun onClick(view: View) {
        if (view === bind.install) {
            install()
        }
    }
}
