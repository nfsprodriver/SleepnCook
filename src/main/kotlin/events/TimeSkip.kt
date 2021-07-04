package events;

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.TimeSkipEvent
import org.bukkit.inventory.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.min

class TimeSkip(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun timeSkip(event: TimeSkipEvent) {
        if (event.skipReason != TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            return
        }
        event.world.loadedChunks.forEach { chunk ->
            chunk.tileEntities.filterIsInstance<Furnace>().forEach { furnace ->
                val fuel: ItemStack? = furnace.inventory.fuel
                val source: ItemStack? = furnace.inventory.smelting
                if (fuel != null && source != null) {
                    val recipe: CookingRecipe<*> = getRecipeBySource(source) ?: return
                    val fuelBurningTime: Short = fuelBurningTimes.getOrDefault(fuel.type, 300)
                    val result: ItemStack? = furnace.inventory.result
                    var fuelRemAmount: Int = (event.skipAmount / fuelBurningTime).toInt()
                    var fuelAddTicks: Short = (event.skipAmount % fuelBurningTime).toShort()
                    var sourceRemAmount: Int = (event.skipAmount / furnace.cookTimeTotal).toInt()
                    var sourceAddTicks: Short = (event.skipAmount % furnace.cookTimeTotal).toShort()
                    if (fuelAddTicks > furnace.burnTime) {
                        fuelRemAmount++
                        fuelAddTicks = (fuelAddTicks - furnace.burnTime).toShort()
                    }
                    if (sourceAddTicks > furnace.cookTime) {
                        sourceRemAmount++
                        sourceAddTicks = (sourceAddTicks - furnace.cookTime).toShort()
                    }
                    fuelRemAmount = min(fuelRemAmount, fuel.amount)
                    fuel.amount = fuel.amount.minus(fuelRemAmount)
                    sourceRemAmount = min(sourceRemAmount, source.amount)
                    source.amount = source.amount.minus(sourceRemAmount)
                    furnace.burnTime = furnace.burnTime.plus(fuelAddTicks).toShort()
                    furnace.cookTime = furnace.cookTime.plus(sourceAddTicks).toShort()
                    val recipeResult: ItemStack = recipe.result.clone()
                    recipeResult.amount = (result?.amount ?: 0) + sourceRemAmount
                    val recipeXp: Float = recipe.experience
                    val nightXpKey = NamespacedKey(plugin, "nightXpKey")
                    val nightXp: Float = furnace.persistentDataContainer.getOrDefault(nightXpKey, PersistentDataType.FLOAT, 0.0F)
                    furnace.persistentDataContainer.set(nightXpKey, PersistentDataType.FLOAT, nightXp + recipeXp * sourceRemAmount)
                    furnace.update()
                    furnace.inventory.fuel = fuel
                    furnace.inventory.smelting = source
                    furnace.inventory.result = recipeResult
                }
            }
        }
    }

    private fun getRecipeBySource(source: ItemStack): CookingRecipe<*>? {
        Bukkit.recipeIterator().forEach { recipe ->
            if (recipe is CookingRecipe<*> && recipe.input.type == source.type) {
                return recipe
            }
        }
        return null
    }

    private val fuelBurningTimes: Map<Material, Short> = mapOf(
        Material.LAVA_BUCKET to 20000,
        Material.COAL_BLOCK to 16000,
        Material.DRIED_KELP_BLOCK to 4000,
        Material.BLAZE_ROD to 2400,
        Material.COAL to 1600,
        Material.CHARCOAL to 1600,
        Material.ACACIA_BOAT to 1200,
        Material.BIRCH_BOAT to 1200,
        Material.DARK_OAK_BOAT to 1200,
        Material.JUNGLE_BOAT to 1200,
        Material.OAK_BOAT to 1200,
        Material.SPRUCE_BOAT to 1200,
        Material.SCAFFOLDING to 400
    )
}
