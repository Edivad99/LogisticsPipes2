package logisticspipes.interfaces;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.SinkReply;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

public interface ISendRoutedItem {

  default int getSourceID() {
    return getRouter().getSimpleID();
  }

  IRouter getRouter();

  IRoutedItem sendStack(ItemStack stack, Tuple<Integer, SinkReply> reply, CoreRoutedPipe.ItemSendMode mode, Direction direction);

  IRoutedItem sendStack(ItemStack stack, int destination, CoreRoutedPipe.ItemSendMode mode, IAdditionalTargetInformation info, Direction direction);

  default IRoutedItem sendStack(ItemStack stack, int destRouterId, SinkReply sinkReply, CoreRoutedPipe.ItemSendMode itemSendMode, Direction direction) {
    return sendStack(stack, new Tuple<>(destRouterId, sinkReply), itemSendMode, direction);
  }
}
