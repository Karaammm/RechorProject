package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * The QueryUI class represents a user interface component for querying public
 * transport schedules. It encapsulates nodes and observable values for
 * departure stop, arrival stop, date, and time. Designed for use with a
 * provided StopIndex instance to help users interact and create queries.
 */
public record QueryUI(Node rootNode, ObservableValue<String> depStopO,
                      ObservableValue<String> arrStopO, ObservableValue<LocalDate> dateO,
                      ObservableValue<LocalTime> timeO) {

    /**
     * Creates a new QueryUI instance with the provided StopIndex.
     *
     * @param stopIndex the StopIndex to be used for departure and arrival stop
     *                  fields
     * @return a new QueryUI instance containing GUI elements for querying stops
     *         with configurable date, time, and stop information
     */
    public static QueryUI create(StopIndex stopIndex) {

        // date
        Label dateLabel = new Label("Date:\u202f");
        LocalDate today = LocalDate.now();
        SimpleObjectProperty<LocalDate> dateObs = new SimpleObjectProperty<>(today);
        DatePicker datePicker = new DatePicker(dateObs.get());
        datePicker.setId("date");
        dateObs.bind(datePicker.valueProperty());

        // time
        Label timeLabel = new Label("Heure:\u202f");
        TextField timeField = new TextField();
        DateTimeFormatter toString = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter fromString = DateTimeFormatter.ofPattern("H:mm");
        LocalTimeStringConverter converter = new LocalTimeStringConverter(toString, fromString);
        SimpleObjectProperty<LocalTime> timeObs = new SimpleObjectProperty<>(LocalTime.now());
        TextFormatter<LocalTime> formatter = new TextFormatter<>(converter, timeObs.get());
        timeField.setTextFormatter(formatter);
        timeObs.bindBidirectional(formatter.valueProperty());

        // departure
        Label departLabel = new Label("Départ:\u202f");
        StopField departStopField = StopField.create(stopIndex);
        TextField departTextField = departStopField.textField();
        departTextField.setPromptText("Nom de l'arrêt de départ");
        departTextField.setId("depStop");
        ObservableValue<String> depStopName = departStopField.stopO();

        // arrival
        Label arriveLabel = new Label("Arrivée:\u202f");
        StopField arriveStopField = StopField.create(stopIndex);
        TextField arriveTextField = arriveStopField.textField();
        arriveTextField.setPromptText("Nom de l'arrêt d'arrivée");
        ObservableValue<String> arrStopName = arriveStopField.stopO();

        // exchange button
        Button exchangeButton = new Button("↔");
        exchangeButton.setOnAction(e -> {
            String dep = departStopField.textField().getText();
            String arr = arriveStopField.textField().getText();
            departStopField.setTo(arr);
            arriveStopField.setTo(dep);
        });

        // box containing the bottom layer
        HBox bottomBox = new HBox(dateLabel, datePicker, timeLabel, timeField);

        // box containing top layer
        HBox topBox = new HBox(departLabel, departTextField,
                               exchangeButton, arriveLabel, arriveTextField);

        // root box
        VBox rootBox = new VBox(topBox, bottomBox);
        rootBox.getStylesheets().add("query.css");

        return new QueryUI(rootBox, depStopName, arrStopName, dateObs, timeObs);
    }
}
