package protoevo.biology;

import protoevo.core.Settings;
import protoevo.core.Simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Implementation of <a href="https://en.wikipedia.org/wiki/Cell_adhesion">cell adhesion</a> logic.
 */
public class CellAdhesion implements Serializable  {

    private static final long serialVersionUID = 1L;

    /**
     * <b>Cell-adhesion molecules</b> (CAMs) can facilitate the following kinds of cell-junctions:
     *
     * (1) <b>Anchoring junctions</b>,
     * which maintain cells together and strengthens contact between cells.
     *
     * (2) <b>Occluding junctions</b>, which seal gaps between cells through
     * cell–cell contact, making an impermeable barrier for diffusion
     *
     * (3) <b>Channel-forming junctions</b>, which links cytoplasm of adjacent
     * cells allowing transport of molecules to occur between cells
     *
     * (3) <b>Signal-relaying junctions</b>, which can be synapses in the nervous system
     */
    public enum CAMJunctionType implements Serializable {
        ANCHORING("Anchoring"),
        OCCLUDING("Occluding"),
        CHANNEL_FORMING("Channel Forming"),
        SIGNAL_RELAYING("Signal Relaying");

        private final String name;

        CAMJunctionType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static CAMJunctionType randomJunctionType() {
            ArrayList<CAMJunctionType> junctionTypes = new ArrayList<>();
            if (Settings.enableAnchoringBinding)
                junctionTypes.add(ANCHORING);
            if (Settings.enableOccludingBinding)
                junctionTypes.add(OCCLUDING);
            if (Settings.enableChannelFormingBinding)
                junctionTypes.add(CHANNEL_FORMING);
            if (Settings.enableSignalRelayBinding)
                junctionTypes.add(SIGNAL_RELAYING);
            int idx = Simulation.RANDOM.nextInt(junctionTypes.size());
            return junctionTypes.get(idx);
        }
    }

    public static class CellBinding implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Cell srcCell, destCell;
        private final CellAdhesionMolecule cam;

        public CellBinding(Cell srcCell, Cell destCell, CellAdhesionMolecule cam) {
            this.srcCell = srcCell;
            this.destCell = destCell;
            this.cam = cam;
        }

        public Cell getSourceEntity() {
            return srcCell;
        }

        public Cell getDestinationEntity() {
            return destCell;
        }

        public CellAdhesionMolecule getCAM() {
            return cam;
        }

        public CAMJunctionType getJunctionType() {
            return cam.getJunctionType();
        }
    }

    public interface CellAdhesionMolecule extends Serializable {
        boolean bindsTo(CellAdhesionMolecule cam);
        CAMJunctionType getJunctionType();
        int getChemicalBindingSignature();

        default float getProductionCost() {
            return Settings.camProductionEnergyCost;
        }
    }

    private final static ConcurrentHashMap<Integer, CellAdhesionMolecule> existingCAMs =
            new ConcurrentHashMap<>(Settings.numPossibleCAMs);

    private static CellAdhesionMolecule newCAM(Function<Integer, CellAdhesionMolecule> camBuilder) {
        int newSignature = randomBindingSignature();
        if (existingCAMs.containsKey(newSignature))
            return existingCAMs.get(newSignature);
        CellAdhesionMolecule cam = camBuilder.apply(newSignature);
        existingCAMs.put(newSignature, cam);
        return cam;
    }

    public static CellAdhesionMolecule randomCAM() {
//        if (Simulation.RANDOM.nextBoolean())
            return newHomophilicCAM();
//        else
//            return newHeterophilicCAM();
    }

    public static CellAdhesionMolecule newHomophilicCAM() {
        return newCAM(CellAdhesion::buildHomophilicCAM);
    }

    public static CellAdhesionMolecule newHeterophilicCAM() {
        return newCAM(CellAdhesion::buildHeterophilicCAM);
    }

    private static CellAdhesionMolecule buildHomophilicCAM(int camSignature) {
        return new CellAdhesionMolecule() {
            private final int signature = camSignature;
            private final CAMJunctionType junctionType = CAMJunctionType.randomJunctionType();

            @Override
            public boolean bindsTo(CellAdhesionMolecule cam) {
                return getChemicalBindingSignature() == cam.getChemicalBindingSignature();
            }

            @Override
            public CAMJunctionType getJunctionType() {
                return junctionType;
            }

            @Override
            public int getChemicalBindingSignature() {
                return signature;
            }

            @Override
            public String toString() {
                return signature + "";
            }
        };
    }

    private static CellAdhesionMolecule buildHeterophilicCAM(int camSignature) {
        return new CellAdhesionMolecule() {
            private final int signature = camSignature;
            private final int bindingSignature = randomExistingBindingSignature();
            private final CAMJunctionType junctionType = CAMJunctionType.randomJunctionType();

            @Override
            public boolean bindsTo(CellAdhesionMolecule cam) {
                return bindingSignature == cam.getChemicalBindingSignature();
            }

            @Override
            public CAMJunctionType getJunctionType() {
                return junctionType;
            }

            @Override
            public int getChemicalBindingSignature() {
                return signature;
            }

            @Override
            public String toString() {
                return signature + "";
            }
        };
    }

    private static int randomExistingBindingSignature() {
        ConcurrentHashMap.KeySetView<Integer, CellAdhesionMolecule> keySet = existingCAMs.keySet();
        if (keySet.size() > 0) {
            int selectedIdx = Simulation.RANDOM.nextInt(keySet.size());
            int i = 0;
            for (Integer signature : keySet) {
                if (i == selectedIdx)
                    return signature;
                i++;
            }
        }
        return randomBindingSignature();
    }

    private static int randomBindingSignature() {
        return Simulation.RANDOM.nextInt(Settings.numPossibleCAMs);
    }

}
