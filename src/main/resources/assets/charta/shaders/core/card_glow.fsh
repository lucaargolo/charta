// This shader "fakes" a 3D-camera perspective on 2D quads.
// Adapted from: https://godotshaders.com/shader/2d-perspective/

#version 150

#moj_import <charta:palette.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 o;
in vec3 p;

out vec4 fragColor;

void main() {
    vec2 uv = (p.xy / p.z) - o;
    vec4 color = paletteTexture(Sampler0, uv + 0.5);
    bool weird = false;
    if (color.a < 2.0) {
        weird = true;
        color.a = 1.0;
    }
    color.a = min(color.a, 1.0);
    fragColor = color * vertexColor * ColorModulator;
    fragColor.a *= step(max(abs(uv.x), abs(uv.y)), 0.5);
    if(fragColor.a < 0.1) {
        discard;
    }
    if(weird) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}
