package dev.lucaargolo.charta.blockentity;

import dev.lucaargolo.charta.block.CardTableBlock;
import dev.lucaargolo.charta.block.SeatBlock;
import dev.lucaargolo.charta.block.GameChairBlock;
import dev.lucaargolo.charta.entity.SeatEntity;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.item.ModDataComponentTypes;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardTableBlockEntity extends BlockEntity {


    private ItemStack deckStack = ItemStack.EMPTY;
    @Nullable
    private ResourceLocation gameId = null;
    @Nullable
    private CardGame<?> game = null;
    private int age = 0;

    public CardTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.CARD_TABLE.get(), pos, blockState);
    }

    public List<CardPlayer> getPlayers() {
        List<CardPlayer> players = new ArrayList<>();
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
                                if(!passengers.isEmpty() && passengers.getFirst() instanceof LivingEntityMixed mixed) {
                                    players.add(mixed.charta_getCardPlayer());
                                }
                            }
                        }
                    }
                }
            }
        }
        return players;
    }

    @Nullable
    public CardDeck getDeck() {
        return this.deckStack.get(ModDataComponentTypes.CARD_DECK.get());
    }

    public ItemStack getDeckStack() {
        return deckStack;
    }

    public void setDeckStack(ItemStack deckStack) {
        this.deckStack = deckStack;
        setChanged();
    }

    @Nullable
    public CardGame<?> getGame() {
        return game;
    }

    public Component startGame(@Nullable ResourceLocation gameId) {
        CardDeck deck = getDeck();
        if(deck != null) {
            if(gameId != null) {
                List<CardPlayer> players = this.getPlayers();
                CardGames.CardGameFactory<?> factory = CardGames.getGame(gameId);
                if(factory != null) {
                    CardGame<?> game = factory.create(players, this.getDeck());
                    if(CardGame.canPlayGame(game, this.getDeck())) {
                        if (players.size() >= game.getMinPlayers()) {
                            if (players.size() <= game.getMaxPlayers()) {
                                this.game = game;
                                this.game.startGame();
                                this.game.runGame();
                                this.gameId = gameId;
                                this.getPlayers().forEach(player -> player.openScreen(this.game, this.worldPosition, deck));
                                return Component.literal("Game successfully started").withStyle(ChatFormatting.GREEN);
                            } else {
                                this.game = null;
                                this.gameId = null;
                                return Component.literal("You need at most " + game.getMaxPlayers() + " players to play this game").withStyle(ChatFormatting.RED);
                            }
                        } else {
                            this.game = null;
                            this.gameId = null;
                            return Component.literal("You need at least " + game.getMinPlayers() + " players to play this game").withStyle(ChatFormatting.RED);
                        }
                    }else{
                        this.game = null;
                        this.gameId = null;
                        return Component.literal("You can't play this game with this deck.").withStyle(ChatFormatting.RED);
                    }
                }else{
                    this.game = null;
                    this.gameId = null;
                    return Component.literal("Table received an unknown game id.").withStyle(ChatFormatting.RED);
                }
            }else{
                this.game = null;
                this.gameId = null;
                return Component.literal("Table received no game id.").withStyle(ChatFormatting.RED);
            }
        }else{
            this.game = null;
            this.gameId = null;
            return Component.literal("You need a deck to be able to play games.").withStyle(ChatFormatting.RED);
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
        if(tag.contains("gameId")) {
            CardDeck deck = getDeck();
            this.gameId = ResourceLocation.tryParse(tag.getString("gameId"));
            if(deck != null && this.gameId != null) {
                CardGames.CardGameFactory<?> factory = CardGames.getGame(this.gameId);
                if(factory != null) {
                    this.game = factory.create(this.getPlayers(), this.getDeck());
                }
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        if(game != null && gameId != null) {
            tag.putString("gameId", gameId.toString());
        }
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CardTableBlockEntity blockEntity) {
        if(!state.getValue(CardTableBlock.CLOTH) && !blockEntity.getDeckStack().isEmpty()) {
            Vec3 c = pos.getCenter();
            Containers.dropItemStack(level, c.x, c.y, c.z, blockEntity.getDeckStack());
            blockEntity.setDeckStack(ItemStack.EMPTY);
            level.sendBlockUpdated(pos, state, state, 3);
        }
        if(blockEntity.getGame() != null) {
            CardGame<?> game = blockEntity.getGame();
            if(blockEntity.age++ % 100 == 0) {
                List<CardPlayer> players = blockEntity.getPlayers();
                if(!players.containsAll(game.getPlayers())) {
                    game.endGame();
                }
            }
            game.tick();
        }
    }
}
