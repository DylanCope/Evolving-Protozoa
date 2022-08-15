package protoevo.biology;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public abstract class ConstructionProject implements Serializable {

    private static final long serialVersionUID = 1L;

    private final float requiredMass;
    private final float requiredEnergy;
    private float timeSpent;
    private final float timeToComplete;
    private final Map<Food.ComplexMolecule, Float> requiredComplexMolecules;

    /**
     * @param requiredMass required mass to contribute to project
     * @param requiredEnergy required energy to contribute to project
     * @param timeToComplete required time to contribute to project
     * @param requiredComplexMolecules required complex molecules to contribute to project
     */
    public ConstructionProject(float requiredMass,
                               float requiredEnergy,
                               float timeToComplete,
                               Map<Food.ComplexMolecule, Float> requiredComplexMolecules) {
        this.requiredMass = requiredMass;
        this.requiredComplexMolecules = requiredComplexMolecules;
        this.timeToComplete = timeToComplete;
        this.requiredEnergy = requiredEnergy;
    }

    public float getRequiredMass() {
        return requiredMass;
    }

    public boolean requiresComplexMolecules() {
        return requiredComplexMolecules != null && !requiredComplexMolecules.isEmpty();
    }

    public Collection<Food.ComplexMolecule> getRequiredMolecules() {
        return requiredComplexMolecules.keySet();
    }

    public float getRequiredComplexMoleculeAmount(Food.ComplexMolecule molecule) {
        return requiredComplexMolecules.getOrDefault(molecule, 0f);
    }

    public boolean canMakeProgress(float availableEnergy,
                                   float availableMass,
                                   Map<Food.ComplexMolecule, Float> availableComplexMolecules,
                                   float delta) {
        if (availableEnergy < energyToMakeProgress(delta) || availableMass < massToMakeProgress(delta))
            return false;
        if (requiresComplexMolecules() && availableComplexMolecules != null)
            for (Food.ComplexMolecule molecule : getRequiredMolecules()) {
                float available = availableComplexMolecules.getOrDefault(molecule, 0f);
                if (available < complexMoleculesToMakeProgress(delta, molecule))
                    return false;
            }
        return true;
    }

    public float getProgress() {
        return Math.max(Math.min(timeSpent / timeToComplete, 1f), 0f);
    }

    public boolean notFinished() {
        return timeSpent < timeToComplete;
    }

    /**
     * Make progress on the project
     * @param delta Change in time
     */
    public void progress(float delta) {
        timeSpent = Math.min(timeSpent + delta, timeToComplete);
    }

    public float massToMakeProgress(float delta) {
        return delta * requiredMass / timeToComplete;
    }

    public float energyToMakeProgress(float delta) {
        return delta * requiredEnergy / timeToComplete;
    }

    public float complexMoleculesToMakeProgress(float delta, Food.ComplexMolecule molecule) {
        float amount = getRequiredComplexMoleculeAmount(molecule);
        return delta * amount / timeToComplete;
    }
}