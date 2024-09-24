package logisticspipes.modules;

import logisticspipes.pipes.PipeLogisticsChassis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class ChassisModule extends LogisticsModule {

  private final PipeLogisticsChassis parentChassis;

  public ChassisModule(int moduleCount, PipeLogisticsChassis parentChassis) {
    this.parentChassis = parentChassis;
    this.registerPosition(ModulePositionType.IN_PIPE, 0);
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    return new CompoundTag();
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

  }
}
