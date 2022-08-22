package protoevo.env;

import protoevo.core.Settings;
import protoevo.core.Simulation;
import protoevo.utils.Geometry;
import protoevo.utils.Vector2;

import java.util.ArrayList;
import java.util.List;

public class RockGeneration {

    public static void generateRingOfRocks(Tank tank, Vector2 ringCentre, float ringRadius) {
        generateRingOfRocks(tank, ringCentre, ringRadius, 0f);
    }

    public static void generateRingOfRocks(Tank tank, Vector2 ringCentre, float ringRadius, float breakProb) {
        float angleDelta = (float) (2 * Math.asin(Settings.minRockSize / (2 * ringRadius)));
        Rock currentRock = null;
        for (float angle = 0; angle < 2*Math.PI; angle += angleDelta) {
            if (breakProb > 0 && Simulation.RANDOM.nextFloat() < breakProb) {
                currentRock = null;
                angle += angleDelta * 10;
            }
            if (currentRock == null || currentRock.allEdgesAttached()) {
                currentRock = newCircumferenceRockAtAngle(ringCentre, ringRadius, angle);
                if (isRockObstructed(currentRock, tank.getRocks(), Settings.minRockOpeningSize)) {
                    currentRock = null;
                } else {
                    tank.getRocks().add(currentRock);
                }
            } else {
                Rock bestNextRock = null;
                float bestRockDistToCirc = Float.MAX_VALUE;
                int bestRockAttachIdx = -1;
                for (int i = 0; i < currentRock.getEdges().length; i++) {
                    float sizeRange = (Settings.maxRockSize - Settings.minRockOpeningSize);
                    float rockSize = 1.5f * Settings.minRockOpeningSize + sizeRange * Simulation.RANDOM.nextFloat();
                    if (!currentRock.isEdgeAttached(i)) {
                        Rock newRock = newAttachedRock(currentRock, i, tank.getRocks(), rockSize);
                        if (newRock != null) {
                            float dist = Math.abs(newRock.getCentre().sub(ringCentre).len() - ringRadius);
                            if (dist < bestRockDistToCirc) {
                                bestRockDistToCirc = dist;
                                bestNextRock = newRock;
                                bestRockAttachIdx = i;
                            }
                        }
                    }
                }
                if (bestNextRock != null) {
                    tank.getRocks().add(bestNextRock);
                    bestNextRock.setEdgeAttached(0);
                    currentRock.setEdgeAttached(bestRockAttachIdx);
                }
                currentRock = bestNextRock;
            }
        }
    }

    private static Rock newCircumferenceRockAtAngle(Vector2 pos, float r, float angle) {
        Vector2 dir = Vector2.fromAngle(angle);
        Vector2 centre = dir.mul(r).add(pos);
        return newRockAt(centre, dir);
    }


    public static void generateRocks(Tank tank) {
        List<Rock> unattachedRocks = new ArrayList<>();
        for (Rock rock : tank.getRocks())
            if (!rock.allEdgesAttached())
                unattachedRocks.add(rock);



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
                    if (!toAttach.isEdgeAttached(edgeIdx))
                        break;
                    edgeIdx++;
                }
                if (edgeIdx == 3)
                    continue;

                Rock rock = newAttachedRock(toAttach, edgeIdx, tank.getRocks());
                if (rock != null) {
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

    public static Rock newAttachedRock(Rock toAttach, int edgeIdx, List<Rock> rocks) {
        float sizeRange = (Settings.maxRockSize - Settings.minRockSize);
        float rockSize = Settings.minRockSize + sizeRange * Simulation.RANDOM.nextFloat();
        return newAttachedRock(toAttach, edgeIdx, rocks, rockSize);
    }

    public static Rock newAttachedRock(Rock toAttach, int edgeIdx, List<Rock> rocks, float rockSize) {
        Vector2[] edge = toAttach.getEdge(edgeIdx);
        Vector2 normal = toAttach.getNormals()[edgeIdx];
        Vector2 p1 = edge[0], p2 = edge[1];

        Vector2 p3 = p1.add(p2).scale(0.5f).translate(normal.unit().scale(rockSize));

        Vector2[] newEdge1 = new Vector2[]{p1, p3};
        Vector2[] newEdge2 = new Vector2[]{p2, p3};
        if (notInAnyRocks(newEdge1, newEdge2, rocks, toAttach)
                && leavesOpening(p3, rocks, Settings.minRockOpeningSize)) {
            return new Rock(p1, p2, p3);
        }
        return null;
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

    private static boolean isRockObstructed(Rock rock, List<Rock> rocks, float openingSize) {
        for (Rock otherRock : rocks)
            if (otherRock.intersectsWith(rock))
                return true;
        if (openingSize > 0)
            for (Vector2 point : rock.getPoints())
                if (!leavesOpening(point, rocks, openingSize))
                    return true;
        return false;
    }

    private static boolean notInAnyRocks(Vector2[] e1, Vector2[] e2, List<Rock> rocks, Rock excluding) {
        for (Rock rock : rocks)
            for (Vector2[] rockEdge : rock.getEdges())
                if (!rock.equals(excluding) &&
                        (Rock.edgesIntersect(rockEdge, e1) || Rock.edgesIntersect(rockEdge, e2)))
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
        return newRockAt(centre);
    }

    public static Rock newRockAt(Vector2 centre) {
        Vector2 dir = Vector2.fromAngle((float) (2 * Math.PI * Simulation.RANDOM.nextFloat()));
        return newRockAt(centre, dir);
    }

    public static Rock newRockAt(Vector2 centre, Vector2 dir) {
        float sizeRange = (Settings.maxRockSize - Settings.minRockSize);
        float rockSize = Settings.minRockSize + sizeRange * Simulation.RANDOM.nextFloat();

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
