package padda;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.List;
import java.util.LinkedList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.lwjgl.util.vector.Matrix4f;

/**
 * Describes a three-dimensional world
 * z is up/down
 * 
 * 
 * This maps screen (x y) to world (x y z)
 * and vice versa
 * */
public class World {
    
    static public World instance_;
    
    private Tile[][] tiles_;
    private int tilesCols_;
    private int tilesRows_;
    private float tileSize_;
    private Image groundTile_;
    public List<Sprite> tileSprites_;
    
    // Position of view on screen
    public Vector2f origin_;
    
    // Transformation matrix for converting world-to-screen
    private Matrix4f transform_;
    // Transformation matrix for converting screen-to-world
    private Matrix4f transformInv_;
    // Projection for depth check
    private Vector2f depthVector_;
    
    public Vector3f gravity_;
        
    public World(Vector3f gravity) {
        origin_ = new Vector2f(500.0f, 200.0f);
        
        transform_ = new Matrix4f();
        // Init as isometric world
        transform_.translate(origin_);
        // Rotate by 45 degrees around x
        transform_.rotate((float)(Math.PI/3.0), new Vector3f(1.0f,0.0f,0.0f));
        // Rotate by 45 degrees around z
        transform_.rotate((float)(Math.PI/4.0), new Vector3f(0.0f,0.0f,1.0f));
        // Store the inverse
        transformInv_ = new Matrix4f();
        transformInv_.load(transform_);
        transformInv_.invert();
        
        // Set up depth vector for depth ordering
        depthVector_ = new Vector2f();
        depthVector_.x = depthVector_.y = 1.0f/(float)Math.sqrt(2.0f);
        
        // Init gravity
        gravity_ = gravity;
        
        // Set up tiles
        try {
            groundTile_ = new Image("entities/ground.png");
        } catch(SlickException e) {
            System.out.println(e);
            return;
        }
        tileSprites_ = new LinkedList<Sprite>();
        tilesCols_ = 40;
        tilesRows_ = 40;
        tileSize_ = 36.0f;
        tiles_ = new Tile[tilesCols_][tilesRows_];
        for (int x = 0; x < tilesCols_; x++) {
            for (int y = 0; y < tilesRows_; y++) {
                Tile tile = new Tile(0, (float)Math.random()*20.0f);
                tiles_[x][y] = tile;
                Vector2f pos = this.worldToScreen(this.tileToWorld(tile, x, y));
                tileSprites_.add(new Sprite(groundTile_, 
                                            pos.x, 
                                            pos.y, 
                                            this.worldToDepth(this.tileToWorld(tile, x, y)),
                                            1.0f));
            }
        }
        instance_ = this;
    }
    
    
    public Vector2f worldToScreen(Vector3f v) {
        Vector4f input = new Vector4f(v.x, v.y, v.z, 1.0f);
        Vector4f result = new Vector4f();
        Matrix4f.transform(transform_, input, result);
        Vector2f output = new Vector2f(result.x, result.y);
        return output;
    }
    
    public Vector3f worldToScreenWithDepth(Vector3f v) {
        Vector4f input = new Vector4f(v.x, v.y, v.z, 1.0f);
        Vector4f result = new Vector4f();
        Matrix4f.transform(transform_, input, result);
        // Find depth
        Vector2f point = new Vector2f(v.x, v.y);
        float p = Vector2f.dot(point, depthVector_) / (depthVector_.length() * depthVector_.length());
        float depth = new Vector2f(depthVector_).scale(p).length();
        depth *= p < 0 ? -1.0f : 1.0f;
        
        Vector3f output = new Vector3f(result.x, result.y, depth);
        return output;
    }
    
    public float worldToDepth(Vector3f v) {
        Vector2f point = new Vector2f(v.x, v.y);
        float p = Vector2f.dot(point, depthVector_) / (depthVector_.length() * depthVector_.length());
        float depth = new Vector2f(depthVector_).scale(p).length();
        depth *= p < 0 ? -1.0f : 1.0f;
        return depth;
    }
    
    public Vector3f screenToWorld(Vector2f v) {
        Vector4f input = new Vector4f(v.x, v.y, 0.0f, 1.0f);
        Vector4f result = new Vector4f();
        Matrix4f.transform(transformInv_, input, result);
        Vector3f output = new Vector3f(result.x, result.y, 0.0f);
        return output;
    }
    
    public Tile getTile(Vector3f p) {
        float xOffset = (float)tilesCols_ / 2.0f;
        float yOffset = (float)tilesRows_ / 2.0f;
        int x = (int) (p.x / (float)tileSize_ + xOffset);
        int y = (int) (p.y / (float)tileSize_ + yOffset);
        if (x < tilesCols_ 
            && x >= 0 
            && y < tilesRows_ 
            && y >= 0) {
            return tiles_[x][y];
        }
        else {
            return new Tile(0, 0.0f);
        }
    }
    
    /**
     * return center
     * */
    public Vector3f tileToWorld(Tile tile, int x, int y) {
        float xOffset = (float)tilesCols_ / 2.0f;
        float yOffset = (float)tilesRows_ / 2.0f;
        return new Vector3f(((float)x - xOffset) * tileSize_ + tileSize_/2.0f, 
                            ((float)y - yOffset) * tileSize_ + tileSize_/2.0f, 
                            tile.z_);
    }
    
    
    public void draw(Graphics g) {
        
        // Axes
        Vector3f xAxis = new Vector3f(100.0f, 0.0f, 0.0f);
        Vector3f yAxis = new Vector3f(0.0f, 100.0f, 0.0f);
        Vector3f zAxis = new Vector3f(0.0f, 0.0f, 100.0f);
        
        Vector2f xAxis2 = this.worldToScreen(xAxis);
        Vector2f yAxis2 = this.worldToScreen(yAxis);
        Vector2f zAxis2 = this.worldToScreen(zAxis);
        
        g.setColor(Color.red);
        g.drawLine(origin_.x, origin_.y, xAxis2.x, xAxis2.y);
        g.setColor(Color.blue);
        g.drawLine(origin_.x, origin_.y, yAxis2.x, yAxis2.y);
        g.setColor(Color.green);
        g.drawLine(origin_.x, origin_.y, zAxis2.x, zAxis2.y);
        
        // Tiles
        for (int x = 0; x < tilesCols_; x++) {
            for (int y = 0; y < tilesRows_; y++) {
                Tile tile = tiles_[x][y];
                switch (tile.type_) {
                    case 0:
                        g.setColor(Color.green);
                        break;
                    case 1:
                        g.setColor(Color.black);
                        break;
                }
                Vector2f pos = this.worldToScreen(this.tileToWorld(tile, x, y));
                //g.drawLine(pos.x, pos.y, pos.x, pos.y + 1.0f);
                groundTile_.draw(pos.x - 32, 
                                 pos.y - 16);
            }
        }
    }
    
}
