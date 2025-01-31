package logisticspipes.pipes.basic.debug;

import java.util.ArrayList;
import java.util.List;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.utils.PlayerCollectionList;
import net.minecraft.world.entity.player.Player;

public class DebugLogController {

  private static int nextID = 0;
  private final int ID = DebugLogController.nextID++;
  public final CoreUnroutedPipe pipe;
  public boolean debugThisPipe = false;
  private List<StatusEntry> oldList = new ArrayList<>();
  private PlayerCollectionList players = new PlayerCollectionList();

  public DebugLogController(CoreUnroutedPipe pipe) {
    this.pipe = pipe;
  }

  public void log(String info) {
    System.out.println(info);
    if (players.isEmptyWithoutCheck()) {
      return;
    }
    //MainProxy.sendToPlayerList(PacketHandler.getPacket(SendNewLogLine.class).setWindowID(ID)
    // .setLine(info), players);
  }

  public void tick() {
    if (players.isEmpty()) {
      return;
    }
    generateStatus();
  }

  public void generateStatus() {
    List<StatusEntry> status = new ArrayList<>();
    pipe.addStatusInformation(status);
    if (!status.equals(oldList)) {
      //MainProxy.sendToPlayerList(PacketHandler.getPacket(UpdateStatusEntries.class).setWindowID
      // (ID).setStatus(status), players);
      oldList = status;
    }
  }

  public void openForPlayer(Player player) {
    players.add(player);
    List<StatusEntry> status = new ArrayList<>();
    pipe.addStatusInformation(status);
    //MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SendNewLogWindow.class).setWindowID
    // (ID).setTitle(pipe.toString()), player);
    //MainProxy.sendPacketToPlayer(PacketHandler.getPacket(UpdateStatusEntries.class).setWindowID
    // (ID).setStatus(status), player);
  }
}
