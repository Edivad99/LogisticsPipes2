package logisticspipes.network.to_client;

import java.util.List;
import logisticspipes.LogisticsPipes;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Triplet;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Tuple;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ItemBufferSyncPacket extends ListSyncPacket<Triplet<ItemIdentifierStack, Tuple<Integer, Integer>, LPTravelingItem.LPTravelingItemServer>> {

  public static final CustomPacketPayload.Type<ItemBufferSyncPacket> TYPE =
      new CustomPacketPayload.Type<>(LogisticsPipes.rl("item_buffer_sync"));

  public static StreamCodec<RegistryFriendlyByteBuf, ItemBufferSyncPacket> STREAM_CODEC =
      StreamCodec.ofMember(ItemBufferSyncPacket::encode, ItemBufferSyncPacket::new);

  public ItemBufferSyncPacket(BlockPos blockPos,
      List<Triplet<ItemIdentifierStack, Tuple<Integer, Integer>, LPTravelingItem.LPTravelingItemServer>> list) {
    super(blockPos, list);
  }

  public ItemBufferSyncPacket(RegistryFriendlyByteBuf buffer) {
    super(buffer);
  }

  @Override
  public Triplet<ItemIdentifierStack, Tuple<Integer, Integer>, LPTravelingItem.LPTravelingItemServer> decode(RegistryFriendlyByteBuf buffer) {
    var itemIdentifierStack = ItemIdentifierStack.STREAM_CODEC.decode(buffer);
    return new Triplet<>(itemIdentifierStack, null, null);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer,
      Triplet<ItemIdentifierStack, Tuple<Integer, Integer>, LPTravelingItem.LPTravelingItemServer> value) {
    ItemIdentifierStack.STREAM_CODEC.encode(buffer, value.getA());
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(ItemBufferSyncPacket message, IPayloadContext context) {
    LogisticsGenericPipeBlockEntity<?> blockEntity = message.getPipe(context.player().level());
    blockEntity.pipe.getTransport().itemBuffer.clear();
    blockEntity.pipe.getTransport().itemBuffer.addAll(message.getList());
  }
}
