package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The DetailUI class encapsulates a user interface designed to display details
 * about a journey. It uses JavaFX components and reactive programming
 * techniques to bind the UI to journey data changes.
 *
 * The UI has two major states: - When no journey is present: displays a message
 * indicating that no journey exists. - When a journey is present: displays the
 * journey's details, including legs, stops, and buttons for additional
 * features.
 *
 * The UI is updated based on the state of the provided
 * ObservableValue<Journey>, enabling real-time synchronization with journey
 * data changes.
 *
 * Components: - `noJourneyBox`: A VBox displaying a message when no journey is
 * available. - `legsPane`: A grid layout for displaying details of the
 * journey's legs, stops, and durations. - `buttonBox`: A horizontal box
 * containing buttons for exporting the journey as a calendar file or opening
 * its map view. - `mainStackPane`: A stack pane containing all other panes,
 * ensuring appropriate visibility for `journeyBox` or `noJourneyBox`.
 *
 * Methods: - `create`: Static factory method that creates and returns a
 * DetailUI instance. Hooks the UI components to the journey observable. -
 * `updateJourneyDisplay`: Private method that updates the journey details
 * displayed in the UI, based on changes in the provided Journey object.
 *
 * Nested Classes: - `LegsGridPane`: A specialized GridPane for rendering the
 * visual connection between journey stops. It manages connections using pairs
 * of circles to draw lines representing continuity in the journey.
 */
public record DetailUI(Node rootNode) {

    /**
     * Creates a new instance of {@code DetailUI} that dynamically updates its
     * display based on the observed {@code Journey} object. The UI provides
     * features for journey visualization, including a map view and calendar export
     * functionality.
     *
     * @param journeyObs an {@code ObservableValue} representing the observed
     *                   {@code Journey}. The displayed content adapts automatically
     *                   if the {@code Journey} changes.
     * @return a {@code DetailUI} instance containing the constructed user
     *         interface.
     */
    public static DetailUI create(ObservableValue<Journey> journeyObs) {
        Journey journey = journeyObs.getValue();

        // no journey case
        Text noJourneyText = new Text("Aucun voyage");
        VBox noJourneyBox = new VBox(noJourneyText);
        noJourneyBox.setId("no-journey");

        // journey isn't empty case
        Pane linePane = new Pane();
        linePane.setId("annotations");
        LegsGridPane legsPane = new LegsGridPane(linePane);
        legsPane.setId("legs");

        // create buttons but initially disabled
        Button mapButton = new Button("Carte");
        Button calendarButton = new Button("Calendrier");
        mapButton.setDisable(true);
        calendarButton.setDisable(true);
        HBox buttonBox = new HBox(mapButton, calendarButton);
        buttonBox.setId("buttons");

        // update the display and bind it to the observer
        journeyObs.subscribe(newJourney -> {
            updateJourneyDisplay(newJourney, noJourneyBox, legsPane);
            if (newJourney != null) {
                calendarButton.setDisable(false);
                String icalendar = JourneyIcalConverter.toIcalendar(newJourney);
                calendarButton.setOnAction(e -> {
                    try {
                        FileChooser fileChooser = new FileChooser();
                        String date = newJourney.depTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        fileChooser.setInitialFileName("voyage_" + date + ".ics");

                        File selectedFile = fileChooser.showSaveDialog(null);
                        if (selectedFile != null) {
                            Files.writeString(selectedFile.toPath(),
                                              icalendar, StandardOpenOption.CREATE);
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                mapButton.setDisable(false);
                String geoJsonDoc = JourneyGeoJsonConverter.toGeoJson(newJourney).toString();
                mapButton.setOnAction(event -> {
                    try {
                        URI url = new URI("https",
                                          "umap.osm.ch",
                                          "/fr/map",
                                          "data=" + geoJsonDoc,
                                          null);
                        Desktop.getDesktop().browse(url);
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(e);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

            }
        });

        StackPane tripPane = new StackPane(linePane, legsPane);
        Pane journeyBox = new VBox(tripPane, buttonBox);

        Pane mainStackPane = new StackPane(journeyBox, noJourneyBox);
        ScrollPane root = new ScrollPane(mainStackPane);
        root.setId("detail");
        root.getStylesheets().add("detail.css");

        return new DetailUI(root);
    }

    /**
     * Updates the journey display by dynamically modifying the visibility and
     * content of the provided UI components based on the journey data. If the
     * journey is null or has no legs, displays an alternative view; otherwise,
     * builds a detailed visualization for the journey's legs and intermediate
     * stops.
     *
     * @param journey      the {@code Journey} object containing the travel
     *                     information, including its legs and intermediate stops.
     *                     If null or empty, alternative UI elements are shown.
     * @param noJourneyBox a {@code VBox} used to display when no journey is
     *                     available or the journey contains no legs.
     * @param legsPane     a {@code LegsGridPane} responsible for visualizing
     *                     detailed journey leg information, including stops, time,
     *                     and transport modes.
     */
    private static void updateJourneyDisplay(Journey journey,
                                             VBox noJourneyBox,
                                             LegsGridPane legsPane) {
        boolean journeyIsEmpty = journey == null || journey.legs().isEmpty();

        noJourneyBox.setVisible(journeyIsEmpty);
        legsPane.setVisible(!journeyIsEmpty);
        legsPane.getChildren().clear();
        legsPane.pairs.clear();

        if (journeyIsEmpty)
            return;

        int rowIndex = -1;
        for (Journey.Leg leg : journey.legs()) {
            switch (leg) {
            case Journey.Leg.Foot f -> {
                rowIndex++;
                Text footText = new Text(FormatterFr.formatLeg(f));
                GridPane.setConstraints(footText, 2, rowIndex, 2, 1);
                legsPane.getChildren().add(footText);

            }

            case Journey.Leg.Transport t -> {
                List<Journey.Leg.IntermediateStop> intermediateStops = t.intermediateStops();

                // First row: departure
                rowIndex++;
                Text depTime = new Text(FormatterFr.formatTime(t.depTime()));
                Circle depCircle = new Circle(3d);
                Text depStation = new Text(t.depStop().name());
                Text depPlatform = new Text(FormatterFr.formatPlatformName(t.depStop()));
                depTime.getStyleClass().add("departure");
                depPlatform.getStyleClass().add("departure");
                GridPane.setConstraints(depTime, 0, rowIndex);
                GridPane.setConstraints(depCircle, 1, rowIndex);
                GridPane.setConstraints(depStation, 2, rowIndex);
                GridPane.setConstraints(depPlatform, 3, rowIndex);
                legsPane.getChildren().addAll(depTime, depCircle, depStation, depPlatform);

                // Second row: icon and destination
                rowIndex++;
                int iconRowSpan = intermediateStops.isEmpty() ? 1 : 2;
                ImageView vehicleIcon = new ImageView(VehicleIcons.iconFor(t.vehicle()));
                vehicleIcon.setFitWidth(31);
                vehicleIcon.setFitHeight(31);
                Text destination = new Text(FormatterFr.formatRouteDestination(t));
                GridPane.setConstraints(vehicleIcon, 0, rowIndex, 1, iconRowSpan);
                GridPane.setConstraints(destination, 2, rowIndex, 2, 1);
                legsPane.getChildren().addAll(vehicleIcon, destination);

                // Third row: intermediate stops (if any)
                if (!intermediateStops.isEmpty()) {
                    rowIndex++;
                    GridPane stopsGrid = new GridPane();
                    stopsGrid.getStyleClass().add("intermediate-stops");
                    int stopRow = 0;
                    for (Journey.Leg.IntermediateStop s : intermediateStops) {
                        Text arrTime = new Text(FormatterFr.formatTime(s.arrTime()));
                        Text depStopTime = new Text(FormatterFr.formatTime(s.depTime()));
                        Text stopName = new Text(s.stop().name());

                        GridPane.setConstraints(arrTime, 0, stopRow);
                        GridPane.setConstraints(depStopTime, 1, stopRow);
                        GridPane.setConstraints(stopName, 2, stopRow);
                        stopsGrid.getChildren().addAll(arrTime, depStopTime, stopName);
                        stopRow++;
                    }
                    String titledText = intermediateStops.size() + " arrêts, "
                            + FormatterFr.formatDuration(t.duration());
                    TitledPane titledPane = new TitledPane(titledText, stopsGrid);
                    Accordion accordion = new Accordion(titledPane);
                    GridPane.setConstraints(accordion, 2, rowIndex, 2, 1);
                    legsPane.getChildren().add(accordion);
                }

                // Fourth row: arrival
                rowIndex++;
                Text arrTime = new Text(FormatterFr.formatTime(t.arrTime()));
                Circle arrCircle = new Circle(3d);
                Text arrStation = new Text(t.arrStop().name());
                Text arrPlatform = new Text(FormatterFr.formatPlatformName(t.arrStop()));
                GridPane.setConstraints(arrTime, 0, rowIndex);
                GridPane.setConstraints(arrCircle, 1, rowIndex);
                GridPane.setConstraints(arrStation, 2, rowIndex);
                GridPane.setConstraints(arrPlatform, 3, rowIndex);
                legsPane.addCirclePair(new javafx.util.Pair<>(depCircle, arrCircle));
                legsPane.getChildren().addAll(arrTime, arrCircle, arrStation, arrPlatform);

            }
            }
        }
    }

    /**
     * A private subclass of {@code GridPane} designed specifically to manage and
     * visualize pairs of {@code Circle} instances with connecting lines. The lines
     * are dynamically updated based on the layout of the circles within the grid.
     *
     * <ul>
     * Responsibilities of the class: - Manages a collection of {@code Pair} objects
     * consisting of two {@code Circle} instances each. - Adds and stores pairs of
     * {@code Circle} objects. - Draws lines connecting the circles in each pair
     * whenever the layout changes. - Clears and redraws the lines dynamically
     * during layout updates.
     */
    private static class LegsGridPane extends GridPane {
        private final List<Pair<Circle, Circle>> pairs = new ArrayList<>();
        private final Pane linePane;

        private LegsGridPane(Pane linePane) {
            this.linePane = linePane;
        }

        private void addCirclePair(Pair<Circle, Circle> pair) {
            pairs.add(pair);
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            linePane.getChildren().clear();
            for (Pair<Circle, Circle> pair : pairs) {
                Line line = new Line(pair.getKey().getBoundsInParent().getCenterX(),
                        pair.getKey().getBoundsInParent().getCenterY(),
                        pair.getValue().getBoundsInParent().getCenterX(),
                        pair.getValue().getBoundsInParent().getCenterY());
                line.setStrokeWidth(2);
                line.setStroke(Paint.valueOf("red"));
                linePane.getChildren().add(line);
            }
        }
    }
}
