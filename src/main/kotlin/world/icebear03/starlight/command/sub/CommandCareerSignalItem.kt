package world.icebear03.starlight.command.sub

import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.spell.old.DischargeHandler


val commandCareerSignalItem = subCommand {
    execute<Player> { sender, _, _ ->
        sender.giveItem(DischargeHandler.item.clone())
        sender.sendMessage("§a生涯系统 §7>> §b职业信物§7已经发送至背包")
    }
}