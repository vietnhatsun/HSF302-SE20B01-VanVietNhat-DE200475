package com.hsf302.ch4.repository;

import com.hsf302.ch4.dto.CourseEnrollmentCount;
import com.hsf302.ch4.dto.CourseStatDTO;
import com.hsf302.ch4.pojo.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    List<Course> findBySemesterOrderByCodeAsc(String semester);
    long countBySemester(String semester);

    // ===== TODO 10 =====
    List<Course> findByStudents_StudentCodeOrderByCodeAsc(String studentCode);
    List<Course> findByStudents_Department_CodeOrderByCodeAsc(String deptCode);          // có thể TRÙNG
    List<Course> findDistinctByStudents_Department_CodeOrderByCodeAsc(String deptCode);  // loại trùng

    // ===== TODO 11 =====
    List<Course> findByStudentsIsEmpty();

    // ===== TODO 13 =====
    @Query("SELECT new com.hsf302.ch4.dto.CourseStatDTO(c.code, c.name, c.capacity, COUNT(s), AVG(s.gpa)) " +
           "FROM Course c LEFT JOIN c.students s " +
           "GROUP BY c.code, c.name, c.capacity ORDER BY c.code")
    List<CourseStatDTO> getCourseStats();

    // ===== TODO 15 =====
    @Query("SELECT c FROM Course c WHERE SIZE(c.students) >= c.capacity ORDER BY c.code")
    List<Course> findFullCourses();

    // ===== TODO 16 =====
    @EntityGraph(attributePaths = "students")
    Optional<Course> findWithStudentsByCode(String code);

    // ===== TODO 17 =====
    @Query(value = "SELECT TOP (:n) c.code AS code, c.name AS name, COUNT(sc.student_id) AS enrolled " +
                   "FROM courses c LEFT JOIN student_courses sc ON sc.course_id = c.id " +
                   "GROUP BY c.code, c.name " +
                   "ORDER BY enrolled DESC, c.code",
           nativeQuery = true)
    List<CourseEnrollmentCount> findTopEnrolledNative(@Param("n") int n);
    List<Course> findByNameContainingIgnoreCase(String keyword);
    List<Course> findByNameContainingIgnoreCaseOrderByNameDesc(String keyword);

    // ===== 3 CÂU HỎI KIỂM TRA =====
    // Câu 1: Khóa học có credits trong khoảng min–max (Custom Query & Derived Method)
    @Query("SELECT c FROM Course c WHERE c.credits BETWEEN :min AND :max ORDER BY c.credits ASC, c.code ASC")
    List<Course> findByCreditsInRange(@Param("min") int min, @Param("max") int max);
    List<Course> findByCreditsBetweenOrderByCreditsAscCodeAsc(int min, int max);

    // Câu 2: Đếm số khóa có credits > 3 (Custom Query & Derived Method)
    @Query("SELECT COUNT(c) FROM Course c WHERE c.credits > :val")
    long countCoursesWithCreditsGreaterThan(@Param("val") int val);
    long countByCreditsGreaterThan(int val);

    // Câu 3: Tìm khóa có tên chứa từ khóa (không phân biệt hoa/thường)
    @Query("SELECT c FROM Course c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%')) ORDER BY c.code ASC")
    List<Course> searchByNameContaining(@Param("kw") String keyword);
}




