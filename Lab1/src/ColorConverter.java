import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ColorConverter extends JFrame {

    // Флаг для предотвращения рекурсивных обновлений
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);

    private final int[] rgb = {0, 0, 0};

    // Панель для отображения цвета
    private JPanel colorPanel;

    // Массивы компонентов
    private final JSlider[] rgbSliders = new JSlider[3];
    private final JTextField[] rgbFields = new JTextField[3];

    private final JSlider[] cmykSliders = new JSlider[4];
    private final JTextField[] cmykFields = new JTextField[4];

    private final JSlider[] hlsSliders = new JSlider[3];
    private final JTextField[] hlsFields = new JTextField[3];

    // Метки для компонентов
    private final String[] rgbLabels = {"Красный (R)", "Зеленый (G)", "Синий (B)"};
    private final String[] cmykLabels = {"Голубой (C, %)", "Пурпурный (M, %)", "Желтый (Y, %)", "Ключевой (K, %)"};
    private final String[] hlsLabels = {"Оттенок (H, °)", "Яркость (L, %)", "Насыщенность (S, %)"};

    // Максимальные значения для слайдеров
    private final int[] rgbMax = {255, 255, 255};
    private final int[] cmykMax = {100, 100, 100, 100};
    private final int[] hlsMax = {360, 100, 100};

    // Конструктор
    public ColorConverter() {
        setTitle("Лабораторная работа 1: CMYK ↔ RGB ↔ HLS");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception _) {
            System.exit(1);
        }

        initComponents();
        setupListeners();

        updateAllModels(0, 0, 0);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ColorConverter().setVisible(true));
    }

    private void initComponents() {
        // Создаем главную панель с отступами
        JPanel mainPanel = new JPanel(new BorderLayout(30, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Первая панель
        JPanel firstPanel = getFirstPanel();
        mainPanel.add(firstPanel, BorderLayout.NORTH);

        // Вторая панель
        JPanel secondPanel = getSecondPanel();
        mainPanel.add(secondPanel, BorderLayout.CENTER);

        // Третья панель
        JPanel footerPanel = getThirdPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel getFirstPanel() {
        colorPanel = new JPanel();
        colorPanel.setPreferredSize(new Dimension(500, 100));
        colorPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        JLabel titleLabel = new JLabel("Интерактивный конвертер цветовых моделей (CMYK-RGB-HLS)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel firstPanel = new JPanel(new BorderLayout(10, 10));
        firstPanel.add(titleLabel, BorderLayout.NORTH);
        firstPanel.add(colorPanel, BorderLayout.SOUTH);
        return firstPanel;
    }

    private JPanel getSecondPanel() {
        JPanel secondPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        secondPanel.add(createRgbPanel());
        secondPanel.add(createCmykPanel());
        secondPanel.add(createHlsPanel());
        return secondPanel;
    }

    private JPanel getThirdPanel() {
        JButton colorChooserButton = new JButton("Выбрать цвет из палитры...");
        colorChooserButton.addActionListener(_ -> {
            Color chosenColor = JColorChooser.showDialog(this, "Выберите цвет", new Color(rgb[0], rgb[1], rgb[2]));
            if (chosenColor != null) {
                updateAllModels(chosenColor.getRed(), chosenColor.getGreen(), chosenColor.getBlue());
            }
        });

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.add(colorChooserButton);
        return footerPanel;
    }

    private JPanel createRgbPanel() {
        JPanel panel = createModelPanel("RGB (Red, Green, Blue)", new Color(220, 38, 38, 20));

        // Инициализация RGB компонентов
        for (int i = 0; i < 3; i++) {
            rgbSliders[i] = createSlider(rgbMax[i], 0);
            rgbFields[i] = createTextField();
            panel.add(createControlGroup(rgbLabels[i], rgbSliders[i], rgbFields[i]));
        }
        return panel;
    }

    private JPanel createCmykPanel() {
        JPanel panel = createModelPanel("CMYK (Cyan, Magenta, Yellow, Key)", new Color(6, 182, 212, 20));

        // Инициализация CMYK компонентов
        for (int i = 0; i < 4; i++) {
            cmykSliders[i] = createSlider(cmykMax[i], 0);
            cmykFields[i] = createTextField();
            panel.add(createControlGroup(cmykLabels[i], cmykSliders[i], cmykFields[i]));
        }
        return panel;
    }

    private JPanel createHlsPanel() {
        JPanel panel = createModelPanel("HLS (Hue, Lightness, Saturation)", new Color(22, 163, 74, 20));
        // Инициализация HLS компонентов
        for (int i = 0; i < 3; i++) {
            hlsSliders[i] = createSlider(hlsMax[i], 0);
            hlsFields[i] = createTextField();
            panel.add(createControlGroup(hlsLabels[i], hlsSliders[i], hlsFields[i]));
        }
        return panel;
    }

    private JPanel createModelPanel(String title, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(bgColor);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private JSlider createSlider(int max, int value) {
        JSlider slider = new JSlider(0, max, value);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(max / 4);
        slider.setMinorTickSpacing(max / 20);
        slider.setPaintLabels(false);
        return slider;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(3);
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        return textField;
    }

    private JPanel createControlGroup(String labelText, JSlider slider, JTextField field) {
        JPanel groupPanel = new JPanel(new BorderLayout(5, 0));

        JLabel label = new JLabel(labelText + ":");
        label.setPreferredSize(new Dimension(120, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(label, BorderLayout.WEST);
        topPanel.add(field, BorderLayout.EAST);

        groupPanel.add(topPanel, BorderLayout.NORTH);
        groupPanel.add(slider, BorderLayout.CENTER);

        return groupPanel;
    }

    private void setupListeners() {
        // Слушатель для ползунка RGB
        ChangeListener rgbSliderListener = _ -> {
            if (isUpdating.get()) return;
            updateAllModels(rgbSliders[0].getValue(), rgbSliders[1].getValue(), rgbSliders[2].getValue());
        };

        // Слушатель для поля ввода RGB
        Runnable rgbFieldUpdater = () -> {
            if (isUpdating.get()) return;
            try {
                int newR = getValidatedValue(rgbFields[0].getText(), 0, 255);
                int newG = getValidatedValue(rgbFields[1].getText(), 0, 255);
                int newB = getValidatedValue(rgbFields[2].getText(), 0, 255);
                updateAllModels(newR, newG, newB);
            } catch (NumberFormatException _) {
                // Игнорируем неверный ввод
            }
        };

        // Привязка слушателей RGB
        for (int i = 0; i < 3; i++) {
            rgbSliders[i].addChangeListener(rgbSliderListener);
            rgbFields[i].addActionListener(_ -> rgbFieldUpdater.run());
        }

        // Слушатель для ползунка CMYK
        ChangeListener cmykSliderListener = _ -> {
            if (isUpdating.get()) return;

            // Получаем текущие значения, которые установил пользователь
            int c = cmykSliders[0].getValue();
            int m = cmykSliders[1].getValue();
            int y = cmykSliders[2].getValue();
            int k = cmykSliders[3].getValue();

            // Пересчитываем только RGB
            Color newRgb = cmykToRgb(c, m, y, k);

            // Обновляем все модели, кроме CMYK (которые уже установлены)
            updateNonCmyk(newRgb.getRed(), newRgb.getGreen(), newRgb.getBlue(), c, m, y, k);
        };

        // Слушатель для поля ввода CMYK
        Runnable cmykFieldUpdater = () -> {
            if (isUpdating.get()) return;
            try {
                int[] cmykValues = getValidatedValues(cmykFields, 0, 100);

                // Обновляем слайдеры CMYK (для синхронизации)
                for (int i = 0; i < 4; i++) {
                    cmykSliders[i].setValue(cmykValues[i]);
                }

                // Пересчитываем только RGB
                Color newRgb = cmykToRgb(cmykValues[0], cmykValues[1], cmykValues[2], cmykValues[3]);

                // Обновляем все модели, кроме CMYK
                updateNonCmyk(newRgb.getRed(), newRgb.getGreen(), newRgb.getBlue(),
                        cmykValues[0], cmykValues[1], cmykValues[2], cmykValues[3]);
            } catch (NumberFormatException _) {
                // Игнорируем неверный ввод
            }
        };

        // Слушатель для ползунка HLS
        ChangeListener hlsSliderListener = _ -> {
            if (isUpdating.get()) return;

            // Получаем текущие значения, которые установил пользователь
            int h = hlsSliders[0].getValue();
            int l = hlsSliders[1].getValue();
            int s = hlsSliders[2].getValue();

            // Пересчитываем только RGB
            Color newRgb = hlsToRgb(h, l, s);

            // Обновляем все модели, кроме HLS (которые уже установлены)
            updateNonHls(newRgb.getRed(), newRgb.getGreen(), newRgb.getBlue(), h, l, s);
        };

        // Слушатель для поля ввода HLS
        Runnable hlsFieldUpdater = () -> {
            if (isUpdating.get()) return;
            try {
                int[] hlsValues = getValidatedValues(hlsFields, new int[]{0, 0, 0}, new int[]{360, 100, 100});

                // Обновляем слайдеры HLS (для синхронизации)
                for (int i = 0; i < 3; i++) {
                    hlsSliders[i].setValue(hlsValues[i]);
                }

                // Пересчитываем только RGB
                Color newRgb = hlsToRgb(hlsValues[0], hlsValues[1], hlsValues[2]);

                // Обновляем все модели, кроме HLS
                updateNonHls(newRgb.getRed(), newRgb.getGreen(), newRgb.getBlue(),
                        hlsValues[0], hlsValues[1], hlsValues[2]);
            } catch (NumberFormatException _) {
                // Игнорируем неверный ввод
            }
        };

        // Привязка слушателей CMYK
        for (int i = 0; i < 4; i++) {
            cmykSliders[i].addChangeListener(cmykSliderListener);
            cmykFields[i].addActionListener(_ -> cmykFieldUpdater.run());
        }

        // Привязка слушателей HLS
        for (int i = 0; i < 3; i++) {
            hlsSliders[i].addChangeListener(hlsSliderListener);
            hlsFields[i].addActionListener(_ -> hlsFieldUpdater.run());
        }
    }

    private int getValidatedValue(String text, int min, int max) {
        int value = Integer.parseInt(text);
        int temp = Math.min(max, value);
        return Math.max(min, temp);
    }

    private int[] getValidatedValues(JTextField[] fields, int min, int max) {
        int[] values = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            values[i] = getValidatedValue(fields[i].getText(), min, max);
        }
        return values;
    }

    private int[] getValidatedValues(JTextField[] fields, int[] min, int[] max) {
        int[] values = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            values[i] = getValidatedValue(fields[i].getText(), min[i], max[i]);
        }
        return values;
    }

    private void updateAllModels(int r, int g, int b) {
        if (isUpdating.get()) return;

        isUpdating.set(true);
        try {
            // Обновляем RGB
            rgb[0] = r;
            rgb[1] = g;
            rgb[2] = b;

            // Обновляем RGB компоненты
            for (int i = 0; i < 3; i++) {
                rgbSliders[i].setValue(rgb[i]);
                rgbFields[i].setText(String.valueOf(rgb[i]));
            }

            // Обновляем CMYK компоненты
            float[] cmyk = rgbToCmyk(r, g, b);
            for (int i = 0; i < 4; i++) {
                int val = Math.round(cmyk[i] * 100);
                cmykSliders[i].setValue(val);
                cmykFields[i].setText(String.valueOf(val));
            }

            // Обновляем HLS компоненты
            float[] hls = rgbToHls(r, g, b);
            int hVal = Math.round(hls[0]);
            hlsSliders[0].setValue(hVal);
            hlsFields[0].setText(String.valueOf(hVal));
            for (int i = 1; i < 3; i++) {
                int val = Math.round(hls[i] * 100);
                hlsSliders[i].setValue(val);
                hlsFields[i].setText(String.valueOf(val));
            }

            // Обновляем панель цвета
            colorPanel.setBackground(new Color(r, g, b));
            colorPanel.repaint();
        } finally {
            isUpdating.set(false);
        }
    }
    private void updateNonCmyk(int r, int g, int b, int c, int m, int y, int k) {
        if (isUpdating.get()) return;

        isUpdating.set(true);
        try {
            // Обновляем RGB
            rgb[0] = r;
            rgb[1] = g;
            rgb[2] = b;

            // Обновляем RGB компоненты
            for (int i = 0; i < 3; i++) {
                rgbSliders[i].setValue(rgb[i]);
                rgbFields[i].setText(String.valueOf(rgb[i]));
            }

            // Обновляем CMYK компоненты (используем переданные значения)
            cmykSliders[0].setValue(c);
            cmykFields[0].setText(String.valueOf(c));
            cmykSliders[1].setValue(m);
            cmykFields[1].setText(String.valueOf(m));
            cmykSliders[2].setValue(y);
            cmykFields[2].setText(String.valueOf(y));
            cmykSliders[3].setValue(k);
            cmykFields[3].setText(String.valueOf(k));

            // Обновляем HLS компоненты (пересчет из RGB)
            float[] hls = rgbToHls(r, g, b);
            int hVal = Math.round(hls[0]);
            hlsSliders[0].setValue(hVal);
            hlsFields[0].setText(String.valueOf(hVal));
            for (int i = 1; i < 3; i++) {
                int val = Math.round(hls[i] * 100);
                hlsSliders[i].setValue(val);
                hlsFields[i].setText(String.valueOf(val));
            }

            // Обновляем панель цвета
            colorPanel.setBackground(new Color(r, g, b));
            colorPanel.repaint();
        } finally {
            isUpdating.set(false);
        }
    }

    /**
     * Обновляет все модели, когда источником является HLS.
     * HLS-компоненты фиксируются, CMYK пересчитывается из RGB.
     */
    private void updateNonHls(int r, int g, int b, int h, int l, int s) {
        if (isUpdating.get()) return;

        isUpdating.set(true);
        try {
            // Обновляем RGB
            rgb[0] = r;
            rgb[1] = g;
            rgb[2] = b;

            // Обновляем RGB компоненты
            for (int i = 0; i < 3; i++) {
                rgbSliders[i].setValue(rgb[i]);
                rgbFields[i].setText(String.valueOf(rgb[i]));
            }

            // Обновляем CMYK компоненты (пересчет из RGB)
            float[] cmyk = rgbToCmyk(r, g, b);
            for (int i = 0; i < 4; i++) {
                int val = Math.round(cmyk[i] * 100);
                cmykSliders[i].setValue(val);
                cmykFields[i].setText(String.valueOf(val));
            }

            // Обновляем HLS компоненты (используем переданные значения)
            hlsSliders[0].setValue(h);
            hlsFields[0].setText(String.valueOf(h));
            hlsSliders[1].setValue(l);
            hlsFields[1].setText(String.valueOf(l));
            hlsSliders[2].setValue(s);
            hlsFields[2].setText(String.valueOf(s));

            // Обновляем панель цвета
            colorPanel.setBackground(new Color(r, g, b));
            colorPanel.repaint();
        } finally {
            isUpdating.set(false);
        }
    }

    private float[] rgbToCmyk(int r, int g, int b) {
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float k = 1.0f - Math.max(Math.max(rf, gf), bf);

        if (k == 1.0f) {
            // Черный цвет
            return new float[]{0, 0, 0, 1};
        }

        float c = (1.0f - rf - k) / (1.0f - k);
        float m = (1.0f - gf - k) / (1.0f - k);
        float y = (1.0f - bf - k) / (1.0f - k);

        // Устранение ошибок с плавающей точкой
        if (Float.isNaN(c) || c < 0) c = 0;
        if (Float.isNaN(m) || m < 0) m = 0;
        if (Float.isNaN(y) || y < 0) y = 0;

        return new float[]{c, m, y, k};
    }

    private Color cmykToRgb(int c, int m, int y, int k) {
        float cf = c / 100.0f;
        float mf = m / 100.0f;
        float yf = y / 100.0f;
        float kf = k / 100.0f;

        // Формула преобразования CMYK to RGB
        float r = (1.0f - cf) * (1.0f - kf);
        float g = (1.0f - mf) * (1.0f - kf);
        float b = (1.0f - yf) * (1.0f - kf);

        // Ограничиваем значения 0-255 и округляем
        int rNew = Math.max(0, Math.min(255, Math.round(r * 255)));
        int gNew = Math.max(0, Math.min(255, Math.round(g * 255)));
        int bNew = Math.max(0, Math.min(255, Math.round(b * 255)));

        return new Color(rNew, gNew, bNew);
    }

    private float[] rgbToHls(int r, int g, int b) {
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(Math.max(rf, gf), bf);
        float min = Math.min(Math.min(rf, gf), bf);
        float h, l, s;

        l = (max + min) / 2.0f;

        if (max == min) {
            h = s = 0;
        } else {
            float delta = max - min;
            // Условие для L: l > 0.5f ? delta / (2.0f - max - min) : delta / (max + min);
            s = (l > 0.5f) ? (delta / (2.0f - (max + min))) : (delta / (max + min));

            if (max == rf) {
                h = (gf - bf) / delta + (gf < bf ? 6.0f : 0.0f);
            } else if (max == gf) {
                h = (bf - rf) / delta + 2.0f;
            } else { // max == bf
                h = (rf - gf) / delta + 4.0f;
            }
            h /= 6.0f;
        }

        return new float[]{h * 360.0f, l, s};
    }

    private Color hlsToRgb(int h, int l, int s) {
        float hf = h / 360.0f;
        float lf = l / 100.0f;
        float sf = s / 100.0f;

        if (sf == 0) {
            // Оттенки серого
            int v = Math.round(lf * 255);
            return new Color(v, v, v);
        }

        float q = lf < 0.5f ? lf * (1.0f + sf) : lf + sf - lf * sf;
        float p = 2.0f * lf - q;

        float r = hue2rgb(p, q, hf + 1.0f / 3.0f);
        float g = hue2rgb(p, q, hf);
        float b = hue2rgb(p, q, hf - 1.0f / 3.0f);

        int rNew = Math.round(r * 255);
        int gNew = Math.round(g * 255);
        int bNew = Math.round(b * 255);

        return new Color(rNew, gNew, bNew);
    }

    private float hue2rgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0f / 6.0f) return p + (q - p) * 6.0f * t;
        if (t < 1.0f / 2.0f) return q;
        if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6.0f;
        return p;
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            if (string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;
            if (text.matches("\\d+")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}