package world.icebear03.starlight.recipe

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

val shapedRecipes = mutableListOf<ShapedRecipe>()
val shapelessRecipes = mutableListOf<ShapelessRecipe>()

fun shapedRecipe(
    key: NamespacedKey,
    result: ItemStack,
    rows: List<String>,
    vararg ingredients: Pair<Char, Material>
): ShapedRecipe {
    val recipe = ShapedRecipe(key, result)
    recipe.shape(*rows.toTypedArray())
    ingredients.forEach {
        recipe.setIngredient(it.first, it.second)
    }
    Bukkit.removeRecipe(key)
    Bukkit.addRecipe(recipe)
    shapedRecipes += recipe
    return recipe
}

fun shapelessRecipe(
    key: NamespacedKey,
    result: ItemStack,
    vararg ingredients: Pair<Int, Material>
): ShapelessRecipe {
    val recipe = ShapelessRecipe(key, result)
    ingredients.forEach {
        recipe.addIngredient(it.first, it.second)
    }
    Bukkit.removeRecipe(key)
    Bukkit.addRecipe(recipe)
    shapelessRecipes += recipe
    return recipe
}