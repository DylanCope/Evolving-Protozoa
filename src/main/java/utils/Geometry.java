package utils;

public class Geometry {

    public static float[] circleIntersectLineCoefficients(Vector2 dir, Vector2 x, float r) {
        float a = dir.len2();
        float b = -2*dir.dot(x);
        float c = x.len2() - r*r;
        float disc = b*b - 4*a*c;
        if (disc < 0)
            return null;

        float t1 = (float) ((-b + Math.sqrt(disc)) / (2*a));
        float t2 = (float) ((-b - Math.sqrt(disc)) / (2*a));

        return new float[]{t1, t2};
    }

    public static boolean lineIntersectCondition(float[] coefs) {
        if (coefs == null)
            return false;
        float t1 = coefs[0], t2 = coefs[1];
        return (0 < t1 && t1 < 1) || (0 < t2 && t2 < 1);
    }

    public static boolean doesLineIntersectCircle(Vector2[] line, Vector2 circlePos, float circleR) {
        Vector2 dir = line[1].sub(line[0]);
        Vector2 x = circlePos.sub(line[0]);
        float[] intersectionCoefs = circleIntersectLineCoefficients(dir, x, circleR);
        return lineIntersectCondition(intersectionCoefs);
    }

}
