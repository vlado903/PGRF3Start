package oglutils;

import java.awt.Font;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class OGLTextRenderer {
    private final TextRenderer renderer;


    public OGLTextRenderer(){
        renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 12));
    }

    /**
     * Draw string on 2D coordinates of the raster frame
     *
     * @param glDrawable
     * @param x
     *            x position of string in range <0, width> of raster frame
     * @param y
     *            y position of string in range <0, height> of raster frame
     * @param s
     *            string to draw
     */
    public void drawStr2D(GLAutoDrawable glDrawable, float x, float y,
                          String s) {
        if (glDrawable == null )
            return;
        GL2 gl = glDrawable.getGL().getGL2();

        if (renderer == null || s == null)
            return;

        // push all parameters
        int shaderProgram = OGLUtils.pushAll(glDrawable);
        // setup drawing environment
        gl.glDisable(GLLightingFunc.GL_LIGHTING);
        gl.glDisable(GLLightingFunc.GL_COLOR_MATERIAL);

        gl.glMaterialfv(GL2.GL_FRONT, GLLightingFunc.GL_EMISSION, new float[] { 0f, 0f, 1f, 1f }, 0);

        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glFrontFace(GL2.GL_CCW);
        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE);

        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        gl.glViewport(0, 0, glDrawable.getSurfaceWidth(),
                glDrawable.getSurfaceHeight());

        renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        renderer.beginRendering(glDrawable.getSurfaceWidth(),
                glDrawable.getSurfaceHeight());
        renderer.draw(s, (int) x, (int) y);

        renderer.endRendering();

        //renderer.dispose();

        // pop all parameters
        OGLUtils.popAll(glDrawable, shaderProgram);
    }

    /**
     * Draw string on 3D coordinates of the raster screen using GLUT library
     *
     * @param glDrawable
     * @param x
     * @param y
     * @param z
     * @param s
     */
    public static void drawStrGLUT(GLAutoDrawable glDrawable, float x, float y,
                                   float z, String s) {
        if (glDrawable == null)
            return;
        GL2 gl = glDrawable.getGL().getGL2();
        GLUT glut = new GLUT();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glRasterPos3f(x, y, z);
        glut.glutBitmapString(GLUT.BITMAP_8_BY_13, s);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
    }

}
