package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.StudentCreditDTO;
import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;

import java.util.List;

public interface EnrollmentService {
    List<Course> getCoursesOfStudent(String studentCode);
    List<Student> getStudentsOfCourse(String courseCode);

    // TODO 9
    List<Student> findStudentsInCourse(String courseCode);
    long countStudentsInCourse(String courseCode);
    List<Student> findActiveStudentsInCourse(String courseCode);

    // TODO 11
    List<Student> findStudentsWithoutCourses();
    boolean isEnrolled(String studentCode, String courseCode);

    // ===== Part D =====
    List<Student> findGoodStudentsInCourse(String courseCode, double minGpa);              // TODO 12
    List<StudentCreditDTO> getCreditSummary(int minCredits);                               // TODO 14
    List<Student> findStudentsWithMoreThan(int n);                                         // TODO 15
}



