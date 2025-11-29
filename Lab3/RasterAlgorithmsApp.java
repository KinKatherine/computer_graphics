import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class RasterAlgorithmsApp extends JFrame {

    private DrawingPanel drawingPanel;

    private JTextField x1Field, y1Field, x2Field, y2Field, radiusField;
    private JTextArea logArea;
    private JSpinner scaleSpinner;

    public RasterAlgorithmsApp() {
        setTitle("Лабораторная работа №3 (Полная версия)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel controlsPanel = new JPanel(new GridLayout(2, 1));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("X1:")); x1Field = new JTextField("1", 3); inputPanel.add(x1Field);
        inputPanel.add(new JLabel("Y1:")); y1Field = new JTextField("1", 3); inputPanel.add(y1Field);
        inputPanel.add(new JLabel("X2:")); x2Field = new JTextField("2", 3); inputPanel.add(x2Field);
        inputPanel.add(new JLabel("Y2:")); y2Field = new JTextField("5", 3); inputPanel.add(y2Field);
        inputPanel.add(new JLabel("R:")); radiusField = new JTextField("5", 3); inputPanel.add(radiusField);

        inputPanel.add(new JLabel("Масштаб:"));
        scaleSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 150, 1));
        scaleSpinner.addChangeListener(e -> {
            drawingPanel.setScale((Integer) scaleSpinner.getValue());
            drawingPanel.repaint();
        });
        inputPanel.add(scaleSpinner);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnStep = new JButton("Пошаговый");
        JButton btnDDA = new JButton("ЦДА");
        JButton btnBresLine = new JButton("Брезенхем");
        JButton btnBresCircle = new JButton("Круг");
        JButton btnCastle = new JButton("Кастл-Питвей");
        JButton btnWu = new JButton("Ву (Сглаживание)");

        JButton btnClear = new JButton("Очистить");
        btnClear.setBackground(new Color(255, 200, 200));

        buttonPanel.add(btnStep);
        buttonPanel.add(btnDDA);
        buttonPanel.add(btnBresLine);
        buttonPanel.add(btnCastle);
        buttonPanel.add(btnWu);
        buttonPanel.add(btnBresCircle);
        buttonPanel.add(btnClear);

        controlsPanel.add(inputPanel);
        controlsPanel.add(buttonPanel);
        add(controlsPanel, BorderLayout.NORTH);

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        logArea = new JTextArea(5, 20);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(logScroll, BorderLayout.SOUTH);

        btnStep.addActionListener(e -> runAlgorithm("Пошаговый", this::stepByStepAlgorithm));
        btnDDA.addActionListener(e -> runAlgorithm("ЦДА", this::ddaAlgorithm));
        btnBresLine.addActionListener(e -> runAlgorithm("Брезенхем", this::bresenhamLineAlgorithm));
        btnBresCircle.addActionListener(e -> runAlgorithm("Круг Брезенхема", this::bresenhamCircleAlgorithm));
        btnCastle.addActionListener(e -> runAlgorithm("Кастл-Питвей", this::castlePittewayAlgorithm));
        btnWu.addActionListener(e -> runAlgorithm("Алгоритм Ву", this::wuLineAlgorithm));

        btnClear.addActionListener(e -> {
            drawingPanel.clearPoints();
            logArea.setText("");
        });
    }

    private void runAlgorithm(String name, Runnable algorithm) {
        drawingPanel.clearPoints();
        long startTime = System.nanoTime();
        try {
            algorithm.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
            return;
        }
        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;
        drawingPanel.repaint();
        logArea.append(String.format("%s: %.4f мс\n", name, durationMs));
    }

    private void stepByStepAlgorithm() {
        int x1 = Integer.parseInt(x1Field.getText());
        int y1 = Integer.parseInt(y1Field.getText());
        int x2 = Integer.parseInt(x2Field.getText());
        int y2 = Integer.parseInt(y2Field.getText());

        int dx = x2 - x1;
        int dy = y2 - y1;


        if (Math.abs(dx) == 0 && Math.abs(dy) == 0) {
            drawingPanel.addPoint(x1, y1, 1.0f);
            return;
        }


        if (Math.abs(dx) >= Math.abs(dy)) {

            if (x1 > x2) { int t = x1; x1 = x2; x2 = t; t = y1; y1 = y2; y2 = t; }

            double k = (double)(y2 - y1) / (x2 - x1);
            double b = y1 - k * x1;

            for (int x = x1; x <= x2; x++) {
                int y = (int) Math.round(k * x + b);
                drawingPanel.addPoint(x, y, 1.0f);
            }
        } else {

            if (y1 > y2) { int t = x1; x1 = x2; x2 = t; t = y1; y1 = y2; y2 = t; }


            double kInv = (double)(x2 - x1) / (y2 - y1);

            for (int y = y1; y <= y2; y++) {
                int x = (int) Math.round(x1 + kInv * (y - y1));
                drawingPanel.addPoint(x, y, 1.0f);
            }
        }
    }


    private void ddaAlgorithm() {
        int x1 = Integer.parseInt(x1Field.getText());
        int y1 = Integer.parseInt(y1Field.getText());
        int x2 = Integer.parseInt(x2Field.getText());
        int y2 = Integer.parseInt(y2Field.getText());
        double dx = x2 - x1;
        double dy = y2 - y1;
        double steps = Math.max(Math.abs(dx), Math.abs(dy));
        double xInc = dx / steps;
        double yInc = dy / steps;
        double x = x1, y = y1;
        for (int i = 0; i <= steps; i++) {
            drawingPanel.addPoint((int) Math.round(x), (int) Math.round(y), 1.0f);
            x += xInc; y += yInc;
        }
    }

    private void bresenhamLineAlgorithm() {
        int x1 = Integer.parseInt(x1Field.getText());
        int y1 = Integer.parseInt(y1Field.getText());
        int x2 = Integer.parseInt(x2Field.getText());
        int y2 = Integer.parseInt(y2Field.getText());
        int w = x2 - x1; int h = y2 - y1;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        if (w < 0) dx1 = -1; else if (w > 0) dx1 = 1;
        if (h < 0) dy1 = -1; else if (h > 0) dy1 = 1;
        if (w < 0) dx2 = -1; else if (w > 0) dx2 = 1;
        int longest = Math.abs(w); int shortest = Math.abs(h);
        if (!(longest > shortest)) {
            longest = Math.abs(h); shortest = Math.abs(w);
            if (h < 0) dy2 = -1; else if (h > 0) dy2 = 1;
            dx2 = 0;
        }
        int numerator = longest >> 1;
        int x = x1; int y = y1;
        for (int i = 0; i <= longest; i++) {
            drawingPanel.addPoint(x, y, 1.0f);
            numerator += shortest;
            if (!(numerator < longest)) {
                numerator -= longest; x += dx1; y += dy1;
            } else {
                x += dx2; y += dy2;
            }
        }
    }

    private void castlePittewayAlgorithm() {
        int x1 = Integer.parseInt(x1Field.getText());
        int y1 = Integer.parseInt(y1Field.getText());
        int x2 = Integer.parseInt(x2Field.getText());
        int y2 = Integer.parseInt(y2Field.getText());

        int x = x1, y = y1;
        int dx = x2 - x1, dy = y2 - y1;
        int sx = (dx > 0) ? 1 : -1;
        int sy = (dy > 0) ? 1 : -1;
        dx = Math.abs(dx); dy = Math.abs(dy);

        boolean steep = dy > dx;
        if (steep) { int t = dx; dx = dy; dy = t; }

        int d = 2 * dy - dx;
        int d1 = 2 * dy;
        int d2 = 2 * (dy - dx);

        for (int i = 0; i <= dx; i++) {
            drawingPanel.addPoint(x, y, 1.0f);
            if (d >= 0) {
                if (steep) x += sx; else y += sy;
                d += d2;
            } else {
                d += d1;
            }
            if (steep) y += sy; else x += sx;
        }
    }

    private void wuLineAlgorithm() {
        int x1 = Integer.parseInt(x1Field.getText());
        int y1 = Integer.parseInt(y1Field.getText());
        int x2 = Integer.parseInt(x2Field.getText());
        int y2 = Integer.parseInt(y2Field.getText());

        boolean steep = Math.abs(y2 - y1) > Math.abs(x2 - x1);
        if (steep) {
            int t; t=x1; x1=y1; y1=t; t=x2; x2=y2; y2=t;
        }
        if (x1 > x2) {
            int t; t=x1; x1=x2; x2=t; t=y1; y1=y2; y2=t;
        }

        double dx = x2 - x1;
        double dy = y2 - y1;
        double gradient = dy / dx;
        if (dx == 0.0) gradient = 1.0;

        // Первая точка
        int xend = x1;
        double yend = y1 + gradient * (xend - x1);
        double xgap = 1.0;
        int xpxl1 = xend;
        int ypxl1 = (int) yend;

        if (steep) {
            drawingPanel.addPoint(ypxl1, xpxl1, (float)((1 - (yend - (int) yend)) * xgap));
            drawingPanel.addPoint(ypxl1 + 1, xpxl1, (float)((yend - (int) yend) * xgap));
        } else {
            drawingPanel.addPoint(xpxl1, ypxl1, (float)((1 - (yend - (int) yend)) * xgap));
            drawingPanel.addPoint(xpxl1, ypxl1 + 1, (float)((yend - (int) yend) * xgap));
        }
        double intery = yend + gradient;

        int xpxl2 = x2;


        for (int x = xpxl1 + 1; x <= xpxl2; x++) {
            if (steep) {
                drawingPanel.addPoint((int)intery, x, (float)(1 - (intery - (int)intery)));
                drawingPanel.addPoint((int)intery + 1, x, (float)(intery - (int)intery));
            } else {
                drawingPanel.addPoint(x, (int)intery, (float)(1 - (intery - (int)intery)));
                drawingPanel.addPoint(x, (int)intery + 1, (float)(intery - (int)intery));
            }
            intery += gradient;
        }
    }

    private void bresenhamCircleAlgorithm() {
        int xc = Integer.parseInt(x1Field.getText());
        int yc = Integer.parseInt(y1Field.getText());
        int r = Integer.parseInt(radiusField.getText());
        int x = 0, y = r, d = 3 - 2 * r;
        drawCirclePoints(xc, yc, x, y);
        while (y >= x) {
            x++;
            if (d > 0) { y--; d = d + 4 * (x - y) + 10; }
            else { d = d + 4 * x + 6; }
            drawCirclePoints(xc, yc, x, y);
        }
    }
    private void drawCirclePoints(int xc, int yc, int x, int y) {
        drawingPanel.addPoint(xc + x, yc + y, 1.0f); drawingPanel.addPoint(xc - x, yc + y, 1.0f);
        drawingPanel.addPoint(xc + x, yc - y, 1.0f); drawingPanel.addPoint(xc - x, yc - y, 1.0f);
        drawingPanel.addPoint(xc + y, yc + x, 1.0f); drawingPanel.addPoint(xc - y, yc + x, 1.0f);
        drawingPanel.addPoint(xc + y, yc - x, 1.0f); drawingPanel.addPoint(xc - y, yc - x, 1.0f);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RasterAlgorithmsApp().setVisible(true));
    }


    static class GridPoint {
        int x, y;
        float intensity;
        public GridPoint(int x, int y, float intensity) {
            this.x = x; this.y = y; this.intensity = intensity;
        }
    }

    class DrawingPanel extends JPanel {
        private int scale = 30;
        private final List<GridPoint> points = new ArrayList<>();

        public DrawingPanel() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int gridX = (e.getX() - centerX) / scale;
                    if (e.getX() < centerX) gridX = (e.getX() - centerX - scale) / scale;
                    int gridY = (centerY - e.getY()) / scale;
                    if (e.getY() > centerY) gridY = (centerY - e.getY() - scale) / scale;
                    System.out.println("Клик по сетке: " + gridX + ", " + gridY);
                }
            });
        }

        public void setScale(int s) { this.scale = s; }
        public void addPoint(int x, int y, float intensity) {
            points.add(new GridPoint(x, y, intensity));
        }
        public void clearPoints() { points.clear(); repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(); int h = getHeight();
            int centerX = (w / 2 / scale) * scale;
            int centerY = (h / 2 / scale) * scale;


            g2.setColor(new Color(235, 235, 235));
            for (int x = centerX; x < w; x += scale) g2.drawLine(x, 0, x, h);
            for (int x = centerX; x > 0; x -= scale) g2.drawLine(x, 0, x, h);
            for (int y = centerY; y < h; y += scale) g2.drawLine(0, y, w, y);
            for (int y = centerY; y > 0; y -= scale) g2.drawLine(0, y, w, y);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(0, centerY, w, centerY);
            g2.drawLine(centerX, 0, centerX, h);
            g2.drawString("0", centerX + 5, centerY + 15);

            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.setColor(Color.DARK_GRAY);
            for (int i = 1; i * scale < w / 2; i++) {
                if (i % 2 != 0 && scale < 20) continue;
                g2.drawString(String.valueOf(i), centerX + i * scale - 3, centerY + 15);
                g2.drawString(String.valueOf(i), centerX + 5, centerY - i * scale + 5);
            }

            for (GridPoint p : points) {
                int screenX = centerX + p.x * scale;
                int screenY = centerY - p.y * scale - scale;
                int alpha = Math.max(0, Math.min(255, (int)(p.intensity * 255)));
                g2.setColor(new Color(255, 0, 0, alpha));
                g2.fillRect(screenX + 1, screenY + 1, scale - 1, scale - 1);
            }
        }
    }
}