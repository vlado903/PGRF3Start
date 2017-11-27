package view;


import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import grid.WorldRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame {

    private static final int FPS = 60; // animator's target frames per second

    private JButton switchModeButton = new JButton("Osvětlení VS/FS");
    private JButton switchColorModeButton = new JButton("Změnit obarvení");
    private JButton changeTextureButton = new JButton("Změnit texturu");
    private JButton changeFunctionButton = new JButton("Změnit funkci");
    private JButton toggleSpotlightButton = new JButton("Přepnout baterku");
    private JButton resetCameraButton = new JButton("Reset kamery");

    private WorldRenderer renderer;

    public MainWindow() throws HeadlessException {
        setLocationRelativeTo(null);
        setSize(960, 480);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        JPanel toolbar = new JPanel();
        add(toolbar, BorderLayout.NORTH);
        toolbar.add(switchModeButton);
        toolbar.add(switchColorModeButton);
        toolbar.add(changeTextureButton);
        toolbar.add(changeFunctionButton);
        toolbar.add(toggleSpotlightButton);
        toolbar.add(resetCameraButton);

        initListeners();

        GLCanvas canvas = createCanvas();
        add(canvas);

        final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        if (animator.isStarted()) animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });
        animator.start();
    }

    private void initListeners() {
        switchModeButton.addActionListener(e -> renderer.switchLightningMode());
        switchColorModeButton.addActionListener(e -> renderer.switchColorMode());
        changeTextureButton.addActionListener(e -> renderer.changeTexture());
        changeFunctionButton.addActionListener(e -> renderer.changeFunction());
        toggleSpotlightButton.addActionListener(e -> renderer.toggleSpotlight());
        resetCameraButton.addActionListener(e -> renderer.resetCamera());
    }


    private GLCanvas createCanvas() {
        // setup OpenGL version
        GLProfile profile = GLProfile.getMaximum(true);
        GLCapabilities capabilities = new GLCapabilities(profile);

        // The canvas is the widget that's drawn in the JFrame
        GLCanvas canvas = new GLCanvas(capabilities);
        renderer = new WorldRenderer();
        canvas.addGLEventListener(renderer);
        canvas.addMouseListener(renderer);
        canvas.addMouseMotionListener(renderer);
        canvas.addMouseWheelListener(renderer);
        canvas.addKeyListener(renderer);
        canvas.setSize(960, 480);

        return canvas;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
