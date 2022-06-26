package env;

import biology.Cell;
import core.Collidable;
import core.Simulation;
import utils.Vector2;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class Rock extends Collidable implements Serializable {
    public static final long serialVersionUID = 1L;

    private final Vector2[] points;
    private final Vector2[][] edges;
    private final boolean[] edgeAttachStates;
    private final Vector2 centre;
    private final Vector2[] normals;
    private final Vector2[] boundingBox;
    private final Color colour;

    public Rock(Vector2 p1, Vector2 p2, Vector2 p3) {
        points = new Vector2[]{p1, p2, p3};
        edges = new Vector2[][]{
                {points[0], points[1]},
                {points[1], points[2]},
                {points[0], points[2]}
        };
        edgeAttachStates = new boolean[]{false, false, false};
        centre = computeCentre();
        normals = computeNormals();
        colour = randomRockColour();
        boundingBox = computeBounds();
    }

    private Vector2 computeCentre() {
        Vector2 c = new Vector2(0, 0);
        for (Vector2 p : points)
            c.translate(p);
        return c.scale(1f / points.length);
    }

    private Vector2[] computeBounds() {
        float minX = Math.min(points[0].getX(), Math.min(points[1].getX(), points[2].getX()));
        float minY = Math.min(points[0].getY(), Math.min(points[1].getY(), points[2].getY()));
        float maxX = Math.max(points[0].getX(), Math.max(points[1].getX(), points[2].getX()));
        float maxY = Math.max(points[0].getY(), Math.max(points[1].getY(), points[2].getY()));
        return new Vector2[]{new Vector2(minX, minY), new Vector2(maxX, maxY)};
    }

    public Vector2[] getBoundingBox() {
        return boundingBox;
    }

    @Override
    public boolean handlePotentialCollision(Collidable other, float delta) {
        if (other instanceof Cell)
            return ((Cell) other).handlePotentialCollision(this, delta);
        return false;
    }

    public Vector2[][] getEdges() {
        return edges;
    }

    public boolean isEdgeAttached(int edgeIdx) {
        return edgeAttachStates[edgeIdx];
    }

    public void setEdgeAttached(int edgeIdx) {
        edgeAttachStates[edgeIdx] = true;
    }

    private Vector2[] computeNormals() {
        Vector2[] normals = new Vector2[3];
        Vector2[][] edges = getEdges();
        for (int i = 0; i < edges.length; i++)
            normals[i] = normal(edges[i][0], edges[i][1]);
        return normals;
    }

    private Vector2 normal(Vector2 p1, Vector2 p2) {
        Vector2 n = p1.sub(p2).perp().unit();
        if (n.dot(p1.sub(centre)) < 0)
            n.scale(-1);
        return n;
    }

    public Vector2[] getPoints() {
        return points;
    }

    public Vector2[] getEdge(int i) {
        Vector2[][] edges = getEdges();
        return edges[i];
    }

    public Vector2[] getNormals() {
        return normals;
    }

    private float sign(Vector2 p1, Vector2 p2, Vector2 p3) {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY())
                - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    public boolean pointInside(Vector2 x) {
        float d1 = sign(x, points[0], points[1]);
        float d2 = sign(x, points[1], points[2]);
        float d3 = sign(x, points[2], points[0]);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    @Override
    public boolean rayIntersects(Vector2 start, Vector2 end) {
        return false;
    }

    @Override
    public Vector2[] rayCollisions(Vector2 start, Vector2 end) {
        Vector2[] ray = new Vector2[]{start, end};
        Vector2 dirRay = ray[1].sub(ray[0]);
        ArrayList<Vector2> collisions = new ArrayList<>(edges.length * 2);
        for (int i = 0; i < edges.length; i++) {
            if (isEdgeAttached(i))
                continue;

            Vector2[] edge = edges[i];
            Vector2 dirEdge = edge[1].sub(edge[0]);
            float[] coefs = edgesIntersectCoef(ray[0], dirRay, edge[0], dirEdge);
            if ((coefs != null) && edgeIntersectCondition(coefs))
                collisions.add(ray[0].add(dirRay.mul(coefs[0])));

        }
        return collisions.toArray(new Vector2[0]);
    }

    public boolean intersectsWith(Rock otherRock) {
        for (Vector2[] e1 : otherRock.getEdges())
            for (Vector2[] e2 : getEdges())
                if (edgesIntersect(e1, e2))
                    return true;
        return false;
    }

    public static float[] edgesIntersectCoef(Vector2 start1, Vector2 dir1, Vector2 start2, Vector2 dir2) {

        float[][] coefs = new float[][]{
                {dir1.len2(), -dir1.dot(dir2)},
                {-dir2.dot(dir1), dir2.len2()}
        };

        float[] consts = new float[]{
                start2.dot(dir1) - start1.dot(dir1),
                start1.dot(dir2) - start2.dot(dir2),
        };

        float det = coefs[0][0] * coefs[1][1] - coefs[1][0] * coefs[0][1];

        if (det == 0)
            return null;

        float t1 = (consts[0]*coefs[1][1] - consts[1]*coefs[0][1]) / det;
        float t2 = (-consts[0]*coefs[1][0] + consts[1]*coefs[0][0]) / det;

        return new float[]{t1, t2};
    }

    public static boolean edgeIntersectCondition(float[] coefs) {
        float t1 = coefs[0], t2 = coefs[1];
        return 0f < t1 && t1 < 1f && 0f < t2 && t2 < 1f;
    }

    public static boolean edgesIntersect(Vector2 start1, Vector2 dir1, Vector2 start2, Vector2 dir2) {
        float[] coefs = edgesIntersectCoef(start1, dir1, start2, dir2);
        if (coefs == null)
            return false;
        return edgeIntersectCondition(coefs);
    }

    public static boolean edgesIntersect(Vector2[] e1, Vector2[] e2) {
        Vector2 dir1 = e1[1].sub(e1[0]);
        Vector2 dir2 = e2[1].sub(e2[0]);
        return edgesIntersect(e1[0], dir1, e2[0], dir2);
    }

    public Color getColor() {
        return colour;
    }

    public static Color randomRockColour() {
        int tone = 80 + Simulation.RANDOM.nextInt(20);
        int yellowing = Simulation.RANDOM.nextInt(20);
        return new Color(tone + yellowing, tone + yellowing, tone);
    }

    public boolean allEdgesAttached() {
        return isEdgeAttached(0) && isEdgeAttached(1) && isEdgeAttached(2);
    }

    public Vector2 getCentre() {
        return centre;
    }
}
