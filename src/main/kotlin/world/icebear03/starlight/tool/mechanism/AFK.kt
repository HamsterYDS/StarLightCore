package world.icebear03.starlight.tool.mechanism

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.onlinePlayers
import world.icebear03.starlight.station.mechanism.StaminaModifier
import java.util.*

object AFK {

    val afkPlayers = mutableListOf<UUID>()
    val checking = mutableListOf<UUID>()
    val checked = mutableMapOf<UUID, Long>()

    fun initialize() {
        submit(period = 20) {
            onlinePlayers.forEach { player ->
                if (QTEProvider.isQTEing(player))
                    return@forEach
                if (isAFKing(player) || checking.contains(player.uniqueId))
                    return@forEach
                if (player.isSleeping || player.isGliding)
                    return@forEach
                if (System.currentTimeMillis() - (checked[player.uniqueId] ?: 0) <= 10 * 60 * 1000)
                    return@forEach

                val ran = if (StaminaModifier.resting.contains(player.uniqueId)) 0.001 else 0.0002

                if (Math.random() <= ran) {
                    checking += player.uniqueId

                    var tot = 0
                    submit(period = 20L) {
                        if (tot++ >= 5) {
                            cancel()
                            player.closeInventory()
                            QTEProvider.sendQTE(
                                player,
                                QTEProvider.QTEDifficulty.HARD,
                                QTEProvider.QTEType.ONE_TIME,
                                {
                                    checking -= player.uniqueId
                                    if (it != QTEProvider.QTEResult.ACCEPTED) {
                                        sendTitle("", "§7检测到挂机，重新进入服务器以刷新状态", 0, 86400 * 20, 0)
                                        afkPlayers += uniqueId
                                    } else {
                                        checked[player.uniqueId] = System.currentTimeMillis()
                                    }
                                },
                                "§b挂机检测"
                            )
                        } else {
                            player.sendMessage("§b繁星工坊 §7>> 挂机检测将在§e${5 - tot}秒§7后出现！")
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        afkPlayers -= player.uniqueId
        player.sendTitle("", "§7", 0, 5, 0)
    }

    fun isAFKing(player: Player): Boolean {
        return afkPlayers.contains(player.uniqueId)
    }
}