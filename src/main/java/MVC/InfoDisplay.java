package MVC;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

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
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public final class InfoDisplay implements Initializable {

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

    /** A Progress Bar displaying how rotten a Corpse Entity is.
     * Invariant: Progress ranges from 0 to 99%. */
    @FXML
    private ProgressBar rottingBar;

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

    /** Custom Scroll Bar for the Creature Info Scroll Pane. */
    @FXML
    private ScrollBar creatureInfoScrollBar;

    /** Creature Info's Scroll Pane with a Custom Scroll Bar. */
    @FXML
    private ScrollPane creatureInfoScrollPane;

    private Ref<GridWorld.ReadOnlyWorld> model;

    private Ref<Entity.ReadOnlyEntity> selectedEntity;

    /** Initializer automatically called by JavaFX right after FXML injected all dependencies. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindProperties();

        selectInfoTab(null);
        worldInfoToggle.setDisable(true);
        entityInfoToggle.setDisable(true);

        creatureInfoScrollBar.minProperty().bind(creatureInfoScrollPane.vminProperty());
        creatureInfoScrollBar.maxProperty().bind(creatureInfoScrollPane.vmaxProperty());
        creatureInfoScrollBar.visibleAmountProperty().bind(creatureInfoScrollPane.heightProperty().divide(480));
        creatureInfoScrollPane.vvalueProperty().bindBidirectional(creatureInfoScrollBar.valueProperty());
    }

    /** Custom initializer called by {@linkplain MainView}. */
    public void init(Ref<GridWorld.ReadOnlyWorld> model, Ref<Entity.ReadOnlyEntity> selectedEntity) {
        this.model = model;
        this.selectedEntity = selectedEntity;

        // set worldInfoToggle and entityInfoToggle to only be enabled when !selectedEntity.isEmpty()
        selectedEntity.onUpdate(roe -> {
            if (roe == null) {
                worldInfoToggle.setDisable(true);
                entityInfoToggle.setDisable(true);
                selectInfoTab(null);
            } else {
                worldInfoToggle.setDisable(false);
                entityInfoToggle.setDisable(false);

                // check whether entity or world info tab is open before updating information for optimization.
                if (!worldInfoTab.isVisible()) updateEntityInfo();
            }
        });

        model.onUpdate(this::updateWorldInfo);
    }

    /** Binds the various width and height properties of the JavaFX FXML components correspondent
     * to this class. */
    private void bindProperties() {
        // TODO implement

        for (AnchorPane infoTab : new AnchorPane[]{creatureInfoTab, corpseInfoTab, bushInfoTab, eggInfoTab}) {
            for (String selector : new String[]{"#coordinate", "#velocity"}) {
                Set<Node> nodeSet = infoTab.lookupAll(selector);
                for (Node node : nodeSet) {
                    ((Text) node).setWrappingWidth(0);
                    ((Text) node).textProperty().addListener((observable, oldValue, newValue) -> {
                        bindTextResize(((Text) node), node.getParent().getLayoutBounds().getWidth() * 0.9);
                    });
                }
            }
        }
    }

    private void bindTextResize(Text text, double maxWidth) {
        double fontSize = 18;
        text.setFont(Font.font("System", FontWeight.BOLD, fontSize));

        while (text.getLayoutBounds().getWidth() > maxWidth && fontSize > 1) {
            fontSize -= 0.5;
            text.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        }
    }

    /** Updates the world info tab whenever {@linkplain #model} gets an update */
    private void updateWorldInfo(GridWorld.ReadOnlyWorld newModel) {
        // update world info tab
        timeElapsed.setText(String.valueOf(newModel.timeElapsed));
        int numCreaturesInModel = 0, numBushesInModel = 0;
        for (Entity.ReadOnlyEntity roe : newModel.entities)
            if (roe instanceof Creature.ReadOnlyCreature || roe instanceof Egg.ReadOnlyEgg) numCreaturesInModel++;
            else if (roe instanceof Bush.ReadOnlyBush) numBushesInModel++;

        numCreatures.setText(String.valueOf(numCreaturesInModel));
        numBushes.setText(String.valueOf(numBushesInModel));
        numEntities.setText(String.valueOf(newModel.entities.length));
    }

    /** Updates the Entity Information on the selected entity */
    private void updateEntityInfo() {
        // list information about the selected entity here
        switch (selectedEntity.get()) {
            case Corpse.ReadOnlyCorpse corpse -> {
                ((Text) corpseInfoTab.lookup("#size")).setText(" " + corpse.getSize());
                ((Text) corpseInfoTab.lookup("#energy")).setText(" " + corpse.energy());
                ((Text) corpseInfoTab.lookup("#coordinate")).setText(corpse.getX() + ", " + corpse.getY());
                ((Text) corpseInfoTab.lookup("#velocity")).setText(corpse.velocityX() + ", " + corpse.velocityY());
                rottingBar.setProgress(corpse.getRottenPerct());
            }
            case Bush.ReadOnlyBush bush -> {
                ((Text) bushInfoTab.lookup("#size")).setText(" " + bush.getSize());
                ((Text) bushInfoTab.lookup("#stored-energy")).setText(" " + bush.getStoredEnergy());
                ((Text) bushInfoTab.lookup("#coordinate")).setText(bush.getX() + ", " + bush.getY());
                ((Text) bushInfoTab.lookup("#berries")).setText(" " + bush.numBerries());
            }
            case Egg.ReadOnlyEgg egg -> {
                ((Text) eggInfoTab.lookup("#size")).setText(" " + egg.getSize());
                ((Text) eggInfoTab.lookup("#incubation-time")).setText(" " + egg.incubationTime());
                ((Text) eggInfoTab.lookup("#coordinate")).setText(egg.getX() + ", " + egg.getY());
                ((Text) eggInfoTab.lookup("#health")).setText(" " + egg.health());
            }
            case Creature.ReadOnlyCreature creature -> {
                ((Text) creatureInfoTab.lookup("#size")).setText(" " + creature.getSize());
                ((Text) creatureInfoTab.lookup("#force")).setText(" " + creature.force());
                ((Text) creatureInfoTab.lookup("#coordinate")).setText(creature.getX() + ", " + creature.getY());
                ((Text) creatureInfoTab.lookup("#velocity")).setText(creature.velocityX() + ", " + creature.velocityY());
                ((Text) creatureInfoTab.lookup("#health")).setText(" " + creature.health());
                ((Text) creatureInfoTab.lookup("#energy")).setText(" " + creature.energy());
                ((Text) creatureInfoTab.lookup("#strength")).setText(" " + creature.strength());
                ((Text) creatureInfoTab.lookup("#armour")).setText(" " + creature.armour());
                ((Text) creatureInfoTab.lookup("#herbivore")).setText(" " + creature.herbivore());
                ((Text) creatureInfoTab.lookup("#carnivore")).setText(" " + creature.carnivore());
                ((Text) creatureInfoTab.lookup("#offspring-investment")).setText(" " + creature.offspringInvestment());
                ((Text) creatureInfoTab.lookup("#maturity")).setText(" " + creature.maturity());
                ((Text) creatureInfoTab.lookup("#vision-range")).setText(" " + creature.visionRange());
                ((Text) creatureInfoTab.lookup("#alignment")).setText(" " + creature.alignment());
                ((Text) creatureInfoTab.lookup("#cohesion")).setText(" " + creature.cohesion());
                ((Text) creatureInfoTab.lookup("#separation")).setText(" " + creature.separation());
            }
            case null, default -> throw new IllegalStateException("Unexpected value: " + selectedEntity.get());
        }
    }

    @FXML
    private void handleWorldEntityTogglePressed(final ActionEvent e) {
        if (e.getSource() == worldInfoToggle) {
            selectInfoTab(null);
        } else if (e.getSource() == entityInfoToggle) {
            // selected Entity must not be null if entity-info-toggle was pressed.
            selectInfoTab(selectedEntity.get());
        }
    }

    /**
     * Selects and displays the appropriate information tab based on the given index.
     * <p>
     * This method updates the visibility of different information tabs (world, corpse, bush,
     * egg, creature) and toggles the selection state of the info toggle buttons accordingly.
     * </p>
     *
     * @param roe a Read-only entity object representing the type of information to display:
     *          <ul>
     *              <li>{@code null} - World Info</li>
     *              <li>{@code ReadOnlyCorpse} - Corpse Info</li>
     *              <li>{@code ReadOnlyBush} - Bush Info</li>
     *              <li>{@code ReadOnlyEgg} - Egg Info</li>
     *              <li>{@code ReadOnlyCreature} - Creature Info</li>
     *          </ul>
     */
    private void selectInfoTab(Entity.ReadOnlyEntity roe) {
        int i = switch (roe) {
            case null -> 0;
            case Corpse.ReadOnlyCorpse ignored -> 1;
            case Bush.ReadOnlyBush ignored -> 2;
            case Egg.ReadOnlyEgg ignored -> 3;
            case Creature.ReadOnlyCreature ignored -> 4;
            default -> throw new IllegalStateException("Unexpected value: " + selectedEntity.get());
        };

        worldInfoToggle.setSelected(i == 0);
        entityInfoToggle.setSelected(i != 0);

        worldInfoTab.setVisible(i == 0);
        corpseInfoTab.setVisible(i == 1);
        bushInfoTab.setVisible(i == 2);
        eggInfoTab.setVisible(i == 3);
        creatureInfoTab.setVisible(i == 4);
    }

    /** Selects the entity info tab when a new Entity is selected. */
    public void selectEntityInfoTab() {
        if (selectedEntity.isEmpty()) return;
        worldInfoToggle.setDisable(false);
        entityInfoToggle.setDisable(false);

        selectInfoTab(selectedEntity.get());

        updateEntityInfo();
    }
}
