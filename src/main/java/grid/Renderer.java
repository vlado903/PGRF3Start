package grid;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import oglutils.OGLBuffers;
import oglutils.OGLUtils;
import oglutils.ShaderUtils;
import oglutils.ToFloatArray;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * GLSL sample:<br/>
 * Read and compile shader from files "/shader/glsl01/start.*" using ShaderUtils
 * class in oglutils package (older GLSL syntax can be seen in
 * "/shader/glsl01/startForOlderGLSL")<br/>
 * Manage (create, bind, draw) vertex and index buffers using OGLBuffers class
 * in oglutils package<br/>
 * Requires JOGL 2.3.0 or newer
 *
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2015-09-05
 */
public class Renderer implements GLEventListener, MouseListener,
        MouseMotionListener, KeyListener {

    int width, height;
    int oldX, oldY;
    int shaderProgram, locTime, locMv, locProj;
    float time = 0;

    Camera cam = new Camera();
    OGLBuffers buffers;

    Mat4 proj;

    @Override
    public void init(GLAutoDrawable glDrawable) {
        // check whether shaders are supported
        GL4 gl = glDrawable.getGL().getGL4();
        OGLUtils.shaderCheck(gl);
        OGLUtils.printOGLparameters(gl);

        shaderProgram = ShaderUtils.loadProgram(gl,
                "/grid/shade.vert",
                "/grid/shade.frag",
                null,
                null,
                null,
                null);

        locMv = gl.glGetUniformLocation(shaderProgram, "mMv");
        locProj = gl.glGetUniformLocation(shaderProgram, "mProj");
        locTime = gl.glGetUniformLocation(shaderProgram, "time");

        cam = cam.withPosition(new Vec3D(5, 5, 2.5))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125);

        buffers = GridFactory.gridGenerate(gl, 30, 30);
    }


    @Override
    public void display(GLAutoDrawable glDrawable) {
        GL4 gl = glDrawable.getGL().getGL4();

        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

        // set the current shader to be used, could have been done only once (in
        // init) in this sample (only one shader used)
        gl.glUseProgram(shaderProgram);
        time += 0.1;

        gl.glUniform1f(locTime, time); // correct shader must be set before this
        gl.glUniformMatrix4fv(locMv, 1, false, ToFloatArray.convert(cam.getViewMatrix()), 0);
        gl.glUniformMatrix4fv(locProj, 1, false, ToFloatArray.convert(proj), 0);

        gl.glEnable(GL4.GL_DEPTH_TEST);

        gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_FILL);

        // bind and draw
        buffers.draw(GL4.GL_TRIANGLES, shaderProgram);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
                        int height) {
        this.width = width;
        this.height = height;
        proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 10000.0);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        oldX = e.getX();
        oldY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        cam = cam.addAzimuth(Math.PI * (e.getX() - oldX) / width)
                .addZenith(Math.PI * (e.getY() - oldY) / width);
        oldX = e.getX();
        oldY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                cam = cam.forward(1);
                break;
            case KeyEvent.VK_D:
                cam = cam.right(1);
                break;
            case KeyEvent.VK_S:
                cam = cam.backward(1);
                break;
            case KeyEvent.VK_A:
                cam = cam.left(1);
                break;
            case KeyEvent.VK_CONTROL:
                cam = cam.down(1);
                break;
            case KeyEvent.VK_SHIFT:
                cam = cam.up(1);
                break;
            case KeyEvent.VK_SPACE:
                cam = cam.withFirstPerson(!cam.getFirstPerson());
                break;
            case KeyEvent.VK_R:
                cam = cam.mulRadius(0.9f);
                break;
            case KeyEvent.VK_F:
                cam = cam.mulRadius(1.1f);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void dispose(GLAutoDrawable glDrawable) {
        GL4 gl = glDrawable.getGL().getGL4();
        gl.glDeleteProgram(shaderProgram);
    }
}