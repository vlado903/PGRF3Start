#version 410
in vec3 normal;
in vec3 lightDirection;
in vec3 spotlightDirection;
in vec3 viewDirection;
in vec2 texCoord;
in vec3 position;
in float distance;

in vec3 diffuseTotalVert;
in vec3 specularTotalVert;

uniform vec3 eyePos;
uniform bool computeLightInFS;
uniform bool spotlight;
uniform bool normalTexture;
uniform int colorMode;
uniform sampler2D chosenTexture;
uniform sampler2D chosenTextureNormal;
uniform sampler2D chosenTextureHeight;

out vec4 outColor;

const float ambientLight = 0.3;
const float diffuseLight = 0.5;
const float specularLight = 0.3;
const vec2 heightFactor = vec2(0.04, -0.02);

const float constantAttenuation = 0.05;
const float linearAttenuation = 0.025;
const float quadraticAttenuation = 0.0025;

const float spotlightConeAngle = 0.5; //28.65 deg
const vec3 spotlightAngleDirection = vec3(0.0, 0.0, -3.0);

const int COLOR_MODE_POSITION_ONLY = 0;
const int COLOR_MODE_NORMAL = 1;
const int COLOR_MODE_TEXTURE_BASIC = 2;

void main() {
    vec3 finalNormal;
    if (normalTexture) {
        finalNormal = normalize(texture(chosenTextureNormal, texCoord).xyz * 2.0 - 1.0);
    } else {
        finalNormal = normalize(normal);
    }

    vec3 baseColor;
    // použití switche způsobuje artefakty
    if (colorMode == COLOR_MODE_POSITION_ONLY) {
        baseColor = normalize(position);
    } else if (colorMode == COLOR_MODE_NORMAL) {
        baseColor = finalNormal;
    } else if (colorMode == COLOR_MODE_TEXTURE_BASIC) {
        baseColor = texture(chosenTexture, texCoord).rgb;
    } else { // výpočet offsetu pro parallax podle height textury
        float height = texture(chosenTextureHeight, texCoord).r;
        height = height * heightFactor.x + heightFactor.y;

        vec2 offset = normalize(eyePos).xz * height;
        vec2 texCoord = texCoord + normalize(viewDirection).xy * offset;

        baseColor = texture(chosenTexture, texCoord).rgb;
    }

    vec3 ambientTotal = ambientLight * baseColor;
    vec3 diffuseTotal, specularTotal;
    if (computeLightInFS) {
        float NdotL = max(dot(normalize(lightDirection), finalNormal), 0);
        diffuseTotal = NdotL * diffuseLight * baseColor;

        vec3 halfVector = normalize(lightDirection + viewDirection);
        float NdotHV = max(dot(finalNormal, halfVector), 0);
        specularTotal = vec3(pow(NdotHV, 50) * specularLight);
    } else {
        diffuseTotal = diffuseTotalVert;
        specularTotal = specularTotalVert;
    }

    float attenuation;
    if (spotlight) {
        float theta = acos(dot(normalize(-spotlightDirection), normalize(spotlightAngleDirection)));
        if (theta > spotlightConeAngle) { // není nasvícen
            attenuation = 0.0;
        } else {
    	    attenuation = 1.0/(constantAttenuation + (linearAttenuation * distance) + quadraticAttenuation * distance * distance);
    	}
    } else {
        attenuation = 1.0/(constantAttenuation + (linearAttenuation * distance) + quadraticAttenuation * distance * distance);
    }

    outColor = vec4(ambientTotal + attenuation * (diffuseTotal + specularTotal), 1.0);
}
