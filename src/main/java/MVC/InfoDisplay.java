package MVC;

import Entities.Bush;
import Entities.Corpse;
import Entities.Creature.Creature;
import Entities.Creature.Egg;
import Entities.Entity;
import Physics.GridWorld;
import Utils.Ref;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class InfoDisplay implements Initializable {

    /** Updates the view to show world info */
    @FXML
    private ToggleButton worldInfoToggle;

    /** Updates the view to show world info */
    @FXML
    private ToggleButton entityInfoToggle;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    private Text timeElapsed;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    private Text numCreatures;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    private Text numBushes;

    /** A function in View that Controller calls to update Text directly */
    @FXML
    private Text numEntities;

    /** Displays various world-related information constantly. */
    @FXML
    private AnchorPane worldInfoTab;

    /** Displays various entity-related information when a corpse is selected. */
    @FXML
    private AnchorPane corpseInfoTab;

    /** Displays various entity-related information when a bush is selected. */
    @FXML
    private AnchorPane bushInfoTab;

    /** Displays various entity-related information when an egg is selected. */
    @FXML
    private AnchorPane eggInfoTab;

    /** Displays various entity-related information when a creature is selected. */
    @FXML
    private AnchorPane creatureInfoTab;

    /** Stores the reference to the instance of the model this InfoDisplay displays. */
    private Ref<GridWorld.ReadOnlyWorld> model;

    /** Stores a reference to the current selected Entity. */
    private Ref<Entity> selectedEntity;

    /** Initializer automatically called by JavaFX right after FXML injected all dependencies. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        selectInfoTab(0);
        entityInfoToggle.setDisable(true);
    }

    /** Custom initializer called by {@linkplain GUI}. */
    public void init(Ref<GridWorld.ReadOnlyWorld> model, Ref<Entity> selectedEntity) {
        this.model = model;
        this.selectedEntity = selectedEntity;
    }

    /** Updates the world info tab when the tab is open and visible. */
    private void updateWorldInfo() {
        if (worldInfoTab.isVisible()) {
            // TODO update world info tab

        }
    }

    /** Updates the Entity Information on the selected entity, or closes the info tab if no entity is selected. */
    private void updateEntityInfo() {
        // TODO check whether entity or world info tab is open before updating information for optimization.
        if (!selectedEntity.isEmpty() && !worldInfoTab.isVisible()) {
            // TODO list information about the selected entity here
//            ReadOnlyCritter readOnlyCritter;
//            try {
//                readOnlyCritter = model.getReadOnlyCritter(selectedTileCoords[0], selectedTileCoords[1]).get();
//            } catch (NoMaybeValue e) {
//                throw new RuntimeException("Unexpected outcome whilst trying to obtain Maybe value");
//            }
//
//            int[] critterMem = readOnlyCritter.getMemory();
//            memsizeText.setText(String.valueOf(critterMem[0]));
//            defenseText.setText(String.valueOf(critterMem[1]));
//            attackText.setText(String.valueOf(critterMem[2]));
//            sizeText.setText(String.valueOf(critterMem[3]));
//            energyText.setText(String.valueOf(critterMem[4]));
//            passnumText.setText(String.valueOf(critterMem[5]));
//            postureText.setText(String.valueOf(critterMem[6]));
//            lastRuleDone.setText(readOnlyCritter.getLastRuleString()
//                    .orElse("This Critter hasn't executed any rules yet..."));
//            critterProgram.setText(readOnlyCritter.getProgramString());
        } else {
            selectInfoTab(0);
            entityInfoToggle.setDisable(true);
        }
    }

    @FXML
    private void handleWorldEntityTogglePressed(final ActionEvent e) {
        if (e.getSource() == worldInfoToggle) {
            selectInfoTab(0);
        } else if (e.getSource() == entityInfoToggle) {
            // selected Entity must not be null if entity-info-toggle was pressed.
            switch (selectedEntity.get()) {
                case Corpse ignored -> selectInfoTab(1);
                case Bush ignored -> selectInfoTab(2);
                case Egg ignored -> selectInfoTab(3);
                case Creature ignored -> selectInfoTab(4);
                case null, default -> throw new IllegalStateException("Unexpected value: " + selectedEntity.get());
            }
        }
    }

    /**
     * Selects and displays the appropriate information tab based on the given index.
     * <p>
     * This method updates the visibility of different information tabs (world, corpse, bush,
     * egg, creature) and toggles the selection state of the info toggle buttons accordingly.
     * </p>
     *
     * @param i an integer representing the type of information to display:
     *          <ul>
     *              <li>0 - World Info</li>
     *              <li>1 - Corpse Info</li>
     *              <li>2 - Bush Info</li>
     *              <li>3 - Egg Info</li>
     *              <li>4 - Creature Info</li>
     *          </ul>
     */
    private void selectInfoTab(int i) {
        if (i < 0 || i > 4) throw new RuntimeException("Unsupported tab selection {" + i + "}.");

        worldInfoToggle.setSelected(i == 0);
        entityInfoToggle.setSelected(i != 0);

        worldInfoTab.setVisible(i == 0);
        corpseInfoTab.setVisible(i == 1);
        bushInfoTab.setVisible(i == 2);
        eggInfoTab.setVisible(i == 3);
        creatureInfoTab.setVisible(i == 4);
    }
}
