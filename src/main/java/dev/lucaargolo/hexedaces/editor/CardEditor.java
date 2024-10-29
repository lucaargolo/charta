package dev.lucaargolo.hexedaces.editor;

import dev.lucaargolo.hexedaces.HexedAces;
import dev.lucaargolo.hexedaces.utils.CardImage;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class CardEditor extends JFrame {

    private static final int CARD_SCALE = 10;
    private static final int SCALED_WIDTH = CardImage.WIDTH*CARD_SCALE;
    private static final int SCALED_HEIGHT = CardImage.HEIGHT*CARD_SCALE;

    private final Deque<CardImage> undoHistory = new ArrayDeque<>();
    private final Deque<CardImage> redoHistory = new ArrayDeque<>();
    private CardImage currentImage;

    private final JLabel imageLabel;
    private final ToolPanel toolPanel;
    private int draggingButton = MouseEvent.NOBUTTON;

    public CardEditor() {
        setTitle("Card Editor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if(!files.isEmpty()) {
                        loadImageFile(files.getFirst());
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        add(imageLabel, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New Image");
        JMenuItem loadItem = new JMenuItem("Load Image");
        JMenuItem saveItem = new JMenuItem("Save Image");
        fileMenu.add(newItem);
        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);

        JMenu utilsMenu = new JMenu("Utils");
        JMenuItem convertAtlas = new JMenuItem("Convert Atlas");
        utilsMenu.add(convertAtlas);
        menuBar.add(utilsMenu);

        setJMenuBar(menuBar);

        newItem.addActionListener(e -> createNewImage());
        loadItem.addActionListener(e -> loadCardImage());
        saveItem.addActionListener(e -> saveCardImage());
        convertAtlas.addActionListener(e -> convertCardAtlas());

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                validEvent(e, e.getButton(), true);
                draggingButton = e.getButton();
                imageLabel.requestFocus();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                validEvent(e, draggingButton, false);
                imageLabel.requestFocus();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                validEvent(e, draggingButton, true);
                imageLabel.requestFocus();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                imageLabel.requestFocus();

            }

            private void validEvent(MouseEvent e, int button, boolean save) {
                Dimension dimension = imageLabel.getSize();
                int left = dimension.width/2 - SCALED_WIDTH/2;
                int top = dimension.height/2 - SCALED_HEIGHT/2;

                int x = (e.getX() - left)/CARD_SCALE;
                int y = (e.getY() - top)/CARD_SCALE;

                paintImage(button, x, y, save);
            }
        };
        imageLabel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_Z:
                            handleUndo();
                            break;
                        case KeyEvent.VK_Y:
                            handleRedo();
                            break;
                    }
                }
            }
        });
        imageLabel.setFocusable(true);
        imageLabel.addMouseListener(mouseAdapter);
        imageLabel.addMouseMotionListener(mouseAdapter);

        toolPanel = new ToolPanel();
        add(toolPanel, BorderLayout.SOUTH);

        createNewImage();
        updateImage();
    }

    private void paintImage(int button, int x, int y, boolean save) {
        if(currentImage != null && toolPanel != null) {
            if(save) {
                if(!undoHistory.isEmpty()) {
                    CardImage last = undoHistory.getLast();
                    if(!last.equals(currentImage)) {
                        handleHistory();
                    }
                }else{
                    handleHistory();
                }
            }
            if (isValidButton(button) && x >= 0 && x < CardImage.WIDTH && y >= 0 && y < CardImage.HEIGHT) {
                int colorIndex = button == MouseEvent.BUTTON1 ? toolPanel.getLeftIndex() : toolPanel.getRightIndex();
                int alphaIndex = toolPanel.getAlphaIndex() - 1;
                if(!toolPanel.isFilling()) {
                    currentImage.setPixel(x, y, colorIndex, alphaIndex);
                }else{
                    byte targetPixel = (byte) ((alphaIndex << 6) | (colorIndex & 0x3F));
                    byte currentPixel = currentImage.getPixel(x, y);
                    if(currentPixel != targetPixel)
                        fillArea(x, y, currentPixel, colorIndex, alphaIndex);
                }
            }
            updateImage();
        }
    }

    private void fillArea(int startX, int startY, byte targetPixel, int colorIndex, int alphaIndex) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY));

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            int x = p.x;
            int y = p.y;

            if (x >= 0 && x < CardImage.WIDTH && y >= 0 && y < CardImage.HEIGHT && currentImage.getPixel(x, y) == targetPixel) {
                currentImage.setPixel(x, y, colorIndex, alphaIndex);

                queue.add(new Point(x + 1, y));
                queue.add(new Point(x - 1, y));
                queue.add(new Point(x, y + 1));
                queue.add(new Point(x, y - 1));
            }
        }
    }

    private boolean isValidButton(int button) {
        return button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3;
    }

    private void handleHistory() {
        undoHistory.addLast(currentImage.copy());
        if(undoHistory.size() > 1000) {
            undoHistory.removeFirst();
        }
        redoHistory.clear();
    }

    private void handleUndo() {
        if(!undoHistory.isEmpty()) {
            redoHistory.addLast(currentImage);
            CardImage lastImage = undoHistory.removeLast();
            if(lastImage.equals(currentImage)) {
                lastImage = undoHistory.removeLast();
            }
            currentImage = lastImage;
        }
        updateImage();
    }

    private void handleRedo() {
        if(!redoHistory.isEmpty()) {
            undoHistory.addLast(currentImage);
            currentImage = redoHistory.removeLast();
        }
        updateImage();
    }

    private void updateImage() {
        BufferedImage displayImage = new BufferedImage(CardImage.WIDTH, CardImage.HEIGHT, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < CardImage.WIDTH; x++) {
            for (int y = 0; y < CardImage.HEIGHT; y++) {
                displayImage.setRGB(x, y, currentImage.getARGBPixel(x, y));
            }
        }

        imageLabel.setIcon(new ImageIcon(displayImage.getScaledInstance(SCALED_WIDTH, SCALED_HEIGHT, Image.SCALE_AREA_AVERAGING)));

        revalidate();
        repaint();
    }

    private void createNewImage() {
        currentImage = new CardImage();
        undoHistory.clear();
        redoHistory.clear();
        for (int x = 0; x < CardImage.WIDTH; x++) {
            for (int y = 0; y < CardImage.HEIGHT; y++) {
                currentImage.setPixel(x, y, 7, 3);
            }
        }
        updateImage();
    }

    private void loadCardImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadImageFile(selectedFile);
        }
    }

    private void loadImageFile(File selectedFile) {
        try {
            if(selectedFile.getName().endsWith(".mccard")) {
                currentImage = CardImage.loadFromFile(selectedFile);
                undoHistory.clear();
                redoHistory.clear();
            }else{
                BufferedImage image = ImageIO.read(selectedFile);
                currentImage = new CardImage();
                undoHistory.clear();
                redoHistory.clear();
                int width = Math.min(image.getWidth(), CardImage.WIDTH);
                int height = Math.min(image.getHeight(), CardImage.HEIGHT);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int argb = image.getRGB(x, y);
                        currentImage.setARGBPixel(x, y, argb);
                    }
                }
            }
            updateImage();
        } catch (IOException e) {
            HexedAces.LOGGER.error("Error loading image: {}", selectedFile.getAbsoluteFile(), e);
            JOptionPane.showMessageDialog(this, "Error loading image.");
        }
    }

    private void convertCardAtlas() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String fileName = selectedFile.getName();
                String cardName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                File outputFile = new File(selectedFile.toPath().getParent() + File.separator + cardName);
                CardImage.saveCards(ImageIO.read(selectedFile), outputFile);
                JOptionPane.showMessageDialog(this, "Finalized atlas conversion.");
            } catch (IOException e) {
                HexedAces.LOGGER.error("Error loading image: {}", selectedFile.getAbsoluteFile(), e);
                JOptionPane.showMessageDialog(this, "Error loading image: "+selectedFile.getName());
            }
        }
    }

    // Save image to .mccard file
    private void saveCardImage() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "No image to save.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().endsWith(".mccard")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".mccard");
            }
            try {
                currentImage.saveToFile(fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Image saved successfully.");
            } catch (IOException e) {
                HexedAces.LOGGER.error("Error saving image: {}", fileToSave.getAbsoluteFile(), e);
                JOptionPane.showMessageDialog(this, "Error saving image.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CardEditor editor = new CardEditor();
            editor.setUndecorated(true);
            editor.setBackground(new Color(0, 0, 0, 1));
            editor.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
            editor.setVisible(true);
        });
    }
}
