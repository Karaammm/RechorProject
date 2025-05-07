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

public record SummaryUI(Node rootNode, ObservableValue<Journey> journeyObs) {
    public static SummaryUI create(ObservableValue<List<Journey>> journeysObs, ObservableValue<LocalTime> departureTime){
        LocalTime depTime = departureTime.getValue();
        List<Journey> journeys = journeysObs.getValue();
        ListView<Journey> journeysListView = new ListView<>();
        journeysListView.setItems(FXCollections.observableArrayList(journeys));
        journeysListView.setCellFactory(p -> new TripCell());

        departureTime.subscribe(newTime -> {
            for(Journey j : journeys){
                if(!j.depTime().toLocalTime().isBefore(newTime)){
                    journeysListView.getSelectionModel().select(j);
                    journeysListView.scrollTo(j);
                    return;
                }
            }
            if (!journeys.isEmpty()) {
                journeysListView.getSelectionModel().select(journeys.getLast());
                journeysListView.scrollTo(journeys.getLast());
            }
        });

        BorderPane root = new BorderPane(journeysListView);
        return new SummaryUI(root, journeysListView.getSelectionModel().selectedItemProperty());
    }

    private static class TripCell extends ListCell<Journey>{
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

        TripCell(){
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
            centerPane = new Pane(){
                @Override
                protected void layoutChildren(){
                    super.layoutChildren();
                    setPrefSize(0d,0d);
                    double w = getWidth();
                    double h = getHeight();
                    line.setStartX(5);
                    line.setStartY(h / 2);
                    line.setEndX(w - 5);
                    line.setEndY(h / 2);
                    centerGroup.getChildren().clear();

                    for (Circle c : stopCircles) {
                        double ratio = (double) c.getUserData(); // make sure it's a Double!
                        double x = line.getStartX() + ratio * (line.getEndX() - line.getStartX());
                        double y = h / 2;
                        c.setCenterX(x);
                        c.setCenterY(y);
                        centerGroup.getChildren().add(c);
                    }
                    centerGroup.toFront();
                }
            };
            centerPane.getChildren().addAll(centerGroup,line);
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


        @Override
        protected void updateItem(Journey journey, boolean empty){
            super.updateItem(journey,empty);
            if (empty || journey == null) {
                setText(null);
                setGraphic(null);
            } else {
                arrTimeText.setText(FormatterFr.formatTime(journey.arrTime()));
                depTimeText.setText(FormatterFr.formatTime(journey.depTime()));
                durationText.setText(FormatterFr.formatDuration(journey.duration()));

                stopCircles.clear();
                Circle startCircle = new Circle(3);
                startCircle.setUserData(0.0);
                startCircle.getStyleClass().add("dep-arr");
                stopCircles.add(startCircle);
                for (int i = 1; i < journey.legs().size() - 1; i++) {
                        Duration sinceStart = Duration.between(journey.depTime(), journey.legs().get(i).depTime());
                        double ratio = (double) sinceStart.toMinutes() / journey.duration().toMinutes();

                        Circle circle = new Circle(3);
                        circle.setUserData(ratio); // store ratio as double
                        if (journey.legs().get(i) instanceof Journey.Leg.Foot) {
                            circle.getStyleClass().add("transfer");
                            stopCircles.add(circle);
                        }
                    }
                    Circle endCircle = new Circle(3);
                    endCircle.setUserData(1.0); // full journey = ratio 1.0
                    endCircle.getStyleClass().add("dep-arr");
                    stopCircles.add(endCircle);

                centerPane.setUserData(journey);

                Journey.Leg.Transport transportLeg = journey.legs().getFirst() instanceof Journey.Leg.Transport
                    ? (Journey.Leg.Transport) journey.legs().getFirst()
                    : (Journey.Leg.Transport ) journey.legs().get(1);
                directionText.setText(FormatterFr.formatRouteDestination(transportLeg));
                transportImage.setImage(VehicleIcons.iconFor(transportLeg.vehicle()));

                setGraphic(borderPane);
            }
        }
    }
}

