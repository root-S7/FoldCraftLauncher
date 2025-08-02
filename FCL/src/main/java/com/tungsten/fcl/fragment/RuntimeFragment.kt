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
import com.tungsten.fclcore.task.Schedulers
import com.tungsten.fcllibrary.component.FCLFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class RuntimeFragment : FCLFragment(), View.OnClickListener {
    private var _binding: FragmentRuntimeBinding? = null
    private val bind get() = _binding!!
    
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
    
    // 安装相关变量
    private var installing = false
    private val showErrDialog = AtomicBoolean(false)
    private var installResources: InstallResources? = null
    private val installJobs = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_runtime, container, false)
        _binding = FragmentRuntimeBinding.bind(view)
        bind.install.setOnClickListener(this)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { initState() }
            if (isAdded) {
                refreshDrawables()
                check()
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 取消所有安装任务
        cancelAllInstallJobs()
        // 清理InstallResources
        installResources?.destroy()
        installResources = null
        // 清理binding引用
        _binding = null
    }

    private fun cancelAllInstallJobs() {
        installJobs.forEach { it.cancel() }
        installJobs.clear()
        installing = false
    }

    private fun initState() {
        val splashActivity = activity as? SplashActivity ?: return
        gameFiles = splashActivity.gameFiles
        configFiles = splashActivity.configFiles
        lwjgl = splashActivity.lwjgl
        cacio = splashActivity.cacio
        cacio11 = splashActivity.cacio11
        cacio17 = splashActivity.cacio17
        java8 = splashActivity.java8
        java11 = splashActivity.java11
        java17 = splashActivity.java17
        java21 = splashActivity.java21
        jna = splashActivity.jna
    }

    private fun refreshDrawables() {
        if (!isAdded || context == null) return
        
        val stateUpdate =
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_update_24)
        val stateDone =
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_done_24)

        _binding?.apply {
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

    private val isLatest: Boolean
        get() = gameFiles && configFiles && lwjgl && cacio && cacio11 && cacio17 && java8 && java11 && java17 && java21 && jna

    private fun check() {
        if (isLatest && isAdded) {
            (activity as? SplashActivity)?.enterLauncher()
        }
    }

    private fun install() {
        if (installing || !isAdded) return

        val currentActivity = activity ?: return
        val binding = _binding ?: return

        // 取消之前的安装任务
        cancelAllInstallJobs()

        // 创建新的InstallResources实例
        installResources?.destroy()
        installResources = InstallResources(currentActivity, binding.backgroundInstallView)

        installing = true
        showErrDialog.set(false)

        // 游戏文件安装
        if (!gameFiles) {
            val job = createInstallJob(
                stateView = binding.gameFileState,
                progressView = binding.gameFilesProgress,
                installAction = {
                    installResources?.installGameFiles(
                        getSelectedPath(config()).absolutePath,
                        ".minecraft",
                        sharedPreferences.edit()
                    )
                    gameFiles = true
                }
            )
            installJobs.add(job)
        }

        // 配置文件安装
        if (!configFiles) {
            val job = createInstallJob(
                stateView = binding.configFileState,
                progressView = binding.configFilesProgress,
                installAction = {
                    installResources?.installConfigFiles(FCLPath.CONFIG_DIR, "app_config")
                    configFiles = true
                }
            )
            installJobs.add(job)
        }

        // LWJGL安装
        if (!lwjgl) {
            val job = createInstallJob(
                stateView = binding.lwjglState,
                progressView = binding.lwjglProgress,
                installAction = {
                    RuntimeUtils.install(context, FCLPath.LWJGL_DIR, "app_runtime/lwjgl")
                    RuntimeUtils.install(
                        context,
                        FCLPath.LWJGL_DIR + "-boat",
                        "app_runtime/lwjgl-boat"
                    )
                    lwjgl = true
                }
            )
            installJobs.add(job)
        }

        // Cacio安装
        if (!cacio) {
            val job = createInstallJob(
                stateView = binding.cacioState,
                progressView = binding.cacioProgress,
                installAction = {
                    RuntimeUtils.install(
                        context,
                        FCLPath.CACIOCAVALLO_8_DIR,
                        "app_runtime/caciocavallo"
                    )
                    cacio = true
                }
            )
            installJobs.add(job)
        }

        // Cacio11安装
        if (!cacio11) {
            val job = createInstallJob(
                stateView = binding.cacio11State,
                progressView = binding.cacio11Progress,
                installAction = {
                    RuntimeUtils.install(
                        context,
                        FCLPath.CACIOCAVALLO_11_DIR,
                        "app_runtime/caciocavallo11"
                    )
                    cacio11 = true
                }
            )
            installJobs.add(job)
        }

        // Cacio17安装
        if (!cacio17) {
            val job = createInstallJob(
                stateView = binding.cacio17State,
                progressView = binding.cacio17Progress,
                installAction = {
                    RuntimeUtils.install(
                        context,
                        FCLPath.CACIOCAVALLO_17_DIR,
                        "app_runtime/caciocavallo17"
                    )
                    cacio17 = true
                }
            )
            installJobs.add(job)
        }

        // Java8安装
        if (!java8) {
            val job = createInstallJob(
                stateView = binding.java8State,
                progressView = binding.java8Progress,
                installAction = {
                    RuntimeUtils.installJava(
                        context,
                        FCLPath.JAVA_8_PATH,
                        "app_runtime/java/jre8"
                    )
                    java8 = true
                }
            )
            installJobs.add(job)
        }

        // Java11安装
        if (!java11) {
            val job = createInstallJob(
                stateView = binding.java11State,
                progressView = binding.java11Progress,
                installAction = {
                    RuntimeUtils.installJava(
                        context,
                        FCLPath.JAVA_11_PATH,
                        "app_runtime/java/jre11"
                    )
                    java11 = true
                }
            )
            installJobs.add(job)
        }

        // Java17安装
        if (!java17) {
            val job = createInstallJob(
                stateView = binding.java17State,
                progressView = binding.java17Progress,
                installAction = {
                    RuntimeUtils.installJava(
                        context,
                        FCLPath.JAVA_17_PATH,
                        "app_runtime/java/jre17"
                    )
                    java17 = true
                }
            )
            installJobs.add(job)
        }

        // Java21安装
        if (!java21) {
            val job = createInstallJob(
                stateView = binding.java21State,
                progressView = binding.java21Progress,
                installAction = {
                    RuntimeUtils.installJava(
                        context,
                        FCLPath.JAVA_21_PATH,
                        "app_runtime/java/jre21"
                    )
                    java21 = true
                }
            )
            installJobs.add(job)
        }

        // JNA安装
        if (!jna) {
            val job = createInstallJob(
                stateView = binding.jnaState,
                progressView = binding.jnaProgress,
                installAction = {
                    RuntimeUtils.installJna(
                        context,
                        FCLPath.JNA_PATH,
                        "app_runtime/jna"
                    )
                    jna = true
                }
            )
            installJobs.add(job)
        }
    }

    /**
     * 创建安装任务的协程
     */
    private fun createInstallJob(
        stateView: View,
        progressView: View,
        installAction: suspend () -> Unit
    ): Job {
        return lifecycleScope.launch {
            if (!isAdded) return@launch

            // 显示进度条
            safeUpdateUI {
                stateView.visibility = View.GONE
                progressView.visibility = View.VISIBLE
            }

            withContext(Dispatchers.IO) {
                runCatching {
                    installAction()
                }.onFailure { exception ->
                    exception.printStackTrace()
                    if (isAdded && showErrDialog.compareAndSet(false, true)) {
                        withContext(Dispatchers.Main) {
                            safeUpdateUI {
                                showErrorDialog(activity, exception.message, true)
                            }
                        }
                    }
                }
            }

            // 恢复UI状态
            safeUpdateUI {
                stateView.visibility = View.VISIBLE
                progressView.visibility = View.GONE
                refreshDrawables()
                check()
            }
        }
    }

    /**
     * 安全地更新UI，检查Fragment状态
     */
    private inline fun safeUpdateUI(crossinline action: () -> Unit) {
        if (isAdded && _binding != null && !requireActivity().isFinishing) {
            action()
        }
    }

    override fun onClick(view: View) {
        if (view === _binding?.install) {
            install()
        }
    }
}
