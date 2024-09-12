package logisticspipes.world.inventory;

import logisticspipes.world.inventory.slot.PhantomSlot;
import logisticspipes.world.level.block.entity.BasicPipeBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BasicPipeMenu extends LogisticsPipesMenu {

  public BasicPipeMenu(int id, Inventory inventory, BasicPipeBlockEntity blockEntity) {
    super(LogisticsPipesMenuTypes.BASIC_PIPE.get(), id, inventory.player, blockEntity::isStillValid);

    for (int i = 0; i < 9; i++) {
      this.addSlot(new PhantomSlot(blockEntity.getInvPattern(), i, 8 + i * 18, 18));
    }

    this.addInventorySlots(inventory, 142);
  }
}
