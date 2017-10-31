#version 150
in vec3 normal; // input from the previous pipeline stage
in vec3 lightDirection; // input from the previous pipeline stage

out vec4 outColor; // output from the fragment shader

const float ambientLight = 0.05;

void main() {
    float intensity = dot(normalize(lightDirection), normalize(normal));

	outColor = vec4(vec3(min(intensity + ambientLight, 1)), 1.0);
} 
