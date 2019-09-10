package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

public class VRState extends AbstractAppState {
    
    Main app;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (Main)app;
    }
    
    public void showSinbad() {
        app.getStateManager().getState(ARState.class).ngemeng();
    }
    
}
