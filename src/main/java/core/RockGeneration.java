package core;

import biology.Entity;
import utils.Geometry;
import utils.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RockGeneration {

    public static void generateRocks(Tank tank) {
        List<Rock> unattachedRocks = new ArrayList<>();
        for (int i = 0; i < Settings.rockGenerationIterations; i++) {
            if (i < Settings.rockSeedingIterations
                    || unattachedRocks.size() == 0
                    || Simulation.RANDOM.nextFloat() > Settings.rockClustering) {
                Rock rock = newRock(tank);
                if (tryAdd(rock, tank.getRocks())) {
                    unattachedRocks.add(rock);
                }
            } else {
                Rock toAttach = selectRandomUnattachedRock(tank, unattachedRocks);
                int edgeIdx = 0;
                while (edgeIdx < 3) {
                    if (!toAttach.getEdgeAttachedState(edgeIdx))
                        break;
                    edgeIdx++;
                }
                if (edgeIdx == 3)
                    continue;

                Vector2[] edge = toAttach.getEdge(edgeIdx);
                Vector2 normal = toAttach.getNormals()[edgeIdx];
                Vector2 p1 = edge[0], p2 = edge[1];

                float sizeRange = (Settings.maxRockSize - Settings.minRockSize);
                float rockSize = Settings.minRockSize + sizeRange * Simulation.RANDOM.nextFloat();

                Vector2 p3 = p1.add(p2).scale(0.5f).translate(normal.unit().scale(rockSize));

                Vector2[] newEdge1 = new Vector2[]{p1, p3};
                Vector2[] newEdge2 = new Vector2[]{p2, p3};
                if (notInAnyRocks(newEdge1, newEdge2, tank.getRocks())
                        && leavesOpening(p3, tank.getRocks(), Settings.minRockOpeningSize)) {
                    Rock rock = new Rock(p1, p2, p3);
                    tank.getRocks().add(rock);
                    unattachedRocks.add(rock);
                    rock.setEdgeAttached(0);
                    toAttach.setEdgeAttached(edgeIdx);
                    if (edgeIdx == 2) // no edges left to attach to
                        unattachedRocks.remove(toAttach);
                }
            }
        }
    }

    private static Rock selectRandomUnattachedRock(Tank tank, List<Rock> unattachedRocks) {
        int i = Simulation.RANDOM.nextInt(unattachedRocks.size());
        return unattachedRocks.get(i);
    }

    private static boolean tryAdd(Rock rock, List<Rock> rocks) {
        for (Rock otherRock : rocks)
            if (rock.intersectsWith(otherRock))
                return false;
        rocks.add(rock);
        return true;
    }

    private static boolean notInAnyRocks(Vector2[] e1, Vector2[] e2, List<Rock> rocks) {
        for (Rock rock : rocks)
            for (Vector2[] rockEdge : rock.getEdges())
                if (Rock.edgesIntersect(rockEdge, e1) || Rock.edgesIntersect(rockEdge, e2))
                    return false;
        return true;
    }

    private static boolean leavesOpening(Vector2 rockPoint, List<Rock> rocks, float openingSize) {
        for (Rock rock : rocks) {
            for (Vector2[] edge : rock.getEdges()) {
                if (Geometry.doesLineIntersectCircle(edge, rockPoint, openingSize))
                    return false;
            }
        }
        return true;
    }

    public static Rock newRock(Tank tank) {
        float centreR = tank.getRadius() * Simulation.RANDOM.nextFloat();
        float centreT = (float) (2*Math.PI * Simulation.RANDOM.nextFloat());
        Vector2 centre = Vector2.fromAngle(centreT).setLength(centreR);

        float sizeRange = (Settings.maxRockSize - Settings.minRockSize);
        float rockSize = Settings.minRockSize + sizeRange * Simulation.RANDOM.nextFloat();


        Vector2 dir = Vector2.fromAngle((float) (2 * Math.PI * Simulation.RANDOM.nextFloat()));
        float k1 = 0.95f + 0.1f * Simulation.RANDOM.nextFloat();
        Vector2 p1 = centre.add(dir.setLength(k1 * rockSize));

        float tMin = Settings.minRockSpikiness;
        float tMax = (float) (2*Math.PI / 3);
        float t1 = tMin + (tMax - 2*tMin) * Simulation.RANDOM.nextFloat();
        float k2 = 0.95f + 0.1f * Simulation.RANDOM.nextFloat();
        dir = dir.rotate(t1);
        Vector2 p2 = centre.add(dir.setLength(k2 * rockSize));

        float t2 = tMin + (tMax - tMin) * Simulation.RANDOM.nextFloat();
        float l3 = Settings.minRockSize + sizeRange * Simulation.RANDOM.nextFloat();
        dir = dir.rotate(t2);
        Vector2 p3 = centre.add(dir.setLength(l3));

        return new Rock(p1, p2, p3);
    }
}
