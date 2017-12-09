#version 410
in vec2 inPosition;

out vec3 normal;
out vec3 lightDirection;
out vec3 spotlightDirection;
out vec3 viewDirection;
out vec2 texCoord;
out vec3 position;
out float distance;

out vec3 diffuseTotalVert;
out vec3 specularTotalVert;
const float diffuseLight = 0.5;
const float specularLight = 0.3;

uniform mat4 mMv;
uniform mat4 mProj;
uniform vec3 eyePos;
uniform float time;
uniform int function;
uniform bool computeLightInFS;
uniform bool spotlight;
uniform bool normalTexture;
uniform int colorMode;

vec3 lightSource = vec3(-2, 2, 15);
vec3 spotlightSource = vec3(-2, 2, 15);

const float PI = 3.1415;

vec3 getCartesian(vec2 position) {
    if (function == 0) {
        return 1.5 * vec3(position.x, position.y, 0.8);
    } else {
        return 1.3 * vec3(position.x * 2 - 1, position.y * 2 - 1, 0.3 * sin(position.x + position.y + time / 4) + 0.7);
    }
}

vec3 getSpherical(vec2 position) {
	float azimuth = position.x * 2.0 * PI;
	float zenith = position.y * PI - PI/2.0;
    float r;

    if (function == 2) {
        r = 1;
        azimuth += time / 20;
    } else {
        r = cos(4 * azimuth);

    }

	vec3 positionWithZ;
	positionWithZ.x= cos(azimuth) * cos(zenith);
	positionWithZ.y= r * sin(azimuth) * cos(zenith);
	positionWithZ.z= r * sin(zenith);

	return positionWithZ;
}

vec3 getCylindrical(vec2 position) {
    float s = position.x * PI * 2.0;
    float t = position.y * PI * 2.0;
    float r;

    if (function == 4) {
        r = 2.0 + cos(2.0 * t);
    } else {
        r = 2.0 + cos(2.0 * t * (sin(time/8))) * sin(s);
    }

    vec3 positionWithZ;
    positionWithZ.x = cos(s) * r;
    positionWithZ.y = sin(s) * r;
    positionWithZ.z = t - 2;
    return positionWithZ / 4.0;
}

vec3 getPositionWithZ(vec2 position) {
    vec3 positionWithZ;
    if (function < 2) {
        positionWithZ = getCartesian(position);
    } else if (function < 4) {
        positionWithZ = getSpherical(position);
    } else {
        positionWithZ = getCylindrical(position);
    }
    return positionWithZ;
}

vec3 getNormal(vec2 position) {
    const float delta = 0.01;
    vec3 du = getPositionWithZ(vec2(position.x + delta, position.y)) - getPositionWithZ(vec2(position.x - delta, position.y));
    vec3 dv = getPositionWithZ(vec2(position.x, position.y + delta)) - getPositionWithZ(vec2(position.x, position.y - delta));

    return normalize(cross(du, dv));
}

vec3 getTangent(vec2 position) {
    const float delta = 0.01;
    vec3 du = getPositionWithZ(vec2(position.x + delta, position.y)) - getPositionWithZ(vec2(position.x - delta, position.y));
    return normalize(du);
}

void main() {
    position = getPositionWithZ(inPosition);
    vec4 positionMv = mMv * vec4(position, 1.0);
    lightSource = (mMv * vec4(lightSource, 1.0)).xyz;
    spotlightSource = (mMv * vec4(spotlightSource, 1.0)).xyz;

    viewDirection = eyePos - positionMv.xyz;
    lightDirection = lightSource - positionMv.xyz;
    spotlightDirection = spotlightSource - positionMv.xyz;

    if (normalTexture) {
        mat3 normalMatrix = inverse(transpose(mat3(mMv)));
        normal = normalize(normalMatrix * getNormal(inPosition));
        vec3 tangent = normalize(normalMatrix * getTangent(inPosition));
        vec3 binormal = normalize(cross(normal,tangent));

        mat3 matTBN = mat3(tangent, binormal, normal);

        viewDirection = normalize(viewDirection * matTBN);
        lightDirection = normalize(lightDirection * matTBN);
        spotlightDirection = normalize(spotlightDirection * matTBN);
    } else {
        normal = getNormal(inPosition);
    }

    gl_Position = mProj * positionMv;

    if (!computeLightInFS) {
        float NdotL = max(dot(normalize(lightDirection), normal), 0);
        diffuseTotalVert = vec3(NdotL * diffuseLight);

        vec3 halfVector = normalize(normalize(lightDirection) + normalize(viewDirection));
        float NdotHV = max(dot(normal, halfVector), 0);
        specularTotalVert = vec3(pow(NdotHV, 50) * specularLight);
    }

    distance = length(lightSource);
    texCoord = inPosition;
}