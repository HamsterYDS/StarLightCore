package world.icebear03.starlight.career.mechanism.listener

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.career.mechanism.limit.MaterialLimitLibrary
import world.icebear03.starlight.career.mechanism.limit.checkAbility

object SinterItem {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: InventoryClickEvent) {
        val inv = event.inventory
        if (inv.type != InventoryType.FURNACE &&
            inv.type != InventoryType.BLAST_FURNACE
        )
            return

        if (event.slotType != InventoryType.SlotType.RESULT)
            return

        val item = event.currentItem ?: return

        val type = item.type

        val player = event.whoClicked as Player
        
        val result = player.checkAbility(MaterialLimitLibrary.sinterLimits[type])
        if (!result.first) {
            event.isCancelled = true

            player.closeInventory()
            player.sendMessage("无法烧炼获得此方块，需要解锁以下条件其中之一: ")
            result.second.forEach {
                player.sendMessage("                - $it")
            }
        }
    }
}