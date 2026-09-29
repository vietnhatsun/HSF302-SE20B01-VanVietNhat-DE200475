package com.hsf302.ch4.specification;

import com.hsf302.ch4.pojo.Student;
import org.springframework.data.jpa.domain.Specification;

public final class StudentSpecs {

    private StudentSpecs() { }

    public static Specification<Student> nameContains(String kw) {
        return (root, query, cb) -> (kw == null || kw.isBlank())
                ? null                                                   // null = bỏ qua điều kiện
                : cb.like(cb.lower(root.get("fullName")), "%" + kw.toLowerCase() + "%");
    }

    public static Specification<Student> inDepartment(String code) {
        return (root, query, cb) -> code == null
                ? null
                : cb.equal(root.join("department").get("code"), code);
    }

    public static Specification<Student> gpaAtLeast(Double min) {
        return (root, query, cb) -> min == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("gpa"), min);
    }

    public static Specification<Student> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? null
                : cb.equal(root.get("active"), active);
    }
}
