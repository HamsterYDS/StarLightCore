package world.icebear03.starlight.career.spell.handler.listener

import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.isMainhand
import taboolib.platform.util.isRightClick
import world.icebear03.starlight.career.spell.handler.EventHandler
import world.icebear03.starlight.career.spell.handler.internal.HandlerType

object Interact {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun clickBlock(event: PlayerInteractEvent) {
        if (!event.hasBlock())
            return
        if (!event.isRightClick())
            return
        val type = event.clickedBlock!!.type
        val player = event.player

        val useResult = EventHandler.checkLimit(HandlerType.USE, player, type)
        if (!useResult.first) {
            event.isCancelled = true
            if (event.isMainhand()) {
                player.sendMessage("§a生涯系统 §7>> 无法使用此方块，需要解锁以下其中之一: ")
                useResult.second.forEach {
                    player.sendMessage("               §7|—— $it")
                }
            }
            return
        }

        event.isCancelled = !EventHandler.triggerLowest(event, type, HandlerType.USE, player)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun clickItem(event: PlayerInteractEvent) {
        if (!event.hasItem())
            return
        val type = event.item!!.type
        val player = event.player

        val useResult = EventHandler.checkLimit(HandlerType.USE, player, type)
        if (!useResult.first) {
            event.isCancelled = true
            player.sendMessage("§a生涯系统 §7>> 无法使用此物品，需要解锁以下其中之一: ")
            useResult.second.forEach {
                player.sendMessage("               §7|—— $it")
            }
            return
        }

        event.isCancelled = !EventHandler.triggerLowest(event, type, HandlerType.USE, player)
    }
}