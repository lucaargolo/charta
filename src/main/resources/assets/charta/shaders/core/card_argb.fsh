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
    vec4 color = texture(Sampler0, uv + 0.5);
    color.a = min(color.a, 1.0);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * vertexColor * ColorModulator;
    fragColor.a *= step(max(abs(uv.x), abs(uv.y)), 0.5);
}
