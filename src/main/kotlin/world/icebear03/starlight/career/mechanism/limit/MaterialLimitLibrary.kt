package world.icebear03.starlight.career.mechanism.limit

import org.bukkit.Material
import world.icebear03.starlight.career.mechanism.limit.LimitType.*
import world.icebear03.starlight.career.mechanism.set.FortressEngineerSet
import world.icebear03.starlight.career.mechanism.set.StructuralEngineerSet

object MaterialLimitLibrary {

    //type-分支/技能/顿悟ID with 等级(顿悟不看等级)
    val craftLimits = mutableMapOf<Material, MutableList<Pair<String, Int>>>()
    val placeLimits = mutableMapOf<Material, MutableList<Pair<String, Int>>>()
    val breakLimits = mutableMapOf<Material, MutableList<Pair<String, Int>>>()
    val useLimits = mutableMapOf<Material, MutableList<Pair<String, Int>>>()
    val sinterLimits = mutableMapOf<Material, MutableList<Pair<String, Int>>>()
    val igniteLimits = mutableMapOf<Material, MutableList<Pair<String, Int>>>()
    val dropIfBreakLimits = mutableMapOf<Material, MutableList<Pair<String, Int>>>()

    fun initialize() {
        StructuralEngineerSet.values().forEach {
            install(it.types, it.limits)
        }
        FortressEngineerSet.values().forEach {
            install(it.types, it.limits)
        }
    }

    fun install(types: List<Material>, limits: List<Pair<LimitType, Pair<String, Int>>>) {
        limits.forEach { (limitType, limit) ->
            types.forEach { type ->
                createIfNotExist(type)
                when (limitType) {
                    PLACE -> placeLimits[type]!!
                    BREAK -> breakLimits[type]!!
                    USE -> useLimits[type]!!
                    CRAFT -> craftLimits[type]!!
                    SINTER -> sinterLimits[type]!!
                    IGNITE -> igniteLimits[type]!!
                    DROP_IF_BREAK -> dropIfBreakLimits[type]!!
                } += limit
            }
        }
    }

    fun createIfNotExist(type: Material) {
        if (placeLimits.containsKey(type))
            return
        placeLimits[type] = mutableListOf()
        breakLimits[type] = mutableListOf()
        useLimits[type] = mutableListOf()
        craftLimits[type] = mutableListOf()
        sinterLimits[type] = mutableListOf()
        igniteLimits[type] = mutableListOf()
        dropIfBreakLimits[type] = mutableListOf()
    }
}