package com.fu.ch08.t4;

import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ScoreRow {
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty java = new SimpleDoubleProperty();
    private final DoubleProperty spring = new SimpleDoubleProperty();
    private final DoubleProperty javafx = new SimpleDoubleProperty();

    public ScoreRow(String n, double j, double s, double f) {
        name.set(n);
        java.set(j);
        spring.set(s);
        javafx.set(f);
    }

    public double average() {
        return Math.round((java.get() + spring.get() + javafx.get()) / 3 * 100) / 100.0;
    }

    public String grade() {
        double a = average();
        return a >= 8.5 ? "Giỏi" : a >= 7 ? "Khá" : a >= 5 ? "Trung bình" : "Yếu";
    }

    /** extractor: các property mà khi đổi thì ObservableList phát sự kiện "update" */
    public Observable[] observables() {
        return new Observable[]{name, java, spring, javafx};
    }

    public StringProperty nameProperty() { return name; }
    public DoubleProperty javaProperty() { return java; }
    public DoubleProperty springProperty() { return spring; }
    public DoubleProperty javafxProperty() { return javafx; }
    public String getName() { return name.get(); }
}
