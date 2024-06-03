package org.kongit.hsdamageindicate.events

import com.github.horangshop.lib.plugin.inventory.HSInventory
import com.github.horangshop.lib.plugin.storage.Storage
import com.github.horangshop.lib.util.common.ComponentUtil
import com.github.horangshop.lib.util.support.ItemUtil
import com.google.gson.JsonObject
import org.apache.commons.lang3.tuple.Pair
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.kongit.hsdamageindicate.HSDamageIndicate
import org.kongit.hsdamageindicate.util.ConfigManager


class MenuEvent : HSInventory(HSDamageIndicate.getInstance()) {
    private val inv = Bukkit.createInventory(this,54,"스킨 메뉴")
    private val storage: Storage? = HSDamageIndicate.getInstance()?.storage
    public fun openInventory(player: Player) {
        if (storage?.get("user",Pair.of("uuid",player.uniqueId.toString()))!!.isEmpty()) {
            val jsonObject =  JsonObject()
            jsonObject.addProperty("uuid",player.uniqueId.toString())
            storage.add("user",jsonObject)
        }
        player.openInventory(setInventory(player))
    }

    private fun setInventory(player:Player) : Inventory {
        val data = storage?.get("user",Pair.of("uuid",player.uniqueId.toString()))?.get(0)?.get("skin").toString().replace("\"","") ?: ""
        val skin = ConfigManager.skins[data]
        var itemStack:ItemStack? = null
        var itemMeta:ItemMeta? = null
        var loreMeta: MutableList<String> = mutableListOf()
        var num = 9
        for (i in 0..8) {
            inv.setItem(i, ItemStack(Material.WHITE_STAINED_GLASS_PANE))
        }
        for (i in 45..53) {
            inv.setItem(i, ItemStack(Material.WHITE_STAINED_GLASS_PANE))
        }

        for (i in (ConfigManager.skins.keys ?: listOf()).filterNotNull()) {
            itemStack = ItemUtil.fromId(ConfigManager.skins[i]!!["indicate-items"].toString()) ?: ItemStack(Material.BARRIER)
            itemMeta = itemStack.itemMeta!!
            itemMeta.setDisplayName(ComponentUtil.toString(ComponentUtil.miniMessage(ConfigManager.skins[i]!!["indicate-name"].toString())))
            itemMeta.persistentDataContainer.set(NamespacedKey(HSDamageIndicate.getInstance()!!, "skin-name"), PersistentDataType.STRING, i.toString());
            itemMeta.persistentDataContainer.set(NamespacedKey(HSDamageIndicate.getInstance()!!, "permission-use"), PersistentDataType.STRING, ConfigManager.skins[i]!!["indicate-permission.use"].toString());
            itemMeta.persistentDataContainer.set(NamespacedKey(HSDamageIndicate.getInstance()!!, "permission-contents"), PersistentDataType.STRING, ConfigManager.skins[i]!!["indicate-permission.contents"].toString());
            loreMeta = mutableListOf()
            for (l in (ConfigManager.skins[i]!!["indicate-lore"] as List<*>).filterNotNull()) {
                if (l.toString().startsWith("[op]")) {
                    if (!player.isOp) continue
                    loreMeta.add(ComponentUtil.toString(ComponentUtil.miniMessage(l.toString().replace("[op]","") ?: "")))
                    continue
                }
                loreMeta.add(ComponentUtil.toString(ComponentUtil.miniMessage(l.toString() ?: "")))
            }
            itemMeta.lore = loreMeta
            itemStack.setItemMeta(itemMeta)
            inv.setItem(num, itemStack)
            num += 1
        }

        if (skin == null) {
            storage?.set("user",Pair.of("uuid",player.uniqueId.toString()),Pair.of("skin",""))
            itemStack = ItemStack(Material.BARRIER)
            itemMeta = itemStack.itemMeta!!
            itemMeta.setDisplayName(ComponentUtil.toString(ComponentUtil.miniMessage("스킨을 장착 하지 않았습니다")))
            itemStack.setItemMeta(itemMeta)
        } else {
            itemStack = ItemUtil.fromId(skin["indicate-items"].toString()) ?: ItemStack(Material.BARRIER)
            itemMeta = itemStack.itemMeta!!
            if (itemStack == ItemStack(Material.BARRIER)) {
                itemMeta.setDisplayName(ComponentUtil.toString(ComponentUtil.miniMessage("스킨을 장착 하지 않았습니다")))
                itemStack.setItemMeta(itemMeta)
            } else {
                itemMeta.setDisplayName(ComponentUtil.toString(ComponentUtil.miniMessage(skin["indicate-name"].toString())))
                itemMeta.lore = mutableListOf()
                loreMeta = mutableListOf()
                for (l in skin["indicate-lore"] as List<*>) {
                    if (l.toString().startsWith("[op]")) {
                        if (!player.isOp) continue
                        loreMeta.add(ComponentUtil.toString(ComponentUtil.miniMessage(l.toString().replace("[op]",""))))
                        continue
                    }
                    loreMeta.add(ComponentUtil.toString(ComponentUtil.miniMessage(l.toString())))
                }
                itemMeta.lore = loreMeta
                itemStack.setItemMeta(itemMeta)
            }
        }
        inv.setItem(4, itemStack)
        return inv
    }


    override fun getInventory(): Inventory {
        return this.inv
    }

    override fun onClick(event: Event, click: Click): Boolean {
        if (event.inventory.type != InventoryType.CHEST) return false

        if (event.inventory.holder is MenuEvent) {
            if (event.inventory.getItem(click.slot) == ItemStack(Material.AIR)) return true
            if (click.slot !in 9..44) return false
            val container:PersistentDataContainer? = event.inventory.getItem(click.slot)?.itemMeta?.persistentDataContainer
            if ((container ?: return false ).has(NamespacedKey(HSDamageIndicate.getInstance()!!, "skin-name"), PersistentDataType.STRING)) {
                val name = container.get(NamespacedKey(HSDamageIndicate.getInstance()!!, "skin-name"), PersistentDataType.STRING);
                val use = container.get(NamespacedKey(HSDamageIndicate.getInstance()!!, "permission-use"), PersistentDataType.STRING);
                val contents = container.get(NamespacedKey(HSDamageIndicate.getInstance()!!, "permission-contents"), PersistentDataType.STRING);
                if (!use.toBoolean() || event.player.hasPermission(contents ?: return false)) {
                    storage?.set("user",Pair.of("uuid",event.player.uniqueId.toString()),Pair.of("skin",name))
                    event.player.sendMessage(ComponentUtil.toString(HSDamageIndicate.getInstance()!!.prefix.append(ComponentUtil.miniMessage("<b>데미지 스킨</b>을 <green><b>적용</b></green>하였습니다."))))
                    openInventory(event.player)
                }
            } else {
                event.player.sendMessage(ComponentUtil.toString(HSDamageIndicate.getInstance()!!.prefix.append(ComponentUtil.miniMessage("해당 <b>데미지 스킨</b> <red>사용 권한</red>이 없습니다."))))
            }
            return false
        }
        return true
    }
    override fun onClose(event: Event?) {

    }
}