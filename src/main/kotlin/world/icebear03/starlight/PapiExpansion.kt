package world.icebear03.starlight

import org.bukkit.entity.Player
import taboolib.common5.format
import taboolib.platform.compat.PlaceholderExpansion
import taboolib.platform.compat.replacePlaceholder
import world.icebear03.starlight.career.display
import world.icebear03.starlight.career.getSpell
import world.icebear03.starlight.career.spell.checkCooldownStamp
import world.icebear03.starlight.career.spell.isDischarging
import world.icebear03.starlight.other.NearestPlayer
import world.icebear03.starlight.station.mechanism.StaminaModifier
import world.icebear03.starlight.station.mechanism.StationMechanism
import world.icebear03.starlight.station.station
import world.icebear03.starlight.utils.secToFormattedTime
import world.icebear03.starlight.utils.secondLived
import world.icebear03.starlight.utils.toRoman

object PapiExpansion : PlaceholderExpansion {

    override val autoReload: Boolean
        get() = true

    override val identifier: String
        get() = "starlight"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null)
            return args
        val career = player.career()
        if (args.startsWith("career_shortcut_")) {
            val int = args.replace("career_shortcut_", "").toInt()
            val name = career.shortCuts[int] ?: return ""
            val spell = getSpell(name)!!

            val level = career.getSpellLevel(name)
            val cd = spell.cd(level)
            val duration = spell.duration(level)

            val cdPair = player.checkCooldownStamp(name, cd)

            val state = when (cdPair.first) {
                true -> "&a✔"
                false -> {
                    if (player.isDischarging(name, false)) {
                        if (duration != -1) {
                            "&e✷ &7(&b${(duration - (cd - cdPair.second)).format(1)}秒&7)"
                        } else {
                            "&e✷"
                        }
                    } else {
                        "&c✘ &7(&e${cdPair.second}s&7)"
                    }
                }
            }

            return "$int &7- ${display(name)} &7> $state"
        }

        if (args.startsWith("station_halo_")) {
            val index = args.replace("station_halo_", "").toInt()
            val pairs = StationMechanism.haloMap[player.uniqueId]!!.toList()
            if (index > pairs.size)
                return ""
            val pair = pairs[index - 1]
            return "&e${pair.first} &7> &a+${pair.second.format(1)}/秒"
        }
        return when (args) {
            "tag" -> "%deluxetags_tag%".replacePlaceholder(player).ifEmpty { "&6跋涉者" }
            "career_points" -> career.points.toString()
            "career_resonate" -> career.resonantBranch?.display() ?: "&7无"
            "career_resonate_tag" -> career.resonantBranch?.display() ?: "&e玩家"
            "period_alive" -> player.secondLived().secToFormattedTime()
            "nearest_player" -> NearestPlayer.nearestMap.getOrDefault(player.uniqueId, "无" to "N/A").first
            "nearest_distance" -> NearestPlayer.nearestMap.getOrDefault(player.uniqueId, "无" to "N/A").second
            "stamina" -> player.stamina().display()
            "stamina_magnification" -> "×" + (StaminaModifier.magnificationMap[player.uniqueId] ?: 1.0).format(2)
            "station_level" -> player.station().level.toRoman()
            "station_cd" -> {
                val result = player.station().checkStamp()
                if (result.first) {
                    "&a✔ &7可放置/回收"
                } else {
                    "&c✘ &7${result.second.secToFormattedTime()}"
                }
            }

            "station_state" -> {
                player.station().location?.let { "&b驻扎中" } ?: "&e未驻扎"
            }

            else -> args
        }
    }
}