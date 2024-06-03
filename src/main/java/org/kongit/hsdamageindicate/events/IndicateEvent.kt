package org.kongit.hsdamageindicate.events

import com.github.horangshop.lib.plugin.listener.HSListener
import com.github.horangshop.lib.plugin.storage.Storage
import com.github.horangshop.lib.util.common.ComponentUtil
import com.google.gson.JsonObject
import com.ticxo.modelengine.api.ModelEngineAPI
import org.apache.commons.lang3.tuple.Pair
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle
import org.kongit.hsdamageindicate.HSDamageIndicate
import org.kongit.hsdamageindicate.util.ConfigManager
import org.w3c.dom.events.EventListener
import kotlin.math.sin
import kotlin.random.Random

class IndicateEvent : HSListener(HSDamageIndicate.getInstance()) {
    private val storage: Storage? = HSDamageIndicate.getInstance()?.storage
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (storage?.get("user",Pair.of("uuid",event.player.uniqueId.toString()))!!.isEmpty()) {
            val jsonObject =  JsonObject()
            jsonObject.addProperty("uuid",event.player.uniqueId.toString())
            storage.add("user",jsonObject)
        }
    }
    @EventHandler
    fun onAttack(event : EntityDamageByEntityEvent) {
        if (event.damager is Player) {
            try {
                val loc = event.entity.location
                loc.y += event.entity.height
                this.spawnTempArmorStand(event.damager as Player,loc, String.format("%.2f", event.finalDamage) )
            } catch (e: Error) { HSDamageIndicate.getInstance()!!.console(ComponentUtil.miniMessage("데미지 이벤트에서 <red>비정상적인</red> 처리가 발생하였습니다.")) }
        }
    }
    private fun getDamageString(player:Player,damage: String) : String {
        var data = (storage?.get("user", Pair.of("uuid",player.uniqueId.toString()))?.get(0)?.get("skin").toString() ?: "").replace("\"","")
        if (data == "null" && HSDamageIndicate.getInstance()!!.config.getBoolean("default.use")) {
            val key = if ((HSDamageIndicate.getInstance()!!.config.getString("default.skin") ?: "").contains(".yml")) { HSDamageIndicate.getInstance()!!.config.getString("default.skin") ?: "" } else { (HSDamageIndicate.getInstance()!!.config.getString("default.skin") ?: "") + ".yml" }
            if (ConfigManager.skins.keys.contains(key)) { data = key }
        }
        val skin = ConfigManager.skins[data] ?: mutableMapOf()
        val prefix = (skin["indicate-prefix"] ?: HSDamageIndicate.getInstance()!!.config.getString("indicate-prefix") ?: "").toString().replace("\"","")
        var damageString: String = ""
        if (skin.isEmpty()) {
            if (HSDamageIndicate.getInstance()?.config?.getBoolean("indicate-skin.use") == true) {
                for (key in damage.split("")) {
                    if (key == "") continue
                    damageString = if (key == ".") {
                        "${damageString}${ComponentUtil.toString(ComponentUtil.miniMessage((HSDamageIndicate.getInstance()!!.config.getString("indicate-skin.skin.dot").toString() ?: "")))}"
                    } else {
                        "${damageString}${ComponentUtil.toString(ComponentUtil.miniMessage((HSDamageIndicate.getInstance()!!.config.getString("indicate-skin.skin.${key}").toString() ?: "")))}"
                    }
                }
            } else {
                damageString = prefix + damage
            }
        } else {
            if (skin["indicate-skin.use"].toString().replace("\"","").toBoolean() ?: false) {
                for (key in damage.split("")) {
                    if (key == "") continue
                    damageString = if (key == ".") {
                        "${damageString}${ComponentUtil.toString(ComponentUtil.miniMessage(skin["indicate-skin.skin.dot"].toString().replace("\"","")))}"
                    } else {
                        "${damageString}${ComponentUtil.toString(ComponentUtil.miniMessage(skin["indicate-skin.skin.${key}"].toString().replace("\"","")))}"
                    }
                }
            } else {
                damageString = prefix + damage
            }
        }

        return damageString
    }

    private fun spawnTempArmorStand(player:Player, location: Location, damage: String) {
        val data = storage?.get("user", Pair.of("uuid",player.uniqueId.toString()))?.get(0)?.get("skin").toString().replace("\"","") ?: ""
        val skin = ConfigManager.skins[data]
        val world = location.world ?: return // Ensure the world is not null
        val duration = (skin?.get("indicate-duration") ?: (HSDamageIndicate.getInstance()!!.config.getInt("indicate-duration") ?: 1)).toString().replace("\"","").toDouble().toInt() * 1L
        val type = (skin?.get("indicate-effect") ?: HSDamageIndicate.getInstance()!!.config.getString("indicate-effect") ?: "basic").toString().replace("\"","")
        val loc = (skin?.get("indicate-region") ?: (HSDamageIndicate.getInstance()!!.config.getDouble("indicate-region") ?: 0.5)).toString().replace("\"","").toDouble()

        // Create the ArmorStand
        val armorStand = world.spawn(location.clone().add(Random.nextDouble(-loc, loc),0.0,Random.nextDouble(-loc, loc)), ArmorStand::class.java) {
            it.isVisible = false // Make the ArmorStand invisible
            it.isMarker = true // Avoid interaction with the ArmorStand
            it.customName = getDamageString(player,damage) // Set display name to the damage string
            it.isCustomNameVisible = true // Make sure the name is visible
            it.setGravity(false) // Prevent the ArmorStand from falling
            it.isInvulnerable = true // Make the ArmorStand invulnerable
        }

        // Schedule the ArmorStand to be removed after the duration
        when (type) {
            "bound" -> {
                val startTime = System.currentTimeMillis()
                val endTime = startTime + duration * 1000

                object : BukkitRunnable() {
                    override fun run() {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime > endTime) {
                            armorStand.remove() // 시간이 지나면 아머스탠드 제거
                            cancel() // 이 태스크 종료
                        } else {
                            // 시간에 따라 Y 위치를 사인 함수를 사용해 업데이트
                            val timeElapsed = (currentTime - startTime).toDouble() / 1000.0
                            val bounceHeight = sin(timeElapsed * Math.PI) * 0.5 // 최대 0.5 블록 높이로 바운스
                            armorStand.teleport(location.clone().add(0.0, bounceHeight, 0.0))
                        }
                    }
                }.runTaskTimer(HSDamageIndicate.getInstance()!!, 0L, 1L) // 매 틱마다 실행
            }
            "spin" -> {
                object : BukkitRunnable() {
                    var angle = 0.0
                    override fun run() {
                        if (angle > 360) angle = 0.0
                        armorStand.headPose = EulerAngle(0.0, Math.toRadians(angle), 0.0)
                        angle += 10.0
                    }
                }.runTaskTimer(HSDamageIndicate.getInstance()!!, 0L, 1L)

                // Remove after duration
                Bukkit.getScheduler().runTaskLater(HSDamageIndicate.getInstance()!!, Runnable { armorStand.remove() }, duration * 20)
            }
            else -> {
                object : BukkitRunnable() {
                    override fun run() {
                        armorStand.remove() // Remove the ArmorStand
                    }
                }.runTaskLater(HSDamageIndicate.getInstance()!!, duration * 20) // Convert seconds to ticks (20 ticks per second)
            }
        }

    }
}