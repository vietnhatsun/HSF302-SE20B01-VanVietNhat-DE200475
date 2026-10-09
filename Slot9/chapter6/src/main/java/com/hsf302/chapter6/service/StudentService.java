package com.hsf302.chapter6.service;

import com.hsf302.chapter6.dto.MajorStatisticDto;
import com.hsf302.chapter6.entity.Student;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface StudentService {

    // ==================== CƠ BẢN CRUD ====================

    List<Student> findAll();

    Optional<Student> findById(Long id);

    Student create(Student student);

    /** @return true nếu tìm thấy và cập nhật; false nếu không tồn tại id */
    boolean update(Long id, Student data);

    /** @return true nếu xoá được; false nếu không tồn tại id */
    boolean delete(Long id);

    /** Kiểm tra email trùng. excludeId = null khi thêm mới, = id hiện tại khi cập nhật */
    boolean isEmailTaken(String email, Long excludeId);

    List<String> getMajors();

    // ==================== BÀI NÂNG CAO 1 & 2 ====================

    /** Tìm kiếm sinh viên theo tên hoặc email */
    List<Student> search(String keyword);

    /** Phân trang danh sách sinh viên kết hợp tìm kiếm */
    Page<Student> findPaginated(String keyword, int page, int size);

    // ==================== BÀI NÂNG CAO 3 & 4: SẮP XẾP & LỌC ĐA TIÊU CHÍ ====================

    /**
     * Tìm kiếm, lọc đa điều kiện kết hợp phân trang và sắp xếp linh hoạt theo cột.
     */
    Page<Student> findWithFilterAndSort(
            String keyword,
            String major,
            Double minGpa,
            Double maxGpa,
            int page,
            int size,
            String sortField,
            String sortDir
    );

    // ==================== PHẦN GIÁO VIÊN KIỂM TRA TRÊN LỚP ====================

    /** Lấy danh sách theo chuyên ngành */
    List<Student> findByMajor(String major);

    /** Lấy Top 3 sinh viên có GPA cao nhất */
    List<Student> findTop3ByGpa();

    /** Lấy danh sách thống kê tổng hợp theo chuyên ngành */
    List<MajorStatisticDto> getMajorStatistics();

    /** Thao tác Edit đặc thù: Cộng điểm cho sinh viên theo chuyên ngành */
    int addBonusGpaForMajor(String major, Double bonus);

    /** Thao tác Delete đặc thù 1: Xóa toàn bộ sinh viên của 1 chuyên ngành */
    long deleteByMajor(String major);

    /** Thao tác Delete đặc thù 2: Xóa toàn bộ sinh viên có GPA dưới mức chỉ định */
    int deleteStudentsWithGpaLessThan(Double minGpa);
}
