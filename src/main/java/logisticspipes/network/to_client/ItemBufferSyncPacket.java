package logisticspipes.network.to_client;

import java.util.List;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.to_server.ImportItemSinkMessage;
import logisticspipes.network.to_server.SetDefaultRouteItemSinkMessage;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.tuples.Triplet;
import logisticspipes.world.level.block.entity.BasicPipeBlockEntity;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ItemBufferSyncPacket extends ListSyncPacket<Triplet<ItemStack, Tuple<Integer, Integer>, LPTravelingItem.LPTravelingItemServer>> {

  public static final CustomPacketPayload.Type<ItemBufferSyncPacket> TYPE =
      new CustomPacketPayload.Type<>(LogisticsPipes.rl("item_buffer_sync"));

  public static StreamCodec<RegistryFriendlyByteBuf, ItemBufferSyncPacket> STREAM_CODEC =
      StreamCodec.ofMember(ItemBufferSyncPacket::encode, ItemBufferSyncPacket::new);

  public ItemBufferSyncPacket(BlockPos blockPos,
      List<Triplet<ItemStack, Tuple<Integer, Integer>, LPTravelingItem.LPTravelingItemServer>> list) {
    super(blockPos, list);
  }

  public ItemBufferSyncPacket(RegistryFriendlyByteBuf buffer) {
    super(buffer);
  }

  @Override
  public Triplet<ItemStack, Tuple<Integer, Integer>, LPTravelingItem.LPTravelingItemServer> decode(RegistryFriendlyByteBuf buffer) {
    var itemStack = ItemStack.STREAM_CODEC.decode(buffer);
    return new Triplet<>(itemStack, null, null);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer,
      Triplet<ItemStack, Tuple<Integer, Integer>, LPTravelingItem.LPTravelingItemServer> value) {
    ItemStack.STREAM_CODEC.encode(buffer, value.getA());
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(ItemBufferSyncPacket message, IPayloadContext context) {
    LogisticsGenericPipeBlockEntity<?> pipe = message.getPipe(context.player().level());
    if (pipe == null) {
      return;
    }
    pipe.pipe.getTransport().itemBuffer.clear();
    pipe.pipe.getTransport().itemBuffer.addAll(message.getList());
  }
}
