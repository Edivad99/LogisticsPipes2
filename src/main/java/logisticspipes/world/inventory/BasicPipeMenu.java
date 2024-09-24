package logisticspipes.world.inventory;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.world.inventory.slot.PhantomSlot;
import logisticspipes.world.level.block.entity.BasicPipeBlockEntity;
import lombok.Getter;
import net.minecraft.world.entity.player.Inventory;

@Getter
public class BasicPipeMenu extends LogisticsPipesMenu {

  private final BasicPipeBlockEntity blockEntity;
  private final ModuleItemSink module;

  public BasicPipeMenu(int id, Inventory inventory, BasicPipeBlockEntity blockEntity) {
    super(LogisticsPipesMenuTypes.BASIC_PIPE.get(), id, inventory.player, blockEntity::isStillValid);
    this.blockEntity = blockEntity;
    this.module = blockEntity.pipe.getLogisticsModule();

    var requestedItemsInventory = this.module.getRequestedItemsInventory();
    for (int i = 0; i < requestedItemsInventory.getContainerSize(); i++) {
      this.addSlot(new PhantomSlot(requestedItemsInventory, i, 8 + i * 18, 18));
    }

    this.addInventorySlots(inventory, 142);
  }
}
