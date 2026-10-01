package com.hsf302.ch4.service;

import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.CourseRepository;
import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<Course> getCoursesOfStudent(String studentCode) {
        Student s = getStudent(studentCode);
        return s.getCourses().stream()                       // nạp LAZY: vẫn trong transaction → OK
                .sorted(Comparator.comparing(Course::getCode))
                .toList();
    }

    @Override
    public List<Student> getStudentsOfCourse(String courseCode) {
        Course c = getCourse(courseCode);
        return c.getStudents().stream()                      // inverse side vẫn ĐỌC được bình thường
                .sorted(Comparator.comparing(Student::getFullName))
                .toList();
    }

    // ===== TODO 9 =====
    @Override
    public List<Student> findStudentsInCourse(String courseCode) {
        return studentRepository.findByCourses_CodeOrderByFullNameAsc(courseCode);
    }

    @Override
    public long countStudentsInCourse(String courseCode) {
        return studentRepository.countByCourses_Code(courseCode);
    }

    @Override
    public List<Student> findActiveStudentsInCourse(String courseCode) {
        return studentRepository.findByCourses_CodeAndActiveTrueOrderByFullNameAsc(courseCode);
    }

    // ===== TODO 11 =====
    @Override
    public List<Student> findStudentsWithoutCourses() {
        return studentRepository.findByCoursesIsEmptyOrderByFullNameAsc();
    }

    @Override
    public boolean isEnrolled(String studentCode, String courseCode) {
        return studentRepository.existsByStudentCodeAndCourses_Code(studentCode, courseCode);
    }

    // ===== Part D =====
    @Override
    public List<Student> findGoodStudentsInCourse(String courseCode, double minGpa) {
        if (minGpa < 0 || minGpa > 4) {
            throw new IllegalArgumentException("minGpa must be in [0, 4]");
        }
        return studentRepository.findGoodStudentsInCourse(courseCode, minGpa);
    }


    // ===== helper dùng chung cho mọi method =====
    private Student getStudent(String studentCode) {
        if (studentCode == null || studentCode.isBlank()) {
            throw new IllegalArgumentException("Student code must not be blank");
        }
        return studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentCode));
    }

    private Course getCourse(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code must not be blank");
        }
        return courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseCode));
    }
}
