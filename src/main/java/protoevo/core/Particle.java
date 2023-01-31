package protoevo.core;

import protoevo.env.Rock;
import protoevo.env.Tank;
import protoevo.utils.Geometry;
import protoevo.utils.Vector2;

import java.awt.*;
import java.io.Serializable;
import java.util.Iterator;

public class Particle extends Collidable implements Serializable {

    private static final long serialVersionUID = -4333766895269415282L;
    private Vector2 pos, prevPos, vel;
    private final Vector2 acc = new Vector2(0, 0);
    private float radius;
    private final Tank tank;
    private int recentRigidCollisions;

    public Particle(Tank tank) {
        this.tank = tank;
    }

    public void resetPhysics() {
        acc.set(0, 0);
        recentRigidCollisions = 0;
    }

    public void physicsUpdate(float delta) {
        float subStepDelta = delta / Settings.physicsSubSteps;
        for (int i = 0; i < Settings.physicsSubSteps; i++)
            physicsStep(subStepDelta);
    }

    public void physicsStep(float delta) {
        ChunkManager chunkManager = tank.getChunkManager();
        Iterator<Collidable> entities = chunkManager.broadCollisionDetection(getPos(), radius);
        entities.forEachRemaining(o -> handlePotentialCollision(o, delta));
        if (prevPos == null)
            prevPos = pos.copy();

        if (delta != 0)
            vel = pos.sub(prevPos).scale(1 / delta);
        else
            return;

        move(delta);
    }

    public void move(float delta)
    {
        Vector2 verletVel = pos.sub(prevPos).scale(1f - Settings.tankFluidResistance);
        Vector2 dx = verletVel.translate(acc.mul(delta * delta));
        float maxTravel = Settings.maxParticleSpeed * delta;
        if (dx.len2() > maxTravel * maxTravel)
            dx.setLength(maxTravel);
        prevPos.set(pos);
        pos.translate(dx);
    }

    public void handleBindingConstraint(Particle attached) {
        Vector2 axis = getPos().sub(attached.getPos());
        float dist = axis.len();
        float targetDist = 1.1f * (getRadius() + attached.getRadius());
        float offset = targetDist - dist;
        Vector2 axisNorm = axis.unit();
        float myMass = getMass();
        float theirMass = attached.getMass();
        float p = myMass / (myMass + theirMass);
        getPos().translate(axisNorm.mul((1 - p) * offset));
        attached.getPos().translate(axisNorm.mul(-p * offset));
    }

    public void accelerate(Vector2 da) {
        acc.translate(da);
    }

    @Override
    public boolean pointInside(Vector2 p) {
        return Geometry.isPointInsideCircle(getPos(), getRadius(), p);
    }

    @Override
    public boolean rayIntersects(Vector2 start, Vector2 end) {
        return false;
    }

    private final Vector2 tmp = new Vector2(0, 0);
    private final Vector2[] collision = new Vector2[]{
            new Vector2(0, 0), new Vector2(0, 0)
    };

    public void rayCollisions(Vector2 start, Vector2 end, Collision[] collisions) {
        for (Collision collision : collisions)
            collision.collided = false;

        Vector2 ray = end.take(start).nor();
        Vector2 p = collisions[0].point.set(getPos()).take(start);

        float a = ray.len2();
        float b = -2 * ray.dot(p);
        float c = p.len2() - getRadius() * getRadius();

        float d = b*b - 4*a*c;
        boolean doesIntersect = d != 0;
        if (!doesIntersect)
            return;

        float l1 = (float) ((-b + Math.sqrt(d)) / (2*a));
        float l2 = (float) ((-b - Math.sqrt(d)) / (2*a));

        if (l1 > 0) {
            collisions[0].collided = true;
            collisions[0].point.set(start).translate(ray.getX() * l1, ray.getY() * l1);
        } else if (l2 > 0) {
            collisions[1].collided = true;
            collisions[1].point.set(start).translate(ray.getX() * l2, ray.getY() * l2);
        }
    }

    @Override
    public boolean handlePotentialCollision(Collidable other, float delta) {
        if (other instanceof Particle)
            return handlePotentialCollision((Particle) other, delta);
        else if (other instanceof Rock)
            return handlePotentialCollision((Rock) other, delta);
        return false;
    }

    public void onParticleCollisionCallback(Particle p, float delta) {}

    private final Vector2 tmp1 = new Vector2(0, 0);
    public void handleParticleCollision(Particle p, float delta) {
        float mr = p.getMass() / (p.getMass() + getMass());
        Vector2 axis = tmp1.set(getPos()).take(p.getPos());
        float dist = axis.len();
        float targetDist = (getRadius() + p.getRadius());
        float offset = targetDist - dist;
        Vector2 axisNorm = axis.nor();
        getPos().translate(axisNorm.scale(mr * offset));
        p.getPos().translate(axisNorm.scale(-(1 - mr) / mr));
        onParticleCollisionCallback(p, delta);
    }

    public boolean handlePotentialCollision(Particle e, float delta) {
        if (e == this)
            return false;

        float sqDist = e.getPos().squareDistanceTo(getPos());
        float r = getRadius() + e.getRadius();

        if (sqDist < r*r)
            handleParticleCollision(e, delta);

        return true;
    }

    public void onRockCollisionCallback(Rock rock, float delta) {}

    public boolean handlePotentialCollision(Rock rock, float delta) {
        Vector2[][] edges = rock.getEdges();
        Vector2 pos = getPos();
        float r = getRadius();

        for (int i = 0; i < edges.length; i++) {
            Vector2[] edge = edges[i];
            Vector2 normal = rock.getNormals()[i];
            Vector2 dir = edge[1].sub(edge[0]);
            Vector2 x = pos.sub(edge[0]);

            if (dir.dot(normal) > 0)
                continue;

            float[] coefs = Geometry.circleIntersectLineCoefficients(dir, x, r);
            if (Geometry.lineIntersectCondition(coefs)) {
                float t1 = coefs[0], t2 = coefs[1];
                float t = (t1 + t2) / 2f;
                float offset = r - x.sub(dir.mul(t)).len();
                pos.translate(normal.mul(offset));
                recentRigidCollisions++;
                onRockCollisionCallback(rock, delta);
                return true;
            }
        }
        return false;
    }

    public boolean isCollidingWith(Collidable other) {
        if (other instanceof Particle)
            return isCollidingWith((Particle) other);
        else if (other instanceof Rock)
            return isCollidingWith((Rock) other);
        return false;
    }

    public boolean isCollidingWith(Rock rock) {
        Vector2[][] edges = rock.getEdges();
        float r = getRadius();
        Vector2 pos = getPos();

        if (rock.pointInside(pos))
            return true;

        for (Vector2[] edge : edges) {
            if (Geometry.doesLineIntersectCircle(edge, pos, r))
                return true;
        }
        return false;
    }

    public boolean isCollidingWith(Particle other)
    {
        if (other == this)
            return false;
        float r = getRadius() + other.getRadius();
        return other.getPos().squareDistanceTo(getPos()) < r*r;
    }

    public Vector2 getPos() {
        return pos;
    }

    public void setPos(Vector2 pos) {
        this.pos = pos;
    }

    public Vector2 getVel() {
        if (vel == null)
            return new Vector2(0, 0);
        return vel;
    }

    public float getSpeed() {
        if (vel == null)
            return 0f;
        return vel.len();
    }

    public float getMass() {
        return getMass(getRadius());
    }

    public float getMass(float r) {
        return getMass(r, 0);
    }

    public float getMass(float r, float extraMass) {
        return Geometry.getSphereVolume(r) * getMassDensity() + extraMass;
    }

    public float getMassDensity() {
        return 1000f;
    }

    public Vector2[] getBoundingBox() {
        float x = pos.getX();
        float y = pos.getY();
        float r = getRadius();
        return new Vector2[]{new Vector2(x - r, y - r), new Vector2(x + r, y + r)};
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        if (this.radius > Settings.maxParticleRadius)
            this.radius = Settings.maxParticleRadius;
        if (this.radius < Settings.minParticleRadius)
            this.radius = Settings.minParticleRadius;
    }

    public Tank getTank() {
        return tank;
    }

    public int getRecentRigidCollisions() {
        return recentRigidCollisions;
    }

    @Override
    public Color getColor() {
        return Color.WHITE.darker();
    }
}
