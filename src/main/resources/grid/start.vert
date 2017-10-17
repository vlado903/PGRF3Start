#version 150
in vec2 inPosition;

out vec3 vertColor; // output from this shader to the next pipeline stage
uniform float time; // variable constant for all vertices in a single draw
uniform mat4 mat; // variable constant for all vertices in a single draw

void main() {
	vec2 position = inPosition;

	float z = cos(sqrt(position.x *position.x + position.y *position.y));
	gl_Position = mat * vec4(position, z, 1.0);

	vertColor = vec3(inPosition, 0.5);
} 
