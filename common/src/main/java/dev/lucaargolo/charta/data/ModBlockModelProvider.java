package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.block.*;
import dev.lucaargolo.charta.registry.ModBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModBlockModelProvider {

    private static final TextureSlot PARTICLE = TextureSlot.PARTICLE;
    private static final TextureSlot PLANKS = TextureSlot.create("planks");
    private static final TextureSlot LOG = TextureSlot.create("log");
    private static final TextureSlot CLOTH = TextureSlot.create("cloth");

    public static void generate(BlockModelGenerators generators) {
        for(DyeColor color : DyeColor.values()) {
            TextureMapping mapping = new TextureMapping();
            mapping.put(CLOTH, ResourceLocation.withDefaultNamespace("block/"+color +"_wool"));
            createModel(generators, mapping,
                ChartaMod.id("block/" + color + "_card_table_center_cloth"),
                ChartaMod.id("block/card_table_center_cloth")
            );
            createModel(generators, mapping,
                ChartaMod.id("block/" + color + "_card_table_corner_cloth"),
                ChartaMod.id("block/card_table_corner_cloth")
            );
            createModel(generators, mapping,
                ChartaMod.id("block/" + color + "_card_table_side_cloth"),
                ChartaMod.id("block/card_table_side_cloth")
            );
            createModel(generators, mapping,
                ChartaMod.id("block/" + color + "_game_chair_cloth"),
                ChartaMod.id("block/game_chair_cloth")
            );
            createModel(generators, mapping,
                ChartaMod.id("block/" + color + "_bar_stool_cloth"),
                ChartaMod.id("block/bar_stool_cloth")
            );
        }

        ModBlocks.CARD_TABLE_MAP.forEach((wood, entry) -> {
            TextureMapping mapping = new TextureMapping();
            mapping.put(PARTICLE, getPlanks(wood));
            mapping.put(PLANKS, getPlanks(wood));
            mapping.put(LOG, getLog(wood));

            createModel(generators, mapping,
                ChartaMod.id("block/"+entry.path()+"_center"),
                ChartaMod.id("block/"+entry.path().replace(wood.name()+"_", "")+"_center")
            );
            createModel(generators, mapping,
                    ChartaMod.id("block/"+entry.path()+"_feet"),
                    ChartaMod.id("block/"+entry.path().replace(wood.name()+"_", "")+"_feet")
            );
            createModel(generators, mapping,
                    ChartaMod.id("block/"+entry.path()+"_corner"),
                    ChartaMod.id("block/"+entry.path().replace(wood.name()+"_", "")+"_corner")
            );
            createModel(generators, mapping,
                    ChartaMod.id("block/"+entry.path()+"_side"),
                    ChartaMod.id("block/"+entry.path().replace(wood.name()+"_", "")+"_side")
            );
            createModel(generators, mapping,
                    ChartaMod.id("item/"+entry.path()),
                    ChartaMod.id("item/"+entry.path().replace(wood.name()+"_", ""))
            );

            createTableBlockState(entry, generators);
        });

        ModBlocks.GAME_CHAIR_MAP.forEach((wood, entry) -> {
            TextureMapping mapping = new TextureMapping();
            mapping.put(PARTICLE, getPlanks(wood));
            mapping.put(PLANKS, getPlanks(wood));
            mapping.put(LOG, getLog(wood));
            createChairBlockStateAndModel(entry, generators, mapping,
                    ChartaMod.id("block/"+entry.path()),
                    ChartaMod.id("block/"+entry.path().replace(wood.name()+"_", ""))
            );
        });


        ModBlocks.BAR_STOOL_MAP.forEach((wood, entry) -> {
            TextureMapping mapping = new TextureMapping();
            mapping.put(PARTICLE, getPlanks(wood));
            mapping.put(PLANKS, getPlanks(wood));
            mapping.put(LOG, getLog(wood));
            createStoolBlockStateAndModel(entry, generators, mapping,
                    ChartaMod.id("block/"+entry.path()),
                    ChartaMod.id("block/"+entry.path().replace(wood.name()+"_", ""))
            );
        });

        ModBlocks.BAR_SHELF_MAP.forEach((wood, entry) -> {
            TextureMapping mapping = new TextureMapping();
            mapping.put(PARTICLE, getPlanks(wood));
            mapping.put(PLANKS, getPlanks(wood));
            mapping.put(LOG, getLog(wood));
            createDirectionalBlockStateAndModel(entry, generators, mapping,
                    ChartaMod.id("block/"+entry.path()),
                    ChartaMod.id("block/"+entry.path().replace(wood.name()+"_", ""))
            );
        });

        ModBlocks.REGISTRY.getEntries().forEach(entry -> {
            ResourceLocation model = ChartaMod.id("block/"+entry.path());
            switch (entry.get()) {
                case BeerGlassBlock ignored -> createDirectionalBlockStateAndModel(entry, generators, null, model, model);
                case WineGlassBlock ignored -> createBaseBlockStateAndModel(entry, generators, null, model, model);
                default -> {
                    if(entry == ModBlocks.DEALER_TABLE) {
                        createBaseBlockStateAndModel(entry, generators, null, model, model);
                    }
                }
            }
        });

    }

    private static ResourceLocation createModel(BlockModelGenerators generators, @Nullable TextureMapping mapping, ResourceLocation model, ResourceLocation parent) {
        if(mapping == null) {
            return parent;
        }else{
            ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(), mapping.slots.keySet().toArray(new TextureSlot[0]));
            template.create(model, mapping, generators.modelOutput);
            return model;
        }
    }

    private static void createBaseBlockStateAndModel(ModBlockRegistry.BlockEntry<?> entry, BlockModelGenerators generators, @Nullable TextureMapping mapping, ResourceLocation model, ResourceLocation parent) {
        ResourceLocation path = createModel(generators, mapping, model, parent);
        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(entry.get(), Variant.variant().with(VariantProperties.MODEL, path));
        generators.blockStateOutput.accept(generator);
    }

    private static void createDirectionalBlockStateAndModel(ModBlockRegistry.BlockEntry<?> entry, BlockModelGenerators generators, @Nullable TextureMapping mapping, ResourceLocation model, ResourceLocation parent) {
        ResourceLocation path = createModel(generators, mapping, model, parent);
        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(entry.get());
        PropertyDispatch.C1<Direction> dispatch = PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING);
        dispatch.select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, path));
        dispatch.select(Direction.EAST, Variant.variant().with(VariantProperties.MODEL, path).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
        dispatch.select(Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, path).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));
        dispatch.select(Direction.WEST, Variant.variant().with(VariantProperties.MODEL, path).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
        generator.with(dispatch);
        generators.blockStateOutput.accept(generator);
    }

    private static void createStoolBlockStateAndModel(ModBlockRegistry.BlockEntry<?> entry, BlockModelGenerators generators, @Nullable TextureMapping mapping, ResourceLocation model, ResourceLocation parent) {
        ResourceLocation path = createModel(generators, mapping, model, parent);
        MultiPartGenerator generator = MultiPartGenerator.multiPart(entry.get());
        generator.with(Variant.variant().with(VariantProperties.MODEL, path));

        for(DyeColor color : DyeColor.values()) {
            Condition.TerminalCondition condition = Condition.condition();
            condition.term(BarStoolBlock.CLOTH, true);
            condition.term(BarStoolBlock.COLOR, color);
            generator.with(condition, Variant.variant().with(VariantProperties.MODEL, ChartaMod.id("block/" + color + "_bar_stool_cloth")));
        }

        generators.blockStateOutput.accept(generator);
    }

    private static void createChairBlockStateAndModel(ModBlockRegistry.BlockEntry<?> entry, BlockModelGenerators generators, @Nullable TextureMapping mapping, ResourceLocation model, ResourceLocation parent) {
        ResourceLocation path = createModel(generators, mapping, model, parent);
        MultiPartGenerator generator = MultiPartGenerator.multiPart(entry.get());

        int rot = 0;
        for(Direction d : Direction.Plane.HORIZONTAL) {
            Condition.TerminalCondition baseCondition = Condition.condition();
            baseCondition.term(GameChairBlock.FACING, d);

            Variant baseVariant = Variant.variant();
            baseVariant.with(VariantProperties.Y_ROT, getRot(rot));
            baseVariant.with(VariantProperties.MODEL, path);

            generator.with(baseCondition, baseVariant);
            for(DyeColor color : DyeColor.values()) {
                Condition.TerminalCondition clothCondition = Condition.condition();
                clothCondition.term(GameChairBlock.FACING, d);
                clothCondition.term(GameChairBlock.CLOTH, true);
                clothCondition.term(GameChairBlock.COLOR, color);

                Variant clothVariant = Variant.variant();
                clothVariant.with(VariantProperties.Y_ROT, getRot(rot));
                clothVariant.with(VariantProperties.MODEL, ChartaMod.id("block/" + color + "_game_chair_cloth"));

                generator.with(clothCondition, clothVariant);
            }
            rot += 90;
        }

        generators.blockStateOutput.accept(generator);
    }

    private static void createTableBlockState(ModBlockRegistry.BlockEntry<?> entry, BlockModelGenerators generators) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(entry.get());

        ResourceLocation center = ChartaMod.id("block/"+entry.path()+"_center");
        ResourceLocation feet = ChartaMod.id("block/"+entry.path()+"_feet");
        ResourceLocation corner = ChartaMod.id("block/"+entry.path()+"_corner");
        ResourceLocation side = ChartaMod.id("block/"+entry.path()+"_side");

        addDirectionTablePart(generator, center, VariantProperties.Rotation.R0, null, null, null, null, null);

        addDirectionTablePart(generator, feet, VariantProperties.Rotation.R0, null, false, false, null, null);
        addDirectionTablePart(generator, feet, VariantProperties.Rotation.R90, null, null, false, false, null);
        addDirectionTablePart(generator, feet, VariantProperties.Rotation.R180, null, null, null, false, false);
        addDirectionTablePart(generator, feet, VariantProperties.Rotation.R270, null, false, null, null, false);

        addDirectionTablePart(generator, corner, VariantProperties.Rotation.R0, true, false, true, true, false);
        addDirectionTablePart(generator, corner, VariantProperties.Rotation.R90, true, false, false, true, true);
        addDirectionTablePart(generator, corner, VariantProperties.Rotation.R180, true, true, false, false, true);
        addDirectionTablePart(generator, corner, VariantProperties.Rotation.R270, true, true, true, false, false);

        addDirectionTablePart(generator, side, VariantProperties.Rotation.R0, true, true, true, true, false);
        addDirectionTablePart(generator, side, VariantProperties.Rotation.R90, true, false, true, true, true);
        addDirectionTablePart(generator, side, VariantProperties.Rotation.R180, true, true, false, true, true);
        addDirectionTablePart(generator, side, VariantProperties.Rotation.R270, true, true, true, false, true);

        for(DyeColor color : DyeColor.values()) {
            ResourceLocation centerCloth = ChartaMod.id("block/"+color+"_card_table_center_cloth");
            ResourceLocation cornerCloth = ChartaMod.id("block/"+color+"_card_table_corner_cloth");
            ResourceLocation sideCloth = ChartaMod.id("block/"+color+"_card_table_side_cloth");

            addDirectionTableClothPart(generator, centerCloth, VariantProperties.Rotation.R0, true, true, true, true, true, color);

            addDirectionTableClothPart(generator, cornerCloth, VariantProperties.Rotation.R0, true, false, true, true, false, color);
            addDirectionTableClothPart(generator, cornerCloth, VariantProperties.Rotation.R90, true, false, false, true, true, color);
            addDirectionTableClothPart(generator, cornerCloth, VariantProperties.Rotation.R180, true, true, false, false, true, color);
            addDirectionTableClothPart(generator, cornerCloth, VariantProperties.Rotation.R270, true, true, true, false, false, color);

            addDirectionTableClothPart(generator, sideCloth, VariantProperties.Rotation.R0, true, true, true, true, false, color);
            addDirectionTableClothPart(generator, sideCloth, VariantProperties.Rotation.R90, true, false, true, true, true, color);
            addDirectionTableClothPart(generator, sideCloth, VariantProperties.Rotation.R180, true, true, false, true, true, color);
            addDirectionTableClothPart(generator, sideCloth, VariantProperties.Rotation.R270, true, true, true, false, true, color);
        }

        generators.blockStateOutput.accept(generator);
    }

    private static void addDirectionTablePart(MultiPartGenerator generator, ResourceLocation modelPath, VariantProperties.Rotation rotationY, Boolean valid, Boolean north, Boolean east, Boolean south, Boolean west) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, modelPath);
        if (rotationY != VariantProperties.Rotation.R0) {
            variant.with(VariantProperties.Y_ROT, rotationY);
        }

        Condition.TerminalCondition condition = Condition.condition();
        if (valid != null) condition.term(CardTableBlock.VALID, valid);
        if (north != null) condition.term(CardTableBlock.NORTH, north);
        if (east != null) condition.term(CardTableBlock.EAST, east);
        if (south != null) condition.term(CardTableBlock.SOUTH, south);
        if (west != null) condition.term(CardTableBlock.WEST, west);

        if (condition.get().getAsJsonObject().isEmpty()) {
            generator.with(variant);
        } else {
            generator.with(condition, variant);
        }
    }

    private static void addDirectionTableClothPart(MultiPartGenerator generator, ResourceLocation modelPath, VariantProperties.Rotation rotationY, Boolean valid, Boolean north, Boolean east, Boolean south, Boolean west, DyeColor color) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, modelPath);
        if(rotationY != VariantProperties.Rotation.R0) {
            variant.with(VariantProperties.Y_ROT, rotationY);
        }

        Condition.TerminalCondition condition = Condition.condition();
        if (valid != null) condition.term(CardTableBlock.VALID, valid);
        if (north != null) condition.term(CardTableBlock.NORTH, north);
        if (east != null) condition.term(CardTableBlock.EAST, east);
        if (south != null) condition.term(CardTableBlock.SOUTH, south);
        if (west != null) condition.term(CardTableBlock.WEST, west);

        condition.term(CardTableBlock.CLOTH, true);
        condition.term(CardTableBlock.COLOR, color);

        if(condition.get().getAsJsonObject().isEmpty()) {
            generator.with(variant);
        }else{
            generator.with(condition, variant);
        }
    }

    private static ResourceLocation getPlanks(WoodType wood) {
        return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_planks");
    }

    private static ResourceLocation getLog(WoodType wood) {
        if (wood == WoodType.BAMBOO) {
            return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_stalk");
        } else if (wood == WoodType.CRIMSON || wood == WoodType.WARPED) {
            return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_stem");
        } else {
            return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_log");
        }
    }

    private static VariantProperties.Rotation getRot(int rot) {
        return switch (rot) {
            case 0 -> VariantProperties.Rotation.R0;
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> throw new IllegalStateException("Invalid rotation "+rot);
        };
    }

}


