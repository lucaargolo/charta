package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.client.render.screen.DeckScreen;
import dev.lucaargolo.charta.game.Deck;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DeckItem extends Item {

    public DeckItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Deck deck = getDeck(stack);
        return deck != null ? deck.getName() : super.getName(stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if(level.isClientSide()) {
            Deck deck = getDeck(stack);
            if(deck != null)
                openScreen(deck);
        }
        return InteractionResultHolder.success(stack);
    }

    private static void openScreen(Deck deck) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new DeckScreen(null, deck));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        Deck deck = getDeck(stack);
        if(deck != null) {
            tooltipComponents.add(Component.literal(String.valueOf(deck.getCards().size())).append(" ").append(Component.translatable("charta.cards")).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Nullable
    public static Deck getDeck(ItemStack stack) {
        ResourceLocation deckId = stack.get(ModDataComponentTypes.CARD_DECK.get());
        return deckId != null ? ChartaMod.CARD_DECKS.getDeck(deckId) : null;
    }

    public static ItemStack getDeck(Deck deck) {
        ResourceLocation deckId = ChartaMod.CARD_DECKS.getDecks()
            .entrySet()
            .stream()
            .filter(e -> e.getValue() == deck)
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(ChartaMod.id("missing"));
        return getDeck(deckId);
    }

    public static ItemStack getDeck(ResourceLocation deckId) {
        Deck deck = ChartaMod.CARD_DECKS.getDeck(deckId);
        ItemStack stack = ModItems.DECK.get().getDefaultInstance();
        stack.set(ModDataComponentTypes.CARD_DECK.get(), deckId);
        stack.set(DataComponents.RARITY, deck.getRarity());
        return stack;
    }

}
