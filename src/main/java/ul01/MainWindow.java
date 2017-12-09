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
    private JLabel colorModeLabel = new JLabel("Obarvení:");
    private String[] colorModesFragment = new String[]{"Pozice", "Normála", "Textura", "Parallax"};
    private String[] colorModesVertex = new String[]{"Pozice", "Normála", "Textura"};
    private JComboBox<String> colorModeCombobox = new JComboBox<String>(colorModesFragment);
    private JCheckBox useNormalTextureCheckbox = new JCheckBox("Normálová textura", true);
    private JButton changeTextureButton = new JButton("Změnit texturu");
    private JButton changeFunctionButton = new JButton("Změnit funkci");
    private JCheckBox spotlightCheckbox = new JCheckBox("Baterka");
    private JButton resetCameraButton = new JButton("Reset kamery");

    private WorldRenderer renderer;

    public MainWindow() throws HeadlessException {
        setLocationRelativeTo(null);
        setSize(980, 640);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        JPanel toolbar = new JPanel();
        add(toolbar, BorderLayout.NORTH);
        toolbar.add(colorModeLabel);
        toolbar.add(colorModeCombobox);
        toolbar.add(computeInVSCheckbox);
        toolbar.add(useNormalTextureCheckbox);
        toolbar.add(spotlightCheckbox);
        toolbar.add(changeTextureButton);
        toolbar.add(changeFunctionButton);
        toolbar.add(resetCameraButton);

        colorModeCombobox.setSelectedIndex(3);

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
        canvas.setSize(980, 640);

        return canvas;
    }
}
