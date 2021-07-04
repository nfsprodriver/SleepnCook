import events.FurnaceExtract
import events.TimeSkip
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class SleepnCook : JavaPlugin(){

    override fun onEnable() {
        server.pluginManager.registerEvents(TimeSkip(this), this)
        server.pluginManager.registerEvents(FurnaceExtract(this), this)
    }
}