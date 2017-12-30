#version 410
in vec2 inPosition;
out vec2 texCoord;

void main() {
	texCoord = inPosition/2.0 + 0.5;
	gl_Position = vec4(inPosition, 0, 1.0);
}
