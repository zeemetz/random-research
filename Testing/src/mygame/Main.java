package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import de.lessvoid.nifty.Nifty;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    static playingState s = new playingState();
    BulletAppState appState = new BulletAppState();
    ARController aRController = new ARController();
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        NiftyJmeDisplay display = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, viewPort);
        Nifty nifty = display.getNifty();
        guiViewPort.addProcessor(display);
        nifty.fromXml("Interface/lollol.xml", "start", aRController);        
    }

    public BulletAppState getB() {
        return appState;
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        
    }
}
