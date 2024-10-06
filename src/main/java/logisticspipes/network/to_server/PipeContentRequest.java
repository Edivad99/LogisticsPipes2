package logisticspipes.network.to_server;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.to_client.PipeContentPacket;
import logisticspipes.transport.LPTravelingItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PipeContentRequest implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<PipeContentRequest> TYPE =
      new CustomPacketPayload.Type<>(LogisticsPipes.rl("pipe_content_request"));

  public static StreamCodec<RegistryFriendlyByteBuf, PipeContentRequest> STREAM_CODEC =
      StreamCodec.ofMember(PipeContentRequest::encode, PipeContentRequest::new);

  public final int travelId;

  public PipeContentRequest(int travelId) {
    this.travelId = travelId;
  }

  public PipeContentRequest(RegistryFriendlyByteBuf buffer) {
    this.travelId = buffer.readVarInt();
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeVarInt(this.travelId);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(PipeContentRequest message, IPayloadContext iPayloadContext) {
    var ref = LPTravelingItem.SERVER_LIST.get(message.travelId);
    LPTravelingItem.LPTravelingItemServer item = null;
    if (ref != null) {
      item = ref.get();
    }
    if (item != null) {
      PacketDistributor.sendToPlayer((ServerPlayer) iPayloadContext.player(),
          new PipeContentPacket(item.getItemIdentifierStack(), item.getId()));
    }
  }
}
