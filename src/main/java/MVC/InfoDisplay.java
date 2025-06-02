package MVC;

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

    /** Displays various entity-related information when an entity is selected. */
    @FXML
    private AnchorPane entityInfoTab;

    /** Stores the reference to the instance of the model this InfoDisplay displays. */
    private Ref<GridWorld.ReadOnlyWorld> model;

    /** Stores a reference to the current selected Entity. */
    private Ref<Entity> selectedEntity;

    /** Initializer automatically called by JavaFX right after FXML injected all dependencies. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        entityInfoTab.setVisible(false);
        worldInfoToggle.setSelected(true);
        entityInfoToggle.setDisable(true);
    }

    /** Custom initializer called by {@linkplain MainView}. */
    public void init(Ref<GridWorld.ReadOnlyWorld> model, Ref<Entity> selectedEntity) {
        this.model = model;
        this.selectedEntity = selectedEntity;
    }

    /** Updates the Entity Information on the selected entity, or closes the info tab if no entity is selected. */
    private void updateSelectedEntity() {
        if (selectedEntity.isEmpty()) {
            entityInfoToggle.setDisable(false);
            worldInfoTab.setVisible(false);
            entityInfoTab.setVisible(true);
            worldInfoToggle.setSelected(false);
            entityInfoToggle.setSelected(true);
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
            entityInfoToggle.setDisable(true);
            worldInfoToggle.setSelected(true);
            entityInfoTab.setVisible(false);
            worldInfoTab.setVisible(true);
            entityInfoToggle.setSelected(false);
        }
    }

    @FXML
    private void handleWorldCritterTogglePressed(final ActionEvent e) {
        if (e.getSource() == worldInfoToggle) {
            worldInfoToggle.setSelected(true);
            entityInfoTab.setVisible(false);
            worldInfoTab.setVisible(true);
            entityInfoToggle.setSelected(false);
        } else if (e.getSource() == entityInfoToggle) {
            worldInfoTab.setVisible(false);
            entityInfoTab.setVisible(true);
            worldInfoToggle.setSelected(false);
        }
    }
}
