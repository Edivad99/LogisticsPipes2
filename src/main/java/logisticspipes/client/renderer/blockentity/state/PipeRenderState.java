package logisticspipes.client.renderer.blockentity.state;

import java.util.Arrays;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import logisticspipes.utils.CoordinateUtils;
import logisticspipes.utils.DoubleCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

public class PipeRenderState {
  public enum LocalCacheType {
    QUADS
  }

  public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
  public final TextureMatrix textureMatrix = new TextureMatrix();

  //public List<RenderEntry> cachedRenderer = null;
  public Cache<LocalCacheType, Object> objectCache = CacheBuilder.newBuilder().build();
  public int cachedRenderIndex = -1;
  public boolean forceRenderOldPipe = false;
  private boolean[] solidSidesCache = new boolean[6];

  public int[] buffer = null;
  //public Map<ResourceLocation, GLRenderList> renderLists;

  private boolean dirty = true;

  public PipeRenderState() {
  }

  public void clean() {
    dirty = false;
    pipeConnectionMatrix.clean();
    textureMatrix.clean();
    clearRenderCaches();
  }

  public boolean isDirty() {
    return dirty || pipeConnectionMatrix.isDirty() || textureMatrix.isDirty();
  }

  public boolean needsRenderUpdate() {
    return pipeConnectionMatrix.isDirty() || textureMatrix.isDirty();
  }

  public void checkForRenderUpdate(LevelAccessor worldIn, BlockPos blockPos) {
    boolean[] solidSides = new boolean[6];
    for (Direction dir : Direction.values()) {
      DoubleCoordinates pos = CoordinateUtils.add(new DoubleCoordinates(blockPos), dir);
      var blockSide = pos.getBlockState(worldIn);
      if (blockSide.isFaceSturdy(worldIn, pos.getBlockPos(), dir.getOpposite()) && !pipeConnectionMatrix.isConnected(dir)) {
        solidSides[dir.ordinal()] = true;
      }
    }
    if (!Arrays.equals(solidSides, solidSidesCache)) {
      solidSidesCache = solidSides.clone();
      clearRenderCaches();
    }
  }

  public void clearRenderCaches() {
    //cachedRenderer = null;
    objectCache.invalidateAll();
    objectCache.cleanUp();
  }
}
