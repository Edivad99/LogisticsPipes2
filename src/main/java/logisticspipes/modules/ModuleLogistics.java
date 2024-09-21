package logisticspipes.modules;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class ModuleLogistics implements INBTSerializable<CompoundTag> {

  @Nullable
  protected Level level;
  @Nullable
  protected BlockPos pos;

  public abstract void registerModule(Level level, BlockPos pos);

}
