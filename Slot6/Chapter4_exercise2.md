# Chapter 4 — Exercise 2: Đăng ký Khóa học với Spring Data JPA (Many-To-Many)

> **Mục tiêu:** Tiếp nối Exercise 1, áp dụng Chapter 4 — Spring Data JPA cho quan hệ **nhiều – nhiều**:
> - Ánh xạ quan hệ **Many-To-Many** giữa `Student` và `Course` bằng `@ManyToMany` + `@JoinTable` (bảng trung gian `student_courses`).
> - Phân biệt **owning side** (`Student.courses`) và **inverse side** (`Course.students` – `mappedBy`), viết helper đồng bộ 2 chiều, dùng `Set` + `equals/hashCode` theo **business key**.
> - Viết **derived query** đi xuyên qua collection (`Courses_Code`, `Students_Department_Code`, `IsEmpty`, `Distinct`, `existsBy...And...`).
> - Viết **custom query** với `@Query`: `JOIN` collection, `LEFT JOIN` + `GROUP BY` + DTO, `HAVING`, `SIZE()`, `JOIN FETCH`, `@EntityGraph`, native SQL trên bảng trung gian, interface projection, phân trang.
> - Thực hiện nghiệp vụ **ghi**: đăng ký / huỷ đăng ký / đổi lớp (rollback), xoá khóa học an toàn, xoá hàng loạt trên bảng trung gian (`@Modifying` native).
> - *(Bonus)* Tìm kiếm động bằng **Specification** có `join` + `distinct`.
> - Giữ kiến trúc **N-layer**: `Exercise2Runner` → **Service** (interface + implementation) → **Repository** → Entity.
> - Quản lý mã nguồn bằng **Git**: mỗi TODO hoàn thành là **một commit** theo chuẩn **Conventional Commits**.
>
> **Thời gian gợi ý:** 3 – 4 giờ · **Hình thức:** Console app (Spring Boot + `CommandLineRunner`), chưa cần Web/Controller.
>
> **Điều kiện:** Đã hoàn thành Exercise 1 (entity `Department`, `Student`, `DataInitializer`, `StudentService`…).
>
> **Hướng dẫn chi tiết từng bước:** xem `Chapter4_exercise2_guide.md`

---

## 1. Công nghệ

| Thành phần | Phiên bản / lựa chọn |
|---|---|
| JDK | 17+ |
| Spring Boot | 3.2.x |
| Dependencies | Spring Data JPA, MS SQL Server Driver, Lombok |
| Database | SQL Server — **vẫn dùng database `HSF302_CH4`** của Exercise 1 |
| Build | Maven |

**Làm tiếp trên project Exercise 1** (base package `com.hsf302.ch4`), tạo nhánh Git `exercise2`. Các file **mới** / **sửa**:

```
com.hsf302.ch4
├── pojo/          Course.java (mới) · Student.java (sửa — thêm @ManyToMany)
├── dto/           CourseStatDTO.java, StudentCreditDTO.java,             (mới)
│                  CourseEnrollmentCount.java, EnrollmentView.java
├── repository/    CourseRepository.java (mới) · StudentRepository.java (bổ sung method)
├── specification/ EnrollmentSpecs.java                                  (bonus)
├── service/       CourseService.java, CourseServiceImpl.java,           (mới)
│                  EnrollmentService.java, EnrollmentServiceImpl.java
└── runner/        CourseDataInitializer.java, Exercise2Runner.java      (mới)
                   ExerciseRunner.java (sửa — chỉ chạy với profile ex1)
```

---

## 2. Mô hình dữ liệu

```
┌──────────────┐ 1      N ┌────────────────────┐ 1     N ┌──────────────────┐ N      1 ┌──────────────────────┐
│ departments  │──────────│ students           │─────────│ student_courses  │──────────│ courses              │
├──────────────┤          ├────────────────────┤         ├──────────────────┤          ├──────────────────────┤
│ id (PK)      │          │ id (PK)            │         │ student_id (PK,FK)│         │ id (PK, identity)    │
│ code         │          │ student_code       │         │ course_id  (PK,FK)│         │ code (unique)        │
│ name         │          │ ... (Exercise 1)   │         └──────────────────┘          │ name                 │
└──────────────┘          │ department_id (FK) │                                        │ credits  (int)       │
                          └────────────────────┘                                        │ capacity (int)       │
                                                                                        │ semester (FA26/SU26) │
                                                                                        └──────────────────────┘
                          Student  N ───────────────── @ManyToMany ──────────────────── N  Course
```

- `Student` là **owning side**: `@ManyToMany` + `@JoinTable(name = "student_courses", joinColumns = student_id, inverseJoinColumns = course_id)`, kiểu `Set<Course>`.
- `Course` là **inverse side**: `@ManyToMany(mappedBy = "courses")`, kiểu `Set<Student>`.
- **Không cascade** ở cả hai phía (xoá khóa học không được xoá sinh viên và ngược lại). Fetch mặc định **LAZY**.
- Bảng `student_courses` **không** có entity riêng — Hibernate tự tạo với khoá chính ghép `(student_id, course_id)`.
- `Course.equals/hashCode` theo `code`; `Student.equals/hashCode` theo `studentCode` (business key).

---

## 3. Dữ liệu mẫu

`DataInitializer` của Exercise 1 vẫn seed **4 department** và **10 student** như cũ. `CourseDataInitializer` (mới) seed thêm:

**Course**

| id | code | name | credits | capacity | semester |
|---|---|---|---|---|---|
| 1 | PRJ301 | Java Web Application Development | 3 | 5 | FA26 |
| 2 | HSF302 | Hibernate & Spring Framework | 3 | 6 | FA26 |
| 3 | SWP391 | Software Development Project | 4 | 4 | FA26 |
| 4 | AIL303 | Machine Learning | 3 | 4 | FA26 |
| 5 | IAA202 | Risk Management in Information Systems | 3 | 4 | SU26 |
| 6 | MKT101 | Marketing Principles | 2 | 4 | SU26 *(chưa có sinh viên)* |

**Đăng ký (bảng `student_courses`) — 18 dòng**

| Student | Tên (GPA, trạng thái) | Khóa học đã đăng ký |
|---|---|---|
| SE001 | Nguyen Van An (3.2) | PRJ301, HSF302, SWP391 |
| SE002 | Tran Thi Binh (3.8) | PRJ301, HSF302, AIL303 |
| SE003 | Le Van Cuong (2.5, **inactive**) | PRJ301 |
| SE004 | Nguyen Thi Mai (3.6) | HSF302, SWP391 |
| AI001 | Pham Thi Dung (3.5) | AIL303, HSF302 |
| AI002 | Hoang Van Em (2.8) | AIL303 |
| AI003 | Vo Thi Hoa (3.9) | AIL303, PRJ301, SWP391 |
| IA001 | Dang Van Giang (1.9, **inactive**) | IAA202 |
| IA002 | Bui Thi Lan (3.1) | IAA202, HSF302 |
| IA003 | Do Van Nam (2.2) | *(chưa đăng ký)* |

> Vẫn dùng `spring.jpa.hibernate.ddl-auto=create` → mỗi lần chạy, Hibernate tạo lại **toàn bộ** bảng (`departments`, `students`, `courses`, `student_courses`) và seed lại từ đầu.
> Các TODO chạy **theo đúng thứ tự** trong `Exercise2Runner`: Part B → D và Bonus chỉ **đọc**, Part E mới **thay đổi** dữ liệu.

---

## 4. Yêu cầu — các TODO

**Quy ước in kết quả:** mỗi TODO in tiêu đề `===== TODO x: ... =====`, sau đó in kết quả. `toString()` của `Student` và `Course` **không được** truy cập quan hệ (`department`, `courses`, `students`).

### 4.0 Kiến trúc bắt buộc & tầng Service

```
Exercise2Runner ──► CourseService / EnrollmentService / StudentService(Ex1)   (interface)
                          ▲ implements
                   CourseServiceImpl / EnrollmentServiceImpl                   (@Service, @Transactional)
                          │
                   CourseRepository / StudentRepository                        (Spring Data JPA)
```

**Quy định**
1. `Exercise2Runner` **chỉ inject Service interface** (`CourseService`, `EnrollmentService`, và `StudentService` của Exercise 1 cho TODO 16a, 21), **không** inject repository.
2. `CourseService`: nghiệp vụ xoay quanh **khóa học**. `EnrollmentService`: nghiệp vụ trên **quan hệ đăng ký** (sinh viên ↔ khóa học).
3. Mỗi TODO từ 6 trở đi: **Repository** (nếu cần) → **Service interface** → **ServiceImpl** → **Runner**.
4. `ServiceImpl` gắn `@Service`, `@Transactional(readOnly = true)` ở mức class; method **ghi dữ liệu** (Part E) gắn thêm `@Transactional`.
5. Inject repository bằng **constructor** (`@RequiredArgsConstructor` + field `final`).
6. Kiểm tra đầu vào ở **Service**: dữ liệu vào sai / không tìm thấy → `IllegalArgumentException`; vi phạm **quy tắc nghiệp vụ** (đã đăng ký, hết chỗ, sinh viên inactive…) → `IllegalStateException`.
7. Seed dữ liệu (`CourseDataInitializer`) được phép dùng repository trực tiếp.

**Service (method bắt buộc)**

| TODO | `CourseService` | `EnrollmentService` |
|---|---|---|
| 6 | `long count()` · `List<Course> findAllOrderByCode()` · `Optional<Course> findById(Long id)` | |
| 7 | | `List<Course> getCoursesOfStudent(String studentCode)` · `List<Student> getStudentsOfCourse(String courseCode)` |
| 8 | `Optional<Course> findByCode(String code)` · `List<Course> findBySemester(String semester)` · `long countBySemester(String semester)` | |
| 9 | | `List<Student> findStudentsInCourse(String courseCode)` · `long countStudentsInCourse(String courseCode)` · `List<Student> findActiveStudentsInCourse(String courseCode)` |
| 10 | `List<Course> findCoursesOfStudent(String studentCode)` · `List<Course> findCoursesOfDepartment(String deptCode, boolean distinct)` | |
| 11 | `List<Course> findCoursesWithoutStudents()` | `List<Student> findStudentsWithoutCourses()` · `boolean isEnrolled(String studentCode, String courseCode)` |
| 12 | | `List<Student> findGoodStudentsInCourse(String courseCode, double minGpa)` |
| 13 | `List<CourseStatDTO> getStatistics()` | |
| 14 | | `List<StudentCreditDTO> getCreditSummary(int minCredits)` |
| 15 | `List<Course> findFullCourses()` | `List<Student> findStudentsWithMoreThan(int n)` |
| 16 | `Course getWithStudents(String code)` | `Student getStudentWithCourses(String studentCode)` |
| 17 | `List<CourseEnrollmentCount> findTopEnrolled(int n)` | |
| 18 | | `List<EnrollmentView> getEnrollmentsOfDepartment(String deptCode)` |
| 19 | | `Page<Student> findStudentsInCoursePage(String courseCode, int pageIndex, int size)` |
| 20 | | `void enroll(String studentCode, String courseCode)` |
| 21 | | `void unenroll(String studentCode, String courseCode)` |
| 22 | | `void switchCourse(String studentCode, String fromCode, String toCode)` |
| 23 | `void deleteCourseDirectly(String code)` · `int deleteCourse(String code)` | |
| 24 | | `int removeEnrollmentsOfInactiveStudents()` |
| 25 | | `List<Student> search(String courseCode, String semester, String deptCode, Double minGpa)` |

**Validate bắt buộc ở Service:** `findGoodStudentsInCourse` (0 ≤ minGpa ≤ 4) · `getCreditSummary` (minCredits ≥ 0) · `findStudentsWithMoreThan` (n ≥ 0) · `findTopEnrolled` (n > 0) · `findStudentsInCoursePage` (pageIndex ≥ 0, size > 0) · `enroll`/`unenroll`/`switchCourse` (student & course phải tồn tại; `switchCourse` 2 khóa phải khác nhau) · quy tắc đăng ký ở TODO 20.

### Part A — Mapping Many-To-Many (2.0 điểm)

| TODO | Yêu cầu |
|---|---|
| **TODO 1** | Tạo nhánh `exercise2` từ project Exercise 1. Gắn `@Profile("ex1")` cho `ExerciseRunner` cũ; thêm `spring.profiles.active=ex2` vào `application.properties`. Giữ nguyên kết nối database `HSF302_CH4`, `ddl-auto=create`, `show-sql`, `format_sql`. |
| **TODO 2** | Tạo entity `Course` (bảng `courses`): `id` identity; `code` unique not null length 10; `name` not null length 100; `credits`, `capacity` (Integer, not null); `semester` not null length 10; `Set<Student> students` với `@ManyToMany(mappedBy = "courses")`. Override `equals/hashCode` theo `code`, `toString()` **không chứa** `students`. **Không dùng `@Data`.** |
| **TODO 3** | Sửa entity `Student`: thêm `Set<Course> courses` với `@ManyToMany` + `@JoinTable(name = "student_courses", joinColumns = @JoinColumn(name = "student_id"), inverseJoinColumns = @JoinColumn(name = "course_id"))`. Viết helper `enroll(Course c)` và `unenroll(Course c)` đồng bộ **2 chiều**. Override `equals/hashCode` theo `studentCode`. Không cascade. |
| **TODO 4** | Tạo `CourseRepository extends JpaRepository<Course, Long>`. Tạo khung tầng Service: interface `CourseService`, `EnrollmentService` và class `CourseServiceImpl`, `EnrollmentServiceImpl` (`@Service`, `@Transactional(readOnly = true)`, inject `CourseRepository` + `StudentRepository` qua constructor). |
| **TODO 5** | Tạo `CourseDataInitializer implements CommandLineRunner` (`@Order(2)`, method `run` gắn `@Transactional`) seed 6 course và 18 lượt đăng ký ở mục 3 bằng helper `enroll()`. Tạo `Exercise2Runner` (`@Order(3)`, `@Profile("ex2")`) chỉ inject `CourseService`, `EnrollmentService`, `StudentService`. |

### Part B — Method có sẵn & điều hướng quan hệ (1.0 điểm)

| TODO | Yêu cầu | Kỹ thuật |
|---|---|---|
| **TODO 6** | In tổng số course; in toàn bộ course sắp xếp theo `code` tăng dần; tìm course id = 2 và id = 99 (in `"Not found"` nếu không có). | `count()`, `findAll(Sort)`, `findById()` |
| **TODO 7** | (a) In các khóa học của sinh viên `SE001` bằng cách **điều hướng** `student.getCourses()` (owning side). (b) In các sinh viên của khóa `AIL303` bằng `course.getStudents()` (inverse side). Kết quả sắp xếp theo `code` / `fullName` (sắp xếp trong Service). | Truy cập collection LAZY **trong** transaction của Service |

### Part C — Derived query trên quan hệ N–N (2.0 điểm)

Khai báo method trong repository **chỉ bằng tên method** (không dùng `@Query`).

| TODO | Yêu cầu |
|---|---|
| **TODO 8** | (a) Tìm course theo `code` = `"HSF302"` và `"XXX000"` (trả `Optional`). (b) Tìm course của học kỳ `"SU26"`, sắp xếp theo `code`. (c) Đếm số course của học kỳ `"FA26"`. |
| **TODO 9** | (a) Tìm sinh viên đăng ký khóa `"PRJ301"`, sắp xếp theo `fullName` *(nested property qua collection `courses`)*. (b) Đếm số sinh viên của khóa `"HSF302"`. (c) Tìm sinh viên **active** đăng ký khóa `"PRJ301"`, sắp xếp theo `fullName`. |
| **TODO 10** | (a) Tìm các khóa học mà sinh viên `"SE002"` đã đăng ký, sắp xếp theo `code` *(từ phía inverse: `Students_StudentCode`)*. (b) Tìm các khóa học có sinh viên của khoa `"AI"` đăng ký *(2 cấp: `Students_Department_Code`)* — chạy **không** `Distinct` và **có** `Distinct`, so sánh số dòng. |
| **TODO 11** | (a) Tìm sinh viên **chưa đăng ký** khóa nào. (b) Tìm khóa học **chưa có** sinh viên. (c) Kiểm tra `SE001` đã đăng ký `AIL303` chưa; `SE002` đã đăng ký `AIL303` chưa. |

### Part D — Custom query với `@Query` (3.0 điểm)

| TODO | Yêu cầu | Kỹ thuật |
|---|---|---|
| **TODO 12** | Tìm sinh viên đăng ký khóa `courseCode` có GPA ≥ `minGpa`, sắp xếp GPA giảm dần. Chạy với `("HSF302", 3.5)`. | JPQL `JOIN s.courses c` + named parameter |
| **TODO 13** | Thống kê **từng** khóa học (kể cả khóa chưa có sinh viên): `code`, `name`, `capacity`, số sinh viên đã đăng ký, GPA trung bình; sắp xếp theo `code`. Trả về record `CourseStatDTO` có thêm method `remaining()` (số chỗ còn trống). In GPA với 3 chữ số thập phân. | `LEFT JOIN` collection + `GROUP BY` + `COUNT/AVG` + constructor expression |
| **TODO 14** | Tổng hợp tín chỉ theo sinh viên: `studentCode`, `fullName`, số khóa, **tổng tín chỉ**; chỉ lấy sinh viên có tổng tín chỉ ≥ `minCredits`; sắp xếp tổng tín chỉ giảm dần rồi `fullName`. Trả về record `StudentCreditDTO`. Chạy với `7`. | `GROUP BY` + `SUM` + `HAVING` |
| **TODO 15** | (a) Tìm các khóa học **đã đủ chỗ** (số sinh viên ≥ `capacity`). (b) Tìm sinh viên đăng ký **nhiều hơn 2** khóa, sắp xếp theo `fullName`. | `SIZE()` trên collection |
| **TODO 16** | (a) Dùng `studentService.findByStudentCode("SE001")` (Exercise 1) rồi gọi `getCourses().size()` → **bắt và in** exception. (b) Viết query lấy sinh viên **kèm** các khóa học trong 1 câu SQL (`JOIN FETCH`), in các khóa của `SE001`. (c) Lấy khóa `SWP391` **kèm** danh sách sinh viên bằng `@EntityGraph`. | `LazyInitializationException`, `JOIN FETCH`, `@EntityGraph` |
| **TODO 17** | Lấy **top N** khóa học đông sinh viên nhất (kể cả khóa 0 sinh viên), trả về `code`, `name`, `enrolled`, sắp xếp `enrolled` giảm dần rồi `code`. Viết bằng **native SQL** trên bảng `courses` + `student_courses` (SQL Server `TOP`). Kết quả là interface `CourseEnrollmentCount`. Chạy với `n = 3`. | `nativeQuery = true` + interface projection |
| **TODO 18** | Lấy **bảng đăng ký** của sinh viên thuộc một khoa: `studentCode`, `fullName`, `courseCode`, `courseName`, `credits`; sắp xếp theo `studentCode`, `courseCode`. Trả về interface `EnrollmentView`. Chạy với `"AI"`. | Interface projection + alias, JOIN 3 entity |
| **TODO 19** | Phân trang sinh viên của khóa `"HSF302"`, sắp xếp `fullName` tăng dần, mỗi trang 2 phần tử. In **tất cả** các trang cùng `totalElements`, `totalPages`. | `@Query` + `countQuery` + `Pageable` |

### Part E — Thay đổi dữ liệu (2.0 điểm)

Các thao tác ghi đặt trong ServiceImpl và gắn `@Transactional` trên từng method ghi. Runner bọc mỗi lời gọi trong `try/catch` và in `✔ ...` (thành công) hoặc `✘ ... → <message>` (thất bại).

| TODO | Yêu cầu | Kỹ thuật |
|---|---|---|
| **TODO 20** | Viết `enroll(studentCode, courseCode)` với **quy tắc**: sinh viên phải **active**; chưa đăng ký khóa đó; khóa còn chỗ (`số SV < capacity`). Chạy lần lượt: `(IA003, MKT101)` ✔ · `(SE001, PRJ301)` ✘ đã đăng ký · `(SE004, AIL303)` ✘ hết chỗ · `(SE003, HSF302)` ✘ inactive · `(XX999, HSF302)` ✘ không tìm thấy. In lại khóa học của `IA003` và số SV của `MKT101`. | Helper `enroll()` + dirty checking → INSERT vào `student_courses` |
| **TODO 21** | Viết `unenroll(studentCode, courseCode)` (báo lỗi nếu chưa đăng ký). Chạy: `(AI002, AIL303)` ✔ · `(IA003, PRJ301)` ✘ · sau đó `enroll(SE004, AIL303)` ✔ (vì đã có chỗ trống). In sinh viên của `AIL303`, khóa học của `AI002`, và chứng minh sinh viên `AI002` **vẫn tồn tại**, số course vẫn là 6. | Helper `unenroll()` → chỉ DELETE dòng trong bảng trung gian |
| **TODO 22** | Viết `switchCourse(studentCode, fromCode, toCode)` trong **1 transaction**: huỷ `from` rồi đăng ký `to` (dùng lại quy tắc TODO 20). Chạy: `(SE001, SWP391, MKT101)` ✔ · `(SE001, PRJ301, AIL303)` ✘ hết chỗ → **toàn bộ rollback**, `SE001` vẫn còn `PRJ301`. In khóa học của `SE001` sau mỗi lần. | `@Transactional` rollback khi có `RuntimeException` |
| **TODO 23** | (a) Viết `deleteCourseDirectly(code)`: xoá thẳng course `IAA202` (đang có sinh viên) + `flush()` → **bắt và in** exception. (b) Viết `deleteCourse(code)` đúng cách: gỡ course khỏi **owning side** của mọi sinh viên rồi mới xoá; trả về số sinh viên bị gỡ. In danh sách course còn lại và khóa học của `IA002`. | Xoá từ phía inverse, `DataIntegrityViolationException` |
| **TODO 24** | Xoá **mọi lượt đăng ký** của sinh viên **inactive** bằng **một câu** DELETE native trên bảng `student_courses`; in số dòng bị xoá, chạy lại thống kê TODO 13 và danh sách sinh viên chưa đăng ký khóa nào. | `@Modifying` + native DELETE (bảng trung gian không có entity) |

### Bonus — Specification (+1.0 điểm)

| TODO | Yêu cầu |
|---|---|
| **TODO 25** | Viết `EnrollmentSpecs` với các điều kiện **tuỳ chọn** (null/blank = bỏ qua): `enrolledIn(courseCode)`, `inSemester(semester)` (join `courses`, bật `distinct`), `inDepartment(deptCode)`, `gpaAtLeast(minGpa)`. Viết `search(courseCode, semester, deptCode, minGpa)` ghép các điều kiện, sắp xếp theo `fullName`. Chạy (trên dữ liệu gốc — gọi **trước Part E**): `search(null, "SU26", null, null)` · `search("HSF302", null, "SE", 3.5)` · `search(null, "FA26", "AI", null)`. |

---

## 5. Kết quả mong đợi (để tự kiểm tra)

| TODO | Kết quả |
|---|---|
| 6 | 6 courses · thứ tự: AIL303, HSF302, IAA202, MKT101, PRJ301, SWP391 · id=2 → *HSF302* · id=99 → *Not found* |
| 7 | (a) SE001: HSF302, PRJ301, SWP391 · (b) AIL303: *Hoang Van Em, Pham Thi Dung, Tran Thi Binh, Vo Thi Hoa* |
| 8 | HSF302 → *Hibernate & Spring Framework* · XXX000 → không tìm thấy · SU26: IAA202, MKT101 · FA26: **4** |
| 9 | (a) *Le Van Cuong, Nguyen Van An, Tran Thi Binh, Vo Thi Hoa* · (b) 5 · (c) *Nguyen Van An, Tran Thi Binh, Vo Thi Hoa* |
| 10 | (a) AIL303, HSF302, PRJ301 · (b) không Distinct: **6** dòng (AIL303 ×3, HSF302, PRJ301, SWP391) · có Distinct: **4** dòng (AIL303, HSF302, PRJ301, SWP391) |
| 11 | (a) *Do Van Nam* · (b) *MKT101* · (c) SE001–AIL303 → `false` · SE002–AIL303 → `true` |
| 12 | Binh 3.8, Mai 3.6, Dung 3.5 |
| 13 | AIL303 4/4 (còn 0) – 3.500 · HSF302 5/6 (còn 1) – 3.440 · IAA202 2/4 (còn 2) – 2.500 · MKT101 0/4 (còn 4) – null · PRJ301 4/5 (còn 1) – 3.350 · SWP391 3/4 (còn 1) – 3.567 |
| 14 | SE001 An 3 khóa – 10 TC · AI003 Hoa 3 – 10 · SE002 Binh 3 – 9 · SE004 Mai 2 – 7 |
| 15 | (a) *AIL303* · (b) *Nguyen Van An, Tran Thi Binh, Vo Thi Hoa* |
| 16 | (a) `LazyInitializationException` · (b) SE001: HSF302, PRJ301, SWP391 · (c) SWP391: *Nguyen Thi Mai, Nguyen Van An, Vo Thi Hoa* |
| 17 | HSF302 – 5 · AIL303 – 4 · PRJ301 – 4 |
| 18 | 6 dòng: AI001–AIL303, AI001–HSF302, AI002–AIL303, AI003–AIL303, AI003–PRJ301, AI003–SWP391 |
| 19 | Trang 0: Lan, Mai · Trang 1: An, Dung · Trang 2: Binh · totalElements = 5 · totalPages = 3 |
| 25 | `(null,"SU26",null,null)` → *Bui Thi Lan, Dang Van Giang* · `("HSF302",null,"SE",3.5)` → *Nguyen Thi Mai, Tran Thi Binh* · `(null,"FA26","AI",null)` → *Hoang Van Em, Pham Thi Dung, Vo Thi Hoa* (3 dòng, không trùng) |
| 20 | 1 ✔ + 4 ✘ (đã đăng ký / đủ 4/4 / inactive / không tìm thấy) · IA003: MKT101 · MKT101 có 1 SV |
| 21 | AIL303: *Nguyen Thi Mai, Pham Thi Dung, Tran Thi Binh, Vo Thi Hoa* · AI002: *(không có khóa nào)* · AI002 vẫn tồn tại → `true` · course = 6 |
| 22 | Lần 1 ✔ → SE001: HSF302, MKT101, PRJ301 · Lần 2 ✘ (AIL303 đủ 4/4) → SE001 **vẫn**: HSF302, MKT101, PRJ301 |
| 23 | (a) `DataIntegrityViolationException` (vi phạm FK từ `student_courses`) · (b) gỡ 2 SV · còn 5 course: AIL303, HSF302, MKT101, PRJ301, SWP391 · IA002: HSF302 |
| 24 | Xoá **1** dòng (Cuong–PRJ301) · Thống kê: AIL303 4/4 – 3.700 · HSF302 5/6 – 3.440 · MKT101 2/4 – 2.700 · PRJ301 3/5 – 3.633 · SWP391 2/4 – 3.750 · Chưa đăng ký: *Dang Van Giang, Hoang Van Em, Le Van Cuong* |

> Thứ tự chạy trong `Exercise2Runner`: Part B → Part C → Part D → **Bonus** → Part E.

---

## 6. Quy định commit Git

### 6.1 Yêu cầu

1. Tạo nhánh mới từ nhánh của Exercise 1: `git checkout -b exercise2`.
2. **Mỗi TODO làm xong và chạy đúng là một commit** — không gộp nhiều TODO, không commit code lỗi biên dịch.
3. Commit theo **đúng thứ tự thực hiện**.
4. Message theo chuẩn **Conventional Commits** như Exercise 1, footer ghi `Refs: EX2 TODO <số>`.
5. Không commit mật khẩu thật.

### 6.2 Chuẩn commit message

```
<type>(<scope>): <subject>

[body — tuỳ chọn]

Refs: EX2 TODO <số>
```

- `type`: `feat` · `fix` · `chore` · `refactor` · `docs` · `test` · `style`
- `scope`: `setup`, `entity`, `repository`, `data`, `builtin`, `derived`, `query`, `spec`, `enrollment`, `course`
- `subject`: tiếng Anh, thể mệnh lệnh, viết thường chữ đầu, không dấu chấm cuối, ≤ 72 ký tự.

**Lệnh commit mẫu**
```bash
git add .
git commit -m "feat(entity): map many-to-many between Student and Course" -m "Refs: EX2 TODO 3"
```

### 6.3 Commit message mẫu cho từng TODO

| TODO | Commit message (tiêu đề) |
|---|---|
| 1 | `chore(setup): create exercise2 branch and profile-based runners` |
| 2 | `feat(entity): add Course entity as inverse side` |
| 3 | `feat(entity): map many-to-many between Student and Course` |
| 4 | `feat(repository): add CourseRepository and enrollment service skeleton` |
| 5 | `feat(data): seed courses and student enrollments` |
| 6 | `feat(builtin): count, sort and find courses by id` |
| 7 | `feat(enrollment): navigate courses of student and students of course` |
| 8 | `feat(derived): find courses by code and semester` |
| 9 | `feat(derived): find students by enrolled course code` |
| 10 | `feat(derived): find courses by student and department with distinct` |
| 11 | `feat(derived): find unenrolled students and empty courses` |
| 12 | `feat(query): find good students of course with jpql join` |
| 13 | `feat(query): add course statistics with dto projection` |
| 14 | `feat(query): summarize student credits with group by having` |
| 15 | `feat(query): find full courses and busy students with size` |
| 16 | `fix(query): load collections with join fetch and entity graph` |
| 17 | `feat(query): get top enrolled courses with native sql` |
| 18 | `feat(query): add enrollment view interface projection` |
| 19 | `feat(query): paginate students of course` |
| 25 | `feat(spec): add dynamic enrollment search with specification` |
| 20 | `feat(enrollment): enroll student with business rules` |
| 21 | `feat(enrollment): unenroll student from course` |
| 22 | `feat(enrollment): switch course in one transaction` |
| 23 | `feat(course): delete course safely from owning side` |
| 24 | `feat(enrollment): remove enrollments of inactive students` |

> Bonus TODO 25 commit **trước Part E** vì chạy trên dữ liệu gốc (nếu làm sau cùng thì commit sau cùng cũng được).

---

## 7. Ràng buộc & lưu ý

- Dùng `Set` (không dùng `List`) cho cả 2 phía của `@ManyToMany`.
- **Không** dùng `CascadeType.REMOVE`/`ALL` ở quan hệ N–N (xoá 1 course không được kéo theo xoá student).
- Chỉ **owning side** (`Student.courses`) quyết định dữ liệu bảng `student_courses` → mọi thao tác thêm/gỡ phải đi qua helper `enroll()`/`unenroll()` của `Student`.
- **Không** tạo entity cho bảng `student_courses` (bài này dùng `@ManyToMany` thuần).
- **Không** dùng `EntityManager` trực tiếp; `Exercise2Runner` **không** gọi repository.
- Part C **chỉ** dùng derived query; Part D **bắt buộc** dùng `@Query` (riêng TODO 16c dùng `@EntityGraph`).
- JPQL viết theo **tên entity/field** (`s.courses`, `c.students`); native SQL viết theo **tên bảng/cột** (`student_courses.course_id`).
- Không dùng `@Data`; `toString()` không chứa collection; `equals/hashCode` theo business key.
- Không nối chuỗi tham số vào query — luôn dùng `:param`.

---

## 8. Checklist hoàn thành

### Part A — Mapping
- [ ] TODO 1 — Có nhánh `exercise2`; `ExerciseRunner` cũ chỉ chạy với profile `ex1`; app chạy với profile `ex2`, vẫn kết nối `HSF302_CH4`.
- [ ] TODO 2 — `Course` có `@ManyToMany(mappedBy = "courses")`, `Set<Student>`, `equals/hashCode` theo `code`.
- [ ] TODO 3 — `Student` có `@ManyToMany` + `@JoinTable` đúng tên bảng/cột; helper `enroll/unenroll` đồng bộ 2 chiều; `equals/hashCode` theo `studentCode`.
- [ ] TODO 3 — Console có `create table student_courses (student_id ..., course_id ..., primary key (student_id, course_id))` và 2 FK.
- [ ] TODO 4 — `CourseRepository`; 2 Service interface + 2 ServiceImpl (`@Service`, `@Transactional(readOnly = true)`).
- [ ] TODO 5 — Seed 6 course, 18 dòng `student_courses`; thứ tự runner: `DataInitializer(1)` → `CourseDataInitializer(2)` → `Exercise2Runner(3)`.

### Part B
- [ ] TODO 6 — `count()`, `findAll(Sort)`, `findById()` xử lý `Optional`.
- [ ] TODO 7 — Duyệt `getCourses()` / `getStudents()` **trong** Service, không lỗi Lazy.

### Part C
- [ ] TODO 8 — `findByCode`, `findBySemesterOrderByCodeAsc`, `countBySemester`.
- [ ] TODO 9 — `findByCourses_Code...`, `countByCourses_Code`, `...AndActiveTrue...`.
- [ ] TODO 10 — `findByStudents_StudentCode...`, so sánh có/không `Distinct`.
- [ ] TODO 11 — `IsEmpty` trên collection ở cả 2 phía, `existsBy...And...`.

### Part D
- [ ] TODO 12 — JPQL `JOIN s.courses c`.
- [ ] TODO 13 — `LEFT JOIN c.students` + `GROUP BY` + DTO; MKT101 hiển thị 0.
- [ ] TODO 14 — `SUM` + `HAVING`, `ORDER BY` theo aggregate.
- [ ] TODO 15 — `SIZE(c.students)`, `SIZE(s.courses)`.
- [ ] TODO 16 — Tái hiện `LazyInitializationException`; sửa bằng `JOIN FETCH` và `@EntityGraph`.
- [ ] TODO 17 — Native SQL `TOP (:n)` + `LEFT JOIN student_courses` + interface projection.
- [ ] TODO 18 — Interface projection alias khớp getter.
- [ ] TODO 19 — `@Query` có `countQuery`, in đủ 3 trang.

### Part E
- [ ] TODO 20 — Đủ 3 quy tắc nghiệp vụ, ném `IllegalStateException`; thêm dòng vào `student_courses` bằng dirty checking.
- [ ] TODO 21 — `unenroll` chỉ xoá dòng bảng trung gian, không xoá `Student`/`Course`.
- [ ] TODO 22 — Lần đổi lớp thất bại **rollback** toàn bộ.
- [ ] TODO 23 — Giải thích được vì sao xoá từ inverse side lỗi FK; `deleteCourse` gỡ từ owning side rồi mới xoá.
- [ ] TODO 24 — `@Modifying(clearAutomatically = true)` + native DELETE, trả về số dòng.

### Bonus
- [ ] TODO 25 — `EnrollmentSpecs` null-safe, `join` + `query.distinct(true)`, không trùng kết quả.

### Tầng Service & chất lượng code
- [ ] Mọi method trong bảng mục 4.0 được khai báo ở interface và cài đặt ở ServiceImpl; Runner gọi qua Service cho **mọi** TODO 6–25.
- [ ] Method ghi (TODO 20–24) có `@Transactional` (import `org.springframework.transaction.annotation.Transactional`).
- [ ] Validate theo mục 4.0; phân biệt `IllegalArgumentException` / `IllegalStateException`.
- [ ] Không exception ngoài ý muốn (trừ các exception chủ động bắt ở TODO 16a, 20–23).
- [ ] Kết quả console khớp mục 5.

### Git
- [ ] Làm trên nhánh `exercise2`; mỗi TODO **1 commit** (≥ 24 commit, thêm 1 nếu làm bonus), đúng thứ tự.
- [ ] Message đúng `<type>(<scope>): <subject>`, footer `Refs: EX2 TODO <số>`.
- [ ] Không commit `target/`, `.idea/`, mật khẩu thật.

---

## 9. Thang điểm

| Phần | Điểm |
|---|---|
| Part A — Mapping Many-To-Many (TODO 1–5) | 2.0 |
| Part B — Built-in & điều hướng (TODO 6–7) | 1.0 |
| Part C — Derived query (TODO 8–11) | 2.0 |
| Part D — Custom query (TODO 12–19) | 3.0 |
| Part E — Thay đổi dữ liệu (TODO 20–24) | 2.0 |
| **Tổng** | **10.0** |
| Trừ điểm Git: thiếu commit cho một TODO / commit sai chuẩn | −0.1 mỗi lỗi (tối đa −1.0) |
| Bonus — Specification (TODO 25) | +1.0 |

**Nộp bài:** link repository (nhánh `exercise2`) hoặc file nén có thư mục `.git`, kèm ảnh chụp `git log --oneline`.
