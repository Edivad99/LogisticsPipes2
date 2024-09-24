package logisticspipes.modules;

import org.jetbrains.annotations.Nullable;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILevelProvider;
import logisticspipes.interfaces.IPipeServiceProvider;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
}
