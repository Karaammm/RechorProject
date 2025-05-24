package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import java.util.List;

/**
 * The StopField record encapsulates a text field and an observable value for a
 * stop name. It provides functionality for creating a text field with
 * auto-complete suggestions based on a provided StopIndex. The suggestions are
 * displayed in a popup window.
 *
 * Features: - Auto-complete suggestions for stop names based on user input. - A
 * popup window that dynamically updates with matching stop names. - Listeners
 * to handle text input and focus changes. - Ability to set the stop name
 * programmatically.
 */
public record StopField (TextField textField, ObservableValue<String> stopO){


    /**
     * Creates a StopField instance with auto-complete functionality.
     *
     * @param stopIndex the StopIndex used to fetch matching stop names for
     *                  auto-complete.
     * @return a StopField instance containing the text field and observable stop
     *         name.
     */
    public static StopField create(StopIndex stopIndex) {
        TextField textField = new TextField();
        ObservableList<String> stopsList = FXCollections.observableArrayList();
        SimpleStringProperty stopName = new SimpleStringProperty("");

        ListView<String> results = new ListView<>(stopsList);
        results.setFocusTraversable(false);
        results.setMaxHeight(240);

        Popup window = new Popup();
        window.setHideOnEscape(false);
        window.getContent().add(results);

        ChangeListener<String> textListener = (obs, oldVal, newVal) -> {
            List<String> matches = stopIndex.stopsMatching(newVal, 30);
            stopsList.setAll(matches);
            if (matches.isEmpty()) {
                window.hide();
            } else {
                results.getSelectionModel().selectFirst();
                Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
                window.show(textField, bounds.getMinX(), bounds.getMaxY());
            }
        };

        ChangeListener<Bounds> boundsListener = (obs, oldBounds, newBounds) -> {
            Bounds screenBounds = textField.localToScreen(newBounds);
            if (screenBounds != null) {
                window.setAnchorX(screenBounds.getMinX());
                window.setAnchorY(screenBounds.getMaxY());
            }
        };

        textField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
            case UP:
                results.getSelectionModel().selectPrevious();
                e.consume();
                break;
            case DOWN:
                results.getSelectionModel().selectNext();
                e.consume();
                break;
            }
        });

        textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                textField.textProperty().removeListener(textListener);
                textField.boundsInLocalProperty().removeListener(boundsListener);

                String selected = results.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    stopName.set(selected);
                    textField.setText(selected);
                } else {
                    stopName.set("");
                    textField.clear();
                }
                window.hide();
            } else {
                textField.textProperty().addListener(textListener);
                textField.boundsInLocalProperty().addListener(boundsListener);
            }
        });

        return new StopField(textField, stopName);
    }

    /**
     * Sets the stop name programmatically.
     *
     * @param stopName the name of the stop to set in the text field and observable
     *                 value.
     */
    public void setTo(String stopName) {
        textField.setText(stopName);
        ((SimpleStringProperty) stopO).set(stopName);
    }
}
