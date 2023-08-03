package world.icebear03.starlight.career.spell.entry.farmer

import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.countItem
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.*
import world.icebear03.starlight.career.spell.handler.internal.HandlerType
import world.icebear03.starlight.utils.effect
import world.icebear03.starlight.utils.isDischarging
import world.icebear03.starlight.utils.takeItem

object Botanist {

    val saplings = Material.values().filter { it.toString().contains("SAPLING") }

    //方块
    val crops = mapOf(
        Material.WHEAT to Material.WHEAT,
        Material.PUMPKIN to Material.PUMPKIN,
        Material.MELON to Material.MELON,
        Material.BEETROOTS to Material.BEETROOT,
        Material.CARROTS to Material.CARROT,
        Material.POTATOES to Material.POTATO,
        Material.SWEET_BERRIES to Material.SWEET_BERRIES,
        Material.GLOW_BERRIES to Material.GLOW_BERRIES
    )

    val seeds = listOf(
        Material.WHEAT_SEEDS,
        Material.PUMPKIN_SEEDS,
        Material.MELON_SEEDS,
        Material.BEETROOT_SEEDS,
        Material.COCOA_BEANS,
        Material.TORCHFLOWER_SEEDS,
        Material.PITCHER_POD
    )

    val leaves = Material.values().filter { it.toString().contains("LEAVES") }

    fun initialize() {
        addLimit(HandlerType.PLACE, "植物学家" to 0, *saplings.toTypedArray())
        addLimit(HandlerType.USE, "植物学家" to 0, Material.BONE_MEAL)
        addLimit(HandlerType.USE, "植物学家" to 0, Material.COMPOSTER)

        crops.keys.toList().addHighListener(HandlerType.BREAK) { _, player, type ->
            val level = player.spellLevel("合理密植")
            val rate = 0.1 + 0.05 * level
            if (level >= 0 && Math.random() <= rate) {
                player.giveItem(ItemStack(crops[type]!!))
                "§a技能 ${display("合理密植")} §7使得本次收割时获得额外作物"
            } else null
        }

        "科学施肥".discharge { name, _ ->
            "§a技能 ${display(name)} §7释放成功，接下来一段时间内施肥有概率不消耗骨粉"
        }

        "植物遗传学".discharge { name, level ->
            finish(name)
            val amount = minOf(640, inventory.countItem { seeds.contains(it.type) })
            takeItem(amount) { seeds.contains(it.type) }
            effect(PotionEffectType.FAST_DIGGING, amount / 2, if (level == 3) 3 else 2)
            "§a技能 ${display(name)} §7释放成功，消耗${amount}个种子获取急迫效果"
        }

        leaves.addHighListener(HandlerType.BREAK) { event, player, _ ->
            val breakEvent = event as BlockBreakEvent
            val block = breakEvent.block
            val world = block.world
            if (player.meetRequirement("于荫求果") && Math.random() <= 0.1) {
                world.dropItem(block.location, ItemStack(Material.APPLE))
                "§d顿悟 ${display("于荫求果")} §7使得本次破坏树叶时额外掉落苹果"
            } else null
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun breakItem(event: PlayerItemBreakEvent) {
        val type = event.brokenItem.type
        val player = event.player
        if (type == Material.NETHERITE_HOE && player.meetRequirement("终极奉献")) {
            player.giveItem(ItemStack(type))
            player.sendMessage("§a生涯系统 §7>> §d顿悟 ${display("终极奉献")} §7使得您获得了一把全新的下界合金锄")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun fertilize(event: PlayerItemConsumeEvent) {
        val type = event.item.type
        val player = event.player
        if (type == Material.BONE_MEAL && player.isDischarging("科学施肥")) {
            val level = player.spellLevel("科学施肥")
            if (Math.random() <= 0.1 * level) {
                player.sendMessage("§a生涯系统 §7>> §a技能 ${display("科学施肥")} §7使得本次施肥时不消耗骨粉")
                player.giveItem(ItemStack(type))
            }
        }
    }
}