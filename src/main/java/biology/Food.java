package biology;

import core.Settings;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Food implements Serializable {

    public enum Type {
        Plant(Settings.plantEnergyDensity), Meat(Settings.meatEnergyDensity);

        private final float energyDensity;

        Type(float energyDensity) {
            this.energyDensity = energyDensity;
        }

        public float getEnergyDensity() {
            return energyDensity;
        }

        public static int numTypes() {
            return values().length;
        }
    }

    /**
     * Complex molecules are required for the construction of specialised cell behaviour.
     * The implemented molecules are:
     * (1) <a href="https://en.wikipedia.org/wiki/Retinal">Retinal</a>.
     * (2) <a href="https://en.wikipedia.org/wiki/Neurotransmitter">Neurotransmitter</a>.
     * (3) <a href="https://en.wikipedia.org/wiki/Trichocyst">Toxicyst</a>.
     */
    public enum ComplexMolecule {
        Retinal(1), Neurotransmitter(1), Toxicyst(1);

        private final float productionCost;

        ComplexMolecule(float productionCost) {
            this.productionCost = productionCost;
        }

        /**
         * @return energy required to produce one unit of the complex molecule
         */
        public float getProductionCost() {
            return productionCost;
        }
    }

    private float mass;
    private final Type type;
    private final Map<ComplexMolecule, Float> complexMoleculeMasses;

    public Food(float mass, Type foodType, HashMap<ComplexMolecule, Float> complexMoleculeMass) {
        this.mass = mass;
        this.type = foodType;
        this.complexMoleculeMasses = complexMoleculeMass;
    }

    public Food(float mass, Type foodType) {
        this(mass, foodType, new HashMap<>(0));
    }

    public Type getType() {
        return type;
    }

    public float getSimpleMass() {
        return mass;
    }

    public void addSimpleMass(float m) {
        mass += m;
    }

    public void subtractSimpleMass(float m) {
        mass = Math.max(0, mass - m);
    }

    public float getComplexMoleculeMass(ComplexMolecule molecule) {
        return complexMoleculeMasses.getOrDefault(molecule, 0f);
    }

    public Collection<ComplexMolecule> getComplexMolecules() {
        return complexMoleculeMasses.keySet();
    }

    public void subtractComplexMolecule(ComplexMolecule molecule, float extracted) {
        float currentAmount = getComplexMoleculeMass(molecule);
        complexMoleculeMasses.put(molecule, Math.max(0, currentAmount - extracted));
    }

    public Map<ComplexMolecule, Float> getComplexMoleculeMasses() {
        return complexMoleculeMasses;
    }

    public void addComplexMolecules(Map<ComplexMolecule, Float> masses) {
        for (ComplexMolecule molecule : masses.keySet())
            addComplexMoleculeMass(molecule, masses.get(molecule));
    }

    public void addComplexMoleculeMass(ComplexMolecule molecule, float mass) {
        float currentMass = complexMoleculeMasses.getOrDefault(molecule, 0f);
        complexMoleculeMasses.put(molecule, currentMass + mass);
    }

    public float getEnergy(float m) {
        return type.getEnergyDensity() * m;
    }

    @Override
    public String toString() {
        return type.name();
    }
}
