package biology;

public class FoodType {

    public enum EnergyRichMolecule {

    }

    private float mass;
    private float energyDensity;

    public FoodType(float mass, float energyDensity) {
        this.mass = mass;
        this.energyDensity = energyDensity;
    }

    public float getMass() {
        return mass;
    }

    private float getEnergyDensity() {
        return energyDensity;
    }
}
