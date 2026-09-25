package com.hsf302.ch4.runner;

import com.hsf302.ch4.pojo.Department;
import com.hsf302.ch4.pojo.Gender;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.DepartmentRepository;
import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.hsf302.ch4.pojo.Gender.FEMALE;
import static com.hsf302.ch4.pojo.Gender.MALE;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    @Override
    public void run(String... args) {
        if (studentRepository.count() > 0) return; // đề phòng khi đổi ddl-auto=update

        Department se = new Department("SE", "Software Engineering");
        Department ai = new Department("AI", "Artificial Intelligence");
        Department ia = new Department("IA", "Information Assurance");
        Department gd = new Department("GD", "Graphic Design");
        departmentRepository.saveAll(List.of(se, ai, ia, gd));

        studentRepository.saveAll(List.of(
            st("SE001", "Nguyen Van An",  "an.nv@fpt.edu.vn",   MALE,   "2005-03-15", 3.2, true,  se),
            st("SE002", "Tran Thi Binh",  "binh.tt@fpt.edu.vn", FEMALE, "2004-07-22", 3.8, true,  se),
            st("SE003", "Le Van Cuong",   "cuong.lv@fpt.edu.vn",MALE,   "2003-11-05", 2.5, false, se),
            st("AI001", "Pham Thi Dung",  "dung.pt@fpt.edu.vn", FEMALE, "2006-01-10", 3.5, true,  ai),
            st("AI002", "Hoang Van Em",   "em.hv@gmail.com",    MALE,   "2002-09-30", 2.8, true,  ai),
            st("AI003", "Vo Thi Hoa",     "hoa.vt@fpt.edu.vn",  FEMALE, "2005-05-18", 3.9, true,  ai),
            st("IA001", "Dang Van Giang", "giang.dv@gmail.com", MALE,   "2001-12-01", 1.9, false, ia),
            st("IA002", "Bui Thi Lan",    "lan.bt@fpt.edu.vn",  FEMALE, "2004-02-14", 3.1, true,  ia),
            st("SE004", "Nguyen Thi Mai", "mai.nt@fpt.edu.vn",  FEMALE, "2003-08-08", 3.6, true,  se),
            st("IA003", "Do Van Nam",     null,                 MALE,   "2005-10-20", 2.2, true,  ia)
        ));
        System.out.println(">>> Seeded " + departmentRepository.count() + " departments, "
                + studentRepository.count() + " students");
    }

    private Student st(String code, String name, String email, Gender gender,
                       String dob, double gpa, boolean active, Department dept) {
        Student s = new Student();
        s.setStudentCode(code);
        s.setFullName(name);
        s.setEmail(email);
        s.setGender(gender);
        s.setDob(LocalDate.parse(dob));
        s.setGpa(gpa);
        s.setActive(active);
        dept.addStudent(s); // set cả 2 chiều; quan trọng nhất là s.department (owning side)
        return s;
    }
}
