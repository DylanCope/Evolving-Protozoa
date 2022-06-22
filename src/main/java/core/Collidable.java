package core;

import utils.Vector2;

import java.awt.*;

public abstract class Collidable {

    public abstract boolean pointInside(Vector2 p);
    public abstract boolean rayIntersects(Vector2 start, Vector2 end);
    public abstract Vector2[] rayCollisions(Vector2 start, Vector2 end);

    public abstract Color getColor();

    public abstract Vector2[] getBoundingBox();

    public abstract boolean handlePotentialCollision(Collidable other, float delta);

}
