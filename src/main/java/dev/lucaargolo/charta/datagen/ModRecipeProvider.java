package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

    private final CompletableFuture<HolderLookup.Provider> registries;

    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        this.registries = registries;
    }

    @Override
    public void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        registries.thenAccept(holderLookup -> {
            HolderLookup.RegistryLookup<Item> itemLookup = holderLookup.lookupOrThrow(Registries.ITEM);

            ModBlocks.CARD_TABLE_MAP.forEach((woodType, holder) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder, 1)
                        .pattern("PSP")
                        .pattern("L L")
                        .define('P', getPlanks(woodType, itemLookup))
                        .define('S', getSlab(woodType, itemLookup))
                        .define('L', getLog(woodType, itemLookup))
                        .unlockedBy("has_wood", has(getLog(woodType, itemLookup)))
                        .save(recipeOutput);
            });

            ModBlocks.GAME_CHAIR_MAP.forEach((woodType, holder) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder, 1)
                        .pattern("P ")
                        .pattern("PS")
                        .pattern("LL")
                        .define('P', getPlanks(woodType, itemLookup))
                        .define('S', getSlab(woodType, itemLookup))
                        .define('L', getLog(woodType, itemLookup))
                        .unlockedBy("has_wood", has(getLog(woodType, itemLookup)))
                        .save(recipeOutput);
            });

            ModBlocks.BAR_STOOL_MAP.forEach((woodType, holder) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder, 1)
                        .pattern("SLS")
                        .pattern(" P ")
                        .define('P', getPlanks(woodType, itemLookup))
                        .define('S', getSlab(woodType, itemLookup))
                        .define('L', getLog(woodType, itemLookup))
                        .unlockedBy("has_wood", has(getLog(woodType, itemLookup)))
                        .save(recipeOutput);
            });

            ModBlocks.BAR_SHELF_MAP.forEach((woodType, holder) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder, 1)
                        .pattern("SLS")
                        .pattern("SLS")
                        .define('S', getSlab(woodType, itemLookup))
                        .define('L', getLog(woodType, itemLookup))
                        .unlockedBy("has_wood", has(getLog(woodType, itemLookup)))
                        .save(recipeOutput);
            });

            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_LEAD, 1)
                    .pattern(" I ")
                    .pattern("ILI")
                    .pattern(" I ")
                    .define('I', ConventionalItemTags.IRON_NUGGETS)
                    .define('L', Items.LEAD)
                    .unlockedBy("has_lead", has(Items.LEAD))
                    .save(recipeOutput);
        });
    }

    private ItemLike getPlanks(WoodType woodType, HolderLookup.RegistryLookup<Item> itemLookup) {
        String wood = woodType.name();
        ResourceKey<Item> planksResource = ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(wood+"_planks"));
        return itemLookup.get(planksResource).orElseThrow().value();
    }

    private ItemLike getSlab(WoodType woodType, HolderLookup.RegistryLookup<Item> itemLookup) {
        String wood = woodType.name();
        ResourceKey<Item> planksResource = ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(wood+"_slab"));
        return itemLookup.get(planksResource).orElseThrow().value();
    }

    private ItemLike getLog(WoodType woodType, HolderLookup.RegistryLookup<Item> itemLookup) {
        String wood = woodType.name();
        ResourceKey<Item> logResource = ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(wood+"_log"));
        if(itemLookup.get(logResource).isEmpty()) {
            logResource = ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(wood+"_stem"));
            if(itemLookup.get(logResource).isEmpty()) {
                logResource = ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(wood));
            }
        }
        return itemLookup.get(logResource).orElseThrow().value();
    }
}
