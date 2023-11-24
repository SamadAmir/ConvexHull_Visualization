package com.example.algo_project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class GrahamScan {

    @FXML
    private Canvas canvas;
    @FXML
    private Label coordinateLabel;

    private GraphicsContext gc;
    private List<MyPoint2D> points = new ArrayList<>();
    private Stack<MyPoint2D> convexHull = new Stack<>();
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
                    points.add(new MyPoint2D(x, y));
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
        points.add(new MyPoint2D(x, y));

        clearCanvas();
        drawPoints();

        if (!isAnimating) {
            drawConvexHull();
        }

        updateCoordinateLabel(x, y);
    }

    private void computeConvexHull() {
        int n = points.size();
        if (n < 3) {
            convexHull.clear();
            return;
        }

        // Find the point with the minimum y-value (and leftmost if tied)
        MyPoint2D anchor = points.get(0);
        for (int i = 1; i < n; i++) {
            MyPoint2D current = points.get(i);
            if (current.y() < anchor.y() || (current.y() == anchor.y() && current.x() < anchor.x())) {
                anchor = current;
            }
        }

        // Sort the points based on polar angle with respect to the anchor
        points.sort(anchor.POLAR_ORDER);

        convexHull.clear();
        convexHull.push(points.get(0));
        convexHull.push(points.get(1));

        for (int i = 2; i < n; i++) {
            while (convexHull.size() > 1 && MyPoint2D.ccw(convexHull.elementAt(convexHull.size() - 2), convexHull.peek(), points.get(i)) <= 0) {
                convexHull.pop();
            }
            convexHull.push(points.get(i));
        }
    }


    private void animateHullCreation() {
        isAnimating = true;
        Timeline timeline = new Timeline();
        int delay = 0;

        Stack<MyPoint2D> hullPoints = new Stack<>();
        for (MyPoint2D p : convexHull) {
            hullPoints.push(p);
        }

        while (!hullPoints.isEmpty()) {
            MyPoint2D current = hullPoints.pop();
            MyPoint2D next = hullPoints.isEmpty() ? convexHull.firstElement() : hullPoints.peek();

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(delay), e -> drawLine(current, next))
            );

            delay += 1000; // Adjust the delay based on your preference.
        }

        timeline.setOnFinished(e -> isAnimating = false);
        timeline.play();
    }

    private void drawLine(MyPoint2D start, MyPoint2D end) {
        gc.setStroke(Color.YELLOW);
        gc.strokeLine(start.x(), start.y(), end.x(), end.y());
    }

    private void drawPoint(double x, double y, Color color) {
        gc.setFill(color);
        gc.fillOval(x - 5, y - 5, 10, 10);
    }

    private void drawPoints() {
        for (MyPoint2D point : points) {
            drawPoint(point.x(), point.y(), Color.RED);
        }
    }

    private void drawConvexHull() {
        if (!convexHull.isEmpty()) {
            for (int i = 1; i < convexHull.size(); i++) {
                MyPoint2D start = convexHull.get(i - 1);
                MyPoint2D end = convexHull.get(i);
                drawLine(start, end);
            }
            drawLine(convexHull.lastElement(), convexHull.firstElement());
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
}


class MyPoint2D implements Comparable<MyPoint2D> {

    public static final Comparator<MyPoint2D> X_ORDER = Comparator.comparingDouble(MyPoint2D::x).thenComparingDouble(MyPoint2D::y);
    public static final Comparator<MyPoint2D> Y_ORDER = Comparator.comparingDouble(MyPoint2D::y).thenComparingDouble(MyPoint2D::x);
    public final Comparator<MyPoint2D> POLAR_ORDER = new PolarOrder();


    private final double x;
    private final double y;

    public MyPoint2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public static int ccw(MyPoint2D a, MyPoint2D b, MyPoint2D c) {
        double area2 = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
        if (area2 < 0)
            return -1;
        else if (area2 > 0)
            return +1;
        else
            return 0;
    }

    @Override
    public int compareTo(MyPoint2D that) {
        if (this.y < that.y)
            return -1;
        if (this.y > that.y)
            return +1;
        if (this.x < that.x)
            return -1;
        if (this.x > that.x)
            return +1;
        return 0;
    }

    private class PolarOrder implements Comparator<MyPoint2D> {
        public int compare(MyPoint2D q1, MyPoint2D q2) {
            double angle1 = angleTo(q1);
            double angle2 = angleTo(q2);
            if (angle1 < angle2)
                return -1;
            else if (angle1 > angle2)
                return +1;
            else
                return 0;
        }
    }

    private double angleTo(MyPoint2D that) {
        double dx = that.x - this.x;
        double dy = that.y - this.y;
        return Math.atan2(dy, dx);
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null)
            return false;
        if (other.getClass() != this.getClass())
            return false;
        MyPoint2D that = (MyPoint2D) other;
        return this.x == that.x && this.y == that.y;
    }
}
