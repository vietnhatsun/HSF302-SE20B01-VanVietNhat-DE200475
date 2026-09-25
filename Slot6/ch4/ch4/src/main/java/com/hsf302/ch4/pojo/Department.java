package com.hsf302.ch4.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    // Inverse side: "department" là TÊN FIELD bên Student
    @OneToMany(mappedBy = "department")
    private List<Student> students = new ArrayList<>();

    public Department(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // Helper đồng bộ 2 chiều
    public void addStudent(Student s) {
        students.add(s);
        s.setDepartment(this);
    }

    @Override
    public String toString() {
        return code + " - " + name; // KHÔNG in students (tránh LazyInitializationException và lặp vô tận)
    }
}
