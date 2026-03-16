#version 320 es
precision highp float;

in vec2 v_texCoord;
in vec4 v_color;

out vec4 fragColor;

void main() {

    float dist = length(v_texCoord - vec2(0.5));
    if (dist > 0.5) discard;

    float r = dist / 0.5; // нормализованный радиус 0..1

    float shade = 1.0;

    // затемнение центра (20%)
    if (r < 0.2) {
        shade = 0.6; // -40%
    }

    // затемнение края (20%)
    if (r > 0.8) {
        shade = 0.7; // -30%
    }

    fragColor = vec4(v_color.rgb * shade, 1.0/*v_color.a*/);
}
