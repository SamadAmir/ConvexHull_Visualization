package com.example.algo_project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BruteForce {
    @FXML
    private Canvas canvas;
    @FXML
    private Label coordinateLabel;

    private GraphicsContext gc;
    private List<Point> points = new ArrayList<>();
    private List<Point> convexHull = new ArrayList<>();
    private List<Point[]> potentialHullLines = new ArrayList<>();

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();

        // Set the background color to black
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Uncomment the following line if you want to read points from the file on application startup.
        readPointsFromFile("points.txt");

        canvas.setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();
            points.add(new Point(x, y));
            drawPoint(x, y);
            updateCoordinateLabel(x, y);
        });
    }

    @FXML
    public void onComputeHullButtonClicked() {
        potentialHullLines.clear();
        computeConvexHull();
        animateHullCreation();
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
                    drawPoint(x, y);
                    // You may want to update the label here as well if needed.
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            // Handle file reading or parsing errors.
        }
    }

    private void computeConvexHull() {
        int n = points.size();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    boolean valid = true;
                    for (int k = 0; k < n; k++) {
                        if (k != i && k != j) {
                            if (isInsideTriangle(points.get(i), points.get(j), points.get(k))) {
                                valid = false;
                                break;
                            }
                        }
                    }
                    if (valid) {
                        potentialHullLines.add(new Point[]{points.get(i), points.get(j)});
                    }
                }
            }
        }
    }

    private boolean isInsideTriangle(Point a, Point b, Point c) {
        // This method should be properly implemented for the convex hull logic.
        return false;
    }

    private void animateHullCreation() {
        Timeline timeline = new Timeline();
        int delay = 0;

        for (Point[] line : potentialHullLines) {
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(delay), e -> {
                        gc.setStroke(Color.WHITE);  // Color for potential hull lines.
                        gc.strokeLine(line[0].getX(), line[0].getY(), line[1].getX(), line[1].getY());
                    })
            );
            delay += 200;  // 200ms delay between each line drawn for demonstration.
        }

        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(delay), e -> drawFinalHull())
        );

        timeline.play();
    }

    private void drawFinalHull() {
        // ... Logic to determine and draw the final convex hull ...
    }

    private void drawPoint(double x, double y) {
        gc.setFill(Color.YELLOW);  // Change color to red for visibility.
        gc.fillOval(x - 5, y - 5, 10, 10);  // Increase point size.
    }

    private void updateCoordinateLabel(double x, double y) {
        coordinateLabel.setText("Point added: p(" + x + ", " + y + ")");
        coordinateLabel.setLayoutX(x + 15); // Adjust this value to set the label's X position relative to the point
        coordinateLabel.setLayoutY(y - 15); // Adjust this value to set the label's Y position relative to the point
    }

    private class Point {
        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}
