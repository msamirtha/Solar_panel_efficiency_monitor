
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SolarMonitor extends JFrame {

    JComboBox<String> panelType, climate;
    JTextField capacity, age, temperature, sunlight, cleaningDate;
    JTextArea result;

    public SolarMonitor() {
        setTitle("Solar Panel Efficiency & Output Monitor");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridLayout(0, 2, 10, 10));
        main.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        panelType = new JComboBox<>(new String[]{
            "Monocrystalline", "Polycrystalline", "Thin Film"
        });

        climate = new JComboBox<>(new String[]{
            "Sunny", "Cloudy", "Rainy"
        });

        capacity = new JTextField("5");
        age = new JTextField("2");
        temperature = new JTextField("30");
        sunlight = new JTextField("6");
        cleaningDate = new JTextField(
            LocalDate.now().minusDays(10).toString()
        );

        addField(main, "Panel Type:", panelType);
        addField(main, "Capacity (kW):", capacity);
        addField(main, "Panel Age (years):", age);
        addField(main, "Temperature (°C):", temperature);
        addField(main, "Sunlight (hours/day):", sunlight);
        addField(main, "Climate:", climate);
        addField(main, "Last Cleaning Date (YYYY-MM-DD):",
                 cleaningDate);

        JButton calculate = new JButton("Calculate Efficiency");
        main.add(calculate);

        JButton clear = new JButton("Clear Results");
        main.add(clear);

        result = new JTextArea(9, 30);
        result.setEditable(false);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setMargin(new Insets(10, 10, 10, 10));

        add(main, BorderLayout.NORTH);
        add(new JScrollPane(result), BorderLayout.CENTER);

        calculate.addActionListener(e -> calculateOutput());
        clear.addActionListener(e -> result.setText(""));
    }

    void addField(JPanel panel, String label, Component field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    void calculateOutput() {
        try {
            double power = Double.parseDouble(capacity.getText().trim());
            double years = Double.parseDouble(age.getText().trim());
            double temp = Double.parseDouble(temperature.getText().trim());
            double hours = Double.parseDouble(sunlight.getText().trim());

            LocalDate lastClean =
                LocalDate.parse(cleaningDate.getText().trim());

            if (!Double.isFinite(power) ||
                !Double.isFinite(years) ||
                !Double.isFinite(temp) ||
                !Double.isFinite(hours)) {
                throw new IllegalArgumentException(
                    "Please enter finite numeric values."
                );
            }

            if (power <= 0 || years < 0 ||
                temp < -50 || temp > 100 ||
                hours < 0 || hours > 24) {
                throw new IllegalArgumentException(
                    "Check capacity, age, temperature and sunlight hours."
                );
            }

            long days = ChronoUnit.DAYS.between(
                lastClean, LocalDate.now()
            );

            if (days < 0) {
                throw new IllegalArgumentException(
                    "Cleaning date cannot be in the future."
                );
            }

            double dirtRate;

            switch ((String) climate.getSelectedItem()) {
                case "Sunny":
                    dirtRate = 2.0;
                    break;
                case "Cloudy":
                    dirtRate = 1.5;
                    break;
                default:
                    dirtRate = 0.5;
            }

            double dirtLoss = Math.min(days * dirtRate, 40);
            double degradation = Math.min(years * 0.5, 20);

            double panelFactor;

            switch ((String) panelType.getSelectedItem()) {
                case "Monocrystalline":
                    panelFactor = 1.0;
                    break;
                case "Polycrystalline":
                    panelFactor = 0.85;
                    break;
                default:
                    panelFactor = 0.75;
            }

            double temperatureLoss =
                Math.max(0, temp - 25) * 0.4;

            double efficiency = Math.max(
                0, 100 - dirtLoss - degradation - temperatureLoss
            );

            double dailyOutput = power * hours * panelFactor *
                (efficiency / 100.0);

            String condition;

            if (efficiency >= 80) {
                condition = "Good";
            } else if (efficiency >= 60) {
                condition = "Moderate";
            } else {
                condition = "Poor";
            }

            String maintenance = days >= 30
                ? "Clean the panel soon."
                : "Continue regular monitoring.";

            result.setText(String.format(
                "SOLAR PANEL REPORT%n" +
                "------------------------------%n" +
                "Panel Type: %s%n" +
                "Days Since Cleaning: %d%n" +
                "Dirt Loss: %.1f%%%n" +
                "Age Degradation: %.1f%%%n" +
                "Temperature Loss: %.1f%%%n" +
                "Estimated Efficiency: %.1f%%%n" +
                "Daily Energy Output: %.2f kWh%n" +
                "Panel Condition: %s%n" +
                "Recommendation: %s%n%n" +
                "Note: This is a simplified estimate.",
                panelType.getSelectedItem(),
                days,
                dirtLoss,
                degradation,
                temperatureLoss,
                efficiency,
                dailyOutput,
                condition,
                maintenance
            ));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter valid numbers."
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Check your inputs: " + ex.getMessage(),
                "Input Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SolarMonitor().setVisible(true);
        });
    }
}
