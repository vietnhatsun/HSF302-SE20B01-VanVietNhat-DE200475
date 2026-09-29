package com.hsf302.ch4.runner;

import com.hsf302.ch4.dto.StudentSummary;
import com.hsf302.ch4.pojo.Department;
import com.hsf302.ch4.pojo.Gender;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.service.DepartmentService;
import com.hsf302.ch4.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.hibernate.LazyInitializationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
        todo10();
        todo11();
    }

    private void partD() {
        todo12();
        todo13();
        todo14();
        todo15();
        todo16();
        todo17();
        todo18();
        todo19();
    }

    private void bonus() {
        todo24();
    }

    private void partE() {
        todo20();
        todo21();
        todo22();
        // todo23();
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

    private void todo10() {
        title("TODO 10: Between / And / True / After");
        printList("GPA in [3.0, 3.6] desc", studentService.findByGpaRange(3.0, 3.6));
        printList("MALE & active", studentService.findActiveByGender(Gender.MALE));
        printList("dob after 2005-01-01", studentService.findBornAfter(LocalDate.of(2005, 1, 1)));
    }

    private void todo11() {
        title("TODO 11: Nested property / Top / IsEmpty");
        printList("Students of SE (order by name)", studentService.findByDepartment("SE"));
        System.out.println("count students of AI -> " + studentService.countByDepartment("AI"));
        printList("Top 3 GPA", studentService.findTop3ByGpa());
        printList("Departments without students", departmentService.findDepartmentsWithoutStudents());
    }

    private void todo12() {
        title("TODO 12: JPQL + named parameter");
        printList("SE, GPA >= 3.0", studentService.findGoodStudents("SE", 3.0));
    }

    private void todo13() {
        title("TODO 13: JPQL LIKE");
        printList("keyword 'hoa'", studentService.searchByKeyword("hoa"));
        printList("keyword 'gmail'", studentService.searchByKeyword("gmail"));
    }

    private void todo14() {
        title("TODO 14: Statistics by department (DTO)");
        printList("code | name | total | avgGpa", departmentService.getStatistics());
    }

    private void todo15() {
        title("TODO 15: Subquery - GPA average");
        printList("GPA > AVG", studentService.findAboveAverageGpa());
    }

    private void todo16() {
        title("TODO 16: LazyInitializationException & JOIN FETCH");

        Department ai = departmentService.findByCode("AI").orElseThrow();
        try {
            System.out.println("AI has " + ai.getStudents().size() + " students");
        } catch (LazyInitializationException e) {
            System.out.println("(a) Caught: " + e.getClass().getSimpleName());
            System.out.println("    " + e.getMessage());
        }

        Department aiFull = departmentService.getWithStudents("AI");
        System.out.println("(b) " + aiFull);
        aiFull.getStudents().forEach(s -> System.out.println("     " + s));
    }

    private void todo17() {
        title("TODO 17: Native query - TOP N");
        printList("Top 2 GPA of SE", studentService.findTopNInDepartment("SE", 2));
    }

    private void todo18() {
        title("TODO 18: Interface projection");
        List<StudentSummary> list = studentService.getActiveSummaries();
        list.forEach(p -> System.out.printf("   %s | %-15s | %.1f | %s%n",
                p.getStudentCode(), p.getFullName(), p.getGpa(), p.getDepartmentName()));
        System.out.println("   -> " + list.size() + " record(s)");
    }

    private void todo19() {
        title("TODO 19: @Query + Pageable");
        for (int i = 0; i < 2; i++) {
            Page<Student> page = studentService.findActiveByDepartment("SE", i, 2);
            printList("SE active - page " + page.getNumber(), page.getContent());
            System.out.println("   totalElements=" + page.getTotalElements()
                    + ", totalPages=" + page.getTotalPages());
        }
    }

    private void todo24() {
        title("TODO 24 (Bonus): Specification");
        printList("search(null, AI, 3.0, true)", studentService.search(null, "AI", 3.0, true));
        printList("search(van, null, null, null)", studentService.search("van", null, null, null));
    }

    private void todo20() {
        title("TODO 20: Update GPA (dirty checking)");
        System.out.println("Before: " + studentService.findByStudentCode("SE001").orElseThrow());
        studentService.updateGpa("SE001", 3.4);
        System.out.println("After : " + studentService.findByStudentCode("SE001").orElseThrow());
    }

    private void todo21() {
        title("TODO 21: @Modifying UPDATE");
        int rows = studentService.deactivateLowGpa(2.5);
        System.out.println("Rows affected: " + rows);
        System.out.println("Active students now: " + studentService.countActive());
    }

    private void todo22() {
        title("TODO 22: Transfer IA -> SE, then delete IA");
        int moved = departmentService.transferStudentsAndDelete("IA", "SE");
        System.out.println("Students moved: " + moved);
        System.out.println("Students of SE: " + studentService.countByDepartment("SE"));
        printList("Departments left", departmentService.findAll());
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
