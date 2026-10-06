package com.hsf302.chapter6.config;

import com.hsf302.chapter6.entity.Student;
import com.hsf302.chapter6.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final StudentRepository studentRepository;

    public DataInitializer(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() > 0) {
            log.info("Bảng students đã có dữ liệu → bỏ qua seed");
            return;
        }
        studentRepository.saveAll(List.of(
                new Student("Nguyễn Văn An",  "an@fpt.edu.vn",    20, "CNTT", 3.5),
                new Student("Trần Thị Bình",  "binh@fpt.edu.vn",  21, "KTPM", 3.2),
                new Student("Lê Minh Cường",  "cuong@fpt.edu.vn", 19, "ATTT", 3.8),
                new Student("Phạm Thị Dung",  "dung@fpt.edu.vn",  22, "HTTT", 2.9)
        ));
        log.info("Đã seed {} sinh viên", studentRepository.count());
    }
}
