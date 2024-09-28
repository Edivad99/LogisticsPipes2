package logisticspipes.fromkotlin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public class PipeInventoryConnectionChecker {
  private final Set<Class<?>> allowedConnectionClasses = new HashSet<>();
  private final Map<Class<? extends BlockEntity>, Boolean> cachedClasses = new HashMap<>();

  public PipeInventoryConnectionChecker() {
    allowedConnectionClasses.add(HopperBlockEntity.class);
    //checkAndAddClass("gregtech", "gregtech.api.block.BlockStateTileEntity");
  }

  /*private void checkAndAddClass(String modId, String className) {
    if (Loader.isModLoaded(modId)) {
      try {
        Class<?> clazz = Class.forName(className);
        addSupportedClassType(clazz);
      } catch (ClassNotFoundException e) {
        // Handle exception if needed
      }
    }
  }*/

  /*public void addSupportedClassType(Class<?> clazz) {
    allowedConnectionClasses.add(clazz);
  }*/

  public boolean shouldLPProvideInventoryTo(BlockEntity blockEntity) {
    return cachedClasses.computeIfAbsent(blockEntity.getClass(), k -> {
      Class<?> clazz = k;
      while (clazz.getSuperclass() != Object.class) {
        if (allowedConnectionClasses.contains(clazz)) {
          return true;
        }
        clazz = clazz.getSuperclass();
      }
      return false;
    });
  }
}

