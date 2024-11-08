package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.*;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Charta.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for(DyeColor color : DyeColor.values()) {
            String centerClothPath = "block/"+color+"_card_table_center_cloth";
            String cornerClothPath = "block/"+color+"_card_table_corner_cloth";
            String sideClothPath = "block/"+color+"_card_table_side_cloth";
            String chairClothPath = "block/"+color+"_game_chair_cloth";
            String stoolClothPath = "block/"+color+"_bar_stool_cloth";

            this.models().getBuilder(centerClothPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/card_table_center_cloth")))
                    .texture("particle", this.mcLoc("block/"+color+"_wool"))
                    .texture("cloth", this.mcLoc("block/"+color+"_wool"));
            this.models().getBuilder(cornerClothPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/card_table_corner_cloth")))
                    .texture("particle", this.mcLoc("block/"+color+"_wool"))
                    .texture("cloth", this.mcLoc("block/"+color+"_wool"));
            this.models().getBuilder(sideClothPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/card_table_side_cloth")))
                    .texture("particle", this.mcLoc("block/"+color+"_wool"))
                    .texture("cloth", this.mcLoc("block/"+color+"_wool"));
            this.models().getBuilder(chairClothPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/game_chair_cloth")))
                    .texture("particle", this.mcLoc("block/"+color+"_wool"))
                    .texture("cloth", this.mcLoc("block/"+color+"_wool"));
            this.models().getBuilder(stoolClothPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/bar_stool_cloth")))
                    .texture("particle", this.mcLoc("block/"+color+"_wool"))
                    .texture("cloth", this.mcLoc("block/"+color+"_wool"));
        }
        ModBlocks.CARD_TABLE_MAP.forEach((woodType, cardTable) -> {
            MultiPartBlockStateBuilder cardTableBuilder = this.getMultipartBuilder(cardTable.get());
            String wood = woodType.name();

            String centerPath = "block/"+wood+"_card_table_center";
            String feetPath = "block/"+wood+"_card_table_feet";
            String cornerPath = "block/"+wood+"_card_table_corner";
            String sidePath = "block/"+wood+"_card_table_side";
            String itemPath = "item/"+wood+"_card_table";

            ResourceLocation logResource = getLogResource(wood);

            this.models().getBuilder(centerPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/card_table_center")))
                    .texture("particle", this.mcLoc("block/"+wood+"_planks"))
                    .texture("planks", this.mcLoc("block/"+wood+"_planks"));
            this.models().getBuilder(feetPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/card_table_feet")))
                    .texture("particle", logResource)
                    .texture("log", logResource);
            this.models().getBuilder(cornerPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/card_table_corner")))
                    .texture("particle", this.mcLoc("block/"+wood+"_planks"))
                    .texture("planks", this.mcLoc("block/"+wood+"_planks"))
                    .texture("log", logResource);
            this.models().getBuilder(sidePath)
                    .parent(this.models().getExistingFile(this.modLoc("block/card_table_side")))
                    .texture("particle", this.mcLoc("block/"+wood+"_planks"))
                    .texture("planks", this.mcLoc("block/"+wood+"_planks"));
            this.models().getBuilder(itemPath)
                    .parent(this.models().getExistingFile(this.modLoc("item/card_table")))
                    .texture("particle", this.mcLoc("block/"+wood+"_planks"))
                    .texture("planks", this.mcLoc("block/"+wood+"_planks"))
                    .texture("log", logResource);

            addDirectionPart(cardTableBuilder.part(), this.modLoc(centerPath), 0, null, null, null, null, null);

            addDirectionPart(cardTableBuilder.part(), this.modLoc(feetPath), 0, null, false, false, null, null);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(feetPath), 90, null, null, false, false, null);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(feetPath), 180, null, null, null, false, false);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(feetPath), 270, null, false, null, null, false);

            addDirectionPart(cardTableBuilder.part(), this.modLoc(cornerPath), 0, true, false, true, true, false);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(cornerPath), 90, true, false, false, true, true);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(cornerPath), 180, true, true, false, false, true);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(cornerPath), 270, true, true, true, false, false);

            addDirectionPart(cardTableBuilder.part(), this.modLoc(sidePath), 0, true, true, true, true, false);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(sidePath), 90, true, false, true, true, true);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(sidePath), 180, true, true, false, true, true);
            addDirectionPart(cardTableBuilder.part(), this.modLoc(sidePath), 270, true, true, true, false, true);

            for(DyeColor color : DyeColor.values()) {
                String centerClothPath = "block/"+color+"_card_table_center_cloth";
                String cornerClothPath = "block/"+color+"_card_table_corner_cloth";
                String sideClothPath = "block/"+color+"_card_table_side_cloth";

                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(centerClothPath), 0, true, true, true, true, true, color);

                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(cornerClothPath), 0, true, false, true, true, false, color);
                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(cornerClothPath), 90, true, false, false, true, true, color);
                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(cornerClothPath), 180, true, true, false, false, true, color);
                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(cornerClothPath), 270, true, true, true, false, false, color);

                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(sideClothPath), 0, true, true, true, true, false, color);
                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(sideClothPath), 90, true, false, true, true, true, color);
                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(sideClothPath), 180, true, true, false, true, true, color);
                addClothCardTablePart(cardTableBuilder.part(), this.modLoc(sideClothPath), 270, true, true, true, false, true, color);
            }
        });
        ModBlocks.GAME_CHAIR_MAP.forEach((woodType, cardTable) -> {
            MultiPartBlockStateBuilder gameChairBuilder = this.getMultipartBuilder(cardTable.get());

            String wood = woodType.name();

            String chairPath = "block/"+wood+"_game_chair";
            String itemPath = "item/"+wood+"_game_chair";

            ResourceLocation logResource = getLogResource(wood);

            this.models().getBuilder(chairPath)
                    .parent(this.models().getExistingFile(this.modLoc("block/game_chair")))
                    .texture("particle", this.mcLoc("block/"+wood+"_planks"))
                    .texture("planks", this.mcLoc("block/"+wood+"_planks"))
                    .texture("log", logResource);
            this.models().getBuilder(itemPath).parent(this.models().getExistingFile(this.modLoc(chairPath)));

            int rot = 0;
            for(Direction d : Direction.Plane.HORIZONTAL) {
                gameChairBuilder.part()
                    .modelFile(this.models().getExistingFile(this.modLoc(chairPath)))
                    .rotationY(rot)
                    .addModel()
                    .condition(GameChairBlock.FACING, d);
                for(DyeColor color : DyeColor.values()) {
                    String chairClothPath = "block/"+color+"_game_chair_cloth";
                    gameChairBuilder.part()
                        .modelFile(this.models().getExistingFile(this.modLoc(chairClothPath)))
                        .rotationY(rot)
                        .addModel()
                        .condition(GameChairBlock.FACING, d)
                        .condition(GameChairBlock.CLOTH, true)
                        .condition(GameChairBlock.COLOR, color);
                }
                rot += 90;
            }

        });
        ModBlocks.BAR_STOOL_MAP.forEach((woodType, barStool) -> {
            MultiPartBlockStateBuilder barStoolBuilder = this.getMultipartBuilder(barStool.get());

            String wood = woodType.name();

            String stoolPath = "block/"+wood+"_bar_stool";
            String itemPath = "item/"+wood+"_bar_stool";

            ResourceLocation logResource = getLogResource(wood);

            this.models().getBuilder(stoolPath)
                .parent(this.models().getExistingFile(this.modLoc("block/bar_stool")))
                .texture("particle", this.mcLoc("block/"+wood+"_planks"))
                .texture("planks", this.mcLoc("block/"+wood+"_planks"))
                .texture("log", logResource);
            this.models().getBuilder(itemPath).parent(this.models().getExistingFile(this.modLoc(stoolPath)));

            barStoolBuilder.part()
                    .modelFile(this.models().getExistingFile(this.modLoc(stoolPath)))
                    .addModel();
            for(DyeColor color : DyeColor.values()) {
                String stoolClothPath = "block/" + color + "_bar_stool_cloth";
                barStoolBuilder.part()
                        .modelFile(this.models().getExistingFile(this.modLoc(stoolClothPath)))
                        .addModel()
                        .condition(GameChairBlock.CLOTH, true)
                        .condition(GameChairBlock.COLOR, color);
            }
        });
        ModBlocks.BLOCKS.getEntries().stream().filter(b -> b.get() instanceof BeerGlassBlock).forEach(block -> {
            VariantBlockStateBuilder beerGlassBuilder = this.getVariantBuilder(block.get());
            int i = 0;
            for(Direction d : List.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH)) {
                beerGlassBuilder.partialState()
                    .with(BeerGlassBlock.FACING, d)
                    .modelForState()
                    .modelFile(this.models().getExistingFile(this.modLoc("block/"+block.getId().getPath())))
                    .rotationY((i++) * 90)
                    .addModel();
            }
        });
        ModBlocks.BLOCKS.getEntries().stream().filter(b -> b.get() instanceof WineGlassBlock).forEach(block -> {
            VariantBlockStateBuilder wineGlassBuilder = this.getVariantBuilder(block.get());
            wineGlassBuilder.partialState()
                .modelForState()
                .modelFile(this.models().getExistingFile(this.modLoc("block/"+block.getId().getPath())))
                .addModel();
        });
    }

    private void addClothCardTablePart(ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> builder, ResourceLocation modelPath, int rotationY, Boolean valid, Boolean north, Boolean east, Boolean south, Boolean west, DyeColor color) {
        MultiPartBlockStateBuilder.PartBuilder model = addDirectionPart(builder, modelPath, rotationY, valid, north, east, south, west);
        model.condition(CardTableBlock.CLOTH, true);
        model.condition(CardTableBlock.COLOR, color);
    }

    private MultiPartBlockStateBuilder.PartBuilder addDirectionPart(ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> builder, ResourceLocation modelPath, int rotationY, Boolean valid, Boolean north, Boolean east, Boolean south, Boolean west) {
        builder.modelFile(this.models().getExistingFile(modelPath));
        builder.rotationY(rotationY);
        MultiPartBlockStateBuilder.PartBuilder model = builder.addModel();
        if(valid != null)
            model.condition(CardTableBlock.VALID, valid);
        if(north != null)
            model.condition(CardTableBlock.NORTH, north);
        if(east != null)
            model.condition(CardTableBlock.EAST, east);
        if(south != null)
            model.condition(CardTableBlock.SOUTH, south);
        if(west != null)
            model.condition(CardTableBlock.WEST, west);
        return model;
    }

    private ResourceLocation getLogResource(String wood) {
        ResourceLocation logResource = this.mcLoc("block/"+wood+"_log");
        if(!this.models().existingFileHelper.exists(this.mcLoc("textures/block/"+wood+"_log.png"), PackType.CLIENT_RESOURCES)) {
            logResource = this.mcLoc("block/"+wood+"_stem");
            if(!this.models().existingFileHelper.exists(this.mcLoc("textures/block/"+wood+"_stem.png"), PackType.CLIENT_RESOURCES)) {
                logResource = this.mcLoc("block/"+wood+"_stalk");
            }
        }
        return logResource;
    }

}
