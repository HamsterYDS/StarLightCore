package world.icebear03.starlight.other

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleSneakEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.sendActionBar
import world.icebear03.starlight.other.QTEProvider.QTEType.*
import java.util.*
import kotlin.math.roundToInt

// POWERED BY 白熊_IceBear
// GPL v3.0
object QTEProvider {

    val shiftMap = mutableMapOf<UUID, Boolean>()

    fun sendQTE(
        player: Player,
        difficulty: QTEDifficulty,
        type: QTEType,
        function: Player.(suc: Boolean) -> Unit,
        title: String = "",
        subtitle: String = "§7请完成下方校准 (在§e合适时机§7下蹲)"
    ) {
        player.sendTitle(title, subtitle)

        val uuid = player.uniqueId
        shiftMap[uuid] = false

        //格式
        val format = "&7>&f {bar} &7<"
        //QTE总长度，如果要修改此处，请一起修改下方的intervalStart计算公式，否则有可能超下标
        val total = 100

        //QTE读条速度（负相关）
        val period = difficulty.period
        //QTE成功区间长度
        val interval = difficulty.interval

        //QTE成功区间开头的index
        var intervalStart = ((0.2 + Math.random() * 0.6) * total).roundToInt()

        //QTE总时长
        val ticks = when (type) {
            ONE_TIME -> 1 * total * period
            TWO_TIMES -> 2 * total * period
            THREE_TIMES -> 3 * total * period
        }

        //失败次数
        var failTime = 0
        //计时器
        var tot = 0
        var lastTickChance = 1
        submit(period = 1L) {
            //完成QTE，并进行下一步操作
            fun finish(result: Boolean? = null) {
                result?.let { function.invoke(player, result) }
                shiftMap.remove(uuid)
                cancel()
            }

            //目前是第几次
            val chance = tot / (total * period) + 1

            //重置校准位置
            if (chance != lastTickChance) {
                lastTickChance = chance
                intervalStart = ((0.2 + Math.random() * 0.6) * total).roundToInt()
            }

            //玩家不能完成QTE了
            if (player.isDead || !player.isOnline) {
                finish()
                return@submit
            }

            //时间超了，或者玩家放弃了
            tot += difficulty.mag
            if (tot >= ticks || !shiftMap.containsKey(uuid)) {
                finish(false)
                return@submit
            }

            //玩家响应了
            if (shiftMap[uuid]!!) {
                val intervalThisTime = when (chance) {
                    1 -> intervalStart
                    2 -> total * 2 - intervalStart - interval
                    3 -> total * 2 + intervalStart
                    else -> 0
                } * period

                if (tot in intervalThisTime..intervalThisTime + interval * period) {
                    finish(true)
                    return@submit
                } else {
                    failTime += 1
                    if (failTime >= type.time) {
                        finish(false)
                        return@submit
                    }
                    shiftMap[uuid] = false
                }
            }

            //发送下一单位的QTE状态条
            if (tot % period == 0) {
                var bar = ""
                repeat(100) {
                    val isPassed = when (chance) {
                        1 -> it <= tot / period
                        2 -> it > 2 * total - tot / period
                        3 -> it <= tot / period - total * 2
                        else -> false
                    }
                    bar += if (isPassed) SymbolType.PASSED.colored[chance - 1]
                    else if (it in intervalStart..intervalStart + interval) SymbolType.INTERVAL.colored[chance - 1]
                    else SymbolType.WAITING.colored[chance - 1]
                }
                player.sendActionBar(format.replace("{bar}", bar).colored())
            }
        }
    }

    @SubscribeEvent
    fun shift(event: PlayerToggleSneakEvent) {
        val player = event.player
        if (!player.isSneaking)
            shiftMap[player.uniqueId] = true
    }

    enum class SymbolType(val colored: List<String>) {
        WAITING(listOf("&7|", "&6|", "&c|")),
        INTERVAL(listOf("&e|", "&e|", "&e|")),
        PASSED(listOf("&6|", "&c|", "&4|"))
    }

    enum class QTEDifficulty(val period: Int, val interval: Int, val mag: Int = 1) {
        EASY(3, 15),
        HARD(2, 12),
        CHAOS(1, 9),
        GLITCH(1, 6, 2),
        BETA(1, 3, 2)
    }

    //给玩家几次机会，增加容错率
    enum class QTEType(val time: Int) {
        ONE_TIME(1),
        TWO_TIMES(2),
        THREE_TIMES(3)
    }
}