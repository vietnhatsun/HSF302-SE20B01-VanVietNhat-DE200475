package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.StudentSummary;
import com.hsf302.ch4.pojo.Gender;
import com.hsf302.ch4.pojo.Student;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    long count();                                                       // TODO 6
    Optional<Student> findById(Long id);                                // TODO 6
    List<Student> findAllOrderByGpaDesc();                              // TODO 7a
    Page<Student> findPage(int pageIndex, int size, String sortField);  // TODO 7b
    Optional<Student> findByStudentCode(String studentCode);            // TODO 8a
    boolean isEmailExisted(String email);                               // TODO 8b
    long countActive();                                                 // TODO 8c
    List<Student> searchByName(String keyword);                         // TODO 9a
    List<Student> findByEmailDomain(String domain);                     // TODO 9b
    List<Student> findWithoutEmail();                                   // TODO 9c
    List<Student> findByGpaRange(double min, double max);               // TODO 10a
    List<Student> findActiveByGender(Gender gender);                    // TODO 10b
    List<Student> findBornAfter(LocalDate date);                        // TODO 10c
    List<Student> findByDepartment(String deptCode);                    // TODO 11a
    long countByDepartment(String deptCode);                            // TODO 11b (dùng lại ở TODO 22)
    List<Student> findTop3ByGpa();                                      // TODO 11c
    List<Student> findGoodStudents(String deptCode, double minGpa);     // TODO 12
    List<Student> searchByKeyword(String keyword);                      // TODO 13
    List<Student> findAboveAverageGpa();                                // TODO 15
    List<Student> findTopNInDepartment(String deptCode, int n);         // TODO 17
    List<StudentSummary> getActiveSummaries();                          // TODO 18
    Page<Student> findActiveByDepartment(String deptCode, int pageIndex, int size);   // TODO 19
}


