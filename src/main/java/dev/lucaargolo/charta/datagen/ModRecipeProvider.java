package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
        ModBlocks.CARD_TABLE_MAP.forEach((woodType, holder) -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get(), 1)
                .pattern("PSP")
                .pattern("L L")
                .define('P', getPlanks(woodType))
                .define('S', getSlab(woodType))
                .define('L', getLog(woodType))
                .unlockedBy("has_wood", has(getLog(woodType)))
                .save(pWriter);
        });

        ModBlocks.GAME_CHAIR_MAP.forEach((woodType, holder) -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get(), 1)
                .pattern("P ")
                .pattern("PS")
                .pattern("LL")
                .define('P', getPlanks(woodType))
                .define('S', getSlab(woodType))
                .define('L', getLog(woodType))
                .unlockedBy("has_wood", has(getLog(woodType)))
                .save(pWriter);
        });

        ModBlocks.BAR_STOOL_MAP.forEach((woodType, holder) -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get(), 1)
                    .pattern("SLS")
                    .pattern(" P ")
                    .define('P', getPlanks(woodType))
                    .define('S', getSlab(woodType))
                    .define('L', getLog(woodType))
                    .unlockedBy("has_wood", has(getLog(woodType)))
                    .save(pWriter);
        });

        ModBlocks.BAR_SHELF_MAP.forEach((woodType, holder) -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get(), 1)
                    .pattern("SLS")
                    .pattern("SLS")
                    .define('S', getSlab(woodType))
                    .define('L', getLog(woodType))
                    .unlockedBy("has_wood", has(getLog(woodType)))
                    .save(pWriter);
        });

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_LEAD.get(), 1)
                .pattern(" I ")
                .pattern("ILI")
                .pattern(" I ")
                .define('I', Tags.Items.NUGGETS_IRON)
                .define('L', Items.LEAD)
                .unlockedBy("has_lead", has(Items.LEAD))
                .save(pWriter);
    }

    private ItemLike getPlanks(WoodType woodType) {
        String wood = woodType.name();
        ResourceKey<Item> planksResource = ResourceKey.create(Registries.ITEM, new ResourceLocation(wood+"_planks"));
        return ForgeRegistries.ITEMS.getHolder(planksResource).orElseThrow().value();
    }

    private ItemLike getSlab(WoodType woodType) {
        String wood = woodType.name();
        ResourceKey<Item> planksResource = ResourceKey.create(Registries.ITEM, new ResourceLocation(wood+"_slab"));
        return ForgeRegistries.ITEMS.getHolder(planksResource).orElseThrow().value();
    }

    private ItemLike getLog(WoodType woodType) {
        String wood = woodType.name();
        ResourceKey<Item> logResource = ResourceKey.create(Registries.ITEM, new ResourceLocation(wood+"_log"));
        if(ForgeRegistries.ITEMS.getHolder(logResource).isEmpty()) {
            logResource = ResourceKey.create(Registries.ITEM, new ResourceLocation(wood+"_stem"));
            if(ForgeRegistries.ITEMS.getHolder(logResource).isEmpty()) {
                logResource = ResourceKey.create(Registries.ITEM, new ResourceLocation(wood));
            }
        }
        return ForgeRegistries.ITEMS.getHolder(logResource).orElseThrow().value();
    }
}
