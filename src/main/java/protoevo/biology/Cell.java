package protoevo.biology;

import protoevo.core.Particle;
import protoevo.core.Settings;
import protoevo.core.Simulation;
import protoevo.env.Rock;
import protoevo.env.Tank;
import protoevo.utils.Geometry;
import protoevo.utils.Vector2;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

public abstract class Cell extends Particle implements Serializable
{
	private static final long serialVersionUID = -4333766895269415282L;

	@FunctionalInterface
	public interface EntityBuilder<T, R> {
		R apply(T t) throws MiscarriageException;
	}
	private Color healthyColour, fullyDegradedColour;
	private int generation = 1;
	private boolean dead = false;
	protected boolean hasHandledDeath = false;
	private float timeAlive = 0f;
	private float health = 1f;
	private float growthRate = 0.0f;
	private float energyAvailable = Settings.startingAvailableCellEnergy;
	private float constructionMassAvailable, wasteMass;
	private final Map<Food.ComplexMolecule, Float> availableComplexMolecules;
	private final Set<CellAdhesion.CellBinding> cellBindings, toAttach;
	private final Map<CellAdhesion.CellAdhesionMolecule, Float> surfaceCAMs;
	private final Map<Food.Type, Float> foodDigestionRates;
	private final Map<Food.Type, Food> foodToDigest;
	private final Set<ConstructionProject> constructionProjects;
	private final Map<Food.ComplexMolecule, Float> complexMoleculeProductionRates;
	private final Map<CellAdhesion.CellAdhesionMolecule, Float> camProductionRates;
	private final ArrayList<Cell> children = new ArrayList<>();

	public Cell(Tank tank)
	{
		super(tank);
		healthyColour = new Color(255, 255, 255);
		foodDigestionRates = new HashMap<>(0);
		foodToDigest = new HashMap<>(0);
		cellBindings = new HashSet<>(0);
		toAttach = new HashSet<>(0);
		surfaceCAMs = new HashMap<>(0);
		constructionProjects = new HashSet<>(0);
		complexMoleculeProductionRates = new HashMap<>(0);
		camProductionRates = new HashMap<>(0);
		availableComplexMolecules = new HashMap<>(0);
	}
	
	public void update(float delta) {

		timeAlive += delta;
		digest(delta);
		repair(delta);
		resourceProduction(delta);
		progressConstructionProjects(delta);

		if (!toAttach.isEmpty()) {
			cellBindings.addAll(toAttach);
			toAttach.clear();
		}
		cellBindings.removeIf(this::detachCondition);
		for (CellAdhesion.CellBinding binding : cellBindings)
			handleBindingInteraction(binding, delta);
	}

	public void progressConstructionProjects(float delta) {
		for (ConstructionProject project : constructionProjects) {
			if (project.notFinished() && project.canMakeProgress(
					energyAvailable,
					constructionMassAvailable,
					availableComplexMolecules,
					delta)) {
				useEnergy(project.energyToMakeProgress(delta));
				useConstructionMass(project.massToMakeProgress(delta));
				if (project.requiresComplexMolecules())
					for (Food.ComplexMolecule molecule : project.getRequiredMolecules()) {
						float amountUsed = project.complexMoleculesToMakeProgress(delta, molecule);
						depleteComplexMolecule(molecule, amountUsed);
					}
				project.progress(delta);
			}
		}
	}

	public void resourceProduction(float delta) {
		for (Food.ComplexMolecule molecule : complexMoleculeProductionRates.keySet()) {
			float producedMass = delta * complexMoleculeProductionRates.getOrDefault(molecule, 0f);
			float requiredEnergy = molecule.getProductionCost() * producedMass;
			if (producedMass > 0 && constructionMassAvailable > producedMass && energyAvailable > requiredEnergy) {
				addAvailableComplexMolecule(molecule, producedMass);
				useConstructionMass(producedMass);
				useEnergy(requiredEnergy);
			}
		}
		for (CellAdhesion.CellAdhesionMolecule cam : camProductionRates.keySet()) {
			float producedMass = delta * camProductionRates.getOrDefault(cam, 0f);
			float requiredEnergy = cam.getProductionCost() * producedMass;
			if (producedMass > 0 && constructionMassAvailable > producedMass && energyAvailable > requiredEnergy) {
				float currentAmount = surfaceCAMs.getOrDefault(cam, 0f);
				surfaceCAMs.put(cam, currentAmount + producedMass);
				useConstructionMass(producedMass);
				useEnergy(requiredEnergy);
			}
		}
	}

	public float getDigestionRate(Food.Type foodType) {
		return foodDigestionRates.getOrDefault(foodType, 0f);
	}

	public void setDigestionRate(Food.Type foodType, float rate) {
		foodDigestionRates.put(foodType, rate);
	}

	public void extractFood(EdibleCell cell, float extraction) {
		Food.Type foodType = cell.getFoodType();
		float extractedMass = cell.getMass() * extraction;
		cell.removeMass(Settings.foodExtractionWasteMultiplier * extractedMass);
		cell.setHealth(cell.getHealth() * (1 - 5f * extraction));
		Food food = foodToDigest.getOrDefault(foodType, new Food(extractedMass, foodType));
		food.addSimpleMass(extractedMass);
		for (Food.ComplexMolecule molecule : cell.getComplexMolecules()) {
			if (cell.getComplexMoleculeAvailable(molecule) > 0) {
				float extractedAmount = extraction * cell.getComplexMoleculeAvailable(molecule);
				cell.depleteComplexMolecule(molecule, extractedAmount);
				food.addComplexMoleculeMass(molecule, extractedMass);
			}
		}
		foodToDigest.put(foodType, food);
	}

	public void digest(float delta) {
		for (Food food : foodToDigest.values()) {
			float rate = delta * 2f * getDigestionRate(food.getType());
			if (food.getSimpleMass() > 0) {
				float massExtracted = food.getSimpleMass() * rate;
				addConstructionMass(massExtracted);
				food.subtractSimpleMass(massExtracted);
				energyAvailable += food.getEnergy(massExtracted);
			}
			for (Food.ComplexMolecule molecule : food.getComplexMolecules()) {
				float amount = food.getComplexMoleculeMass(molecule);
				if (amount == 0)
					continue;
				float extracted = Math.min(amount, amount * rate);
				addAvailableComplexMolecule(molecule, extracted);
				food.subtractComplexMolecule(molecule, extracted);
			}
		}
	}

	public void repair(float delta) {
		if (!isDead() && getHealth() < 1f && getGrowthRate() > 0) {
			float massRequired = getMass() * 0.01f * delta;
			float energyRequired = massRequired * 3f;
			if (massRequired < constructionMassAvailable && energyRequired < energyAvailable) {
				useEnergy(energyRequired);
				useConstructionMass(massRequired);
				setHealth(getHealth() + delta * Settings.cellRepairRate);
			}
		}
	}

	public boolean detachCondition(CellAdhesion.CellBinding binding) {
		Cell e = binding.getDestinationEntity();
		if (e.isDead())
			return true;
		float dist = e.getPos().sub(getPos()).len();
		float maxDist = 1.3f * (e.getRadius() + getRadius());
		float minDist = 0.95f * (e.getRadius() + getRadius());
		return dist > maxDist || dist < minDist;
	}

	@Override
	public void physicsStep(float delta) {
		for (CellAdhesion.CellBinding binding : cellBindings)
			handleBindingConstraint(binding.getDestinationEntity());
		super.physicsStep(delta);
	}

	public void addConstructionProject(ConstructionProject project) {
		constructionProjects.add(project);
	}

	public void handleInteractions(float delta) {
		grow(delta);
	}

	public void grow(float delta) {
		float gr = getGrowthRate();
		float newR = super.getRadius() * (1 + gr * delta);
		float massChange = getMass(newR) - getMass(super.getRadius());
		if (massChange < constructionMassAvailable &&
				(newR > Settings.minPlantBirthRadius || gr > 0)) {
			setRadius(newR);
			if (massChange > 0)
				useConstructionMass(massChange);
			else
				wasteMass -= massChange;
		}
		if (Float.isNaN(getRadius()))
			killCell();
	}

	public void setGrowthRate(float gr) {
		growthRate = gr;
	}

	public float getGrowthRate() {
		if (getRecentRigidCollisions() > 2)
			return 0;
		return growthRate;
	}

	public synchronized void attach(CellAdhesion.CellBinding binding) {
		if (!cellBindings.contains(binding))
			toAttach.add(binding);
	}

	public Set<CellAdhesion.CellBinding> getCellBindings() {
		return cellBindings;
	}

	public Collection<CellAdhesion.CellAdhesionMolecule> getSurfaceCAMs() {
		return surfaceCAMs.keySet();
	}

	public boolean cannotMakeBinding() {
		return false;
	}

	@Override
	public void onParticleCollisionCallback(Particle p, float delta) {
		if (p instanceof Cell) {
			Cell otherCell = (Cell) p;
			if (otherCell.cannotMakeBinding() || cannotMakeBinding())
				return;

			for (CellAdhesion.CellAdhesionMolecule myCAM : getSurfaceCAMs()) {
				for (CellAdhesion.CellAdhesionMolecule theirCAM : otherCell.getSurfaceCAMs()) {
					// TODO: implement probabilistic CAM binding based on amounts
					if (myCAM.bindsTo(theirCAM)) {
						createNewBinding(myCAM, otherCell);
						otherCell.createNewBinding(theirCAM, this);
					}
				}
			}
		}
	}

	public void handleBindingInteraction(CellAdhesion.CellBinding binding, float delta) {
		CellAdhesion.CAMJunctionType junctionType = binding.getCAM().getJunctionType();
		if (junctionType.equals(CellAdhesion.CAMJunctionType.OCCLUDING))
			handleOcclusionBindingInteraction(binding, delta);
		else if (junctionType.equals(CellAdhesion.CAMJunctionType.CHANNEL_FORMING))
			handleChannelBindingInteraction(binding, delta);
		else if (junctionType.equals(CellAdhesion.CAMJunctionType.SIGNAL_RELAYING))
			handleSignallingBindingInteraction(binding, delta);
	}

	public void handleOcclusionBindingInteraction(CellAdhesion.CellBinding binding, float delta) {}

	public void handleChannelBindingInteraction(CellAdhesion.CellBinding binding, float delta) {
		Cell other = binding.getDestinationEntity();
		float transferRate = Settings.channelBindingEnergyTransport;

		float massDelta = getConstructionMassAvailable() - other.getConstructionMassAvailable();
		float constructionMassTransfer = Math.abs(transferRate * massDelta * delta);
		if (massDelta > 0) {
			other.addConstructionMass(constructionMassTransfer);
			useConstructionMass(constructionMassTransfer);
		} else {
			addConstructionMass(constructionMassTransfer);
			other.useConstructionMass(constructionMassTransfer);
		}

		float energyDelta = getEnergyAvailable() - other.getEnergyAvailable();
		float energyTransfer = Math.abs(transferRate * energyDelta * delta);
		if (energyDelta > 0) {
			other.addAvailableEnergy(energyTransfer);
			useEnergy(energyTransfer);
		} else {
			addAvailableEnergy(energyTransfer);
			other.useEnergy(energyTransfer);
		}

		for (Food.ComplexMolecule molecule : getComplexMolecules())
			handleComplexMoleculeTransport(other, molecule, delta);
		for (Food.ComplexMolecule molecule : other.getComplexMolecules())
			other.handleComplexMoleculeTransport(this, molecule, delta);
	}

	private void handleComplexMoleculeTransport(Cell other, Food.ComplexMolecule molecule, float delta) {
		float massDelta = getComplexMoleculeAvailable(molecule) - other.getComplexMoleculeAvailable(molecule);
		float transferRate = Settings.occludingBindingEnergyTransport;
		if (massDelta > 0) {
			float massTransfer = transferRate * massDelta * delta;
			other.addAvailableComplexMolecule(molecule, massTransfer);
			depleteComplexMolecule(molecule, massTransfer);
		}
	}

	public void handleSignallingBindingInteraction(CellAdhesion.CellBinding binding, float delta) {}

	public boolean isAttached(Cell e) {
		for (CellAdhesion.CellBinding binding : cellBindings)
			if (binding.getDestinationEntity().equals(e))
				return true;
		return false;
	}
	
	public abstract boolean isEdible();

	public void createNewBinding(CellAdhesion.CellAdhesionMolecule cam, Cell e) {
		attach(new CellAdhesion.CellBinding(this, e, cam));
	}

	public void setHealth(float h)
	{
		health = h;
		if (health > 1) 
			health = 1;

		if (health < 0.05)
			killCell();
	}

	public void handleDeath() {
		hasHandledDeath = true;
	}

	@Override
	public boolean handlePotentialCollision(Rock rock, float delta) {
		if (rock.pointInside(getPos())) {
			killCell();
			return true;
		}
		return super.handlePotentialCollision(rock, delta);
	}

	public abstract String getPrettyName();

	public Map<String, Float> getStats() {
		TreeMap<String, Float> stats = new TreeMap<>();
		stats.put("Age", 100 * timeAlive);
		stats.put("Health", 100 * getHealth());
		stats.put("Size", Settings.statsDistanceScalar * getRadius());
		stats.put("Speed", Settings.statsDistanceScalar * getSpeed());
		stats.put("Generation", (float) getGeneration());
		float energyScalar = Settings.statsMassScalar * Settings.statsDistanceScalar * Settings.statsDistanceScalar;
		stats.put("Available Energy", energyScalar * energyAvailable);
		stats.put("Total Mass", Settings.statsMassScalar * getMass());
		stats.put("Construction Mass", Settings.statsMassScalar * constructionMassAvailable);
		if (wasteMass > 0)
			stats.put("Waste Mass", Settings.statsDistanceScalar * wasteMass);

		float gr = getGrowthRate();
		stats.put("Growth Rate", Settings.statsDistanceScalar * gr);

		for (Food.ComplexMolecule molecule : availableComplexMolecules.keySet())
			if (availableComplexMolecules.get(molecule) > 0)
				stats.put(molecule + " Available", availableComplexMolecules.get(molecule));

		if (cellBindings.size() > 0)
			stats.put("Num Cell Bindings", (float) cellBindings.size());

		for (CellAdhesion.CAMJunctionType junctionType : CellAdhesion.CAMJunctionType.values()) {
//			int count = 0;
//			for (CellAdhesion.CellBinding binding : cellBindings)
//				if (binding.getJunctionType().equals(junctionType))
//					count++;
//			if (count > 0)
//				stats.put(junctionType + " Bindings", (float) count);

			float camMass = 0;
			for (CellAdhesion.CellAdhesionMolecule molecule : surfaceCAMs.keySet())
				if (molecule.getJunctionType().equals(junctionType))
					camMass += surfaceCAMs.get(molecule);
			if (camMass > 0)
				stats.put(junctionType + " CAM Mass", camMass);
		}

		float massTimeScalar = Settings.statsMassScalar / Settings.statsTimeScalar;
		for (Food.ComplexMolecule molecule : complexMoleculeProductionRates.keySet())
			if (complexMoleculeProductionRates.get(molecule) > 0)
				stats.put(molecule + " Production", massTimeScalar * complexMoleculeProductionRates.get(molecule));

		for (Food.ComplexMolecule molecule : availableComplexMolecules.keySet())
			if (availableComplexMolecules.get(molecule) > 0)
				stats.put(molecule + " Available", 100f * Settings.statsMassScalar * availableComplexMolecules.get(molecule));

		for (Food.Type foodType : foodDigestionRates.keySet())
			if (foodDigestionRates.get(foodType) > 0)
				stats.put(foodType + " Digestion Rate", massTimeScalar * foodDigestionRates.get(foodType));

		for (Food food : foodToDigest.values())
			stats.put(food + " to Digest", Settings.statsMassScalar * food.getSimpleMass());

		return stats;
	}

	public Map<String, Float> getDebugStats() {
		TreeMap<String, Float> stats = new TreeMap<>();
		stats.put("Position X", Settings.statsDistanceScalar * getPos().getX());
		stats.put("Position Y", Settings.statsDistanceScalar * getPos().getY());
		return stats;
	}
	
	public float getHealth() {
		return health;
	}

	public boolean isDead() {
		return dead || health < 0.05f;
	}

	public void killCell() {
		dead = true;
		health = 0;
	}

	@Override
	public Color getColor() {
		Color healthyColour = getHealthyColour();
		Color degradedColour = getFullyDegradedColour();
		return new Color(
			(int) (healthyColour.getRed() + (1 - getHealth()) * (degradedColour.getRed() - healthyColour.getRed())),
			(int) (healthyColour.getGreen() + (1 - getHealth()) * (degradedColour.getGreen() - healthyColour.getGreen())),
			(int) (healthyColour.getBlue() + (1 - getHealth()) * (degradedColour.getBlue() - healthyColour.getBlue()))
		);
	}

	public Color getHealthyColour() {
		return healthyColour;
	}

	public void setHealthyColour(Color healthyColour) {
		this.healthyColour = healthyColour;
	}

	public void setDegradedColour(Color fullyDegradedColour) {
		this.fullyDegradedColour = fullyDegradedColour;
	}

	public Color getFullyDegradedColour() {
		if (fullyDegradedColour == null) {
			Color healthyColour = getHealthyColour();
			int r = healthyColour.getRed();
			int g = healthyColour.getGreen();
			int b = healthyColour.getBlue();
			float p = 0.7f;
			return new Color((int) (r*p), (int) (g*p), (int) (b*p));
		}
		return fullyDegradedColour;
	}

	public int getGeneration() {
		return generation;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

	public int burstMultiplier() {
		return 20;
	}

	public <T extends Cell> void burst(Class<T> type, EntityBuilder<Float, T> createChild) {
		killCell();
		hasHandledDeath = true;

		float angle = (float) (2 * Math.PI * Simulation.RANDOM.nextDouble());
		int maxChildren = (int) (burstMultiplier() * getRadius() / Settings.maxParticleRadius);

		int nChildren = (maxChildren <= 1) ? 2 : 2 + Simulation.RANDOM.nextInt(maxChildren);

		Tank tank = getTank();
		for (int i = 0; i < nChildren; i++) {
			Vector2 dir = new Vector2((float) Math.cos(angle), (float) Math.sin(angle));
			float p = (float) (0.3 + 0.7 * Simulation.RANDOM.nextDouble() / nChildren);

			int nEntities = tank.cellCounts.getOrDefault(type, 0);
			int maxEntities = tank.cellCapacities.getOrDefault(type, 0);
			if (nEntities > maxEntities)
				return;
			try {
				T child = createChild.apply(getRadius() * p);
				child.setPos(getPos().add(dir.mul(2 * child.getRadius())));
				child.setGeneration(getGeneration() + 1);
				allocateChildResources(child, p);
				for (Cell otherChild : children)
					child.handlePotentialCollision(otherChild, 0);
				children.add(child);
			} catch (MiscarriageException ignored) {}
			angle += 2 * Math.PI / nChildren;
		}

		for (int j = 0; j < 8; j++)
			for (Cell child1 : children)
				for (Cell child2 : children)
					child1.handlePotentialCollision(child2, 0);
		children.forEach(tank::add);
	}

	private void allocateChildResources(Cell child, float p) {
		child.setAvailableConstructionMass(constructionMassAvailable * p);
		child.setEnergyAvailable(energyAvailable * p);
		for (Food.ComplexMolecule molecule : availableComplexMolecules.keySet())
			child.setComplexMoleculeAvailable(molecule, p * getComplexMoleculeAvailable(molecule));

		for (CellAdhesion.CellAdhesionMolecule cam : getSurfaceCAMs())
			child.setCAMAvailable(cam, p * getCAMAvailable(cam));

		for (Food.Type foodType : foodToDigest.keySet()) {
			Food oldFood = foodToDigest.get(foodType);
			Food newFood = new Food(p * oldFood.getSimpleMass(), foodType);
			for (Food.ComplexMolecule molecule : oldFood.getComplexMolecules()) {
				float moleculeAmount = p * oldFood.getComplexMoleculeMass(molecule);
				newFood.addComplexMoleculeMass(molecule, moleculeAmount);
			}
			child.setFoodToDigest(foodType, newFood);
		}
	}

	public void setFoodToDigest(Food.Type foodType, Food food) {
		foodToDigest.put(foodType, food);
	}

	public Collection<Cell> getChildren() {
		return children;
	}

	public float getCAMAvailable(CellAdhesion.CellAdhesionMolecule cam) {
		return surfaceCAMs.getOrDefault(cam, 0f);
	}

	public void setCAMAvailable(CellAdhesion.CellAdhesionMolecule cam, float amount) {
		surfaceCAMs.put(cam, amount);
	}

	public boolean enoughEnergyAvailable(float work) {
		return work < energyAvailable;
	}

	public float getEnergyAvailable() {
		return energyAvailable;
	}

	public void addAvailableEnergy(float energy) {
		energyAvailable = Math.min(energyAvailable + energy, getAvailableEnergyCap()) ;
	}

	private float getAvailableEnergyCap() {
		return Settings.startingAvailableCellEnergy * getRadius() / Settings.minParticleRadius;
	}

	public void setEnergyAvailable(float energy) {
		energyAvailable = energy;
	}

	public void useEnergy(float energy) {
		energyAvailable = Math.max(0, energyAvailable - energy);
	}

	public Collection<Food.ComplexMolecule> getComplexMolecules() {
		return availableComplexMolecules.keySet();
	}

	public void depleteComplexMolecule(Food.ComplexMolecule molecule, float amount) {
		float currAmount = getComplexMoleculeAvailable(molecule);
		setComplexMoleculeAvailable(molecule, currAmount - amount);
	}

	public float getComplexMoleculeAvailable(Food.ComplexMolecule molecule) {
		return availableComplexMolecules.getOrDefault(molecule, 0f);
	}

	private void addAvailableComplexMolecule(Food.ComplexMolecule molecule, float amount) {
		float currentAmount = availableComplexMolecules.getOrDefault(molecule, 0f);
		float newAmount = Math.min(getComplexMoleculeMassCap(), currentAmount + amount);
		availableComplexMolecules.put(molecule, newAmount);
	}

	private float getComplexMoleculeMassCap() {
		return getMass(getRadius() * 0.1f);
	}

	public void setComplexMoleculeAvailable(Food.ComplexMolecule molecule, float amount) {
		availableComplexMolecules.put(molecule, Math.max(0, amount));
	}

	public float getConstructionMassCap() {
		return 2 * getMassDensity() * Geometry.getSphereVolume(getRadius() * 0.25f);
	}

	public void setAvailableConstructionMass(float mass) {
		constructionMassAvailable = Math.min(mass, getConstructionMassCap());
	}

	public float getConstructionMassAvailable() {
		return constructionMassAvailable;
	}

	public void addConstructionMass(float mass) {
		setAvailableConstructionMass(constructionMassAvailable + mass);
	}

	public void useConstructionMass(float mass) {
		constructionMassAvailable = Math.max(0, constructionMassAvailable - mass);
	}

	public void setComplexMoleculeProductionRate(Food.ComplexMolecule molecule, float rate) {
		complexMoleculeProductionRates.put(molecule, rate);
	}

	public void setCAMProductionRate(CellAdhesion.CellAdhesionMolecule cam, float rate) {
		camProductionRates.put(cam, rate);
	}


	@Override
	public float getMass() {
		float extraMass = constructionMassAvailable + wasteMass;
		for (float mass : complexMoleculeProductionRates.values())
			extraMass += mass;
		return getMass(getRadius(), extraMass);
	}

	/**
	 * Changes the radius of the cell to remove the given amount of mass
	 * @param mass mass to remove
	 */
	public void removeMass(float mass) {
		double x = 3 * mass / (4 * getMassDensity() * Math.PI);
		float r = getRadius();
		float newR = (float) Math.pow(r*r*r - x, 1 / 3.);
		if (newR < Settings.minParticleRadius * 0.9f)
			killCell();
		setRadius(newR);
	}

}
