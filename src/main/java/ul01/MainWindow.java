package ul01;


import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame {

    private static final int FPS = 60; // animator's target frames per second

    private JCheckBox computeInVSCheckbox = new JCheckBox("Osvětlení ve VS");
    private String[] colorModesFragment = new String[]{"Pozice", "Normála", "Textura", "Parallax"};
    private JComboBox<String> colorModeCombobox = new JComboBox<>(colorModesFragment);
    private JCheckBox useNormalTextureCheckbox = new JCheckBox("Normálová textura", true);
    private JButton changeTextureButton = new JButton("Změnit texturu");
    private JButton changeFunctionButton = new JButton("Změnit funkci");
    private JCheckBox spotlightCheckbox = new JCheckBox("Baterka");
    private JButton resetCameraButton = new JButton("Reset kamery");
    private JSlider blurSlider = new JSlider(1, 10);

    private WorldRenderer renderer;

    public MainWindow() throws HeadlessException {
        setLocationRelativeTo(null);
        setSize(800, 600);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        JPanel toolbar = new JPanel();
        add(toolbar, BorderLayout.NORTH);
        toolbar.add(new JLabel("Obarvení"));
        toolbar.add(colorModeCombobox);
        toolbar.add(new JLabel("Motion Blur"));
        toolbar.add(blurSlider);
        toolbar.add(computeInVSCheckbox);
        toolbar.add(useNormalTextureCheckbox);
        toolbar.add(spotlightCheckbox);

        JPanel toolbar2 = new JPanel();
        add(toolbar2, BorderLayout.SOUTH);
        toolbar2.add(changeTextureButton);
        toolbar2.add(changeFunctionButton);
        toolbar2.add(resetCameraButton);

        initListeners();

        GLCanvas canvas = createCanvas();
        add(canvas);

        colorModeCombobox.setSelectedIndex(renderer.getColorMode());
        blurSlider.setValue(renderer.getBlurTextureCount());

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
        colorModeCombobox.addActionListener(e -> renderer.setColorMode(colorModeCombobox.getSelectedIndex()));
        computeInVSCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = computeInVSCheckbox.isSelected();
                renderer.setComputeLightInFS(selected);
                if (selected) {
                    if (useNormalTextureCheckbox.isSelected()) {
                        renderer.setUseNormalTexture(false);
                        useNormalTextureCheckbox.setSelected(false);
                    }
                    if (colorModeCombobox.getSelectedIndex() == 3) {
                        colorModeCombobox.setSelectedIndex(2);
                    }
                }
                useNormalTextureCheckbox.setEnabled(selected);
            }
        });
        useNormalTextureCheckbox.addActionListener(e -> renderer.setUseNormalTexture(useNormalTextureCheckbox.isSelected()));
        spotlightCheckbox.addActionListener(e -> renderer.toggleSpotlight());
        changeTextureButton.addActionListener(e -> renderer.changeTexture());
        changeFunctionButton.addActionListener(e -> renderer.changeFunction());
        resetCameraButton.addActionListener(e -> renderer.resetCamera());
        blurSlider.addChangeListener(e -> renderer.setBlurTextureCount(blurSlider.getValue()));
    }


    private GLCanvas createCanvas() {
        GLProfile profile = GLProfile.getMaximum(true);
        GLCapabilities capabilities = new GLCapabilities(profile);

        GLCanvas canvas = new GLCanvas(capabilities);
        renderer = new WorldRenderer();
        canvas.addGLEventListener(renderer);
        canvas.addMouseListener(renderer);
        canvas.addMouseMotionListener(renderer);
        canvas.addMouseWheelListener(renderer);
        canvas.addKeyListener(renderer);
        canvas.setSize(800, 600);

        return canvas;
    }
}
