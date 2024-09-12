package logisticspipes.world.inventory;

import java.util.function.Predicate;
import logisticspipes.world.inventory.slot.LogisticsPipesSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class LogisticsPipesMenu extends AbstractContainerMenu {

  private final Player player;

  private final Predicate<Player> validator;

  protected LogisticsPipesMenu(MenuType<?> type, int id, Player player, Predicate<Player> validator) {
    super(type, id);
    this.player = player;
    this.validator = validator;
  }

  @Override
  public boolean stillValid(Player player) {
    return this.validator.test(player);
  }

  protected final void addInventorySlots(Inventory inventory) {
    this.addInventorySlots(inventory, 166);
  }

  protected final void addInventorySlots(Inventory inventory, int guiHeight) {
    for (int i = 0; i < 3; i++) {
      for (int k = 0; k < 9; k++) {
        this.addSlot(new Slot(inventory, k + i * 9 + 9, 8 + k * 18, guiHeight - 82 + i * 18));
      }
    }
    for (int j = 0; j < 9; j++) {
      this.addSlot(new Slot(inventory, j, 8 + j * 18, guiHeight - 24));
    }
  }

  @Override
  public void clicked(int slotId, int mouseButton, ClickType clickType, Player player) {
    if (slotId >= 0) {
      if (this.slots.get(slotId) instanceof LogisticsPipesSlot slot && slot.isPhantom()) {
        this.slotClickPhantom(slot, mouseButton, clickType, player);
      }
    }
    super.clicked(slotId, mouseButton, clickType, player);
  }

  private void slotClickPhantom(LogisticsPipesSlot slot, int mouseButton,
      ClickType clickType, Player player) {
    if (mouseButton == 2) {
      if (slot.canAdjustPhantom()) {
        slot.set(ItemStack.EMPTY);
      }
    } else if (mouseButton == 0 || mouseButton == 1) {
      var containerMenu = player.containerMenu;
      slot.setChanged();
      ItemStack stackSlot = slot.getItem();
      ItemStack stackHeld = containerMenu.getCarried();

      if (stackSlot.isEmpty() && !stackHeld.isEmpty() && slot.mayPlace(stackHeld)) {
        fillPhantomSlot(slot, stackHeld, mouseButton);
      } else if (stackHeld.isEmpty()) {
        adjustPhantomSlot(slot, mouseButton, clickType);
        slot.onTake(player, containerMenu.getCarried());
      }
    }
  }

  private void adjustPhantomSlot(LogisticsPipesSlot slot, int mouseButton, ClickType clickType) {
    if (!slot.canAdjustPhantom()) {
      return;
    }
    ItemStack stackSlot = slot.getItem();
    if (stackSlot.isEmpty()) {
      return;
    }
    int stackSize;
    if (clickType == ClickType.QUICK_MOVE) {
      stackSize = mouseButton == 0 ? (stackSlot.getCount() + 1) / 2 : stackSlot.getCount() * 2;
    } else {
      stackSize = mouseButton == 0 ? stackSlot.getCount() - 1 : stackSlot.getCount() + 1;
    }

    if (stackSize > slot.getMaxStackSize()) {
      stackSize = slot.getMaxStackSize();
    }

    stackSlot.setCount(stackSize);

    if (stackSlot.isEmpty()) {
      slot.set(ItemStack.EMPTY);
    }
  }

  private void fillPhantomSlot(LogisticsPipesSlot slot, ItemStack stackHeld, int mouseButton) {
    if (!slot.canAdjustPhantom()) {
      return;
    }
    int stackSize = mouseButton == 0 ? stackHeld.getCount() : 1;
    if (stackSize > slot.getMaxStackSize()) {
      stackSize = slot.getMaxStackSize();
    }
    var phantomStack = stackHeld.copy();
    phantomStack.setCount(stackSize);

    slot.set(phantomStack);
  }


  private boolean tryMoveItemStackTo(ItemStack itemStack, int endIndex) {
    boolean changed = false;
    if (itemStack.isStackable()) {
      for (int i = 0; !itemStack.isEmpty() && i < endIndex; i++) {
        var slot = this.slots.get(i);
        var stackInSlot = slot.getItem();
        if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, stackInSlot)) {
          int j = stackInSlot.getCount() + itemStack.getCount();
          int maxSize = Math.min(slot.getMaxStackSize(), itemStack.getMaxStackSize());
          if (j <= maxSize) {
            itemStack.setCount(0);
            stackInSlot.setCount(j);
            slot.setChanged();
            changed = true;
          } else if (stackInSlot.getCount() < maxSize) {
            itemStack.shrink(maxSize - stackInSlot.getCount());
            stackInSlot.setCount(maxSize);
            slot.setChanged();
            changed = true;
          }
        }
      }
    }

    if (!itemStack.isEmpty()) {
      for (int i = 0; i < endIndex; i++) {
        var slot = this.slots.get(i);
        var itemstack = slot.getItem();
        if (itemstack.isEmpty() && mayPlace(slot, itemStack)) {
          if (itemStack.getCount() > slot.getMaxStackSize()) {
            slot.setByPlayer(itemStack.split(slot.getMaxStackSize()));
          } else {
            slot.setByPlayer(itemStack.split(itemStack.getCount()));
          }

          slot.setChanged();
          changed = true;
          break;
        }
      }
    }

    return changed;
  }

  private static boolean mayPlace(Slot slot, ItemStack itemStack) {
    /*if (slot instanceof RailcraftSlot railcraftSlot) {
      if (railcraftSlot.isPhantom()) {
        return false;
      }
      if (!railcraftSlot.canShift()) {
        return false;
      }
    }*/
    return slot.mayPlace(itemStack);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slotIndex) {
    var originalStack = ItemStack.EMPTY;
    var slot = this.slots.get(slotIndex);
    final int numSlots = this.slots.size();
    final int slotsAdded = numSlots - 9 * 4;
    if (slot.hasItem() && slotsAdded > 0) {
      var stackInSlot = slot.getItem();
      originalStack = stackInSlot.copy();
      if (slotIndex < slotsAdded) { // Custom slots to vanilla inventory slots
        if (!this.moveItemStackTo(stackInSlot, slotsAdded, numSlots, false)) {
          return ItemStack.EMPTY;
        }
      } else { // Vanilla inventory slots to custom slots
        if (!this.tryMoveItemStackTo(stackInSlot, slotsAdded)) {
          return ItemStack.EMPTY;
        }
      }

      if (stackInSlot.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }
    return originalStack;
  }
}
