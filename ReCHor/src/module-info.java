module ReCHor {
    requires javafx.controls;
    requires java.desktop;
    requires java.net.http;
    requires junit.platform.console.standalone;
    //requires junit.platform.console.standalone;

    exports ch.epfl.rechor;
    exports ch.epfl.rechor.timetable;
    exports ch.epfl.rechor.gui;
    exports ch.epfl.rechor.gui_debug;
    exports ch.epfl.rechor.journey;
    exports ch.epfl.rechor.timetable.mapped;
}