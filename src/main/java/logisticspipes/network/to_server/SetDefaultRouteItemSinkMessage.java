package logisticspipes.network.to_server;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.level.block.entity.BasicPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetDefaultRouteItemSinkMessage(BlockPos pos, boolean defaultRoute) implements CustomPacketPayload {

  public static final Type<SetDefaultRouteItemSinkMessage> TYPE =
      new Type<>(LogisticsPipes.rl("set_default_route_item_sink"));

  public static final StreamCodec<FriendlyByteBuf, SetDefaultRouteItemSinkMessage> STREAM_CODEC =
      StreamCodec.composite(
          BlockPos.STREAM_CODEC, SetDefaultRouteItemSinkMessage::pos,
          ByteBufCodecs.BOOL, SetDefaultRouteItemSinkMessage::defaultRoute,
          SetDefaultRouteItemSinkMessage::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(SetDefaultRouteItemSinkMessage message, IPayloadContext context) {
    var player = context.player();
    var level = player.level();
    if (level.getBlockEntity(message.pos) instanceof BasicPipeBlockEntity pipe) {
      pipe.setDefaultRoute(message.defaultRoute);
    }
  }
}
