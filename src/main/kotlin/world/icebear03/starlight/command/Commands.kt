package world.icebear03.starlight.command

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.expansion.createHelper
import world.icebear03.starlight.command.sub.commandCareerMenu
import world.icebear03.starlight.command.sub.commandCareerPoint
import world.icebear03.starlight.command.sub.commandCareerReset
import world.icebear03.starlight.command.sub.commandCareerSignalItem

@CommandHeader(name = "starlightcore", aliases = ["sl", "slc"])
object Commands {

    @CommandBody
    val main = mainCommand {
        createHelper(checkPermissions = true)
    }

    @CommandBody
    val careerMenu = commandCareerMenu

    @CommandBody(permission = "starlightcore.career.point")
    val careerPoint = commandCareerPoint

    @CommandBody
    val careerSignalItem = commandCareerSignalItem

    @CommandBody
    val careerReset = commandCareerReset
}