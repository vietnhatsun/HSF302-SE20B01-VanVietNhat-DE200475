package com.hsf302.chapter6.service.impl;

import com.hsf302.chapter6.entity.Student;
import com.hsf302.chapter6.repository.StudentRepository;
import com.hsf302.chapter6.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)          // mặc định: mọi method chỉ đọc (tối ưu hiệu năng)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    // Constructor injection (khuyên dùng, không cần @Autowired)
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    @Transactional                      // ghi dữ liệu → bỏ readOnly
    public Student create(Student student) {
        student.setId(null);            // luôn INSERT, không bao giờ ghi đè bản ghi cũ
        return studentRepository.save(student);
    }

    @Override
    @Transactional
    public boolean update(Long id, Student data) {
        return studentRepository.findById(id)
                .map(existing -> {
                    existing.setName(data.getName());
                    existing.setEmail(data.getEmail());
                    existing.setAge(data.getAge());
                    existing.setMajor(data.getMajor());
                    existing.setGpa(data.getGpa());
                    // Không cần gọi save(): entity đang "managed",
                    // Hibernate tự sinh UPDATE khi transaction commit (dirty checking)
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!studentRepository.existsById(id)) {
            return false;
        }
        studentRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean isEmailTaken(String email, Long excludeId) {
        if (email == null || email.isBlank()) return false;
        return excludeId == null
                ? studentRepository.existsByEmailIgnoreCase(email.trim())
                : studentRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), excludeId);
    }

    @Override
    public List<String> getMajors() {
        return List.of("CNTT", "KTPM", "HTTT", "ATTT", "MMT");
    }

    @Override
    public List<Student> search(String keyword) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        if (keyword == null || keyword.isBlank()) {
            return studentRepository.findAll(sort);
        }
        String cleanKeyword = keyword.trim();
        return studentRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(cleanKeyword, cleanKeyword, sort);
    }

    @Override
    public Page<Student> findPaginated(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        if (keyword == null || keyword.isBlank()) {
            return studentRepository.findAll(pageable);
        }
        String cleanKeyword = keyword.trim();
        return studentRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(cleanKeyword, cleanKeyword, pageable);
    }

}
