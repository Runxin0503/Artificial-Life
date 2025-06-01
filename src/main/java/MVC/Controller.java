package MVC;

import Physics.GridWorld;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Controller implements Runnable{

    /** The view module associated with this Controller in the MVC pairing */
    private final MainView view;

    /** The World that runs in fixed intervals */
    private GridWorld world;

    /** Acts as a buffer for how many tick steps the world has taken without
     * calculating and updating the steps/sec rate on {@link MainView}. */
    private int stepCounter;

    private ExecutorService executorService;

    /** If true, ticks the world at every tick possible. Otherwise, waits explicitly
     * for a Task to tick the world or change this value to true. */
    private boolean runContinuously;

    public Controller(MainView view) {
        this.view = view;
        this.world = null;
        this.runContinuously = false;
        stepCounter = 0;
        this.executorService = Executors.newCachedThreadPool();
    }

    /** Contains the loop checking and executing relevant tasks from view's TaskQueue */
    @Override
    public void run() {
        long stepsPerSec = 0;
        long stepsPerSecCatcher = 1001;
        while (true) {
            long millis = System.currentTimeMillis();
            if ((stepsPerSecCatcher - stepsPerSec) >= 1000){
                stepsPerSec = System.currentTimeMillis();
            }
            Task tracker;
            while ((tracker = view.pollTaskQueue()) != null){
                if (tracker.getType() == Task.TaskType.RUN_CONTINUOUSLY){
                    runContinuously = true;
                }
                else if (tracker.getType() == Task.TaskType.STEP){
                    stepWorld();
                    updateViewWorld(false);
                }
                else if (tracker.getType() == Task.TaskType.STOP_RUNNING){
                    updateViewWorld(false);
                    runContinuously = false;
                }
                else if (tracker.getType() == Task.TaskType.CREATE_NEW_WORLD){
                    createNewWorld();
                }
                else if (tracker.getType() == Task.TaskType.LOAD_WORLD){
                    loadWorld((String)tracker.getValue()[0]);
                }
            }

            if(runContinuously) {
                int simSpeed = view.pollSimSpeed();
                if(simSpeed == 1000) {
                    // step world without waiting
                }else if(simSpeed == 0) {
                    // don't step world at all, try to execute tasks
                    continue;
                }else{
                    //wait for 1000/simSpeed - (time it took to execute tasks)
                    try {
                        Thread.sleep(Math.max(0,1000/simSpeed - (millis - System.currentTimeMillis())));
                    } catch (InterruptedException ignored) {
                    }
                }
                stepWorld();
                stepsPerSecCatcher = System.currentTimeMillis();
                if ((stepsPerSecCatcher - stepsPerSec) >= 1000) {
                    double h = (1000.0/(stepsPerSecCatcher - stepsPerSec));
                    view.updateStepsPerSec(((int)(stepCounter*h*100))*0.01);
                    stepCounter = 0;
                }
                updateViewWorld(false);
            }
            //if runContinuously is true, step the world 1 tick and update view's readOnlyWorld
            //read from view's task queue and see what tasks need to be executed
            //updates the View on the observedCritter information
        }
    }

    private void stepWorld(){
        world.tick(executorService);
        stepCounter++;
    }

    private void createNewWorld(){
        runContinuously = false;
        world = new GridWorld();
        updateViewWorld(true);
    }

    private void loadWorld(String filePath) {
        runContinuously = false;
        GridWorld world;
        if((world = GridWorld.loadWorld(filePath)) != null) {
            this.world = world;
            updateViewWorld(true);
        }
    }

    private void updateViewWorld(boolean reset){
        GridWorld.ReadOnlyWorld readWorld = world.getReadOnlyCopy();
        if(reset){view.loadNewViewModel(readWorld);}
        else{view.updateViewModel(readWorld);}
    }
}