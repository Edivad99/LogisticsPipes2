package logisticspipes.utils;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.ICoordinates;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Data
public class DoubleCoordinates implements ICoordinates {

  private double xCoord;
  private double yCoord;
  private double zCoord;

  public DoubleCoordinates() {
    this(0, 0, 0);
  }

  public DoubleCoordinates(BlockEntity blockEntity) {
    this(blockEntity.getBlockPos());
  }

  public DoubleCoordinates(CoreUnroutedPipe pipe) {
    this(pipe.getPos());
  }

  public DoubleCoordinates(IPipeInformationProvider pipe) {
    this(pipe.getX(), pipe.getY(), pipe.getZ());
  }

  public DoubleCoordinates(BlockPos blockPos) {
    this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
  }

  public DoubleCoordinates(double xCoord, double yCoord, double zCoord) {
    setXCoord(xCoord);
    setYCoord(yCoord);
    setZCoord(zCoord);
  }

  public DoubleCoordinates(ICoordinates coords) {
    this(coords.getXDouble(), coords.getYDouble(), coords.getZDouble());
  }

  public DoubleCoordinates add(DoubleCoordinates toAdd) {
    setXCoord(getXCoord() + toAdd.getXCoord());
    setYCoord(getYCoord() + toAdd.getYCoord());
    setZCoord(getZCoord() + toAdd.getZCoord());
    return this;
  }

  @Override
  public double getXDouble() {
    return getXCoord();
  }

  @Override
  public double getYDouble() {
    return getYCoord();
  }

  @Override
  public double getZDouble() {
    return getZCoord();
  }

  @Nullable
  public BlockEntity getBlockEntity(Level level) {
    return level.getBlockEntity(getBlockPos());
  }

  public BlockState getBlockState(LevelAccessor levelAccessor) {
    return levelAccessor.getBlockState(getBlockPos());
  }

  public BlockPos getBlockPos() {
    return new BlockPos(this.getXInt(), this.getYInt(), this.getZInt());
  }

  public DoubleCoordinates center() {
    DoubleCoordinates coords = new DoubleCoordinates();
    coords.setXCoord(getXInt() + 0.5);
    coords.setYCoord(getYInt() + 0.5);
    coords.setYCoord(getZInt() + 0.5);
    return this;
  }
}
