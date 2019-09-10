package mygame;

import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.AssetLinkNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    Spatial terrain;
    Spatial sinbad;
    Geometry geom;
    HSController hSController = new HSController();
    HSController enemyControl = new HSController();
    Node playerNode = new Node("player");
    Spatial enemy1;
    
    Node floor = new Node("floor");
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    private void setupTerrain() {
        terrain = assetManager.loadModel("Scenes/newScene.j3o");
        rootNode.attachChild(terrain);
    }
    private void setupLight() {
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-1, -1, -1));
        rootNode.addLight(directionalLight);
    }
    private void setupCam() {
        cam.setLocation(new Vector3f(0, 50, 0));
        cam.lookAt(new Vector3f(0, -1, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(100);
        flyCam.setDragToRotate(true);
    }
    private void setupCursor() {
        Box b = new Box(4, 0.1f, 4);
        geom = new Geometry("box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
    }
    private void updateCursor() {
        CollisionResults results = new CollisionResults();
        Vector2f cursor = inputManager.getCursorPosition();
        Vector3f startPoint = cam.getWorldCoordinates(cursor, 0);
        Vector3f direction = cam.getWorldCoordinates(cursor, 1).subtract(startPoint).normalize();
        
        Ray ray = new Ray(startPoint, direction);
        int size = terrain.collideWith(ray, results);
        
        if(size > 0) {
            Vector3f loc = results.getClosestCollision().getContactPoint();
            
            if(loc.x >= 0 ) loc.x += 4 - loc.x%8;
            else loc.x -= 4 - Math.abs(loc.x)%8;
            
            if(loc.z >= 0) loc.z += 4 - loc.z%8;
            else loc.z -= 4 - Math.abs(loc.z)%8;
            
            terrain.setUserData("hp", 100);
            
            geom.setLocalTranslation(loc);
        }
    }
    private void setupInput() {
        inputManager.addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("enemymove", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "click");
        inputManager.addListener(actionListener, "enemymove");
    }
    private void setupSinbad() {
        sinbad = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        playerNode.attachChild(sinbad);
        rootNode.attachChild(playerNode);
        sinbad.setLocalTranslation(4, 4.7f, 4);
        sinbad.addControl(hSController); 
    }
    private void generateMovement(Vector3f pos, int times) {
        for(int row = -times ; row <= times; row++ ) {
            for(int col = -times; col <= times; col++) {
                if( Math.abs(row) + Math.abs(col) > times ) continue;
                Vector3f temporary = pos.clone();
                temporary.x += row*8;
                temporary.z += col*8;
                
                Box b = new Box(temporary, 4, .1f, 4);
                Geometry geom = new Geometry("box", b);
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", ColorRGBA.Red);
                geom.setMaterial(mat);
                floor.attachChild(geom);
                
            }
        }
    }
    private void setupEnemy() {
        enemy1 = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        enemy1.setLocalTranslation(-60, 4.7f, 60);
        enemy1.addControl(enemyControl);
        rootNode.attachChild(enemy1);
    }
    
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean isPressed, float tpf) {
            if(name == "enemymove" && isPressed) {
                Vector3f enemyPos = enemy1.getWorldTranslation();
                Vector3f playerPos = sinbad.getWorldTranslation();
                
                Vector3f d = enemyPos.clone().subtract(playerPos);
                
                if( Math.abs(d.x) > Math.abs(d.z) ) {
                    //TODO: gerak secara horizontal
                    float x = enemyPos.x;
                    
                    if(d.x > 0 ) x -= 8;
                    else x += 8;
                    
                    enemyControl.setDestination(enemyPos.clone().setX(x));
                    
                } else {
                    //TODO: gerak secara vertical
                    float z = enemyPos.z;
                    
                    if(d.z > 0 ) z -= 8;
                    else z += 8;
                    
                    enemyControl.setDestination(enemyPos.clone().setZ(z));
                }
            } else if(name == "click" && isPressed) {
                CollisionResults results = new CollisionResults();
                Vector2f cursor = inputManager.getCursorPosition();
                Vector3f startPoint = cam.getWorldCoordinates(cursor, 0);
                Vector3f direction = cam.getWorldCoordinates(cursor, 1).subtract(startPoint).normalize();

                Ray ray = new Ray(startPoint, direction);
                int size = playerNode.collideWith(ray, results);

                if(size > 0) {
                    CollisionResult result = results.getClosestCollision();
                    hSController.isPicked = true;
                    generateMovement(sinbad.getLocalTranslation(), 1);
                    return;
                }
                results.clear();
                size = floor.collideWith(ray, results);
                if( size > 0 ) {
                    floor.detachAllChildren();
                    Vector3f dest = results.getClosestCollision().getContactPoint();
                    
                    if(dest.x >= 0 ) dest.x += 4 - dest.x%8;
                    else dest.x -= 4 - Math.abs(dest.x)%8;

                    if(dest.z >= 0) dest.z += 4 - dest.z%8;
                    else dest.z -= 4 - Math.abs(dest.z)%8;
                    
                    hSController.setDestination(dest);
                    
                    return;
                }
            }
        }
    };
    
    
    @Override
    public void simpleInitApp() {
        setupTerrain();
        setupLight();
        setupCam();
        setupCursor();
        setupInput();
        setupSinbad();
        setupEnemy();
        rootNode.attachChild(floor);
    }

    @Override
    public void simpleUpdate(float tpf) {
        updateCursor();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
