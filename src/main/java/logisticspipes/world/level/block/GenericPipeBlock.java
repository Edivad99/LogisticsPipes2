package logisticspipes.world.level.block;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.tags.LogisticsPipesTags;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;

public abstract class GenericPipeBlock extends BaseEntityBlock {

  public static final Map<Direction, BooleanProperty> CONNECTION =
      Arrays.stream(Direction.values())
          .collect(Collectors.toMap(key -> key, key -> BooleanProperty.create("connection_" + key.getName())));

  private static final VoxelShape CENTER = box(4, 4, 4, 12, 12, 12);
  private static final Map<Direction, VoxelShape> END = new HashMap<>() {
    {
      put(Direction.NORTH, box(4, 4, 0, 12, 12, 4));
      put(Direction.SOUTH, box(4, 4, 12, 12, 12, 16));
      put(Direction.EAST, box(12, 4, 4, 16, 12, 12));
      put(Direction.WEST, box(0, 4, 4, 4, 12, 12));
      put(Direction.UP, box(4, 12, 4, 12, 16, 12));
      put(Direction.DOWN, box(4, 0, 4, 12, 4, 12));
    }
  };

  public GenericPipeBlock(Properties properties) {
    super(properties);
    BlockState state = this.stateDefinition.any();
    for (var property : CONNECTION.values()) {
      state = state.setValue(property, false);
    }
    this.registerDefaultState(state);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    CONNECTION.values().forEach(builder::add);
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
      CollisionContext context) {
    var shape = CENTER;
    for (var value : Direction.values()) {
      if (state.getValue(CONNECTION.get(value))) {
        shape = Shapes.or(shape, END.get(value));
      }
    }
    return shape;
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return getState(context.getLevel(), context.getClickedPos());
  }

  @Override
  public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState,
      LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
    return getState(level, currentPos);
  }

  @Override
  public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
      BlockPos fromPos, boolean isMoving) {
    super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    level.setBlockAndUpdate(pos, getState(level, pos));
  }

  private BlockState getState(LevelAccessor level, BlockPos pos) {
    boolean isConnectToNorth = checkBlock(level, pos.north(), Direction.NORTH);
    boolean isConnectToEast = checkBlock(level, pos.east(), Direction.EAST);
    boolean isConnectToSouth = checkBlock(level, pos.south(), Direction.SOUTH);
    boolean isConnectToWest = checkBlock(level, pos.west(), Direction.WEST);
    boolean isConnectToUp = checkBlock(level, pos.above(), Direction.UP);
    boolean isConnectToDown = checkBlock(level, pos.below(), Direction.DOWN);

    return defaultBlockState()
        .setValue(CONNECTION.get(Direction.NORTH), isConnectToNorth)
        .setValue(CONNECTION.get(Direction.EAST), isConnectToEast)
        .setValue(CONNECTION.get(Direction.SOUTH), isConnectToSouth)
        .setValue(CONNECTION.get(Direction.WEST), isConnectToWest)
        .setValue(CONNECTION.get(Direction.UP), isConnectToUp)
        .setValue(CONNECTION.get(Direction.DOWN), isConnectToDown);
  }

  private boolean checkBlock(LevelAccessor levelAccessor, BlockPos pos, Direction direction) {
    if (levelAccessor.getBlockState(pos).getBlock() instanceof GenericPipeBlock) {
      return true;
    }
    if (levelAccessor instanceof Level level) {
      return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null) != null;
    }
    return false;
    //return block instanceof GenericPipeBlock || block instanceof ChestBlock;
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
      boolean movedByPiston) {
    super.onPlace(state, level, pos, oldState, movedByPiston);
    if (level instanceof ServerLevel) {
      var blockEntity = level.getBlockEntity(pos);
      if (blockEntity instanceof LogisticsGenericPipeBlockEntity<?> blockEntityPipe) {
        blockEntityPipe.initialize();
      }
    }
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
      BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    if (player instanceof ServerPlayer) {
      if (stack.is(LogisticsPipesTags.Items.WRENCH)) {
        if (level.getBlockEntity(pos) instanceof MenuProvider menuProvider) {
          player.openMenu(menuProvider, pos);
        }
      }
    }
    return ItemInteractionResult.sidedSuccess(level.isClientSide());
  }

  public static boolean isValid(@Nullable CoreUnroutedPipe pipe) {
    return GenericPipeBlock.isFullyDefined(pipe);
  }

  public static boolean isFullyDefined(@Nullable CoreUnroutedPipe pipe) {
    return pipe != null && pipe.container != null;
  }
}
