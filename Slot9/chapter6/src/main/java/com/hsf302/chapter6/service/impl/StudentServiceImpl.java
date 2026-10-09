package com.hsf302.chapter6.service.impl;

import com.hsf302.chapter6.dto.MajorStatisticDto;
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
import java.util.Set;

@Service
@Transactional(readOnly = true)          // mặc định: mọi method chỉ đọc (tối ưu hiệu năng)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "email", "age", "major", "gpa");

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // ==================== CƠ BẢN CRUD ====================

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

    // ==================== BÀI NÂNG CAO 1 & 2 ====================

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

    // ==================== BÀI NÂNG CAO 3 & 4: SẮP XẾP & LỌC ĐA TIÊU CHÍ ====================

    @Override
    public Page<Student> findWithFilterAndSort(
            String keyword,
            String major,
            Double minGpa,
            Double maxGpa,
            int page,
            int size,
            String sortField,
            String sortDir
    ) {
        // Chuẩn hóa trường sắp xếp an toàn (tránh SQL injection / lỗi property không tồn tại)
        String validField = (sortField != null && ALLOWED_SORT_FIELDS.contains(sortField.trim().toLowerCase()))
                ? sortField.trim()
                : "id";

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, validField));

        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanMajor = (major != null && !major.isBlank()) ? major.trim() : null;

        return studentRepository.searchAndFilter(cleanKeyword, cleanMajor, minGpa, maxGpa, pageable);
    }

    // ==================== PHẦN GIÁO VIÊN KIỂM TRA TRÊN LỚP ====================

    @Override
    public List<Student> findByMajor(String major) {
        return studentRepository.findByMajor(major);
    }

    @Override
    public List<Student> findTop3ByGpa() {
        return studentRepository.findTop3ByOrderByGpaDesc();
    }

    @Override
    public List<MajorStatisticDto> getMajorStatistics() {
        return studentRepository.getMajorStatistics();
    }

    @Override
    @Transactional
    public int addBonusGpaForMajor(String major, Double bonus) {
        if (bonus == null || bonus <= 0) return 0;
        return studentRepository.addBonusGpaForMajor(major, bonus);
    }

    @Override
    @Transactional
    public long deleteByMajor(String major) {
        return studentRepository.deleteByMajor(major);
    }

    @Override
    @Transactional
    public int deleteStudentsWithGpaLessThan(Double minGpa) {
        if (minGpa == null) return 0;
        return studentRepository.deleteStudentsWithGpaLessThan(minGpa);
    }
}
