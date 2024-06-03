package org.kongit.hsdamageindicate.commands

import com.github.horangshop.lib.plugin.command.CommandData
import com.github.horangshop.lib.plugin.command.HSCommand
import com.github.horangshop.lib.util.common.ComponentUtil
import com.github.horangshop.lib.util.common.FileUtil
import com.google.gson.JsonObject
import org.apache.commons.lang3.tuple.Pair
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

import org.kongit.hsdamageindicate.HSDamageIndicate
import org.kongit.hsdamageindicate.events.MenuEvent
import org.kongit.hsdamageindicate.util.ConfigManager



class DamageIndicateCommand : HSCommand(HSDamageIndicate.getInstance(), listOf("HSDamage","데미지스킨")) {
    private val plugins = HSDamageIndicate.getInstance()!!
    private val configManager: ConfigManager = ConfigManager()
    override fun command(data: CommandData?) : Boolean {
        when (data!!.args(0)) {
            "구성" -> {
                if (!sender.hasPermission("hsdamage.reload") && sender !is ConsoleCommandSender) return true
                plugins.reloadConfig()
                configManager.init()
                sendMessage(plugins.prefix.append(ComponentUtil.miniMessage("콘피그 <gold><b>리로드</b></gold>를 <color:#00ff15>완료</color>하였습니다.")))
                return true
            }
            "스킨" -> {
                if (sender !is Player) {
                    sendMessage(plugins.prefix.append(ComponentUtil.miniMessage("해당 명령어는 <color:#fff4b0><b>콘솔</b></color>에서 사용이 <color:#ff403d>불가능</color>합니다.")))
                    return true
                }
                if (!sender.hasPermission("hsdamage.open") && !sender.isOp) {
                    sendMessage(plugins.prefix.append(ComponentUtil.miniMessage("명령어 사용 권한이 없습니다.")))
                    return true
                }
                MenuEvent().openInventory(sender as Player)
                return true
            }
            "data" -> {
                val storage = HSDamageIndicate.storage!!
                if (storage.get("data", Pair.of("uuid", "console")).isEmpty()) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("uuid", "console")
                    storage.add("data", jsonObject)
                    sendMessage(plugins.prefix.append(ComponentUtil.miniMessage("콘피그 <gold><b>Data</b></gold>를 <color:#00ff15>추가</color>하였습니다.")))
                } else {
                    sendMessage(plugins.prefix.append(ComponentUtil.miniMessage("${storage.get("data",Pair.of("uuid","console"))}")))
                    storage.set("data", Pair.of("uuid","console"),Pair.of("test","Hello World"))

                }

            }
            else -> sendMessage(plugins.prefix.append(ComponentUtil.miniMessage("정상 <color:#ff1100><color:#00ff15><b>작동중</b></color></color>입니다.\n<aqua><gold>버전</gold></aqua> : ${plugins.description.version}")))
        }
        return false
    }
    override fun tabComplete(data: CommandData?): MutableList<String> {
        if (sender.isOp) {
            return mutableListOf("구성","스킨")
        }
        return mutableListOf("스킨")
    }
}