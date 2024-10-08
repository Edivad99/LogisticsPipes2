package logisticspipes.interfaces;

import net.minecraft.world.item.ItemStack;

public interface IItemAdvancedExistence {

  boolean canExistInNormalInventory(ItemStack stack);

  boolean canExistInWorld(ItemStack stack);
}
