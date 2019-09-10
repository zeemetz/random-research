package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

public class HSController extends AbstractControl {
    
    public boolean isPicked = false;
    public boolean isMoving = false;
    
    AnimControl animControl;
    AnimChannel animChannelTop;
    AnimChannel animChannelBase;
    Vector3f dest;
    
    public void setDestination(Vector3f dest) {
        this.dest = dest;
        isMoving = true;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if( animControl == null ) {
            animControl = spatial.getControl(AnimControl.class);
            animChannelTop = animControl.createChannel();
            animChannelBase = animControl.createChannel();
            animChannelTop.setAnim("IdleTop");
            animChannelBase.setAnim("IdleBase");
            spatial.setUserData("hp", 100);
        }
        
        if(isPicked ) {
            animChannelTop.setAnim("RunTop");
            animChannelBase.setAnim("RunBase");
            isPicked = false;
        }
        
        if(isMoving) {
            Vector3f loc = spatial.getLocalTranslation();       
            int positiveX = 1;
            int positiveZ = 1;
            int movex = 1;
            int movez = 1;
            
            if(Math.round(Math.abs(loc.x - dest.x)) <= .5f) movex = 0;
            if(Math.round(Math.abs(loc.z - dest.z)) <= .5f) movez = 0;
            
            if(movex == 0 && movez == 0) {
                isMoving = false;
                animChannelTop.setAnim("IdleTop");
                animChannelBase.setAnim("IdleBase");
            }
            
            if(loc.x > dest.x ) positiveX *= -1;
            if(loc.z > dest.z ) positiveZ *= -1;
            
            spatial.move(
                    0.1f * positiveX * movex, 
                    0, 
                    0.1f * positiveZ * movez );
            
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        return null;
    }
    
}
