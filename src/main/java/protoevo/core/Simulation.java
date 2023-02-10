package protoevo.core;

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

	public static String defaultSettingsPath = "config/default_settings.yaml";
	public static String settingsPath = defaultSettingsPath;

	private final Tank tank;
	private boolean simulate, pause = false;
	private float timeDilation = 1, timeSinceSave = 0, timeSinceSnapshot = 0;
	private double lastUpdateTime = 0;
	
	public static Random RANDOM;
	private boolean debug = false, delayUpdate = true;

	private final String name;
	private final String genomeFile, historyFile;
	private List<String> statsNames;
	private final REPL repl;

	public Simulation()
	{
		simulate = true;
		name = generateSimName();
		System.out.println("Created new simulation named: " + name);
		genomeFile = "saves/" + name + "/genomes.csv";
		historyFile = "saves/" + name + "/history.csv";
		settingsPath = "saves/" + name + "/settings.yaml";
		newSaveDir();
		tank = new Tank();
		tank.setGenomeFile(genomeFile);
		loadSettings();
		RANDOM = new Random(Settings.simulationSeed);
		repl = new REPL(this);
		new Thread(repl).start();
	}

	public Simulation(String name)
	{
		simulate = true;
		this.name = name;
		genomeFile = "saves/" + name + "/genomes.csv";
		historyFile = "saves/" + name + "/history.csv";
		settingsPath = "saves/" + name + "/settings.yaml";

		newSaveDir();
		tank = loadMostRecentTank();
		loadSettings();
		RANDOM = new Random(Settings.simulationSeed);
		repl = new REPL(this);
		new Thread(repl).start();
	}

	public Simulation(String name, String save)
	{
		simulate = true;
		this.name = name;
		genomeFile = "saves/" + name + "/genomes.csv";
		historyFile = "saves/" + name + "/history.csv";

		newSaveDir();
		tank = loadTank("saves/" + name + "/tank/" + save);
		loadSettings();
		RANDOM = new Random(Settings.simulationSeed);
		repl = new REPL(this);
		new Thread(repl).start();
	}

	public REPL getREPL() {
		return repl;
	}

	private void loadSettings() {
//		tank.cellCapacities.put(Protozoan.class, Settings.maxProtozoa);
//		tank.cellCapacities.put(PlantCell.class, Settings.maxPlants);
//		tank.cellCapacities.put(MeatCell.class, Settings.maxMeat);
	}

	private void newSaveDir() {
		try {
			Path saveDir = Paths.get("saves/" + name);
			if (!Files.exists(saveDir)) {
				Files.createDirectories(saveDir);
				Files.createDirectories(Paths.get("saves/" + name + "/tank"));

				File original = new File(defaultSettingsPath);
				File copied = new File(settingsPath);
				Files.copy(original.toPath(), copied.toPath());
			}

			Path genomePath = Paths.get(genomeFile);
			if (!Files.exists(genomePath))
				Files.createFile(genomePath);

			Path historyPath = Paths.get(historyFile);
			if (!Files.exists(historyPath))
				Files.createFile(historyPath);

			String seedFile = "saves/" + name + "/seed.txt";
			Path seedPath = Paths.get(seedFile);
			if (!Files.exists(seedPath)) {
				try {
					Files.createFile(seedPath);
					FileIO.appendLine(seedFile, Settings.simulationSeed + "");
				} catch (IOException e) {
					System.out.println("Failed to create seed file.");
				}
			}

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

	public void setupTank() {
		tank.initialise();
	}

	public Tank loadTank(String filename)
	{
		try {
			Tank tank = (Tank) FileIO.load(filename);
			System.out.println("Loaded tank at: " + filename);
			return tank;
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Unable to load tank at " + filename + " because: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public Tank loadMostRecentTank() {
		Path dir = Paths.get("saves/" + name + "/tank");
		if (Files.exists(dir)) {
			try (Stream<Path> pathStream = Files.list(dir)) {
				Optional<Path> lastFilePath = pathStream
						.filter(f -> !Files.isDirectory(f))
						.max(Comparator.comparingLong(f -> f.toFile().lastModified()));

				if (lastFilePath.isPresent())
					return loadTank(lastFilePath.get().toString().replace(".dat", ""));
				else throw new RuntimeException("No tank files found.");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else throw new RuntimeException("No tank files found.");
	}

	public void simulate() {
		setupTank();
		makeHistorySnapshot();
		float refreshDelay = 1f / Settings.targetFPS;
		while (simulate) {

			if (delayUpdate && Settings.targetFPS > 0) {
				double currTime = Utils.getTimeSeconds();
				if (!pause)
					update();
				float updateTime = (float) (Utils.getTimeSeconds() - currTime);
				try {
					if (refreshDelay - updateTime > 0)
						Thread.sleep((long) (1000 * (refreshDelay - updateTime)));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			} else {
				if (!pause)
					update();
			}

			if (tank.numberOfProtozoa() <= 0 && Settings.finishOnProtozoaExtinction) {
				simulate = false;
				System.out.println();
				System.out.println("Finished simulation. All protozoa died.");
				printStats();
			}
		}
		System.out.println("Simulation loop ended.");
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

	public void toggleUpdateDelay() {
		delayUpdate = !delayUpdate;
	}

	public boolean isPaused() {
		return pause;
	}

	public boolean isFinished() {
		return !simulate;
	}

	public void toggleSpeed() {
		if (timeDilation <= 1f)
			timeDilation = 2f;
		else if (timeDilation <= 2f)
			timeDilation = 5f;
//		else if (timeDilation <= 5f)
//			timeDilation = 10f;
		else
			timeDilation = 1f;
	}
}
