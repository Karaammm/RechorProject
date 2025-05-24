package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The SummaryUI class provides a graphical user interface for displaying a list
 * of journeys and allows users to interact with the journey data. It includes
 * functionality to update the displayed journeys based on a selected departure
 * time and to visually represent journey details using custom cells.
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> journeyObs) {
    private static final double CIRCLE_RADIUS = 3.0;
    private static final double LINE_OFFSET = 5.0;

    /**
     * Creates a new SummaryUI instance.
     *
     * @param journeysObs   An observable value containing the list of journeys.
     * @param departureTime An observable value representing the selected departure
     *                      time.
     * @return A new SummaryUI instance with the root node and journey observable.
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeysObs,
            ObservableValue<LocalTime> departureTime) {
        ListView<Journey> journeysListView = new ListView<>();

        departureTime.subscribe(newTime -> {
            List<Journey> currentJourneys = journeysObs.getValue();
            if (currentJourneys != null) {
                updateDepartureTime(currentJourneys, journeysListView, newTime);
            }
        });

        journeysObs.subscribe(newJourneys -> {
            updateJourneysList(newJourneys, journeysListView);
        });

        BorderPane root = new BorderPane(journeysListView);
        return new SummaryUI(root, journeysListView.getSelectionModel().selectedItemProperty());
    }

    /**
     * Updates the selected journey in the ListView based on the given departure
     * time.
     *
     * @param journeys         The list of journeys to search through.
     * @param journeysListView The ListView displaying the journeys.
     * @param newTime          The new departure time to use for selection.
     */
    private static void updateDepartureTime(List<Journey> journeys,
                                            ListView<Journey> journeysListView,
                                            LocalTime newTime) {
        for (Journey j : journeys) {
            if (!j.depTime().toLocalTime().isBefore(newTime)) {
                journeysListView.getSelectionModel().select(j);
                journeysListView.scrollTo(j);
                return;
            }
        }
        if (!journeys.isEmpty()) {
            journeysListView.getSelectionModel().select(journeys.getLast());
            journeysListView.scrollTo(journeys.getLast());
        }
    }

    /**
     * Updates the ListView with a new list of journeys.
     *
     * @param newJourneys      The new list of journeys to display.
     * @param journeysListView The ListView to update.
     */
    private static void updateJourneysList(List<Journey> newJourneys,
                                           ListView<Journey> journeysListView) {
        journeysListView.setItems(FXCollections.observableArrayList(newJourneys));
        journeysListView.setCellFactory(p -> new TripCell());
    }

    /**
     * A custom ListCell implementation for displaying journey details.
     */
    private static class TripCell extends ListCell<Journey> {
        private final Text depTimeText;
        private final Text arrTimeText;
        private final Text durationText;
        private final Text directionText;
        private final ImageView transportImage;
        private final Line line;
        private final BorderPane borderPane;
        private final Group centerGroup;
        private final Pane centerPane;
        private final List<Circle> stopCircles = new ArrayList<>();

        /**
         * Constructs a new TripCell instance and initializes its layout and components.
         */
        TripCell() {
            depTimeText = new Text();
            depTimeText.getStyleClass().add("departure");
            arrTimeText = new Text();
            durationText = new Text();
            directionText = new Text();
            transportImage = new ImageView();
            transportImage.setFitHeight(20);
            transportImage.setFitWidth(20);
            line = new Line();
            line.setStrokeWidth(1);

            HBox durationBox = new HBox(durationText);
            durationBox.getStyleClass().add("duration");
            centerGroup = new Group();
            centerPane = new Pane() {
                @Override
                protected void layoutChildren() {
                    super.layoutChildren();
                    setPrefSize(0d, 0d);
                    double w = getWidth();
                    double h = getHeight();
                    line.setStartX(LINE_OFFSET);
                    line.setStartY(h / 2);
                    line.setEndX(w - LINE_OFFSET);
                    line.setEndY(h / 2);
                    centerGroup.getChildren().clear();

                    for (Circle c : stopCircles) {
                        double ratio = (double) c.getUserData();
                        double x = line.getStartX() + ratio * (line.getEndX() - line.getStartX());
                        double y = h / 2;
                        c.setCenterX(x);
                        c.setCenterY(y);
                        centerGroup.getChildren().add(c);
                    }
                    centerGroup.toFront();
                }
            };
            centerPane.getChildren().addAll(centerGroup, line);
            HBox topBox = new HBox(directionText, transportImage);
            topBox.getStyleClass().add("route");

            borderPane = new BorderPane();
            borderPane.getStyleClass().add("journey");
            borderPane.getStylesheets().add("summary.css");
            borderPane.setLeft(depTimeText);
            borderPane.setRight(arrTimeText);
            borderPane.setTop(topBox);
            borderPane.setCenter(centerPane);
            borderPane.setBottom(durationBox);
        }

        /**
         * Updates the cell's content based on the given journey.
         *
         * @param journey The journey to display in the cell.
         * @param empty   Whether the cell is empty.
         */
        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);
            if (empty || journey == null) {
                setText(null);
                setGraphic(null);
            } else {
                arrTimeText.setText(FormatterFr.formatTime(journey.arrTime()));
                depTimeText.setText(FormatterFr.formatTime(journey.depTime()));
                durationText.setText(FormatterFr.formatDuration(journey.duration()));

                stopCircles.clear();
                Circle startCircle = new Circle(CIRCLE_RADIUS);
                startCircle.setUserData(0.0);
                startCircle.getStyleClass().add("dep-arr");
                stopCircles.add(startCircle);
                for (int i = 1; i < journey.legs().size() - 1; i++) {
                    Duration sinceStart = Duration.between(journey.depTime(),
                                                           journey.legs().get(i).depTime());
                    double ratio = (double) sinceStart.toMinutes() / journey.duration().toMinutes();

                    Circle circle = new Circle(CIRCLE_RADIUS);
                    circle.setUserData(ratio);
                    if (journey.legs().get(i) instanceof Journey.Leg.Foot) {
                        circle.getStyleClass().add("transfer");
                        stopCircles.add(circle);
                    }
                }
                Circle endCircle = new Circle(CIRCLE_RADIUS);
                endCircle.setUserData(1.0);
                endCircle.getStyleClass().add("dep-arr");
                stopCircles.add(endCircle);

                centerPane.setUserData(journey);

                Journey.Leg.Transport transportLeg =
                        journey.legs().getFirst() instanceof Journey.Leg.Transport ?
                            (Journey.Leg.Transport) journey.legs().getFirst() :
                            (Journey.Leg.Transport) journey.legs().get(1);
                directionText.setText(FormatterFr.formatRouteDestination(transportLeg));
                transportImage.setImage(VehicleIcons.iconFor(transportLeg.vehicle()));

                setGraphic(borderPane);
            }
        }
    }
}
