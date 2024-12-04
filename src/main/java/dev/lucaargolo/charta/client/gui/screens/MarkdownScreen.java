package dev.lucaargolo.charta.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import dev.lucaargolo.charta.client.ChartaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MarkdownScreen extends Screen {

    private final Screen parent;
    private final List<Either<String, String>> markdown;

    private MarkdownWidget widget;

    public MarkdownScreen(Component title, Screen parent, String markdown) {
        super(title);
        this.parent = parent;
        String resourceId = I18n.get("charta_md:"+markdown);
        ResourceLocation id = ResourceLocation.tryParse(resourceId);
        if (id != null) {
            this.markdown = ChartaClient.MARKDOWN.getMarkdown(id);
        }else{
            this.markdown = List.of(Either.left("p"), Either.right(resourceId));
        }
    }

    @Override
    protected void init() {
        this.widget = this.addRenderableWidget(new MarkdownWidget(minecraft, width, height - 60, 30));

        LinkedList<MarkdownTag> stack = new LinkedList<>();
        LinkedList<Integer> countStack = new LinkedList<>();
        AtomicInteger space = new AtomicInteger();
        AtomicBoolean started = new AtomicBoolean(false);
        MutableComponent text = Component.empty();
        for(Either<String, String> either : markdown) {
            either.ifLeft(t -> {
                boolean isCancel = t.startsWith("/");
                MarkdownTag tag = MarkdownTag.get(t.replace("/", ""));

                if(isCancel) {
                    if(tag == MarkdownTag.OL) {
                        countStack.removeLast();
                    }
                    if(tag.start) {
                        started.set(false);
                        this.widget.addEntry(new MarkdownLine(font, List.copyOf(stack), text.copy(), space.get(), false));
                        text.getSiblings().clear();
                    }
                    stack.removeLastOccurrence(tag);
                }else{
                    if(tag == MarkdownTag.OL) {
                        countStack.add(0);
                    }
                    if(tag.start) {
                        if(started.get()) {
                            this.widget.addEntry(new MarkdownLine(font, List.copyOf(stack), text.copy(), space.get(), false));
                            text.getSiblings().clear();
                        }
                        if(!this.widget.children().isEmpty() && tag.line) {
                            this.widget.addEmptyEntry(font);
                        }
                        started.set(true);
                    }
                    stack.addLast(tag);
                }
            }).ifRight(t -> {
                boolean bold = false;
                boolean italic = false;
                if(text.getString().isEmpty()) {
                    int listTabs = 0;
                    int numberedTabs = 0;
                    boolean numbered = false;

                    for (MarkdownTag tag : stack) {
                        switch (tag) {
                            case UL -> {
                                listTabs++;
                                numbered = false;
                            }
                            case OL -> {
                                numberedTabs++;
                                numbered = true;
                            }
                        }
                    }

                    space.set(0);

                    for(int i = 0; i < listTabs; i++) {
                        text.append("  ");
                        space.addAndGet(font.width("  "));
                    }
                    for(int i = 0; i < numberedTabs - 1; i++) {
                        text.append("  ");
                        space.addAndGet(font.width("  "));
                    }

                    if(numbered) {
                        countStack.addLast(countStack.removeLast() + 1);
                        text.append(countStack.getLast() + ". ");
                        space.addAndGet(font.width(countStack.getLast() + ". "));
                    }else if(listTabs + numberedTabs > 0){
                        text.append(" - ");
                        space.addAndGet(font.width(" - "));
                    }
                }
                for (MarkdownTag tag : stack) {
                    switch (tag) {
                        case STRONG -> bold = true;
                        case EM -> italic = true;
                    }
                }
                boolean em = italic;
                boolean strong = bold;
                text.append(Component.literal(t).withStyle(s -> s.withBold(strong).withItalic(em)));
            });
        }


    }


    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, title, width/2, 10, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        if(this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static class MarkdownLine extends ContainerObjectSelectionList.Entry<MarkdownLine> {

        private final Font font;
        private final List<MarkdownTag> tags;
        private final boolean tall;
        private final FormattedText text;
        private final double space;
        private final boolean align;

        public MarkdownLine(Font font, List<MarkdownTag> tags, FormattedText text, double space, boolean align) {
            this.font = font;
            this.tags = tags;
            this.tall = tags.stream().anyMatch(t -> t == MarkdownTag.H1 || t == MarkdownTag.H2 || t == MarkdownTag.H3 || t == MarkdownTag.H4);
            this.text = text;
            for(MarkdownTag tag : tags) {
                switch (tag) {
                    case H1 -> space = space*2.0;
                    case H2 -> space = space*1.5;
                    case H3 -> space = space*1.25;
                    case H4 -> space = space*1.125;
                    case H6 -> space = space*0.75;
                }
            }
            this.space = space;
            this.align = align;

        }

        public MarkdownLine withText(FormattedText text, boolean first) {
            return new MarkdownLine(this.font, this.tags, text, space, !first);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            PoseStack stack = guiGraphics.pose();
            stack.pushPose();
            stack.translate(left-12, top, 0);
            if(align) {
                stack.translate(space, 0, 0);
            }
            for(MarkdownTag tag : tags) {
                switch (tag) {
                    case H1 -> stack.scale(2.0f, 2.0f, 2.0f);
                    case H2 -> stack.scale(1.5f, 1.5f, 1.5f);
                    case H3 -> stack.scale(1.25f, 1.25f, 1.25f);
                    case H4 -> stack.scale(1.125f, 1.125f, 1.125f);
                    case H6 -> stack.scale(0.75f, 0.75f, 0.75f);
                }
            }
            guiGraphics.drawString(this.font, Language.getInstance().getVisualOrder(text), 0, 0, 0xFFFFFF);
            stack.popPose();
        }

    }

    public static class MarkdownWidget extends ContainerObjectSelectionList<MarkdownLine> {


        public MarkdownWidget(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, 10);
        }

        @Override
        protected void renderItem(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
            super.renderItem(guiGraphics, mouseX, mouseY, partialTick, index, left, top, width, height);
        }

        public void addEmptyEntry(Font font) {
            super.addEntry(new MarkdownLine(font, List.of(), Component.empty(), 0, false));
        }

        @Override
        public int addEntry(@NotNull MarkdownLine entry) {
            if(!entry.text.getString().isEmpty()) {
                List<FormattedText> lines = entry.font.getSplitter().splitLines(entry.text, this.getRowWidth(), Style.EMPTY);
                boolean first = true;
                for(FormattedText line : lines) {
                    if (entry.tall) {
                        super.addEntry(entry.withText(line, first));
                        addEmptyEntry(entry.font);
                    } else {
                        super.addEntry(entry.withText(line, first));
                    }
                    first = false;
                }
            }
            return this.children().size() - 1;
        }

        @Override
        public int getRowWidth() {
            return Math.min(600, minecraft.getWindow().getGuiScaledWidth()-32);
        }

    }

    public enum MarkdownTag {
        P(true, true), H1(true, true), H2(true, true), H3(true, true), H4(true, true), H5(true, true), H6(true, true),
        STRONG(false, false), EM(false, false),
        UL(false, false), OL(false, false),
        LI(true, false), A(false, false);

        final boolean start;
        final boolean line;

        MarkdownTag(boolean start, boolean line) {
            this.start = start;
            this.line = line;
        }

        public static MarkdownTag get(String tag) {
            try {
                return MarkdownTag.valueOf(tag.toUpperCase());
            }catch (Exception e) {
                return MarkdownTag.P;
            }
        }
    }

}
