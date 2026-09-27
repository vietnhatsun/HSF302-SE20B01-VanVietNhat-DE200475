package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.DepartmentStatDTO;
import com.hsf302.ch4.pojo.Department;

import java.util.List;

public interface DepartmentService {
    long count();                                       // TODO 6
    boolean existsById(Long id);                        // TODO 6
    List<Department> findDepartmentsWithoutStudents();  // TODO 11d
    List<DepartmentStatDTO> getStatistics();            // TODO 14 (dùng lại ở TODO 23)
}

