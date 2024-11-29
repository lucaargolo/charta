#version 150

#moj_import <charta:palette.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = paletteTexture(Sampler0, texCoord0) * vertexColor;
    bool weird = false;
    if (color.a < 2.0) {
        weird = true;
        color.a = 1.0;
    }
    color.a = min(color.a, 1.0);
    fragColor = color * ColorModulator;
    if(fragColor.a < 0.1) {
        discard;
    }
    if(weird) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}
