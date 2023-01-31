package protoevo.ui.main;

import protoevo.core.Simulation;
import protoevo.ui.Window;
import protoevo.ui.components.TextButton;
import protoevo.ui.components.TextObject;
import protoevo.ui.components.TextStyle;
import protoevo.ui.simulation.SimulationController;
import protoevo.ui.simulation.SimulationRenderer;
import protoevo.utils.Vector2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainScreenRenderer extends Canvas {
    private final TextObject title;
    private final Window window;
    private float time = 0;
    private final BufferedImage thumbnailImage;
    private final TextButton newSimulationButton;
    private final List<TextButton> loadSaveButtons = new ArrayList<>();

    public static void main(String[] args)
    {
        TextStyle.loadFonts();
        protoevo.ui.Window window = new Window("Evolving Protozoa");
        MainScreenRenderer renderer = new MainScreenRenderer(window);
        window.set(renderer, new MainScreenController(window, renderer));
        window.run();
    }

    public MainScreenRenderer(Window window) {
        this.window = window;

        title = new TextObject("Evolving Protozoa",window.getHeight() / 16, Color.WHITE);
        title.setPosition(new Vector2(window.getWidth() / 2f - title.getWidth() / 2f, window.getHeight() / 8f));

        String pathToFile = "resources/thumbnail.png";
        try {
            BufferedImage thumbnail = ImageIO.read(new File(pathToFile));
            int size = window.getHeight() / 2;
            thumbnailImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale((double) size / thumbnail.getWidth(), (double) size / thumbnail.getHeight());
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
            Graphics2D g = thumbnailImage.createGraphics();
            g.drawImage(thumbnail, scaleOp, 0, 0);
            g.dispose();

        } catch (IOException e) {
            throw new RuntimeException("Particle image not found: " + pathToFile + "\n" + e);
        }

        int buttonTextSize = window.getHeight() / 30;
        TextObject newSimulationButtonText = new TextObject(
                "Create New Simulation", buttonTextSize, Color.WHITE.darker());

        newSimulationButton = new TextButton(
                newSimulationButtonText,
                () -> {
                    window.reset();
                    Simulation simulation = new Simulation();
                    simulation.getREPL().setWindow(window);
                    SimulationRenderer renderer = new SimulationRenderer(simulation, window);
                    SimulationController controller = new SimulationController(window, simulation, renderer);
                    window.set(renderer, controller);

                    new Thread(simulation::simulate).start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
        window.getInput().registerUIClickable(newSimulationButton);

        float buttonsX = window.getWidth() * 0.45f;
        newSimulationButton.setPosition(new Vector2(
                buttonsX,
                window.getHeight() / 2f - newSimulationButtonText.getHeight() / 2f));

        // retrieve all folders in the saves folder
        File savesFolder = new File("saves");
        File[] saves = savesFolder.listFiles();
        if (saves != null && saves.length > 0) {
//            saves = Arrays.stream(saves)
//                    .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified())).toArray();
            Arrays.sort(saves, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            float offset = newSimulationButton.getHeight() * 1.25f;
            int nSaves = 0;
            for (File save : saves) {
                if (save.isDirectory()) {
                    nSaves++;
                }
            }
            int maxSaves = 7;
            nSaves = Math.min(nSaves, maxSaves);

//            float newSimY = Math.max(
//                    window.getHeight() / 4f - newSimulationButtonText.getHeight() / 2f,
            float newSimY = window.getHeight() / 2f - newSimulationButtonText.getHeight() / 2f - nSaves * offset / 2f;

            newSimulationButton.setPosition(new Vector2(buttonsX, newSimY));

            for (int i = 0; i < saves.length; i++) {
                File save = saves[i];
                if (i == maxSaves) {
                    break;
                }
                if (save.isDirectory()) {
                    TextObject loadSaveButtonText = new TextObject(
                            "Load " + save.getName(), buttonTextSize, Color.WHITE.darker());

                    TextButton loadSaveButton = new TextButton(
                            loadSaveButtonText,
                            () -> {
                                window.reset();
                                Simulation simulation = new Simulation(save.getName());
                                simulation.getREPL().setWindow(window);
                                SimulationRenderer renderer = new SimulationRenderer(simulation, window);
                                SimulationController controller = new SimulationController(window, simulation, renderer);
                                window.set(renderer, controller);

                                new Thread(simulation::simulate).start();

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    window.getInput().registerUIClickable(loadSaveButton);

                    loadSaveButton.setPosition(new Vector2(
                            buttonsX,
                            newSimY + (i + 1) * offset));

                    loadSaveButtons.add(loadSaveButton);
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;

        time += 0.1;
        int backgroundR = 25 + (int)(5 *Math.cos(time/100.0));
        int backgroundG = 35 + (int)(20*Math.sin(time/100.0));
        int backgroundB = 45 + (int)(15*Math.cos(time/100.0 + 1));
        Color backgroundColour = new Color(backgroundR, backgroundG, backgroundB);
        graphics.setColor(backgroundColour);

        graphics.fillRect(0, 0, window.getWidth(), window.getHeight());

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        title.render(graphics);

        int imgX = (int) (window.getWidth() / 4f - thumbnailImage.getWidth() / 2f);
        int imgY = (int) (window.getHeight() / 2f - thumbnailImage.getHeight() / 2f);
        graphics.drawImage(thumbnailImage, imgX, imgY, null);

        newSimulationButton.render(graphics);
        loadSaveButtons.forEach(button -> button.render(graphics));
    }
}
