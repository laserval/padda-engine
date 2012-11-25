package padda;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Color;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Input;

import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class PlayingGameState extends BasicGameState {
    
    private List<Entity> entities_;
    
    // Player control
    private Entity player_;
    private boolean[] playerInput_ = {false, false, false, false, false};
    private Vector3f playerVectorNorth_;
    private Vector3f playerVectorWest_;
    private Vector3f playerVectorSouth_;
    private Vector3f playerVectorEast_;
    private Vector3f playerVectorJump_;
    private int playerJumpCounter_ = 0;
    private int playerJumpCounterLimit_ = 100;
    
    private World world_;
    // Center of view in world
    public Vector3f viewPos_;
    
    private boolean updateDrawn_ = false;
    
    private float deltaScaling = 0.001f;
    
    
    // Sprite list
    private List<Sprite> renderList_;
    
    public static final int ID = 10;
    private StateBasedGame game_;
    
    public int getID() {
        return ID;
    }
    
    public void init(GameContainer container, StateBasedGame game) throws SlickException { 
        game_ = game;
        entities_ = new LinkedList<Entity>();
        renderList_ = new LinkedList<Sprite>();
        world_ = new World(new Vector3f(0.0f, 0.0f, -1500.0f));
        
        Image[] playerImages = new Image[1];
		try {
			playerImages[0] = new Image("entities/box.png");
		} catch(SlickException e) {
			System.out.println(e);
			return;
		}

		Animation playerSprite = new Animation(false);
		playerSprite.addFrame(playerImages[0], 1);
        
        player_ = new Entity("thing", 1.0f, 1000.0f, playerSprite);
        entities_.add(player_);
        player_.setPosition(new Vector3f(50.0f, 50.0f, 0.0f));
        
        
        playerVectorNorth_ = new Vector3f(-1.0f, -1.0f, 0.0f).normalise(null);
        playerVectorWest_ = new Vector3f(-1.0f, 1.0f, 0.0f).normalise(null);
        playerVectorSouth_ = new Vector3f(1.0f, 1.0f, 0.0f).normalise(null);
        playerVectorEast_ = new Vector3f(1.0f, -1.0f, 0.0f).normalise(null);
        playerVectorJump_ = new Vector3f(0.0f, 0.0f, 4000.0f);
    }
    
    
    public void render(GameContainer gc, StateBasedGame game, Graphics g) {
        renderList_.clear();
        //g.setBackground(Color.white);
        
        //world_.draw(g);
        renderList_.addAll(world_.tileSprites_);
        
        for (Entity entity : entities_) {
            renderList_.add(entity.drawShadow());
            renderList_.add(entity.draw());
        }
        
        Collections.sort(renderList_);
        
        for (Sprite spr : renderList_) {
            spr.draw();
        }
    }
    
    public void update(GameContainer gc, StateBasedGame game, int delta) {
        float timeDelta = (float)delta * deltaScaling;
        // Player input
        if (playerInput_[0]) player_.propel(playerVectorNorth_);
        if (playerInput_[1]) player_.propel(playerVectorWest_);
        if (playerInput_[2]) player_.propel(playerVectorSouth_);
        if (playerInput_[3]) player_.propel(playerVectorEast_);
        if (playerInput_[4] && playerJumpCounter_ < playerJumpCounterLimit_) {
            player_.impulse(playerVectorJump_);
        }
        
        // Enitity handling
        for (Entity entity : entities_) {
            Tile currentTile = world_.getTile(entity.getPosition());
            entity.setTile(currentTile);
            entity.update(timeDelta);
            
            
            // Gravity
            if (entity.getPosition().z > 0.01f + currentTile.top()) {
                entity.impulse(world_.gravity_);
            }
            else {
                entity.putOnGround();
                // Apply friction
                entity.friction(0.1f);
            }
            // Apply motion
            entity.move(timeDelta);
            
            // Collision checking
            for (Entity other : entities_) {
                if (!entity.equals(other)) {
                    if (entity.collidesWith(other)) {
                        entity.setPosition(entity.closestNonCollidingPoint(other));
                        entity.reduceVelocity(entity.collisionVector(other));
                    }
                }
            }
            
            
            //System.out.println(entity);
        }
        
        playerJumpCounter_ += delta;
        
    }
    
    public void enter(GameContainer gc, StateBasedGame game) throws SlickException {
        
    }
    
    public void leave(GameContainer gc, StateBasedGame game) throws SlickException {
        
    }
    
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_W) {
            playerInput_[0] = true;
        }
        if (key == Input.KEY_A) {
            playerInput_[1] = true;
        }
        if (key == Input.KEY_S) {
            playerInput_[2] = true;
        }
        if (key == Input.KEY_D) {
            playerInput_[3] = true;
        }
        if (key == Input.KEY_SPACE 
            && player_.onGround()) {
            playerInput_[4] = true;
            playerJumpCounter_ = 0;
        }
        if (key == Input.KEY_P) {
            Image[] playerImages = new Image[1];
            try {
                playerImages[0] = new Image("entities/box.png");
            } catch(SlickException e) {
                System.out.println(e);
                return;
            }

            Animation otherthingSprite = new Animation(false);
            otherthingSprite.addFrame(playerImages[0], 1);
            Updater updater = new Updater() {
                private float angle = 0.0f;
                private float angleVelocity = (float)(Math.PI/(Math.random()*3.0 + 0.1));
                private Vector3f vec = new Vector3f(1.0f, 0.0f, 0.0f);
                
                public void update(float delta) {
                    angle += angleVelocity * delta;
                    vec.x = (float)Math.cos(angle);
                    vec.y = (float)Math.sin(angle);
                    parent_.propel(vec);
                }
            };
            entities_.add(new Entity("rotator", 1.0f, (float)Math.random()*3000.0f, otherthingSprite, updater));
        }
    }
    
    public void keyReleased(int key, char c) {
        if (key == Input.KEY_W) {
            playerInput_[0] = false;
        }
        if (key == Input.KEY_A) {
            playerInput_[1] = false;
        }
        if (key == Input.KEY_S) {
            playerInput_[2] = false;
        }
        if (key == Input.KEY_D) {
            playerInput_[3] = false;
        }
        if (key == Input.KEY_SPACE) {
            playerInput_[4] = false;
        }
    }
}
