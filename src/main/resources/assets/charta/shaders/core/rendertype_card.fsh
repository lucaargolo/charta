// This shader "fakes" a 3D-camera perspective on 2D quads.
// Adapted from: https://godotshaders.com/shader/2d-perspective/

#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 o;
in vec3 p;

const vec3 COLOR_PALETTE[64] = vec3[](
    vec3(0.0, 0.0, 0.0), vec3(0.145, 0.145, 0.145), vec3(0.286, 0.286, 0.286), vec3(0.431, 0.431, 0.431),
    vec3(0.573, 0.573, 0.573), vec3(0.718, 0.718, 0.718), vec3(0.863, 0.863, 0.863), vec3(1.0, 1.0, 1.0),
    vec3(0.498, 0.0, 0.0), vec3(0.698, 0.0, 0.0), vec3(0.89, 0.0, 0.0), vec3(1.0, 0.0, 0.0),
    vec3(1.0, 0.325, 0.325), vec3(1.0, 0.459, 0.459), vec3(1.0, 0.596, 0.596), vec3(1.0, 0.729, 0.729),
    vec3(0.498, 0.247, 0.0), vec3(0.698, 0.345, 0.0), vec3(0.89, 0.439, 0.0), vec3(1.0, 0.498, 0.0),
    vec3(1.0, 0.663, 0.325), vec3(1.0, 0.729, 0.459), vec3(1.0, 0.796, 0.596), vec3(1.0, 0.863, 0.729),
    vec3(0.498, 0.498, 0.0), vec3(0.698, 0.698, 0.0), vec3(0.89, 0.89, 0.0), vec3(1.0, 1.0, 0.0),
    vec3(1.0, 1.0, 0.325), vec3(1.0, 1.0, 0.459), vec3(1.0, 1.0, 0.596), vec3(1.0, 1.0, 0.729),
    vec3(0.0, 0.498, 0.0), vec3(0.0, 0.698, 0.0), vec3(0.0, 0.89, 0.0), vec3(0.0, 1.0, 0.0),
    vec3(0.325, 1.0, 0.325), vec3(0.459, 1.0, 0.459), vec3(0.596, 1.0, 0.596), vec3(0.729, 1.0, 0.729),
    vec3(0.0, 0.498, 0.498), vec3(0.0, 0.698, 0.698), vec3(0.0, 0.89, 0.89), vec3(0.0, 1.0, 1.0),
    vec3(0.325, 1.0, 1.0), vec3(0.459, 1.0, 1.0), vec3(0.596, 1.0, 1.0), vec3(0.729, 1.0, 1.0),
    vec3(0.0, 0.0, 0.498), vec3(0.0, 0.0, 0.698), vec3(0.0, 0.0, 0.89), vec3(0.0, 0.0, 1.0),
    vec3(0.325, 0.325, 1.0), vec3(0.459, 0.459, 1.0), vec3(0.596, 0.596, 1.0), vec3(0.729, 0.729, 1.0),
    vec3(0.498, 0.0, 0.498), vec3(0.698, 0.0, 0.698), vec3(0.89, 0.0, 0.89), vec3(1.0, 0.0, 1.0),
    vec3(1.0, 0.325, 1.0), vec3(1.0, 0.459, 1.0), vec3(1.0, 0.596, 1.0), vec3(1.0, 0.729, 1.0)
);

const float ALPHA_PALETTE[4] = float[](0.0, 0.333, 0.667, 1.0);

out vec4 fragColor;

void main() {
    vec2 uv = (p.xy / p.z) - o;

    float redChannel = texture(Sampler0, uv + 0.5).r;

    int pixelValue = int(redChannel * 255.0);

    int colorIndex = pixelValue & 63;
    int transparencyIndex = (pixelValue >> 6) & 3;

    vec3 color = COLOR_PALETTE[colorIndex];
    float alpha = ALPHA_PALETTE[transparencyIndex];

    fragColor = vec4(color, alpha) * vertexColor * ColorModulator;
    fragColor.a *= step(max(abs(uv.x), abs(uv.y)), 0.5);
}
