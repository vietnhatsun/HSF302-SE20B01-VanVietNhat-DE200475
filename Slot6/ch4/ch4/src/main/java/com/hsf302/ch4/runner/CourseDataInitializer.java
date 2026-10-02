package com.hsf302.ch4.runner;

import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.CourseRepository;
import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class CourseDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional // BẮT BUỘC: s.getCourses() là LAZY
    public void run(String... args) {
        if (courseRepository.count() > 0) return;

        Course prj = new Course("PRJ301", "Java Web Application Development",       3, 5, "FA26");
        Course hsf = new Course("HSF302", "Hibernate & Spring Framework",           3, 6, "FA26");
        Course swp = new Course("SWP391", "Software Development Project",           4, 4, "FA26");
        Course ail = new Course("AIL303", "Machine Learning",                       3, 4, "FA26");
        Course iaa = new Course("IAA202", "Risk Management in Information Systems", 3, 4, "SU26");
        Course mkt = new Course("MKT101", "Marketing Principles",                   2, 4, "SU26");
        courseRepository.saveAll(List.of(prj, hsf, swp, ail, iaa, mkt));

        enroll("SE001", prj, hsf, swp);
        enroll("SE002", prj, hsf, ail);
        enroll("SE003", prj);
        enroll("SE004", hsf, swp);
        enroll("AI001", ail, hsf);
        enroll("AI002", ail);
        enroll("AI003", ail, prj, swp);
        enroll("IA001", iaa);
        enroll("IA002", iaa, hsf);
        // IA003 (Do Van Nam) chưa đăng ký khóa nào; MKT101 chưa có sinh viên

        System.out.println(">>> Seeded " + courseRepository.count() + " courses");
    }

    private void enroll(String studentCode, Course... courses) {
        Student s = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new IllegalStateException("Missing student " + studentCode));
        for (Course c : courses) {
            s.enroll(c);
        }
    }
}
