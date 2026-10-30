package com.fu.ch08.t1;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class Student {
    private final StringProperty studentId = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty className = new SimpleStringProperty();
    private final DoubleProperty gpa = new SimpleDoubleProperty();
    private final ReadOnlyStringWrapper grade = new ReadOnlyStringWrapper();

    public Student(String id, String name, String cls, double gpa) {
        setStudentId(id);
        setFullName(name);
        setClassName(cls);
        setGpa(gpa);
        grade.bind(Bindings.createStringBinding(() -> classify(this.gpa.get()), this.gpa));
    }

    public static String classify(double g) {
        if (g >= 9) return "Xuất sắc";
        if (g >= 8) return "Giỏi";
        if (g >= 6.5) return "Khá";
        if (g >= 5) return "Trung bình";
        return "Yếu";
    }

    public String getStudentId() { return studentId.get(); }
    public void setStudentId(String v) { studentId.set(v); }
    public StringProperty studentIdProperty() { return studentId; }

    public String getFullName() { return fullName.get(); }
    public void setFullName(String v) { fullName.set(v); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getClassName() { return className.get(); }
    public void setClassName(String v) { className.set(v); }
    public StringProperty classNameProperty() { return className; }

    public double getGpa() { return gpa.get(); }
    public void setGpa(double v) { gpa.set(v); }
    public DoubleProperty gpaProperty() { return gpa; }

    public String getGrade() { return grade.get(); }
    public ReadOnlyStringProperty gradeProperty() { return grade.getReadOnlyProperty(); }
}
