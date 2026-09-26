package com.hsf302.ch4.runner;

import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.service.DepartmentService;
import com.hsf302.ch4.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class ExerciseRunner implements CommandLineRunner {

    // Runner CHỈ phụ thuộc vào Service (interface), KHÔNG inject Repository
    private final DepartmentService departmentService;
    private final StudentService studentService;

    @Override
    public void run(String... args) {
        partB();
        partC();
        partD();
        bonus(); // chạy trên dữ liệu gốc -> trước Part E
        partE();
    }

    private void partB() {
        todo6();
        todo7();
    }

    private void partC() {
        todo8();
        todo9();
        // todo10(); todo11();
    }

    private void partD() {
        // todo12(); todo13(); todo14(); todo15(); todo16(); todo17(); todo18(); todo19();
    }

    private void bonus() {
        // todo24();
    }

    private void partE() {
        // todo20(); todo21(); todo22(); todo23();
    }

    // ===== TODO implementations =====
    private void todo6() {
        title("TODO 6: count / findById / existsById");
        System.out.println("Departments: " + departmentService.count());
        System.out.println("Students   : " + studentService.count());

        studentService.findById(1L).ifPresentOrElse(
                s -> System.out.println("findById(1)  -> " + s),
                () -> System.out.println("findById(1)  -> Not found"));

        System.out.println("findById(99) -> " + studentService.findById(99L)
                .map(Object::toString)
                .orElse("Not found"));

        System.out.println("existsById(4) department -> " + departmentService.existsById(4L));
    }

    private void todo7() {
        title("TODO 7: Sort & Pageable");

        // (a) GPA giảm dần
        printList("All students order by GPA desc", studentService.findAllOrderByGpaDesc());

        // (b) Trang THỨ 2 -> index 1 (Spring Data đánh số trang từ 0)
        Page<Student> page = studentService.findPage(1, 3, "fullName");
        printList("Page index " + page.getNumber() + " (size " + page.getSize() + ")", page.getContent());
        System.out.println("totalElements=" + page.getTotalElements()
                + ", totalPages=" + page.getTotalPages()
                + ", hasNext=" + page.hasNext()
                + ", hasPrevious=" + page.hasPrevious());
    }

    private void todo8() {
        title("TODO 8: findBy / existsBy / countBy");
        for (String code : List.of("AI002", "XX999")) {
            System.out.println("findByStudentCode(" + code + ") -> " +
                    studentService.findByStudentCode(code).map(Object::toString).orElse("Not found"));
        }
        System.out.println("isEmailExisted(binh.tt@fpt.edu.vn) -> "
                + studentService.isEmailExisted("binh.tt@fpt.edu.vn"));
        System.out.println("countActive -> " + studentService.countActive());
    }

    private void todo9() {
        title("TODO 9: Containing / EndingWith / IsNull");
        printList("fullName contains 'nguyen'", studentService.searchByName("nguyen"));
        printList("email domain 'gmail.com'", studentService.findByEmailDomain("gmail.com"));
        printList("email is null", studentService.findWithoutEmail());
    }

    // ===== helpers =====
    private void title(String t) {
        System.out.println("\n===== " + t + " =====");
    }

    private void printList(String label, Collection<?> list) {
        System.out.println("-- " + label + ":");
        list.forEach(o -> System.out.println("   " + o));
        System.out.println("   -> " + list.size() + " record(s)");
    }
}
