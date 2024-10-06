package logisticspipes.modules;

import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.world.level.block.entity.ChassiPipeBlockEntity;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class ModuleItemSink extends LogisticsModule {

  @Getter
  private final SimpleContainer requestedItemsInventory = new SimpleContainer(9);
  @Getter
  private boolean defaultRoute;

  private SinkReply sinkReply;
  private SinkReply sinkReplyDefault;

  public ModuleItemSink() {
    this.requestedItemsInventory.addListener(__ -> this.service.setChanged());
  }

  @Override
  public void registerPosition(ModulePositionType slot, int positionInt) {
    super.registerPosition(slot, positionInt);
    this.sinkReply = new SinkReply(SinkReply.FixedPriority.ITEM_SINK, 0, true, false, 1, 0,
        new ChassiPipeBlockEntity.ChassiTargetInformation(this.getPositionInt()));
    this.sinkReplyDefault = new SinkReply(SinkReply.FixedPriority.DEFAULT_ROUTE, 0, true, true, 1, 0,
        new ChassiPipeBlockEntity.ChassiTargetInformation(this.getPositionInt()));
  }

  @Override
  public @Nullable SinkReply sinksItem(ItemStack stack, ItemIdentifier item, int bestPriority,
      int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
      boolean forcePassive) {
    if (defaultRoute && !allowDefault) {
      return null;
    }
    if (bestPriority > this.sinkReply.fixedPriority.ordinal() || (bestPriority == this.sinkReply.fixedPriority.ordinal()
        && bestCustomPriority >= this.sinkReply.customPriority)) {
      return null;
    }
    if (service == null) {
      return null;
    }
    /*if (filterInventory.containsUndamagedItem(item.getUndamaged())) {
      if (service.canUseEnergy(1)) {
        return this.sinkReply;
      }
      return null;
    }*/
    final ISlotUpgradeManager upgradeManager = getUpgradeManager();
    if (upgradeManager.isFuzzyUpgrade()) {
      /*for (var filter : filterInventory.contents()) {
        if (filter == null) {
          continue;
        }
        if (filter.getValue1() == null) {
          continue;
        }
        ItemIdentifier ident1 = item;
        ItemIdentifier ident2 = filter.getValue1().getItem();
        IBitSet slotFlags = getSlotFuzzyFlags(filter.getValue2());
        if (FuzzyUtil.INSTANCE.get(slotFlags, FuzzyFlag.IGNORE_DAMAGE)) {
          ident1 = ident1.getIgnoringData();
          ident2 = ident2.getIgnoringData();
        }
        if (FuzzyUtil.INSTANCE.get(slotFlags, FuzzyFlag.IGNORE_NBT)) {
          ident1 = ident1.getIgnoringNBT();
          ident2 = ident2.getIgnoringNBT();
        }
        if (ident1.equals(ident2)) {
          if (service.canUseEnergy(5)) {
            return _sinkReply;
          }
          return null;
        }
      }*/
    }
    if (defaultRoute) {
      if (bestPriority > this.sinkReplyDefault.fixedPriority.ordinal() || (
          bestPriority == this.sinkReplyDefault.fixedPriority.ordinal()
              && bestCustomPriority >= this.sinkReplyDefault.customPriority)) {
        return null;
      }
      if (service.canUseEnergy(1)) {
        return this.sinkReplyDefault;
      }
      return null;
    }
    return null;
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    var tag = new CompoundTag();
    tag.put("requestedItemsInventory", this.requestedItemsInventory.createTag(provider));
    tag.putBoolean("defaultRoute", this.defaultRoute);
    return tag;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
    this.requestedItemsInventory.fromTag(tag.getList("requestedItemsInventory", 10), provider);
    this.defaultRoute = tag.getBoolean("defaultRoute");
  }

  public void setDefaultRoute(boolean defaultRoute) {
    this.defaultRoute = defaultRoute;
    this.service.setChanged();
  }

  @Nullable
  private IItemHandler getInventory(Direction direction) {
    var level = this.getLevel();
    if (level == null) {
      return null;
    }
    return level.getCapability(Capabilities.ItemHandler.BLOCK, this.getPos().relative(direction), null);
  }

  public boolean isNearInventory() {
    return Arrays.stream(Direction.values())
        .anyMatch(direction -> this.getInventory(direction) != null);
  }

  public void importFromAdjacentInventory() {
    for (var direction : Direction.values()) {
      var cap = this.getInventory(direction);
      if (cap == null) {
        continue;
      }
      this.requestedItemsInventory.clearContent();
      int requestedItemsIndex = 0;
      for (int i = 0; i < cap.getSlots() && requestedItemsIndex < this.requestedItemsInventory.getContainerSize(); i++) {
        var stack = cap.getStackInSlot(i);
        if (!stack.isEmpty()) {
          this.requestedItemsInventory.setItem(requestedItemsIndex++, stack.copyWithCount(1));
        }
      }
      break;
    }
  }

  @Override
  public boolean receivePassive() {
    return true;
  }

  @Override
  public void tick() {
  }

  @Override
  public boolean hasGenericInterests() {
    return this.defaultRoute;
  }
}
