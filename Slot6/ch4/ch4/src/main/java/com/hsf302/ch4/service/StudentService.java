package com.hsf302.ch4.service;

import com.hsf302.ch4.pojo.Student;
import org.springframework.data.domain.Page;

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
}


