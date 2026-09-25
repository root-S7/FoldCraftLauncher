package com.tungsten.fcl.game

import com.tungsten.fcl.FCLApp.getAppContext
import com.tungsten.fcl.setting.Config
import com.tungsten.fcl.setting.ConfigHolder
import com.tungsten.fcl.setting.Controller
import com.tungsten.fcl.setting.Controllers
import com.tungsten.fcl.util.ParseAuthlibInjectorServerUtils
import com.tungsten.fcl.util.RuntimeUtils
import com.tungsten.fclauncher.utils.AssetsPath
import com.tungsten.fclauncher.utils.FCLPath
import com.tungsten.fclcore.util.io.IOUtils
import java.io.File

class InstallConfigHelper {

    fun install(listener: RuntimeUtils.InstallListener?) {
        run {
            listener?.onUpdate("正在解析与保存启动器配置...")
            val rawJson = IOUtils.readFullyAsString(IOUtils.openAssets(getAppContext(), AssetsPath.LAUNCHER_CONFIG))
            val config = ConfigHolder.validateProfile(Config.fromJson(rawJson))

            ParseAuthlibInjectorServerUtils.parseUrlToConfig(config)
            ConfigHolder.writeToConfig(config)
        }.run {
            listener?.onUpdate("正在初始化控制器...")
            Controllers.init()
            val controllerDir = File(FCLPath.CONTROLLER_DIR).apply { mkdirs() }

            getAppContext().assets.list("controllers")
                .orEmpty()
                .filter { it.endsWith(".json") }
                .forEach { fileName ->
                    runCatching {
                        val controller = IOUtils.readFullyAsString(IOUtils.openAssets(getAppContext(), "controllers/$fileName"))
                            .let { Controller.GSON.fromJson(it, Controller::class.java) }

                        controller.file = File(controllerDir, controller.fileName)
                        controller.saveToDisk()
                        Controllers.addController(controller)
                    }
                }
        }
    }
}