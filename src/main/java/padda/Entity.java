package padda;

import org.newdawn.slick.Renderable;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.util.FastTrig;
import org.newdawn.slick.util.LocatedImage;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * An entity is a physics-affected object in the world
 * 
 * It has a collision sphere
 * 
 * The natural order of entities is their draw order (depth)
 * */
public class Entity implements Comparable<Entity> {
    
    private String name_;
    
    // Position in the world
    private Vector3f position_;
    
    private Vector3f velocity_;
    
    private Vector3f acc_;
    
    private Tile currentTile_;
    
    // Atttributes
    private float mass_;
    private float propulsion_;
    private float propulsionAirFactor_ = 0.3f;
    
    // Update object
    private Updater updater_;
    
    // Collision
    private float height_;
    private Circle collisionMask_;
    
    // Graphics
    private Animation sprite_;
    private float shadowScale_ = 1.0f;
    
    // Shadow image used for all entities
    static protected Image shadow_;
    
    public Entity(String name, float mass, float propulsion, Animation sprite) {
        // Load shadow to class if not loaded
        if (shadow_ == null) {
            try {
                shadow_ = new Image("entities/shadow.png");
            } catch(SlickException e) {
                System.out.println(e);
                return;
            }
        }
        name_ = name;
        mass_ = mass;
        propulsion_ = propulsion;
        sprite_ = sprite;
        height_ = 5.0f;
        
        position_ = new Vector3f();
        velocity_ = new Vector3f();
        acc_ = new Vector3f();
        
        collisionMask_ = new Circle(0.0f, 0.0f, 15.0f);
        currentTile_ = new Tile(0, 0.0f);
    }
    
    public Entity(String name, float mass, float propulsion, Animation sprite, Updater updater) {
        this(name, mass, propulsion, sprite);
        updater_ = updater;
        updater_.setParent(this);
    }
    
    
    public void update(float delta) {
        
        if (updater_ != null) {
            updater_.update(delta);
        }
    }
    
    
    public Sprite draw() {
        World world = World.instance_;
        // Draw sprite at bottom center
        Vector2f spriteScreenPos = world.worldToScreen(position_);
        return new Sprite(sprite_.getCurrentFrame(), 
                        spriteScreenPos.x - sprite_.getWidth()/2.0f,
                        spriteScreenPos.y - sprite_.getHeight() + shadow_.getHeight()/3.0f,
                        World.instance_.worldToDepth(this.position_),
                        1.0f);
    }
    
    public Sprite drawShadow() {
        World world = World.instance_;
        // Draw shadow centered and scaled
        Vector2f shadowScreenPos = world.worldToScreen(new Vector3f(position_.x, position_.y, currentTile_.top()));
        return new Sprite(shadow_, 
                        shadowScreenPos.x - (shadow_.getWidth()/2.0f)*shadowScale_,
                        shadowScreenPos.y - (shadow_.getHeight()/2.0f)*shadowScale_,
                        World.instance_.worldToDepth(this.position_),
                        shadowScale_);
    }
    
    
    public boolean collidesWith(Entity other) {
        return collisionMask_.intersects(other.collisionMask_) 
                && !(this.position_.z > other.position_.z + other.height_)
                && !(this.position_.z + this.height_ < other.position_.z);
    }
    
    /**
     * Calculates the vector from this entity to another entity
     * */
    public Vector3f collisionVector(Entity other) {
        Vector3f vec = new Vector3f();
        Vector3f.sub(other.position_, this.position_, vec);
        return vec;
    }
    
    /**
     * Finds the closest non-colliding point to an entity that this
     * entity is colliding with.
     * */
    public Vector3f closestNonCollidingPoint(Entity other) {
        Vector3f vec = this.collisionVector(other);
        float l = vec.length();
        if (l == 0) {
            // If on exact same point
            double angle = (Math.random()*Math.PI*2.0);
            vec.x = (float)FastTrig.cos(angle);
            vec.y = (float)FastTrig.sin(angle);
            l = vec.length();
        }
        // Find vector it takes to move out of sphere
        float thisRadius = this.collisionMask_.getRadius();
        float otherRadius = other.collisionMask_.getRadius();
        vec.normalise().scale(thisRadius + otherRadius - l);
        Vector3f.sub(this.position_, vec, vec);
        return vec;
    }
    
    
    public Vector3f getPosition() {
        return position_;
    }
    
    public void setPosition(Vector3f pos) {
        position_.x = pos.x;
        position_.y = pos.y;
        position_.z = pos.z;
        
        collisionMask_.setLocation(position_.x, position_.y);
    }
    
    public void setZ(float z) {
        position_.z = z;
    }
    
    public void setPropulsion(float p) {
        propulsion_ = p;
    }
    
    /* *
     * Impulses
     * */
    public void impulse(Vector3f imp) {
        Vector3f.add(imp, acc_, acc_);
    }
    
    
    public void propel(Vector3f dir) {
        if (this.onGround()) {
            this.impulse((Vector3f)(new Vector3f(dir).scale(propulsion_)));
        }
        else {
            this.impulse((Vector3f)(new Vector3f(dir).scale(propulsion_ * propulsionAirFactor_)));
        }
    }
    
    public void friction(float f) {
        if (velocity_.length() < 0.1f) {
            velocity_.set(0.0f, 0.0f);
        }
        else {
            Vector3f momentum = (Vector3f)(new Vector3f(velocity_).scale(mass_).scale(f));
            Vector3f.sub(velocity_, momentum, velocity_);
        }
    }
    
    public void move(float delta) {
        // Add acceleration scaled by time elapsed
        Vector3f.add((Vector3f)(new Vector3f(acc_).scale(delta)), velocity_, velocity_);
        // Add distance travelled
        Vector3f.add((Vector3f)(new Vector3f(velocity_).scale(delta)), position_, position_);
        // Reset acceleration
        acc_.set(0.0f,0.0f,0.0f);
        // Scale shadow
        shadowScale_ = 1.0f - position_.z/100.0f > 0.1f ? 1.0f - position_.z/100.0f : 0.1f;
        // Move collision mask
        collisionMask_.setLocation(position_.x, position_.y);
    }
    
    public void reduceVelocity(Vector3f v) {
        float p = Vector3f.dot(velocity_, v) / (v.length() * v.length());
        v.normalise().scale(p);
        Vector3f.sub(velocity_, v, velocity_);
    }
    
    public boolean inAir() {
        return 0.0f != position_.z;
    }
    
    public boolean onGround() {
        return currentTile_.top() == position_.z;
    }
    
    public void putOnGround() {
        position_.z = currentTile_.top();
        velocity_.z = 0.0f;
    }
    
    public void setTile(Tile tile) {
        currentTile_ = tile;
    }
    
    public String toString() {
        return name_ + "\n" +
                "pos: " + position_ + "\n" +
                "v: " + velocity_ + "\n" +
                "depth: " + World.instance_.worldToDepth(position_);
    }
    
    // For draw order
    public int compareTo(Entity other) {
        float d1 = World.instance_.worldToDepth(this.position_);
        float d2 = World.instance_.worldToDepth(other.position_);
        if (d1 < d2) {
            return -1;
        }
        else if (d1 > d2) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
