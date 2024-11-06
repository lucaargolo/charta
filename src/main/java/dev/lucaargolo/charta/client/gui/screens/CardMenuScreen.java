package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.client.gui.components.CardSlotWidget;
import dev.lucaargolo.charta.client.gui.components.CardWidget;
import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.network.CardContainerSlotClickPayload;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.CardPlayerHead;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import dev.lucaargolo.charta.utils.TickableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class CardMenuScreen<G extends CardGame<G>, T extends AbstractCardMenu<G>> extends AbstractContainerScreen<T> implements HoverableRenderable {

    private final List<CardSlotWidget<G>> slotWidgets = new ArrayList<>();
    private HoverableRenderable hoverable = null;
    private CardSlot<G> hoveredCardSlot = null;
    private int hoveredCardId = -1;

    public CardMenuScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public CardDeck getDeck() {
        return menu.getDeck();
    }

    public G getGame() {
        return menu.getGame();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredCardSlot != null) {
            PacketDistributor.sendToServer(new CardContainerSlotClickPayload(menu.containerId, hoveredCardSlot.index, hoveredCardId));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public final void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    protected void init() {
        super.init();
        slotWidgets.clear();
        menu.cardSlots.forEach(slot -> {
            slotWidgets.add(new CardSlotWidget<>(this, slot));
        });
    }

    public void renderTopBar(@NotNull GuiGraphics guiGraphics) {
        G game = this.menu.getGame();
        int players = game.getPlayers().size();
        float totalWidth = CardSlot.getWidth(CardSlot.Type.PREVIEW) + 28;
        float playersWidth = (players * totalWidth) + ((players-1f) * (totalWidth/10f));
        guiGraphics.fill(0, 0, Mth.floor((width - playersWidth)/2f), 28, 0x88000000);
        guiGraphics.fill(width - Mth.floor((width - playersWidth)/2f), 0, width, 28, 0x88000000);
        for(int i = 0; i < players; i++) {
            CardPlayer player = game.getPlayers().get(i);
            float x = width/2f - playersWidth/2f + (i*(totalWidth + totalWidth/10f));
            Component text = player.getName();
            DyeColor color = player.getColor();
            guiGraphics.fill(Mth.floor(x), 0, Mth.ceil(x + totalWidth), 28, 0x88000000 + color.getTextureDiffuseColor());
            if(i < players-1) {
                guiGraphics.fill(Mth.ceil(x + totalWidth), 0, Mth.floor(x + totalWidth + totalWidth/10f), 28, 0x88000000);
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate( x + 26f, 2f, 0f);
            guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
            guiGraphics.drawString(font, text, 0, 0, 0xFFFFFFFF, true);
            guiGraphics.pose().popPose();

            Minecraft mc = Minecraft.getInstance();
            Function<ResourceLocation, TextureAtlasSprite> function = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
            ResourceLocation wool = ResourceLocation.withDefaultNamespace("block/"+color.getName()+"_wool");
            TextureAtlasSprite woolSprite = function.apply(wool);
            guiGraphics.blit((int) (x+4), 2, 0, 16, 1, woolSprite);
            guiGraphics.blit((int) (x+2), 2+1, 0, 20, 22, woolSprite);
            guiGraphics.blit((int) (x+4), 2+23, 0, 16, 1, woolSprite);

            CardPlayerHead.renderHead(guiGraphics, (int) x, 2, player);
        }
    }

    public void renderBottomBar(@NotNull GuiGraphics guiGraphics) {
        CardPlayer player = menu.getCardPlayer();
        DyeColor color = player.getColor();
        int totalWidth = Mth.floor(CardSlot.getWidth(CardSlot.Type.INVENTORY)) + 10;
        guiGraphics.fill(0, height-63, (width-totalWidth)/2, height, 0x88000000);
        guiGraphics.fill((width-totalWidth)/2, height-63, (width-totalWidth)/2 + totalWidth, height, 0x88000000  + color.getTextureDiffuseColor());
        guiGraphics.fill((width-totalWidth)/2 + totalWidth, height-63, width, height, 0x88000000);

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBlurredBackground(partialTick);

        this.renderTopBar(guiGraphics);
        this.renderBottomBar(guiGraphics);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        List<Renderable> renderablesBackup = List.copyOf(this.renderables);

        this.hoveredCardSlot = null;
        for (int k = 0; k < this.menu.cardSlots.size(); k++) {
            CardSlot<G> slot = this.menu.cardSlots.get(k);

            if (this.isHoveringPrecise(slot, mouseX, mouseY)) {
                this.hoveredCardSlot = slot;
            }

            if(!slot.isEmpty()) {
                CardSlotWidget<G> slotWidget = this.slotWidgets.get(k);
                slotWidget.setPreciseX(slot.x + this.leftPos);
                if(slot.getType() == CardSlot.Type.INVENTORY) {
                    slotWidget.setPreciseY(slot.y + this.height - slotWidget.getPreciseHeight());
                }else if(slot.getType() == CardSlot.Type.PREVIEW) {
                    slotWidget.setPreciseY(slot.y);
                }else{
                    slotWidget.setPreciseY(slot.y + this.topPos);
                }
                this.renderables.add(slotWidget);
            }
        }

        if(this.hoverable != null && !this.renderables.contains(hoverable)) {
            this.hoverable = null;
        }

        for (Renderable renderable : this.renderables) {
            if(renderable != this.hoverable) {
                /*
                This method call might seem weird at first, but by rendering the
                current hoverable before each other possible hoverable, we stop
                the cards from flashing weirdly on certain changing cases.
                 */
                if(this.hoverable != null) {
                    this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                if(renderable instanceof AbstractWidget widget && renderable instanceof HoverableRenderable) {
                    if(widget.isHovered && (!(this.hoverable instanceof AbstractWidget other) || !other.isHovered)){
                        this.hoverable = (HoverableRenderable) renderable;
                    }
                }
            }
        }

        this.renderables.clear();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderables.addAll(renderablesBackup);

        if(this.hoverable != null) {
            this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
            if(this.hoverable instanceof AbstractWidget widget && !widget.isHovered) {
                this.hoverable = null;
            }
        }

        if(this.hoveredCardSlot != null && this.hoverable instanceof CardSlotWidget<?> cardSlotWidget) {
            this.hoveredCardId = cardSlotWidget.getHoveredId();
        }

        List<Card> cards = this.menu.getCarriedCards();
        if (!cards.isEmpty()) {
            Card card = cards.getLast();
            if(card.isFlipped()) {
                CardWidget.renderCard(this.getDeck().getDeckTexture(), guiGraphics, mouseX- CardImage.WIDTH, mouseY-CardImage.HEIGHT, mouseX, mouseY, partialTick);
            }else{
                CardWidget.renderCard(this.getDeck().getCardTexture(card), guiGraphics, mouseX-CardImage.WIDTH, mouseY-CardImage.HEIGHT, mouseX, mouseY, partialTick);
            }
        }
    }

    public boolean isHoveredCardSlot(CardSlot<G> slot) {
        return this.hoveredCardSlot == slot;
    }

    private boolean isHoveringPrecise(CardSlot<G> slot, float mouseX, float mouseY) {
        return switch (slot.getType()) {
            case INVENTORY -> this.isHoveringPrecise(slot.x, slot.y - topPos + height - CardSlot.getHeight(slot), CardSlot.getWidth(slot), CardSlot.getHeight(slot), mouseX, mouseY);
            case PREVIEW -> this.isHoveringPrecise(slot.x, slot.y - topPos, CardSlot.getWidth(slot), CardSlot.getHeight(slot), mouseX, mouseY);
            default -> this.isHoveringPrecise(slot.x, slot.y, CardSlot.getWidth(slot), CardSlot.getHeight(slot), mouseX, mouseY);
        };
    }

    protected boolean isHoveringPrecise(float x, float y, float width, float height, double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        mouseX -= i;
        mouseY -= j;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if(this.minecraft != null) {
            int mouseX = (int) (this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow().getGuiScaledWidth() / (double) this.minecraft.getWindow().getScreenWidth());
            int mouseY = (int) (this.minecraft.mouseHandler.ypos() * (double) this.minecraft.getWindow().getGuiScaledHeight() / (double) this.minecraft.getWindow().getScreenHeight());
            for (GuiEventListener widget : this.children()) {
                if (widget instanceof TickableWidget tickable) {
                    tickable.tick(mouseX, mouseY);
                }
            }
            for(CardSlotWidget<G> widget : this.slotWidgets) {
                widget.tick(mouseX, mouseY);
            }
        }
    }


    @Override
    public @Nullable HoverableRenderable getHoverable() {
        return this.hoverable;
    }

}
