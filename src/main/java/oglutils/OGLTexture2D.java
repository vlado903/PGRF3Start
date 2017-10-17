package oglutils;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import transforms.Mat4Scale;
import transforms.Mat4Transl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;

public class OGLTexture2D implements OGLTexture {
	private final GL4 gl;
	private final Texture texture;
	
	public static class Viewer implements OGLTexture.Viewer{
		protected final GL4 gl;
		protected final int shaderProgram;
		protected final OGLBuffers buffers;
		protected final int locMat;
		
		private static final String shaderVertSrc[] = {
				"#version 330\n",
				"in vec2 inPosition;", 
				"in vec2 inTexCoord;", 
				"uniform mat4 matTrans;",
				"out vec2 texCoords;", 
				"void main() {",
				"	gl_Position = matTrans * vec4(inPosition , 0.0f, 1.0f);",
				"   texCoords = inTexCoord;",
				"}"
			};
		
		private static final String shaderFragSrc[] = { 
				"#version 330\n",
				"in vec2 texCoords;", 
				"out vec4 fragColor;", 
				"uniform sampler2D drawTexture;",
				"void main() {",
				" 	fragColor = texture(drawTexture, texCoords);", 
				"}" 
			};

		private OGLBuffers createBuffers(GL4 gl) {
			float[] vertexBufferData = { 
					0, 0, 0, 0, 
					1, 0, 1, 0, 
					1, 1, 1, 1,
					0, 1, 0, 1 };
			int[] indexBufferData = { 0, 1, 2, 3 };

			OGLBuffers.Attrib[] attributes = { new OGLBuffers.Attrib("inPosition", 2),
					new OGLBuffers.Attrib("inTexCoord", 2) };

			return new OGLBuffers(gl, vertexBufferData, attributes, indexBufferData);
		}

		public Viewer(GL4 gl) {
			this(gl, ShaderUtils.loadProgram(gl, shaderVertSrc, shaderFragSrc, null, null, null, null));
		}

		protected Viewer(GL4 gl, int shaderProgram) {
			this.gl = gl;
			buffers = createBuffers(gl);
			this. shaderProgram =  shaderProgram;
			locMat = gl.glGetUniformLocation(shaderProgram, "matTrans");
		}

		@Override
		public void view(OGLTexture texture) {
			view(texture, -1, -1);
		}

		@Override
		public void view(OGLTexture texture, double x, double y) {
			view(texture, x, y, 1.0);
		}

		@Override
		public void view(OGLTexture texture, double x, double y, double scale) {
			view(texture, x, y, scale, 1.0);
		}

		@Override
		public void view(OGLTexture texture, double posX, double posY, double scale, double aspectXY) {
			if (shaderProgram > 0) {
				gl.glActiveTexture(GL4.GL_TEXTURE0);
				gl.glEnable(GL4.GL_TEXTURE_2D);
				gl.glUseProgram(shaderProgram);
				gl.glUniformMatrix4fv(locMat, 1, false, ToFloatArray
						.convert(new Mat4Scale(scale * aspectXY, scale, 1).mul(new Mat4Transl(posX, posY, 0))), 0);
				texture.bind(shaderProgram,"drawTexture",0);
				buffers.draw(GL4.GL_QUADS, shaderProgram);
				gl.glDisable(GL4.GL_TEXTURE_2D);
				gl.glUseProgram(0);
			}
		}
	}
	
	public OGLTexture2D(GL4 gl, String fileName) {
		this(gl, TextureIO.newTexture(GL4.GL_TEXTURE_2D));
		TextureData textureData = null;
		textureData = readTextureDataFromFile(gl.getGLProfile(), fileName);
		texture.updateImage(gl, textureData);	
	}
	
	public OGLTexture2D(GL4 gl, Texture texture) {
		this.gl = gl;
		this.texture = texture;
	}

	private static String getExtension(String s) {
		String ext = "";
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
	
	static TextureData readTextureDataFromFile(GLProfile glProfile, String fileName) {
			TextureData data = null;
			System.out.print("Reading texture file" + fileName);
			try {
				InputStream is = OGLTexture2D.class.getResourceAsStream(fileName);
				data = TextureIO.newTextureData(glProfile, is, true,
						getExtension(fileName));
				is.close();
				System.out.println(" ... OK");
			} catch (IOException e) {
				System.err.println(" failed");
				System.err.println(e.getMessage());
			}
			return data;
	}
	
	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLTexture2D(GL4 gl, OGLTexImageType image) {
		this.gl = gl;
		Buffer buffer = image.getDataBuffer();
		int[] textureID = new int[1];
		gl.glGenTextures(1, textureID, 0);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, textureID[0]);
		gl.glTexImage2D(GL4.GL_TEXTURE_2D, 0, image.getFormat().getInternalFormat(), image.getWidth(),
				image.getHeight(), 0, image.getFormat().getPixelFormat(), image.getFormat().getPixelType(), buffer);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		texture = new Texture(textureID[0], GL4.GL_TEXTURE_2D, image.getWidth(), image.getHeight(), image.getWidth(),
				image.getHeight(), false);
	}
	
	public Texture getTexture() { return texture; }

	public void save(String fileName) {
		try {
			System.out.print("Saving texture " + fileName );
			TextureIO.write(texture, new File(fileName));
			System.out.println(" ... OK");
		} catch (GLException | IOException e) {
			System.err.println("failed");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> void setTextureBuffer(
			OGLTexImage.Format<OGLTexImageType> format, Buffer buffer) {
		texture.bind(gl);
		gl.glTexSubImage2D(GL4.GL_TEXTURE_2D, 0, 0, 0, texture.getWidth(), texture.getHeight(), format.getPixelFormat(),
				format.getPixelType(), buffer);
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> Buffer getTextureBuffer(
			OGLTexImage.Format<OGLTexImageType> format) {
		texture.bind(gl);
		Buffer buffer = format.newBuffer(texture.getWidth(), texture.getHeight());
		gl.glGetTexImage(GL4.GL_TEXTURE_2D, 0, format.getPixelFormat(), format.getPixelType(), buffer);
		return buffer;
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> void setTexImage(OGLTexImageType image) {
		setTextureBuffer(image.getFormat(), image.getDataBuffer());
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLTexImageType getTexImage(
			OGLTexImage.Format<OGLTexImageType> format) {
		OGLTexImageType image = format.newTexImage(texture.getWidth(), texture.getHeight());
		image.setDataBuffer(getTextureBuffer(format));
		return image;
	}

	@Override
	public void bind(int shaderProgram, String name, int slot) {
		if (texture == null)
			return;
		gl.glActiveTexture(GL4.GL_TEXTURE0 + slot);
		texture.bind(gl);
		gl.glGetUniformLocation(shaderProgram, name);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, name), slot);
	}

	public BufferedImage toBufferedImage() {
		int[] array = new int[texture.getWidth() * texture.getHeight()]; // buffer.array();
		texture.bind(gl);
		gl.glGetTexImage(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGBA, GL4.GL_UNSIGNED_BYTE, IntBuffer.wrap(array));
		BufferedImage image = new BufferedImage(texture.getWidth(), texture.getHeight(), BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, texture.getWidth(), texture.getHeight(), array, 0, texture.getWidth());
		return image;
	}

	public void fromBufferedImage(BufferedImage img) {
		texture.bind(gl);
		int[] array = new int[texture.getWidth() * texture.getHeight()];
		img.getRGB(0, 0, texture.getWidth(), texture.getHeight(), array, 0, texture.getWidth());
		gl.glTexSubImage2D(GL4.GL_TEXTURE_2D, 0, 0, 0, texture.getWidth(), texture.getHeight(), GL4.GL_RGBA,
				GL4.GL_UNSIGNED_INT_8_8_8_8_REV, IntBuffer.wrap(array));
	}


	

}
