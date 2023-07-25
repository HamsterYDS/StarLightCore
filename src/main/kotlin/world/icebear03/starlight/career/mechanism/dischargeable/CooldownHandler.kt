package world.icebear03.starlight.career.mechanism.dischargeable

import org.bukkit.entity.Player
import taboolib.common5.format
import taboolib.platform.util.actionBar
import world.icebear03.starlight.career.mechanism.display
import java.util.*


val cooldownStamps = mutableMapOf<UUID, MutableMap<String, Long>>()
fun Player.addCooldownStamp(key: String) {
    cooldownStamps.getOrPut(this.uniqueId) { mutableMapOf() }[key] = System.currentTimeMillis()
}

fun Player.clearCooldownStamp(key: String? = null) {
    if (key == null)
        cooldownStamps.remove(this.uniqueId)
    else
        cooldownStamps.getOrPut(this.uniqueId) { mutableMapOf() }[key]
}

//pair#first 冷却是否结束，冷却中为false
//pair#second 冷却若未结束，离结束还剩下的时间（秒）
fun Player.checkStamp(key: String, cdInSec: Double, info: Boolean = false): Pair<Boolean, Double> {
    val stamp = (cooldownStamps[this.uniqueId] ?: return true to 0.0)[key] ?: return true to 0.0
    val period = (System.currentTimeMillis() - stamp) / 1000.0
    val left = (cdInSec - period).format(1)

    if (info) {
        this.actionBar("无法释放技能 ${key.display()}，还需等待 ${left}秒")
    }

    return (period >= cdInSec) to left
}