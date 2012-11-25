package padda;

import org.newdawn.slick.Image;
import org.lwjgl.util.vector.Vector2f;

public class Sprite implements Comparable<Sprite> {
    
    private float depth_;
    private Image img_;
    private float x_;
    private float y_;
    private float scale_;
    
    public Sprite(Image img, float x, float y, float depth, float scale) {
        img_ = img;
        depth_ = depth;
        x_ = x;
        y_ = y;
        scale_ = scale;
    }
    
    public void draw() {
        img_.draw(x_, y_, scale_);
    }


    public int compareTo(Sprite other) {
        if (this.depth_ < other.depth_) {
            return -1;
        }
        else if (this.depth_ > other.depth_) {
            return 1;
        }
        else {
            return 0;
        }
    }
    
}
