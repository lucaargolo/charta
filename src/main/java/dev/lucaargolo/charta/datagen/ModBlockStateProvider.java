package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.CardTableBlock;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Charta.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        MultiPartBlockStateBuilder cardTableBuilder = this.getMultipartBuilder(ModBlocks.CARD_TABLE.get());

        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_center")))
            .addModel();

        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_feet")))
            .rotationY(0)
            .addModel()
            .condition(CardTableBlock.NORTH, false)
            .condition(CardTableBlock.EAST, false);
        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_feet")))
            .rotationY(90)
            .addModel()
            .condition(CardTableBlock.SOUTH, false)
            .condition(CardTableBlock.EAST, false);
        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_feet")))
            .rotationY(180)
            .addModel()
            .condition(CardTableBlock.SOUTH, false)
            .condition(CardTableBlock.WEST, false);
        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_feet")))
            .rotationY(270)
            .addModel()
            .condition(CardTableBlock.NORTH, false)
            .condition(CardTableBlock.WEST, false);

        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_corner")))
            .rotationY(0)
            .addModel()
            .condition(CardTableBlock.NORTH, false)
            .condition(CardTableBlock.WEST, false)
            .condition(CardTableBlock.SOUTH, true)
            .condition(CardTableBlock.EAST, true);
        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_corner")))
            .rotationY(90)
            .addModel()
            .condition(CardTableBlock.SOUTH, true)
            .condition(CardTableBlock.WEST, true)
            .condition(CardTableBlock.EAST, false)
            .condition(CardTableBlock.NORTH, false);
        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_corner")))
            .rotationY(180)
            .addModel()
            .condition(CardTableBlock.SOUTH, false)
            .condition(CardTableBlock.EAST, false)
            .condition(CardTableBlock.NORTH, true)
            .condition(CardTableBlock.WEST, true);
        cardTableBuilder.part()
            .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_corner")))
            .rotationY(270)
            .addModel()
            .condition(CardTableBlock.NORTH, true)
            .condition(CardTableBlock.EAST, true)
            .condition(CardTableBlock.SOUTH, false)
            .condition(CardTableBlock.WEST, false);

        cardTableBuilder.part()
                .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_side")))
                .rotationY(0)
                .addModel()
                .condition(CardTableBlock.NORTH, true)
                .condition(CardTableBlock.SOUTH, true)
                .condition(CardTableBlock.EAST, true)
                .condition(CardTableBlock.WEST, false);
        cardTableBuilder.part()
                .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_side")))
                .rotationY(90)
                .addModel()
                .condition(CardTableBlock.SOUTH, true)
                .condition(CardTableBlock.WEST, true)
                .condition(CardTableBlock.EAST, true)
                .condition(CardTableBlock.NORTH, false);
        cardTableBuilder.part()
                .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_side")))
                .rotationY(180)
                .addModel()
                .condition(CardTableBlock.SOUTH, true)
                .condition(CardTableBlock.EAST, false)
                .condition(CardTableBlock.NORTH, true)
                .condition(CardTableBlock.WEST, true);
        cardTableBuilder.part()
                .modelFile(this.models().getExistingFile(this.modLoc("block/card_table_side")))
                .rotationY(270)
                .addModel()
                .condition(CardTableBlock.NORTH, true)
                .condition(CardTableBlock.EAST, true)
                .condition(CardTableBlock.SOUTH, false)
                .condition(CardTableBlock.WEST, true);

    }

}
