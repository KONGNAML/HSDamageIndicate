package org.kongit.hsdamageindicate.util

import com.github.horangshop.lib.util.common.ComponentUtil
import com.github.horangshop.lib.util.common.FileUtil
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.apache.commons.lang3.tuple.Pair
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.YamlConfiguration
import org.kongit.hsdamageindicate.HSDamageIndicate
import java.io.File

open class ConfigManager {

    companion object {
        val skins: MutableMap<String,MutableMap<String,Any>> = mutableMapOf()
    }
    fun init() {
        initSkin(FileUtil.copyResource(HSDamageIndicate.getInstance()!!, "skin","example.yml"))
    }
    private fun initSkin(file: File) {
        val listFiles: Array<out File> = FileUtil.getResourceFolder( "${HSDamageIndicate.getInstance()!!.dataFolder}/skin").listFiles() ?: arrayOf()
        //val storage = HSDamageIndicate.storage ?: return
        var config: Configuration? = null
        HSDamageIndicate.getInstance()!!.console(ComponentUtil.miniMessage("loading skins"))
        for (f in listFiles.filterNotNull()) {
            config = YamlConfiguration.loadConfiguration(f) ?: continue
            skins[f.name] = mutableMapOf()
            skins[f.name]?.set("indicate-items", config.getString("indicate-items") ?: "")
            skins[f.name]?.set("indicate-name", config.getString("indicate-name") ?: "")
            skins[f.name]?.set("indicate-lore", config.getList("indicate-lore") ?: listOf<String>())
            skins[f.name]?.set("indicate-effect", config.getString("indicate-effect") ?: "basic")
            skins[f.name]?.set("indicate-region", config.getDouble("indicate-region") ?: 1.0)
            skins[f.name]?.set("indicate-duration", config.getDouble("indicate-duration") ?: 1.0)
            skins[f.name]?.set("indicate-prefix", config.getString("indicate-prefix") ?: "")
            skins[f.name]?.set("indicate-permission.use", config.getBoolean("indicate-permission.use") ?: true)
            skins[f.name]?.set("indicate-permission.contents", config.getString("indicate-permission.contents") ?: "")
            skins[f.name]?.set("indicate-skin.use", config.getBoolean("indicate-skin.use") ?: false)
            skins[f.name]?.set("indicate-skin.skin.dot", config.getString("indicate-skin.skin.dot") ?: ".")
            for (int in 0..9) {
                skins[f.name]?.set("indicate-skin.skin.${int}", config.getString("indicate-skin.skin.${int}") ?: "${int}")
            }
            skins[f.name]?.set("indicate-skin.skin.dot", config.getString("indicate-skin.skin.dot") ?: ".")

        }
    }
}