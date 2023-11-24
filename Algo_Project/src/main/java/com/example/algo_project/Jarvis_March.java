package com.example.algo_project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Jarvis_March {
    @FXML
    private Canvas canvas;
    @FXML
    private Label coordinateLabel;

    private GraphicsContext gc;
    private List<Point> points = new ArrayList<>();
    private List<Point> convexHull = new ArrayList<>();
    private List<Line> allLines = new ArrayList<>();
    private boolean isAnimating = false;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();

        // Set the background color to black
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        readPointsFromFile("points.txt");
    }
    private void readPointsFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] coordinates = line.split(",");
                if (coordinates.length == 2) {
                    double x = Double.parseDouble(coordinates[0]);
                    double y = Double.parseDouble(coordinates[1]);
                    points.add(new Point(x, y));
                    drawPoint(x, y, Color.RED);
                    updateCoordinateLabel(x, y);
                }
            }

            // Draw the convex hull after reading points
            if (!isAnimating) {
                drawConvexHull();
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            // Handle file reading or parsing errors.
        }
    }

    @FXML
    public void onComputeHullButtonClicked() {
        if (!isAnimating) {
            computeConvexHull();
            animateHullCreation();
        }
    }

    @FXML
    public void onCanvasMouseClicked(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        points.add(new Point(x, y));

        clearCanvas();
        drawLines(allLines);
        drawPoints();

        if (!isAnimating) {
            drawConvexHull();
        }

        updateCoordinateLabel(x, y);
    }

    private void computeConvexHull() {
        int n = points.size();
        if (n < 3) {
            // Convex hull is not possible with less than 3 points
            convexHull.clear();
            allLines.clear();
            return;
        }

        convexHull.clear();

        int leftmostIndex = findLeftmostPoint();
        int current = leftmostIndex;
        int next;

        do {
            convexHull.add(points.get(current));
            next = (current + 1) % n;

            for (int i = 0; i < n; i++) {
                if (orientation(points.get(current), points.get(i), points.get(next)) == 2) {
                    next = i;
                }
            }

            current = next;

        } while (current != leftmostIndex);

        convexHull.add(points.get(leftmostIndex));
        updateLines();
    }

    private void updateLines() {
        allLines.clear();

        for (int i = 0; i < convexHull.size() - 1; i++) {
            allLines.add(new Line(convexHull.get(i), convexHull.get(i + 1)));
        }
    }

    private void animateHullCreation() {
        isAnimating = true;
        Timeline timeline = new Timeline();
        int delay = 0;

        for (int i = 0; i < allLines.size(); i++) {
            final int current = i;

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(delay), e -> drawLine(allLines.get(current)))
            );
            delay += 1000;  // Adjust the delay based on your preference.
        }

        timeline.setOnFinished(e -> isAnimating = false);
        timeline.play();
    }

    private void drawLine(Line line) {
        gc.setStroke(Color.YELLOW);
        gc.strokeLine(line.start.x, line.start.y, line.end.x, line.end.y);
    }

    private void drawPoint(double x, double y, Color color) {
        gc.setFill(color);
        gc.fillOval(x - 5, y - 5, 10, 10);
    }

    private void drawPoints() {
        for (Point point : points) {
            drawPoint(point.x, point.y, Color.RED);
        }
    }

    private void drawLines(List<Line> lines) {
        for (Line line : lines) {
            drawLine(line);
        }
    }

    private void drawConvexHull() {
        for (Line line : allLines) {
            gc.setStroke(Color.WHITE);
            gc.strokeLine(line.start.x, line.start.y, line.end.x, line.end.y);
        }
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updateCoordinateLabel(double x, double y) {
        coordinateLabel.setText("Point added: p(" + x + ", " + y + ")");
        coordinateLabel.setLayoutX(x + 15);
        coordinateLabel.setLayoutY(y - 15);
    }

    private int orientation(Point p, Point q, Point r) {
        double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0) return 0;  // colinear
        return (val > 0) ? 1 : 2; // clock or counterclockwise
    }

    private int findLeftmostPoint() {
        int leftmostIndex = 0;
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).x < points.get(leftmostIndex).x ||
                    (points.get(i).x == points.get(leftmostIndex).x && points.get(i).y < points.get(leftmostIndex).y)) {
                leftmostIndex = i;
            }
        }
        return leftmostIndex;
    }

    private static class Point {
        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Line {
        public Point start;
        public Point end;

        public Line(Point start, Point end) {
            this.start = start;
            this.end = end;
        }
    }
}
