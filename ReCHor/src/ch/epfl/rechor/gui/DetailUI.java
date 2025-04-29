package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.List;

public record DetailUI(Node rootNode){

    public static DetailUI create(ObservableValue<Journey> journey){

        //buttons section
        Button mapButton = new Button("Carte");
        Button calendarButton = new Button("Calendier");
        HBox buttonBox = new HBox(mapButton, calendarButton);
        buttonBox.setId("buttons");

        //no journey case
        Text noJourneyText = new Text("Aucune voyage");
        Pane noJourneyBox = new VBox(noJourneyText);
        noJourneyBox.setId("no-journey");

        Pane legsPane = new GridPane();
        legsPane.setId("legs");
        int rowIndex = 0;
        for(Journey.Leg leg : journey.getValue().legs()){
            switch (leg){
                case Journey.Leg.Foot f -> {
                    rowIndex++;
                    Text footText = new Text(FormatterFr.formatLeg(f));
                    GridPane.setConstraints(footText,2,rowIndex,2,1);
                    legsPane.getChildren().add(footText);
                }
                case Journey.Leg.Transport t -> {
                    List<Journey.Leg.IntermediateStop> intermediateStops = t.intermediateStops();

                    //first row
                    rowIndex++;
                    Text depTime = new Text(FormatterFr.formatTime(t.depTime()));
                    Circle depCircle = new Circle(3d);
                    Text depStation = new Text(t.depStop().name());
                    Text depPlatform = new Text(FormatterFr.formatPlatformName(t.depStop()));
                    depTime.getStyleClass().add("departure.css");
                    depPlatform.getStyleClass().add("departure.css");
                    GridPane.setConstraints(depTime,0,rowIndex);
                    GridPane.setConstraints(depCircle, 1,rowIndex);
                    GridPane.setConstraints(depStation, 2, rowIndex);
                    GridPane.setConstraints(depPlatform,3,rowIndex);

                    //second row
                    rowIndex++;
                    int iconRowSpan = intermediateStops.isEmpty() ? 1 : 2;
                    ImageView vehicleIcon = new ImageView(VehicleIcons.iconFor(t.vehicle()));
                    vehicleIcon.setFitWidth(31);
                    vehicleIcon.setFitHeight(31);
                    Text destination = new Text(FormatterFr.formatRouteDestination(t));
                    GridPane.setConstraints(vehicleIcon,0,iconRowSpan);
                    GridPane.setConstraints(destination,2,rowIndex,2,1);

                    //third row, if needed
                    if(!intermediateStops.isEmpty()){
                        rowIndex++;
                        VBox stops = new VBox();
                        GridPane stopsGrid = new GridPane();
                        stopsGrid.getStyleClass().add("intermediate-stops");
                        int stopRow = 0;
                        for(Journey.Leg.IntermediateStop s : intermediateStops){
                            Text arrTime = new Text(FormatterFr.formatTime(s.arrTime()));
                            Text stopName = new Text(s.stop().name());

                            GridPane.setConstraints(arrTime, 0, stopRow);
                            GridPane.setConstraints(depTime, 1, stopRow);
                            GridPane.setConstraints(stopName, 2, stopRow);

                            stopsGrid.getChildren().addAll(arrTime, depTime, stopName);
                            stopRow++;
                        }
                        String tiltedText = intermediateStops.size() + " arrêts, "
                            + FormatterFr.formatDuration(t.duration());
                        TitledPane intermediateStopsPane = new TitledPane(tiltedText,stops);
                        Accordion intermediateLegs = new Accordion(intermediateStopsPane);
                        GridPane.setConstraints(intermediateLegs,2,rowIndex,2,1);
                    }

                    //fourth row
                    rowIndex++;
                    Text arrTime = new Text(FormatterFr.formatTime(t.arrTime()));
                    Circle arrCircle = new Circle(3d);
                    Text arrStation = new Text(t.arrStop().name());
                    Text arrPlatform = new Text(FormatterFr.formatPlatformName(t.arrStop()));
                    GridPane.setConstraints(arrTime,0,rowIndex);
                    GridPane.setConstraints(arrCircle, 1,rowIndex);
                    GridPane.setConstraints(arrStation, 2, rowIndex);
                    GridPane.setConstraints(arrPlatform,3,rowIndex);
                }
            }
        }

        Pane journeyBox = new VBox(buttonBox);
        Pane mainStackPane = new StackPane(journeyBox, noJourneyBox);


        ScrollPane root = new ScrollPane(mainStackPane);
        root.setId("detail");
        root.getStylesheets().add("detail.css");

        Pane tripPane = new StackPane();
        Pane gridPane = new GridPane();

        return new DetailUI(root);
    }
}
