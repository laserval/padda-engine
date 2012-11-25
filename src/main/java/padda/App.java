package padda;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import org.newdawn.slick.state.StateBasedGame;


public class App extends StateBasedGame {
    public static App instance_;
    
    private static int hres_ = 854;
    private static int vres_ = 480;
    private static boolean fullscreen_ = false;
    private static int fps_ = 60;
    
    public App() {
        super("PADDAN");
        instance_ = this;
    }
    
    public static void main(String[] args) throws Exception {
        AppGameContainer container = new AppGameContainer(new App());
        // Graphics setup
        container.setDisplayMode(hres_, vres_, fullscreen_);
        container.setTargetFrameRate(fps_);
        container.start();
    }
    
    public void initStatesList(GameContainer container) throws SlickException {
        // 
        this.addState(new PlayingGameState());
        
    }


}
