#version 410
in vec2 texCoord;
out vec4 outColor;

uniform sampler2D[10] blurTextures; // velikost pole musí být konstantní, 10 je max
uniform int blurTextureCount;

void main() {
    vec3 baseColor = vec3(texture(blurTextures[0], texCoord).rgb);

    for (int i = 1; i < blurTextureCount; i++) {
        float frameImpactRatio = 1 / pow(2, i + 1);
        baseColor *= 1 - frameImpactRatio;
        baseColor += texture(blurTextures[i], texCoord).rgb * frameImpactRatio;
    }

	outColor = vec4(baseColor, 1.0);
} 
