package com.hsf302.ch4.repository;

import com.hsf302.ch4.dto.CourseStatDTO;
import com.hsf302.ch4.pojo.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}


