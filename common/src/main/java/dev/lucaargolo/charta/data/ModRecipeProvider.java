package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    private static final TagKey<Item> NUGGETS_IRON = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/iron"));

    private final CompletableFuture<HolderLookup.Provider> registries;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        this.registries = registries;
    }

    @Override
    public void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        this.registries.thenAcceptAsync(provider -> {
            this.innerBuildRecipes(recipeOutput, provider);
        });
    }

    private void innerBuildRecipes(@NotNull RecipeOutput recipeOutput, HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<Item> itemLookup = provider.lookupOrThrow(Registries.ITEM);

        ModBlocks.CARD_TABLE_MAP.forEach((woodType, holder) -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get(), 1)
                .pattern("PSP")
                .pattern("L L")
                .define('P', getPlanks(woodType, itemLookup))
                .define('S', getSlab(woodType, itemLookup))
                .define('L', getLog(woodType, itemLookup))
                .unlockedBy("has_wood", has(getLog(woodType, itemLookup)))
                .save(recipeOutput);
        });

        ModBlocks.GAME_CHAIR_MAP.forEach((woodType, holder) -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get(), 1)
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
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get(), 1)
                    .pattern("SLS")
                    .pattern(" P ")
                    .define('P', getPlanks(woodType, itemLookup))
                    .define('S', getSlab(woodType, itemLookup))
                    .define('L', getLog(woodType, itemLookup))
                    .unlockedBy("has_wood", has(getLog(woodType, itemLookup)))
                    .save(recipeOutput);
        });

        ModBlocks.BAR_SHELF_MAP.forEach((woodType, holder) -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get(), 1)
                    .pattern("SLS")
                    .pattern("SLS")
                    .define('S', getSlab(woodType, itemLookup))
                    .define('L', getLog(woodType, itemLookup))
                    .unlockedBy("has_wood", has(getLog(woodType, itemLookup)))
                    .save(recipeOutput);
        });

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_LEAD.get(), 1)
                .pattern(" I ")
                .pattern("ILI")
                .pattern(" I ")
                .define('I', NUGGETS_IRON)
                .define('L', Items.LEAD)
                .unlockedBy("has_lead", has(Items.LEAD))
                .save(recipeOutput);
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
