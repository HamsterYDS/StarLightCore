package world.icebear03.starlight.career.core.spell

import world.icebear03.starlight.career.core.Basic
import world.icebear03.starlight.career.core.branch.Branch
import world.icebear03.starlight.utils.calcToInt

data class Spell(
    override val name: String,
    override val skull: String,
    override val color: String,
    val branch: Branch, //所属分支
    val cooldown: String, //冷却时间
    val duration: String, //持续时间
    val isEureka: Boolean, //是否是顿悟
    val type: SpellType, //主动还是被动
    val description: List<List<String>> //按等级 0 1 2
) : Basic() {

    fun cd(level: Int = 1): Int {
        return cooldown.calcToInt("level" to level)
    }

    fun duration(level: Int = 1): Int {
        return duration.calcToInt("level" to level)
    }

    fun description(level: Int = 1): List<String> {
        return (if (type == SpellType.ACTIVE)
            listOf(
                "&7类型: ${type.typeName}",
                "&7冷却时间: ${cd(level)}",
                "&7持续时间: ${duration(level)}",
                "&7",
            )
        else listOf("&7类型: ${type.typeName}", "&7")).also { description[level - 1] }
    }

    fun prefix(withVerb: Boolean = false): String {
        return if (withVerb) {
            if (isEureka) "§d顿悟" else "§a技能"
        } else {
            if (isEureka) "§7激活§d顿悟" else "§7升级§a技能"
        }
    }
}