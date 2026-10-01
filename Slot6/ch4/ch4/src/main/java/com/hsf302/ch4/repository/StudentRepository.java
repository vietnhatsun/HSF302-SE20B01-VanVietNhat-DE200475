package com.hsf302.ch4.repository;

import com.hsf302.ch4.dto.StudentSummary;
import com.hsf302.ch4.pojo.Department;
import com.hsf302.ch4.pojo.Gender;
import com.hsf302.ch4.pojo.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>,
                                           JpaSpecificationExecutor<Student> {

    // ===== TODO 8 =====
    Optional<Student> findByStudentCode(String studentCode);   // WHERE student_code = ?
    boolean existsByEmail(String email);                        // kiểm tra tồn tại
    long countByActiveTrue();                                   // WHERE active = 1 (không cần tham số)

    // ===== TODO 9 =====
    List<Student> findByFullNameContainingIgnoreCase(String keyword);   // UPPER(full_name) LIKE UPPER('%kw%')
    List<Student> findByEmailEndingWith(String suffix);                 // email LIKE '%suffix'
    List<Student> findByEmailIsNull();                                  // email IS NULL

    // ===== TODO 10 =====
    List<Student> findByGpaBetweenOrderByGpaDesc(double min, double max);   // gpa BETWEEN ? AND ? ORDER BY gpa DESC
    List<Student> findByGenderAndActiveTrue(Gender gender);                  // gender = ? AND active = 1
    List<Student> findByDobAfter(LocalDate date);                            // dob > ?

    // ===== TODO 11 =====
    List<Student> findByDepartment_CodeOrderByFullNameAsc(String code);   // JOIN departments ... WHERE d.code = ?
    long countByDepartment_Code(String code);
    List<Student> findTop3ByOrderByGpaDesc();                              // SELECT TOP 3 ... ORDER BY gpa DESC

    // ===== TODO 12 =====
    @Query("SELECT s FROM Student s " +
           "WHERE s.department.code = :code AND s.gpa >= :minGpa " +
           "ORDER BY s.gpa DESC")
    List<Student> findGoodStudentsInDepartment(@Param("code") String code,
                                               @Param("minGpa") double minGpa);

    // ===== TODO 13 =====
    @Query("SELECT s FROM Student s " +
           "WHERE LOWER(s.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "   OR LOWER(s.email)    LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "ORDER BY s.fullName")
    List<Student> searchByKeyword(@Param("kw") String keyword);

    // ===== TODO 15 =====
    @Query("SELECT s FROM Student s " +
           "WHERE s.gpa > (SELECT AVG(s2.gpa) FROM Student s2) " +
           "ORDER BY s.gpa DESC")
    List<Student> findAboveAverageGpa();

    // ===== TODO 17 =====
    @Query(value = "SELECT TOP (:n) s.* " +
                   "FROM students s JOIN departments d ON s.department_id = d.id " +
                   "WHERE d.code = :code " +
                   "ORDER BY s.gpa DESC",
           nativeQuery = true)
    List<Student> findTopNByDepartmentNative(@Param("code") String code, @Param("n") int n);

    // ===== TODO 18 =====
    @Query("SELECT s.studentCode AS studentCode, s.fullName AS fullName, " +
           "       s.gpa AS gpa, d.name AS departmentName " +
           "FROM Student s JOIN s.department d " +
           "WHERE s.active = true " +
           "ORDER BY s.fullName")
    List<StudentSummary> findActiveSummaries();

    // ===== TODO 19 =====
    @Query("SELECT s FROM Student s WHERE s.department.code = :code AND s.active = true")
    Page<Student> findActiveByDepartment(@Param("code") String code, Pageable pageable);

    // ===== TODO 21 =====
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Student s SET s.active = false WHERE s.gpa < :threshold AND s.active = true")
    int deactivateLowGpa(@Param("threshold") double threshold);

    // ===== TODO 22 =====
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Student s SET s.department = :to WHERE s.department = :from")
    int transferStudents(@Param("from") Department from, @Param("to") Department to);

    // ===== TODO 23 =====
    long deleteByActiveFalse();

    // ===== Exercise 2 (TODO 9) =====
    List<Student> findByCourses_CodeOrderByFullNameAsc(String courseCode);
    long countByCourses_Code(String courseCode);
    List<Student> findByCourses_CodeAndActiveTrueOrderByFullNameAsc(String courseCode);

    // ===== TODO 11 =====
    List<Student> findByCoursesIsEmptyOrderByFullNameAsc();
    boolean existsByStudentCodeAndCourses_Code(String studentCode, String courseCode);

    // ===== TODO 12 (Ex2) =====
    @Query("SELECT s FROM Student s JOIN s.courses c " +
           "WHERE c.code = :code AND s.gpa >= :minGpa ORDER BY s.gpa DESC")
    List<Student> findGoodStudentsInCourse(@Param("code") String courseCode,
                                           @Param("minGpa") double minGpa);

    // ===== TODO 14 =====
    @Query("SELECT new com.hsf302.ch4.dto.StudentCreditDTO(s.studentCode, s.fullName, COUNT(c), SUM(c.credits)) " +
           "FROM Student s JOIN s.courses c " +
           "GROUP BY s.studentCode, s.fullName " +
           "HAVING SUM(c.credits) >= :minCredits " +
           "ORDER BY SUM(c.credits) DESC, s.fullName")
    List<com.hsf302.ch4.dto.StudentCreditDTO> getCreditSummary(@Param("minCredits") long minCredits);

    // ===== TODO 15 (Ex2) =====
    @Query("SELECT s FROM Student s WHERE SIZE(s.courses) > :n ORDER BY s.fullName")
    List<Student> findStudentsWithMoreThanNCourses(@Param("n") int n);

    // ===== TODO 16 =====
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.studentCode = :code")
    Optional<Student> findByStudentCodeWithCourses(@Param("code") String studentCode);
}




