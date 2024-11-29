#version 150

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

vec4 paletteTexture(sampler2D Sampler0, vec2 uv) {
    float redChannel = texture(Sampler0, uv).r;

    int pixelValue = int(redChannel * 255.0);

    int colorIndex = pixelValue & 63;
    int alphaIndex = (pixelValue >> 6) & 3;

    vec3 color = COLOR_PALETTE[colorIndex];
    float alpha;
    if(colorIndex != 0 && alphaIndex == 0) {
        alpha = 2.0;
    }else{
        alpha = ALPHA_PALETTE[alphaIndex];
    }

    return vec4(color, alpha);
}


vec3 rgbToHsb(vec3 c) {
    float r = c.r, g = c.g, b = c.b;
    float maxVal = max(r, max(g, b));
    float minVal = min(r, min(g, b));
    float delta = maxVal - minVal;

    float h = 0.0;
    if (delta > 0.00001) {
        if (maxVal == r) {
            h = mod((g - b) / delta, 6.0);
        } else if (maxVal == g) {
            h = (b - r) / delta + 2.0;
        } else {
            h = (r - g) / delta + 4.0;
        }
        h /= 6.0;
    }

    float s = (maxVal > 0.00001) ? (delta / maxVal) : 0.0;
    float v = maxVal;

    return vec3(h, s, v);
}

// Function to convert HSB to RGB
vec3 hsbToRgb(vec3 c) {
    float h = c.x * 6.0;
    float s = c.y;
    float v = c.z;

    int i = int(floor(h));
    float f = h - float(i);
    float p = v * (1.0 - s);
    float q = v * (1.0 - s * f);
    float t = v * (1.0 - s * (1.0 - f));

    if (i == 0) return vec3(v, t, p);
    if (i == 1) return vec3(q, v, p);
    if (i == 2) return vec3(p, v, t);
    if (i == 3) return vec3(p, q, v);
    if (i == 4) return vec3(t, p, v);
    return vec3(v, p, q);
}