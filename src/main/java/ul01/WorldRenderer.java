package ul01;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import oglutils.*;
import transforms.*;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorldRenderer implements GLEventListener, MouseListener,
        MouseMotionListener, MouseWheelListener, KeyListener {

    private static final int NUM_OF_FUNCTIONS = 6;
    private static final int MAX_MOTION_BLUR_FRAME_COUNT = 10;

    private int width, height;
    private double oldX;
    private double oldY;
    private boolean mouseTracking;

    private int shaderProgram, blurShaderProgram, locMv, locProj, locEyePos, locTime,
            locFunction, locComputeLightInFS, locSpotlight, locColorMode, locNormalTexture, locTrans,
            locBlurTextures, locBlurTextureCount;
    private float time = 0;
    private int function = 2;
    private int textureIndex = 4;

    private boolean computeLightInFS = true;
    private boolean spotlight = false;
    private boolean useNormalTexture = true;
    private int colorMode = 3;
    private int blurTextureCount = 5;

    private Camera cam = new Camera();
    private OGLBuffers buffers;
    private OGLBuffers wholeViewportBuffers;

    private Mat4 proj;

    private List<Texture> normalTextures = new ArrayList<>();
    private List<Texture> heightTextures = new ArrayList<>();
    private List<Texture> textures = new ArrayList<>();

    private List<OGLRenderTarget> blurRenderTargets = new ArrayList<>();

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

        blurShaderProgram = ShaderUtils.loadProgram(gl,
                "/shaders/motionblur.vert",
                "/shaders/motionblur.frag",
                null,
                null,
                null,
                null);

        locMv = gl.glGetUniformLocation(shaderProgram, "mMv");
        locProj = gl.glGetUniformLocation(shaderProgram, "mProj");
        locTrans = gl.glGetUniformLocation(shaderProgram, "mTrans");
        locEyePos = gl.glGetUniformLocation(shaderProgram, "eyePos");
        locTime = gl.glGetUniformLocation(shaderProgram, "time");
        locFunction = gl.glGetUniformLocation(shaderProgram, "function");
        locComputeLightInFS = gl.glGetUniformLocation(shaderProgram, "computeLightInFS");
        locSpotlight = gl.glGetUniformLocation(shaderProgram, "spotlight");
        locNormalTexture = gl.glGetUniformLocation(shaderProgram, "normalTexture");
        locColorMode = gl.glGetUniformLocation(shaderProgram, "colorMode");

        locBlurTextures = gl.glGetUniformLocation(blurShaderProgram, "blurTextures");
        locBlurTextureCount = gl.glGetUniformLocation(blurShaderProgram, "blurTextureCount");

        try {
            File resourceFolder = new File(getClass().getClassLoader().getResource("./textures/").getFile());

            File[] files = resourceFolder.listFiles((dir, name) -> {
                String lowerCase = name.toLowerCase();
                return lowerCase.contains("png") || lowerCase.contains("jpg");
            });
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

        buffers = GridFactory.generateGrid(gl, 30, 30);

        float[] viewportVertices = {-1, -1, -1, 1, 1, 1, 1, -1, -1, -1};
        OGLBuffers.Attrib[] attribs = {new OGLBuffers.Attrib("inPosition", 2)};
        wholeViewportBuffers = new OGLBuffers(gl, viewportVertices, attribs, null);
    }
    
    @Override
    public void display(GLAutoDrawable glDrawable) {
        GL4 gl = glDrawable.getGL().getGL4();

        // postupné plnění snímků pro motion blur
        Collections.rotate(blurRenderTargets.subList(0, blurTextureCount), 1);
        blurRenderTargets.get(0).bind();

        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(shaderProgram);
        time += 0.1;

        gl.glUniform1f(locTime, time);
        gl.glUniformMatrix4fv(locMv, 1, false, ToFloatArray.convert(cam.getViewMatrix()), 0);
        gl.glUniformMatrix4fv(locProj, 1, false, ToFloatArray.convert(proj), 0);
        gl.glUniformMatrix4fv(locTrans, 1, false, ToFloatArray.convert(new Mat4Transl(0, 0, 0)), 0);
        gl.glUniform3f(locEyePos, (float) cam.getPosition().getX(),
                (float) cam.getPosition().getY(),
                (float) cam.getPosition().getZ());
        gl.glUniform1i(locFunction, function);
        gl.glUniform1i(locComputeLightInFS, computeLightInFS ? 1 : 0);
        gl.glUniform1i(locSpotlight, spotlight ? 1 : 0);
        gl.glUniform1i(locNormalTexture, useNormalTexture ? 1 : 0);
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
        gl.glEnable(GL4.GL_POLYGON_SMOOTH);

        //TODO přesunout transformace do vertex shaderu, indexace objektů a podle toho translace
        buffers.draw(GL4.GL_TRIANGLES, shaderProgram);
        gl.glUniformMatrix4fv(locTrans, 1, false, ToFloatArray.convert(new Mat4Transl(5 * Math.cos(time / 3), 5 * Math.sin(time / 3), 0)), 0);
        buffers.draw(GL4.GL_TRIANGLES, shaderProgram);
        gl.glUniformMatrix4fv(locTrans, 1, false, ToFloatArray.convert(new Mat4Transl(10 * Math.cos(-time / 6), 10 * Math.sin(-time / 6), -5)), 0);
        buffers.draw(GL4.GL_TRIANGLES, shaderProgram);

        gl.glUseProgram(blurShaderProgram);

        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0); // unbindování framebufferu
        gl.glViewport(0, 0, width, height);

        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        int[] blurTexturesSlots = new int[blurTextureCount];
        for (int i = 0; i < blurTextureCount; i++) {
            gl.glActiveTexture(GL4.GL_TEXTURE0 + i);
            blurRenderTargets.get(i).getColorTexture().getTexture().bind(gl);
            blurTexturesSlots[i] = i;
        }
        gl.glUniform1i(locBlurTextureCount, blurTextureCount);
        gl.glUniform1iv(locBlurTextures, blurTextureCount, blurTexturesSlots, 0);

        wholeViewportBuffers.draw(GL4.GL_TRIANGLE_STRIP, blurShaderProgram);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
                        int height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);

            createBlurRenderTargets(drawable.getGL().getGL4(), width, height);
        }
    }

    private void createBlurRenderTargets(GL4 gl, int width, int height) {
        for (int i = 0; i < MAX_MOTION_BLUR_FRAME_COUNT; i++) {
            blurRenderTargets.add(new OGLRenderTarget(gl, width, height));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mouseTracking = !mouseTracking;
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
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mouseTracking) {
            cam = cam.addAzimuth(-1 * Math.PI * (e.getX() - oldX) / Math.min(width, height))
                    .addZenith(-1 * Math.PI * (e.getY() - oldY) / Math.min(width, height));
            oldX = e.getX();
            oldY = e.getY();
        }
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

    public void setComputeLightInFS(boolean computeLightInFS) {
        this.computeLightInFS = computeLightInFS;
    }

    public int getColorMode() {
        return colorMode;
    }

    public void setColorMode(int colorMode) {
        this.colorMode = colorMode;
    }

    public void setUseNormalTexture(boolean useNormalTexture) {
        this.useNormalTexture = useNormalTexture;
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

    public int getBlurTextureCount() {
        return blurTextureCount;
    }

    public void setBlurTextureCount(int blurTextureCount) {
        this.blurTextureCount = blurTextureCount;
    }
}