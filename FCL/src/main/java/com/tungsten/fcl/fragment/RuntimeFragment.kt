package com.tungsten.fcl.fragment

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import com.tungsten.fcl.R
import com.tungsten.fcl.activity.SplashActivity
import com.tungsten.fcl.databinding.FragmentRuntimeBinding
import com.tungsten.fcl.util.ConfigUtils
import com.tungsten.fcl.util.RuntimeUtils
import com.tungsten.fclauncher.utils.FCLPath
import com.tungsten.fclcore.task.Schedulers
import com.tungsten.fclcore.util.io.FileUtils
import com.tungsten.fcllibrary.component.FCLFragment
import com.tungsten.fcllibrary.util.LocaleUtils
import java.io.File
import java.io.IOException
import java.util.Locale

class RuntimeFragment : FCLFragment(), View.OnClickListener {
    private lateinit var  bind: FragmentRuntimeBinding
    var gameFiles: Boolean = false
    var configFiles: Boolean = false
    var lwjgl: Boolean = false
    var cacio: Boolean = false
    var cacio11: Boolean = false
    var cacio17: Boolean = false
    var java8: Boolean = false
    var java11: Boolean = false
    var java17: Boolean = false
    var java21: Boolean = false
    var jna: Boolean = false

    private val sharedPreferences = FCLPath.CONTEXT.getSharedPreferences("launcher", MODE_PRIVATE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_runtime, container, false)
        bind = FragmentRuntimeBinding.bind(view)
        bind.install.setOnClickListener(this)
        Schedulers.defaultScheduler().execute {
            initState()
            Schedulers.androidUIThread().execute {
                refreshDrawables()
                check()
            }
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

    private fun install() {
        if (installing) return

        bind.apply {
            val installResources = RuntimeUtils.InstallResources(activity, activity?.findViewById(R.id.background_install_view))
            installing = true
            if (!gameFiles) {
                gameFileState.visibility = View.GONE
                gameFilesProgress.visibility = View.VISIBLE
                Thread {
                    try {
                        installResources.installGameFiles(ConfigUtils.getGameDirectory(), ".minecraft", sharedPreferences.edit())
                        gameFiles = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        gameFileState.visibility = View.VISIBLE
                        gameFilesProgress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!configFiles) {
                configFileState.visibility = View.GONE
                configFilesProgress.visibility = View.VISIBLE
                Thread {
                    installResources.installConfigFiles(FCLPath.CONFIG_DIR, "app_config")
                    configFiles = true
                    activity?.runOnUiThread {
                        configFileState.visibility = View.VISIBLE
                        configFilesProgress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!lwjgl) {
                lwjglState.visibility = View.GONE
                lwjglProgress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.install(context, FCLPath.LWJGL_DIR, "app_runtime/lwjgl")
                        RuntimeUtils.install(
                            context,
                            FCLPath.LWJGL_DIR + "-boat",
                            "app_runtime/lwjgl-boat"
                        )
                        lwjgl = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        lwjglState.visibility = View.VISIBLE
                        lwjglProgress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!cacio) {
                cacioState.visibility = View.GONE
                cacioProgress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.install(
                            context,
                            FCLPath.CACIOCAVALLO_8_DIR,
                            "app_runtime/caciocavallo"
                        )
                        cacio = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        cacioState.visibility = View.VISIBLE
                        cacioProgress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!cacio11) {
                cacio11State.visibility = View.GONE
                cacio11Progress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.install(
                            context,
                            FCLPath.CACIOCAVALLO_11_DIR,
                            "app_runtime/caciocavallo11"
                        )
                        cacio11 = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        cacio11State.visibility = View.VISIBLE
                        cacio11Progress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!cacio17) {
                cacio17State.visibility = View.GONE
                cacio17Progress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.install(
                            context,
                            FCLPath.CACIOCAVALLO_17_DIR,
                            "app_runtime/caciocavallo17"
                        )
                        cacio17 = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        cacio17State.visibility = View.VISIBLE
                        cacio17Progress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!java8) {
                java8State.visibility = View.GONE
                java8Progress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.installJava(
                            context,
                            FCLPath.JAVA_8_PATH,
                            "app_runtime/java/jre8"
                        )
                        java8 = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        java8State.visibility = View.VISIBLE
                        java8Progress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!java11) {
                java11State.visibility = View.GONE
                java11Progress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.installJava(
                            context,
                            FCLPath.JAVA_11_PATH,
                            "app_runtime/java/jre11"
                        )
                        FileUtils.writeText(
                            File(FCLPath.JAVA_11_PATH + "/resolv.conf"),
                            String.format(
                                "nameserver %s\nnameserver %s",
                                FCLPath.GENERAL_SETTING.getProperty("primary-nameserver", "119.29.29.29"),
                                FCLPath.GENERAL_SETTING.getProperty("secondary-nameserver", "8.8.8.8")
                            )
                        )
                        java11 = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        java11State.visibility = View.VISIBLE
                        java11Progress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!java17) {
                java17State.visibility = View.GONE
                java17Progress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.installJava(
                            context,
                            FCLPath.JAVA_17_PATH,
                            "app_runtime/java/jre17"
                        )
                        FileUtils.writeText(
                            File(FCLPath.JAVA_17_PATH + "/resolv.conf"),
                            String.format(
                                "nameserver %s\nnameserver %s",
                                FCLPath.GENERAL_SETTING.getProperty("primary-nameserver", "119.29.29.29"),
                                FCLPath.GENERAL_SETTING.getProperty("secondary-nameserver", "8.8.8.8")
                            )
                        )
                        java17 = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        java17State.visibility = View.VISIBLE
                        java17Progress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!java21) {
                java21State.visibility = View.GONE
                java21Progress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.installJava(
                            context,
                            FCLPath.JAVA_21_PATH,
                            "app_runtime/java/jre21"
                        )
                        FileUtils.writeText(
                            File(FCLPath.JAVA_21_PATH + "/resolv.conf"),
                            String.format(
                                "nameserver %s\nnameserver %s",
                                FCLPath.GENERAL_SETTING.getProperty("primary-nameserver", "119.29.29.29"),
                                FCLPath.GENERAL_SETTING.getProperty("secondary-nameserver", "8.8.8.8")
                            )
                        )
                        java21 = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        java21State.visibility = View.VISIBLE
                        java21Progress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
            if (!jna) {
                jnaState.visibility = View.GONE
                jnaProgress.visibility = View.VISIBLE
                Thread {
                    try {
                        RuntimeUtils.installJna(
                            context,
                            FCLPath.JNA_PATH,
                            "app_runtime/jna"
                        )
                        jna = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    activity?.runOnUiThread {
                        jnaState.visibility = View.VISIBLE
                        jnaProgress.visibility = View.GONE
                        refreshDrawables()
                        check()
                    }
                }.start()
            }
        }
    }

    override fun onClick(view: View) {
        if (view === bind.install) {
            install()
        }
    }
}
