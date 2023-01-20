package protoevo.core;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import protoevo.biology.MeatCell;
import protoevo.biology.PlantCell;
import protoevo.biology.Protozoan;
import com.github.javafaker.Faker;
import protoevo.env.Tank;
import protoevo.utils.FileIO;
import protoevo.utils.Utils;

import java.io.*;
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
	private boolean simulate, pause = false;
	private float timeDilation = 1, timeSinceSave = 0, timeSinceSnapshot = 0;
	private double updateDelay = Application.refreshDelay / 1000.0, lastUpdateTime = 0;
	
	public static Random RANDOM;
	private boolean debug = false, delayUpdate = true;

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
		newDefaultTank();
		loadSettings();
	}

	public Simulation(long seed, String name)
	{
		RANDOM = new Random(seed);
		simulate = true;
		this.name = name;
		genomeFile = "saves/" + name + "/genomes.csv";
		historyFile = "saves/" + name + "/history.csv";

		newSaveDir();
		loadMostRecentTank();
		loadSettings();
	}

	public Simulation(long seed, String name, String save)
	{
		RANDOM = new Random(seed);
		simulate = true;
		this.name = name;
		genomeFile = "saves/" + name + "/genomes.csv";
		historyFile = "saves/" + name + "/history.csv";

		newSaveDir();
		loadTank("saves/" + name + "/tank/" + save);
		loadSettings();
	}

	private void loadSettings() {
		tank.cellCapacities.put(Protozoan.class, Settings.maxProtozoa);
		tank.cellCapacities.put(PlantCell.class, Settings.maxPlants);
		tank.cellCapacities.put(MeatCell.class, Settings.maxMeat);
	}

	private void newSaveDir() {
		try {
			Files.createDirectories(Paths.get("saves/" + name));
			Files.createDirectories(Paths.get("saves/" + name + "/tank"));

			Path genomePath = Paths.get(genomeFile);
			if (!Files.exists(genomePath))
				Files.createFile(genomePath);

			Path historyPath = Paths.get(historyFile);
			if (!Files.exists(historyPath))
				Files.createFile(historyPath);

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
	
	public void newDefaultTank()
	{
		tank = new Tank();
		loadSettings();
		tank.setGenomeFile(genomeFile);
	}

	public void setupTank() {
		tank.initialise();
	}

	public void loadTank(String filename)
	{
		try {
			tank = (Tank) FileIO.load(filename);
			System.out.println("Loaded tank at: " + filename);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Unable to load tank at " + filename + " because: " + e.getMessage());
			newDefaultTank();
		}
	}

	public void loadMostRecentTank() {
		Path dir = Paths.get("saves/" + name + "/tank");
		if (Files.exists(dir))
			try (Stream<Path> pathStream = Files.list(dir)) {
				Optional<Path> lastFilePath = pathStream
						.filter(f -> !Files.isDirectory(f))
						.max(Comparator.comparingLong(f -> f.toFile().lastModified()));

				lastFilePath.ifPresentOrElse(
						path -> loadTank(path.toString().replace(".dat", "")),
						this::newDefaultTank
				);
			} catch (IOException e) {
				newDefaultTank();
			}
		else newDefaultTank();
	}

	public void simulate() {
		setupTank();
		makeHistorySnapshot();
		while (simulate) {
			if (pause)
				continue;

			if (delayUpdate && updateDelay > 0) {
				double currTime = Utils.getTimeSeconds();
				if ((currTime - lastUpdateTime) > updateDelay) {
					update();
					lastUpdateTime = currTime;
				}
			} else {
				update();
			}

			if (tank.numberOfProtozoa() <= 0 && Settings.finishOnProtozoaExtinction) {
				simulate = false;
				System.out.println();
				System.out.println("Finished simulation. All protozoa died.");
				printStats();
			}
		}
	}

	public void printStats() {
		tank.getStats(true).forEach(
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
		Map<String, Float> stats = tank.getStats(true);

		if (statsNames == null) {
			statsNames = new ArrayList<>(tank.getStats(true).keySet());
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

	public void togglePause() {
		pause = !pause;
	}

	public boolean inDebugMode() {
		return debug;
	}

	public Tank getTank() { return tank; }

	public long getGeneration() { return tank.getGeneration(); }

	public float getElapsedTime() { return tank.getElapsedTime(); }

	public float getTimeDilation() { return timeDilation; }

	public void setTimeDilation(float td) { timeDilation = td; }

	public void setUpdateDelay(float updateDelay) {
		this.updateDelay = updateDelay;
	}

	public void toggleUpdateDelay() {
		delayUpdate = !delayUpdate;
	}
}
