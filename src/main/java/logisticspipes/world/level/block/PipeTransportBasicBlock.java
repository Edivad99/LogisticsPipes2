package logisticspipes.world.level.block;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.mojang.serialization.MapCodec;
import logisticspipes.world.level.block.entity.LogisticsTileGenericPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

public class PipeTransportBasicBlock extends BaseEntityBlock {
  private static final MapCodec<PipeTransportBasicBlock> CODEC = simpleCodec(PipeTransportBasicBlock::new);

  public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);
  public static final EnumProperty<PipeRenderModel> MODEL_TYPE =
      EnumProperty.create("model_type", PipeRenderModel.class);
  public static final Map<Direction, BooleanProperty> CONNECTION =
      Arrays.stream(Direction.values())
          .collect(Collectors.toMap(key -> key, key -> BooleanProperty.create("connection_" + key.ordinal())));

  public static final float PIPE_MIN_POS = 0.1875F;
  public static final float PIPE_MAX_POS = 0.8125F;

  public static final AABB PIPE_CENTER_BB =
      new AABB(PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MAX_POS, PIPE_MAX_POS);
  public static final List<AABB> PIPE_CONN_BB = Arrays.asList(
      new AABB(PIPE_MIN_POS, 0, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MIN_POS, PIPE_MAX_POS),
      new AABB(PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MIN_POS, PIPE_MAX_POS, 1, PIPE_MAX_POS),
      new AABB(PIPE_MIN_POS, PIPE_MIN_POS, 0, PIPE_MAX_POS, PIPE_MAX_POS, PIPE_MIN_POS),
      new AABB(PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MAX_POS, PIPE_MAX_POS, 1),
      new AABB(0, PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MAX_POS),
      new AABB(PIPE_MAX_POS, PIPE_MIN_POS, PIPE_MIN_POS, 1, PIPE_MAX_POS, PIPE_MAX_POS)
  );

  public enum PipeRenderModel implements StringRepresentable {
    NONE,
    REQUEST_TABLE;

    @Override
    public String getSerializedName() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public PipeTransportBasicBlock(Properties properties) {
    super(properties);
    BlockState state = this.stateDefinition.any()
        .setValue(ROTATION, 0)
        .setValue(MODEL_TYPE, PipeRenderModel.NONE);
    for (var property : CONNECTION.values()) {
      state = state.setValue(property, false);
    }
    this.registerDefaultState(state);
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(ROTATION, MODEL_TYPE);
    CONNECTION.values().forEach(builder::add);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new LogisticsTileGenericPipe(pos, state);
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }
}
