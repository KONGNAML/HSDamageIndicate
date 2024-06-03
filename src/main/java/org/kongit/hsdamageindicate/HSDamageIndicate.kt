package org.kongit.hsdamageindicate

import com.github.horangshop.lib.plugin.HSPlugin
import com.github.horangshop.lib.plugin.storage.Storage
import com.github.horangshop.lib.util.common.ComponentUtil
import org.kongit.hsdamageindicate.commands.DamageIndicateCommand
import org.kongit.hsdamageindicate.events.IndicateEvent
import org.kongit.hsdamageindicate.events.MenuEvent
import org.kongit.hsdamageindicate.util.ConfigManager


class HSDamageIndicate : HSPlugin(ComponentUtil.miniMessage("<gradient:#ff9633:#ffd633>HSDamageIndicate | </gradient>")) {
    companion object {
        private var plugins : HSDamageIndicate? = null
        private val configManager: ConfigManager = ConfigManager()
        public var storage: Storage? = null
        fun getInstance() : HSDamageIndicate? { return plugins }
    }
    override fun enable() {
        //if (!VersionUtil.isSupportVersion("1.17.1")) {
        //    Bukkit.getPluginManager().disablePlugin(this)
        //}
        plugins = this
        configManager.init()
        configurations.initStorage("user")
        registerCommand(DamageIndicateCommand())
        registerEvent(IndicateEvent())
        registerEvent(MenuEvent())
        if (!configFileExists()) {
            this.saveDefaultConfig()
        }
    }

    override fun disable() {
        storage?.close()
        plugins = null
    }
    private fun configFileExists(): Boolean {
        val configFile = dataFolder.resolve("config.yml")
        return configFile.exists()
    }
}

