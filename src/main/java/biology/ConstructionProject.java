package biology;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public abstract class ConstructionProject implements Serializable {

    private static final long serialVersionUID = 1L;

    private float requiredMass;
    private final Map<Food.ComplexMolecule, Float> requiredComplexMolecules;

    public ConstructionProject(float requiredMass, Map<Food.ComplexMolecule, Float> requiredComplexMolecules) {
        this.requiredMass = requiredMass;
        this.requiredComplexMolecules = requiredComplexMolecules;
    }

    public ConstructionProject(float requiredMass) {
        this(requiredMass, null);
    }

    public float getRequiredMass() {
        return requiredMass;
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
        if (availableEnergy < energyToMakeProgress(delta))
            return false;
        if (availableMass < massToMakeProgress(delta))
            return false;
        for (Food.ComplexMolecule molecule : getRequiredMolecules())
            if (availableComplexMolecules.get(molecule) < complexMoleculesToMakeProgress(delta, molecule))
                return false;
        return true;
    }

    public void contributeMassToProject(float mass) {
        requiredMass = Math.max(requiredMass - mass, 0);
    }

    public void contributeComplexMoleculeToProject(Food.ComplexMolecule molecule, float amount) {
        float req = getRequiredComplexMoleculeAmount(molecule);
        requiredComplexMolecules.put(molecule, Math.max(req - amount, 0));
    }

    public boolean isFinished() {
        for (Food.ComplexMolecule molecule : requiredComplexMolecules.keySet())
            if (requiredComplexMolecules.get(molecule) > 0)
                return false;
        return requiredMass == 0;
    }

    /**
     * Make progress on the project
     * @param delta Change in time
     */
    public abstract void progress(float delta);

    public abstract float energyToMakeProgress(float delta);

    public abstract float massToMakeProgress(float delta);

    public abstract float complexMoleculesToMakeProgress(float delta, Food.ComplexMolecule molecule);
}