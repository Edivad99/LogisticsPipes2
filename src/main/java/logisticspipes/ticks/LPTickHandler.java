package logisticspipes.ticks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.MapMaker;
import logisticspipes.grow.ServerTickDispatcher;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.DoubleCoordinates;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

//TODO:MOVE
public class LPTickHandler {

  public static int adjChecksDone = 0;

  @SubscribeEvent
  public void clientTick(ClientTickEvent.Post event) {
    //FluidIdentifier.initFromForge(true);
    //SimpleServiceLocator.clientBufferHandler.clientTick();
    //MainProxy.proxy.tickClient();
    //DebugGuiController.instance().execClient();
  }

  @SubscribeEvent
  public void serverTick(ServerTickEvent.Post event) {
    //HudUpdateTick.tick();
    //SimpleServiceLocator.serverBufferHandler.serverTick();
    MainProxy.addTick();
    LPTickHandler.adjChecksDone = 0;
    //DebugGuiController.instance().execServer();
    ServerTickDispatcher.INSTANCE.tick();
  }

  private static final Map<Level, LPWorldInfo> LEVEL_INFO = new MapMaker().weakKeys().makeMap();

  @SubscribeEvent
  public void worldTick(LevelTickEvent.Post event) {
    if (event.getLevel().isClientSide) {
      return;
    }
    LPWorldInfo info = LPTickHandler.getWorldInfo(event.getLevel());
    info.worldTick++;
  }

  public static LPWorldInfo getWorldInfo(Level level) {
    LPWorldInfo info = LEVEL_INFO.get(level);
    if (info == null) {
      info = new LPWorldInfo();
      LEVEL_INFO.put(level, info);
    }
    return info;
  }

  @Getter
  @Data
  public static class LPWorldInfo {

    @Setter(value = AccessLevel.PRIVATE)
    private long worldTick = 0;
    private Set<DoubleCoordinates> updateQueued = new HashSet<>();

    @Setter
    private boolean skipBlockUpdateForWorld = false;
  }
}
