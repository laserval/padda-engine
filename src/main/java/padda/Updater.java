package padda;


/**
 * An update function wrapper
 * A parent entity must be assigned to the updater
 * This parent is then accessible in the update function
 * */
public abstract class Updater {

    protected Entity parent_;
    
    protected Updater() {
        
    }
    
    protected Updater(Entity parent) {
        parent_ = parent;
    }
    
    public void setParent(Entity parent) {
        parent_ = parent;
    }

    abstract public void update(float delta);
    
    

}
