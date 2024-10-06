package logisticspipes.modules

import logisticspipes.interfaces.IInventoryUtil
import logisticspipes.interfaces.IPipeServiceProvider
import logisticspipes.interfaces.ISlotUpgradeManager
import logisticspipes.logisticspipes.NeighborBlockEntity
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity

fun IPipeServiceProvider.availableSneakyInventories(sneakyDirection: Direction?): List<IInventoryUtil?> =
  sneakyDirection?.let {
    availableAdjacent.inventories().map { adjacent ->
      adjacent.sneakyInsertion().from(sneakyDirection).inventoryUtil
    }
  } ?: availableInventories()

fun IPipeServiceProvider.availableSneakyInventories(upgradeManager: ISlotUpgradeManager): List<IInventoryUtil?> =
  availableAdjacent.inventories().map { adjacent -> adjacent.sneakyInsertion().from(upgradeManager).inventoryUtil }

fun IPipeServiceProvider.availableInventories(): List<IInventoryUtil?> =
  availableAdjacent.inventories().map(NeighborBlockEntity<BlockEntity>::getInventoryUtil)
