package dev.lucaargolo.charta.resources;


import com.mojang.datafixers.util.Either;
import dev.lucaargolo.charta.Charta;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.commonmark.internal.renderer.NodeRendererMap;
import org.commonmark.internal.util.Escaping;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.html.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MarkdownResource implements ResourceManagerReloadListener {

    private final HashMap<ResourceLocation, List<Either<String, String>>> markdowns = new HashMap<>();
    private final Parser parser = Parser.builder().build();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        markdowns.clear();
        manager.listResources("lang/charta_md", id -> id.getPath().endsWith(".md")).forEach((id, resource) -> {
            try(InputStream stream = resource.open()) {
                ResourceLocation location = id.withPath(s -> s.replace("lang/charta_md/", "").replace(".md", ""));
                String markdown = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

                Node document = this.parser.parse(markdown);
                ObjectCollector renderer = new ObjectCollector();
                renderer.render(document, null);
                this.markdowns.put(location, renderer.getNodes());
            }catch (IOException e) {
                Charta.LOGGER.error("Error while reading markdown {} :", id, e);
            }
        });
        Charta.LOGGER.info("Loaded {} markdowns", markdowns.size());
    }

    @Nullable
    public List<Either<String, String>> getMarkdown(ResourceLocation location) {
        return markdowns.get(location);
    }

    public static class ObjectCollector implements Renderer {

        private final String softBreak;
        private final boolean escapeHtml;
        private final boolean percentEncodeUrls;
        private final List<HtmlNodeRendererFactory> nodeRendererFactories;
        private final ObjectWriter objectWriter = new ObjectWriter();

        public ObjectCollector() {
            this.softBreak = "\n";
            this.escapeHtml = true;
            this.percentEncodeUrls = true;
            this.nodeRendererFactories = new ArrayList<>(1);
            this.nodeRendererFactories.add(CoreHtmlNodeRenderer::new);
        }

        @Override
        public void render(Node node, Appendable output) {
            RendererContext context = new RendererContext(objectWriter);
            context.render(node);
        }

        @Override
        public String render(Node node) {
            throw new AssertionError("This is not a renderer");
        }

        public List<Either<String, String>> getNodes() {
            return objectWriter.getNodes();
        }

        private class RendererContext implements HtmlNodeRendererContext, AttributeProviderContext {

            private final HtmlWriter htmlWriter;
            private final NodeRendererMap nodeRendererMap = new NodeRendererMap();

            private RendererContext(HtmlWriter htmlWriter) {
                this.htmlWriter = htmlWriter;
                for (int i = nodeRendererFactories.size() - 1; i >= 0; i--) {
                    HtmlNodeRendererFactory nodeRendererFactory = nodeRendererFactories.get(i);
                    NodeRenderer nodeRenderer = nodeRendererFactory.create(this);
                    nodeRendererMap.add(nodeRenderer);
                }
            }

            @Override
            public boolean shouldEscapeHtml() {
                return escapeHtml;
            }

            @Override
            public boolean shouldOmitSingleParagraphP() {
                return false;
            }

            @Override
            public boolean shouldSanitizeUrls() {
                return false;
            }

            @Override
            public UrlSanitizer urlSanitizer() {
                return null;
            }

            @Override
            public String encodeUrl(String url) {
                if (percentEncodeUrls) {
                    return Escaping.percentEncodeUrl(url);
                } else {
                    return url;
                }
            }

            @Override
            public Map<String, String> extendAttributes(Node node, String tagName, Map<String, String> attributes) {
                return new LinkedHashMap<>(attributes);
            }

            @Override
            public HtmlWriter getWriter() {
                return htmlWriter;
            }

            @Override
            public String getSoftbreak() {
                return softBreak;
            }

            @Override
            public void render(Node node) {
                nodeRendererMap.render(node);
            }
        }
    }

    public static class ObjectWriter extends HtmlWriter {

        private final List<Either<String, String>> nodes = new ArrayList<>();

        public ObjectWriter() {
            super(new StringBuilder());
        }

        @Override
        public void raw(String s) {
            nodes.add(Either.right(s));
        }

        @Override
        public void text(String text) {
            nodes.add(Either.right(text));
        }

        @Override
        public void tag(String name) {
            nodes.add(Either.left(name));
        }

        @Override
        public void tag(String name, Map<String, String> attrs) {
            nodes.add(Either.left(name));
        }

        @Override
        public void tag(String name, Map<String, String> attrs, boolean voidElement) {
            tag(name, attrs);
        }

        @Override
        public void line() {
        }

        @Override
        protected void append(String s) {
            nodes.add(Either.right(s));
        }

        public List<Either<String, String>> getNodes() {
            return nodes;
        }
    }

}
