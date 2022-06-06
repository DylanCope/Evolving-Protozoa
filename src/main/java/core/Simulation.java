package core;

import biology.PlantPellet;
import biology.Protozoa;
import com.github.javafaker.Faker;
import neat.SynapseGene;
import utils.FileIO;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simulation
{
	private Tank tank;
	private boolean simulate;
	private float timeDilation = 1, timeSinceSave = 0, timeSinceSnapshot = 0;
	private double updateDelay = Application.refreshDelay / 1000.0, lastUpdateTime = 0;
	
	public static Random RANDOM;
	private boolean debug = false;

	private final String name;
	private final String genomeFile, historyFile;
	private List<String> statsNames;

	public Simulation(long seed)
	{
		RANDOM = new Random(seed);
		simulate = true;
		name = generateSimName();
		System.out.println("Created new simulation named: " + name);
		genomeFile = "saves/" + name + "/genomes.csv";
		historyFile = "saves/" + name + "/history.csv";
		newSaveDir();
		initDefaultTank();
	}

	public Simulation(long seed, String name)
	{
		RANDOM = new Random(seed);
		simulate = true;
		this.name = name;
		genomeFile = "saves/" + name + "/genomes.csv";
		historyFile = "saves/" + name + "/history.csv";

		loadMostRecentTank();
	}

	private void newSaveDir() {
		try {
			Files.createDirectories(Paths.get("saves/" + name));
			Files.createDirectories(Paths.get("saves/" + name + "/tank"));
			Files.createFile(Paths.get(genomeFile));
			Files.createFile(Paths.get(historyFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String generateSimName() {
		Faker faker = new Faker();
		return String.format("%s-%s-%s",
				faker.ancient().primordial().toLowerCase().replaceAll(" ", "-"),
				faker.pokemon().name().toLowerCase().replaceAll(" ", "-"),
				faker.lorem().word().toLowerCase().replaceAll(" ", "-"));
	}
	
	public Simulation() {
		this(new Random().nextLong());
	}
	
	public void initDefaultTank()
	{
		tank = new Tank();
		tank.setGenomeFile(genomeFile);

		for (int i = 0; i < Settings.numInitialProtozoa; i++)
			tank.addRandom(new Protozoa(tank));

		for (int i = 0; i < Settings.numInitialPlantPellets; i++)
			tank.addRandom(new PlantPellet(tank));

		makeHistorySnapshot();
	}
	
	public void loadTank(String filename)
	{
		try {
			tank = (Tank) FileIO.load(filename);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Unable to load tank at " + filename + " because: " + e.getMessage());
			initDefaultTank();
		}
	}

	public void loadMostRecentTank() {
		Path dir = Paths.get("saves/" + name + "/tank");

		try (Stream<Path> pathStream = Files.list(dir)) {
			Optional<Path> lastFilePath = pathStream
					.filter(f -> !Files.isDirectory(f))
					.max(Comparator.comparingLong(f -> f.toFile().lastModified()));

			lastFilePath.ifPresent(path -> loadTank(path.toString()));
		} catch (IOException e) {
			initDefaultTank();
		}
	}

	public void simulate() {
		while (simulate) {
			if (updateDelay > 0) {
				double currTime = Utils.getTimeSeconds();
				if ((currTime - lastUpdateTime) > updateDelay) {
					update();
					lastUpdateTime = currTime;
				}
			} else {
				update();
			}

			if (tank.numberOfProtozoa() <= 0) {
				simulate = false;
				System.out.println();
				System.out.println("Finished simulation. All protozoa died.");
				printStats();
			}
		}
	}

	public void printStats() {
		tank.getStats().forEach(
			(k, v) -> System.out.printf("%s: %.5f\n", k, v)
		);
	}

	public void update()
	{
		float delta = timeDilation * Settings.simulationUpdateDelta;
		synchronized (tank) {
			tank.update(delta);
		}

		timeSinceSave += delta;
		if (timeSinceSave > Settings.timeBetweenSaves) {
			timeSinceSave = 0;
			saveTank();
		}

		timeSinceSnapshot += delta;
		if (timeSinceSnapshot > Settings.historySnapshotTime) {
			timeSinceSnapshot = 0;
			makeHistorySnapshot();
		}
	}

	public void close() {
		simulate = false;
		System.out.println();
		System.out.println("Closing simulation.");
		saveTank();
	}

	public void saveTank() {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new java.util.Date());
		String fileName = "saves/" + name + "/tank/" + timeStamp;
		FileIO.save(tank, fileName);
	}

	public void makeHistorySnapshot() {
		Map<String, Float> stats = tank.getStats();

		if (statsNames == null) {
			statsNames = new ArrayList<>(tank.getStats().keySet());
			String statsCsvHeader = String.join(",", statsNames);
			FileIO.appendLine(historyFile, statsCsvHeader);
		}

		String statsString = statsNames.stream()
				.map(k -> String.format("%.5f", stats.get(k)))
				.collect(Collectors.joining(","));

		FileIO.appendLine(historyFile, statsString);
	}

	public void toggleDebug() {
		debug = !debug;
	}

	public boolean inDebugMode() {
		return debug;
	}

	public Tank getTank() { return tank; }

	public int getGeneration() { return tank.getGeneration(); }

	public float getElapsedTime() { return tank.getElapsedTime(); }

	public float getTimeDilation() { return timeDilation; }

	public void setTimeDilation(float td) { timeDilation = td; }

	public void setUpdateDelay(float updateDelay) {
		this.updateDelay = updateDelay;
	}
}
