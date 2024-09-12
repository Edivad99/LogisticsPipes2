package logisticspipes.data;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.level.block.GenericPipeBlock;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class LogisticsPipesBlockStateProvider extends BlockStateProvider {

  private final ExistingFileHelper exFileHelper;

  public LogisticsPipesBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
    super(output, LogisticsPipes.ID, exFileHelper);
    this.exFileHelper = exFileHelper;
  }

  @Override
  protected void registerStatesAndModels() {
    var center = new ModelFile.ExistingModelFile(
        modLoc(ModelProvider.BLOCK_FOLDER + "/center"), this.exFileHelper);
    var end = new ModelFile.ExistingModelFile(
        modLoc(ModelProvider.BLOCK_FOLDER + "/end"), this.exFileHelper);


    getMultipartBuilder(LogisticsPipesBlocks.PIPE_BASIC.get())
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

    this.simpleBlockItem(LogisticsPipesBlocks.PIPE_BASIC.get(), center);
  }
}
