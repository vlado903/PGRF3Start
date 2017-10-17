package oglutils;

import java.nio.Buffer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GL4;

import transforms.Mat4Scale;
import transforms.Mat4Transl;

public class OGLTextureVolume implements OGLTexture {
	private final GL4 gl;
	private int[] volumeTextureID;
	private final int width, height, depth;
	
	public static class Viewer extends OGLTexture2D.Viewer {
		private static final String shaderVertSrc[] = {
				"#version 330\n",
				"in vec2 inPosition;", 
				"in vec2 inTexCoord;", 
				"uniform mat4 matTrans;",
				"out vec2 texCoord;", 
				"void main() {",
				"	gl_Position = matTrans * vec4(inPosition , 0.0f, 1.0f);",
				"   texCoord = inTexCoord;",
				"}"
			};
		
		private static final String shaderFragSrc[] = { 
				"#version 330\n",
				"in vec2 texCoord;", 
				"out vec4 fragColor;", 
				"uniform sampler3D drawTexture;",
				"void main() {",
				"	vec3 coord;", 
				"	int row = 4;",
				"	int column = 4;",
				"	int i = int(texCoord.x * column);",
				"	int j = int(texCoord.y * row);",
				"	coord.x = (texCoord.x * column) - i;",
				"	coord.y = (texCoord.y * row) - j;",
				"	coord.z = (i + j*column)/float(row*column);",
				"	//fragColor = vec4( coord.xyz, 1.0);",
				"	fragColor = texture(drawTexture, coord);",
				"}" 
			};

		public Viewer(GL4 gl) {
			super(gl, ShaderUtils.loadProgram(gl, shaderVertSrc, shaderFragSrc, null, null, null, null ));
		}

		@Override
		public void view(OGLTexture texture, double posX, double posY, double scale, double aspectXY) {
				if (shaderProgram > 0){
					gl.glActiveTexture(GL4.GL_TEXTURE0);
					gl.glEnable(GL4.GL_TEXTURE_3D);
					gl.glUseProgram(shaderProgram);
					gl.glUniformMatrix4fv(locMat, 1, false,
							ToFloatArray.convert(new Mat4Scale(scale*aspectXY, scale,1).mul(new Mat4Transl(posX, posY, 0))), 0);
					
					texture.bind(shaderProgram,"drawTexture",0);
					buffers.draw(GL4.GL_QUADS, shaderProgram);
					gl.glDisable(GL4.GL_TEXTURE_3D);
					gl.glUseProgram(0);
				}
		}
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLTextureVolume(GL4 gl, OGLTexImageType volume) {
		this.gl = gl;
		this.width = volume.getWidth();
		this.height = volume.getHeight();
		this.depth = volume.getDepth();
		Buffer buffer = volume.getDataBuffer();
		volumeTextureID = new int[1];
		gl.glGenTextures(1, volumeTextureID, 0);
		gl.glBindTexture(GL4.GL_TEXTURE_3D, volumeTextureID[0]);
		gl.glPixelStorei(GL4.GL_UNPACK_ALIGNMENT, 1);

		gl.glTexImage3D(GL4.GL_TEXTURE_3D, 0,
				volume.getFormat().getInternalFormat(),
				volume.getWidth(), volume.getHeight(), volume.getDepth(),
				0, volume.getFormat().getPixelFormat(),
				volume.getFormat().getPixelType(), buffer);
		gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
		gl.glTexParameteri(GL4.GL_TEXTURE_3D, GL4.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP);
	}


	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> void setTextureBuffer(
			OGLTexImage.Format<OGLTexImageType> format, Buffer buffer) {
		gl.glBindTexture(GL4.GL_TEXTURE_3D, volumeTextureID[0]);
		gl.glTexSubImage3D(GL4.GL_TEXTURE_3D, 0, 0, 0, 0,
				width, height, depth, 
				format.getPixelFormat(), format.getPixelType(), buffer);
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> void setTexImage(OGLTexImageType volume) {
		setTextureBuffer(volume.getFormat(), volume.getDataBuffer());
	}

	@Override
	public void bind(int shaderProgram, String name, int slot) {
		gl.glActiveTexture(GL4.GL_TEXTURE0 + slot);
		gl.glBindTexture(GL4.GL_TEXTURE_3D, volumeTextureID[0]);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, name), slot);
	}
}
