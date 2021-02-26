import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.List;

public class BubbleChart extends JFrame {
    private final int WIDTH = 4000;
    private final int HEIGHT = 2000;
    private final int RADIUS_SCALE = 200;
    private final int PADDING = 200;
    private String xField;
    private String yField;
    private String sizeField;
    private String dataSource;
    private Map<String, Integer> fields;
    private List<List<String>> data;

    private float minX = Float.MAX_VALUE;
    private float maxX = Float.MIN_VALUE;
    private float minY = Float.MAX_VALUE;
    private float maxY = Float.MIN_VALUE;
    private float minR = Float.MAX_VALUE;
    private float maxR = Float.MIN_VALUE;

    public BubbleChart(String xField, String yField, String sizeField, String dataSource) {
        super("Bubble Chart");
        this.xField = xField;
        this.yField = yField;
        this.sizeField = sizeField;
        this.dataSource = dataSource;
        this.fields = new HashMap<>();
        this.fetchData();

        setSize(WIDTH, HEIGHT);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                dispose();
                System.exit(0);
            }
        });
    }

    private void fetchData() {

        List<List<String>> result = new ArrayList<>();
        try {
            // open file and read in
            File file = new File(this.dataSource);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            // temp vars for reading data in
            String line; // raw data line
            String[] data; // split at ","

            // grab fields list from first line and put them in a hash map so we know what data belongs in what column
            String[] fieldsList = br.readLine().split(",");
            for (int i = 0; i < fieldsList.length; i++) {
                String s = fieldsList[i];
                String sanitized = s.replaceAll("\"", "");
                this.fields.put(sanitized, i);
            }
            // read in the rest of the lines to a list of lists
            while ((line = br.readLine()) != null) {
                data = line.split(",");
                for (int i = 0; i < data.length; i++) {
                    String s = data[i];
                    String sanitized = s.replaceAll("\"", "");
                    data[i] = sanitized;
                }

                List<String> datalist = Arrays.asList(data);
                try {
                    this.minX = Math.min(this.minX, Float.parseFloat(datalist.get(this.fields.get(this.xField))));
                    this.maxX = Math.max(this.maxX, Float.parseFloat(datalist.get(this.fields.get(this.xField))));
                    this.minY = Math.min(this.minY, Float.parseFloat(datalist.get(this.fields.get(this.yField))));
                    this.maxY = Math.max(this.maxY, Float.parseFloat(datalist.get(this.fields.get(this.yField))));
                    this.minR = Math.min(this.minR, Float.parseFloat(datalist.get(this.fields.get(this.sizeField))));
                    this.maxR = Math.max(this.maxR, Float.parseFloat(datalist.get(this.fields.get(this.sizeField))));
                    result.add(datalist);
                } catch (NumberFormatException ignored) {
                    // ignore NA values
                }
            }
            br.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        this.data = result;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        this.draw((Graphics2D) g);
    }

    public void draw(Graphics2D g) {
        Map<String, Color> colors = new HashMap<>();
        colors.put("bmw", this.makeColor(86, 59, 89));
        colors.put("toyota", this.makeColor(86, 59, 89));
        colors.put("ford", this.makeColor(184, 193, 190));
        colors.put("honda", this.makeColor(166, 126, 91));
        colors.put("mercedes", this.makeColor(166, 94, 68));

        // draw

        for (List<String> row : this.data) {
            try {
                float x = Float.parseFloat(row.get(this.fields.get(this.xField)));
                float y = Float.parseFloat(row.get(this.fields.get(this.yField)));
                float r = Float.parseFloat(row.get(this.fields.get(this.sizeField)));
                g.setColor(colors.get(row.get(this.fields.get("Manufacturer"))));
                int ir = this.scaleRadius(r);
                int ix = this.scaleX(x) - ir / 2; // center the circle
                int iy = this.scaleY(y) - ir / 2; // center the circle
                g.fillOval(ix, iy, ir, ir);
            } catch (NumberFormatException ignored) {
                // ignore NAs
            }
        }

        // draw axes

        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont((float) 36));
        g.drawString(this.xField, WIDTH / 2, HEIGHT - PADDING / 2);
        g.translate(PADDING / 2, HEIGHT / 2);
        g.rotate(-Math.toRadians(90));
        g.drawString(this.yField, 0, 0);
        g.rotate(Math.toRadians(90));
        g.translate(-PADDING / 2, -HEIGHT / 2);

        g.drawLine(PADDING, PADDING, PADDING, HEIGHT - PADDING);
        g.drawLine(PADDING, HEIGHT - PADDING, WIDTH - PADDING, HEIGHT - PADDING);

        for (int i = 10; i <= 35; i += 5) {
            int pos = this.scaleY(i);
            g.drawLine(PADDING - 10, pos, PADDING + 10, pos);
            String label = String.valueOf(i);
            g.drawString(label, PADDING - 40, pos);
        }
        for (int i = 2000; i <= 4500; i += 500) {
            int pos = this.scaleX(i);
            g.drawLine(pos, HEIGHT - PADDING + 10, pos, HEIGHT - PADDING - 10);
            String label = String.valueOf(i);
            g.drawString(label, pos, HEIGHT - PADDING + 40);
        }
    }

    private int scaleRadius(float r) {
        double result = r - this.minR;
        result /= this.maxR - this.minR;
        result *= RADIUS_SCALE;
        result += 20; // min size for radius
        return (int) Math.ceil(result);
    }

    private int scaleX(float x) {
        double result = x - this.minX;
        result /= this.maxX - this.minX;
        result *= WIDTH - 2 * PADDING;
        result += PADDING;
        return (int) Math.ceil(result);
    }

    private int scaleY(float y) {
        double result = y - this.minY;
        result /= this.maxY - this.minY;
        result = 1 - result; // flip y axis because pixels are upside down in computer land
        result *= HEIGHT - 2 * PADDING;
        result += PADDING;
        return (int) Math.ceil(result);
    }

    private Color makeColor(int r, int g, int b) {
        return new Color((float) r / 255.0f, (float) g / 255.0f, (float) b / 255.0f, 0.5f);
    }

    @Override
    public String toString() {
        return "BubbleChart{" +
                "xField='" + xField + '\'' +
                ", yField='" + yField + '\'' +
                ", sizeField='" + sizeField + '\'' +
                ", dataSource='" + dataSource + '\'' +
                '}';
    }
}
