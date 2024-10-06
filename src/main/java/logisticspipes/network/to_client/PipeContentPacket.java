package logisticspipes.network.to_client;

import java.lang.ref.WeakReference;
import logisticspipes.LogisticsPipes;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Tuple;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PipeContentPacket implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<PipeContentPacket> TYPE =
      new CustomPacketPayload.Type<>(LogisticsPipes.rl("pipe_content"));

  public static StreamCodec<RegistryFriendlyByteBuf, PipeContentPacket> STREAM_CODEC =
      StreamCodec.ofMember(PipeContentPacket::encode, PipeContentPacket::new);

  private final ItemIdentifierStack stack;
  private final int travelId;

  public PipeContentPacket(ItemIdentifierStack stack, int travelId) {
    this.stack = stack;
    this.travelId = travelId;
  }

  public PipeContentPacket(RegistryFriendlyByteBuf buffer) {
    this.stack = ItemIdentifierStack.STREAM_CODEC.decode(buffer);
    this.travelId = buffer.readInt();
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    ItemIdentifierStack.STREAM_CODEC.encode(buffer, stack);
    buffer.writeInt(travelId);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(PipeContentPacket message, IPayloadContext iPayloadContext) {
    var ref = LPTravelingItem.CLIENT_LIST.get(message.travelId);
    LPTravelingItem.LPTravelingItemClient content = null;
    if (ref != null) {
      content = ref.get();
    }
    if (content == null) {
      content = new LPTravelingItem.LPTravelingItemClient(message.travelId, message.stack);
      LPTravelingItem.CLIENT_LIST.put(message.travelId, new WeakReference<>(content));
      synchronized (LPTravelingItem.FORCE_KEEP) {
        LPTravelingItem.FORCE_KEEP.add(new Tuple<>(10, content)); //Keep in memory for min 10 ticks
      }
    } else {
      content.setItem(message.stack);
    }
  }
}
