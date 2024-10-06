package logisticspipes.modules;

import java.util.Collection;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILevelProvider;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class LogisticsModule implements INBTSerializable<CompoundTag> {

  @Nullable
  protected ILevelProvider level;
  @Nullable
  protected IPipeServiceProvider service;

  @Nullable
  @Getter
  protected ModulePositionType slot;
  @Getter
  protected int positionInt;

  /**
   * Registers the Inventory and ItemSender to the module
   *
   * @param level   that the module is in.
   * @param service Inventory access, power and utility functions provided by the pipe.
   */
  public void registerHandler(ILevelProvider level, IPipeServiceProvider service) {
    this.level = level;
    this.service = service;
  }

  public void registerPosition(ModulePositionType slot, int positionInt) {
    this.slot = slot;
    this.positionInt = positionInt;
  }

  public BlockPos getPos() {
    if (this.service == null) {
      if (LogisticsPipes.isDebug()) {
        throw new IllegalStateException("Module has no service, but getPos was called");
      }
      return BlockPos.ZERO;
    } else if (slot != null && slot.isInWorld()) {
      return this.service.getPos();
    } else {
      if (LogisticsPipes.isDebug()) {
        throw new IllegalStateException("Module is not in world, but getPos was called");
      }
      return BlockPos.ZERO;
    }
  }

  @Nullable
  public Level getLevel() {
    if (this.level == null) {
      return null;
    }
    return this.level.getLevel();
  }

  public LogisticsModule getModule() {
    return this;
  }

  protected ISlotUpgradeManager getUpgradeManager() {
    return Objects.requireNonNull(this.service, "service object was null in " + this)
        .getUpgradeManager(slot, positionInt);
  }

  /**
   * is this module a valid destination for bounced items.
   */
  public abstract boolean receivePassive();

  /**
   * Gives a sink answer on the given itemstack
   *
   * @param stack              to sink
   * @param item               to sink
   * @param bestPriority       best priority seen so far
   * @param bestCustomPriority best custom sub-priority
   * @param allowDefault       is a default only sink allowed to sink this?
   * @param includeInTransit   include the "in transit" items? -- true for a destination
   *                           search, false for a sink check.
   * @param forcePassive       check for passive routing only, in case this method is redirected to other sinks
   * @return SinkReply whether the module sinks the item or not
   */
  @Nullable
  public SinkReply sinksItem(ItemStack stack, ItemIdentifier item, int bestPriority,
      int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
    return null;
  }

  /**
   * A tick for the Module
   */
  public abstract void tick();

  @Getter
  public enum ModulePositionType {
    SLOT(true),
    IN_HAND(false),
    IN_PIPE(true);

    private final boolean inWorld;

    ModulePositionType(boolean inWorld) {
      this.inWorld = inWorld;
    }
  }

  /**
   * Is this module interested in all items, or just some specific ones?
   *
   * @return true: this module will be checked against every item request
   * false: only requests involving items collected by {@link #collectSpecificInterests(Collection)} will be checked
   */
  public abstract boolean hasGenericInterests();

  /**
   * Collects the items which this module is capable of providing or supplying
   * (or is otherwise interested in)
   *
   * @param itemIdentifiers the collection to add the interests to
   */
  public void collectSpecificInterests(Collection<ItemIdentifier> itemIdentifiers) {
  }
}
