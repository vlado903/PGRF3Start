package oglutils;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;

import static com.jogamp.opengl.GL.GL_EXTENSIONS;


public class OGLUtils {

	/**
	 * Print version, vendor and extensions of current OpenGL
	 * 
	 * @param gl
	 * 			OpenGL context
	 */
	public static void printOGLparameters(GL4 gl) {
		if (gl == null)
			return;
		System.out.println("GL class: " + gl.getClass().getName());
		System.out.println("GL vendor: " + gl.glGetString(GL4.GL_VENDOR));
		System.out.println("GL renderer: " + gl.glGetString(GL4.GL_RENDERER));
		System.out.println("GL version: " + gl.glGetString(GL4.GL_VERSION) +
				" (#version " + getVersion(gl) + ")" );
		System.out.println("GL shading language version: " + gl.glGetString(GL4.GL_SHADING_LANGUAGE_VERSION));
		System.out.print("GL extensions: " + getExtensions(gl));
	}

	/**
	 * Get extensions of current OpenGL
	 * 
	 */
	public static String getExtensions(GL4 gl) {
		String extensions;
		if (getVersion(gl) < 300){
			extensions = gl.glGetString(GL_EXTENSIONS);  //Deprecated in newer versions
		}else {
			int[] numberExtensions = new int[1];
			gl.glGetIntegerv(GL4.GL_NUM_EXTENSIONS, numberExtensions, 0);
			extensions = gl.glGetStringi(GL_EXTENSIONS , 1);
			for(int i = 1; i < numberExtensions[0]; i++){
				extensions = extensions + " " + gl.glGetStringi(GL_EXTENSIONS , i);
			}
		}
		return extensions;
	}
	
	/**
	 * Get version of current OpenGL
	 * 
	 */
	public static int getVersion(GL4 gl) {
		String version = new String(gl.glGetString(GL4.GL_VERSION));
		String[] parts = version.split(" |\\.");
		int versionNumber = Integer.parseInt(parts[0])*100
			+ Integer.parseInt(parts[1])*10;
		return versionNumber;
	}
	
	/**
	 * Print parameters of current JOGL
	 * 
	 */
	public static void printJOGLparameters() {
		Package p = Package.getPackage("com.jogamp.opengl");
		System.out.println("JOGL specification version: " + p.getSpecificationVersion());
		System.out.println("JOGL implementation version: " + p.getImplementationVersion());
		System.out.println("JOGL implementation title: " + p.getImplementationTitle());
		System.out.println("JOGL implementation vendor: " + p.getImplementationVendor());
	}
	
	/**
	 * Print parameters of current JAVA
	 * 
	 */
	public static void printJAVAparameters() {
		System.out.println("Java version: " + System.getProperty("java.version"));
		System.out.println("Java vendor: " + System.getProperty("java.vendor"));
	}

	/**
	 * Check OpenGL shaders support 
	 * 
	 * @param gl
	 */
	public static void shaderCheck(GL4 gl){
		String extensions = gl.glGetString(GL_EXTENSIONS);
		if (extensions.indexOf("GL_ARB_vertex_shader") == -1
			|| extensions.indexOf("GL_ARB_fragment_shader") == -1) {
	//		throw new RuntimeException("Shaders are not available.");
		}
		System.out.println("This OpenGL version supports vertex and fragment shader");
	}
	
	/**
	 * Push current OpenGL settings and set default shader program
	 * 
	 * @param glDrawable
	 * 
	 * @return shader program
	 */
	public static int pushAll(GLAutoDrawable glDrawable) {
		if (glDrawable == null)
			return 0;
		return pushAll(glDrawable.getGL().getGL2());
	}

	/**
	 * Push current OpenGL settings and set default shader program
	 * 
	 * @return shader program
	 */
	public static int pushAll(GL2 gl) {
		if (gl == null)
			return 0;
		// push all parameters
		int[] shaderProgram = new int[1];
		gl.glGetIntegerv(GL4.GL_CURRENT_PROGRAM, shaderProgram, 0);
		gl.glUseProgram(0);
		gl.glPushAttrib(GL2.GL_ENABLE_BIT |
				GL2.GL_DEPTH_BUFFER_BIT |
				GL2.GL_VIEWPORT_BIT |
				GL2.GL_TEXTURE_BIT |
				GL2.GL_COLOR_BUFFER_BIT |
				GL2.GL_TRANSFORM_BIT |
				GL2.GL_POLYGON_BIT);
		gl.glDisable(GL4.GL_DEPTH_TEST);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl.glDepthMask(false);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		return shaderProgram[0];
	}

	/**
	 * Pop OpenGL settings set shader program
	 * 
	 * @param glDrawable
	 * @param shaderProgram
	 *            shader program to set
	 */
	public static void popAll(GLAutoDrawable glDrawable, int shaderProgram) {
		if (glDrawable == null)
			return;
		popAll(glDrawable.getGL().getGL2(), shaderProgram);
	}
	/**
	 * Pop OpenGL settings set shader program
	 * 
	 * @param shaderProgram
	 *            shader program to set
	 */
	public static void popAll(GL2 gl, int shaderProgram) {
		if (gl == null)
			return;
		// pop all parameters
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glPopAttrib();
		gl.glUseProgram(shaderProgram);
	}
	
	/**
	 * Check GL error
	 * 
	 * @param glDrawable
	 * @param longReport
	 *            type of report
	 */
	static public void checkGLError(GLAutoDrawable glDrawable, String text, boolean longReport) {
		checkGLError(glDrawable.getGL().getGL4(), text, longReport);
	}

	/**
	 * Check GL error
	 * 
	 * @param gl
	 * @param longReport
	 *            type of report
	 */
	static public void checkGLError(GL4 gl, String text, boolean longReport) {
		int err = gl.glGetError();
		String errorName, errorDesc;
		
		while (err != GL4.GL_NO_ERROR) {

			switch (err) {
			case GL4.GL_INVALID_ENUM:
				errorName = "GL_INVALID_ENUM";
				errorDesc = "An unacceptable value is specified for an enumerated argument. The offending command is ignored and has no other side effect than to set the error flag.";
				break;

			case GL4.GL_INVALID_VALUE:
				errorName = "GL_INVALID_VALUE";
				errorDesc = "A numeric argument is out of range. The offending command is ignored and has no other side effect than to set the error flag.";
				break;

			case GL4.GL_INVALID_OPERATION:
				errorName = "GL_INVALID_OPERATION";
				errorDesc = "The specified operation is not allowed in the current state. The offending command is ignored and has no other side effect than to set the error flag.";
				break;
			case GL4.GL_INVALID_FRAMEBUFFER_OPERATION:
				errorName = "GL_INVALID_FRAMEBUFFER_OPERATION";
				errorDesc = "The framebuffer object is not complete. The offending command is ignored and has no other side effect than to set the error flag.";
				break;
			case GL4.GL_OUT_OF_MEMORY:
				errorName = "GL_OUT_OF_MEMORY";
				errorDesc = "There is not enough memory left to execute the command. The state of the GL is undefined, except for the state of the error flags, after this error is recorded.";
				break;
			default:
				return;
			}
			if (longReport)
				System.err.println(text + " GL error: " + err + " " + errorName + ": " + errorDesc);
			else
				System.err.println(text + " GL error: " + errorName);
			err = gl.glGetError();
		}
		
	}

	/**
	 * Check GL error
	 * 
	 * @param glDrawable
	 */
	static public void checkGLError(GLAutoDrawable glDrawable, String text) {
		checkGLError(glDrawable, text, false);
	}
	
	static public void checkGLError(GL4 gl, String text) {
		checkGLError(gl, text, false);
	}
	/**
	 * Check GL error
	 * 
	 * @param glDrawable
	 */
	static public void checkGLError(GLAutoDrawable glDrawable) {
		checkGLError(glDrawable, "", false);
	}

	static public void checkGLError(GL4 gl) {
		checkGLError(gl, "", false);
	}

}
