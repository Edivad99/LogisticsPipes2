package logisticspipes.world.level.block.entity;

import org.jetbrains.annotations.Nullable;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.ConnectionType;
import logisticspipes.world.level.block.GenericPipeBlock;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class LogisticsGenericPipeBlockEntity<T extends CoreUnroutedPipe> extends BlockEntity implements
    IPipeInformationProvider {

  public T pipe;
  @Getter
  private boolean initialized = false;
  private boolean pipeBound = false;

  public LogisticsGenericPipeBlockEntity(BlockEntityType<?> type, BlockPos pos,
      BlockState blockState, T pipe) {
    super(type, pos, blockState);
    this.pipe = pipe;
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    this.pipe.saveAdditional(tag, registries);
    if (pipe instanceof CoreRoutedPipe routedPipe) {
      var module = routedPipe.getLogisticsModule();
      tag.put("module", module.serializeNBT(registries));
    }
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    this.pipe.loadAdditional(tag, registries);
    if (pipe instanceof CoreRoutedPipe routedPipe) {
      var module = routedPipe.getLogisticsModule();
      module.deserializeNBT(registries, tag.getCompound("module"));
    }
  }

  @Override
  public boolean canConnect(BlockEntity to, Direction direction, boolean ignoreSystemDisconnection) {
    return pipe.canPipeConnect(to, direction);
  }

  public void initialize() {
    this.bindPipe();
    //this.computeConnections();
    //this.scheduleRenderUpdate();

    if (this.pipe.needsInit()) {
      this.pipe.initialize();
    }
    this.initialized = true;
  }

  @Override
  public void setRemoved() {
    if (this.pipe == null) {
      this.remove = false;
      this.initialized = false;
      //this.tileBuffer = null;
      super.setRemoved();
    } else if (!this.pipe.preventRemove()) {
      this.remove = true;
      this.initialized = false;
      //this.tileBuffer = null;
      this.pipe.setRemoved();
      super.setRemoved();
    }
  }

  @Override
  public void clearRemoved() {
    super.clearRemoved();
    this.initialized = false;
    //this.tileBuffer = null;
    this.bindPipe();
    if (this.pipe != null) {
      this.pipe.validate();
    }
  }

  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    if (this.pipe != null) {
      this.pipe.onChunkUnload();
    }
  }

  private void bindPipe() {
    if (!this.pipeBound && pipe != null) {
      //pipe.setTile(this);
      this.pipeBound = true;
    }
  }

  public boolean canPipeConnect(@Nullable BlockEntity with, Direction direction) {
    if (this.level.isClientSide) {
      return false;
    }

    if (with == null) {
      return false;
    }

    if (!GenericPipeBlock.isValid(pipe)) {
      return false;
    }

    if (with instanceof LogisticsGenericPipeBlockEntity) {
      CoreUnroutedPipe otherPipe = ((LogisticsGenericPipeBlockEntity<?>) with).pipe;

      if (!(GenericPipeBlock.isValid(otherPipe))) {
        return false;
      }

      if (!(otherPipe.canPipeConnect(this, direction.getOpposite()))) {
        return false;
      }
    }
    return pipe.canPipeConnect(with, direction);
  }

  @Override
  public boolean isCorrect(ConnectionType connectionType) {
    return true;
  }

  public boolean isStillValid(Player player) {
    return isStillValid(this, player, 64);
  }

  public static boolean isStillValid(BlockEntity blockEntity, Player player, int maxDistance) {
    var pos = blockEntity.getBlockPos();
    var distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
    return !blockEntity.isRemoved()
        && blockEntity.getLevel().getBlockEntity(pos).equals(blockEntity)
        && distance <= maxDistance;
  }

  @Override
  public final CompoundTag getUpdateTag(HolderLookup.Provider provider) {
    return this.saveWithoutMetadata(provider);
  }
}
