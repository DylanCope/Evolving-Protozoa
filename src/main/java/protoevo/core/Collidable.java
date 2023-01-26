package protoevo.core;

import protoevo.utils.Vector2;

import java.awt.*;
import java.io.Serializable;

public abstract class Collidable {

    public static class Collision implements Serializable {
        public static final long serialVersionUID = 1L;

        public final Vector2 point = new Vector2(0, 0);
        public boolean collided;
    }

    public abstract boolean pointInside(Vector2 p);
    public abstract boolean rayIntersects(Vector2 start, Vector2 end);
    public abstract void rayCollisions(Vector2 start, Vector2 end, Collision[] collisions);

    public abstract Color getColor();

    public abstract Vector2[] getBoundingBox();

    public abstract boolean handlePotentialCollision(Collidable other, float delta);

}
