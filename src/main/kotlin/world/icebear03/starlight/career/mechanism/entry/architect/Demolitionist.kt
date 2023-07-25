package world.icebear03.starlight.career.mechanism.entry.architect

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.*
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.util.Vector
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.giveItem
import world.icebear03.starlight.career.getSkillLevel
import world.icebear03.starlight.career.hasBranch
import world.icebear03.starlight.career.hasEureka
import world.icebear03.starlight.career.mechanism.discharge.defineDischarge
import world.icebear03.starlight.career.mechanism.discharge.isDischarging
import world.icebear03.starlight.career.mechanism.display
import world.icebear03.starlight.career.mechanism.limit.LimitType
import java.util.*

object DemolitionistActive {

    fun initialize() {
        "气浪行者".defineDischarge { id, level ->
            val duration = 2 + level
            val percent = 35 + 15 * level
            "§a技能 ${id.display()} §7释放成功，接下来§e${duration}秒§7内受到爆炸伤害减少§e${percent}%"
        }
        "手摇TNT火炮".defineDischarge { id, level ->
            "§d顿悟 ${id.display()} §7释放成功，接下来§e30秒§7内箭矢变为TNT"
        }
        "前沿爆破线".defineDischarge { id, level ->
            val amount = 2 + level
            val percent = 1.5 + 0.5 * level
            val eye = this.eyeLocation.direction.multiply(3)
            for (i in 1..amount) {
                val velocity = eye.clone().add(Vector(Math.random() * 0.5, Math.random() * 0.5, Math.random() * 0.5))
                this.location.shootPrimedTNT(velocity)
            }
            "§a技能 ${id.display()} §7释放成功，发射出§e${amount}个§7点燃的TNT"
        }
    }

    val tnts = mutableListOf<UUID>()

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun arrowSpawn(event: EntitySpawnEvent) {
        val arrow = event.entity
        if (arrow !is Arrow)
            return
        val shooter = arrow.shooter
        if (shooter !is Player)
            return

        if (shooter.isDischarging("手摇TNT火炮"))
            submit {
                arrow.location.shootPrimedTNT(arrow.velocity)
                arrow.remove()
            }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun tntExplode(event: EntityExplodeEvent) {
        val uuid = event.entity.uniqueId
        if (tnts.contains(uuid)) {
            event.blockList().clear()
            tnts.remove(uuid)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun damaged(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity !is Player)
            return
        if (entity.isDischarging("气浪行者")) {
            val percent = 35 + 15 * entity.getSkillLevel("气浪行者")
            event.damage = event.damage * (1 - percent / 100.0)
        }
    }
}

object DemolitionistPassive {

    val specialRecipes = mutableListOf<NamespacedKey>()

    fun initialize() {
        val key = NamespacedKey.minecraft("crystal_special")
        val recipe = ShapedRecipe(key, ItemStack(Material.END_CRYSTAL))
        recipe.shape("aba", "aba", "aba")
        recipe.setIngredient('a', Material.GLASS)
        recipe.setIngredient('b', Material.TNT)
        Bukkit.removeRecipe(key)
        Bukkit.addRecipe(recipe)
        specialRecipes += key
    }

    //TODO 考虑同化
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun event(event: CraftItemEvent) {
        val player = event.whoClicked as Player

        val recipe = event.recipe
        if (recipe !is ShapedRecipe)
            return

        val key = recipe.key
        if (specialRecipes.contains(key)) {
            if (!player.hasEureka("末影硝酸甘油")) {
                event.isCancelled = true
                player.closeInventory()
                player.sendMessage("§a生涯系统 §7>> 必须激活§d顿悟 " + "末影硝酸甘油".display() + " §7才可以使用此特殊合成")
            }
        }

        val type = recipe.result.type

        if (DemolitionistSet.TNT.types.contains(type)) {
            if (player.hasEureka("精炼炸药")) {
                if (Math.random() <= 0.2) {
                    player.giveItem(ItemStack(type))
                    player.sendMessage("§a生涯系统 §7>> 合成爆炸物品时获得了额外的产物")
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun igniteBlock(event: BlockIgniteEvent) {
        val player = event.player ?: return
        if (!player.hasBranch("爆破师")) {
            if (Math.random() >= 0.2)
                submit {
                    event.block.type = Material.AIR
                    player.sendMessage("§a生涯系统 §7>> 使用打火石失败，请解锁§e职业分支 ${"爆破师".display()} §7以提高成功率")
                }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun igniteCreeper(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity !is Creeper)
            return
        val type = player.inventory.itemInMainHand.type
        if (type != Material.FLINT_AND_STEEL &&
            type != Material.FIRE_CHARGE
        )
            return
        if (!player.hasBranch("爆破师")) {
            event.isCancelled = true

            player.sendMessage("§a生涯系统 §7>> 无法点燃苦力怕，需要解锁 ${"爆破师".display()}")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun damagedByExplosion(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity !is Player)
            return

        if (event.cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION &&
            event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
        )
            return

        if (entity.hasBranch("爆破师")) {
            event.damage = event.damage * 0.8
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun craftItem(event: CraftItemEvent) {
        val player = event.whoClicked as Player

        val type = event.recipe.result.type

        if (type == Material.TNT || type == Material.TNT_MINECART) {
            val level = player.getSkillLevel("稳定三硝基甲苯")

            val failPercent = when (level) {
                1 -> 0.25
                2 -> 0.1
                3 -> 0.0
                else -> 0.5
            }

            if (Math.random() <= failPercent) {
                event.isCancelled = true
                event.inventory.clear()
                player.closeInventory()

                if (Math.random() <= failPercent) {
                    val trace = player.rayTraceBlocks(5.0) ?: return
                    val block = trace.hitBlock ?: return
                    val loc = block.location
                    val world = block.world
                    block.type = Material.AIR
                    world.createExplosion(loc, 3.0F)

                    player.sendMessage("§a生涯系统 §7>> 炸药合成时爆炸了，升级技能 ${"稳定三硝基甲苯".display()} §7以规避")
                } else {
                    player.sendMessage("§a生涯系统 §7>> 炸药合成失败，升级技能 ${"稳定三硝基甲苯".display()} §7以提升成功率")
                }
            }
        }
    }
}

enum class DemolitionistSet(
    val types: List<Material>,
    val limits: List<Pair<LimitType, Pair<String, Int>>>
) {
    ENDER_CRYSTAL(
        listOf(
            Material.END_CRYSTAL
        ), listOf(
            LimitType.USE to ("爆破师" to 0)
        )
    ),
    TNT(
        listOf(
            Material.TNT,
            Material.TNT_MINECART
        ), listOf(
            LimitType.CRAFT to ("爆破师" to 0),
            LimitType.PLACE to ("爆破师" to 0),
            LimitType.USE to ("爆破师" to 0)
        )
    ),
    FIRE(
        listOf(
            Material.FIRE_CHARGE
        ),
        listOf(
            LimitType.CRAFT to ("爆破师" to 0),
            LimitType.USE to ("爆破师" to 0)
        )
    )
}

fun Location.shootPrimedTNT(velocity: Vector, fuseTicks: Int = 100, breakBlocks: Boolean = false) {
    val tnt = this.world!!.spawnEntity(this, EntityType.PRIMED_TNT) as TNTPrimed
    submit {
        tnt.velocity = velocity
        tnt.fuseTicks = fuseTicks
        tnt.isGlowing = true
        if (!breakBlocks) {
            DemolitionistActive.tnts += tnt.uniqueId
        }
    }
}