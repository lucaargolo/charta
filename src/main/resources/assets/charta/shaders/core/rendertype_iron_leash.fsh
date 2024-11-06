#version 150

#moj_import <fog.glsl>

uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
flat in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = linear_fog(vertexColor, vertexDistance, FogStart, FogEnd, FogColor);
    float c = (color.r + color.g + color.b)/3.0;
    fragColor = vec4(c, c, c, color.a);
}
