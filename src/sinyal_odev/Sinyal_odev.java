/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sinyal_odev;

import cezeri.device.kinect.CizWithKinect;
import static cezeri.factory.FactoryUtils.var;
import cezeri.image_processing.ImageProcess;
import cezeri.machine_learning.extraction.FeatureExtractionPistachio;
import cezeri.matrix.CMatrix;
import cezeri.search.meta_heuristic.simulated_annealing.SimulatedAnnealing;





import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import static java.time.Duration.from;
import java.util.ArrayList;
import static java.util.Date.from;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/*
 *
 * @author BAP1
 */
public class Sinyal_odev {

    private static BufferedImage image;

    int initial_temperature = 100;
    double cooling = 0.8;  //cooling coefficient
    int number_variables = 2;
    int[] upper_bounds = new int[]{3, 3};
    int[] lower_bounds = new int[]{-3, -3};
    int computing_time = 1;  //second(s)
    static JFrame frame;
    static ValueMarker marker;
    static XYPlot plot;
    static JFreeChart chart;

    static int x_size = 1000;

    private static DecimalFormat df2 = new DecimalFormat("#.####");

    public static void main(String[] args) {

        Sinyal_odev obj = new Sinyal_odev();
        CMatrix cm = CMatrix.getInstance().randTimeSeries(x_size, 1, -0.1, 0.1);
        obj.matrix = cm.toDoubleArray1D();
        for (int i = 0; i < obj.matrix.length; i++) {
            double eski = obj.matrix[i];

            obj.matrix[i] = Double.valueOf(String.format("%.5f", eski));
        }

        frame = new JFrame("Simulated Annealing ");
        frame.setSize(1000, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        XYDataset ds = createDataset(obj.matrix);
        chart = ChartFactory.createXYLineChart("Simulated Annealing", "x", "y", ds, PlotOrientation.VERTICAL, true, true, false);
        plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setRangeGridlinesVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        marker = new ValueMarker(0);
        marker.setPaint(Color.yellow);

        marker.setValue(0);
        marker.setLabel("X= " + 0 + " Y= " + 0);

        marker.setLabelAnchor(RectangleAnchor.TOP);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addDomainMarker(0, marker, Layer.FOREGROUND);
        ChartPanel cp = new ChartPanel(chart);

        frame.getContentPane().add(cp);

        obj.SimulatedAnnealingAlgorithm(cm);

    }

    private static XYDataset createDataset(double[] d) {
        DefaultXYDataset ds = new DefaultXYDataset();
        double[][] data = new double[2][x_size];
        for (int i = 0; i < x_size; i++) {
            data[0][i] = i;
            data[1][i] = d[i];
        }

        // double[][] data = { {0.1, 0.2, 0.3}, {1, 2, 3} };
        ds.addSeries("series1", data);

        return ds;
    }

    double[] matrix;
    double temperature, min, max;
    int currentpoint;
    List<Integer> points;

    public void SimulatedAnnealingAlgorithm(CMatrix cm) {

        temperature = 25;
        points = new ArrayList();
        min = cm.getMinTotal();
        max = cm.getMaxTotal();
        matrix = cm.toDoubleArray1D();
        System.out.println("Min = " + min);
        System.out.println("Max = " + max);
        currentpoint = getIndex(min, matrix);

        for (int i = 0; i < matrix.length; i++) {

            points.add(i);

            // System.out.println(i);
        }

        while (true) {

            if (temperature > 0.1) {
                temperature *= 0.99;
            } else {
                System.out.println("temperature 0");
                return;
            }

            double rand = Math.random();
            double sol = 1 / Math.exp(matrix[currentpoint] / temperature);

            // System.out.println(rand);
            // System.out.println(sol);
            if (rand < sol) {
                System.out.println(currentpoint + " " + matrix[currentpoint] + "  " + temperature);
                currentpoint = nextPoint(currentpoint, points, matrix);
                animasiton(currentpoint, matrix[currentpoint]);

            }

            if (points.size() == 0) {
                temperature = 0;
                System.out.println("Point 0");
                return;
            }

        }

    }

    public int getIndex(double val, double[] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == val) {
                return i;
            }
        }
        return 0;
    }

    public int nextPoint(int cuurentPoint, List<Integer> points, double[] matrix) {
        int nextPoint = points.get(new Random().nextInt(points.size()));

        double curent = matrix[cuurentPoint];
        double next = matrix[nextPoint];
        //System.out.println("---------Curent "+curent);
        while (true) {

            nextPoint = points.get(new Random().nextInt(points.size()));

            next = matrix[nextPoint];

            if (curent < next) {
                //System.out.println("Next :  " + next);
                return nextPoint;
            }

        }
        // return nextPoint;
    }

    public static void animasiton(int index, double value) {

        plot.removeDomainMarker(marker);

        marker.setValue(index);
        marker.setLabel("X= " + index + " Y= " + value);

        marker.setLabelAnchor(RectangleAnchor.TOP);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addDomainMarker(0, marker, Layer.FOREGROUND);
        ChartPanel cp = new ChartPanel(chart);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(cp);

    }

}
