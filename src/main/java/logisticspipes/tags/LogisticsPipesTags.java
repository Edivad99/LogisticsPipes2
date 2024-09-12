package logisticspipes.tags;

import logisticspipes.LogisticsPipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class LogisticsPipesTags {

  public static class Items {

    public static final TagKey<Item> WRENCH = commonTag("tools/wrench");

    private static TagKey<Item> tag(String name) {
      return ItemTags.create(LogisticsPipes.rl(name));
    }

    public static TagKey<Item> commonTag(String name) {
      return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
    }
  }
}
