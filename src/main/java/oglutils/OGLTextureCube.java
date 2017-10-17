package oglutils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.GL4;

import java.nio.Buffer;

import oglutils.OGLTexture2D;
import transforms.Mat4Scale;
import transforms.Mat4Transl;

public class OGLTextureCube implements OGLTexture {
	private final GL4 gl;
	private final Texture texture;
	public static final String[] SUFFICES_POS_NEG = { "posx", "negx", "posy", "negy", "posz", "negz" };
	public static final String[] SUFFICES_POS_NEG_FLIP_Y = { "posx", "negx", "negy", "posy", "posz", "negz" };
	public static final String[] SUFFICES_POSITIVE_NEGATIVE = { "positive_x", "negative_x", "positive_y", "negative_y", "positive_z", "negative_z" };
	public static final String[] SUFFICES_POSITIVE_NEGATIVE_FLIP_Y = { "positive_x", "negative_x", "negative_y", "positive_y", "positive_z", "negative_z" };
	public static final String[] SUFFICES_RIGHT_LEFT = { "right", "left", "bottom", "top", "front", "back" };
	public static final String[] SUFFICES_RIGHT_LEFT_FLIP_Y  = { "right", "left", "top", "bottom", "front", "back" };
	private static final int[] targets = { GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
	                                         GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
	                                         GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
	                                         GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
	                                         GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
	                                         GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z };
	
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
				"uniform samplerCube drawTexture;",
				"void main() {",
				" 	//fragColor = vec4( texCoord.xy, 0.0, 1.0);", 
				"	vec2 coord;", 
				//top
				"	if ((texCoord.y <= 1.0) &&(texCoord.y >= 2.0/3.0) && (texCoord.x >= 1.0/4.0) && (texCoord.x <= 2.0/4.0)){", 
				"		coord.y = (texCoord.y - 2.0/3.0) * 3.0 * 2.0 - 1.0;", 
				"		coord.x = (texCoord.x - 1.0/4.0) * 4.0 * 2.0 - 1.0;", 
				"		fragColor = texture(drawTexture, vec3(coord.x, -1.0, -coord.y));", 
				"	}else", 
				"	if ((texCoord.y >= 0.0) &&(texCoord.y <= 1.0/3.0) && (texCoord.x >= 1.0/4.0) && (texCoord.x <= 2.0/4.0)){", 
				"		coord.y = (texCoord.y) * 3.0 * 2.0 - 1.0;", 
				"		coord.x = (texCoord.x - 1.0/4.0) * 4.0 * 2.0 - 1.0;", 
				"		fragColor = texture(drawTexture, vec3(coord.x, 1.0, coord.y));", 
				"	}else", 
				//front
				"	if ((texCoord.y <= 2.0/3.0) && (texCoord.y >= 1.0/3.0) && (texCoord.x >= 1.0/4.0) && (texCoord.x <= 2.0/4.0)){", 
				"		coord.y = (texCoord.y - 1.0/3.0) * 3.0 * 2.0 - 1.0;", 
				"		coord.x = (texCoord.x - 1.0/4.0) * 4.0 * 2.0 - 1.0;", 
				"		fragColor = texture(drawTexture, vec3( coord.x, -coord.y, +1.0));", 
				"	}else", 
				"	if ((texCoord.y <= 2.0/3.0) && (texCoord.y >= 1.0/3.0) && (texCoord.x >= 3.0/4.0) && (texCoord.x <= 4.0/4.0)){", 
				"		coord.y = (texCoord.y - 1.0/3.0) * 3.0 * 2.0 - 1.0;", 
				"		coord.x = (texCoord.x - 3.0/4.0) * 4.0 * 2.0 - 1.0;", 
				"		fragColor = texture(drawTexture, vec3( -coord.x, -coord.y, -1.0));", 
				"	}else", 
		   	    //left
				"	if ((texCoord.y <= 2.0/3) && (texCoord.y >= 1.0/3.0) && (texCoord.x >= 0.0) && (texCoord.x <= 1.0/4.0)){", 
				"		coord.y = (texCoord.y - 1.0/3.0) * 3.0 * 2.0 - 1.0;", 
				"		coord.x = (texCoord.x ) * 4.0 * 2.0 - 1.0;", 
				"		fragColor = texture(drawTexture, vec3( -1.0, -coord.y, coord.x));", 
				"	}else", 
				"	if ((texCoord.y <= 2.0/3.0) && (texCoord.y >= 1.0/3.0) && (texCoord.x >= 1.0/2.0) && (texCoord.x <= 3.0/4.0)){", 
				"		coord.y = (texCoord.y - 1.0/3) * 3.0 * 2.0 - 1.0;", 
				"		coord.x = (texCoord.x - 2.0/4) * 4.0 * 2.0 - 1.0;", 
				"		fragColor = texture(drawTexture, vec3( +1.0, -coord.y, -coord.x));", 
				"	} else", 
				"		discard;", 
				"}" 
			};
		public Viewer(GL4 gl) {
			super(gl, ShaderUtils.loadProgram(gl, shaderVertSrc, shaderFragSrc, null, null, null, null ));
		}

		@Override
		public void view(OGLTexture texture, double posX, double posY, double scale, double aspectXY) {
				if (shaderProgram > 0){
					gl.glActiveTexture(GL4.GL_TEXTURE0);
					gl.glEnable(GL4.GL_TEXTURE_CUBE_MAP);
					gl.glUseProgram(shaderProgram);
					gl.glUniformMatrix4fv(locMat, 1, false,
							ToFloatArray.convert(new Mat4Scale(scale*aspectXY, scale,1).mul(new Mat4Transl(posX, posY, 0))), 0);
					texture.bind(shaderProgram,"drawTexture",0);
					buffers.draw(GL4.GL_QUADS, shaderProgram);
					gl.glDisable(GL4.GL_TEXTURE_CUBE_MAP);
					gl.glUseProgram(0);
				}
		}
	}
	
	public OGLTextureCube(GL4 gl, String[] fileNames) {
		this.gl = gl;
		texture = TextureIO.newTexture(GL4.GL_TEXTURE_CUBE_MAP);
		for (int i = 0; i < fileNames.length; i++) {
    		TextureData data;
			data = OGLTexture2D.readTextureDataFromFile(gl.getGLProfile(), fileNames[i]);
			texture.updateImage(gl, data, targets[i]);
		}
   }

	public OGLTextureCube(GL4 gl, String fileName, String[] suffixes) {
		this.gl = gl;
		texture = TextureIO.newTexture(GL4.GL_TEXTURE_CUBE_MAP);
		String baseName=fileName.substring(0,fileName.lastIndexOf('.'));
    	String suffix=fileName.substring(fileName.lastIndexOf('.')+1,fileName.length());
    	for (int i = 0; i < suffixes.length; i++) {
    		String fullName = new String(baseName + suffixes[i] + "." + suffix);
    		TextureData data;
			data = OGLTexture2D.readTextureDataFromFile(gl.getGLProfile(), fullName);
			texture.updateImage(gl, data, targets[i]);
		}
   }

	public Texture getTexture() { return texture; }

	@Override
	public void bind(int shaderProgram, String name, int slot) {
		if (texture == null) return;
		gl.glActiveTexture(GL4.GL_TEXTURE0 + slot);
		texture.bind(gl);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, name), slot);
	}
	
	public void setTextureBuffer(int pixelFormat, int pixelType, Buffer buffer, int indexCube) {
		texture.bind(gl);
		gl.glTexSubImage2D(targets[indexCube], 0, 0, 0, texture.getWidth(),
				texture.getHeight(), pixelFormat, pixelType, buffer);
	}

	public Buffer getTextureBuffer(int pixelFormat, int pixelType, int indexCube) {
		texture.bind(gl);
		Buffer buffer = Buffers.newDirectByteBuffer(texture.getWidth() * texture.getHeight() * 4);
		gl.glGetTexImage(targets[indexCube], 0, pixelFormat, pixelType, buffer);
		return buffer;
	}

}
