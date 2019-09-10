package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.cinematic.events.MotionTrack.Direction;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.terrain.Terrain;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import jme3tools.converters.ImageToAwt;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication implements AnalogListener, PhysicsCollisionListener
{
    Geometry geom;
    Geometry geomSphere;
    BulletAppState bulletAppState; // masukkin physic yang ada ke app state
    
    CharacterControl player;
    
    // untuk atur gerakan butuh vector
    Vector3f walkDirection = new Vector3f(); 
    
    boolean kiri= false, kanan= false, maju= false, mundur = false;
    
    // buat animasi
    AnimControl control; // buat dapatin semua animasinya
    AnimChannel channel; // buat set animasinya
    
    public static void main(String[] args) {
        Main app = new Main();
        
        //setting sendiri
        AppSettings appset = new AppSettings(true);
        appset.setFullscreen(false);
        appset.setTitle("Post Training");
        //implement setting kedalam app
        app.setSettings(appset);
        app.setShowSettings(false);
        
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        //setting awal
        flyCam.setMoveSpeed(100);
        cam.setLocation(new Vector3f(0, 50, 0));
        // hilangkan tulisan
        statsView.removeFromParent();
        fpsText.removeFromParent();
        // hilangkan camera
        //flyCam.setEnabled(false);
        // tampilkan cursor
        inputManager.setCursorVisible(true);
        
        // model
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/Wood Texture.jpg"));
        //mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        //rootNode.attachChild(geom);
        
        // buat bentuk baru bola
        Sphere s = new Sphere(32, 32, 2);
        geomSphere = new Geometry("Sphere", s);
        
        Material matSphere = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matSphere.setColor("Color", ColorRGBA.Red);
        matSphere.getAdditionalRenderState().setWireframe(true);
        geomSphere.setMaterial(matSphere);
        
        geomSphere.setLocalTranslation(0, 0, 10);
        // selain translation bisa pakai move juga
        geomSphere.move(0, 0, -10);
        // bedanya move pindah, kalau set langsung ke koordinatnya
        
        geomSphere.setLocalScale(2, 2, 2);
        // selain localScale ada scale juga
        geomSphere.scale(2, 2, 2);
        // bedanya kalau scale biasa dia akumulasi
        
        geomSphere.setLocalRotation( new Quaternion(1, 0, 1, 0)); 
        // kalau quaternion bisa disimpan, dengan deklarasi di luar ( pakai pallet ) ada x y z w satuannya radian
        //selain yang tadi ada yang langsung rotate
        geomSphere.rotate(FastMath.PI/3, 0, 0);
        
        //rootNode.attachChild(geomSphere);
        
        
        // buat bikin node jadi satu
        Node myNode = new Node();
        myNode.attachChild(geom);
        myNode.attachChild(geomSphere);
        
        rootNode.attachChild(myNode);
        myNode.scale(0.5f);
        
        // lighting pada JMonkey ada :
        // 1. point light = seperti lampu
        // 2. directional light = mirip cahaya matahari
        // 3. ambient light = semua kena cahaya
        // 4. ada satu lagi
        Spatial oto = assetManager.loadModel("Models/Oto/Oto.mesh.xml"); // butuh cahaya
        rootNode.attachChild(oto);
        
        // add animasi
        control = oto.getControl(AnimControl.class);
        channel = control.createChannel(); // buat dapat animasi di oto
        // cara tau animasinya dengan : 
        for (String anim:control.getAnimationNames())
        {
            System.out.println(anim);
        }
        //channel.setAnim("Walk");
        
        // add character control physics , check collition
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(1.5f, 6); // param 1 adalah radius jaraknya, tingginya 6 >> coba2 sendri
        player = new CharacterControl(capsule, 0.3f); // param 1 adalah collition shape, param2 adalah jarak tinggi kaki dengan terrain
        // settingan yang hanya ada di character control
        player.setJumpSpeed(20); // contoh dengan listener
        oto.addControl(player);
        bulletAppState.getPhysicsSpace().add(player);
        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        initTerrain();
        initLight(); // agar Oto terlihat
        initKeys();
        
        // buat bikin camera mengikuti si oto
        ChaseCamera chaseCam = new ChaseCamera(cam, oto, inputManager);
        chaseCam.setDefaultHorizontalRotation(-(FastMath.PI/2));
    }

    public void initKeys()
    {
        // mapping key apa yang kita pakai
        inputManager.addMapping("maju", new KeyTrigger(keyInput.KEY_NUMPAD8)); 
        inputManager.addMapping("mundur", new KeyTrigger(keyInput.KEY_NUMPAD5)); 
        inputManager.addMapping("kiri", new KeyTrigger(keyInput.KEY_NUMPAD4)); 
        inputManager.addMapping("kanan", new KeyTrigger(keyInput.KEY_NUMPAD6)); 
        inputManager.addMapping("lompat", new KeyTrigger(keyInput.KEY_SPACE)); 
        inputManager.addMapping("Boxjalan", new KeyTrigger(keyInput.KEY_5));
        
        
        // masuk kedalam listener
        // ada 2 cara :
        //     1. balik keatas dengan implements analog listerner
        inputManager.addListener(this, "Boxjalan");
        
        //variable untuk masuk kedalam simple update. pake action listerner
        inputManager.addListener(action, "maju","mundur","kanan","kiri","lompat");
    }
    
    // jangan salah import, pakai yang com.jm3
    
    // action bisa dibagi jadi 2 ada isPressed dan isReleased
    public ActionListener action = new ActionListener() {

        public void onAction(String name, boolean isPressed, float tpf) 
        {
            // butuh parameter tambahan untuk masuk ke simpleUpdate, sebagai flagging
           if( isPressed )
           {
               channel.setAnim("Walk");
               if( name.equals("maju") )
               {
                   maju = true;
               }
               else if(name.equals("mundur"))
               {
                   mundur = true;
               }
               else if(name.equals("kiri"))
               {
                   kiri = true;
               }
               else if(name.equals("kanan"))
               {
                   kanan = true;
               }
               else if(name.equals("lompat"))
               {
                   player.jump(); //ada fungsi jump = lompat
                   channel.setAnim("pull");
               }
           }
           else
           {
                channel.setAnim("stand");
               maju = mundur = kiri = kanan = false;
           }
        }
    };
    
    public void initLight()
    {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -1, -1));
        rootNode.addLight(sun);
    }
    
    public void initTerrain() // ada rigid body control
    {
        // buat map
        // add texture map
        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/Alpha.png"));
        
        // bikin texture lantai
        // untuk warna 1
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex1", grass);
        mat_terrain.setFloat("Tex1Scale", 64);
        
        // untuk warna 2
        Texture road = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex2", road);
        mat_terrain.setFloat("Tex2Scale", 64);
        
        // untuk warna 3
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex3", dirt);
        mat_terrain.setFloat("Tex3Scale", 64);
        
        // buat height map versi beda
        Texture  heightMapImage = assetManager.loadTexture("Textures/Height.png");
        AbstractHeightMap heighMap = new ImageBasedHeightMap( 
                ImageToAwt.convert(heightMapImage.getImage(), false, true, 0) );
        //AbstractHeightMap heighMap = new ImageBasedHeightMap( heightMapImage.getImage() );
        heighMap.load();
        
        
        
        // buat bikin terrain, namanya bebas, pathsize minimun + 1, total size dari height map
        TerrainQuad terrain = new TerrainQuad("My Terrain", 65, 513, heighMap.getHeightMap());
        //abis buat terrain harus dimasukin material yang sudah kita buat diatas
        terrain.setMaterial(mat_terrain);
        
        terrain.scale(2,0.5f,2);
        terrain.setLocalTranslation(0, -50, 0);
        rootNode.attachChild(terrain);
        
        //Physics
        CollisionShape colShape = CollisionShapeFactory.createMeshShape(terrain); // parameter isi spatialnya karena ini rigid body control pakai objectnya terrain
        RigidBodyControl landscape = new RigidBodyControl(colShape, 0); // buat benda padat , param 1 artinya benda bisa digunakan kalau 0 diam
        
        terrain.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(landscape);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
        
        //geom.move(1*tpf, 0, 0); // tpf bakal menyesuaikan dengan komputer
        geomSphere.rotate(tpf, tpf, tpf);
        
        // action listener untuk gerakan
        Vector3f camDir = cam.getDirection().clone().mult(0.5f); // ambil posisi layar
        Vector3f camLeft = cam.getLeft().clone().mult(0.5f); // ambil posisi layar
        
        camDir.y = 0;
        camLeft.y = 0;
        
        walkDirection.set(Vector3f.ZERO);
        
        if ( maju )
        {
            walkDirection.addLocal(camDir);
        }
        if(mundur)
        {
            walkDirection.addLocal(camDir.negate()); // kebalikan dengans negate
        }
        if(kanan)
        {
            walkDirection.addLocal(camLeft.negate()); // kembalikan dengan negate
        }
        if(kiri)
        {
            walkDirection.addLocal(camLeft);
        }
        
        player.setWalkDirection(walkDirection);
        player.setViewDirection(walkDirection);
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void onAnalog(String name, float value, float tpf) 
    {
        if( name.equals("Boxjalan") )
        {
            // cek map listener
            geom.move(0, 10*tpf, 0);
        }
    }

    public void collision(PhysicsCollisionEvent pce) 
    {
        System.out.println(pce.getNodeA().getName()); // dapetin object apa saja yang kena collition
    }
}
