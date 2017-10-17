#version 150
in vec2 inPosition;
out vec3 vertColor; // output from this shader to the next pipeline stage

uniform float time; // variable constant for all vertices in a single draw
uniform mat4 mMv;
uniform mat4 mProj;

const vec3 lightSource = vec3(-2, 2, 15);

vec3 getPositionWithZ(vec2 position);
vec3 getNormal(vec2 position);

void main() {
    vec3 position = getPositionWithZ(inPosition);
    vec4 positionMv = mMv * vec4(position, 1.0);
    vec3 lightDirection = normalize(lightSource - positionMv.xyz);
    vec3 normal = getNormal(inPosition);
    float NdotL = dot(lightDirection, normal);

    gl_Position = mProj * positionMv;
    vertColor = vec3(NdotL);
}

vec3 getPositionWithZ(vec2 position) {
    vec3 positionWithZ = vec3(position.x, position.y, cos(sqrt(position.x*position.x + position.y*position.y)));
    return positionWithZ;
}

vec3 getNormal(vec2 position) {
    const float delta = 0.01;
    vec3 du = getPositionWithZ(vec2(position.x + delta, position.y)) - getPositionWithZ(vec2(position.x - delta, position.y));
    vec3 dv = getPositionWithZ(vec2(position.x, position.y + delta)) - getPositionWithZ(vec2(position.x, position.y - delta));

    return normalize(cross(du, dv));
}