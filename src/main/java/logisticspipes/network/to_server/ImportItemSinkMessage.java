package logisticspipes.network.to_server;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.level.block.entity.BasicPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ImportItemSinkMessage(BlockPos pos) implements CustomPacketPayload {

  public static final Type<ImportItemSinkMessage> TYPE =
      new Type<>(LogisticsPipes.rl("import_item_sink"));

  public static final StreamCodec<FriendlyByteBuf, ImportItemSinkMessage> STREAM_CODEC =
      StreamCodec.composite(
          BlockPos.STREAM_CODEC, ImportItemSinkMessage::pos,
          ImportItemSinkMessage::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(ImportItemSinkMessage message, IPayloadContext context) {
    var player = context.player();
    var level = player.level();
    if (level.getBlockEntity(message.pos) instanceof BasicPipeBlockEntity pipe) {
      pipe.getModuleItemSink().importFromAdjacentInventory();
    }
  }
}
