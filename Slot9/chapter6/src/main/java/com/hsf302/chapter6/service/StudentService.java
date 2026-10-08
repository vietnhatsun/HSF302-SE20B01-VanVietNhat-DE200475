package com.hsf302.chapter6.service;

import com.hsf302.chapter6.entity.Student;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface StudentService {

    List<Student> findAll();

    Optional<Student> findById(Long id);

    Student create(Student student);

    /** @return true nếu tìm thấy và cập nhật; false nếu không tồn tại id */
    boolean update(Long id, Student data);

    /** @return true nếu xoá được; false nếu không tồn tại id */
    boolean delete(Long id);

    /** Kiểm tra email trùng. excludeId = null khi thêm mới, = id hiện tại khi cập nhật */
    boolean isEmailTaken(String email, Long excludeId);

    /** Tìm kiếm sinh viên theo tên hoặc email */
    List<Student> search(String keyword);

    /** Phân trang danh sách sinh viên kết hợp tìm kiếm */
    Page<Student> findPaginated(String keyword, int page, int size);

    List<String> getMajors();
}
