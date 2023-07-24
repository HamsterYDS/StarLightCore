package world.icebear03.starlight

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.expansion.setupPlayerDatabase
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.career.internal.Class
import world.icebear03.starlight.career.mechanism.data.Resonate
import world.icebear03.starlight.career.mechanism.limit.MaterialLimitLibrary
import java.io.File

object StarLightCore : Plugin() {

    override fun onEnable() {

        Class.initialize()

//        setupPlayerDatabase(Config.config.getConfigurationSection("database")!!)
        setupPlayerDatabase(File(getDataFolder(), "data.db"))

        info("Successfully running StarLightCore!")

        Resonate.initialize()

        MaterialLimitLibrary.initialize()
    }

    override fun onDisable() {
        onlinePlayers.forEach { it.kickPlayer("核心插件重载") }
    }
}