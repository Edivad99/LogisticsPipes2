package logisticspipes.datagen;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import logisticspipes.world.level.block.StoneBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CustomBlockStateProvider extends BlockStateProvider {

  private final ExistingFileHelper exFileHelper;

  public CustomBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
    super(output, LogisticsPipes.ID, exFileHelper);
    this.exFileHelper = exFileHelper;
  }

  @Override
  protected void registerStatesAndModels() {
    var center = new ModelFile.ExistingModelFile(
        modLoc(ModelProvider.BLOCK_FOLDER + "/center"), this.exFileHelper);
    var end = new ModelFile.ExistingModelFile(
        modLoc(ModelProvider.BLOCK_FOLDER + "/end"), this.exFileHelper);


    getMultipartBuilder(LogisticsPipesBlocks.STONE.get())
        .part()
        .modelFile(center).addModel().end()
        .part()
        .modelFile(end).addModel()
        .condition(StoneBlock.CONNECTION.get(Direction.NORTH), true).end()
        .part()
        .modelFile(end).rotationY(180).addModel()
        .condition(StoneBlock.CONNECTION.get(Direction.SOUTH), true).end()
        .part()
        .modelFile(end).rotationY(90).addModel()
        .condition(StoneBlock.CONNECTION.get(Direction.EAST), true).end()
        .part()
        .modelFile(end).rotationY(270).addModel()
        .condition(StoneBlock.CONNECTION.get(Direction.WEST), true).end()
        .part()
        .modelFile(end).rotationX(90).addModel()
        .condition(StoneBlock.CONNECTION.get(Direction.DOWN), true).end()
        .part()
        .modelFile(end).rotationX(270).addModel()
        .condition(StoneBlock.CONNECTION.get(Direction.UP), true).end();
  }
}
