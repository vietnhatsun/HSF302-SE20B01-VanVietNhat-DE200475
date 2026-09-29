# Chapter 4 — Exercise 1: Hướng dẫn Step-by-Step

> Đi kèm đề bài `Chapter4_Exercise1.md`. Mỗi TODO gồm: **Mục tiêu → Các bước → Code → Giải thích → Kết quả mong đợi → Lỗi thường gặp**.
> Nên tự làm trước.

---

## Mục lục

- [Bước 0 — Chuẩn bị](#bước-0--chuẩn-bị) · [Quy trình commit sau mỗi TODO](#quy-trình-commit-sau-mỗi-todo)
- **Part A:** [TODO 1](#todo-1--tạo-project--cấu-hình) · [TODO 2](#todo-2--enum-gender--entity-department) · [TODO 3](#todo-3--entity-student) · [TODO 4](#todo-4--repository--khung-service) · [TODO 5](#todo-5--datainitializer--exerciserunner)
- **Part B:** [TODO 6](#todo-6--count-findbyid-existsbyid) · [TODO 7](#todo-7--findallsort--findallpageable)
- **Part C:** [TODO 8](#todo-8--findby-existsby-countby) · [TODO 9](#todo-9--containingignorecase-endingwith-isnull) · [TODO 10](#todo-10--between-and-true-after) · [TODO 11](#todo-11--nested-property-top-isempty)
- **Part D:** [TODO 12](#todo-12--jpql--named-parameter) · [TODO 13](#todo-13--jpql-like) · [TODO 14](#todo-14--thống-kê-left-join--group-by--dto) · [TODO 15](#todo-15--subquery) · [TODO 16](#todo-16--lazyinitializationexception--join-fetch) · [TODO 17](#todo-17--native-query) · [TODO 18](#todo-18--interface-projection) · [TODO 19](#todo-19--query--pageable)
- **Bonus:** [TODO 24](#todo-24-bonus--specification)
- **Part E:** [TODO 20](#todo-20--update-bằng-dirty-checking) · [TODO 21](#todo-21--modifying-update) · [TODO 22](#todo-22--chuyển-khoa--xoá-khoa-trong-1-transaction) · [TODO 23](#todo-23--derived-delete)
- [Tổng hợp code hoàn chỉnh](#tổng-hợp-code-hoàn-chỉnh) (Repository, Service interface & implementation) · [Bảng lỗi thường gặp](#bảng-lỗi-thường-gặp)

---

## Bước 0 — Chuẩn bị

1. Bật SQL Server, mở SSMS/Azure Data Studio, tạo database:
   ```sql
   CREATE DATABASE HSF302_CH4;
   ```
2. Kiểm tra đăng nhập `sa` (hoặc tài khoản SQL) và cổng 1433 đang mở (SQL Server Configuration Manager → TCP/IP = Enabled).
3. IDE: IntelliJ IDEA / STS, **đã cài plugin Lombok** và bật *Annotation Processing*.
4. Kiểm tra Git đã cài và cấu hình tên, email (chỉ cần làm một lần trên máy):
   ```bash
   git --version
   git config --global user.name "Nguyen Van A"
   git config --global user.email "anvse123456@fpt.edu.vn"
   ```

---

## Quy trình commit sau mỗi TODO

Làm xong **mỗi** TODO, thực hiện đủ 4 bước:

1. **Chạy app**, đối chiếu console với phần *Kết quả mong đợi* của TODO đó.
2. **Xem thay đổi** sẽ được commit:
   ```bash
   git status
   git diff
   ```
3. **Stage và commit** theo chuẩn Conventional Commits:
   ```bash
   git add .
   git commit -m "<type>(<scope>): <subject>" -m "Refs: TODO <số>"
   ```
4. **Kiểm tra** commit vừa tạo:
   ```bash
   git log --oneline -3
   ```

**Chuẩn message nhắc nhanh:** `<type>(<scope>): <subject>`
- `type`: `feat` (thêm chức năng) · `fix` (sửa lỗi) · `chore` (cấu hình/setup) · `refactor` · `docs` · `test`
- `subject`: tiếng Anh, thể mệnh lệnh (`add`, `find`, `update`…), viết thường, không dấu chấm cuối, ≤ 72 ký tự
- Footer: `Refs: TODO <số>`

**Sửa commit vừa tạo** (khi chưa push):
```bash
git commit --amend -m "feat(derived): search by name, email suffix and null email" -m "Refs: TODO 9"   # sửa message
git add . && git commit --amend --no-edit                                                            # quên thêm file
```
> ⚠️ Không `--amend` commit đã push lên remote dùng chung.

Mỗi TODO bên dưới đều có một mục **📌 Commit** chứa sẵn lệnh mẫu.

---

# Part A — Thiết lập dự án & mapping

## TODO 1 — Tạo project & cấu hình

**Mục tiêu:** Có một project Spring Boot chạy được, kết nối SQL Server.

**Các bước**

1. Vào https://start.spring.io (hoặc *New Project → Spring Initializr* trong IDE):
   - Project: **Maven** · Language: **Java** · Spring Boot: **3.2.x**
   - Group: `com.hsf302` · Artifact: `ch4` · Package name: `com.hsf302.ch4` · Java: **17**
   - Dependencies: **Spring Data JPA**, **MS SQL Server Driver**, **Lombok**
   - *Không* chọn Spring Web — bài này là console app.
2. Giải nén, mở bằng IDE, đợi Maven tải dependency.
3. Kiểm tra `pom.xml` có:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>
   <dependency>
       <groupId>com.microsoft.sqlserver</groupId>
       <artifactId>mssql-jdbc</artifactId>
       <scope>runtime</scope>
   </dependency>
   <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok</artifactId>
       <optional>true</optional>
   </dependency>
   ```
4. Đổi tên class main thành `Chapter4Application` (nếu cần) và tạo đủ các package: `pojo`, `dto`, `repository`, `specification`, `service`, `runner`.
5. Viết `src/main/resources/application.properties`:
   ```properties
   spring.main.banner-mode=off

   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=HSF302_CH4;encrypt=true;trustServerCertificate=true
   spring.datasource.username=sa
   spring.datasource.password=12345

   spring.jpa.hibernate.ddl-auto=create
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```

**Giải thích**

| Property | Ý nghĩa |
|---|---|
| `trustServerCertificate=true` | Driver mssql-jdbc 10+ bật mã hoá mặc định; SQL Server local không có chứng chỉ hợp lệ → cần tin tưởng chứng chỉ (hoặc `encrypt=false`). |
| `ddl-auto=create` | Mỗi lần chạy: DROP rồi CREATE lại bảng → dữ liệu luôn là dữ liệu seed, kết quả mỗi lần chạy giống nhau. |
| `show-sql` + `format_sql` | In câu SQL Hibernate sinh ra → quan sát được derived query / JOIN FETCH / N+1. |
| Không cần `hibernate.dialect` | Hibernate 6 tự nhận dialect từ kết nối. |

**Kiểm tra:** Chạy `Chapter4Application` → app khởi động rồi tắt (vì chưa có runner), **không** có lỗi kết nối.

**Lỗi thường gặp**
- `Login failed for user 'sa'` → sai user/password hoặc SQL Server chưa bật *SQL Server Authentication*.
- `The TCP/IP connection to the host localhost, port 1433 has failed` → chưa bật TCP/IP hoặc SQL Server service chưa chạy.
- `Cannot open database "HSF302_CH4"` → chưa tạo database (Bước 0).

**Khởi tạo Git** (thư mục gốc project, cùng cấp `pom.xml`):

```bash
git init
```

Kiểm tra / bổ sung `.gitignore` (Spring Initializr đã tạo sẵn phần lớn):
```gitignore
target/
.idea/
*.iml
.vscode/
.DS_Store
application-local.properties
```

> 💡 Không muốn đưa mật khẩu thật lên Git: tách `spring.datasource.password` sang `application-local.properties` (đã ignore) và thêm `spring.profiles.active=local` vào `application.properties`.

**📌 Commit TODO 1**

```bash
git add .
git commit -m "chore(setup): init spring boot project and configure sql server" \
           -m "Thêm .gitignore, cấu hình datasource, ddl-auto=create, show-sql" \
           -m "Refs: TODO 1"
```

Commit message đầy đủ:
```
chore(setup): init spring boot project and configure sql server

Thêm .gitignore, cấu hình datasource, ddl-auto=create, show-sql

Refs: TODO 1
```

---

## TODO 2 — Enum `Gender` & entity `Department`

**Mục tiêu:** Tạo phía **"One"** (inverse side) của quan hệ.

**Các bước**

1. Tạo `pojo/Gender.java`.
2. Tạo `pojo/Department.java`: `@Entity`, `@Table(name = "departments")`, các field, `@OneToMany(mappedBy = "department")`.
3. Viết helper `addStudent()` và `toString()` không chứa `students`.

**Code**

```java
package com.hsf302.ch4.pojo;

public enum Gender {
    MALE, FEMALE
}
```

```java
package com.hsf302.ch4.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    // Inverse side: "department" là TÊN FIELD bên Student
    @OneToMany(mappedBy = "department")
    private List<Student> students = new ArrayList<>();

    public Department(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // Helper đồng bộ 2 chiều
    public void addStudent(Student s) {
        students.add(s);
        s.setDepartment(this);
    }

    @Override
    public String toString() {
        return code + " - " + name;       // KHÔNG in students (lazy + vòng lặp)
    }
}
```

**Giải thích**
- `mappedBy = "department"` nói với JPA: *FK không nằm ở bảng `departments`, hãy xem field `department` trong `Student`*. Phía `mappedBy` **không** tạo cột, chỉ để điều hướng trong Java.
- `@OneToMany` mặc định **LAZY** → `students` chỉ được load khi truy cập trong transaction (sẽ gặp ở TODO 16).
- **Không cascade REMOVE**: xoá department không được tự xoá sinh viên (TODO 22 sẽ chuyển sinh viên trước khi xoá).
- Khởi tạo `new ArrayList<>()` để `addStudent()` không bị `NullPointerException`.
- Không dùng `@Data`: `@Data` sinh `toString/hashCode` duyệt `students` → `Student.toString` lại duyệt `department`… ⇒ `StackOverflowError` hoặc `LazyInitializationException`.

**📌 Commit TODO 2**

```bash
git add .
git commit -m "feat(entity): add Gender enum and Department entity" \
           -m "Department là inverse side với @OneToMany(mappedBy = "department")" \
           -m "Refs: TODO 2"
```

Commit message đầy đủ:
```
feat(entity): add Gender enum and Department entity

Department là inverse side với @OneToMany(mappedBy = "department")

Refs: TODO 2
```

---

## TODO 3 — Entity `Student`

**Mục tiêu:** Tạo phía **"Many"** (owning side) — nắm giữ khoá ngoại.

**Code**

```java
package com.hsf302.ch4.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_code", nullable = false, unique = true, length = 10)
    private String studentCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, length = 100)
    private String email;                         // cho phép null

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private LocalDate dob;

    private Double gpa;

    private boolean active;

    // Owning side: bảng students có cột department_id (FK → departments.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Override
    public String toString() {
        return String.format("%s | %-15s | %-20s | %.1f | %s",
                studentCode, fullName, email, gpa, active ? "active" : "inactive");
        // KHÔNG in department → tránh LazyInitializationException
    }
}
```

**Giải thích**
- `@ManyToOne` mặc định là **EAGER**; ta đổi sang **LAZY** để mỗi lần load Student không kéo theo Department (tránh query thừa). Hệ quả: truy cập `student.getDepartment().getName()` **ngoài transaction** sẽ lỗi → vì vậy `toString()` không đụng tới `department`.
- `@JoinColumn(name = "department_id")` đặt tên cột FK. Nếu bỏ, Hibernate đặt tên `department_id` theo mặc định (field + `_` + id).
- `@Enumerated(EnumType.STRING)` lưu `"MALE"`/`"FEMALE"` — nếu để mặc định `ORDINAL`, thêm/đổi thứ tự enum sẽ làm sai dữ liệu cũ.
- `@Column(name = "full_name")`: tên cột trong DB. Nhưng trong **derived query & JPQL** vẫn dùng tên field **`fullName`**; chỉ **native SQL** mới dùng `full_name`.
- `email` unique nhưng cho phép null: SQL Server chỉ cho **một** giá trị NULL trong cột unique thường — dữ liệu mẫu chỉ có 1 NULL nên không sao.

**Kiểm tra:** Chạy app → console có:
```sql
create table departments (id bigint identity not null, code varchar(10) not null, ...)
create table students (..., department_id bigint not null, ...)
alter table students add constraint FK... foreign key (department_id) references departments
```

**📌 Commit TODO 3**

```bash
git add .
git commit -m "feat(entity): add Student entity with many-to-one department" \
           -m "Student là owning side, FK department_id, fetch LAZY" \
           -m "Refs: TODO 3"
```

Commit message đầy đủ:
```
feat(entity): add Student entity with many-to-one department

Student là owning side, FK department_id, fetch LAZY

Refs: TODO 3
```

---

## TODO 4 — Repository & khung Service

### 4.1 Tạo Repository

**Code**

```java
package com.hsf302.ch4.repository;

import com.hsf302.ch4.pojo.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // sẽ bổ sung dần ở các TODO sau
}
```

```java
package com.hsf302.ch4.repository;

import com.hsf302.ch4.pojo.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentRepository extends JpaRepository<Student, Long>,
                                           JpaSpecificationExecutor<Student> {
    // sẽ bổ sung dần ở các TODO sau
}
```

**Giải thích**
- `JpaRepository<Student, Long>`: `Student` là entity, `Long` là kiểu của field `@Id` (phải khớp).
- Không cần viết class implementation — Spring tạo proxy lúc startup.
- Không cần `@Repository` — Spring Boot tự quét interface extend `Repository` trong package con của class `@SpringBootApplication`.
- `JpaSpecificationExecutor` dùng cho TODO 24 (bonus).

### 4.2 Tạo tầng Service (interface + implementation)

**Mục tiêu:** Tổ chức code theo kiến trúc N-layer. `ExerciseRunner` (sau này là Controller ở Chapter 5) **không gọi repository trực tiếp** mà gọi qua Service.

```
ExerciseRunner ──► DepartmentService / StudentService      (interface)
                          ▲ implements
                   DepartmentServiceImpl / StudentServiceImpl   (@Service, @Transactional)
                          │ gọi
                   DepartmentRepository / StudentRepository     (Spring Data JPA)
                          │
                         Entity ──► SQL Server
```

**Các bước**
1. Tạo 2 interface `service/DepartmentService.java`, `service/StudentService.java` (tạm thời rỗng, method được thêm dần từ TODO 6).
2. Tạo 2 class `service/DepartmentServiceImpl.java`, `service/StudentServiceImpl.java`: gắn `@Service`, `@Transactional(readOnly = true)` ở mức class, inject repository qua constructor.

**Code — interface**

```java
package com.hsf302.ch4.service;

public interface StudentService {
    // Các method được bổ sung dần từ TODO 6
}
```

```java
package com.hsf302.ch4.service;

public interface DepartmentService {
    // Các method được bổ sung dần từ TODO 6
}
```

**Code — implementation**

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)          // mặc định: mọi method chỉ ĐỌC
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    // Các method được cài đặt dần từ TODO 6
}
```

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.repository.DepartmentRepository;
import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;      // dùng ở TODO 22 (chuyển sinh viên)

    // Các method được cài đặt dần từ TODO 6
}
```

**Giải thích**
- **Vì sao tách interface và implementation?** Tầng trên (Runner/Controller) chỉ phụ thuộc vào **interface** (nguyên lý Dependency Inversion) → đổi cách cài đặt không ảnh hưởng nơi gọi, dễ mock khi viết unit test. Spring cũng tạo **proxy** quanh bean để xử lý `@Transactional`.
- **Service làm gì?** Kiểm tra dữ liệu đầu vào, xử lý nghiệp vụ, quản lý **transaction**, ghép nhiều repository trong một thao tác (TODO 22). Repository chỉ lo truy vấn.
- **`@Transactional(readOnly = true)` ở mức class:** mọi method mặc định là giao dịch chỉ đọc → Hibernate bỏ qua dirty checking/flush, nhẹ hơn. Method **ghi dữ liệu** (Part E) phải gắn thêm `@Transactional` (không `readOnly`) để ghi đè.
- Import đúng `org.springframework.transaction.annotation.Transactional` (không phải `jakarta.transaction.Transactional`, vì bản đó không có thuộc tính `readOnly`).
- `@RequiredArgsConstructor` + field `final` = **constructor injection** — cách inject được khuyến nghị (không cần `@Autowired`).
- `@Service` bắt buộc có trên **class implementation** (không đặt trên interface), nếu thiếu: `No qualifying bean of type 'StudentService'`.

> 📌 **Quy trình cho mỗi TODO từ TODO 6 trở đi:**
> ① Khai báo method trong **Repository** (nếu cần) → ② Khai báo method trong **Service interface** → ③ Cài đặt trong **ServiceImpl** → ④ Gọi từ **ExerciseRunner** → ⑤ Chạy, đối chiếu kết quả → ⑥ Commit.


**📌 Commit TODO 4**

```bash
git add .
git commit -m "feat(repository): add repositories and service layer skeleton" \
           -m "Thêm interface DepartmentService, StudentService và lớp implementation" \
           -m "Refs: TODO 4"
```

Commit message đầy đủ:
```
feat(repository): add repositories and service layer skeleton

Thêm interface DepartmentService, StudentService và lớp implementation

Refs: TODO 4
```

---

## TODO 5 — `DataInitializer` & `ExerciseRunner`

**Mục tiêu:** Seed dữ liệu, và có "bộ khung" để chạy từng TODO theo thứ tự.

**Các bước**
1. Tạo `runner/DataInitializer.java` implement `CommandLineRunner`, gắn `@Component` + `@Order(1)`.
2. Tạo 4 department, `saveAll()`.
3. Tạo 10 student bằng helper `st(...)`, mỗi student gắn department qua `addStudent()`, `saveAll()`.
4. Tạo `runner/ExerciseRunner.java` với `@Order(2)`, các hàm in kết quả chung.

**Code — `DataInitializer`**

```java
package com.hsf302.ch4.runner;

import com.hsf302.ch4.pojo.Department;
import com.hsf302.ch4.pojo.Gender;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.DepartmentRepository;
import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.hsf302.ch4.pojo.Gender.FEMALE;
import static com.hsf302.ch4.pojo.Gender.MALE;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    @Override
    public void run(String... args) {
        if (studentRepository.count() > 0) return;          // đề phòng khi đổi ddl-auto=update

        Department se = new Department("SE", "Software Engineering");
        Department ai = new Department("AI", "Artificial Intelligence");
        Department ia = new Department("IA", "Information Assurance");
        Department gd = new Department("GD", "Graphic Design");
        departmentRepository.saveAll(List.of(se, ai, ia, gd));

        studentRepository.saveAll(List.of(
            st("SE001", "Nguyen Van An",  "an.nv@fpt.edu.vn",   MALE,   "2005-03-15", 3.2, true,  se),
            st("SE002", "Tran Thi Binh",  "binh.tt@fpt.edu.vn", FEMALE, "2004-07-22", 3.8, true,  se),
            st("SE003", "Le Van Cuong",   "cuong.lv@fpt.edu.vn",MALE,   "2003-11-05", 2.5, false, se),
            st("AI001", "Pham Thi Dung",  "dung.pt@fpt.edu.vn", FEMALE, "2006-01-10", 3.5, true,  ai),
            st("AI002", "Hoang Van Em",   "em.hv@gmail.com",    MALE,   "2002-09-30", 2.8, true,  ai),
            st("AI003", "Vo Thi Hoa",     "hoa.vt@fpt.edu.vn",  FEMALE, "2005-05-18", 3.9, true,  ai),
            st("IA001", "Dang Van Giang", "giang.dv@gmail.com", MALE,   "2001-12-01", 1.9, false, ia),
            st("IA002", "Bui Thi Lan",    "lan.bt@fpt.edu.vn",  FEMALE, "2004-02-14", 3.1, true,  ia),
            st("SE004", "Nguyen Thi Mai", "mai.nt@fpt.edu.vn",  FEMALE, "2003-08-08", 3.6, true,  se),
            st("IA003", "Do Van Nam",     null,                 MALE,   "2005-10-20", 2.2, true,  ia)
        ));
        System.out.println(">>> Seeded " + departmentRepository.count() + " departments, "
                + studentRepository.count() + " students");
    }

    private Student st(String code, String name, String email, Gender gender,
                       String dob, double gpa, boolean active, Department dept) {
        Student s = new Student();
        s.setStudentCode(code);
        s.setFullName(name);
        s.setEmail(email);
        s.setGender(gender);
        s.setDob(LocalDate.parse(dob));
        s.setGpa(gpa);
        s.setActive(active);
        dept.addStudent(s);           // set cả 2 chiều; quan trọng nhất là s.department (owning side)
        return s;
    }
}
```

**Code — khung `ExerciseRunner`**

```java
package com.hsf302.ch4.runner;

import com.hsf302.ch4.service.DepartmentService;
import com.hsf302.ch4.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Order(2)
@RequiredArgsConstructor
public class ExerciseRunner implements CommandLineRunner {

    // Runner CHỈ phụ thuộc vào Service (interface), KHÔNG inject Repository
    private final DepartmentService departmentService;
    private final StudentService studentService;

    @Override
    public void run(String... args) {
        partB();
        partC();
        partD();
        bonus();      // chạy trên dữ liệu gốc → trước Part E
        partE();
    }

    private void partB() { todo6(); todo7(); }
    private void partC() { todo8(); todo9(); todo10(); todo11(); }
    private void partD() { todo12(); todo13(); todo14(); todo15(); todo16(); todo17(); todo18(); todo19(); }
    private void bonus() { todo24(); }
    private void partE() { todo20(); todo21(); todo22(); todo23(); }

    // ===== helpers =====
    private void title(String t) {
        System.out.println("\n===== " + t + " =====");
    }

    private void printList(String label, Collection<?> list) {
        System.out.println("-- " + label + ":");
        list.forEach(o -> System.out.println("   " + o));
        System.out.println("   -> " + list.size() + " record(s)");
    }

    // todo6() ... todo24() viết ở các TODO bên dưới
}
```

**Giải thích**
- Có 2 `CommandLineRunner` → dùng `@Order` để **seed trước, chạy bài sau**. Số nhỏ chạy trước.
- `saveAll(departments)` trước để department có `id`; sau đó `saveAll(students)` — mỗi student đã trỏ tới department đã được lưu → Hibernate ghi đúng `department_id`.
- Vì `Department.students` **không cascade**, nếu chỉ `departmentRepository.save(se)` thì student **không** được lưu — phải save student riêng.
- Bài này không có web nên app chạy xong runner là **tự tắt** — bình thường.
- `ExerciseRunner` chỉ inject **Service interface** (tạo ở TODO 4.2). Spring tự tìm bean `StudentServiceImpl`/`DepartmentServiceImpl` để inject.
- `DataInitializer` là code khởi tạo dữ liệu (hạ tầng) nên dùng repository trực tiếp là chấp nhận được; toàn bộ **chức năng** của bài (TODO 6 trở đi) phải đi qua Service.

**Kiểm tra:** Console in `>>> Seeded 4 departments, 10 students`; mở SSMS `SELECT * FROM students` thấy 10 dòng, `department_id` đúng.

**📌 Commit TODO 5**

```bash
git add .
git commit -m "feat(data): seed sample departments and students" \
           -m "Thêm DataInitializer @Order(1) và khung ExerciseRunner @Order(2)" \
           -m "Refs: TODO 5"
```

Commit message đầy đủ:
```
feat(data): seed sample departments and students

Thêm DataInitializer @Order(1) và khung ExerciseRunner @Order(2)

Refs: TODO 5
```

---

# Part B — Method có sẵn của `JpaRepository`

## TODO 6 — `count()`, `findById()`, `existsById()`

**Các bước:** Repository không cần khai báo thêm (dùng method có sẵn của `JpaRepository`). Bổ sung method vào 2 Service rồi gọi từ Runner.

**Service — khai báo trong `StudentService`**

```java
long count();                                   // TODO 6
Optional<Student> findById(Long id);            // TODO 6
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public long count() {
    return studentRepository.count();
}

@Override
public Optional<Student> findById(Long id) {
    return studentRepository.findById(id);
}
```

**Service — khai báo trong `DepartmentService`**

```java
long count();                                   // TODO 6
boolean existsById(Long id);                    // TODO 6
```

**Service — cài đặt trong `DepartmentServiceImpl`**

```java
@Override
public long count() {
    return departmentRepository.count();
}

@Override
public boolean existsById(Long id) {
    return departmentRepository.existsById(id);
}
```

**Runner** — chỉ gọi Service

```java
private void todo6() {
    title("TODO 6: count / findById / existsById");
    System.out.println("Departments: " + departmentService.count());
    System.out.println("Students   : " + studentService.count());

    studentService.findById(1L).ifPresentOrElse(
            s -> System.out.println("findById(1)  -> " + s),
            () -> System.out.println("findById(1)  -> Not found"));

    System.out.println("findById(99) -> " + studentService.findById(99L)
            .map(Object::toString)
            .orElse("Not found"));

    System.out.println("existsById(4) department -> " + departmentService.existsById(4L));
}
```

**Giải thích**
- `findById` trả `Optional<T>` → **không** gọi `.get()` trực tiếp (có thể ném `NoSuchElementException`). Dùng `ifPresentOrElse`, `map/orElse`, `orElseThrow`.
- `count()` → `SELECT COUNT(*)`; `existsById()` → câu SELECT nhẹ, không load entity.

**Kết quả mong đợi**
```
===== TODO 6: count / findById / existsById =====
Departments: 4
Students   : 10
findById(1)  -> SE001 | Nguyen Van An   | an.nv@fpt.edu.vn     | 3.2 | active
findById(99) -> Not found
existsById(4) department -> true
```

**📌 Commit TODO 6**

```bash
git add .
git commit -m "feat(builtin): use count, findById and existsById" \
           -m "Refs: TODO 6"
```

Commit message đầy đủ:
```
feat(builtin): use count, findById and existsById

Refs: TODO 6
```

---

## TODO 7 — `findAll(Sort)` & `findAll(Pageable)`

**Service — khai báo trong `StudentService`**

```java
List<Student> findAllOrderByGpaDesc();                              // TODO 7a
Page<Student> findPage(int pageIndex, int size, String sortField);  // TODO 7b
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
// import org.springframework.data.domain.*;
@Override
public List<Student> findAllOrderByGpaDesc() {
    return studentRepository.findAll(Sort.by(Sort.Direction.DESC, "gpa"));
}

@Override
public Page<Student> findPage(int pageIndex, int size, String sortField) {
    if (pageIndex < 0 || size <= 0) {
        throw new IllegalArgumentException("pageIndex phải >= 0 và size phải > 0");
    }
    Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(sortField).ascending());
    return studentRepository.findAll(pageable);
}
```

**Runner** — chỉ gọi Service

```java
private void todo7() {
    title("TODO 7: Sort & Pageable");

    // (a) GPA giảm dần
    printList("All students order by GPA desc", studentService.findAllOrderByGpaDesc());

    // (b) Trang THỨ 2 → index 1 (Spring Data đánh số trang từ 0)
    Page<Student> page = studentService.findPage(1, 3, "fullName");
    printList("Page index " + page.getNumber() + " (size " + page.getSize() + ")", page.getContent());
    System.out.println("totalElements=" + page.getTotalElements()
            + ", totalPages=" + page.getTotalPages()
            + ", hasNext=" + page.hasNext()
            + ", hasPrevious=" + page.hasPrevious());
}
```

> 💡 Service nhận tham số đơn giản (số trang, kích thước, tên field) và **tự tạo `Pageable`** → tầng gọi (Runner, sau này là Controller) không phụ thuộc chi tiết Spring Data; kiểm tra tham số không hợp lệ cũng đặt ở Service.

**Giải thích**
- `Sort.by("gpa")`: chuỗi là **tên field entity**, không phải tên cột (`"full_name"` sẽ lỗi, phải là `"fullName"`).
- `PageRequest.of(page, size, sort)`: **page bắt đầu từ 0** → "trang thứ 2" là `1`.
- `Page` tốn thêm 1 câu `SELECT COUNT(...)` để tính `totalElements/totalPages` — quan sát trong console.
- SQL Server phân trang bằng `OFFSET ? ROWS FETCH NEXT ? ROWS ONLY`.

**Kết quả mong đợi**
```
-- All students order by GPA desc:
   AI003 | Vo Thi Hoa ... 3.9
   SE002 | Tran Thi Binh ... 3.8
   SE004 | Nguyen Thi Mai ... 3.6
   AI001 | Pham Thi Dung ... 3.5
   SE001 | Nguyen Van An ... 3.2
   IA002 | Bui Thi Lan ... 3.1
   AI002 | Hoang Van Em ... 2.8
   SE003 | Le Van Cuong ... 2.5
   IA003 | Do Van Nam ... 2.2
   IA001 | Dang Van Giang ... 1.9
-- Page index 1 (size 3):
   AI002 | Hoang Van Em ...
   SE003 | Le Van Cuong ...
   SE004 | Nguyen Thi Mai ...
totalElements=10, totalPages=4, hasNext=true, hasPrevious=true
```

**📌 Commit TODO 7**

```bash
git add .
git commit -m "feat(builtin): sort and paginate students with findAll" \
           -m "Refs: TODO 7"
```

Commit message đầy đủ:
```
feat(builtin): sort and paginate students with findAll

Refs: TODO 7
```

---

# Part C — Derived query (keyword)

> Nguyên tắc: `find/count/exists/delete` + `By` + `<TênField><Keyword>` + (`And`/`Or` …) + (`OrderBy<Field><Asc|Desc>`). Tên field viết hoa chữ cái đầu. Sai tên field → app **lỗi ngay khi khởi động** (`PropertyReferenceException`).

## TODO 8 — `findBy`, `existsBy`, `countBy`

**Repository — `StudentRepository`**

```java
Optional<Student> findByStudentCode(String studentCode);   // WHERE student_code = ?
boolean existsByEmail(String email);                        // kiểm tra tồn tại
long countByActiveTrue();                                   // WHERE active = 1 (không cần tham số)
```

**Service — khai báo trong `StudentService`**

```java
Optional<Student> findByStudentCode(String studentCode);   // TODO 8a
boolean isEmailExisted(String email);                      // TODO 8b
long countActive();                                        // TODO 8c
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public Optional<Student> findByStudentCode(String studentCode) {
    return studentRepository.findByStudentCode(studentCode);
}

@Override
public boolean isEmailExisted(String email) {
    return studentRepository.existsByEmail(email);
}

@Override
public long countActive() {
    return studentRepository.countByActiveTrue();
}
```

**Runner** — chỉ gọi Service

```java
private void todo8() {
    title("TODO 8: findBy / existsBy / countBy");
    for (String code : List.of("AI002", "XX999")) {
        System.out.println("findByStudentCode(" + code + ") -> " +
                studentService.findByStudentCode(code).map(Object::toString).orElse("Not found"));
    }
    System.out.println("isEmailExisted(binh.tt@fpt.edu.vn) -> "
            + studentService.isEmailExisted("binh.tt@fpt.edu.vn"));
    System.out.println("countActive -> " + studentService.countActive());
}
```

> 💡 Tên method ở Service đặt theo **nghiệp vụ** (`isEmailExisted`, `countActive`), không cần giống tên derived query ở Repository.

**Giải thích**
- `studentCode` là field **unique** → trả `Optional<Student>`. Nếu trả `Student` mà không có kết quả sẽ nhận `null`; nếu có >1 kết quả sẽ ném `IncorrectResultSizeDataAccessException`.
- `True`/`False` là keyword **không cần tham số**.

**Kết quả mong đợi:** `AI002 -> Hoang Van Em`, `XX999 -> Not found`, `exists -> true`, `countByActiveTrue -> 8`.

**📌 Commit TODO 8**

```bash
git add .
git commit -m "feat(derived): find by student code, check email and count active" \
           -m "Refs: TODO 8"
```

Commit message đầy đủ:
```
feat(derived): find by student code, check email and count active

Refs: TODO 8
```

---

## TODO 9 — `ContainingIgnoreCase`, `EndingWith`, `IsNull`

**Repository**

```java
List<Student> findByFullNameContainingIgnoreCase(String keyword);   // UPPER(full_name) LIKE UPPER('%kw%')
List<Student> findByEmailEndingWith(String suffix);                 // email LIKE '%suffix'
List<Student> findByEmailIsNull();                                  // email IS NULL
```

**Service — khai báo trong `StudentService`**

```java
List<Student> searchByName(String keyword);        // TODO 9a
List<Student> findByEmailDomain(String domain);    // TODO 9b
List<Student> findWithoutEmail();                  // TODO 9c
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public List<Student> searchByName(String keyword) {
    if (keyword == null || keyword.isBlank()) {
        return List.of();                              // từ khoá rỗng → không tìm
    }
    return studentRepository.findByFullNameContainingIgnoreCase(keyword.trim());
}

@Override
public List<Student> findByEmailDomain(String domain) {
    String suffix = domain.startsWith("@") ? domain : "@" + domain;   // "gmail.com" → "@gmail.com"
    return studentRepository.findByEmailEndingWith(suffix);
}

@Override
public List<Student> findWithoutEmail() {
    return studentRepository.findByEmailIsNull();
}
```

**Runner** — chỉ gọi Service

```java
private void todo9() {
    title("TODO 9: Containing / EndingWith / IsNull");
    printList("fullName contains 'nguyen'", studentService.searchByName("nguyen"));
    printList("email domain 'gmail.com'", studentService.findByEmailDomain("gmail.com"));
    printList("email is null", studentService.findWithoutEmail());
}
```

> 💡 Xử lý dữ liệu đầu vào (bỏ khoảng trắng, từ khoá rỗng, tự thêm `@`) là **nghiệp vụ** → đặt ở Service, Repository chỉ truy vấn.

**Giải thích**
- `Containing`/`StartingWith`/`EndingWith`: Spring **tự thêm `%`** → chỉ truyền từ khoá.
- Nếu dùng `findByFullNameLike(...)` thì phải tự truyền `"%nguyen%"` — đây là lỗi hay gặp.
- `IsNull` không có tham số, và thể hiện rõ ý định "tìm bản ghi chưa có email". (Derived `findByEmail(null)` cũng được Spring Data JPA chuyển thành `IS NULL`, nhưng khó đọc. Ngược lại, trong `@Query` thì `WHERE s.email = :email` với `email = null` sẽ **không bao giờ** khớp, vì trong SQL `= NULL` luôn cho kết quả UNKNOWN.)

**Kết quả mong đợi**
- (a) Nguyen Van An, Nguyen Thi Mai → 2
- (b) Hoang Van Em, Dang Van Giang → 2
- (c) Do Van Nam → 1

**📌 Commit TODO 9**

```bash
git add .
git commit -m "feat(derived): search by name, email suffix and null email" \
           -m "Refs: TODO 9"
```

Commit message đầy đủ:
```
feat(derived): search by name, email suffix and null email

Refs: TODO 9
```

---

## TODO 10 — `Between`, `And`, `True`, `After`

**Repository**

```java
List<Student> findByGpaBetweenOrderByGpaDesc(double min, double max);   // gpa BETWEEN ? AND ? ORDER BY gpa DESC
List<Student> findByGenderAndActiveTrue(Gender gender);                  // gender = ? AND active = 1
List<Student> findByDobAfter(LocalDate date);                            // dob > ?
```

**Service — khai báo trong `StudentService`**

```java
List<Student> findByGpaRange(double min, double max);   // TODO 10a
List<Student> findActiveByGender(Gender gender);        // TODO 10b
List<Student> findBornAfter(LocalDate date);            // TODO 10c
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public List<Student> findByGpaRange(double min, double max) {
    if (min > max) {
        throw new IllegalArgumentException("min GPA phải <= max GPA");
    }
    return studentRepository.findByGpaBetweenOrderByGpaDesc(min, max);
}

@Override
public List<Student> findActiveByGender(Gender gender) {
    return studentRepository.findByGenderAndActiveTrue(gender);
}

@Override
public List<Student> findBornAfter(LocalDate date) {
    return studentRepository.findByDobAfter(date);
}
```

**Runner** — chỉ gọi Service

```java
private void todo10() {
    title("TODO 10: Between / And / True / After");
    printList("GPA in [3.0, 3.6] desc", studentService.findByGpaRange(3.0, 3.6));
    printList("MALE & active", studentService.findActiveByGender(Gender.MALE));
    printList("dob after 2005-01-01", studentService.findBornAfter(LocalDate.of(2005, 1, 1)));
}
```

**Giải thích**
- `Between` nhận **2 tham số** và **bao gồm 2 đầu mút** (3.6 của Mai có trong kết quả).
- Thứ tự tham số = thứ tự placeholder trong tên method: `GpaBetween(min, max)`.
- `GenderAndActiveTrue`: chỉ `Gender` cần tham số; `ActiveTrue` không cần.
- `After`/`Before` dùng cho kiểu ngày (`LocalDate`, `LocalDateTime`…), **không bao gồm** mốc (dùng `>`/`<`).

**Kết quả mong đợi**
- (a) Mai 3.6, Dung 3.5, An 3.2, Lan 3.1
- (b) Nguyen Van An, Hoang Van Em, Do Van Nam
- (c) Nguyen Van An, Pham Thi Dung, Vo Thi Hoa, Do Van Nam

**📌 Commit TODO 10**

```bash
git add .
git commit -m "feat(derived): filter students by gpa range, gender and dob" \
           -m "Refs: TODO 10"
```

Commit message đầy đủ:
```
feat(derived): filter students by gpa range, gender and dob

Refs: TODO 10
```

---

## TODO 11 — Nested property, `Top`, `IsEmpty`

**StudentRepository**

```java
List<Student> findByDepartment_CodeOrderByFullNameAsc(String code);   // JOIN departments ... WHERE d.code = ?
long countByDepartment_Code(String code);
List<Student> findTop3ByOrderByGpaDesc();                              // SELECT TOP 3 ... ORDER BY gpa DESC
```

**DepartmentRepository**

```java
Optional<Department> findByCode(String code);        // dùng lại ở TODO 16, 22
List<Department> findByStudentsIsEmpty();            // WHERE NOT EXISTS (SELECT ... FROM students ...)
```

**Service — khai báo trong `StudentService`**

```java
List<Student> findByDepartment(String deptCode);    // TODO 11a
long countByDepartment(String deptCode);            // TODO 11b (dùng lại ở TODO 22)
List<Student> findTop3ByGpa();                      // TODO 11c
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public List<Student> findByDepartment(String deptCode) {
    return studentRepository.findByDepartment_CodeOrderByFullNameAsc(deptCode);
}

@Override
public long countByDepartment(String deptCode) {
    return studentRepository.countByDepartment_Code(deptCode);
}

@Override
public List<Student> findTop3ByGpa() {
    return studentRepository.findTop3ByOrderByGpaDesc();
}
```

**Service — khai báo trong `DepartmentService`**

```java
List<Department> findDepartmentsWithoutStudents();  // TODO 11d
```

**Service — cài đặt trong `DepartmentServiceImpl`**

```java
@Override
public List<Department> findDepartmentsWithoutStudents() {
    return departmentRepository.findByStudentsIsEmpty();
}
```

**Runner** — chỉ gọi Service

```java
private void todo11() {
    title("TODO 11: Nested property / Top / IsEmpty");
    printList("Students of SE (order by name)", studentService.findByDepartment("SE"));
    System.out.println("count students of AI -> " + studentService.countByDepartment("AI"));
    printList("Top 3 GPA", studentService.findTop3ByGpa());
    printList("Departments without students", departmentService.findDepartmentsWithoutStudents());
}
```

**Giải thích**
- **Nested property**: `Department_Code` = đi từ `Student.department` sang `Department.code` → Spring tự sinh JOIN. Dấu `_` giúp tách rõ ràng; `findByDepartmentCode` cũng chạy được nhưng `_` an toàn hơn khi tên field mơ hồ.
- `findTop3ByOrderByGpaDesc`: không có điều kiện nhưng **vẫn phải có `By`** trước `OrderBy`.
- `IsEmpty` áp dụng cho **field collection** (`students`) → Hibernate sinh `NOT EXISTS`/subquery.
- `Department.toString()` không in `students` nên in an toàn.

**Kết quả mong đợi**
- (a) Le Van Cuong, Nguyen Thi Mai, Nguyen Van An, Tran Thi Binh
- (b) 3
- (c) Vo Thi Hoa 3.9, Tran Thi Binh 3.8, Nguyen Thi Mai 3.6
- (d) GD - Graphic Design

**📌 Commit TODO 11**

```bash
git add .
git commit -m "feat(derived): query by department code, top gpa and empty dept" \
           -m "Refs: TODO 11"
```

Commit message đầy đủ:
```
feat(derived): query by department code, top gpa and empty dept

Refs: TODO 11
```

---

# Part D — Custom query với `@Query`

> Nhắc lại: **JPQL** dùng **tên entity & field** (`Student s`, `s.fullName`, `s.department.code`); **native SQL** dùng **tên bảng & cột** (`students`, `full_name`, `department_id`).
> Import cần: `org.springframework.data.jpa.repository.Query`, `org.springframework.data.repository.query.Param`.

## TODO 12 — JPQL + named parameter

**Repository**

```java
@Query("SELECT s FROM Student s " +
       "WHERE s.department.code = :code AND s.gpa >= :minGpa " +
       "ORDER BY s.gpa DESC")
List<Student> findGoodStudentsInDepartment(@Param("code") String code,
                                           @Param("minGpa") double minGpa);
```

**Service — khai báo trong `StudentService`**

```java
List<Student> findGoodStudents(String deptCode, double minGpa);   // TODO 12
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public List<Student> findGoodStudents(String deptCode, double minGpa) {
    return studentRepository.findGoodStudentsInDepartment(deptCode, minGpa);
}
```

**Runner** — chỉ gọi Service

```java
private void todo12() {
    title("TODO 12: JPQL + named parameter");
    printList("SE, GPA >= 3.0", studentService.findGoodStudents("SE", 3.0));
}
```

**Giải thích**
- Có `@Query` → tên method **tuỳ ý**, Spring không phân tích tên nữa.
- `:code` ↔ `@Param("code")`. Có thể dùng `?1, ?2` (positional) nhưng named dễ đọc, không phụ thuộc thứ tự.
- `s.department.code`: đi theo quan hệ → Hibernate tự JOIN (implicit join).
- JPQL sai cú pháp/sai tên field → app lỗi **khi startup** (Hibernate parse query sớm).

**Kết quả mong đợi:** Tran Thi Binh 3.8, Nguyen Thi Mai 3.6, Nguyen Van An 3.2.

**📌 Commit TODO 12**

```bash
git add .
git commit -m "feat(query): find good students in department with jpql" \
           -m "Refs: TODO 12"
```

Commit message đầy đủ:
```
feat(query): find good students in department with jpql

Refs: TODO 12
```

---

## TODO 13 — JPQL `LIKE`

**Repository**

```java
@Query("SELECT s FROM Student s " +
       "WHERE LOWER(s.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
       "   OR LOWER(s.email)    LIKE LOWER(CONCAT('%', :kw, '%')) " +
       "ORDER BY s.fullName")
List<Student> searchByKeyword(@Param("kw") String keyword);
```

**Service — khai báo trong `StudentService`**

```java
List<Student> searchByKeyword(String keyword);   // TODO 13
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public List<Student> searchByKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
        return List.of();
    }
    return studentRepository.searchByKeyword(keyword.trim());
}
```

**Runner** — chỉ gọi Service

```java
private void todo13() {
    title("TODO 13: JPQL LIKE");
    printList("keyword 'hoa'", studentService.searchByKeyword("hoa"));
    printList("keyword 'gmail'", studentService.searchByKeyword("gmail"));
}
```

**Giải thích**
- `CONCAT('%', :kw, '%')` là cách chuẩn JPQL. Spring Data cũng hỗ trợ cú pháp rút gọn `LIKE %:kw%`.
- ❌ **Sai:** `LIKE '%:kw%'` — `:kw` nằm trong chuỗi ký tự nên không còn là tham số.
- `LOWER(...)` hai vế → không phân biệt hoa thường, không phụ thuộc collation của DB.
- Email `null` (Do Van Nam): `LOWER(NULL) LIKE ...` cho kết quả UNKNOWN → bị loại, không lỗi.
- Muốn ưu tiên đúng thứ tự `AND/OR` thì thêm ngoặc — derived query không làm được việc này, đây là lý do dùng `@Query`.

**Kết quả mong đợi**
- `"hoa"` → Hoang Van Em (vì "**Hoa**ng"), Vo Thi Hoa
- `"gmail"` → Dang Van Giang, Hoang Van Em

**📌 Commit TODO 13**

```bash
git add .
git commit -m "feat(query): search students by keyword with jpql like" \
           -m "Refs: TODO 13"
```

Commit message đầy đủ:
```
feat(query): search students by keyword with jpql like

Refs: TODO 13
```

---

## TODO 14 — Thống kê: `LEFT JOIN` + `GROUP BY` + DTO

**Bước 1 — Tạo DTO record** `dto/DepartmentStatDTO.java`

```java
package com.hsf302.ch4.dto;

public record DepartmentStatDTO(String code, String name, Long totalStudents, Double avgGpa) {

    @Override
    public String toString() {
        return String.format("%-3s | %-25s | %2d | %s",
                code, name, totalStudents, avgGpa == null ? "null" : String.format("%.3f", avgGpa));
    }
}
```

**Bước 2 — `DepartmentRepository`**

```java
@Query("SELECT new com.hsf302.ch4.dto.DepartmentStatDTO(d.code, d.name, COUNT(s), AVG(s.gpa)) " +
       "FROM Department d LEFT JOIN d.students s " +
       "GROUP BY d.code, d.name " +
       "ORDER BY d.code")
List<DepartmentStatDTO> getDepartmentStats();
```

**Bước 3 — Service**

**Service — khai báo trong `DepartmentService`**

```java
List<DepartmentStatDTO> getStatistics();   // TODO 14 (dùng lại ở TODO 23)
```

**Service — cài đặt trong `DepartmentServiceImpl`**

```java
@Override
public List<DepartmentStatDTO> getStatistics() {
    return departmentRepository.getDepartmentStats();
}
```

**Bước 4 — Runner**

```java
private void todo14() {
    title("TODO 14: Statistics by department (DTO)");
    printList("code | name | total | avgGpa", departmentService.getStatistics());
}
```

**Giải thích**
- **Constructor expression** `SELECT new <tên đầy đủ package>.DepartmentStatDTO(...)`: JPA gọi constructor của record cho mỗi dòng. Thiếu package → lỗi `Could not resolve class`.
- Kiểu phải khớp constructor: `COUNT(...)` trả **`Long`**, `AVG(...)` trả **`Double`**. Khai `int`/`long` nguyên thuỷ sẽ lỗi "no matching constructor".
- **`LEFT JOIN`** giữ lại department không có student (GD) → `COUNT(s) = 0`, `AVG = null`. Nếu dùng `JOIN` (inner), GD biến mất.
- `COUNT(s)` (không phải `COUNT(*)`) đếm student **khác null** → đúng 0 cho GD.
- `FROM Department d LEFT JOIN d.students s`: JOIN theo **field quan hệ**, không cần `ON`.
- Cách cũ trả `List<Object[]>` rồi ép kiểu `row[0]`, `row[1]`… → dễ sai, khó đọc; DTO rõ ràng hơn.

**Kết quả mong đợi**
```
AI  | Artificial Intelligence   |  3 | 3.400
GD  | Graphic Design            |  0 | null
IA  | Information Assurance     |  3 | 2.400
SE  | Software Engineering      |  4 | 3.275
```

**📌 Commit TODO 14**

```bash
git add .
git commit -m "feat(query): add department statistics with dto projection" \
           -m "Dùng LEFT JOIN để giữ khoa chưa có sinh viên" \
           -m "Refs: TODO 14"
```

Commit message đầy đủ:
```
feat(query): add department statistics with dto projection

Dùng LEFT JOIN để giữ khoa chưa có sinh viên

Refs: TODO 14
```

---

## TODO 15 — Subquery

**Repository**

```java
@Query("SELECT s FROM Student s " +
       "WHERE s.gpa > (SELECT AVG(s2.gpa) FROM Student s2) " +
       "ORDER BY s.gpa DESC")
List<Student> findAboveAverageGpa();
```

**Service — khai báo trong `StudentService`**

```java
List<Student> findAboveAverageGpa();   // TODO 15
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public List<Student> findAboveAverageGpa() {
    return studentRepository.findAboveAverageGpa();
}
```

**Runner** — chỉ gọi Service

```java
private void todo15() {
    title("TODO 15: Subquery - GPA above average");
    printList("GPA > AVG", studentService.findAboveAverageGpa());
}
```

**Giải thích**
- Subquery trong JPQL phải dùng **alias khác** (`s2`) với query ngoài.
- Không thể làm bằng derived query vì cần giá trị tính toán (AVG).
- GPA trung bình = 30.5 / 10 = **3.05**.

**Kết quả mong đợi:** Hoa 3.9, Binh 3.8, Mai 3.6, Dung 3.5, An 3.2, Lan 3.1 → 6 record(s).

**📌 Commit TODO 15**

```bash
git add .
git commit -m "feat(query): find students above average gpa with subquery" \
           -m "Refs: TODO 15"
```

Commit message đầy đủ:
```
feat(query): find students above average gpa with subquery

Refs: TODO 15
```

---

## TODO 16 — `LazyInitializationException` & `JOIN FETCH`

**(a) Tái hiện lỗi**

**Service — khai báo trong `DepartmentService`**

```java
Optional<Department> findByCode(String code);   // TODO 16a
```

**Service — cài đặt trong `DepartmentServiceImpl`**

```java
@Override
public Optional<Department> findByCode(String code) {
    return departmentRepository.findByCode(code);
}
```

**Runner** — chỉ gọi Service

```java
// import org.hibernate.LazyInitializationException;
private void todo16() {
    title("TODO 16: LazyInitializationException & JOIN FETCH");

    Department ai = departmentService.findByCode("AI").orElseThrow();
    try {
        System.out.println("AI has " + ai.getStudents().size() + " students");
    } catch (LazyInitializationException e) {
        System.out.println("(a) Caught: " + e.getClass().getSimpleName());
        System.out.println("    " + e.getMessage());
    }
```

**Tại sao lỗi?**
1. `departmentService.findByCode()` chạy trong transaction `readOnly` của `DepartmentServiceImpl` → load `Department`, **chưa** load `students` (LAZY, chỉ là proxy collection).
2. Method của Service kết thúc → transaction + persistence context **đóng**, entity trả về cho Runner bị *detached*.
3. `getStudents().size()` cần query DB nhưng không còn session → `LazyInitializationException: failed to lazily initialize a collection of role: ...Department.students ... no Session`.

> Lưu ý: `spring.jpa.open-in-view` chỉ có tác dụng trong **web request**, không giúp gì ở `CommandLineRunner`.

**(b) Sửa bằng `JOIN FETCH` — `DepartmentRepository`**

```java
@Query("SELECT d FROM Department d LEFT JOIN FETCH d.students WHERE d.code = :code")
Optional<Department> findByCodeWithStudents(@Param("code") String code);
```

**Service — khai báo trong `DepartmentService`**

```java
Department getWithStudents(String code);   // TODO 16b
```

**Service — cài đặt trong `DepartmentServiceImpl`**

```java
@Override
public Department getWithStudents(String code) {
    return departmentRepository.findByCodeWithStudents(code)
            .orElseThrow(() -> new IllegalArgumentException("Department not found: " + code));
}
```

**Runner (tiếp)**

```java
    Department aiFull = departmentService.getWithStudents("AI");
    System.out.println("(b) " + aiFull);
    aiFull.getStudents().forEach(s -> System.out.println("     " + s));
}
```

**Giải thích**
- `JOIN FETCH` bảo Hibernate lấy department **và** students trong **1 câu SQL** (quan sát console: 1 câu `select ... from departments d left join students s ...`) → collection đã được khởi tạo, dùng được sau khi transaction đóng.
- `LEFT JOIN FETCH`: department không có student vẫn trả về (với list rỗng).
- Hibernate 6 tự loại dòng trùng của entity gốc → không bắt buộc `DISTINCT`.
- Cách khác: `@EntityGraph(attributePaths = "students")`, hoặc xử lý dữ liệu (đếm, chuyển sang DTO) **ngay trong method của Service** khi transaction còn mở, rồi mới trả kết quả cho Runner.
- **Không** sửa bằng cách đổi sang `FetchType.EAGER` — sẽ luôn load students ở mọi nơi, dễ gây N+1.

**Kết quả mong đợi**
```
(a) Caught: LazyInitializationException
    failed to lazily initialize a collection of role: com.hsf302.ch4.pojo.Department.students: could not initialize proxy - no Session
(b) AI - Artificial Intelligence
     AI001 | Pham Thi Dung ...
     AI002 | Hoang Van Em ...
     AI003 | Vo Thi Hoa ...
```

**📌 Commit TODO 16**

```bash
git add .
git commit -m "fix(query): load department students with join fetch" \
           -m "Truy cập students ngoài transaction gây LazyInitializationException, thêm findByCodeWithStudents dùng LEFT JOIN FETCH" \
           -m "Refs: TODO 16"
```

Commit message đầy đủ:
```
fix(query): load department students with join fetch

Truy cập students ngoài transaction gây LazyInitializationException,
thêm findByCodeWithStudents dùng LEFT JOIN FETCH

Refs: TODO 16
```

---

## TODO 17 — Native query

**Repository**

```java
@Query(value = "SELECT TOP (:n) s.* " +
               "FROM students s JOIN departments d ON s.department_id = d.id " +
               "WHERE d.code = :code " +
               "ORDER BY s.gpa DESC",
       nativeQuery = true)
List<Student> findTopNByDepartmentNative(@Param("code") String code, @Param("n") int n);
```

**Service — khai báo trong `StudentService`**

```java
List<Student> findTopNInDepartment(String deptCode, int n);   // TODO 17
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public List<Student> findTopNInDepartment(String deptCode, int n) {
    if (n <= 0) {
        throw new IllegalArgumentException("n phải > 0");
    }
    return studentRepository.findTopNByDepartmentNative(deptCode, n);
}
```

**Runner** — chỉ gọi Service

```java
private void todo17() {
    title("TODO 17: Native query - TOP N");
    printList("Top 2 GPA of SE", studentService.findTopNInDepartment("SE", 2));
}
```

**Giải thích**
- Native = SQL thuần của SQL Server: **tên bảng** `students`, **tên cột** `department_id`, `gpa`; phải viết `JOIN ... ON` tường minh.
- `TOP (:n)` — SQL Server cho phép tham số trong `TOP` khi đặt **trong ngoặc**. (MySQL/PostgreSQL dùng `LIMIT` → native query **không portable**.)
- `SELECT s.*` để Hibernate map đủ cột vào entity `Student`. Thiếu cột → lỗi `The column name xxx is not valid`.
- Native sai cú pháp **chỉ lỗi khi chạy** (không kiểm tra lúc startup như JPQL).

**Kết quả mong đợi:** Tran Thi Binh 3.8, Nguyen Thi Mai 3.6.

**📌 Commit TODO 17**

```bash
git add .
git commit -m "feat(query): get top n students of department with native sql" \
           -m "Refs: TODO 17"
```

Commit message đầy đủ:
```
feat(query): get top n students of department with native sql

Refs: TODO 17
```

---

## TODO 18 — Interface projection

**Bước 1 — Tạo interface** `dto/StudentSummary.java`

```java
package com.hsf302.ch4.dto;

public interface StudentSummary {
    String getStudentCode();
    String getFullName();
    Double getGpa();
    String getDepartmentName();
}
```

**Bước 2 — Repository**

```java
@Query("SELECT s.studentCode AS studentCode, s.fullName AS fullName, " +
       "       s.gpa AS gpa, d.name AS departmentName " +
       "FROM Student s JOIN s.department d " +
       "WHERE s.active = true " +
       "ORDER BY s.fullName")
List<StudentSummary> findActiveSummaries();
```

**Bước 3 — Service**

**Service — khai báo trong `StudentService`**

```java
List<StudentSummary> getActiveSummaries();   // TODO 18
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public List<StudentSummary> getActiveSummaries() {
    return studentRepository.findActiveSummaries();
}
```

**Bước 4 — Runner**

```java
private void todo18() {
    title("TODO 18: Interface projection");
    List<StudentSummary> list = studentService.getActiveSummaries();
    list.forEach(p -> System.out.printf("   %s | %-15s | %.1f | %s%n",
            p.getStudentCode(), p.getFullName(), p.getGpa(), p.getDepartmentName()));
    System.out.println("   -> " + list.size() + " record(s)");
}
```

**Giải thích**
- Spring tạo proxy implement `StudentSummary`, map theo **alias**: alias `departmentName` ↔ getter `getDepartmentName()`. Alias sai/thiếu → getter trả `null`.
- Chỉ SELECT 4 cột → nhẹ hơn load cả entity; lấy được `d.name` mà **không** gặp `LazyInitializationException` (vì dữ liệu đã nằm trong kết quả).
- `JOIN s.department d` là JOIN theo quan hệ trong JPQL.
- Interface projection dùng được cả với **native query** (alias phải khớp getter), còn constructor expression (`new ...DTO`) thì **không**.

**Kết quả mong đợi**
```
IA002 | Bui Thi Lan     | 3.1 | Information Assurance
IA003 | Do Van Nam      | 2.2 | Information Assurance
AI002 | Hoang Van Em    | 2.8 | Artificial Intelligence
SE004 | Nguyen Thi Mai  | 3.6 | Software Engineering
SE001 | Nguyen Van An   | 3.2 | Software Engineering
AI001 | Pham Thi Dung   | 3.5 | Artificial Intelligence
SE002 | Tran Thi Binh   | 3.8 | Software Engineering
AI003 | Vo Thi Hoa      | 3.9 | Artificial Intelligence
-> 8 record(s)
```

**📌 Commit TODO 18**

```bash
git add .
git commit -m "feat(query): add active student summary interface projection" \
           -m "Refs: TODO 18"
```

Commit message đầy đủ:
```
feat(query): add active student summary interface projection

Refs: TODO 18
```

---

## TODO 19 — `@Query` + `Pageable`

**Repository**

```java
@Query("SELECT s FROM Student s WHERE s.department.code = :code AND s.active = true")
Page<Student> findActiveByDepartment(@Param("code") String code, Pageable pageable);
```

**Service — khai báo trong `StudentService`**

```java
Page<Student> findActiveByDepartment(String deptCode, int pageIndex, int size);   // TODO 19
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
public Page<Student> findActiveByDepartment(String deptCode, int pageIndex, int size) {
    Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("gpa").descending());
    return studentRepository.findActiveByDepartment(deptCode, pageable);
}
```

**Runner** — chỉ gọi Service

```java
private void todo19() {
    title("TODO 19: @Query + Pageable");
    for (int i = 0; i < 2; i++) {
        Page<Student> page = studentService.findActiveByDepartment("SE", i, 2);
        printList("SE active - page " + page.getNumber(), page.getContent());
        System.out.println("   totalElements=" + page.getTotalElements()
                + ", totalPages=" + page.getTotalPages());
    }
}
```

**Giải thích**
- Thêm tham số `Pageable` **cuối cùng** → Spring tự thêm `ORDER BY` (từ `Sort` trong `Pageable`) + `OFFSET/FETCH`, và **tự sinh câu COUNT** từ JPQL.
- Không viết `ORDER BY` cứng trong `@Query` khi đã sort bằng `Pageable` (tránh xung đột thứ tự).
- Với **native query** + `Page`, nên khai báo thêm `countQuery = "SELECT COUNT(*) ..."`.

**Kết quả mong đợi**
```
-- SE active - page 0:  Tran Thi Binh 3.8, Nguyen Thi Mai 3.6
   totalElements=3, totalPages=2
-- SE active - page 1:  Nguyen Van An 3.2
   totalElements=3, totalPages=2
```

**📌 Commit TODO 19**

```bash
git add .
git commit -m "feat(query): paginate active students of department" \
           -m "Refs: TODO 19"
```

Commit message đầy đủ:
```
feat(query): paginate active students of department

Refs: TODO 19
```

---

# Bonus

## TODO 24 (Bonus) — Specification

> Chạy **trước Part E** để làm trên dữ liệu gốc (đã sắp xếp trong `run()`).

**Bước 1 — `specification/StudentSpecs.java`**

```java
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
```

**Bước 2 — Service**

**Service — khai báo trong `StudentService`**

```java
List<Student> search(String kw, String deptCode, Double minGpa, Boolean active);   // TODO 24
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
// import org.springframework.data.jpa.domain.Specification;
// import com.hsf302.ch4.specification.StudentSpecs;
@Override
public List<Student> search(String kw, String deptCode, Double minGpa, Boolean active) {
    Specification<Student> spec = Specification.where(StudentSpecs.nameContains(kw))
            .and(StudentSpecs.inDepartment(deptCode))
            .and(StudentSpecs.gpaAtLeast(minGpa))
            .and(StudentSpecs.isActive(active));
    return studentRepository.findAll(spec, Sort.by("fullName"));
}
```

**Bước 3 — Runner**

```java
private void todo24() {
    title("TODO 24 (Bonus): Specification");
    printList("search(null, AI, 3.0, true)", studentService.search(null, "AI", 3.0, true));
    printList("search(van, null, null, null)", studentService.search("van", null, null, null));
}
```

**Giải thích**
- Mỗi `Specification` là một lambda `(root, query, cb) -> Predicate`; `root` = entity `Student`, `cb` = `CriteriaBuilder`.
- Trả `null` → Spring bỏ qua điều kiện → rất hợp với form tìm kiếm có ô **tuỳ chọn**. Nếu làm bằng derived query phải viết 2⁴ = 16 method.
- `root.get("fullName")` dùng **chuỗi** → sai tên field chỉ lỗi khi chạy.
- `root.join("department")` tạo JOIN sang bảng departments.
- Các phiên bản Spring Data JPA mới có thể đánh dấu `Specification.where` là deprecated → dùng `Specification.allOf(...)`. Với Spring Boot 3.2.x vẫn dùng bình thường.

**Kết quả mong đợi**
- `(null, "AI", 3.0, true)` → Pham Thi Dung 3.5, Vo Thi Hoa 3.9 (Hoang Van Em 2.8 bị loại)
- `("van", null, null, null)` → Dang Van Giang, Do Van Nam, Hoang Van Em, Le Van Cuong, Nguyen Van An

**📌 Commit TODO 24**

```bash
git add .
git commit -m "feat(spec): add dynamic student search with specification" \
           -m "Refs: TODO 24"
```

Commit message đầy đủ:
```
feat(spec): add dynamic student search with specification

Refs: TODO 24
```

---

# Part E — Thay đổi dữ liệu

> Các thao tác ghi đặt ở **ServiceImpl** và gắn `@Transactional` (không `readOnly`) trên **từng method ghi** để ghi đè `@Transactional(readOnly = true)` của class.
> Import: `org.springframework.transaction.annotation.Transactional`.

## TODO 20 — Update bằng dirty checking

**Service — khai báo trong `StudentService`**

```java
Student updateGpa(String studentCode, double newGpa);   // TODO 20
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
@Transactional                                            // ghi đè readOnly của class
public Student updateGpa(String studentCode, double newGpa) {
    if (newGpa < 0 || newGpa > 4) {
        throw new IllegalArgumentException("GPA phải trong khoảng [0, 4]");
    }
    Student s = studentRepository.findByStudentCode(studentCode)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentCode));
    s.setGpa(newGpa);
    return s;              // commit → Hibernate tự sinh UPDATE (dirty checking)
    // Cách tương đương: return studentRepository.save(s);
}
```

**Runner** — chỉ gọi Service

```java
private void todo20() {
    title("TODO 20: Update GPA (dirty checking)");
    System.out.println("Before: " + studentService.findByStudentCode("SE001").orElseThrow());
    studentService.updateGpa("SE001", 3.4);
    System.out.println("After : " + studentService.findByStudentCode("SE001").orElseThrow());
}
```

**Giải thích**
- Trong `@Transactional`, entity load ra ở trạng thái **managed**. Khi commit, Hibernate so sánh với snapshot ban đầu → field thay đổi → tự sinh `UPDATE students SET ... WHERE id=?`. Không cần gọi `save()`.
- Ngoài transaction (entity detached) thì **bắt buộc** gọi `save()` (→ `merge`) để lưu thay đổi.
- `@Transactional` chỉ có tác dụng khi method được gọi **qua proxy** (từ bean khác). Gọi nội bộ `this.updateGpa()` trong cùng class sẽ **không** có transaction.
- ⚠️ Nếu **quên** `@Transactional` trên method này, method dùng `readOnly = true` của class → Hibernate không flush, thay đổi **không được lưu** (không báo lỗi) — `After` vẫn là 3.2.

**Kết quả mong đợi**
```
Before: SE001 | Nguyen Van An ... | 3.2 | active
After : SE001 | Nguyen Van An ... | 3.4 | active
```

**📌 Commit TODO 20**

```bash
git add .
git commit -m "feat(service): update student gpa with dirty checking" \
           -m "Refs: TODO 20"
```

Commit message đầy đủ:
```
feat(service): update student gpa with dirty checking

Refs: TODO 20
```

---

## TODO 21 — `@Modifying` UPDATE

**Repository**

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE Student s SET s.active = false WHERE s.gpa < :threshold AND s.active = true")
int deactivateLowGpa(@Param("threshold") double threshold);
```

**Service — khai báo trong `StudentService`**

```java
int deactivateLowGpa(double threshold);   // TODO 21
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
@Transactional
public int deactivateLowGpa(double threshold) {
    return studentRepository.deactivateLowGpa(threshold);
}
```

**Runner** — chỉ gọi Service

```java
private void todo21() {
    title("TODO 21: @Modifying UPDATE");
    int rows = studentService.deactivateLowGpa(2.5);
    System.out.println("Rows affected: " + rows);
    System.out.println("Active students now: " + studentService.countActive());
}
```

**Giải thích**
- `@Query` mặc định chỉ cho SELECT → UPDATE/DELETE phải có **`@Modifying`**, nếu không: `InvalidDataAccessApiUsageException ... not supported for DML operations`.
- Phải chạy trong transaction, nếu không: `TransactionRequiredException: Executing an update/delete query`.
- Kiểu trả về `int` = **số dòng bị ảnh hưởng**.
- Bulk UPDATE đi **thẳng xuống DB**, bỏ qua persistence context:
  - `flushAutomatically = true`: flush thay đổi đang chờ **trước** khi chạy UPDATE.
  - `clearAutomatically = true`: xoá cache cấp 1 **sau** UPDATE → lần đọc sau lấy dữ liệu mới, không đọc bản cũ.
- Điều kiện `AND s.active = true` → chỉ đếm dòng thực sự thay đổi. Cuong (2.5) không thoả `< 2.5`; Giang (1.9) đã inactive.

**Kết quả mong đợi:** `Rows affected: 1` (Do Van Nam) · `Active students now: 7`.

**📌 Commit TODO 21**

```bash
git add .
git commit -m "feat(service): deactivate active students with low gpa" \
           -m "Dùng @Modifying(clearAutomatically = true) để tránh đọc dữ liệu cũ" \
           -m "Refs: TODO 21"
```

Commit message đầy đủ:
```
feat(service): deactivate active students with low gpa

Dùng @Modifying(clearAutomatically = true) để tránh đọc dữ liệu cũ

Refs: TODO 21
```

---

## TODO 22 — Chuyển khoa & xoá khoa trong 1 transaction

**Repository**

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE Student s SET s.department = :to WHERE s.department = :from")
int transferStudents(@Param("from") Department from, @Param("to") Department to);
```

**Service — khai báo trong `DepartmentService`**

```java
int transferStudentsAndDelete(String fromCode, String toCode);   // TODO 22
List<Department> findAll();                                      // TODO 22
```

**Service — cài đặt trong `DepartmentServiceImpl`**

```java
@Override
@Transactional
public int transferStudentsAndDelete(String fromCode, String toCode) {
    if (fromCode.equals(toCode)) {
        throw new IllegalArgumentException("Khoa nguồn và khoa đích phải khác nhau");
    }
    Department from = departmentRepository.findByCode(fromCode)
            .orElseThrow(() -> new IllegalArgumentException("Department not found: " + fromCode));
    Department to = departmentRepository.findByCode(toCode)
            .orElseThrow(() -> new IllegalArgumentException("Department not found: " + toCode));

    int moved = studentRepository.transferStudents(from, to);   // 1. chuyển FK sang khoa mới
    departmentRepository.deleteById(from.getId());              // 2. khoa cũ đã rỗng → xoá được
    return moved;
}

@Override
public List<Department> findAll() {
    return departmentRepository.findAll(Sort.by("id"));
}
```

**Runner** — chỉ gọi Service

```java
private void todo22() {
    title("TODO 22: Transfer IA -> SE, then delete IA");
    int moved = departmentService.transferStudentsAndDelete("IA", "SE");
    System.out.println("Students moved: " + moved);
    System.out.println("Students of SE: " + studentService.countByDepartment("SE"));
    printList("Departments left", departmentService.findAll());
}
```

> 💡 Thao tác này dùng **2 repository** (`StudentRepository` + `DepartmentRepository`) trong **1 transaction** → đây chính là việc của tầng Service. Đặt ở `DepartmentService` vì nghiệp vụ chính là *xoá khoa*.

**Giải thích**
- JPQL cho phép truyền **entity làm tham số**: `s.department = :from` được dịch thành `department_id = ?` (id của `from`).
- **Thứ tự quan trọng:** nếu xoá `IA` trước khi chuyển sinh viên → vi phạm FK: `The DELETE statement conflicted with the REFERENCE constraint ...`.
- Cả 2 bước trong **một** `@Transactional`: nếu bước 2 lỗi, bước 1 bị **rollback** → không có trạng thái "đã chuyển nhưng chưa xoá".
- Vì `Department.students` không cascade REMOVE nên `deleteById` chỉ xoá department, không đụng tới student.
- Sau `clearAutomatically`, entity `from` bị detached; `deleteById(id)` tự load lại theo id rồi xoá → an toàn.

**Kết quả mong đợi**
```
Students moved: 3
Students of SE: 7
-- Departments left:
   SE - Software Engineering
   AI - Artificial Intelligence
   GD - Graphic Design
   -> 3 record(s)
```

**📌 Commit TODO 22**

```bash
git add .
git commit -m "feat(service): transfer students and remove department" \
           -m "Chuyển sinh viên và xoá khoa trong cùng một transaction" \
           -m "Refs: TODO 22"
```

Commit message đầy đủ:
```
feat(service): transfer students and remove department

Chuyển sinh viên và xoá khoa trong cùng một transaction

Refs: TODO 22
```

---

## TODO 23 — Derived delete

**Repository**

```java
long deleteByActiveFalse();
```

**Service — khai báo trong `StudentService`**

```java
long deleteInactiveStudents();   // TODO 23
```

**Service — cài đặt trong `StudentServiceImpl`**

```java
@Override
@Transactional
public long deleteInactiveStudents() {
    return studentRepository.deleteByActiveFalse();
}
```

**Runner** — chỉ gọi Service

```java
private void todo23() {
    title("TODO 23: Derived delete");
    long deleted = studentService.deleteInactiveStudents();
    System.out.println("Deleted: " + deleted);
    System.out.println("Students left: " + studentService.count());
    printList("Final statistics", departmentService.getStatistics());
}
```

**Giải thích**
- Derived delete `deleteBy...` **bắt buộc có transaction**, nếu không: `No EntityManager with actual transaction available for current thread`.
- Cơ chế: Spring **SELECT** các student khớp điều kiện rồi gọi `em.remove()` **từng entity** (console thấy 1 SELECT + N câu DELETE) → chạy được cascade & lifecycle callback, nhưng chậm với dữ liệu lớn. Xoá hàng loạt nên dùng `@Modifying @Query("DELETE FROM Student s WHERE s.active = false")` (1 câu DELETE).
- Kiểu trả về `long` = số bản ghi đã xoá; hoặc `List<Student>` = danh sách đã xoá.

**Kết quả mong đợi**
```
Deleted: 3                         (Le Van Cuong, Dang Van Giang, Do Van Nam)
Students left: 7
-- Final statistics:
   AI  | Artificial Intelligence   |  3 | 3.400
   GD  | Graphic Design            |  0 | null
   SE  | Software Engineering      |  4 | 3.475
```
*(SE còn: An 3.4, Binh 3.8, Mai 3.6, Lan 3.1 → TB = 13.9 / 4 = 3.475)*

**📌 Commit TODO 23**

```bash
git add .
git commit -m "feat(service): delete inactive students" \
           -m "Refs: TODO 23"
```

Commit message đầy đủ:
```
feat(service): delete inactive students

Refs: TODO 23
```

---

# Tổng hợp code hoàn chỉnh

### `StudentRepository.java`

```java
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

    // ===== Part C — Derived query =====
    Optional<Student> findByStudentCode(String studentCode);                    // TODO 8
    boolean existsByEmail(String email);
    long countByActiveTrue();

    List<Student> findByFullNameContainingIgnoreCase(String keyword);           // TODO 9
    List<Student> findByEmailEndingWith(String suffix);
    List<Student> findByEmailIsNull();

    List<Student> findByGpaBetweenOrderByGpaDesc(double min, double max);       // TODO 10
    List<Student> findByGenderAndActiveTrue(Gender gender);
    List<Student> findByDobAfter(LocalDate date);

    List<Student> findByDepartment_CodeOrderByFullNameAsc(String code);         // TODO 11
    long countByDepartment_Code(String code);
    List<Student> findTop3ByOrderByGpaDesc();

    // ===== Part D — Custom query =====
    @Query("SELECT s FROM Student s " +                                          // TODO 12
           "WHERE s.department.code = :code AND s.gpa >= :minGpa ORDER BY s.gpa DESC")
    List<Student> findGoodStudentsInDepartment(@Param("code") String code, @Param("minGpa") double minGpa);

    @Query("SELECT s FROM Student s " +                                          // TODO 13
           "WHERE LOWER(s.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "   OR LOWER(s.email)    LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "ORDER BY s.fullName")
    List<Student> searchByKeyword(@Param("kw") String keyword);

    @Query("SELECT s FROM Student s " +                                          // TODO 15
           "WHERE s.gpa > (SELECT AVG(s2.gpa) FROM Student s2) ORDER BY s.gpa DESC")
    List<Student> findAboveAverageGpa();

    @Query(value = "SELECT TOP (:n) s.* FROM students s " +                     // TODO 17
                   "JOIN departments d ON s.department_id = d.id " +
                   "WHERE d.code = :code ORDER BY s.gpa DESC",
           nativeQuery = true)
    List<Student> findTopNByDepartmentNative(@Param("code") String code, @Param("n") int n);

    @Query("SELECT s.studentCode AS studentCode, s.fullName AS fullName, " +    // TODO 18
           "       s.gpa AS gpa, d.name AS departmentName " +
           "FROM Student s JOIN s.department d WHERE s.active = true ORDER BY s.fullName")
    List<StudentSummary> findActiveSummaries();

    @Query("SELECT s FROM Student s WHERE s.department.code = :code AND s.active = true") // TODO 19
    Page<Student> findActiveByDepartment(@Param("code") String code, Pageable pageable);

    // ===== Part E — Modifying =====
    @Modifying(clearAutomatically = true, flushAutomatically = true)            // TODO 21
    @Query("UPDATE Student s SET s.active = false WHERE s.gpa < :threshold AND s.active = true")
    int deactivateLowGpa(@Param("threshold") double threshold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)            // TODO 22
    @Query("UPDATE Student s SET s.department = :to WHERE s.department = :from")
    int transferStudents(@Param("from") Department from, @Param("to") Department to);

    long deleteByActiveFalse();                                                 // TODO 23
}
```

### `DepartmentRepository.java`

```java
package com.hsf302.ch4.repository;

import com.hsf302.ch4.dto.DepartmentStatDTO;
import com.hsf302.ch4.pojo.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);                               // TODO 11, 16, 22
    List<Department> findByStudentsIsEmpty();                                   // TODO 11

    @Query("SELECT new com.hsf302.ch4.dto.DepartmentStatDTO(d.code, d.name, COUNT(s), AVG(s.gpa)) " +
           "FROM Department d LEFT JOIN d.students s " +
           "GROUP BY d.code, d.name ORDER BY d.code")
    List<DepartmentStatDTO> getDepartmentStats();                               // TODO 14, 23

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.students WHERE d.code = :code")
    Optional<Department> findByCodeWithStudents(@Param("code") String code);    // TODO 16
}
```

### `DepartmentService.java`

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.DepartmentStatDTO;
import com.hsf302.ch4.pojo.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    long count();                                                    // TODO 6
    boolean existsById(Long id);                                     // TODO 6
    List<Department> findDepartmentsWithoutStudents();               // TODO 11
    List<DepartmentStatDTO> getStatistics();                         // TODO 14, 23
    Optional<Department> findByCode(String code);                    // TODO 16a
    Department getWithStudents(String code);                         // TODO 16b
    int transferStudentsAndDelete(String fromCode, String toCode);   // TODO 22
    List<Department> findAll();                                      // TODO 22
}
```

### `DepartmentServiceImpl.java`

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.DepartmentStatDTO;
import com.hsf302.ch4.pojo.Department;
import com.hsf302.ch4.repository.DepartmentRepository;
import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    @Override
    public long count() {
        return departmentRepository.count();
    }

    @Override
    public boolean existsById(Long id) {
        return departmentRepository.existsById(id);
    }

    @Override
    public List<Department> findDepartmentsWithoutStudents() {
        return departmentRepository.findByStudentsIsEmpty();
    }

    @Override
    public List<DepartmentStatDTO> getStatistics() {
        return departmentRepository.getDepartmentStats();
    }

    @Override
    public Optional<Department> findByCode(String code) {
        return departmentRepository.findByCode(code);
    }

    @Override
    public Department getWithStudents(String code) {
        return departmentRepository.findByCodeWithStudents(code)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + code));
    }

    @Override
    @Transactional
    public int transferStudentsAndDelete(String fromCode, String toCode) {
        if (fromCode.equals(toCode)) {
            throw new IllegalArgumentException("Khoa nguồn và khoa đích phải khác nhau");
        }
        Department from = departmentRepository.findByCode(fromCode)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + fromCode));
        Department to = departmentRepository.findByCode(toCode)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + toCode));

        int moved = studentRepository.transferStudents(from, to);
        departmentRepository.deleteById(from.getId());
        return moved;
    }

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll(Sort.by("id"));
    }
}
```

### `StudentService.java`

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.StudentSummary;
import com.hsf302.ch4.pojo.Gender;
import com.hsf302.ch4.pojo.Student;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentService {

    // ===== Part B — Built-in =====
    long count();                                                                    // TODO 6
    Optional<Student> findById(Long id);                                             // TODO 6
    List<Student> findAllOrderByGpaDesc();                                           // TODO 7
    Page<Student> findPage(int pageIndex, int size, String sortField);               // TODO 7

    // ===== Part C — Derived query =====
    Optional<Student> findByStudentCode(String studentCode);                         // TODO 8
    boolean isEmailExisted(String email);                                            // TODO 8
    long countActive();                                                              // TODO 8, 21
    List<Student> searchByName(String keyword);                                      // TODO 9
    List<Student> findByEmailDomain(String domain);                                  // TODO 9
    List<Student> findWithoutEmail();                                                // TODO 9
    List<Student> findByGpaRange(double min, double max);                            // TODO 10
    List<Student> findActiveByGender(Gender gender);                                 // TODO 10
    List<Student> findBornAfter(LocalDate date);                                     // TODO 10
    List<Student> findByDepartment(String deptCode);                                 // TODO 11
    long countByDepartment(String deptCode);                                         // TODO 11, 22
    List<Student> findTop3ByGpa();                                                   // TODO 11

    // ===== Part D — Custom query =====
    List<Student> findGoodStudents(String deptCode, double minGpa);                  // TODO 12
    List<Student> searchByKeyword(String keyword);                                   // TODO 13
    List<Student> findAboveAverageGpa();                                             // TODO 15
    List<Student> findTopNInDepartment(String deptCode, int n);                      // TODO 17
    List<StudentSummary> getActiveSummaries();                                       // TODO 18
    Page<Student> findActiveByDepartment(String deptCode, int pageIndex, int size);  // TODO 19

    // ===== Bonus =====
    List<Student> search(String kw, String deptCode, Double minGpa, Boolean active); // TODO 24

    // ===== Part E — Modifying =====
    Student updateGpa(String studentCode, double newGpa);                            // TODO 20
    int deactivateLowGpa(double threshold);                                          // TODO 21
    long deleteInactiveStudents();                                                   // TODO 23
}
```

### `StudentServiceImpl.java`

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.StudentSummary;
import com.hsf302.ch4.pojo.Gender;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.StudentRepository;
import com.hsf302.ch4.specification.StudentSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    // ===== Part B =====
    @Override
    public long count() {
        return studentRepository.count();
    }

    @Override
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    public List<Student> findAllOrderByGpaDesc() {
        return studentRepository.findAll(Sort.by(Sort.Direction.DESC, "gpa"));
    }

    @Override
    public Page<Student> findPage(int pageIndex, int size, String sortField) {
        if (pageIndex < 0 || size <= 0) {
            throw new IllegalArgumentException("pageIndex phải >= 0 và size phải > 0");
        }
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(sortField).ascending());
        return studentRepository.findAll(pageable);
    }

    // ===== Part C =====
    @Override
    public Optional<Student> findByStudentCode(String studentCode) {
        return studentRepository.findByStudentCode(studentCode);
    }

    @Override
    public boolean isEmailExisted(String email) {
        return studentRepository.existsByEmail(email);
    }

    @Override
    public long countActive() {
        return studentRepository.countByActiveTrue();
    }

    @Override
    public List<Student> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return studentRepository.findByFullNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public List<Student> findByEmailDomain(String domain) {
        String suffix = domain.startsWith("@") ? domain : "@" + domain;
        return studentRepository.findByEmailEndingWith(suffix);
    }

    @Override
    public List<Student> findWithoutEmail() {
        return studentRepository.findByEmailIsNull();
    }

    @Override
    public List<Student> findByGpaRange(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min GPA phải <= max GPA");
        }
        return studentRepository.findByGpaBetweenOrderByGpaDesc(min, max);
    }

    @Override
    public List<Student> findActiveByGender(Gender gender) {
        return studentRepository.findByGenderAndActiveTrue(gender);
    }

    @Override
    public List<Student> findBornAfter(LocalDate date) {
        return studentRepository.findByDobAfter(date);
    }

    @Override
    public List<Student> findByDepartment(String deptCode) {
        return studentRepository.findByDepartment_CodeOrderByFullNameAsc(deptCode);
    }

    @Override
    public long countByDepartment(String deptCode) {
        return studentRepository.countByDepartment_Code(deptCode);
    }

    @Override
    public List<Student> findTop3ByGpa() {
        return studentRepository.findTop3ByOrderByGpaDesc();
    }

    // ===== Part D =====
    @Override
    public List<Student> findGoodStudents(String deptCode, double minGpa) {
        return studentRepository.findGoodStudentsInDepartment(deptCode, minGpa);
    }

    @Override
    public List<Student> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return studentRepository.searchByKeyword(keyword.trim());
    }

    @Override
    public List<Student> findAboveAverageGpa() {
        return studentRepository.findAboveAverageGpa();
    }

    @Override
    public List<Student> findTopNInDepartment(String deptCode, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n phải > 0");
        }
        return studentRepository.findTopNByDepartmentNative(deptCode, n);
    }

    @Override
    public List<StudentSummary> getActiveSummaries() {
        return studentRepository.findActiveSummaries();
    }

    @Override
    public Page<Student> findActiveByDepartment(String deptCode, int pageIndex, int size) {
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("gpa").descending());
        return studentRepository.findActiveByDepartment(deptCode, pageable);
    }

    // ===== Bonus =====
    @Override
    public List<Student> search(String kw, String deptCode, Double minGpa, Boolean active) {
        Specification<Student> spec = Specification.where(StudentSpecs.nameContains(kw))
                .and(StudentSpecs.inDepartment(deptCode))
                .and(StudentSpecs.gpaAtLeast(minGpa))
                .and(StudentSpecs.isActive(active));
        return studentRepository.findAll(spec, Sort.by("fullName"));
    }

    // ===== Part E — ghi dữ liệu: @Transactional ghi đè readOnly =====
    @Override
    @Transactional
    public Student updateGpa(String studentCode, double newGpa) {
        if (newGpa < 0 || newGpa > 4) {
            throw new IllegalArgumentException("GPA phải trong khoảng [0, 4]");
        }
        Student s = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentCode));
        s.setGpa(newGpa);
        return s;
    }

    @Override
    @Transactional
    public int deactivateLowGpa(double threshold) {
        return studentRepository.deactivateLowGpa(threshold);
    }

    @Override
    @Transactional
    public long deleteInactiveStudents() {
        return studentRepository.deleteByActiveFalse();
    }
}
```

### Imports cần cho `ExerciseRunner`

```java
import com.hsf302.ch4.dto.StudentSummary;
import com.hsf302.ch4.service.DepartmentService;
import com.hsf302.ch4.service.StudentService;
import com.hsf302.ch4.pojo.Department;
import com.hsf302.ch4.pojo.Gender;
import com.hsf302.ch4.pojo.Student;
import org.hibernate.LazyInitializationException;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
```

---

# Bảng lỗi thường gặp

| Lỗi / hiện tượng | Nguyên nhân | Cách sửa |
|---|---|---|
| `PropertyReferenceException: No property 'name' found for type 'Student'` | Derived query / `Sort` dùng sai tên field (field là `fullName`) | Dùng đúng tên field Java |
| `No property 'orderByGpaDesc'...` | Viết `findTop3OrderByGpaDesc` thiếu `By` | `findTop3ByOrderByGpaDesc` |
| `Could not resolve root entity 'students'` | JPQL dùng tên bảng | JPQL dùng tên entity `Student` |
| `Invalid object name 'Student'` | Native query dùng tên entity | Native dùng tên bảng `students` |
| `Could not resolve class 'DepartmentStatDTO'` / không tìm thấy constructor | Thiếu package trong `new ...` hoặc sai kiểu (`int` thay vì `Long`) | Ghi tên đầy đủ, dùng `Long`/`Double` |
| `LazyInitializationException` | Truy cập quan hệ LAZY ngoài transaction (trong `toString()`, runner…) | `JOIN FETCH`, projection, hoặc `@Transactional` ở service |
| `StackOverflowError` khi in entity | `@Data`/`toString` 2 chiều gọi vòng | Tự viết `toString` không chứa quan hệ |
| `InvalidDataAccessApiUsageException ... DML operations` | `@Query` UPDATE/DELETE thiếu `@Modifying` | Thêm `@Modifying` |
| `TransactionRequiredException` / `No EntityManager with actual transaction` | Modifying query hoặc `deleteBy` không có transaction | `@Transactional` ở service |
| Đọc lại vẫn thấy giá trị cũ sau bulk UPDATE | Cache cấp 1 chưa được xoá | `@Modifying(clearAutomatically = true)` |
| `The DELETE statement conflicted with the REFERENCE constraint` | Xoá department khi còn student trỏ tới | Chuyển/xoá student trước (TODO 22) |
| Kết quả `LIKE` rỗng | Dùng `findByXxxLike("abc")` không có `%` hoặc `'%:kw%'` trong JPQL | Dùng `Containing` hoặc `CONCAT('%', :kw, '%')` |
| Kết quả mỗi lần chạy khác nhau | `ddl-auto=update` → dữ liệu Part E của lần trước còn lại | Dùng `ddl-auto=create` |
| `No qualifying bean of type 'com.hsf302.ch4.service.StudentService'` | Quên `@Service` trên `StudentServiceImpl`, hoặc impl chưa `implements StudentService` | Gắn `@Service` lên class implementation |
| Cập nhật chạy không lỗi nhưng dữ liệu **không đổi** | Method ghi thiếu `@Transactional` nên dùng `readOnly = true` của class | Thêm `@Transactional` lên method ghi |
| `LazyInitializationException` ở Runner dù đã có Service | Service trả entity ra ngoài transaction, Runner truy cập quan hệ LAZY | `JOIN FETCH` / projection, hoặc xử lý trong Service |
| `Cannot resolve method 'readOnly'` | Import nhầm `jakarta.transaction.Transactional` | Dùng `org.springframework.transaction.annotation.Transactional` |
| `Author identity unknown` khi commit | Chưa cấu hình tên/email Git | `git config --global user.name/user.email` |
| Commit kèm thư mục `target/`, `.idea/` | Thiếu `.gitignore` | Thêm `.gitignore`, rồi `git rm -r --cached target .idea` và commit lại |
| Lỡ gộp 2 TODO vào 1 commit | Quên commit sau TODO trước | Chưa push: `git reset --soft HEAD~1` rồi `git add` từng phần và commit lại |
| `Not a managed type: class ...Student` | Import `javax.persistence` hoặc entity nằm ngoài package gốc | Dùng `jakarta.persistence`, đặt trong `com.hsf302.ch4.*` |
