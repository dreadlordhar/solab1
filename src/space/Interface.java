package space;

import javax.swing.*;
import java.awt.*;

public class Interface extends JFrame {

    // ==========================================
    // COMPONENTE
    // ==========================================

    private JProgressBar fuelBar;

    private JLabel fuelLabel;

    private JLabel countdownLabel;

    private JLabel oxygenLabel;

    private JLabel statusLabel;

    private JLabel eventLabel;

    private JButton startButton;

    private JButton stopButton;


    // ==========================================
    // TIMER-E
    // ==========================================

    private FuelTimer fuelTimer;

    private Timer rocketAnimationTimer;


    // ==========================================
    // PANOU RACHETĂ
    // ==========================================

    private RocketPanel rocketPanel;


    // ==========================================
    // POZIȚIA RACHETEI
    // ==========================================

    private int rocketY = 160;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public Interface() {

        setTitle(
                "Space Mission"
        );


        setSize(
                750,
                800
        );


        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );


        setLocationRelativeTo(
                null
        );


        createInterface();
    }


    // ==========================================
    // CREAREA INTERFEȚEI
    // ==========================================

    private void createInterface() {

        JPanel mainPanel =
                new JPanel();


        mainPanel.setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );


        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        30,
                        20,
                        30
                )
        );


        // ==========================================
        // TITLU
        // ==========================================

        JLabel titleLabel =
                new JLabel(
                        "SPACE MISSION",
                        SwingConstants.CENTER
                );


        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        32
                )
        );


        mainPanel.add(
                titleLabel,
                BorderLayout.NORTH
        );


        // ==========================================
        // ZONA DE LANSARE
        // ==========================================

        rocketPanel =
                new RocketPanel();


        rocketPanel.setLayout(
                null
        );


        rocketPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "LAUNCH AREA"
                )
        );


        // ==========================================
        // COUNTDOWN
        // ==========================================

        countdownLabel =
                new JLabel(
                        "00:10.00",
                        SwingConstants.CENTER
                );


        countdownLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        32
                )
        );


        countdownLabel.setOpaque(
                true
        );


        countdownLabel.setBackground(
                Color.WHITE
        );


        countdownLabel.setBorder(
                BorderFactory.createLineBorder(
                        Color.BLACK,
                        2
                )
        );


        countdownLabel.setBounds(
                255,
                20,
                230,
                60
        );


        rocketPanel.add(
                countdownLabel
        );


        mainPanel.add(
                rocketPanel,
                BorderLayout.CENTER
        );


        // ==========================================
        // PANOU INFORMAȚII
        // ==========================================

        JPanel infoPanel =
                new JPanel();


        infoPanel.setLayout(
                new BoxLayout(
                        infoPanel,
                        BoxLayout.Y_AXIS
                )
        );


        // ==========================================
        // FUEL
        // ==========================================

        JLabel fuelTitle =
                new JLabel(
                        "FUEL"
                );


        fuelTitle.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );


        fuelTitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        infoPanel.add(
                fuelTitle
        );


        fuelBar =
                new JProgressBar(
                        0,
                        100
                );


        fuelBar.setValue(
                100
        );


        fuelBar.setStringPainted(
                true
        );


        fuelBar.setMaximumSize(
                new Dimension(
                        560,
                        28
                )
        );


        fuelBar.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        infoPanel.add(
                fuelBar
        );


        fuelLabel =
                new JLabel(
                        "..."
                );


        fuelLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );


        fuelLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        infoPanel.add(
                fuelLabel
        );


        infoPanel.add(
                Box.createVerticalStrut(
                        10
                )
        );


        // ==========================================
        // OXIGEN
        // ==========================================

        oxygenLabel =
                new JLabel(
                        "OXYGEN: 30 seconds"
                );


        oxygenLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        17
                )
        );


        oxygenLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        infoPanel.add(
                oxygenLabel
        );


        infoPanel.add(
                Box.createVerticalStrut(
                        8
                )
        );


        // ==========================================
        // STATUS
        // ==========================================

        statusLabel =
                new JLabel(
                        "READY FOR LAUNCH",
                        SwingConstants.CENTER
                );


        statusLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );


        statusLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        infoPanel.add(
                statusLabel
        );


        infoPanel.add(
                Box.createVerticalStrut(
                        5
                )
        );


        // ==========================================
        // EVENIMENT
        // ==========================================

        eventLabel =
                new JLabel(
                        "Waiting for space event...",
                        SwingConstants.CENTER
                );


        eventLabel.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        15
                )
        );


        eventLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        infoPanel.add(
                eventLabel
        );


        infoPanel.add(
                Box.createVerticalStrut(
                        10
                )
        );


        // ==========================================
        // BUTOANE
        // ==========================================

        JPanel buttonPanel =
                new JPanel();


        startButton =
                new JButton(
                        "START MISSION"
                );


        stopButton =
                new JButton(
                        "STOP"
                );


        buttonPanel.add(
                startButton
        );


        buttonPanel.add(
                stopButton
        );


        infoPanel.add(
                buttonPanel
        );


        mainPanel.add(
                infoPanel,
                BorderLayout.SOUTH
        );


        setContentPane(
                mainPanel
        );


        // ==========================================
        // BUTON START
        // ==========================================

        startButton.addActionListener(
                e -> startMission()
        );


        // ==========================================
        // BUTON STOP
        // ==========================================

        stopButton.addActionListener(
                e -> stopMission()
        );
    }


    // ==========================================
    // START MISSION
    // ==========================================

    private void startMission() {

        // Oprim animația veche

        if (rocketAnimationTimer != null) {

            rocketAnimationTimer.stop();
        }


        // Resetăm racheta

        rocketY = 160;


        rocketPanel.setRocketY(
                rocketY
        );


        // Resetăm informațiile

        countdownLabel.setText(
                "00:10.00"
        );


        fuelBar.setValue(
                100
        );


        fuelLabel.setText(
                "100%"
        );


        oxygenLabel.setText(
                "OXYGEN: 30 seconds"
        );


        statusLabel.setText(
                "COUNTDOWN STARTED..."
        );


        eventLabel.setText(
                "Preparing rocket for launch..."
        );


        startButton.setEnabled(
                false
        );


        // ==========================================
        // CREARE FUEL TIMER
        // ==========================================

        fuelTimer =
                new FuelTimer(

                        // ------------------------------
                        // COUNTDOWN
                        // ------------------------------

                        remaining ->
                                SwingUtilities.invokeLater(
                                        () -> {

                                            countdownLabel.setText(
                                                    formatTime(
                                                            remaining
                                                    )
                                            );
                                        }
                                ),


                        // ------------------------------
                        // FUEL
                        // ------------------------------

                        fuel ->
                                SwingUtilities.invokeLater(
                                        () -> {

                                            fuelBar.setValue(
                                                    fuel
                                            );


                                            fuelLabel.setText(
                                                    fuel + "%"
                                            );
                                        }
                                ),


                        // ------------------------------
                        // OXIGEN
                        // ------------------------------

                        oxygen ->
                                SwingUtilities.invokeLater(
                                        () -> {

                                            oxygenLabel.setText(
                                                    "OXYGEN: "
                                                            + oxygen
                                                            + " seconds"
                                            );
                                        }
                                ),


                        // ==================================
                        // LANSARE
                        // ==================================

                        () ->
                                SwingUtilities.invokeLater(
                                        () -> {

                                            countdownLabel.setText(
                                                    "00:00.00"
                                            );


                                            statusLabel.setText(
                                                    "ROCKET LAUNCHED!"
                                            );


                                            eventLabel.setText(
                                                    "Rocket is taking off!"
                                            );


                                            startRocketAnimation();
                                        }
                                ),


                        // ==================================
                        // SATELIT
                        // ==================================

                        () ->
                                SwingUtilities.invokeLater(
                                        () -> {

                                            eventLabel.setText(
                                                    "Satellite separation!"
                                            );


                                            statusLabel.setText(
                                                    "SATELLITE SEPARATED"
                                            );
                                        }
                                ),


                        // ==================================
                        // OXIGEN TERMINAT
                        // ==================================

                        () ->
                                SwingUtilities.invokeLater(
                                        () -> {

                                            statusLabel.setText(
                                                    "OXYGEN DEPLETED!"
                                            );


                                            eventLabel.setText(
                                                    "MISSION FAILED"
                                            );
                                        }
                                )
                );


        // Pornim timerul

        fuelTimer.start();
    }


    // ==========================================
    // FORMAT COUNTDOWN
    // ==========================================

    private String formatTime(
            long milliseconds
    ) {

        if (milliseconds < 0) {

            milliseconds = 0;
        }


        long seconds =
                milliseconds / 1000;


        long millis =
                milliseconds % 1000;


        return String.format(
                "00:%02d.%02d",
                seconds,
                millis / 10
        );
    }


    // ==========================================
    // ANIMAȚIA RACHETEI
    // ==========================================

    private void startRocketAnimation() {

        if (rocketAnimationTimer != null) {

            rocketAnimationTimer.stop();
        }


        rocketY = 160;


        rocketAnimationTimer =
                new Timer(
                        30,
                        e -> {

                            // Racheta urcă

                            rocketY -= 5;


                            rocketPanel.setRocketY(
                                    rocketY
                            );


                            // Când iese din ecran

                            if (rocketY < -130) {

                                rocketAnimationTimer.stop();


                                rocketY = 160;


                                rocketPanel.setRocketY(
                                        rocketY
                                );
                            }
                        }
                );


        rocketAnimationTimer.start();
    }


    // ==========================================
    // STOP MISSION
    // ==========================================

    private void stopMission() {

        if (fuelTimer != null) {

            fuelTimer.stop();
        }


        if (rocketAnimationTimer != null) {

            rocketAnimationTimer.stop();
        }


        // Resetăm racheta

        rocketY = 160;


        rocketPanel.setRocketY(
                rocketY
        );


        // Resetăm interfața

        countdownLabel.setText(
                "00:10.00"
        );


        fuelBar.setValue(
                100
        );


        fuelLabel.setText(
                "100%"
        );


        oxygenLabel.setText(
                "OXYGEN: 30 seconds"
        );


        statusLabel.setText(
                "MISSION STOPPED"
        );


        eventLabel.setText(
                "Mission stopped."
        );


        startButton.setEnabled(
                true
        );
    }


    // =====================================================
    // PANOU PENTRU SCENA SPAȚIALĂ
    // =====================================================

    private static class RocketPanel
            extends JPanel {


        private int rocketY = 160;


        public RocketPanel() {

            setBackground(
                    new Color(
                            10,
                            15,
                            35
                    )
            );
        }


        // ==========================================
        // POZIȚIA RACHETEI
        // ==========================================

        public void setRocketY(
                int y
        ) {

            rocketY = y;

            repaint();
        }


        // ==========================================
        // DESENAREA SCENEI
        // ==========================================

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);


            Graphics2D g2 =
                    (Graphics2D) g.create();


            // Antialiasing

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            // ==========================================
            // FUNDAL
            // ==========================================

            g2.setColor(
                    new Color(
                            10,
                            15,
                            35
                    )
            );


            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );


            // ==========================================
            // STELE
            // ==========================================

            drawStar(
                    g2,
                    70,
                    80,
                    4
            );


            drawStar(
                    g2,
                    130,
                    120,
                    3
            );


            drawStar(
                    g2,
                    190,
                    60,
                    4
            );


            drawStar(
                    g2,
                    520,
                    70,
                    4
            );


            drawStar(
                    g2,
                    600,
                    120,
                    3
            );


            drawStar(
                    g2,
                    660,
                    55,
                    4
            );


            drawStar(
                    g2,
                    90,
                    200,
                    3
            );


            drawStar(
                    g2,
                    570,
                    200,
                    3
            );


            drawStar(
                    g2,
                    250,
                    150,
                    2
            );


            drawStar(
                    g2,
                    470,
                    150,
                    2
            );


            // ==========================================
            // LUNA
            // ==========================================

            g2.setColor(
                    new Color(
                            220,
                            220,
                            210
                    )
            );


            g2.fillOval(
                    540,
                    35,
                    70,
                    70
            );


            // Cratere

            g2.setColor(
                    new Color(
                            180,
                            180,
                            175
                    )
            );


            g2.fillOval(
                    555,
                    50,
                    12,
                    8
            );


            g2.fillOval(
                    580,
                    40,
                    10,
                    12
            );


            g2.fillOval(
                    570,
                    75,
                    15,
                    9
            );


            // ==========================================
            // PLANETĂ
            // ==========================================

            g2.setColor(
                    new Color(
                            40,
                            100,
                            180
                    )
            );


            g2.fillOval(
                    25,
                    160,
                    70,
                    70
            );


            // Atmosferă

            g2.setColor(
                    new Color(
                            100,
                            180,
                            240,
                            100
                    )
            );


            g2.drawOval(
                    20,
                    155,
                    80,
                    80
            );


            // ==========================================
            // PLATFORMĂ
            // ==========================================

            int groundY =
                    getHeight() - 55;


            g2.setColor(
                    new Color(
                            80,
                            85,
                            95
                    )
            );


            g2.fillRect(
                    120,
                    groundY,
                    480,
                    12
            );


            // Detalii platformă

            g2.setColor(
                    new Color(
                            170,
                            170,
                            175
                    )
            );


            for (
                    int i = 0;
                    i < 8;
                    i++
            ) {

                g2.fillRect(
                        135 + i * 55,
                        groundY + 2,
                        25,
                        5
                );
            }


            // ==========================================
            // TURN DE LANSARE
            // ==========================================

            int towerX =
                    getWidth() / 2;


            g2.setColor(
                    new Color(
                            120,
                            125,
                            135
                    )
            );


            // Stâlp stânga

            g2.fillRect(
                    towerX - 80,
                    groundY - 190,
                    8,
                    190
            );


            // Stâlp dreapta

            g2.fillRect(
                    towerX + 72,
                    groundY - 190,
                    8,
                    190
            );


            // Bare orizontale

            for (
                    int y = groundY - 170;
                    y < groundY;
                    y += 30
            ) {

                g2.fillRect(
                        towerX - 80,
                        y,
                        160,
                        4
                );
            }


            // Bare diagonale

            g2.setStroke(
                    new BasicStroke(
                            2
                    )
            );


            for (
                    int y = groundY - 160;
                    y < groundY - 20;
                    y += 30
            ) {

                g2.drawLine(
                        towerX - 75,
                        y,
                        towerX + 72,
                        y + 30
                );


                g2.drawLine(
                        towerX + 72,
                        y,
                        towerX - 75,
                        y + 30
                );
            }


            // ==========================================
            // RACHETA
            // ==========================================

            int centerX =
                    getWidth() / 2;


            int x =
                    centerX - 27;


            int y =
                    rocketY;


            // ==========================================
            // FUM
            // ==========================================

            if (rocketY < 150) {

                drawSmoke(
                        g2,
                        centerX,
                        y + 110
                );
            }


            // ==========================================
            // CORP
            // ==========================================

            g2.setColor(
                    new Color(
                            220,
                            225,
                            230
                    )
            );


            g2.fillRoundRect(
                    x,
                    y + 25,
                    54,
                    85,
                    27,
                    27
            );


            // ==========================================
            // VÂRF
            // ==========================================

            Polygon nose =
                    new Polygon();


            nose.addPoint(
                    centerX,
                    y
            );


            nose.addPoint(
                    x,
                    y + 35
            );


            nose.addPoint(
                    x + 54,
                    y + 35
            );


            g2.setColor(
                    new Color(
                            210,
                            45,
                            45
                    )
            );


            g2.fillPolygon(
                    nose
            );


            // ==========================================
            // BANDĂ ROȘIE
            // ==========================================

            g2.setColor(
                    new Color(
                            200,
                            45,
                            45
                    )
            );


            g2.fillRect(
                    x,
                    y + 65,
                    54,
                    10
            );


            // ==========================================
            // FEREASTRĂ
            // ==========================================

            g2.setColor(
                    new Color(
                            40,
                            130,
                            200
                    )
            );


            g2.fillOval(
                    centerX - 11,
                    y + 42,
                    22,
                    22
            );


            g2.setColor(
                    Color.WHITE
            );


            g2.drawOval(
                    centerX - 11,
                    y + 42,
                    22,
                    22
            );


            // ==========================================
            // ARIPA STÂNGĂ
            // ==========================================

            Polygon leftWing =
                    new Polygon();


            leftWing.addPoint(
                    x,
                    y + 75
            );


            leftWing.addPoint(
                    x - 22,
                    y + 112
            );


            leftWing.addPoint(
                    x,
                    y + 105
            );


            g2.setColor(
                    new Color(
                            180,
                            40,
                            40
                    )
            );


            g2.fillPolygon(
                    leftWing
            );


            // ==========================================
            // ARIPA DREAPTĂ
            // ==========================================

            Polygon rightWing =
                    new Polygon();


            rightWing.addPoint(
                    x + 54,
                    y + 75
            );


            rightWing.addPoint(
                    x + 76,
                    y + 112
            );


            rightWing.addPoint(
                    x + 54,
                    y + 105
            );


            g2.fillPolygon(
                    rightWing
            );


            // ==========================================
            // MOTOR
            // ==========================================

            g2.setColor(
                    new Color(
                            70,
                            70,
                            75
                    )
            );


            g2.fillRect(
                    centerX - 15,
                    y + 100,
                    30,
                    12
            );


            // ==========================================
            // FLACĂRĂ
            // ==========================================

            int flameHeight =
                    25 +
                    (int)
                    (Math.random() * 25);


            Polygon flame =
                    new Polygon();


            flame.addPoint(
                    centerX - 14,
                    y + 110
            );


            flame.addPoint(
                    centerX,
                    y + 110 + flameHeight
            );


            flame.addPoint(
                    centerX + 14,
                    y + 110
            );


            g2.setColor(
                    new Color(
                            255,
                            120,
                            0
                    )
            );


            g2.fillPolygon(
                    flame
            );


            // Flacără interioară

            Polygon innerFlame =
                    new Polygon();


            innerFlame.addPoint(
                    centerX - 7,
                    y + 110
            );


            innerFlame.addPoint(
                    centerX,
                    y + 110 + flameHeight - 10
            );


            innerFlame.addPoint(
                    centerX + 7,
                    y + 110
            );


            g2.setColor(
                    Color.YELLOW
            );


            g2.fillPolygon(
                    innerFlame
            );


            g2.dispose();
        }


        // ==========================================
        // DESENARE STEA
        // ==========================================

        private void drawStar(
                Graphics2D g2,
                int x,
                int y,
                int size
        ) {

            g2.setColor(
                    Color.WHITE
            );


            g2.fillOval(
                    x,
                    y,
                    size,
                    size
            );
        }


        // ==========================================
        // DESENARE FUM
        // ==========================================

        private void drawSmoke(
                Graphics2D g2,
                int x,
                int y
        ) {

            g2.setColor(
                    new Color(
                            190,
                            190,
                            190,
                            130
                    )
            );


            g2.fillOval(
                    x - 35,
                    y,
                    35,
                    35
            );


            g2.fillOval(
                    x + 5,
                    y + 10,
                    40,
                    40
            );


            g2.fillOval(
                    x - 10,
                    y + 25,
                    45,
                    40
            );
        }
    }
}