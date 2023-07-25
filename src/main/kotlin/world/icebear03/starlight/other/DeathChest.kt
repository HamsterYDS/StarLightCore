package world.icebear03.starlight.other

import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.StorageMinecart
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.utils.effect
import java.util.*

object DeathChest {

    val ownerKey = NamespacedKey.minecraft("death_chest_owner")
    val expKey = NamespacedKey.minecraft("death_chest_exp")
    val lastDeathKey = NamespacedKey.minecraft("last_death_stamp")

    init {
        submit(period = 100L) {
            onlinePlayers.forEach {
                val pdc = it.persistentDataContainer
                val lastDeathStamp = pdc.get(lastDeathKey, PersistentDataType.LONG) ?: return@forEach
                if (System.currentTimeMillis() - lastDeathStamp >= 360 * 1000)
                    return@forEach

                val deathLoc = it.lastDeathLocation ?: return@forEach
                val loc = it.location
                val vector = Vector(
                    deathLoc.x - loc.x,
                    0.0,
                    deathLoc.z - loc.z
                )

                val direction = it.eyeLocation.direction
                direction.y = 0.0

                if (vector.angle(direction) <= 0.8) {
                    it.effect(PotionEffectType.SPEED, 8, 1)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun death(event: PlayerDeathEvent) {
        val player = event.entity
        val loc = player.location
        val items = mutableListOf<ItemStack>()
        val inv = player.inventory
        for (slot in 0 until inv.size) {
            val item = player.inventory.getItem(slot) ?: continue
            items += item
        }

        val minecart = player.world.spawnEntity(loc, EntityType.MINECART_CHEST) as StorageMinecart

        submit {
            items.shuffle()
            items.forEach {
                minecart.inventory.addItem(it)
            }
            minecart.isGlowing = true
            minecart.isCustomNameVisible = true
            minecart.customName = "§e${player.name}的遗物 §r关闭时自动消失"
            minecart.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, player.name)
            minecart.persistentDataContainer.set(expKey, PersistentDataType.INTEGER, getTotalExp(player.level))
            minecart.isInvulnerable = true
        }

        event.keepInventory = false
        event.keepLevel = false
        player.totalExperience = 0

        player.persistentDataContainer.set(lastDeathKey, PersistentDataType.LONG, System.currentTimeMillis())

        player.sendMessage("§b繁星工坊 §7>> 死亡掉落物品已被收集在原地的容器中 坐标 x:${loc.blockX} y:${loc.blockY} z:${loc.blockZ}")

        submit(delay = 3L) {
            if (player.isDead)
                player.spigot().respawn()
        }
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

        val pdc = minecart.persistentDataContainer
        if (!pdc.has(ownerKey, PersistentDataType.STRING))
            return

        val player = event.player as Player

        opening += player.uniqueId

        val exp = maxOf(0, pdc.get(expKey, PersistentDataType.INTEGER)!!)
        minecart.persistentDataContainer.set(expKey, PersistentDataType.INTEGER, 0)
        player.giveExp(exp)
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