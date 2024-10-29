#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 o;
in vec3 p;

out vec4 fragColor;

void main() {
    vec2 uv = (p.xy / p.z) - o;
    fragColor = texture(Sampler0, uv + 0.5) * vertexColor * ColorModulator;
    fragColor.a *= step(max(abs(uv.x), abs(uv.y)), 0.5);
}
