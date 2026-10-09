package com.hsf302.chapter6.repository;

import com.hsf302.chapter6.dto.MajorStatisticDto;
import com.hsf302.chapter6.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // ==================== CƠ BẢN & VALIDATION ====================

    /** Email đã tồn tại? (dùng khi thêm mới) */
    boolean existsByEmailIgnoreCase(String email);

    /** Email đã được sinh viên KHÁC dùng? (dùng khi cập nhật) */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // ==================== BÀI NÂNG CAO 1 & 2: TÌM KIẾM & PHÂN TRANG ====================

    /** Tìm kiếm theo tên HOẶC email (không phân biệt hoa/thường) */
    List<Student> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Sort sort);

    /** Tìm kiếm theo tên HOẶC email có phân trang */
    Page<Student> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    // ==================== BÀI NÂNG CAO 4: LỌC NÂNG CAO ĐA ĐIỀU KIỆN ====================

    /**
     * Tìm kiếm và lọc đa tiêu chí: keyword (tên/email), chuyên ngành, khoảng GPA.
     * Sử dụng JPQL động kết hợp phân trang và sắp xếp tự động qua Pageable.
     */
    @Query("SELECT s FROM Student s WHERE " +
            "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:major IS NULL OR :major = '' OR s.major = :major) AND " +
            "(:minGpa IS NULL OR s.gpa >= :minGpa) AND " +
            "(:maxGpa IS NULL OR s.gpa <= :maxGpa)")
    Page<Student> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("major") String major,
            @Param("minGpa") Double minGpa,
            @Param("maxGpa") Double maxGpa,
            Pageable pageable);

    // ==================== PHẦN GIÁO VIÊN KIỂM TRA TRÊN LỚP ====================

    // 1. CÁC PHƯƠNG THỨC TRUY VẤN (QUERY METHODS)
    /** 1.1 Tìm sinh viên theo chuyên ngành (Derived Query) */
    List<Student> findByMajor(String major);

    /** 1.2 Tìm sinh viên có GPA lớn hơn hoặc bằng mức chỉ định (Derived Query) */
    List<Student> findByGpaGreaterThanEqual(Double minGpa);

    /** 1.3 Tìm sinh viên theo chuyên ngành VÀ GPA >= X (Derived Query) */
    List<Student> findByMajorAndGpaGreaterThanEqual(String major, Double minGpa);

    /** 1.4 Lấy Top 3 sinh viên có điểm GPA cao nhất (Derived Query) */
    List<Student> findTop3ByOrderByGpaDesc();

    /** 1.5 Tìm sinh viên trong độ tuổi từ minAge đến maxAge (Derived Query) */
    List<Student> findByAgeBetween(Integer minAge, Integer maxAge);

    /** 1.6 Thống kê theo chuyên ngành: Tổng số SV, GPA trung bình, lớn nhất, nhỏ nhất (JPQL GROUP BY + Constructor Expression DTO) */
    @Query("SELECT new com.hsf302.chapter6.dto.MajorStatisticDto(s.major, COUNT(s), AVG(s.gpa), MAX(s.gpa), MIN(s.gpa)) " +
            "FROM Student s GROUP BY s.major")
    List<MajorStatisticDto> getMajorStatistics();

    /** 1.7 Truy vấn bằng Native SQL (Giáo viên thường hỏi cách viết câu SQL thuần) */
    @Query(value = "SELECT * FROM students WHERE gpa >= :minGpa ORDER BY age DESC", nativeQuery = true)
    List<Student> findStudentsWithHighGpaNative(@Param("minGpa") Double minGpa);

    // 2. THAO TÁC CẬP NHẬT ĐẶC THÙ (BULK UPDATE)
    /** 2.1 Cộng điểm khuyến khích cho toàn bộ sinh viên thuộc chuyên ngành (không vượt quá 4.0) */
    @Modifying
    @Query("UPDATE Student s SET s.gpa = CASE WHEN (s.gpa + :bonus) > 4.0 THEN 4.0 ELSE (s.gpa + :bonus) END WHERE s.major = :major")
    int addBonusGpaForMajor(@Param("major") String major, @Param("bonus") Double bonus);

    // 3. THAO TÁC XÓA ĐẶC THÙ (BULK DELETE)
    /** 3.1 Xóa sinh viên theo chuyên ngành (Derived Query Delete) */
    long deleteByMajor(String major);

    /** 3.2 Xóa tất cả sinh viên có điểm GPA yếu (< minGpa) bằng JPQL */
    @Modifying
    @Query("DELETE FROM Student s WHERE s.gpa < :minGpa")
    int deleteStudentsWithGpaLessThan(@Param("minGpa") Double minGpa);
}
