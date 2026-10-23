# Chapter 4 — Exercise 2: Hướng dẫn Step-by-Step (Many-To-Many)

> Đi kèm đề bài `Chapter4_exercise2.md`. Mỗi TODO gồm: **Mục tiêu → Các bước → Code → Giải thích → Kết quả mong đợi → Commit**.
> Bài này **làm tiếp trên project Exercise 1** (package `com.hsf302.ch4`, database `HSF302_CH4`). Nên tự làm trước khi xem code.

---

## Mục lục

- [Bước 0 — Chuẩn bị](#bước-0--chuẩn-bị) · [Kiến thức nền: @ManyToMany](#kiến-thức-nền-manytomany-trong-30-giây)
- **Part A:** [TODO 1](#todo-1--nhánh-exercise2--profile) · [TODO 2](#todo-2--entity-course-inverse-side) · [TODO 3](#todo-3--sửa-student-owning-side) · [TODO 4](#todo-4--courserepository--khung-service) · [TODO 5](#todo-5--coursedatainitializer--exercise2runner)
- **Part B:** [TODO 6](#todo-6--count-findallsort-findbyid) · [TODO 7](#todo-7--điều-hướng-quan-hệ-2-chiều)
- **Part C:** [TODO 8](#todo-8--findbycode-findbysemester-countbysemester) · [TODO 9](#todo-9--nested-property-qua-collection-courses_code) · [TODO 10](#todo-10--từ-phía-inverse--distinct) · [TODO 11](#todo-11--isempty--existsbyand)
- **Part D:** [TODO 12](#todo-12--jpql-join-collection) · [TODO 13](#todo-13--thống-kê-khóa-học-left-join--group-by--dto) · [TODO 14](#todo-14--group-by--sum--having) · [TODO 15](#todo-15--size) · [TODO 16](#todo-16--lazyinitializationexception-join-fetch-entitygraph) · [TODO 17](#todo-17--native-sql-trên-bảng-trung-gian) · [TODO 18](#todo-18--interface-projection-bảng-đăng-ký) · [TODO 19](#todo-19--phân-trang-với-join)
- **Bonus:** [TODO 25](#todo-25-bonus--specification-với-join--distinct)
- **Part E:** [TODO 20](#todo-20--đăng-ký-khóa-học-enroll) · [TODO 21](#todo-21--huỷ-đăng-ký-unenroll) · [TODO 22](#todo-22--đổi-lớp-trong-1-transaction-rollback) · [TODO 23](#todo-23--xoá-khóa-học-an-toàn) · [TODO 24](#todo-24--xoá-hàng-loạt-trên-bảng-trung-gian)
- [Tổng hợp code hoàn chỉnh](#tổng-hợp-code-hoàn-chỉnh) · [Bảng lỗi thường gặp](#bảng-lỗi-thường-gặp)

---

## Bước 0 — Chuẩn bị

1. Mở project **Exercise 1** đã hoàn thành, chạy thử một lần để chắc chắn vẫn chạy được.
2. Database: **không cần tạo mới**, vẫn dùng `HSF302_CH4`. Vì `ddl-auto=create`, Hibernate sẽ tự tạo thêm bảng `courses` và `student_courses`.
3. Đảm bảo working tree sạch trước khi tạo nhánh:
   ```bash
   git status          # phải là "nothing to commit, working tree clean"
   git log --oneline -3
   ```

---

## Kiến thức nền: `@ManyToMany` trong 30 giây

```
students                 student_courses                 courses
┌────┬───────┐          ┌────────────┬───────────┐        ┌────┬────────┐
│ id │ code  │          │ student_id │ course_id │        │ id │ code   │
├────┼───────┤          ├────────────┼───────────┤        ├────┼────────┤
│ 1  │ SE001 │◄─────────│     1      │     1     │───────►│ 1  │ PRJ301 │
│    │       │◄─────────│     1      │     2     │───────►│ 2  │ HSF302 │
│ 2  │ SE002 │◄─────────│     2      │     1     │        │    │        │
└────┴───────┘          └────────────┴───────────┘        └────┴────────┘
```

| Khái niệm | Trong bài này |
|---|---|
| Bảng trung gian (join table) | `student_courses(student_id, course_id)` — PK ghép, 2 FK |
| **Owning side** — phía có `@JoinTable` | `Student.courses` → **chỉ thay đổi ở phía này mới được ghi xuống `student_courses`** |
| **Inverse side** — phía có `mappedBy` | `Course.students` → chỉ để đọc/điều hướng trong Java |
| Fetch mặc định | `LAZY` ở cả 2 phía |
| Kiểu collection | `Set` (không trùng lặp, Hibernate xoá/ghi hiệu quả hơn `List` — với `List` (bag), gỡ 1 phần tử Hibernate sẽ xoá hết rồi insert lại) |

> **Chọn owning side thế nào?** Chọn phía mà nghiệp vụ "chủ động" thay đổi quan hệ: *sinh viên đăng ký khóa học* → `Student` là owning side.

---

# Part A — Mapping Many-To-Many

## TODO 1 — Nhánh `exercise2` & profile

**Mục tiêu:** Làm Exercise 2 trên nhánh riêng; `ExerciseRunner` của Exercise 1 không chạy nữa (vì Part E của nó xoá/sửa dữ liệu), nhưng vẫn giữ lại để chạy khi cần.

**Các bước**

1. Tạo nhánh:
   ```bash
   git checkout -b exercise2
   ```
2. Mở `runner/ExerciseRunner.java` (của Exercise 1), thêm `@Profile("ex1")`:
   ```java
   import org.springframework.context.annotation.Profile;

   @Component
   @Order(2)
   @Profile("ex1")                 // chỉ chạy khi profile "ex1" được bật
   @RequiredArgsConstructor
   public class ExerciseRunner implements CommandLineRunner {
       // ... giữ nguyên
   }
   ```
3. Thêm vào cuối `application.properties`:
   ```properties
   # Chọn bài chạy: ex1 = ExerciseRunner (Exercise 1), ex2 = Exercise2Runner (Exercise 2)
   spring.profiles.active=ex2
   ```
   Các cấu hình khác **giữ nguyên** (`databaseName=HSF302_CH4`, `ddl-auto=create`, `show-sql=true`, `format_sql=true`).

**Giải thích**
- `@Profile("ex1")`: bean chỉ được tạo khi profile `ex1` đang active. Với `spring.profiles.active=ex2`, `ExerciseRunner` **không** được tạo ⇒ không chạy.
- `DataInitializer` (Exercise 1) **không** gắn profile ⇒ luôn chạy, luôn seed 4 department + 10 student.
- Muốn chạy lại Exercise 1: đổi thành `spring.profiles.active=ex1` (hoặc thêm VM option `-Dspring.profiles.active=ex1`).

**Kiểm tra:** Chạy app → console chỉ có `>>> Seeded 4 departments, 10 students`, không còn in các TODO của Exercise 1.

**📌 Commit TODO 1**

```bash
git add .
git commit -m "chore(setup): create exercise2 branch and profile-based runners" \
           -m "ExerciseRunner cũ chỉ chạy với profile ex1, mặc định active profile ex2" \
           -m "Refs: EX2 TODO 1"
```

---

## TODO 2 — Entity `Course` (inverse side)

**Mục tiêu:** Tạo phía **inverse** của quan hệ N–N.

**Các bước**
1. Tạo `pojo/Course.java`: `@Entity`, `@Table(name = "courses")`, các field.
2. Khai báo `Set<Student> students` với `@ManyToMany(mappedBy = "courses")`.
3. Override `equals/hashCode` theo `code`, `toString()` không chứa `students`.

**Code**

```java
package com.hsf302.ch4.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false)
    private Integer capacity;              // số chỗ tối đa

    @Column(nullable = false, length = 10)
    private String semester;               // "FA26", "SU26"...

    // Inverse side: "courses" là TÊN FIELD bên Student
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();

    public Course(String code, String name, int credits, int capacity, String semester) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.capacity = capacity;
        this.semester = semester;
    }

    // equals/hashCode theo BUSINESS KEY (code) — không dùng id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course other)) return false;
        return code != null && code.equals(other.getCode());   // dùng getter: an toàn với Hibernate proxy
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return String.format("%s | %-40s | %d credits | cap %d | %s",
                code, name, credits, capacity, semester);        // KHÔNG in students
    }
}
```

**Giải thích**
- `mappedBy = "courses"`: *thông tin bảng trung gian đã được khai báo ở field `courses` của `Student`*. Phía `mappedBy` **không** tạo bảng/cột nào, và **thay đổi trên `Course.students` sẽ bị Hibernate bỏ qua** khi ghi xuống DB.
- **Vì sao `Set` + `equals/hashCode`?** `HashSet` dùng `hashCode/equals` để loại trùng. Nếu dùng mặc định (so sánh địa chỉ object), 2 object cùng `code` nạp ở 2 lần khác nhau sẽ bị coi là khác nhau.
- **Vì sao không dùng `id`?** Entity mới `new Course(...)` có `id = null` → sau khi `save()` id thay đổi → `hashCode` thay đổi khi object **đang nằm trong `HashSet`** → không tìm/xoá được nữa. `code` được gán ngay từ đầu và không đổi ⇒ là business key ổn định.
- `other.getCode()` thay vì `other.code`: nếu `other` là **Hibernate proxy** (lazy), field của proxy luôn `null`, chỉ getter mới kích hoạt nạp dữ liệu.
- Không dùng `@Data`: `hashCode/toString` do Lombok sinh sẽ duyệt `students` → `Student` lại duyệt `courses`… ⇒ `StackOverflowError`/`LazyInitializationException`.

**📌 Commit TODO 2**

```bash
git add .
git commit -m "feat(entity): add Course entity as inverse side" \
           -m "Course dùng @ManyToMany(mappedBy = \"courses\"), equals/hashCode theo code" \
           -m "Refs: EX2 TODO 2"
```

---

## TODO 3 — Sửa `Student` (owning side)

**Mục tiêu:** Khai báo bảng trung gian ở phía **owning** và viết helper đồng bộ 2 chiều.

**Các bước**
1. Mở `pojo/Student.java` (Exercise 1), thêm field `courses` với `@ManyToMany` + `@JoinTable`.
2. Thêm helper `enroll(Course)`, `unenroll(Course)`.
3. Thêm `equals/hashCode` theo `studentCode`. `toString()` giữ nguyên (không chứa `department`, `courses`).

**Code — phần thêm vào `Student`**

```java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Student {

    // ... các field của Exercise 1 giữ nguyên (id, studentCode, fullName, email, gender, dob, gpa, active, department)

    // Owning side: Student quản lý bảng trung gian student_courses
    @ManyToMany                                        // fetch mặc định LAZY, KHÔNG cascade
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),          // FK → students.id (phía hiện tại)
        inverseJoinColumns = @JoinColumn(name = "course_id")     // FK → courses.id (phía bên kia)
    )
    private Set<Course> courses = new HashSet<>();

    // ===== Helper đồng bộ 2 chiều =====
    public void enroll(Course c) {
        courses.add(c);                 // owning side → Hibernate INSERT vào student_courses
        c.getStudents().add(this);      // inverse side → giữ object Java nhất quán
    }

    public void unenroll(Course c) {
        courses.remove(c);              // owning side → Hibernate DELETE khỏi student_courses
        c.getStudents().remove(this);
    }

    // equals/hashCode theo business key studentCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student other)) return false;
        return studentCode != null && studentCode.equals(other.getStudentCode());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(studentCode);
    }

    // toString() của Exercise 1 giữ nguyên — KHÔNG thêm courses
}
```

**Giải thích**
- `@JoinTable.name`: tên bảng trung gian. `joinColumns`: cột FK trỏ về **entity đang khai báo** (`Student`). `inverseJoinColumns`: cột FK trỏ về **entity bên kia** (`Course`). Nếu bỏ `@JoinTable`, Hibernate đặt tên mặc định `students_courses(students_id, courses_id)` — khó đọc.
- **Không cascade**: `CascadeType.REMOVE` trên N–N rất nguy hiểm — xoá 1 sinh viên sẽ xoá luôn các khóa học (mà sinh viên khác vẫn đang học). `PERSIST` cũng không cần vì course được lưu riêng.
- **Helper 2 chiều**: về mặt DB chỉ cần `courses.add(c)`. Nhưng nếu không cập nhật `c.getStudents()`, trong cùng transaction `course.getStudents()` sẽ **thiếu** sinh viên vừa thêm (dữ liệu Java lệch với DB) ⇒ luôn dùng helper, không gọi `getCourses().add()` trực tiếp.
- Với `Set`, Hibernate tạo bảng trung gian có **khoá chính ghép** `(student_id, course_id)` ⇒ DB tự chặn đăng ký trùng.

**Kiểm tra:** Chạy app → console có:
```sql
create table courses (id bigint identity not null, capacity int not null, code varchar(10) not null, ...)
create table student_courses (course_id bigint not null, student_id bigint not null, primary key (course_id, student_id))
alter table student_courses add constraint FK... foreign key (course_id) references courses
alter table student_courses add constraint FK... foreign key (student_id) references students
```

**📌 Commit TODO 3**

```bash
git add .
git commit -m "feat(entity): map many-to-many between Student and Course" \
           -m "Student là owning side với @JoinTable student_courses, helper enroll/unenroll" \
           -m "Refs: EX2 TODO 3"
```

---

## TODO 4 — `CourseRepository` & khung Service

### 4.1 Repository

```java
package com.hsf302.ch4.repository;

import com.hsf302.ch4.pojo.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // bổ sung dần từ TODO 7
}
```

> **Không tạo repository cho `student_courses`** — bảng này không có entity, được quản lý qua `Student.courses`.

### 4.2 Service (interface + implementation)

```
Exercise2Runner ──► CourseService / EnrollmentService           (interface)
                          ▲ implements
                   CourseServiceImpl / EnrollmentServiceImpl        (@Service, @Transactional)
                          │
                   CourseRepository / StudentRepository
```

- `CourseService`: nghiệp vụ trên **khóa học** (tìm, thống kê, xoá khóa học).
- `EnrollmentService`: nghiệp vụ trên **quan hệ đăng ký** (khóa học của sinh viên, sinh viên của khóa học, đăng ký, huỷ, đổi lớp…).

```java
package com.hsf302.ch4.service;

public interface CourseService {
    // bổ sung dần từ TODO 6
}
```

```java
package com.hsf302.ch4.service;

public interface EnrollmentService {
    // bổ sung dần từ TODO 7
}
```

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    // cài đặt dần từ TODO 6
}
```

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.CourseRepository;
import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // cài đặt dần từ TODO 7
}
```

**Giải thích**
- Giống Exercise 1: `@Service` đặt trên **class implementation**, `@Transactional(readOnly = true)` ở mức class; method ghi (Part E) gắn thêm `@Transactional`.
- Import `org.springframework.transaction.annotation.Transactional` (có `readOnly`), không dùng bản `jakarta.transaction`.
- `EnrollmentServiceImpl` cần **cả hai** repository vì nghiệp vụ đăng ký luôn chạm cả `Student` và `Course`.

**📌 Commit TODO 4**

```bash
git add .
git commit -m "feat(repository): add CourseRepository and enrollment service skeleton" \
           -m "Refs: EX2 TODO 4"
```

---

## TODO 5 — `CourseDataInitializer` & `Exercise2Runner`

### 5.1 `CourseDataInitializer`

**Các bước**
1. Tạo `runner/CourseDataInitializer.java`, `@Component`, `@Order(2)` (chạy **sau** `DataInitializer` `@Order(1)`).
2. Gắn `@Transactional` cho `run()`.
3. Tạo 6 course, `saveAll()`, rồi dùng helper `enroll()` của `Student` để đăng ký.

```java
package com.hsf302.ch4.runner;

import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.CourseRepository;
import com.hsf302.ch4.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class CourseDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional                       // BẮT BUỘC: s.getCourses() là LAZY
    public void run(String... args) {
        if (courseRepository.count() > 0) return;

        Course prj = new Course("PRJ301", "Java Web Application Development",       3, 5, "FA26");
        Course hsf = new Course("HSF302", "Hibernate & Spring Framework",           3, 6, "FA26");
        Course swp = new Course("SWP391", "Software Development Project",           4, 4, "FA26");
        Course ail = new Course("AIL303", "Machine Learning",                       3, 4, "FA26");
        Course iaa = new Course("IAA202", "Risk Management in Information Systems", 3, 4, "SU26");
        Course mkt = new Course("MKT101", "Marketing Principles",                   2, 4, "SU26");
        courseRepository.saveAll(List.of(prj, hsf, swp, ail, iaa, mkt));

        enroll("SE001", prj, hsf, swp);
        enroll("SE002", prj, hsf, ail);
        enroll("SE003", prj);
        enroll("SE004", hsf, swp);
        enroll("AI001", ail, hsf);
        enroll("AI002", ail);
        enroll("AI003", ail, prj, swp);
        enroll("IA001", iaa);
        enroll("IA002", iaa, hsf);
        // IA003 (Do Van Nam) chưa đăng ký khóa nào; MKT101 chưa có sinh viên

        System.out.println(">>> Seeded " + courseRepository.count() + " courses");
        // Không cần save student: student đang được quản lý (managed) → dirty checking
        // sẽ INSERT 18 dòng vào student_courses khi transaction commit.
    }

    private void enroll(String studentCode, Course... courses) {
        Student s = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new IllegalStateException("Missing student " + studentCode));
        for (Course c : courses) {
            s.enroll(c);
        }
    }
}
```

**Giải thích**
- **Vì sao cần `@Transactional`?** Student được nạp bằng `findByStudentCode` — `courses` là LAZY. Không có transaction, persistence context đóng ngay sau khi repository trả kết quả ⇒ `s.getCourses().add(...)` ném `LazyInitializationException`. Có transaction: student ở trạng thái **managed** suốt method, collection nạp được, và mọi thay đổi được **flush** khi commit.
- `@Transactional` hoạt động trên `run()` vì Spring gọi runner thông qua **proxy** của bean.
- `findByStudentCode` là method derived đã viết ở Exercise 1 (TODO 8).
- `saveAll(courses)` trước để course có `id` — bảng trung gian cần `course_id`.

### 5.2 Khung `Exercise2Runner`

```java
package com.hsf302.ch4.runner;

import com.hsf302.ch4.service.CourseService;
import com.hsf302.ch4.service.EnrollmentService;
import com.hsf302.ch4.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Order(3)
@Profile("ex2")
@RequiredArgsConstructor
public class Exercise2Runner implements CommandLineRunner {

    // CHỈ inject Service interface
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final StudentService studentService;          // của Exercise 1 (TODO 16a, 21)

    @Override
    public void run(String... args) {
        partB();
        partC();
        partD();
        bonus();        // chạy trên dữ liệu gốc → trước Part E
        partE();
    }

    private void partB() { todo6(); todo7(); }
    private void partC() { todo8(); todo9(); todo10(); todo11(); }
    private void partD() { todo12(); todo13(); todo14(); todo15(); todo16(); todo17(); todo18(); todo19(); }
    private void bonus() { todo25(); }
    private void partE() { todo20(); todo21(); todo22(); todo23(); todo24(); }

    // ===== helpers =====
    private void title(String t) {
        System.out.println("\n===== " + t + " =====");
    }

    private void printList(String label, Collection<?> list) {
        System.out.println("-- " + label + ":");
        list.forEach(o -> System.out.println("   " + o));
        System.out.println("   -> " + list.size() + " record(s)");
    }

    /** Chạy 1 thao tác ghi, in [OK] hoặc [FAIL] + message (dùng cho Part E). */
    private void attempt(String label, Runnable action) {
        try {
            action.run();
            System.out.println("   [OK]   " + label);
        } catch (RuntimeException e) {
            System.out.println("   [FAIL] " + label + " -> " + e.getMessage());
        }
    }

    // todo6() ... todo25() viết ở các TODO bên dưới
}
```

> 💡 Mẹo: trong lúc làm, có thể tạm comment các `todoX()` chưa viết trong `partB()...partE()` để project biên dịch được.

**Kiểm tra:** Console có `>>> Seeded 4 departments, 10 students` rồi `>>> Seeded 6 courses`. Trong SSMS:
```sql
SELECT COUNT(*) FROM student_courses;                    -- 18
SELECT s.student_code, c.code
FROM student_courses sc
JOIN students s ON s.id = sc.student_id
JOIN courses  c ON c.id = sc.course_id
ORDER BY s.student_code, c.code;
```

**📌 Commit TODO 5**

```bash
git add .
git commit -m "feat(data): seed courses and student enrollments" \
           -m "Thêm CourseDataInitializer @Order(2) và khung Exercise2Runner @Order(3)" \
           -m "Refs: EX2 TODO 5"
```

---

# Part B — Method có sẵn & điều hướng quan hệ

## TODO 6 — `count()`, `findAll(Sort)`, `findById()`

**Service — `CourseService`**
```java
long count();
List<Course> findAllOrderByCode();
Optional<Course> findById(Long id);
```

**Service — `CourseServiceImpl`**
```java
@Override
public long count() {
    return courseRepository.count();
}

@Override
public List<Course> findAllOrderByCode() {
    return courseRepository.findAll(Sort.by("code"));
}

@Override
public Optional<Course> findById(Long id) {
    return courseRepository.findById(id);
}
```

**Runner**
```java
private void todo6() {
    title("TODO 6: count, findAll(Sort), findById");
    System.out.println("Total courses: " + courseService.count());
    printList("All courses order by code", courseService.findAllOrderByCode());
    for (long id : new long[]{2L, 99L}) {
        System.out.println("findById(" + id + "): "
                + courseService.findById(id).map(Course::toString).orElse("Not found"));
    }
}
```

**Kết quả mong đợi**
```
Total courses: 6
-- All courses order by code:
   AIL303 | Machine Learning                         | 3 credits | cap 4 | FA26
   HSF302 | Hibernate & Spring Framework             | 3 credits | cap 6 | FA26
   IAA202 | Risk Management in Information Systems   | 3 credits | cap 4 | SU26
   MKT101 | Marketing Principles                     | 2 credits | cap 4 | SU26
   PRJ301 | Java Web Application Development         | 3 credits | cap 5 | FA26
   SWP391 | Software Development Project             | 4 credits | cap 4 | FA26
   -> 6 record(s)
findById(2): HSF302 | Hibernate & Spring Framework ...
findById(99): Not found
```

**📌 Commit TODO 6**
```bash
git add .
git commit -m "feat(builtin): count, sort and find courses by id" -m "Refs: EX2 TODO 6"
```

---

## TODO 7 — Điều hướng quan hệ 2 chiều

**Mục tiêu:** Đọc `student.getCourses()` (owning) và `course.getStudents()` (inverse) — collection LAZY — **bên trong transaction của Service**.

**Repository — `CourseRepository`** (derived, dùng lại ở TODO 8 và Part E)
```java
Optional<Course> findByCode(String code);
```

**Service — `EnrollmentService`**
```java
List<Course> getCoursesOfStudent(String studentCode);
List<Student> getStudentsOfCourse(String courseCode);
```

**Service — `EnrollmentServiceImpl`**
```java
@Override
public List<Course> getCoursesOfStudent(String studentCode) {
    Student s = getStudent(studentCode);
    return s.getCourses().stream()                       // nạp LAZY: vẫn trong transaction → OK
            .sorted(Comparator.comparing(Course::getCode))
            .toList();
}

@Override
public List<Student> getStudentsOfCourse(String courseCode) {
    Course c = getCourse(courseCode);
    return c.getStudents().stream()                      // inverse side vẫn ĐỌC được bình thường
            .sorted(Comparator.comparing(Student::getFullName))
            .toList();
}

// ===== helper dùng chung cho mọi method =====
private Student getStudent(String studentCode) {
    if (studentCode == null || studentCode.isBlank()) {
        throw new IllegalArgumentException("Student code must not be blank");
    }
    return studentRepository.findByStudentCode(studentCode)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentCode));
}

private Course getCourse(String courseCode) {
    if (courseCode == null || courseCode.isBlank()) {
        throw new IllegalArgumentException("Course code must not be blank");
    }
    return courseRepository.findByCode(courseCode)
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseCode));
}
```
> Import thêm: `java.util.Comparator`, `java.util.List`.

**Runner**
```java
private void todo7() {
    title("TODO 7: navigate student.getCourses() / course.getStudents()");
    printList("(a) Courses of SE001", enrollmentService.getCoursesOfStudent("SE001"));
    printList("(b) Students of AIL303", enrollmentService.getStudentsOfCourse("AIL303"));
}
```

**Giải thích**
- `getCourses()` được gọi **trong** method của Service (có `@Transactional(readOnly = true)` ở class) ⇒ Hibernate chạy thêm 1 câu SELECT JOIN `student_courses` để nạp collection.
- Service trả về `List<Course>` đã sắp xếp — Runner chỉ in `Course.toString()` (không chạm `students`) ⇒ không lỗi Lazy.
- `.toList()` có từ Java 16 (JDK 17 dùng được).

**Kết quả mong đợi**
```
-- (a) Courses of SE001:  HSF302, PRJ301, SWP391            -> 3 record(s)
-- (b) Students of AIL303: Hoang Van Em, Pham Thi Dung, Tran Thi Binh, Vo Thi Hoa   -> 4 record(s)
```

**📌 Commit TODO 7**
```bash
git add .
git commit -m "feat(enrollment): navigate courses of student and students of course" -m "Refs: EX2 TODO 7"
```

---

# Part C — Derived query trên quan hệ N–N

> Quy tắc: **chỉ đặt tên method**, không `@Query`. Đi qua collection bằng tên field: `Courses_Code` = `s.courses` → `c.code`. Dấu `_` giúp Spring tách property rõ ràng.

## TODO 8 — `findByCode`, `findBySemester`, `countBySemester`

**Repository — `CourseRepository`**
```java
Optional<Course> findByCode(String code);                     // đã thêm ở TODO 7
List<Course> findBySemesterOrderByCodeAsc(String semester);
long countBySemester(String semester);
```

**Service — `CourseService` / `CourseServiceImpl`**
```java
Optional<Course> findByCode(String code);
List<Course> findBySemester(String semester);
long countBySemester(String semester);
```
```java
@Override
public Optional<Course> findByCode(String code) {
    return courseRepository.findByCode(code);
}

@Override
public List<Course> findBySemester(String semester) {
    return courseRepository.findBySemesterOrderByCodeAsc(semester);
}

@Override
public long countBySemester(String semester) {
    return courseRepository.countBySemester(semester);
}
```

**Runner**
```java
private void todo8() {
    title("TODO 8: findByCode, findBySemester, countBySemester");
    for (String code : List.of("HSF302", "XXX000")) {
        System.out.println("(a) " + code + ": "
                + courseService.findByCode(code).map(Course::getName).orElse("Not found"));
    }
    printList("(b) Semester SU26", courseService.findBySemester("SU26"));
    System.out.println("(c) Courses in FA26: " + courseService.countBySemester("FA26"));
}
```

**Kết quả mong đợi:** HSF302 → *Hibernate & Spring Framework* · XXX000 → *Not found* · SU26: IAA202, MKT101 · FA26: **4**.

**📌 Commit TODO 8**
```bash
git add .
git commit -m "feat(derived): find courses by code and semester" -m "Refs: EX2 TODO 8"
```

---

## TODO 9 — Nested property qua collection (`Courses_Code`)

**Repository — `StudentRepository`** (bổ sung)
```java
// ===== Exercise 2 =====
List<Student> findByCourses_CodeOrderByFullNameAsc(String courseCode);
long countByCourses_Code(String courseCode);
List<Student> findByCourses_CodeAndActiveTrueOrderByFullNameAsc(String courseCode);
```

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
List<Student> findStudentsInCourse(String courseCode);
long countStudentsInCourse(String courseCode);
List<Student> findActiveStudentsInCourse(String courseCode);
```
```java
@Override
public List<Student> findStudentsInCourse(String courseCode) {
    return studentRepository.findByCourses_CodeOrderByFullNameAsc(courseCode);
}

@Override
public long countStudentsInCourse(String courseCode) {
    return studentRepository.countByCourses_Code(courseCode);
}

@Override
public List<Student> findActiveStudentsInCourse(String courseCode) {
    return studentRepository.findByCourses_CodeAndActiveTrueOrderByFullNameAsc(courseCode);
}
```

**Runner**
```java
private void todo9() {
    title("TODO 9: derived query through collection courses");
    printList("(a) Students of PRJ301", enrollmentService.findStudentsInCourse("PRJ301"));
    System.out.println("(b) Students of HSF302: " + enrollmentService.countStudentsInCourse("HSF302"));
    printList("(c) Active students of PRJ301", enrollmentService.findActiveStudentsInCourse("PRJ301"));
}
```

**Giải thích** — SQL Hibernate sinh cho (a):
```sql
select s.* from students s
join student_courses sc on s.id = sc.student_id
join courses c on c.id = sc.course_id
where c.code = ?
order by s.full_name
```
Spring Data tự **JOIN qua bảng trung gian** — ta chỉ cần biết tên field `courses` và `code`.

**Kết quả mong đợi:** (a) *Le Van Cuong, Nguyen Van An, Tran Thi Binh, Vo Thi Hoa* · (b) **5** · (c) *Nguyen Van An, Tran Thi Binh, Vo Thi Hoa* (Cuong inactive bị loại).

**📌 Commit TODO 9**
```bash
git add .
git commit -m "feat(derived): find students by enrolled course code" -m "Refs: EX2 TODO 9"
```

---

## TODO 10 — Từ phía inverse & `Distinct`

**Repository — `CourseRepository`**
```java
List<Course> findByStudents_StudentCodeOrderByCodeAsc(String studentCode);
List<Course> findByStudents_Department_CodeOrderByCodeAsc(String deptCode);          // có thể TRÙNG
List<Course> findDistinctByStudents_Department_CodeOrderByCodeAsc(String deptCode);  // loại trùng
```

**Service — `CourseService` / `CourseServiceImpl`**
```java
List<Course> findCoursesOfStudent(String studentCode);
List<Course> findCoursesOfDepartment(String deptCode, boolean distinct);
```
```java
@Override
public List<Course> findCoursesOfStudent(String studentCode) {
    return courseRepository.findByStudents_StudentCodeOrderByCodeAsc(studentCode);
}

@Override
public List<Course> findCoursesOfDepartment(String deptCode, boolean distinct) {
    return distinct
            ? courseRepository.findDistinctByStudents_Department_CodeOrderByCodeAsc(deptCode)
            : courseRepository.findByStudents_Department_CodeOrderByCodeAsc(deptCode);
}
```

**Runner**
```java
private void todo10() {
    title("TODO 10: derived query from inverse side, Distinct");
    printList("(a) Courses of SE002", courseService.findCoursesOfStudent("SE002"));
    printList("(b1) Courses of AI students - no Distinct", courseService.findCoursesOfDepartment("AI", false));
    printList("(b2) Courses of AI students - Distinct", courseService.findCoursesOfDepartment("AI", true));
}
```

**Giải thích**
- Derived query dùng được ở **cả phía inverse** (`Course.students`) — mapping `mappedBy` vẫn đủ thông tin để JOIN.
- `Students_Department_Code` đi **2 cấp**: `c.students` → `s.department` → `d.code`.
- **Vì sao trùng?** JOIN tạo **1 dòng cho mỗi cặp (course, student)**. AIL303 có 3 sinh viên khoa AI (Dung, Em, Hoa) ⇒ AIL303 xuất hiện 3 lần. Thêm `Distinct` ⇒ SQL `select distinct`.
- Cách làm thay: TODO 7 dùng navigation (`student.getCourses()`), TODO 10a dùng query — kết quả giống nhau, nhưng query **không phải nạp student** trước.

**Kết quả mong đợi**
- (a) AIL303, HSF302, PRJ301
- (b1) **6** dòng: AIL303, AIL303, AIL303, HSF302, PRJ301, SWP391
- (b2) **4** dòng: AIL303, HSF302, PRJ301, SWP391

**📌 Commit TODO 10**
```bash
git add .
git commit -m "feat(derived): find courses by student and department with distinct" -m "Refs: EX2 TODO 10"
```

---

## TODO 11 — `IsEmpty` & `existsBy...And...`

**Repository**
```java
// StudentRepository
List<Student> findByCoursesIsEmptyOrderByFullNameAsc();
boolean existsByStudentCodeAndCourses_Code(String studentCode, String courseCode);

// CourseRepository
List<Course> findByStudentsIsEmpty();
```

**Service**
```java
// CourseService
List<Course> findCoursesWithoutStudents();

// EnrollmentService
List<Student> findStudentsWithoutCourses();
boolean isEnrolled(String studentCode, String courseCode);
```
```java
// CourseServiceImpl
@Override
public List<Course> findCoursesWithoutStudents() {
    return courseRepository.findByStudentsIsEmpty();
}

// EnrollmentServiceImpl
@Override
public List<Student> findStudentsWithoutCourses() {
    return studentRepository.findByCoursesIsEmptyOrderByFullNameAsc();
}

@Override
public boolean isEnrolled(String studentCode, String courseCode) {
    return studentRepository.existsByStudentCodeAndCourses_Code(studentCode, courseCode);
}
```

**Runner**
```java
private void todo11() {
    title("TODO 11: IsEmpty, existsBy...And...");
    printList("(a) Students without courses", enrollmentService.findStudentsWithoutCourses());
    printList("(b) Courses without students", courseService.findCoursesWithoutStudents());
    System.out.println("(c) SE001 enrolled AIL303? " + enrollmentService.isEnrolled("SE001", "AIL303"));
    System.out.println("    SE002 enrolled AIL303? " + enrollmentService.isEnrolled("SE002", "AIL303"));
}
```

**Giải thích**
- `IsEmpty` trên collection ⇒ Hibernate sinh `where not exists (select 1 from student_courses sc where sc.student_id = s.id)`.
- `existsBy...` ⇒ Hibernate chỉ lấy **tối đa 1 dòng** (không nạp entity) và trả `boolean` — nhẹ hơn nhiều so với nạp entity rồi kiểm tra.

**Kết quả mong đợi:** (a) *Do Van Nam* · (b) *MKT101* · (c) `false` · `true`.

**📌 Commit TODO 11**
```bash
git add .
git commit -m "feat(derived): find unenrolled students and empty courses" -m "Refs: EX2 TODO 11"
```

---

# Part D — Custom query với `@Query`

> JPQL JOIN collection bằng **đường dẫn field**: `JOIN s.courses c` — không viết điều kiện `ON` và **không** nhắc tới bảng `student_courses`.

## TODO 12 — JPQL JOIN collection

**Repository — `StudentRepository`**
```java
@Query("SELECT s FROM Student s JOIN s.courses c " +
       "WHERE c.code = :code AND s.gpa >= :minGpa ORDER BY s.gpa DESC")
List<Student> findGoodStudentsInCourse(@Param("code") String courseCode,
                                       @Param("minGpa") double minGpa);
```

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
List<Student> findGoodStudentsInCourse(String courseCode, double minGpa);
```
```java
@Override
public List<Student> findGoodStudentsInCourse(String courseCode, double minGpa) {
    if (minGpa < 0 || minGpa > 4) {
        throw new IllegalArgumentException("minGpa must be in [0, 4]");
    }
    return studentRepository.findGoodStudentsInCourse(courseCode, minGpa);
}
```

**Runner**
```java
private void todo12() {
    title("TODO 12: JPQL JOIN s.courses");
    printList("HSF302 & GPA >= 3.5", enrollmentService.findGoodStudentsInCourse("HSF302", 3.5));
}
```

**Kết quả mong đợi:** Binh 3.8, Mai 3.6, Dung 3.5.

**📌 Commit TODO 12**
```bash
git add .
git commit -m "feat(query): find good students of course with jpql join" -m "Refs: EX2 TODO 12"
```

---

## TODO 13 — Thống kê khóa học: `LEFT JOIN` + `GROUP BY` + DTO

**DTO — `dto/CourseStatDTO.java`**
```java
package com.hsf302.ch4.dto;

public record CourseStatDTO(String code, String name, Integer capacity,
                            Long enrolled, Double avgGpa) {

    public long remaining() {                 // số chỗ còn trống
        return capacity - enrolled;
    }
}
```

**Repository — `CourseRepository`**
```java
@Query("SELECT new com.hsf302.ch4.dto.CourseStatDTO(c.code, c.name, c.capacity, COUNT(s), AVG(s.gpa)) " +
       "FROM Course c LEFT JOIN c.students s " +
       "GROUP BY c.code, c.name, c.capacity ORDER BY c.code")
List<CourseStatDTO> getCourseStats();
```

**Service — `CourseService` / `CourseServiceImpl`**
```java
List<CourseStatDTO> getStatistics();
```
```java
@Override
public List<CourseStatDTO> getStatistics() {
    return courseRepository.getCourseStats();
}
```

**Runner** (tách hàm in để TODO 24 dùng lại)
```java
private void todo13() {
    title("TODO 13: course statistics (LEFT JOIN + GROUP BY + DTO)");
    printCourseStats();
}

private void printCourseStats() {
    courseService.getStatistics().forEach(d -> System.out.printf(
            "   %-6s | %-40s | %d/%d (free %d) | avg GPA %s%n",
            d.code(), d.name(), d.enrolled(), d.capacity(), d.remaining(),
            d.avgGpa() == null ? "null" : String.format("%.3f", d.avgGpa())));
}
```

**Giải thích**
- `LEFT JOIN c.students s`: giữ cả khóa **chưa có sinh viên** (MKT101). Dùng `JOIN` thường sẽ mất MKT101.
- `COUNT(s)` (không phải `COUNT(*)`): với LEFT JOIN, khóa không có sinh viên vẫn có 1 dòng với `s = null` → `COUNT(s)` = 0, còn `COUNT(*)` = 1 (sai).
- Kiểu tham số constructor phải khớp: `COUNT` → `Long`, `AVG` → `Double`, `c.capacity` → `Integer`.
- Mọi cột không nằm trong hàm tổng hợp phải có trong `GROUP BY` (SQL Server bắt buộc).

**Kết quả mong đợi**
```
   AIL303 | Machine Learning                         | 4/4 (free 0) | avg GPA 3.500
   HSF302 | Hibernate & Spring Framework             | 5/6 (free 1) | avg GPA 3.440
   IAA202 | Risk Management in Information Systems   | 2/4 (free 2) | avg GPA 2.500
   MKT101 | Marketing Principles                     | 0/4 (free 4) | avg GPA null
   PRJ301 | Java Web Application Development         | 4/5 (free 1) | avg GPA 3.350
   SWP391 | Software Development Project             | 3/4 (free 1) | avg GPA 3.567
```

**📌 Commit TODO 13**
```bash
git add .
git commit -m "feat(query): add course statistics with dto projection" -m "Refs: EX2 TODO 13"
```

---

## TODO 14 — `GROUP BY` + `SUM` + `HAVING`

**DTO — `dto/StudentCreditDTO.java`**
```java
package com.hsf302.ch4.dto;

public record StudentCreditDTO(String studentCode, String fullName,
                               Long courseCount, Long totalCredits) {
}
```

**Repository — `StudentRepository`**
```java
@Query("SELECT new com.hsf302.ch4.dto.StudentCreditDTO(s.studentCode, s.fullName, COUNT(c), SUM(c.credits)) " +
       "FROM Student s JOIN s.courses c " +
       "GROUP BY s.studentCode, s.fullName " +
       "HAVING SUM(c.credits) >= :minCredits " +
       "ORDER BY SUM(c.credits) DESC, s.fullName")
List<StudentCreditDTO> getCreditSummary(@Param("minCredits") long minCredits);
```

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
List<StudentCreditDTO> getCreditSummary(int minCredits);
```
```java
@Override
public List<StudentCreditDTO> getCreditSummary(int minCredits) {
    if (minCredits < 0) {
        throw new IllegalArgumentException("minCredits must be >= 0");
    }
    return studentRepository.getCreditSummary(minCredits);
}
```

**Runner**
```java
private void todo14() {
    title("TODO 14: total credits per student (GROUP BY + HAVING)");
    enrollmentService.getCreditSummary(7).forEach(d -> System.out.printf(
            "   %s | %-15s | %d course(s) | %d credits%n",
            d.studentCode(), d.fullName(), d.courseCount(), d.totalCredits()));
}
```

**Giải thích**
- `WHERE` lọc **từng dòng trước** khi gom nhóm; `HAVING` lọc **nhóm sau** khi đã tính `SUM` ⇒ điều kiện trên hàm tổng hợp phải đặt ở `HAVING`.
- `SUM` của `Integer` trả về `Long` ⇒ tham số `:minCredits` khai báo kiểu `long` để Hibernate 6 không báo lệch kiểu.

**Kết quả mong đợi**
```
   SE001 | Nguyen Van An   | 3 course(s) | 10 credits
   AI003 | Vo Thi Hoa      | 3 course(s) | 10 credits
   SE002 | Tran Thi Binh   | 3 course(s) | 9 credits
   SE004 | Nguyen Thi Mai  | 2 course(s) | 7 credits
```

**📌 Commit TODO 14**
```bash
git add .
git commit -m "feat(query): summarize student credits with group by having" -m "Refs: EX2 TODO 14"
```

---

## TODO 15 — `SIZE()`

**Repository**
```java
// CourseRepository
@Query("SELECT c FROM Course c WHERE SIZE(c.students) >= c.capacity ORDER BY c.code")
List<Course> findFullCourses();

// StudentRepository
@Query("SELECT s FROM Student s WHERE SIZE(s.courses) > :n ORDER BY s.fullName")
List<Student> findStudentsWithMoreThanNCourses(@Param("n") int n);
```

**Service**
```java
// CourseService
List<Course> findFullCourses();

// EnrollmentService
List<Student> findStudentsWithMoreThan(int n);
```
```java
// CourseServiceImpl
@Override
public List<Course> findFullCourses() {
    return courseRepository.findFullCourses();
}

// EnrollmentServiceImpl
@Override
public List<Student> findStudentsWithMoreThan(int n) {
    if (n < 0) {
        throw new IllegalArgumentException("n must be >= 0");
    }
    return studentRepository.findStudentsWithMoreThanNCourses(n);
}
```

**Runner**
```java
private void todo15() {
    title("TODO 15: SIZE() on collections");
    printList("(a) Full courses", courseService.findFullCourses());
    printList("(b) Students with more than 2 courses", enrollmentService.findStudentsWithMoreThan(2));
}
```

**Giải thích:** `SIZE(c.students)` là hàm JPQL trên collection; Hibernate dịch thành subquery `(select count(*) from student_courses sc where sc.course_id = c.id)` — không cần `GROUP BY`.

**Kết quả mong đợi:** (a) *AIL303* · (b) *Nguyen Van An, Tran Thi Binh, Vo Thi Hoa*.

**📌 Commit TODO 15**
```bash
git add .
git commit -m "feat(query): find full courses and busy students with size" -m "Refs: EX2 TODO 15"
```

---

## TODO 16 — `LazyInitializationException`, `JOIN FETCH`, `@EntityGraph`

**Mục tiêu:** Thấy lỗi Lazy khi truy cập collection N–N ngoài transaction, và 2 cách nạp sẵn collection.

**Repository**
```java
// StudentRepository
@Query("SELECT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.studentCode = :code")
Optional<Student> findByStudentCodeWithCourses(@Param("code") String studentCode);

// CourseRepository
@EntityGraph(attributePaths = "students")
Optional<Course> findWithStudentsByCode(String code);
```
> Import: `org.springframework.data.jpa.repository.EntityGraph`.

**Service**
```java
// EnrollmentService
Student getStudentWithCourses(String studentCode);

// CourseService
Course getWithStudents(String code);
```
```java
// EnrollmentServiceImpl
@Override
public Student getStudentWithCourses(String studentCode) {
    return studentRepository.findByStudentCodeWithCourses(studentCode)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentCode));
}

// CourseServiceImpl
@Override
public Course getWithStudents(String code) {
    return courseRepository.findWithStudentsByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + code));
}
```

**Runner**
```java
private void todo16() {
    title("TODO 16: LazyInitializationException, JOIN FETCH, @EntityGraph");

    // (a) Student trả về từ Service (Exercise 1) → transaction đã đóng → courses chưa được nạp
    try {
        Student s = studentService.findByStudentCode("SE001").orElseThrow();
        System.out.println("(a) courses = " + s.getCourses().size());
    } catch (LazyInitializationException e) {
        System.out.println("(a) Caught: " + e.getClass().getSimpleName());
        System.out.println("    " + e.getMessage());
    }

    // (b) JOIN FETCH: nạp student + courses trong 1 câu SQL
    Student s = enrollmentService.getStudentWithCourses("SE001");
    System.out.println("(b) " + s.getStudentCode() + " - " + s.getFullName());
    s.getCourses().stream()
            .sorted(Comparator.comparing(Course::getCode))
            .forEach(c -> System.out.println("   " + c));

    // (c) @EntityGraph: nạp course + students
    Course c = courseService.getWithStudents("SWP391");
    System.out.println("(c) " + c.getCode() + " - " + c.getName());
    c.getStudents().stream()
            .sorted(Comparator.comparing(Student::getFullName))
            .forEach(st -> System.out.println("   " + st));
}
```
> Import: `org.hibernate.LazyInitializationException`, `java.util.Comparator`.

**Giải thích**
- (a) `findByStudentCode` chạy trong transaction readOnly của `StudentServiceImpl`; khi trả về Runner, persistence context đã đóng, `courses` chỉ là `PersistentSet` **chưa khởi tạo** ⇒ gọi `size()` ném `LazyInitializationException: failed to lazily initialize a collection of role: com.hsf302.ch4.pojo.Student.courses ... could not initialize proxy - no Session`.
- (b) `LEFT JOIN FETCH s.courses` ⇒ 1 câu SELECT join 3 bảng, `courses` đã khởi tạo sẵn ⇒ dùng được ngoài transaction. `LEFT` để sinh viên không có khóa nào vẫn trả về. Hibernate 6 **tự loại trùng** root entity khi fetch join nên không cần `DISTINCT`.
- (c) `@EntityGraph(attributePaths = "students")`: cách **khai báo** thay cho viết `JOIN FETCH` — Spring Data thêm fetch join vào derived query `findWithStudentsByCode` (phần `WithStudents` giữa `find` và `By` chỉ là mô tả, Spring bỏ qua).
- ⚠️ Không fetch join **2 collection** cùng lúc bằng `List` (lỗi `MultipleBagFetchException`) — đây là thêm một lý do dùng `Set`.

**Kết quả mong đợi**
```
(a) Caught: LazyInitializationException
(b) SE001 - Nguyen Van An:  HSF302, PRJ301, SWP391
(c) SWP391 - Software Development Project:  Nguyen Thi Mai, Nguyen Van An, Vo Thi Hoa
```

**📌 Commit TODO 16**
```bash
git add .
git commit -m "fix(query): load collections with join fetch and entity graph" \
           -m "Truy cập Student.courses ngoài transaction gây LazyInitializationException" \
           -m "Refs: EX2 TODO 16"
```

---

## TODO 17 — Native SQL trên bảng trung gian

**Projection — `dto/CourseEnrollmentCount.java`**
```java
package com.hsf302.ch4.dto;

public interface CourseEnrollmentCount {
    String getCode();
    String getName();
    Long getEnrolled();
}
```

**Repository — `CourseRepository`**
```java
@Query(value = "SELECT TOP (:n) c.code AS code, c.name AS name, COUNT(sc.student_id) AS enrolled " +
               "FROM courses c LEFT JOIN student_courses sc ON sc.course_id = c.id " +
               "GROUP BY c.code, c.name " +
               "ORDER BY enrolled DESC, c.code",
       nativeQuery = true)
List<CourseEnrollmentCount> findTopEnrolledNative(@Param("n") int n);
```

**Service — `CourseService` / `CourseServiceImpl`**
```java
List<CourseEnrollmentCount> findTopEnrolled(int n);
```
```java
@Override
public List<CourseEnrollmentCount> findTopEnrolled(int n) {
    if (n <= 0) {
        throw new IllegalArgumentException("n must be > 0");
    }
    return courseRepository.findTopEnrolledNative(n);
}
```

**Runner**
```java
private void todo17() {
    title("TODO 17: native SQL on join table - top 3 enrolled courses");
    courseService.findTopEnrolled(3).forEach(r -> System.out.printf(
            "   %s | %-35s | %d student(s)%n", r.getCode(), r.getName(), r.getEnrolled()));
}
```

**Giải thích**
- Native SQL dùng **tên bảng/cột thật**: `courses`, `student_courses.course_id` — đây là nơi duy nhất trong bài ta "nhìn thấy" bảng trung gian.
- `TOP (:n)` là cú pháp SQL Server; phải có ngoặc khi `n` là tham số.
- Alias `AS code/name/enrolled` phải **khớp tên getter** (`getCode()`…) của interface projection. `COUNT` trên SQL Server trả `int`; Spring tự chuyển sang `Long`.
- `ORDER BY enrolled DESC, c.code`: SQL Server cho phép `ORDER BY` theo alias; thêm `c.code` để thứ tự ổn định khi bằng nhau (AIL303 và PRJ301 cùng 4).

**Kết quả mong đợi**
```
   HSF302 | Hibernate & Spring Framework        | 5 student(s)
   AIL303 | Machine Learning                    | 4 student(s)
   PRJ301 | Java Web Application Development    | 4 student(s)
```

**📌 Commit TODO 17**
```bash
git add .
git commit -m "feat(query): get top enrolled courses with native sql" -m "Refs: EX2 TODO 17"
```

---

## TODO 18 — Interface projection: bảng đăng ký

**Projection — `dto/EnrollmentView.java`**
```java
package com.hsf302.ch4.dto;

public interface EnrollmentView {
    String getStudentCode();
    String getFullName();
    String getCourseCode();
    String getCourseName();
    Integer getCredits();
}
```

**Repository — `StudentRepository`**
```java
@Query("SELECT s.studentCode AS studentCode, s.fullName AS fullName, " +
       "       c.code AS courseCode, c.name AS courseName, c.credits AS credits " +
       "FROM Student s JOIN s.department d JOIN s.courses c " +
       "WHERE d.code = :deptCode " +
       "ORDER BY s.studentCode, c.code")
List<EnrollmentView> findEnrollmentsOfDepartment(@Param("deptCode") String deptCode);
```

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
List<EnrollmentView> getEnrollmentsOfDepartment(String deptCode);
```
```java
@Override
public List<EnrollmentView> getEnrollmentsOfDepartment(String deptCode) {
    return studentRepository.findEnrollmentsOfDepartment(deptCode);
}
```

**Runner**
```java
private void todo18() {
    title("TODO 18: interface projection - enrollments of department AI");
    enrollmentService.getEnrollmentsOfDepartment("AI").forEach(v -> System.out.printf(
            "   %s | %-14s | %s | %-35s | %d%n",
            v.getStudentCode(), v.getFullName(), v.getCourseCode(), v.getCourseName(), v.getCredits()));
}
```

**Giải thích:** Query JOIN 3 entity (`Student` → `Department` qua `@ManyToOne`, `Student` → `Course` qua `@ManyToMany`) nhưng chỉ SELECT các cột cần ⇒ không nạp entity, không có rủi ro Lazy. Alias thiếu hoặc sai ⇒ getter trả `null`.

**Kết quả mong đợi (6 dòng)**
```
   AI001 | Pham Thi Dung  | AIL303 | Machine Learning                    | 3
   AI001 | Pham Thi Dung  | HSF302 | Hibernate & Spring Framework        | 3
   AI002 | Hoang Van Em   | AIL303 | Machine Learning                    | 3
   AI003 | Vo Thi Hoa     | AIL303 | Machine Learning                    | 3
   AI003 | Vo Thi Hoa     | PRJ301 | Java Web Application Development    | 3
   AI003 | Vo Thi Hoa     | SWP391 | Software Development Project        | 4
```

**📌 Commit TODO 18**
```bash
git add .
git commit -m "feat(query): add enrollment view interface projection" -m "Refs: EX2 TODO 18"
```

---

## TODO 19 — Phân trang với JOIN

**Repository — `StudentRepository`**
```java
@Query(value = "SELECT s FROM Student s JOIN s.courses c WHERE c.code = :code",
       countQuery = "SELECT COUNT(s) FROM Student s JOIN s.courses c WHERE c.code = :code")
Page<Student> findPageByCourseCode(@Param("code") String courseCode, Pageable pageable);
```

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
Page<Student> findStudentsInCoursePage(String courseCode, int pageIndex, int size);
```
```java
@Override
public Page<Student> findStudentsInCoursePage(String courseCode, int pageIndex, int size) {
    if (pageIndex < 0 || size <= 0) {
        throw new IllegalArgumentException("pageIndex must be >= 0 and size must be > 0");
    }
    Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("fullName"));
    return studentRepository.findPageByCourseCode(courseCode, pageable);
}
```

**Runner**
```java
private void todo19() {
    title("TODO 19: paginate students of HSF302 (size 2, order by fullName)");
    int pageIndex = 0;
    Page<Student> page;
    do {
        page = enrollmentService.findStudentsInCoursePage("HSF302", pageIndex, 2);
        printList("Page " + pageIndex, page.getContent());
        pageIndex++;
    } while (page.hasNext());
    System.out.println("totalElements = " + page.getTotalElements()
            + ", totalPages = " + page.getTotalPages());
}
```

**Giải thích**
- `Pageable` sinh `ORDER BY s.fullName OFFSET ? ROWS FETCH NEXT ? ROWS ONLY` (SQL Server).
- `countQuery` dùng để tính `totalElements`. Spring Data có thể tự suy ra count query, nhưng với query có JOIN nên **viết rõ** để kiểm soát câu SQL (và bắt buộc khi query chính có `JOIN FETCH`).
- ⚠️ Không dùng `JOIN FETCH` collection kèm phân trang: Hibernate phải nạp **toàn bộ** rồi cắt trang trong bộ nhớ (cảnh báo `HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory`).

**Kết quả mong đợi**
```
-- Page 0: Bui Thi Lan, Nguyen Thi Mai
-- Page 1: Nguyen Van An, Pham Thi Dung
-- Page 2: Tran Thi Binh
totalElements = 5, totalPages = 3
```

**📌 Commit TODO 19**
```bash
git add .
git commit -m "feat(query): paginate students of course" -m "Refs: EX2 TODO 19"
```

---

# Bonus

## TODO 25 (Bonus) — Specification với `join` + `distinct`

> Làm **trước Part E** (chạy trên dữ liệu gốc). `StudentRepository` đã `extends JpaSpecificationExecutor<Student>` từ Exercise 1.

**Specification — `specification/EnrollmentSpecs.java`**
```java
package com.hsf302.ch4.specification;

import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public final class EnrollmentSpecs {

    private EnrollmentSpecs() {
    }

    public static Specification<Student> enrolledIn(String courseCode) {
        return (root, query, cb) -> {
            if (courseCode == null || courseCode.isBlank()) return null;     // null = bỏ qua điều kiện
            query.distinct(true);                                          // JOIN collection → tránh trùng
            Join<Student, Course> c = root.join("courses");
            return cb.equal(c.get("code"), courseCode);
        };
    }

    public static Specification<Student> inSemester(String semester) {
        return (root, query, cb) -> {
            if (semester == null || semester.isBlank()) return null;
            query.distinct(true);
            Join<Student, Course> c = root.join("courses");
            return cb.equal(c.get("semester"), semester);
        };
    }

    public static Specification<Student> inDepartment(String deptCode) {
        return (root, query, cb) -> (deptCode == null || deptCode.isBlank())
                ? null
                : cb.equal(root.get("department").get("code"), deptCode);
    }

    public static Specification<Student> gpaAtLeast(Double minGpa) {
        return (root, query, cb) -> minGpa == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("gpa"), minGpa);
    }
}
```

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
List<Student> search(String courseCode, String semester, String deptCode, Double minGpa);
```
```java
@Override
public List<Student> search(String courseCode, String semester, String deptCode, Double minGpa) {
    Specification<Student> spec = Specification.where(EnrollmentSpecs.enrolledIn(courseCode))
            .and(EnrollmentSpecs.inSemester(semester))
            .and(EnrollmentSpecs.inDepartment(deptCode))
            .and(EnrollmentSpecs.gpaAtLeast(minGpa));
    return studentRepository.findAll(spec, Sort.by("fullName"));
}
```

**Runner**
```java
private void todo25() {
    title("TODO 25 (Bonus): Specification search");
    printList("search(null, SU26, null, null)", enrollmentService.search(null, "SU26", null, null));
    printList("search(HSF302, null, SE, 3.5)", enrollmentService.search("HSF302", null, "SE", 3.5));
    printList("search(null, FA26, AI, null)", enrollmentService.search(null, "FA26", "AI", null));
}
```

**Giải thích**
- `root.join("courses")` = `JOIN s.courses c` trong JPQL. Mỗi lần spec được gọi sẽ tạo JOIN riêng.
- **Không có `query.distinct(true)`**, lời gọi thứ 3 trả **6** dòng (Hoa học 3 khóa FA26, Dung 2 khóa) thay vì 3.
- Trả `null` từ `toPredicate` ⇒ Spring bỏ qua điều kiện đó ⇒ tham số tuỳ chọn.

**Kết quả mong đợi**
- `(null, "SU26", null, null)` → *Bui Thi Lan, Dang Van Giang*
- `("HSF302", null, "SE", 3.5)` → *Nguyen Thi Mai, Tran Thi Binh*
- `(null, "FA26", "AI", null)` → *Hoang Van Em, Pham Thi Dung, Vo Thi Hoa* (3 dòng)

**📌 Commit TODO 25**
```bash
git add .
git commit -m "feat(spec): add dynamic enrollment search with specification" -m "Refs: EX2 TODO 25"
```

---

# Part E — Thay đổi dữ liệu

> Mọi method ghi gắn `@Transactional` (ghi đè `readOnly = true` của class). Quy ước exception:
> - `IllegalArgumentException`: dữ liệu vào sai / không tìm thấy.
> - `IllegalStateException`: vi phạm quy tắc nghiệp vụ.
>
> Cả hai đều là `RuntimeException` ⇒ Spring **tự rollback** transaction.

## TODO 20 — Đăng ký khóa học (`enroll`)

**Service — `EnrollmentService`**
```java
void enroll(String studentCode, String courseCode);
```

**Service — `EnrollmentServiceImpl`**
```java
@Override
@Transactional
public void enroll(String studentCode, String courseCode) {
    Student s = getStudent(studentCode);
    Course c = getCourse(courseCode);
    checkAndEnroll(s, c);
}

/** Kiểm tra quy tắc nghiệp vụ rồi mới đăng ký. Dùng lại ở TODO 22. */
private void checkAndEnroll(Student s, Course c) {
    if (!s.isActive()) {
        throw new IllegalStateException("Student " + s.getStudentCode() + " is inactive");
    }
    if (s.getCourses().contains(c)) {
        throw new IllegalStateException("Student " + s.getStudentCode()
                + " already enrolled in " + c.getCode());
    }
    int enrolled = c.getStudents().size();
    if (enrolled >= c.getCapacity()) {
        throw new IllegalStateException("Course " + c.getCode()
                + " is full (" + enrolled + "/" + c.getCapacity() + ")");
    }
    s.enroll(c);          // helper 2 chiều → dirty checking INSERT student_courses khi commit
}
```

**Runner**
```java
private void todo20() {
    title("TODO 20: enroll with business rules");
    attempt("enroll IA003 -> MKT101", () -> enrollmentService.enroll("IA003", "MKT101"));
    attempt("enroll SE001 -> PRJ301", () -> enrollmentService.enroll("SE001", "PRJ301"));
    attempt("enroll SE004 -> AIL303", () -> enrollmentService.enroll("SE004", "AIL303"));
    attempt("enroll SE003 -> HSF302", () -> enrollmentService.enroll("SE003", "HSF302"));
    attempt("enroll XX999 -> HSF302", () -> enrollmentService.enroll("XX999", "HSF302"));
    printList("Courses of IA003", enrollmentService.getCoursesOfStudent("IA003"));
    System.out.println("Students of MKT101: " + enrollmentService.countStudentsInCourse("MKT101"));
}
```

**Giải thích**
- Không gọi `save()`: `s` là entity **managed** trong transaction ⇒ khi commit, Hibernate so sánh `s.courses` với snapshot ⇒ sinh `insert into student_courses (student_id, course_id) values (?, ?)`.
- `s.getCourses().contains(c)` hoạt động đúng nhờ `Course.equals/hashCode` theo `code`.
- Nếu **quên** kiểm tra "đã đăng ký", PK ghép `(student_id, course_id)` vẫn chặn ở DB — nhưng lúc đó lỗi là `DataIntegrityViolationException` khó hiểu. Kiểm tra ở Service để báo lỗi rõ ràng.

**Kết quả mong đợi**
```
   [OK]   enroll IA003 -> MKT101
   [FAIL] enroll SE001 -> PRJ301 -> Student SE001 already enrolled in PRJ301
   [FAIL] enroll SE004 -> AIL303 -> Course AIL303 is full (4/4)
   [FAIL] enroll SE003 -> HSF302 -> Student SE003 is inactive
   [FAIL] enroll XX999 -> HSF302 -> Student not found: XX999
-- Courses of IA003:  MKT101
Students of MKT101: 1
```

**📌 Commit TODO 20**
```bash
git add .
git commit -m "feat(enrollment): enroll student with business rules" -m "Refs: EX2 TODO 20"
```

---

## TODO 21 — Huỷ đăng ký (`unenroll`)

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
void unenroll(String studentCode, String courseCode);
```
```java
@Override
@Transactional
public void unenroll(String studentCode, String courseCode) {
    Student s = getStudent(studentCode);
    Course c = getCourse(courseCode);
    if (!s.getCourses().contains(c)) {
        throw new IllegalStateException("Student " + studentCode + " is not enrolled in " + courseCode);
    }
    s.unenroll(c);        // chỉ DELETE 1 dòng trong student_courses
}
```

**Runner**
```java
private void todo21() {
    title("TODO 21: unenroll");
    attempt("unenroll AI002 <- AIL303", () -> enrollmentService.unenroll("AI002", "AIL303"));
    attempt("unenroll IA003 <- PRJ301", () -> enrollmentService.unenroll("IA003", "PRJ301"));
    attempt("enroll   SE004 -> AIL303", () -> enrollmentService.enroll("SE004", "AIL303"));
    printList("Students of AIL303", enrollmentService.getStudentsOfCourse("AIL303"));
    printList("Courses of AI002", enrollmentService.getCoursesOfStudent("AI002"));
    System.out.println("AI002 still exists? " + studentService.findByStudentCode("AI002").isPresent());
    System.out.println("Total courses: " + courseService.count());
}
```

**Giải thích**
- `s.unenroll(c)` ⇒ `delete from student_courses where student_id = ? and course_id = ?`. Bản ghi `students` và `courses` **không bị ảnh hưởng**.
- Nếu chỉ gọi `c.getStudents().remove(s)` (inverse side) ⇒ **không có câu DELETE nào** — lỗi logic rất hay gặp. Thử comment dòng `courses.remove(c)` trong helper để thấy.

**Kết quả mong đợi**
```
   [OK]   unenroll AI002 <- AIL303
   [FAIL] unenroll IA003 <- PRJ301 -> Student IA003 is not enrolled in PRJ301
   [OK]   enroll   SE004 -> AIL303
-- Students of AIL303: Nguyen Thi Mai, Pham Thi Dung, Tran Thi Binh, Vo Thi Hoa
-- Courses of AI002:  (trống)  -> 0 record(s)
AI002 still exists? true
Total courses: 6
```

**📌 Commit TODO 21**
```bash
git add .
git commit -m "feat(enrollment): unenroll student from course" -m "Refs: EX2 TODO 21"
```

---

## TODO 22 — Đổi lớp trong 1 transaction (rollback)

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
void switchCourse(String studentCode, String fromCode, String toCode);
```
```java
@Override
@Transactional
public void switchCourse(String studentCode, String fromCode, String toCode) {
    if (fromCode == null || fromCode.equals(toCode)) {
        throw new IllegalArgumentException("fromCode and toCode must be different");
    }
    Student s = getStudent(studentCode);
    Course from = getCourse(fromCode);
    Course to = getCourse(toCode);
    if (!s.getCourses().contains(from)) {
        throw new IllegalStateException("Student " + studentCode + " is not enrolled in " + fromCode);
    }
    s.unenroll(from);          // (1) gỡ lớp cũ
    checkAndEnroll(s, to);     // (2) đăng ký lớp mới — lỗi ⇒ RuntimeException ⇒ rollback cả (1)
}
```

**Runner**
```java
private void todo22() {
    title("TODO 22: switch course in one transaction");
    attempt("switch SE001 SWP391 -> MKT101",
            () -> enrollmentService.switchCourse("SE001", "SWP391", "MKT101"));
    printList("Courses of SE001", enrollmentService.getCoursesOfStudent("SE001"));

    attempt("switch SE001 PRJ301 -> AIL303",
            () -> enrollmentService.switchCourse("SE001", "PRJ301", "AIL303"));
    printList("Courses of SE001 (after rollback)", enrollmentService.getCoursesOfStudent("SE001"));
}
```

**Giải thích**
- Hai bước (1) và (2) nằm trong **cùng một** transaction. Bước (2) ném `IllegalStateException` ⇒ `TransactionInterceptor` gọi **rollback** ⇒ việc gỡ `PRJ301` ở bước (1) cũng bị huỷ, persistence context bị bỏ ⇒ `SE001` vẫn còn `PRJ301`.
- Nếu tách thành 2 lời gọi `unenroll()` rồi `enroll()` từ Runner (2 transaction), lần 2 thất bại thì sinh viên đã **mất** lớp cũ — đó là lý do nghiệp vụ nhiều bước phải đặt trong **một method Service**.
- Mặc định Spring chỉ rollback với `RuntimeException`/`Error`; checked exception phải khai báo `@Transactional(rollbackFor = Exception.class)`.

**Kết quả mong đợi**
```
   [OK]   switch SE001 SWP391 -> MKT101
-- Courses of SE001: HSF302, MKT101, PRJ301
   [FAIL] switch SE001 PRJ301 -> AIL303 -> Course AIL303 is full (4/4)
-- Courses of SE001 (after rollback): HSF302, MKT101, PRJ301
```

**📌 Commit TODO 22**
```bash
git add .
git commit -m "feat(enrollment): switch course in one transaction" \
           -m "Gỡ lớp cũ và đăng ký lớp mới trong cùng transaction, lỗi thì rollback" \
           -m "Refs: EX2 TODO 22"
```

---

## TODO 23 — Xoá khóa học an toàn

**Mục tiêu:** Hiểu vì sao xoá entity ở **inverse side** gây lỗi FK và cách xử lý đúng.

**Service — `CourseService` / `CourseServiceImpl`**
```java
void deleteCourseDirectly(String code);   // cách SAI — để quan sát lỗi
int deleteCourse(String code);            // cách ĐÚNG
```
```java
@Override
@Transactional
public void deleteCourseDirectly(String code) {
    Course c = getCourse(code);
    courseRepository.delete(c);
    courseRepository.flush();          // ép Hibernate chạy DELETE ngay để thấy lỗi
}

@Override
@Transactional
public int deleteCourse(String code) {
    Course c = getCourse(code);
    // copy ra Set mới: unenroll() sẽ sửa c.getStudents() → tránh ConcurrentModificationException
    Set<Student> students = new HashSet<>(c.getStudents());
    students.forEach(s -> s.unenroll(c));   // gỡ từ OWNING side → DELETE các dòng student_courses
    courseRepository.delete(c);             // sau đó mới DELETE courses
    return students.size();
}

private Course getCourse(String code) {
    return courseRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + code));
}
```
> Import: `com.hsf302.ch4.pojo.Student`, `java.util.HashSet`, `java.util.Set`.

**Runner**
```java
private void todo23() {
    title("TODO 23: delete course");
    try {
        courseService.deleteCourseDirectly("IAA202");
        System.out.println("(a) Deleted ?!");
    } catch (DataIntegrityViolationException e) {
        System.out.println("(a) Caught: " + e.getClass().getSimpleName());
        System.out.println("    " + e.getMostSpecificCause().getMessage());
    }

    System.out.println("(b) Unlinked students: " + courseService.deleteCourse("IAA202"));
    printList("Remaining courses", courseService.findAllOrderByCode());
    printList("Courses of IA002", enrollmentService.getCoursesOfStudent("IA002"));
}
```
> Import: `org.springframework.dao.DataIntegrityViolationException`.

**Giải thích**
- `Course.students` là **inverse side** ⇒ khi xoá `Course`, Hibernate **không** xoá các dòng `student_courses` trỏ tới nó ⇒ SQL Server báo: *The DELETE statement conflicted with the REFERENCE constraint "FK…". The conflict occurred in database "HSF302_CH4", table "dbo.student_courses", column 'course_id'.* Spring dịch lỗi thành `DataIntegrityViolationException` và transaction bị rollback.
- Ngược lại, khi xoá `Student` (**owning side**), Hibernate **tự** xoá các dòng `student_courses` của sinh viên đó trước ⇒ không lỗi. (Vì vậy TODO 23 của Exercise 1 — xoá sinh viên inactive — vẫn chạy được sau khi thêm mapping N–N.)
- Cách đúng: gỡ liên kết ở owning side cho **từng** sinh viên rồi mới xoá. Khi flush, Hibernate thực hiện các thao tác collection (DELETE `student_courses`) **trước** các câu DELETE entity ⇒ không vi phạm FK.
- Tuyệt đối **không** "sửa" bằng `cascade = CascadeType.REMOVE` trên `Course.students` — sẽ xoá luôn sinh viên!

**Kết quả mong đợi**
```
(a) Caught: DataIntegrityViolationException
    The DELETE statement conflicted with the REFERENCE constraint "FK..." ... table "dbo.student_courses", column 'course_id'.
(b) Unlinked students: 2
-- Remaining courses: AIL303, HSF302, MKT101, PRJ301, SWP391   -> 5 record(s)
-- Courses of IA002: HSF302
```

**📌 Commit TODO 23**
```bash
git add .
git commit -m "feat(course): delete course safely from owning side" \
           -m "Xoá trực tiếp từ inverse side vi phạm FK student_courses, phải gỡ liên kết trước" \
           -m "Refs: EX2 TODO 23"
```

---

## TODO 24 — Xoá hàng loạt trên bảng trung gian

**Repository — `StudentRepository`**
```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query(value = "DELETE FROM student_courses " +
               "WHERE student_id IN (SELECT id FROM students WHERE active = 0)",
       nativeQuery = true)
int deleteEnrollmentsOfInactiveStudents();
```

**Service — `EnrollmentService` / `EnrollmentServiceImpl`**
```java
int removeEnrollmentsOfInactiveStudents();
```
```java
@Override
@Transactional
public int removeEnrollmentsOfInactiveStudents() {
    return studentRepository.deleteEnrollmentsOfInactiveStudents();
}
```

**Runner**
```java
private void todo24() {
    title("TODO 24: bulk delete enrollments of inactive students");
    System.out.println("Deleted rows: " + enrollmentService.removeEnrollmentsOfInactiveStudents());
    printCourseStats();
    printList("Students without courses", enrollmentService.findStudentsWithoutCourses());
}
```

**Giải thích**
- `student_courses` **không có entity** ⇒ JPQL không `DELETE` trực tiếp được ⇒ dùng **native SQL**. (Cách JPA thuần: nạp từng sinh viên inactive rồi `getCourses().clear()` — N câu SQL; native chỉ 1 câu.)
- `active = 0`: cột `bit` của SQL Server (native SQL dùng giá trị của DB, không dùng `false` của JPQL).
- `@Modifying` bắt buộc cho câu lệnh thay đổi dữ liệu; `clearAutomatically = true` xoá cache cấp 1 để các lần đọc sau không thấy collection cũ; `flushAutomatically = true` đẩy các thay đổi chưa flush xuống DB **trước** khi chạy bulk DELETE.
- Tại thời điểm này: Cuong (inactive) còn PRJ301; Giang (inactive) đã mất IAA202 ở TODO 23 ⇒ chỉ **1** dòng bị xoá.

**Kết quả mong đợi**
```
Deleted rows: 1
   AIL303 | Machine Learning                         | 4/4 (free 0) | avg GPA 3.700
   HSF302 | Hibernate & Spring Framework             | 5/6 (free 1) | avg GPA 3.440
   MKT101 | Marketing Principles                     | 2/4 (free 2) | avg GPA 2.700
   PRJ301 | Java Web Application Development         | 3/5 (free 2) | avg GPA 3.633
   SWP391 | Software Development Project             | 2/4 (free 2) | avg GPA 3.750
-- Students without courses: Dang Van Giang, Hoang Van Em, Le Van Cuong
```

**Kiểm tra trong SSMS:** `SELECT COUNT(*) FROM student_courses;` → **16**.

**📌 Commit TODO 24**
```bash
git add .
git commit -m "feat(enrollment): remove enrollments of inactive students" -m "Refs: EX2 TODO 24"
```

Kiểm tra lịch sử:
```bash
git log --oneline exercise2
```

---

# Tổng hợp code hoàn chỉnh

### `CourseRepository.java`

```java
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

    // ===== Part C — Derived query =====
    Optional<Course> findByCode(String code);                                            // TODO 7, 8
    List<Course> findBySemesterOrderByCodeAsc(String semester);                          // TODO 8
    long countBySemester(String semester);                                               // TODO 8
    List<Course> findByStudents_StudentCodeOrderByCodeAsc(String studentCode);           // TODO 10
    List<Course> findByStudents_Department_CodeOrderByCodeAsc(String deptCode);          // TODO 10
    List<Course> findDistinctByStudents_Department_CodeOrderByCodeAsc(String deptCode);  // TODO 10
    List<Course> findByStudentsIsEmpty();                                                // TODO 11

    // ===== Part D — Custom query =====
    @Query("SELECT new com.hsf302.ch4.dto.CourseStatDTO(c.code, c.name, c.capacity, COUNT(s), AVG(s.gpa)) " +
           "FROM Course c LEFT JOIN c.students s " +
           "GROUP BY c.code, c.name, c.capacity ORDER BY c.code")
    List<CourseStatDTO> getCourseStats();                                                // TODO 13, 24

    @Query("SELECT c FROM Course c WHERE SIZE(c.students) >= c.capacity ORDER BY c.code")
    List<Course> findFullCourses();                                                      // TODO 15

    @EntityGraph(attributePaths = "students")
    Optional<Course> findWithStudentsByCode(String code);                                // TODO 16

    @Query(value = "SELECT TOP (:n) c.code AS code, c.name AS name, COUNT(sc.student_id) AS enrolled " +
                   "FROM courses c LEFT JOIN student_courses sc ON sc.course_id = c.id " +
                   "GROUP BY c.code, c.name " +
                   "ORDER BY enrolled DESC, c.code",
           nativeQuery = true)
    List<CourseEnrollmentCount> findTopEnrolledNative(@Param("n") int n);                // TODO 17
}
```

### `StudentRepository.java` — phần bổ sung cho Exercise 2

```java
// ===== Exercise 2 — Part C =====
List<Student> findByCourses_CodeOrderByFullNameAsc(String courseCode);                  // TODO 9
long countByCourses_Code(String courseCode);                                             // TODO 9, 20
List<Student> findByCourses_CodeAndActiveTrueOrderByFullNameAsc(String courseCode);      // TODO 9
List<Student> findByCoursesIsEmptyOrderByFullNameAsc();                                  // TODO 11, 24
boolean existsByStudentCodeAndCourses_Code(String studentCode, String courseCode);       // TODO 11

// ===== Exercise 2 — Part D =====
@Query("SELECT s FROM Student s JOIN s.courses c " +
       "WHERE c.code = :code AND s.gpa >= :minGpa ORDER BY s.gpa DESC")
List<Student> findGoodStudentsInCourse(@Param("code") String courseCode,
                                       @Param("minGpa") double minGpa);                  // TODO 12

@Query("SELECT new com.hsf302.ch4.dto.StudentCreditDTO(s.studentCode, s.fullName, COUNT(c), SUM(c.credits)) " +
       "FROM Student s JOIN s.courses c " +
       "GROUP BY s.studentCode, s.fullName " +
       "HAVING SUM(c.credits) >= :minCredits " +
       "ORDER BY SUM(c.credits) DESC, s.fullName")
List<StudentCreditDTO> getCreditSummary(@Param("minCredits") long minCredits);          // TODO 14

@Query("SELECT s FROM Student s WHERE SIZE(s.courses) > :n ORDER BY s.fullName")
List<Student> findStudentsWithMoreThanNCourses(@Param("n") int n);                      // TODO 15

@Query("SELECT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.studentCode = :code")
Optional<Student> findByStudentCodeWithCourses(@Param("code") String studentCode);      // TODO 16

@Query("SELECT s.studentCode AS studentCode, s.fullName AS fullName, " +
       "       c.code AS courseCode, c.name AS courseName, c.credits AS credits " +
       "FROM Student s JOIN s.department d JOIN s.courses c " +
       "WHERE d.code = :deptCode " +
       "ORDER BY s.studentCode, c.code")
List<EnrollmentView> findEnrollmentsOfDepartment(@Param("deptCode") String deptCode);   // TODO 18

@Query(value = "SELECT s FROM Student s JOIN s.courses c WHERE c.code = :code",
       countQuery = "SELECT COUNT(s) FROM Student s JOIN s.courses c WHERE c.code = :code")
Page<Student> findPageByCourseCode(@Param("code") String courseCode, Pageable pageable); // TODO 19

// ===== Exercise 2 — Part E =====
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query(value = "DELETE FROM student_courses " +
               "WHERE student_id IN (SELECT id FROM students WHERE active = 0)",
       nativeQuery = true)
int deleteEnrollmentsOfInactiveStudents();                                               // TODO 24
```
> Import thêm: `com.hsf302.ch4.dto.StudentCreditDTO`, `com.hsf302.ch4.dto.EnrollmentView`.

### `CourseService.java`

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.CourseEnrollmentCount;
import com.hsf302.ch4.dto.CourseStatDTO;
import com.hsf302.ch4.pojo.Course;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    long count();                                                        // TODO 6, 21
    List<Course> findAllOrderByCode();                                   // TODO 6, 23
    Optional<Course> findById(Long id);                                  // TODO 6
    Optional<Course> findByCode(String code);                            // TODO 8
    List<Course> findBySemester(String semester);                        // TODO 8
    long countBySemester(String semester);                               // TODO 8
    List<Course> findCoursesOfStudent(String studentCode);               // TODO 10
    List<Course> findCoursesOfDepartment(String deptCode, boolean distinct); // TODO 10
    List<Course> findCoursesWithoutStudents();                           // TODO 11
    List<CourseStatDTO> getStatistics();                                 // TODO 13, 24
    List<Course> findFullCourses();                                      // TODO 15
    Course getWithStudents(String code);                                 // TODO 16
    List<CourseEnrollmentCount> findTopEnrolled(int n);                  // TODO 17
    void deleteCourseDirectly(String code);                              // TODO 23a
    int deleteCourse(String code);                                       // TODO 23b
}
```

### `CourseServiceImpl.java`

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.CourseEnrollmentCount;
import com.hsf302.ch4.dto.CourseStatDTO;
import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public long count() {
        return courseRepository.count();
    }

    @Override
    public List<Course> findAllOrderByCode() {
        return courseRepository.findAll(Sort.by("code"));
    }

    @Override
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public Optional<Course> findByCode(String code) {
        return courseRepository.findByCode(code);
    }

    @Override
    public List<Course> findBySemester(String semester) {
        return courseRepository.findBySemesterOrderByCodeAsc(semester);
    }

    @Override
    public long countBySemester(String semester) {
        return courseRepository.countBySemester(semester);
    }

    @Override
    public List<Course> findCoursesOfStudent(String studentCode) {
        return courseRepository.findByStudents_StudentCodeOrderByCodeAsc(studentCode);
    }

    @Override
    public List<Course> findCoursesOfDepartment(String deptCode, boolean distinct) {
        return distinct
                ? courseRepository.findDistinctByStudents_Department_CodeOrderByCodeAsc(deptCode)
                : courseRepository.findByStudents_Department_CodeOrderByCodeAsc(deptCode);
    }

    @Override
    public List<Course> findCoursesWithoutStudents() {
        return courseRepository.findByStudentsIsEmpty();
    }

    @Override
    public List<CourseStatDTO> getStatistics() {
        return courseRepository.getCourseStats();
    }

    @Override
    public List<Course> findFullCourses() {
        return courseRepository.findFullCourses();
    }

    @Override
    public Course getWithStudents(String code) {
        return courseRepository.findWithStudentsByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + code));
    }

    @Override
    public List<CourseEnrollmentCount> findTopEnrolled(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        }
        return courseRepository.findTopEnrolledNative(n);
    }

    // ===== Part E =====
    @Override
    @Transactional
    public void deleteCourseDirectly(String code) {
        Course c = getCourse(code);
        courseRepository.delete(c);
        courseRepository.flush();
    }

    @Override
    @Transactional
    public int deleteCourse(String code) {
        Course c = getCourse(code);
        Set<Student> students = new HashSet<>(c.getStudents());
        students.forEach(s -> s.unenroll(c));
        courseRepository.delete(c);
        return students.size();
    }

    private Course getCourse(String code) {
        return courseRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + code));
    }
}
```

### `EnrollmentService.java`

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.EnrollmentView;
import com.hsf302.ch4.dto.StudentCreditDTO;
import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EnrollmentService {

    // ===== Part B =====
    List<Course> getCoursesOfStudent(String studentCode);                                  // TODO 7
    List<Student> getStudentsOfCourse(String courseCode);                                  // TODO 7

    // ===== Part C =====
    List<Student> findStudentsInCourse(String courseCode);                                 // TODO 9
    long countStudentsInCourse(String courseCode);                                         // TODO 9, 20
    List<Student> findActiveStudentsInCourse(String courseCode);                           // TODO 9
    List<Student> findStudentsWithoutCourses();                                            // TODO 11, 24
    boolean isEnrolled(String studentCode, String courseCode);                             // TODO 11

    // ===== Part D =====
    List<Student> findGoodStudentsInCourse(String courseCode, double minGpa);              // TODO 12
    List<StudentCreditDTO> getCreditSummary(int minCredits);                               // TODO 14
    List<Student> findStudentsWithMoreThan(int n);                                         // TODO 15
    Student getStudentWithCourses(String studentCode);                                     // TODO 16
    List<EnrollmentView> getEnrollmentsOfDepartment(String deptCode);                      // TODO 18
    Page<Student> findStudentsInCoursePage(String courseCode, int pageIndex, int size);    // TODO 19

    // ===== Bonus =====
    List<Student> search(String courseCode, String semester, String deptCode, Double minGpa); // TODO 25

    // ===== Part E =====
    void enroll(String studentCode, String courseCode);                                    // TODO 20
    void unenroll(String studentCode, String courseCode);                                  // TODO 21
    void switchCourse(String studentCode, String fromCode, String toCode);                 // TODO 22
    int removeEnrollmentsOfInactiveStudents();                                             // TODO 24
}
```

### `EnrollmentServiceImpl.java`

```java
package com.hsf302.ch4.service;

import com.hsf302.ch4.dto.EnrollmentView;
import com.hsf302.ch4.dto.StudentCreditDTO;
import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.repository.CourseRepository;
import com.hsf302.ch4.repository.StudentRepository;
import com.hsf302.ch4.specification.EnrollmentSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // ===== Part B =====
    @Override
    public List<Course> getCoursesOfStudent(String studentCode) {
        Student s = getStudent(studentCode);
        return s.getCourses().stream()
                .sorted(Comparator.comparing(Course::getCode))
                .toList();
    }

    @Override
    public List<Student> getStudentsOfCourse(String courseCode) {
        Course c = getCourse(courseCode);
        return c.getStudents().stream()
                .sorted(Comparator.comparing(Student::getFullName))
                .toList();
    }

    // ===== Part C =====
    @Override
    public List<Student> findStudentsInCourse(String courseCode) {
        return studentRepository.findByCourses_CodeOrderByFullNameAsc(courseCode);
    }

    @Override
    public long countStudentsInCourse(String courseCode) {
        return studentRepository.countByCourses_Code(courseCode);
    }

    @Override
    public List<Student> findActiveStudentsInCourse(String courseCode) {
        return studentRepository.findByCourses_CodeAndActiveTrueOrderByFullNameAsc(courseCode);
    }

    @Override
    public List<Student> findStudentsWithoutCourses() {
        return studentRepository.findByCoursesIsEmptyOrderByFullNameAsc();
    }

    @Override
    public boolean isEnrolled(String studentCode, String courseCode) {
        return studentRepository.existsByStudentCodeAndCourses_Code(studentCode, courseCode);
    }

    // ===== Part D =====
    @Override
    public List<Student> findGoodStudentsInCourse(String courseCode, double minGpa) {
        if (minGpa < 0 || minGpa > 4) {
            throw new IllegalArgumentException("minGpa must be in [0, 4]");
        }
        return studentRepository.findGoodStudentsInCourse(courseCode, minGpa);
    }

    @Override
    public List<StudentCreditDTO> getCreditSummary(int minCredits) {
        if (minCredits < 0) {
            throw new IllegalArgumentException("minCredits must be >= 0");
        }
        return studentRepository.getCreditSummary(minCredits);
    }

    @Override
    public List<Student> findStudentsWithMoreThan(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        }
        return studentRepository.findStudentsWithMoreThanNCourses(n);
    }

    @Override
    public Student getStudentWithCourses(String studentCode) {
        return studentRepository.findByStudentCodeWithCourses(studentCode)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentCode));
    }

    @Override
    public List<EnrollmentView> getEnrollmentsOfDepartment(String deptCode) {
        return studentRepository.findEnrollmentsOfDepartment(deptCode);
    }

    @Override
    public Page<Student> findStudentsInCoursePage(String courseCode, int pageIndex, int size) {
        if (pageIndex < 0 || size <= 0) {
            throw new IllegalArgumentException("pageIndex must be >= 0 and size must be > 0");
        }
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("fullName"));
        return studentRepository.findPageByCourseCode(courseCode, pageable);
    }

    // ===== Bonus =====
    @Override
    public List<Student> search(String courseCode, String semester, String deptCode, Double minGpa) {
        Specification<Student> spec = Specification.where(EnrollmentSpecs.enrolledIn(courseCode))
                .and(EnrollmentSpecs.inSemester(semester))
                .and(EnrollmentSpecs.inDepartment(deptCode))
                .and(EnrollmentSpecs.gpaAtLeast(minGpa));
        return studentRepository.findAll(spec, Sort.by("fullName"));
    }

    // ===== Part E — ghi dữ liệu =====
    @Override
    @Transactional
    public void enroll(String studentCode, String courseCode) {
        Student s = getStudent(studentCode);
        Course c = getCourse(courseCode);
        checkAndEnroll(s, c);
    }

    @Override
    @Transactional
    public void unenroll(String studentCode, String courseCode) {
        Student s = getStudent(studentCode);
        Course c = getCourse(courseCode);
        if (!s.getCourses().contains(c)) {
            throw new IllegalStateException("Student " + studentCode + " is not enrolled in " + courseCode);
        }
        s.unenroll(c);
    }

    @Override
    @Transactional
    public void switchCourse(String studentCode, String fromCode, String toCode) {
        if (fromCode == null || fromCode.equals(toCode)) {
            throw new IllegalArgumentException("fromCode and toCode must be different");
        }
        Student s = getStudent(studentCode);
        Course from = getCourse(fromCode);
        Course to = getCourse(toCode);
        if (!s.getCourses().contains(from)) {
            throw new IllegalStateException("Student " + studentCode + " is not enrolled in " + fromCode);
        }
        s.unenroll(from);
        checkAndEnroll(s, to);
    }

    @Override
    @Transactional
    public int removeEnrollmentsOfInactiveStudents() {
        return studentRepository.deleteEnrollmentsOfInactiveStudents();
    }

    // ===== helpers =====
    private void checkAndEnroll(Student s, Course c) {
        if (!s.isActive()) {
            throw new IllegalStateException("Student " + s.getStudentCode() + " is inactive");
        }
        if (s.getCourses().contains(c)) {
            throw new IllegalStateException("Student " + s.getStudentCode()
                    + " already enrolled in " + c.getCode());
        }
        int enrolled = c.getStudents().size();
        if (enrolled >= c.getCapacity()) {
            throw new IllegalStateException("Course " + c.getCode()
                    + " is full (" + enrolled + "/" + c.getCapacity() + ")");
        }
        s.enroll(c);
    }

    private Student getStudent(String studentCode) {
        if (studentCode == null || studentCode.isBlank()) {
            throw new IllegalArgumentException("Student code must not be blank");
        }
        return studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentCode));
    }

    private Course getCourse(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code must not be blank");
        }
        return courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseCode));
    }
}
```

### Imports cần cho `Exercise2Runner`

```java
import com.hsf302.ch4.pojo.Course;
import com.hsf302.ch4.pojo.Student;
import com.hsf302.ch4.service.CourseService;
import com.hsf302.ch4.service.EnrollmentService;
import com.hsf302.ch4.service.StudentService;
import org.hibernate.LazyInitializationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
```

---

# Bảng lỗi thường gặp

| Lỗi / hiện tượng | Nguyên nhân | Cách sửa |
|---|---|---|
| Thêm vào `course.getStudents()` nhưng bảng `student_courses` **không có dòng mới** | Chỉ sửa **inverse side** (`mappedBy`) — Hibernate bỏ qua | Sửa ở owning side, dùng helper `student.enroll(course)` |
| Bảng trung gian tên `students_courses`, cột `students_id` | Thiếu `@JoinTable` | Khai báo `@JoinTable(name = "student_courses", joinColumns…, inverseJoinColumns…)` |
| Hibernate tạo **2** bảng trung gian | Cả 2 phía đều có `@ManyToMany` mà thiếu `mappedBy` | Thêm `mappedBy = "courses"` ở `Course` |
| `mappedBy reference an unknown target entity property` | Giá trị `mappedBy` không khớp tên field | `mappedBy` = **tên field** bên `Student` (`courses`) |
| `LazyInitializationException ... role: ...Student.courses` | Truy cập collection ngoài transaction (Runner, `toString`, seed không có `@Transactional`) | Xử lý trong Service, `JOIN FETCH`, `@EntityGraph`, hoặc `@Transactional` cho seed |
| `StackOverflowError` khi in / khi `hashCode` | `@Data` hoặc `toString/hashCode` duyệt 2 collection gọi vòng | Tự viết `toString`; `equals/hashCode` chỉ dùng business key |
| `contains()` trả `false` dù cùng khóa học; `remove()` không xoá | Không override `equals/hashCode`, hoặc dựa trên `id` (thay đổi sau khi save) | `equals/hashCode` theo `code` / `studentCode` |
| Kết quả query bị **trùng** dòng | JOIN collection tạo 1 dòng mỗi cặp | `findDistinctBy...`, `SELECT DISTINCT`, `query.distinct(true)` |
| `MultipleBagFetchException` | `JOIN FETCH` nhiều collection kiểu `List` | Dùng `Set`, hoặc fetch từng collection |
| `HHH90003004: firstResult/maxResults specified with collection fetch` | Phân trang + `JOIN FETCH` collection | Bỏ fetch khi phân trang (TODO 19) |
| `The DELETE statement conflicted with the REFERENCE constraint ... student_courses` | Xoá `Course` (inverse side) khi còn liên kết | Gỡ từ owning side trước (TODO 23) |
| Violation of PRIMARY KEY constraint ... `student_courses` | Đăng ký trùng (student, course) | Kiểm tra `contains()` trước khi `enroll` |
| `Could not resolve root entity 'student_courses'` | Dùng bảng trung gian trong JPQL | JPQL dùng `JOIN s.courses c`; chỉ native SQL mới dùng `student_courses` |
| `Parameter value [7] did not match expected type [java.lang.Long]` | So sánh `SUM(...)` (Long) với tham số `int` | Khai báo tham số `long` (TODO 14) |
| Getter projection trả `null` | Alias không khớp tên getter | `AS studentCode` ↔ `getStudentCode()` |
| Đổi lớp thất bại nhưng sinh viên **mất** lớp cũ | `unenroll` và `enroll` ở 2 transaction khác nhau | Gộp vào 1 method `@Transactional` (TODO 22) |
| Dữ liệu không đổi sau `enroll` | Method ghi thiếu `@Transactional` → chạy readOnly | Thêm `@Transactional` trên method ghi |
| Cả `ExerciseRunner` (Ex1) và `Exercise2Runner` cùng chạy, kết quả sai | Chưa gắn `@Profile` hoặc sai `spring.profiles.active` | `@Profile("ex1")` / `@Profile("ex2")`, `spring.profiles.active=ex2` |
| `No qualifying bean of type 'CourseService'` | Thiếu `@Service` trên `CourseServiceImpl` hoặc chưa `implements` | Gắn `@Service` lên class implementation |
| Console in `?` thay cho ký tự đặc biệt | Encoding console Windows | Dùng ký tự ASCII (`[OK]`, `[FAIL]`) hoặc thêm VM option `-Dfile.encoding=UTF-8` |
