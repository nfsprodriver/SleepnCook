package events

import org.bukkit.NamespacedKey
import org.bukkit.block.Furnace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class FurnaceExtract(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onFurnaceExtract(event: InventoryOpenEvent) {
        if (event.inventory.location?.block?.state is Furnace) {
            val furnace: Furnace = event.inventory.location!!.block.state as Furnace
            val nightXpKey = NamespacedKey(plugin, "nightXpKey")
            val nightXp: Float = furnace.persistentDataContainer.getOrDefault(nightXpKey, PersistentDataType.FLOAT, 0.0F)
            val player: Player = event.player as Player
            player.giveExp(nightXp.toInt())
            furnace.persistentDataContainer.set(nightXpKey, PersistentDataType.FLOAT, 0.0F)
            furnace.update()
        }
    }
}