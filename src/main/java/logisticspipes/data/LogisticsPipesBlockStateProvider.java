package logisticspipes.data;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.level.block.GenericPipeBlock;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class LogisticsPipesBlockStateProvider extends BlockStateProvider {

  public LogisticsPipesBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
    super(output, LogisticsPipes.ID, exFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    buildPipe(LogisticsPipesBlocks.PIPE_BASIC.get(), "basic");
    buildPipe(LogisticsPipesBlocks.PIPE_CHASSI_MK2.get(), "chassi_mk2");
  }

  private void buildPipe(Block block, String name) {
    var center = models()
        .withExistingParent(name + "_center",
            modLoc(ModelProvider.BLOCK_FOLDER + "/pipe_center"))
        .texture("1", modLoc("block/pipes/%s/powered_pipe".formatted(name)));
    var end = models()
        .withExistingParent(name + "_end",
            modLoc(ModelProvider.BLOCK_FOLDER + "/pipe_end"))
        .texture("1", modLoc("block/pipes/%s/powered_pipe".formatted(name)));


    getMultipartBuilder(block)
        .part()
        .modelFile(center).addModel().end()
        .part()
        .modelFile(end).addModel()
        .condition(GenericPipeBlock.CONNECTION.get(Direction.NORTH), true).end()
        .part()
        .modelFile(end).rotationY(180).addModel()
        .condition(GenericPipeBlock.CONNECTION.get(Direction.SOUTH), true).end()
        .part()
        .modelFile(end).rotationY(90).addModel()
        .condition(GenericPipeBlock.CONNECTION.get(Direction.EAST), true).end()
        .part()
        .modelFile(end).rotationY(270).addModel()
        .condition(GenericPipeBlock.CONNECTION.get(Direction.WEST), true).end()
        .part()
        .modelFile(end).rotationX(90).addModel()
        .condition(GenericPipeBlock.CONNECTION.get(Direction.DOWN), true).end()
        .part()
        .modelFile(end).rotationX(270).addModel()
        .condition(GenericPipeBlock.CONNECTION.get(Direction.UP), true).end();

    this.simpleBlockItem(block, center);
  }
}
