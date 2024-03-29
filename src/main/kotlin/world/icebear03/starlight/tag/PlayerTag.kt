package world.icebear03.starlight.tag

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.SubscribeEvent
import world.icebear03.starlight.utils.get
import world.icebear03.starlight.utils.remove
import world.icebear03.starlight.utils.set

object PlayerTag {

    //是否已经有了
    fun addTag(player: Player, id: String): Boolean {
        val string = player["tags", PersistentDataType.STRING] ?: ""
        return if (!string.contains(id)) {
            player["tags", PersistentDataType.STRING] = "$string$id,"
            false
        } else true
    }

    fun removeTag(player: Player, id: String) {
        val string = player["tags", PersistentDataType.STRING] ?: ""
        player["tags", PersistentDataType.STRING] = string.replace("$id,", "")
    }

    fun clearTag(player: Player) {
        player.remove("current_tag")
    }

    fun tagList(player: Player): List<Tag> {
        val string = player["tags", PersistentDataType.STRING] ?: ","
        return string.split(",").filter { it.isNotBlank() }.map { TagLibrary.getTag(it)!! }
    }

    fun setTag(player: Player, id: String) {
        player["current_tag", PersistentDataType.STRING] = id
    }

    fun currentTag(player: Player): Tag? {
        return TagLibrary.getTag(player["current_tag", PersistentDataType.STRING] ?: "")
    }

    @SubscribeEvent
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        if (tagList(player).find { it.id == "跋涉者" } == null) {
            addTag(player, "跋涉者")
        }
    }
}