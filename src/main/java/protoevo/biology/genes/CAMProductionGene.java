package protoevo.biology.genes;

import protoevo.biology.CellAdhesion;
import protoevo.core.Simulation;

import java.util.HashMap;
import java.util.Map;

public class CAMProductionGene extends Gene<Map<CellAdhesion.CellAdhesionMolecule, Float>> {

    public CAMProductionGene() {
        super();
    }

    public CAMProductionGene(Map<CellAdhesion.CellAdhesionMolecule, Float> value) {
        super(value);
    }

    @Override
    public <G extends Gene<Map<CellAdhesion.CellAdhesionMolecule, Float>>> G createNew(
            Map<CellAdhesion.CellAdhesionMolecule, Float> value) {
        return (G) new CAMProductionGene(value);
    }

    @Override
    public <G extends Gene<Map<CellAdhesion.CellAdhesionMolecule, Float>>> G mutate(Gene<?>[] genes) {
        Map<CellAdhesion.CellAdhesionMolecule, Float> map = getValue();
        Map<CellAdhesion.CellAdhesionMolecule, Float> newMap = new HashMap<>();
        for (CellAdhesion.CellAdhesionMolecule cam : map.keySet()) {
            if (Simulation.RANDOM.nextBoolean()) {
                newMap.put(cam, map.get(cam));
            } else {
                newMap.put(cam, Simulation.RANDOM.nextFloat());
            }
        }
        CellAdhesion.CellAdhesionMolecule newCAM = CellAdhesion.randomCAM();
        newMap.put(newCAM, Simulation.RANDOM.nextFloat());
        return createNew(newMap, getNumMutations() + 1);
    }

    @Override
    public boolean canDisable() {
        return true;
    }

    @Override
    public Map<CellAdhesion.CellAdhesionMolecule, Float> disabledValue() {
        return getNewValue();
    }

    @Override
    public Map<CellAdhesion.CellAdhesionMolecule, Float> getNewValue() {
        return new HashMap<>();
    }

    @Override
    public String getTraitName() {
        return "CAM Production";
    }

    @Override
    public String valueString() {
        Map<CellAdhesion.CellAdhesionMolecule, Float> map = getValue();
        StringBuilder str = new StringBuilder();
        for (CellAdhesion.CellAdhesionMolecule cam : map.keySet())
            str.append(cam.toString()).append(";")
                    .append(map.get(cam).toString()).append(";")
                    .append(cam.getJunctionType().toString().charAt(0));
        return str.toString();
    }
}
