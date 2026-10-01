package com.hsf302.ch4.runner;

import com.hsf302.ch4.service.CourseService;
import com.hsf302.ch4.service.EnrollmentService;
import com.hsf302.ch4.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Order(3)
@Profile("ex2")
@RequiredArgsConstructor
public class Exercise2Runner implements CommandLineRunner {

    // CHỈ inject Service interface
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final StudentService studentService;          // của Exercise 1 (TODO 16a, 21)

    @Override
    public void run(String... args) {
        partB();
        partC();
        partD();
        bonus();        // chạy trên dữ liệu gốc → trước Part E
        partE();
    }

    private void partB() { /* todo6(); todo7(); */ }
    private void partC() { /* todo8(); todo9(); todo10(); todo11(); */ }
    private void partD() { /* todo12(); todo13(); todo14(); todo15(); todo16(); todo17(); todo18(); todo19(); */ }
    private void bonus() { /* todo25(); */ }
    private void partE() { /* todo20(); todo21(); todo22(); todo23(); todo24(); */ }

    // ===== helpers =====
    private void title(String t) {
        System.out.println("\n===== " + t + " =====");
    }

    private void printList(String label, Collection<?> list) {
        System.out.println("-- " + label + ":");
        list.forEach(o -> System.out.println("   " + o));
        System.out.println("   -> " + list.size() + " record(s)");
    }

    /** Chạy 1 thao tác ghi, in [OK] hoặc [FAIL] + message (dùng cho Part E). */
    private void attempt(String label, Runnable action) {
        try {
            action.run();
            System.out.println("   [OK]   " + label);
        } catch (RuntimeException e) {
            System.out.println("   [FAIL] " + label + " -> " + e.getMessage());
        }
    }
}
