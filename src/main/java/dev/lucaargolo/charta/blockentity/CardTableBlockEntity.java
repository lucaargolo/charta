package dev.lucaargolo.charta.blockentity;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.charta.block.CardTableBlock;
import dev.lucaargolo.charta.block.GameChairBlock;
import dev.lucaargolo.charta.block.SeatBlock;
import dev.lucaargolo.charta.entity.SeatEntity;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.item.CardDeckItem;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.network.GameSlotCompletePayload;
import dev.lucaargolo.charta.network.GameSlotPositionPayload;
import dev.lucaargolo.charta.network.GameSlotResetPayload;
import dev.lucaargolo.charta.network.GameStartPayload;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.GameSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

public class CardTableBlockEntity extends BlockEntity {

    public static final int TABLE_WIDTH = 160;
    public static final int TABLE_HEIGHT = 160;

    private static final Predicate<Vector2i> PY = pos -> pos.x == 0 && pos.y > 0;
    private static final Predicate<Vector2i> PX_PY = pos -> pos.x > 0 && pos.y > 0;
    private static final Predicate<Vector2i> PX = pos -> pos.x > 0 && pos.y == 0;
    private static final Predicate<Vector2i> PX_NY = pos -> pos.x > 0 && pos.y < 0;
    private static final Predicate<Vector2i> NY = pos -> pos.x == 0 && pos.y < 0;
    private static final Predicate<Vector2i> NX_XY = pos -> pos.x < 0 && pos.y < 0;
    private static final Predicate<Vector2i> NX = pos -> pos.x < 0 && pos.y == 0;
    private static final Predicate<Vector2i> NX_PY = pos -> pos.x < 0 && pos.y > 0;

    @SuppressWarnings("unchecked")
    private static final Predicate<Vector2i>[] PREDICATES = new Predicate[] {
        PY, PX_PY, PX, PX_NY, NY, NX_XY, NX, NX_PY
    };

    private final List<GameSlot> trackedGameSlots = new ArrayList<>();
    private final Set<Integer> dirtySlotCards = new HashSet<>();
    private final Set<Integer> dirtySlotPositions = new HashSet<>();

    private ItemStack deckStack = ItemStack.EMPTY;
    public Vector2f centerOffset = new Vector2f();

    @Nullable
    private CardGame<?> game = null;
    private int age = 0;
    public boolean playersDirty = true;

    public CardTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.CARD_TABLE.get(), pos, blockState);
    }

    @Nullable
    public CardDeck getDeck() {
        return CardDeckItem.getDeck(this.deckStack);
    }

    @Nullable
    public CardGame<?> getGame() {
        return game;
    }

    public Component startGame(@Nullable ResourceLocation gameId) {
        CardDeck deck = getDeck();
        if(deck != null) {
            if(gameId != null) {
                List<CardPlayer> players = this.getOrderedPlayers();
                CardGames.CardGameFactory<?> factory = CardGames.getGame(gameId);
                if(factory != null) {
                    CardGame<?> game = factory.create(players, this.getDeck());
                    if(CardGame.canPlayGame(game, this.getDeck())) {
                        if (players.size() >= game.getMinPlayers()) {
                            if (players.size() <= game.getMaxPlayers()) {
                                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), new GameSlotResetPayload(worldPosition));
                                this.resetGameSlots();
                                this.game = game;
                                this.game.startGame();
                                this.game.runGame();
                                int index = 0;
                                for(GameSlot slot : this.game.getGameSlots()) {
                                    slot.setX(slot.getX() + centerOffset.x * 160f);
                                    slot.setY(slot.getY() + centerOffset.y * 160f);
                                    slot.setup(this, index++);
                                    addGameSlot(slot);
                                }
                                for(CardPlayer player : players) {
                                    LivingEntity entity = player.getEntity();
                                    if(entity != null) {
                                        Vector3f offset = entity.position().subtract(worldPosition.getX() + 0.5, entity.getY(), worldPosition.getZ() + 0.5).toVector3f();
                                        Direction direction = GameChairBlock.getSeatedDirection(entity);
                                        if(direction != null) {
                                            float angle = switch (direction) {
                                                case EAST -> 90;
                                                case SOUTH -> 180;
                                                case WEST -> 270;
                                                default -> 0;
                                            };
                                            float x = switch (direction) {
                                                case NORTH -> 40f + (160f * offset.x);
                                                case EAST -> -147.5f + (160f * (offset.x + 2));
                                                case SOUTH -> 160f - 40f + (160f * offset.x);
                                                case WEST -> 160f + 147.5f + (160f * (offset.x - 2));
                                                default -> 0;
                                            };
                                            float y = switch (direction) {
                                                case NORTH -> -147.5f - (160f * (offset.z - 2));
                                                case EAST -> 160f - 40f - (160f * offset.z);
                                                case SOUTH -> 160f + 147.5f - (160f * (offset.z + 2));
                                                case WEST -> 160f - 147.5f - (160f * offset.z) + (CardImage.WIDTH + CardImage.WIDTH/10f);
                                                default -> 0;
                                            };
                                            GameSlot slot = new GameSlot(game.getCensoredHand(player), x, y, 0f, angle, direction.getClockWise());
                                            slot.setup(this, index++);
                                            addGameSlot(slot);
                                        }
                                        if(entity instanceof ServerPlayer serverPlayer) {
                                            PacketDistributor.sendToPlayer(serverPlayer, new GameStartPayload());
                                        }
                                    }
                                    player.openScreen(this.game, this.worldPosition, deck);
                                }
                                return Component.translatable("charta.message.game_started").withStyle(ChatFormatting.GREEN);
                            } else {
                                this.game = null;
                                return Component.translatable("charta.message.too_many_players", game.getMaxPlayers()).withStyle(ChatFormatting.RED);
                            }
                        } else {
                            this.game = null;
                            return Component.translatable("charta.message.not_enough_players", game.getMinPlayers()).withStyle(ChatFormatting.RED);
                        }
                    }else{
                        this.game = null;
                        return Component.translatable("charta.message.cant_play_deck").withStyle(ChatFormatting.RED);
                    }
                }else{
                    this.game = null;
                    return Component.translatable("charta.message.table_unknown_game").withStyle(ChatFormatting.RED);
                }
            }else{
                this.game = null;
                return Component.translatable("charta.message.table_no_game").withStyle(ChatFormatting.RED);
            }
        }else{
            this.game = null;
            return Component.translatable("charta.message.table_no_deck").withStyle(ChatFormatting.RED);
        }

    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if(!deckStack.isEmpty()) {
            tag.put("deckStack", deckStack.save(registries));
        }else{
            tag.put("deckStack", new CompoundTag());
        }
        tag.putFloat("centerOffsetX", centerOffset.x);
        tag.putFloat("centerOffsetY", centerOffset.y);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if(tag.contains("deckStack")) {
            Tag deckTag = tag.get("deckStack");
            if(deckTag instanceof CompoundTag compoundTag && !compoundTag.getAllKeys().isEmpty()) {
                ItemStack.parse(registries, deckTag).ifPresentOrElse(this::setDeckStack, () -> setDeckStack(ItemStack.EMPTY));
            }else {
                setDeckStack(ItemStack.EMPTY);
            }
        }
        centerOffset.x = tag.getFloat("centerOffsetX");
        centerOffset.y = tag.getFloat("centerOffsetY");
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider) {
        this.resetGameSlots();
        super.handleUpdateTag(tag, lookupProvider);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getDeckStack() {
        return deckStack;
    }

    public void setDeckStack(ItemStack deckStack) {
        this.deckStack = deckStack;
        setChanged();
    }

    public GameSlot getGameSlot(int index) {
        return this.trackedGameSlots.get(index);
    }

    public void addGameSlot(GameSlot slot) {
        this.dirtySlotCards.add(this.getGameSlotCount());
        this.trackedGameSlots.add(slot);
    }

    public void setGameSlot(int index, GameSlot slot) {
        this.trackedGameSlots.set(index, slot);
        this.dirtySlotCards.add(index);
    }

    public void setGameSlotDirty(int index, boolean cards) {
        if (cards)
            this.dirtySlotCards.add(index);
        else
            this.dirtySlotPositions.add(index);
    }

    public int getGameSlotCount() {
        return this.trackedGameSlots.size();
    }

    public void resetGameSlots() {
        this.trackedGameSlots.forEach(GameSlot::clear);
        this.trackedGameSlots.clear();
        this.dirtySlotCards.clear();
        this.dirtySlotPositions.clear();
    }

    public List<LivingEntity> getPlayers() {
        List<LivingEntity> players = new ArrayList<>();
        if(this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if(state.getBlock() instanceof CardTableBlock cardTable) {
                Set<BlockPos> set = cardTable.getMultiblock(this.level, this.worldPosition);
                Set<BlockPos> chairs = new HashSet<>();
                for(BlockPos pos : set) {
                    if(!set.contains(pos.north())) chairs.add(pos.north());
                    if(!set.contains(pos.south())) chairs.add(pos.south());
                    if(!set.contains(pos.east())) chairs.add(pos.east());
                    if(!set.contains(pos.west())) chairs.add(pos.west());
                }
                for(BlockPos pos : chairs) {
                    BlockState chairState = this.level.getBlockState(pos);
                    if(chairState.getBlock() instanceof GameChairBlock && set.contains(pos.relative(chairState.getValue(GameChairBlock.FACING)))) {
                        if(SeatBlock.isSeatOccupied(this.level, pos)) {
                            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
                            if(!seats.isEmpty()) {
                                List<Entity> passengers = seats.getFirst().getPassengers();
                                if(!passengers.isEmpty() && passengers.getFirst() instanceof LivingEntity entity) {
                                    players.add(entity);
                                }
                            }
                        }
                    }
                }
            }
        }
        return players;
    }


    private List<CardPlayer> getOrderedPlayers() {
        List<LivingEntity> players = this.getPlayers();
        if(players.isEmpty()) {
            return Collections.emptyList();
        }
        List<Pair<Vector2i, CardPlayer>> entries = new LinkedList<>();
        Vec3 center = worldPosition.getCenter();
        players.forEach(player -> {
            Vector2i pos = new Vector2i(Mth.floor((player.getX()-center.x)*2), Mth.floor((player.getZ()-center.z)*2));
            entries.add(new Pair<>(pos, ((LivingEntityMixed) player).charta_getCardPlayer()));
        });

        Collections.shuffle(entries);
        Pair<Vector2i, CardPlayer> firstEntry = entries.getFirst();
        Vector2i firstPos = firstEntry.getFirst();
        int firstQuadrant = getQuadrant(firstPos);
        List<Integer> order = new ArrayList<>();
        for(int i = 0; i < PREDICATES.length; i++) {
            order.add((firstQuadrant + i) % PREDICATES.length);
        }

        entries.sort((a, b) -> {
            Vector2i va = a.getFirst();
            Vector2i vb = b.getFirst();

            int aq = getQuadrant(va);
            int bq = getQuadrant(vb);

            if(aq == bq) {
                return switch (aq) {
                    case 0 -> Integer.compare(vb.y, va.y);
                    case 1 -> vb.y != va.y ? Integer.compare(vb.y, va.y) : Integer.compare(va.x, vb.x);
                    case 2 -> Integer.compare(vb.x, va.x);
                    case 3 -> vb.x != va.x ? Integer.compare(vb.x, va.x) : Integer.compare(vb.y, va.y);
                    case 4 -> Integer.compare(va.y, vb.y);
                    case 5 -> va.y != vb.y ? Integer.compare(va.y, vb.y) : Integer.compare(vb.x, va.x);
                    case 6 -> Integer.compare(va.x, vb.x);
                    case 7 -> va.x != vb.x ? Integer.compare(va.x, vb.x) : Integer.compare(va.y, vb.y);
                    default -> 0;
                };
            }else{
                return Integer.compare(order.indexOf(aq), order.indexOf(bq));
            }
        });

        return entries.stream().map(Pair::getSecond).toList().reversed();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CardTableBlockEntity blockEntity) {
        Iterator<Integer> updateIterator = blockEntity.dirtySlotCards.iterator();
        while (updateIterator.hasNext()) {
            int index = updateIterator.next();
            GameSlot slot = blockEntity.trackedGameSlots.get(index);
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(pos), new GameSlotCompletePayload(pos, index, slot));
            blockEntity.dirtySlotPositions.remove(index);
            updateIterator.remove();
        }
        updateIterator = blockEntity.dirtySlotPositions.iterator();
        while (updateIterator.hasNext()) {
            int index = updateIterator.next();
            GameSlot slot = blockEntity.trackedGameSlots.get(index);
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(pos), new GameSlotPositionPayload(pos, index, slot.getX(), slot.getY(), slot.getZ(), slot.getAngle()));
            updateIterator.remove();
        }
        if(!state.getValue(CardTableBlock.CLOTH) && !blockEntity.getDeckStack().isEmpty()) {
            Vec3 c = pos.getCenter();
            Containers.dropItemStack(level, c.x, c.y, c.z, blockEntity.getDeckStack());
            blockEntity.setDeckStack(ItemStack.EMPTY);
            level.sendBlockUpdated(pos, state, state, 3);
        }
        if(blockEntity.game != null) {
            CardGame<?> game = blockEntity.game;
            if(!game.isGameOver()) {
                if (blockEntity.age++ % 100 == 0 || blockEntity.playersDirty) {
                    List<CardPlayer> players = blockEntity.getOrderedPlayers();
                    if (!players.containsAll(game.getPlayers())) {
                        game.endGame();
                    }
                    blockEntity.playersDirty = false;
                }
                game.tick();
            }else{
                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(pos), new GameSlotResetPayload(pos));
                blockEntity.resetGameSlots();
                blockEntity.game = null;
            }
        }
    }

    private static int getQuadrant(Vector2i pos) {
        int quadrant = 0;
        for(int i = 0; i < PREDICATES.length; i++) {
            if(PREDICATES[i].test(pos)) {
                quadrant = i;
                break;
            }
        }
        return quadrant;
    }

}
