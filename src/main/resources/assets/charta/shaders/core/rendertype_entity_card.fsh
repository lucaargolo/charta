#version 150

#moj_import <fog.glsl>
#moj_import <charta:palette.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = paletteTexture(Sampler0, texCoord0);
    bool glow = false;
    if(color.a > 1.0) {
        glow = true;
        color.a = 1.0;
    }
    if (color.a < 0.1) {
        discard;
    }
    if(!glow) {
        color *= vertexColor * ColorModulator;
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
        color *= lightMapColor;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
