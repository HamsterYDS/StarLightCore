package world.icebear03.starlight.career.spell.discharge

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.serverct.parrot.parrotx.function.textured
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.hasName
import taboolib.platform.util.modifyMeta
import world.icebear03.starlight.career
import world.icebear03.starlight.utils.shapelessRecipe

object ShortcutDischarge {

    val signalItem = ItemStack(Material.PLAYER_HEAD)
        .textured("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2E5Y2IwNDU3ZDUwMTVkZmJkM2UyNTJkNzY3MDcxMjc1OTEwNjNhMGIyZmViYWY4YzY0NGFjYWRhOTBiZDRkMCJ9fX0=")
        .modifyMeta<ItemMeta> {
            setDisplayName("§b职业信物")
            lore = listOf(
                "§8| §7将此物置于副手，按下交换键(默认F)时",
                "§8| §7即可施放快捷栏格子编号对应的技能",
                "§8| §c蹲下时不触发此判定",
                ""
            )
        }

    fun initialize() {
        shapelessRecipe(
            NamespacedKey.minecraft("career_core"),
            signalItem.clone(),
            1 to Material.IRON_INGOT,
            1 to Material.GOLD_INGOT
        )
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun swapItem(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (player.isSneaking)
            return
        val inv: PlayerInventory = player.inventory
        val item = inv.itemInOffHand
        if (item.hasName()) {
            if (item.itemMeta!!.displayName == "§b职业信物") {
                event.isCancelled = true
                val slot = inv.heldItemSlot + 1
                val msg = triggerShortcut(player, slot) ?: return
                player.sendMessage("§a生涯系统 §7>> $msg")
            }
        }
    }

    fun triggerShortcut(player: Player, key: Int): String? {
        val career = player.career()
        val name = career.shortCuts[key] ?: return "该按键未绑定任何可释放的§a技能§7/§d顿悟"
        return DischargeHandler.discharge(player, name)
    }
}