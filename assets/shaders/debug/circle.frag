#version 320 es
precision highp float;
in vec2 v_texCoord;
in vec4 v_color;
out vec4 fragColor;

void main() {
    float dist_norm = length(v_texCoord - vec2(0.5));
    if (dist_norm > 0.5) discard;
    fragColor = v_color;
}
