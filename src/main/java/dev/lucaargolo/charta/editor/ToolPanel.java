package dev.lucaargolo.charta.editor;

import dev.lucaargolo.charta.utils.CardImage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ToolPanel extends JPanel {

    private int selectedLeftIndex = 0;
    private int selectedRightIndex = 0;

    private final JSlider alphaSlider;
    private final JLabel leftColorLabel;
    private final JLabel rightColorLabel;
    private final JCheckBox pencilCheckbox;
    private final JCheckBox fillCheckbox;

    public ToolPanel() {

        setLayout(new BorderLayout());

        // Color selection area
        JPanel colorPanel = new JPanel(new GridLayout(8, 8));
        for (int i = 0; i < CardImage.COLOR_PALETTE.length; i++) {
            JButton colorButton = getColorButton(i);
            colorPanel.add(colorButton);
        }

        // Create transparency slider
        JPanel transparencyPanel = new JPanel(new BorderLayout());
        alphaSlider = new JSlider(1, 4);
        alphaSlider.setMajorTickSpacing(1);
        alphaSlider.setPaintTicks(true);
        alphaSlider.setPaintLabels(true);
        alphaSlider.setSnapToTicks(true);
        alphaSlider.setLabelTable(alphaSlider.createStandardLabels(1));
        alphaSlider.setValue(4);
        transparencyPanel.add(new JLabel("Alpha Value"), BorderLayout.NORTH);
        transparencyPanel.add(alphaSlider, BorderLayout.SOUTH);

        // Label panels for selected colors
        leftColorLabel = new JLabel();
        leftColorLabel.setOpaque(true);
        leftColorLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        leftColorLabel.setBackground(new Color(CardImage.COLOR_PALETTE[selectedLeftIndex]));
        leftColorLabel.setPreferredSize(new Dimension(25, 25));

        rightColorLabel = new JLabel();
        rightColorLabel.setOpaque(true);
        rightColorLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        rightColorLabel.setBackground(new Color(CardImage.COLOR_PALETTE[selectedRightIndex]));
        rightColorLabel.setPreferredSize(new Dimension(25, 25));

        JPanel leftVisualizingPanel = new JPanel(new BorderLayout());
        leftVisualizingPanel.add(new JLabel("Left"), BorderLayout.NORTH);
        leftVisualizingPanel.add(new JLabel("Right"), BorderLayout.CENTER);
        pencilCheckbox = new JCheckBox("Pencil");
        pencilCheckbox.setSelected(true);
        leftVisualizingPanel.add(pencilCheckbox, BorderLayout.SOUTH);

        JPanel rightVisualizingPanel = new JPanel(new BorderLayout());
        rightVisualizingPanel.add(leftColorLabel, BorderLayout.NORTH);
        rightVisualizingPanel.add(rightColorLabel, BorderLayout.CENTER);
        fillCheckbox = new JCheckBox("Fill");
        fillCheckbox.setSelected(false);
        rightVisualizingPanel.add(fillCheckbox, BorderLayout.SOUTH);

        pencilCheckbox.addChangeListener(e -> {
            if(pencilCheckbox.isSelected()) {
                fillCheckbox.setSelected(false);
            }
        });
        fillCheckbox.addChangeListener(e -> {
            if(fillCheckbox.isSelected()) {
                pencilCheckbox.setSelected(false);
            }
        });

        JPanel visualizingPanel = new JPanel(new BorderLayout());
        visualizingPanel.add(leftVisualizingPanel, BorderLayout.WEST);
        visualizingPanel.add(rightVisualizingPanel, BorderLayout.EAST);

        add(colorPanel, BorderLayout.CENTER);
        add(visualizingPanel, BorderLayout.WEST);
        add(transparencyPanel, BorderLayout.EAST);
    }

    private @NotNull JButton getColorButton(int i) {
        final Color color = new Color(CardImage.COLOR_PALETTE[i]);
        JButton colorButton = new JButton();
        colorButton.setBackground(color);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(true);
        colorButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    selectedLeftIndex = i;
                    leftColorLabel.setBackground(new Color(CardImage.COLOR_PALETTE[selectedLeftIndex]));
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    selectedRightIndex = i;
                    rightColorLabel.setBackground(new Color(CardImage.COLOR_PALETTE[selectedRightIndex]));
                }
            }
        });
        return colorButton;
    }

    public int getLeftIndex() {
        return selectedLeftIndex;
    }

    public int getRightIndex() {
        return selectedRightIndex;
    }

    public int getAlphaIndex() {
        return alphaSlider.getValue();
    }

    public boolean isFilling() {
        return fillCheckbox.isSelected();
    }
}

