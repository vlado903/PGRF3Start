package grid;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import oglutils.OGLBuffers;
import oglutils.OGLUtils;
import oglutils.ShaderUtils;
import oglutils.ToFloatArray;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldRenderer implements GLEventListener, MouseListener,
        MouseMotionListener, MouseWheelListener, KeyListener {

    private static final int NUM_OF_FUNCTIONS = 6;

    private int width, height;
    private int oldX, oldY;
    private int shaderProgram, locMv, locProj, locEyePos, locTime,
            locFunction, locComputeLightInFS, locSpotlight, locColorMode;
    private float time = 0;
    private int function = 2;
    private int textureIndex = 4;

    private boolean computeLightInFS = true;
    private boolean spotlight = false;
    private int colorMode = 3;

    private Camera cam = new Camera();
    private OGLBuffers buffers;

    private Mat4 proj;

    private List<Texture> normalTextures = new ArrayList<>();
    private List<Texture> heightTextures = new ArrayList<>();
    private List<Texture> textures = new ArrayList<>();

    @Override
    public void init(GLAutoDrawable glDrawable) {
        // check whether shaders are supported
        GL4 gl = glDrawable.getGL().getGL4();
        OGLUtils.shaderCheck(gl);
        OGLUtils.printOGLparameters(gl);

        shaderProgram = ShaderUtils.loadProgram(gl,
                "/shaders/shade.vert",
                "/shaders/shade.frag",
                null,
                null,
                null,
                null);

        locMv = gl.glGetUniformLocation(shaderProgram, "mMv");
        locProj = gl.glGetUniformLocation(shaderProgram, "mProj");
        locEyePos = gl.glGetUniformLocation(shaderProgram, "eyePos");
        locTime = gl.glGetUniformLocation(shaderProgram, "time");
        locFunction = gl.glGetUniformLocation(shaderProgram, "function");
        locComputeLightInFS = gl.glGetUniformLocation(shaderProgram, "computeLightInFS");
        locSpotlight = gl.glGetUniformLocation(shaderProgram, "spotlight");
        locColorMode = gl.glGetUniformLocation(shaderProgram, "colorMode");

        try {
            File resourceFolder = new File(getClass().getClassLoader().getResource("./textures/").getFile());

            File[] files = resourceFolder.listFiles();
            Arrays.sort(files);
            for (File file : files) {
                Texture texture = TextureIO.newTexture(file, false);
                if (file.getName().contains("_n.")) {
                    normalTextures.add(texture);
                } else if (file.getName().contains("_h.")) {
                    heightTextures.add(texture);
                } else {
                    textures.add(texture);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        resetCamera();

        buffers = GridFactory.gridGenerate(gl, 30, 30);
    }


    @Override
    public void display(GLAutoDrawable glDrawable) {
        GL4 gl = glDrawable.getGL().getGL4();

        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(shaderProgram);
        time += 0.1;

        gl.glUniform1f(locTime, time);
        gl.glUniformMatrix4fv(locMv, 1, false, ToFloatArray.convert(cam.getViewMatrix()), 0);
        gl.glUniformMatrix4fv(locProj, 1, false, ToFloatArray.convert(proj), 0);
        gl.glUniform3f(locEyePos, (float) cam.getPosition().getX(),
                (float) cam.getPosition().getY(),
                (float) cam.getPosition().getZ());
        gl.glUniform1i(locFunction, function);
        gl.glUniform1i(locComputeLightInFS, computeLightInFS ? 1 : 0);
        gl.glUniform1i(locSpotlight, spotlight ? 1 : 0);
        gl.glUniform1i(locColorMode, colorMode);

        gl.glActiveTexture(GL4.GL_TEXTURE0);
        textures.get(textureIndex).bind(gl);
        gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "chosenTexture"), 0);

        gl.glActiveTexture(GL4.GL_TEXTURE1);
        normalTextures.get(textureIndex).bind(gl);
        gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "chosenTextureNormal"), 1);

        gl.glActiveTexture(GL4.GL_TEXTURE2);
        heightTextures.get(textureIndex).bind(gl);
        gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "chosenTextureHeight"), 2);

        gl.glEnable(GL4.GL_DEPTH_TEST);
        gl.glPolygonMode(GL4.GL_CULL_FACE, GL4.GL_FILL);

        // bind and draw
        buffers.draw(GL4.GL_TRIANGLES, shaderProgram);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
                        int height) {
        this.width = width;
        this.height = height;
        proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
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
                .addZenith(Math.PI * (e.getY() - oldY) / height);
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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        cam = cam.backward((float) e.getWheelRotation() / 5);
    }

    public void switchLightningMode() {
        computeLightInFS = !computeLightInFS;
    }

    public void switchColorMode() {
        colorMode = (colorMode + 1) % 4;
    }

    public void changeTexture() {
        textureIndex = (textureIndex + 1) % textures.size();
    }

    public void changeFunction() {
        function = (function + 1) % NUM_OF_FUNCTIONS;
    }

    public void toggleSpotlight() {
        spotlight = !spotlight;
    }

    public void resetCamera() {
        cam = cam.withPosition(new Vec3D(2, 2, 2.5))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.225);
    }
}