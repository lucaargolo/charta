package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.CardTableBlock;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
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

        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_feet"), 0, null, false, false, null, null);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_feet"), 90, null, null, false, false, null);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_feet"), 180, null, null, null, false, false);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_feet"), 270, null, false, null, null, false);

        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_corner"), 0, true, false, true, true, false);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_corner"), 90, true, false, false, true, true);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_corner"), 180, true, true, false, false, true);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_corner"), 270, true, true, true, false, false);

        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_side"), 0, true, true, true, true, false);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_side"), 90, true, false, true, true, true);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_side"), 180, true, true, false, true, true);
        addDirectionPart(cardTableBuilder.part(), this.modLoc("block/card_table_side"), 270, true, true, true, false, true);
    }

    private void addDirectionPart(ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> builder, ResourceLocation modelPath, int rotationY, Boolean valid, Boolean north, Boolean east, Boolean south, Boolean west) {
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
    }

}
