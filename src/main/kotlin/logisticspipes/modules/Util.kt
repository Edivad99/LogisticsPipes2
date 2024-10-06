package logisticspipes.modules

import logisticspipes.Configs

/**
 * Checks inventory size, everyNthTick and configuration values
 * to determine the number of slot accesses per tick.
 *
 * @param inventorySize the size of the connected inventory.
 * @return 0, if no work can be done and a value greater zero otherwise.
 */
fun determineSlotsPerTick(everyNthTick: Int, inventorySize: Int): Int {
  var slotsPerTick = 0
  if (inventorySize > 0) {
    slotsPerTick = (inventorySize / everyNthTick).coerceAtLeast(Configs.MINIMUM_INVENTORY_SLOT_ACCESS_PER_TICK)
  }
  if (Configs.MAXIMUM_INVENTORY_SLOT_ACCESS_PER_TICK > 0) {
    slotsPerTick = slotsPerTick.coerceAtMost(Configs.MAXIMUM_INVENTORY_SLOT_ACCESS_PER_TICK)
  }
  return slotsPerTick
}

