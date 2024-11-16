// This shader "fakes" a 3D-camera perspective on 2D quads.
// Adapted from: https://godotshaders.com/shader/2d-perspective/

#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform float Fov;
uniform float YRot;
uniform float XRot;
uniform float InSet;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 o;
out vec3 p;

void main() {
    // Apply rotation
    float sin_b = sin(YRot / 180.0 * 3.14159);
    float cos_b = cos(YRot / 180.0 * 3.14159);
    float sin_c = sin(XRot / 180.0 * 3.14159);
    float cos_c = cos(XRot / 180.0 * 3.14159);

    mat3 inv_rot_mat;
    inv_rot_mat[0][0] = cos_b;
    inv_rot_mat[0][1] = 0.0;
    inv_rot_mat[0][2] = -sin_b;

    inv_rot_mat[1][0] = sin_b * sin_c;
    inv_rot_mat[1][1] = cos_c;
    inv_rot_mat[1][2] = cos_b * sin_c;

    inv_rot_mat[2][0] = sin_b * cos_c;
    inv_rot_mat[2][1] = -sin_c;
    inv_rot_mat[2][2] = cos_b * cos_c;

    // FOV and perspective calculations
    float t = tan(Fov / 360.0 * 3.14159);
    p = inv_rot_mat * vec3((UV0 - 0.5), 0.5 / t);
    float v = (0.5 / t) + 0.5;
    p.xy *= v * inv_rot_mat[2].z;
    o = v * inv_rot_mat[2].xy;

    // Adjust vertex position for inset
    vec2 adjustedPos = Position.xy + (UV0 - 0.5) * t * (1.0 - InSet);

    // Apply transformations
    gl_Position = ProjMat * ModelViewMat * vec4(adjustedPos, Position.z, 1.0);

    vertexColor = Color;
}
