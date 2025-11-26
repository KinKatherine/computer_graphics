import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageProcessor extends JFrame {

    private BufferedImage originalImage;
    private JLabel originalLabel;
    private JLabel processedLabel;

    public ImageProcessor() {
        setTitle("Лабораторная работа 2 (Вариант 9): Комплексная обработка изображений");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        originalLabel = new JLabel("Исходное изображение", SwingConstants.CENTER);
        processedLabel = new JLabel("Обработанное изображение", SwingConstants.CENTER);

        setupImageLabel(originalLabel);
        setupImageLabel(processedLabel);
        imagePanel.add(originalLabel);
        imagePanel.add(processedLabel);
        mainPanel.add(imagePanel);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loadButton = new JButton("1. Загрузить");
        JButton fixedThresholdButton = new JButton("2. Порог (Фиксированный)");
        JButton otsuThresholdButton = new JButton("3. Порог (Метод Оцу)");
        JButton edgeButton = new JButton("4. Обнаружение границ (Лаплас)");
        JButton saveButton = new JButton("5. Сохранить (Сжатие JPEG)");

        controlPanel.add(loadButton);
        controlPanel.add(fixedThresholdButton);
        controlPanel.add(otsuThresholdButton);
        controlPanel.add(edgeButton);
        controlPanel.add(saveButton);

        mainPanel.add(controlPanel);

        loadButton.addActionListener(e -> loadImage());

        fixedThresholdButton.addActionListener(e -> processImage(this::applyFixedThreshold, "Глобальный порог (Фиксированный)"));
        otsuThresholdButton.addActionListener(e -> processImage(this::applyOtsuThreshold, "Глобальный порог (Метод Оцу)"));

        edgeButton.addActionListener(e -> processImage(this::applyEdgeDetection, "Сегментация (Обнаружение границ Лапласом)"));
        saveButton.addActionListener(e -> saveImage());

        add(mainPanel);
        pack();
        setVisible(true);
    }


    private void setupImageLabel(JLabel label) {
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setPreferredSize(new Dimension(350, 300));
    }

    @FunctionalInterface
    interface Processor {
        BufferedImage process(BufferedImage src);
    }

    private void processImage(Processor processor, String title) {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BufferedImage result = processor.process(originalImage);
            displayProcessedImage(result, title);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка обработки: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(file);
                if (originalImage != null) {
                    ImageIcon icon = new ImageIcon(originalImage.getScaledInstance(350, 300, Image.SCALE_SMOOTH));
                    originalLabel.setIcon(icon);
                    originalLabel.setText("Исходное изображение: " + file.getName());
                    processedLabel.setIcon(null);
                    processedLabel.setText("Обработанное изображение");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке изображения!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayProcessedImage(BufferedImage image, String title) {
        ImageIcon icon = new ImageIcon(image.getScaledInstance(350, 300, Image.SCALE_SMOOTH));
        processedLabel.setIcon(icon);
        processedLabel.setText(title);
    }

    private void saveImage() {
        if (processedLabel.getIcon() == null) {
            JOptionPane.showMessageDialog(this, "Сначала обработайте изображение.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить файл со сжатием (JPEG)");
        fileChooser.setSelectedFile(new File("compressed_image.jpg"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                Image tempImage = ((ImageIcon) processedLabel.getIcon()).getImage();
                BufferedImage image = new BufferedImage(
                        tempImage.getWidth(null),
                        tempImage.getHeight(null),
                        BufferedImage.TYPE_INT_RGB
                );

                Graphics2D g2 = image.createGraphics();
                g2.drawImage(tempImage, 0, 0, null);
                g2.dispose();

                ImageIO.write(image, "jpg", file);
                JOptionPane.showMessageDialog(this, "Изображение сохранено (JPEG/Сжатие)", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при сохранении изображения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private BufferedImage convertToGrayscale(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                int grayRGB = (gray << 16) | (gray << 8) | gray;
                dest.setRGB(x, y, grayRGB);
            }
        }
        return dest;
    }


    private BufferedImage applyFixedThreshold(BufferedImage src) {
        String input = JOptionPane.showInputDialog(this, "Введите значение порога (0-255):", "127");
        if (input == null) return src;
        int threshold;
        try {
            threshold = Integer.parseInt(input);
            if (threshold < 0 || threshold > 255) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректное значение порога (0-255).", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return src;
        }

        BufferedImage gray = convertToGrayscale(src);
        int w = gray.getWidth();
        int h = gray.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int grayValue = gray.getRGB(x, y) & 0xFF;
                int outputColor = (grayValue > threshold) ? 0xFFFFFFFF : 0xFF000000;
                dest.setRGB(x, y, outputColor);
            }
        }
        return dest;
    }

    private BufferedImage applyOtsuThreshold(BufferedImage src) {
        BufferedImage gray = convertToGrayscale(src);
        int w = gray.getWidth();
        int h = gray.getHeight();
        int total = w * h;

        int[] histData = new int[256];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                histData[gray.getRGB(x, y) & 0xFF]++;
            }
        }

        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * histData[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float maxVariance = 0;
        int optimalThreshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histData[i];
            if (wB == 0) continue;

            wF = total - wB;
            if (wF == 0) break;

            sumB += (float) i * histData[i];

            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            // Межклассовая дисперсия
            float variance = (float) wB * wF * (mB - mF) * (mB - mF);

            if (variance > maxVariance) {
                maxVariance = variance;
                optimalThreshold = i;
            }
        }

        final int finalThreshold = optimalThreshold;

        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int grayValue = gray.getRGB(x, y) & 0xFF;
                int outputColor = (grayValue > finalThreshold) ? 0xFFFFFFFF : 0xFF000000;
                dest.setRGB(x, y, outputColor);
            }
        }
        JOptionPane.showMessageDialog(this, "Оптимальный порог (Оцу): " + optimalThreshold, "Результат Оцу", JOptionPane.INFORMATION_MESSAGE);
        return dest;
    }



    private BufferedImage applyEdgeDetection(BufferedImage src) {
        BufferedImage gray = convertToGrayscale(src);
        int w = gray.getWidth();
        int h = gray.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        int[][] kernel = {{0, 1, 0}, {1, -4, 1}, {0, 1, 0}};

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int sum = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = gray.getRGB(x + kx, y + ky) & 0xFF;
                        sum += pixel * kernel[ky + 1][kx + 1];
                    }
                }
                int result = Math.abs(sum);
                result = Math.min(255, result);

                int outputRGB = (result << 16) | (result << 8) | result;
                dest.setRGB(x, y, outputRGB);
            }
        }
        return dest;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageProcessor::new);
    }
}