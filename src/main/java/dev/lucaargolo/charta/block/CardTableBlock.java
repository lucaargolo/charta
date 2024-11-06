package dev.lucaargolo.charta.block;

import com.mojang.serialization.MapCodec;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.item.CardDeckItem;
import dev.lucaargolo.charta.item.ModDataComponentTypes;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.network.OpenCardTableScreenPayload;
import dev.lucaargolo.charta.utils.DyeColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardTableBlock extends BaseEntityBlock {

    public static final MapCodec<CardTableBlock> CODEC = simpleCodec(CardTableBlock::new);

    private static final List<Vector2i> VALID_DIMENSIONS = List.of(new Vector2i(3, 3), new Vector2i(4, 3), new Vector2i(5, 3));
    private static final int MAX_SIZE;

    public static final BooleanProperty VALID = BooleanProperty.create("valid");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty CLOTH = BooleanProperty.create("cloth");
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    static {
        Vector2i last = VALID_DIMENSIONS.getLast();
        MAX_SIZE = last.x * last.y;
    }

    public CardTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(VALID, false)
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(CLOTH, false)
            .setValue(COLOR, DyeColor.WHITE)
        );
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return !level.isClientSide ? createTickerHelper(blockEntityType, ModBlockEntityTypes.CARD_TABLE.get(), CardTableBlockEntity::serverTick) : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CardTableBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VALID, NORTH, EAST, SOUTH, WEST, CLOTH, COLOR);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if(player instanceof ServerPlayer serverPlayer) {
            if(serverPlayer.isShiftKeyDown()) {
                if (state.getValue(CLOTH)) {
                    BlockPos center = getCenterPos(level, pos);
                    level.getBlockEntity(center, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(cardTable -> {
                        Vec3 c = pos.getCenter();
                        if(!cardTable.getDeckStack().isEmpty()) {
                            Containers.dropItemStack(level, c.x, c.y, c.z, cardTable.getDeckStack());
                            cardTable.setDeckStack(ItemStack.EMPTY);
                            level.sendBlockUpdated(center, state, state, 3);
                        }else{
                            DyeColor color = state.getValue(COLOR);
                            getMultiblock(level, pos).forEach(p -> {
                                BlockState s = level.getBlockState(p);
                                level.setBlockAndUpdate(p, s.setValue(CLOTH, false));
                            });
                            Containers.dropItemStack(level, c.x, c.y, c.z, DyeColorHelper.getCarpet(color).asItem().getDefaultInstance());
                        }
                    });
                }
            }else {
                ItemStack stack = player.getMainHandItem();
                if (state.getValue(VALID)) {
                    if (!state.getValue(CLOTH)) {
                        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof WoolCarpetBlock carpetBlock) {
                            DyeColor color = carpetBlock.getColor();
                            getMultiblock(level, pos).forEach(p -> {
                                BlockState s = level.getBlockState(p);
                                level.setBlockAndUpdate(p, s.setValue(CLOTH, true).setValue(COLOR, color));
                            });
                        } else {
                            player.displayClientMessage(Component.literal("You need to put a cloth on this table.").withStyle(ChatFormatting.RED), true);
                        }
                    } else if (player instanceof LivingEntityMixed mixed) {
                        BlockPos center = getCenterPos(level, pos);
                        level.getBlockEntity(center, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(cardTable -> {
                            if (stack.getItem() instanceof CardDeckItem && stack.has(ModDataComponentTypes.CARD_DECK)) {
                                if(!cardTable.getDeckStack().isEmpty()) {
                                    Vec3 c = center.getCenter();
                                    Containers.dropItemStack(level, c.x, c.y, c.z, cardTable.getDeckStack());
                                }
                                cardTable.setDeckStack(stack.copy());
                                if(!serverPlayer.isCreative()) {
                                    stack.shrink(1);
                                }
                                level.sendBlockUpdated(center, state, state, 3);
                            } else {
                                List<CardPlayer> satPlayers = cardTable.getPlayers();
                                if (satPlayers.contains(mixed.charta_getCardPlayer())){
                                    CardGame<?> game = cardTable.getGame();
                                    if (game == null || game.isGameOver()) {
                                        PacketDistributor.sendToPlayer(serverPlayer, new OpenCardTableScreenPayload(center, cardTable.getDeck(), satPlayers.stream().mapToInt(CardPlayer::getId).toArray()));
                                    }else if(game.getPlayers().contains(mixed.charta_getCardPlayer())) {
                                        game.openScreen(serverPlayer, serverPlayer.serverLevel(), center, cardTable.getDeck());
                                    }else{
                                        player.displayClientMessage(Component.literal("You're not playing the current game.").withStyle(ChatFormatting.RED), true);
                                    }
                                } else{
                                    player.displayClientMessage(Component.literal("You need to be sat in the table to start a game.").withStyle(ChatFormatting.RED), true);
                                }
                            }
                        });
                    } else {
                        player.displayClientMessage(Component.literal("You are not a valid Card Player.").withStyle(ChatFormatting.RED), true);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if(!state.is(newState.getBlock()) && state.getValue(CLOTH)) {
            Vec3 c = pos.getCenter();
            ItemStack carpetStack = DyeColorHelper.getCarpet(state.getValue(COLOR)).asItem().getDefaultInstance();
            Containers.dropItemStack(level, c.x, c.y, c.z, carpetStack);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        boolean valid = isValidMultiblock(level, pos);
        state = switch (direction) {
            case Direction.NORTH -> state.setValue(VALID, valid).setValue(NORTH, neighborState.is(this));
            case Direction.EAST -> state.setValue(VALID, valid).setValue(EAST, neighborState.is(this));
            case Direction.SOUTH -> state.setValue(VALID, valid).setValue(SOUTH, neighborState.is(this));
            case Direction.WEST -> state.setValue(VALID, valid).setValue(WEST, neighborState.is(this));
            default -> super.updateShape(state, direction, neighborState, level, pos, neighborPos).setValue(VALID, valid);
        };
        return neighborState.is(this) ? state.setValue(CLOTH, neighborState.getValue(CLOTH)) : valid ? state : state.setValue(CLOTH, false);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean valid = isValidMultiblock(context.getLevel(), context.getClickedPos());
        return this.defaultBlockState().setValue(VALID, valid)
                .setValue(NORTH, context.getLevel().getBlockState(context.getClickedPos().north()).is(this))
                .setValue(EAST, context.getLevel().getBlockState(context.getClickedPos().east()).is(this))
                .setValue(SOUTH, context.getLevel().getBlockState(context.getClickedPos().south()).is(this))
                .setValue(WEST, context.getLevel().getBlockState(context.getClickedPos().west()).is(this));
    }

    @Override
    protected @NotNull MapCodec<CardTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    public Set<BlockPos> getMultiblock(LevelAccessor level, BlockPos pos) {
        Set<BlockPos> multiblock = new HashSet<>();
        floodFill(level, pos, new HashSet<>(), multiblock);
        return multiblock;
    }

    public BlockPos getCenterPos(LevelAccessor level, BlockPos pos) {
        Set<BlockPos> multiblock = getMultiblock(level, pos);
        BlockBox box = getBoundingBox(multiblock);
        BlockPos min = box.min();
        BlockPos max = box.max();
        return new BlockPos(min.getX() + Mth.floor((max.getX() - min.getX())/2.0), min.getY() + Mth.floor((max.getY() - min.getY())/2.0), min.getZ() + Mth.floor((max.getZ() - min.getZ())/2.0));
    }

    public boolean isValidMultiblock(LevelAccessor level, BlockPos pos) {
        Set<BlockPos> multiblock = getMultiblock(level, pos);
        int width = getWidth(multiblock);
        int height = getHeight(multiblock);
        return isValidDimensions(width, height) && isBoundingBoxFilled(level, getBoundingBox(multiblock));
    }

    private void floodFill(LevelAccessor level, BlockPos pos, Set<BlockPos> visited, Set<BlockPos> multiblock) {
        if (multiblock.size() > MAX_SIZE) return;
        if (!visited.add(pos)) return;
        if (level.getBlockState(pos).getBlock() != this) return;
        multiblock.add(pos);

        floodFill(level, pos.north(), visited, multiblock);
        floodFill(level, pos.south(), visited, multiblock);
        floodFill(level, pos.east(), visited, multiblock);
        floodFill(level, pos.west(), visited, multiblock);
    }

    private BlockBox getBoundingBox(Set<BlockPos> multiblock) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        int y = 0;

        for (BlockPos pos : multiblock) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
            y = pos.getY();
        }

        return BlockBox.of(new BlockPos(minX, y, minZ), new BlockPos(maxX, y, maxZ));
    }

    private boolean isBoundingBoxFilled(LevelAccessor level, BlockBox boundingBox) {
        for(BlockPos pos : boundingBox) {
            if (level.getBlockState(pos).getBlock() != this) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidDimensions(int width, int height) {
        return VALID_DIMENSIONS.contains(new Vector2i(width, height)) || VALID_DIMENSIONS.contains(new Vector2i(height, width));
    }

    private int getWidth(Set<BlockPos> multiblock) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (BlockPos pos : multiblock) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
        }
        return maxX - minX + 1;
    }

    private int getHeight(Set<BlockPos> multiblock) {
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : multiblock) {
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return maxZ - minZ + 1;
    }

}
