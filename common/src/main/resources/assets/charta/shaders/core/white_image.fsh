#version 150

#moj_import <charta:palette.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = paletteTexture(Sampler0, texCoord0);
    color.r = color.g = color.b = 1.0f;
    color *= vertexColor;
    color.a = min(color.a, 1.0);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;
}
