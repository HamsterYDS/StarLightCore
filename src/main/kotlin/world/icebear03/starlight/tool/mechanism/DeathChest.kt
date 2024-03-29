package world.icebear03.starlight.tool.mechanism

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.StorageMinecart
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import world.icebear03.starlight.career
import world.icebear03.starlight.tag.PlayerTag
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.has
import world.icebear03.starlight.utils.set
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

object DeathChest {

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        val loc = player.location
        val items = mutableListOf<ItemStack>()
        val inv = player.inventory
        for (slot in 0 until inv.size) {
            val item = player.inventory.getItem(slot) ?: continue
            val meta = item.itemMeta ?: continue
            if (!meta.has("station_owner_id", PersistentDataType.STRING))
                items += item
        }

        val minecart = player.world.spawnEntity(loc, EntityType.MINECART_CHEST) as StorageMinecart

        var point = player.career().points
        player.career().branches.keys.forEach {
            point += player.career().getBranchLevel(it)
        }
        if (point <= 3) point = 0

        submit {
            items.shuffle()
            items.forEach {
                minecart.inventory.addItem(it)
            }
            minecart.isGlowing = true
            minecart.isCustomNameVisible = true
            minecart.customName = "§e${player.name}的遗物 §r关闭时自动消失"
            minecart["death_chest_stamp", PersistentDataType.LONG] = System.currentTimeMillis()
            minecart["death_chest_owner", PersistentDataType.STRING] = player.name
            minecart["death_chest_level", PersistentDataType.INTEGER] = getTotalExp(player.level)
            minecart["death_chest_point", PersistentDataType.INTEGER] = ceil(point * 0.333).roundToInt()
            minecart.isInvulnerable = true
            minecart.setGravity(false)
        }

        event.keepInventory = true
        event.keepLevel = true

        player.sendMessage("§b繁星工坊 §7>> 死亡掉落物品已被收集在原地的容器中 坐标 x:§e${loc.blockX} §7y:§e${loc.blockY} §7z:§e${loc.blockZ}")

        submit(delay = 3L) {
            if (player.isDead) {
                player.spigot().respawn()

                player.level = 0
                player.exp = 0f
                player.inventory.clear()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        if (player.isDead)
            player.spigot().respawn()
    }

    val opening = mutableListOf<UUID>()

    @SubscribeEvent
    fun close(event: InventoryCloseEvent) {
        if (!opening.contains(event.player.uniqueId))
            return
        opening.remove(event.player.uniqueId)

        val inv = event.inventory
        val minecart = inv.holder
        if (minecart !is StorageMinecart)
            return
        minecart.remove()
    }

    @SubscribeEvent
    fun open(event: InventoryOpenEvent) {
        val inv = event.inventory
        val minecart = inv.holder
        if (minecart !is StorageMinecart)
            return

        if (!minecart.has("death_chest_owner", PersistentDataType.STRING))
            return

        val player = event.player as Player
        val who = minecart["death_chest_owner", PersistentDataType.STRING]
        val stamp = minecart["death_chest_stamp", PersistentDataType.LONG] ?: 0
        if (System.currentTimeMillis() - stamp <= 1000 * 30 * 60 && player.name != who) {
            player.sendMessage("§b繁星工坊 §7>> 死亡箱在生成的§a30分钟§7内只能由主人自己打开，请等待一会")
            event.isCancelled = true
            return
        }

        opening += player.uniqueId

        if (!PlayerTag.addTag(player, "上一世代")) {
            player.sendMessage("§b繁星工坊 §7>> 它们是什么时候留下来的？(获得称号${"&{#b29c6e}上一世代".colored()}§7)")
        }

        val exp = maxOf(0, minecart["death_chest_level", PersistentDataType.INTEGER]!!)
        minecart["death_chest_level", PersistentDataType.INTEGER] = 0
        player.giveExp(exp)

        val point = maxOf(0, minecart["death_chest_point", PersistentDataType.INTEGER]!!)
        minecart["death_chest_point", PersistentDataType.INTEGER] = 0
        if (point != 0) {
            player.career().addPoint(point)
            player.sendMessage("§b繁星工坊 §7>> 你从死亡箱中找到了 §a${point}技能点")
        }
    }


    fun getExpPerLevelInVanilla(level: Int): Int {
        return if (level <= 15) {
            2 * level + 7
        } else if (level <= 30) {
            5 * level - 38
        } else {
            9 * level - 158
        }
    }

    fun getTotalExp(level: Int): Int {
        var exp = 0
        for (i in 0 until level) {
            exp += getExpPerLevelInVanilla(i)
        }
        return exp
    }
}