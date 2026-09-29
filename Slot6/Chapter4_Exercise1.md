# Chapter 4 — Exercise 1: Quản lý Sinh viên – Khoa với Spring Data JPA (One-To-Many)

> **Mục tiêu:** Áp dụng lý thuyết Chapter 4 — Spring Data JPA:
> - Ánh xạ quan hệ **One-To-Many / Many-To-One** giữa `Department` và `Student`.
> - Dùng các **method có sẵn** của `JpaRepository` (CRUD, Sort, Pageable).
> - Viết **derived query** bằng keyword (`And`, `Between`, `ContainingIgnoreCase`, `EndingWith`, `IsNull`, `True`, `After`, `Top`, `OrderBy`, nested property…).
> - Viết **custom query** với `@Query`: JPQL, LIKE, aggregate + DTO, subquery, `JOIN FETCH`, native SQL, interface projection, phân trang.
> - Viết **modifying query** (`@Modifying` + `@Transactional`) và derived delete.
> - *(Bonus)* Tìm kiếm động bằng **Specification**.
> - Tổ chức code theo kiến trúc **N-layer**: `ExerciseRunner` → **Service** (interface + implementation) → **Repository** → Entity.
> - Quản lý mã nguồn bằng **Git**: mỗi TODO hoàn thành là **một commit** theo chuẩn **Conventional Commits**.
>
> **Thời gian gợi ý:** 3 – 4 giờ · **Hình thức:** Console app (Spring Boot + `CommandLineRunner`), chưa cần Web/Controller.
>
> **Hướng dẫn chi tiết từng bước:** xem `Chapter4_Exercise1_guide.md`

---

## 1. Công nghệ

| Thành phần | Phiên bản / lựa chọn |
|---|---|
| JDK | 17+ |
| Spring Boot | 3.2.x |
| Dependencies | Spring Data JPA, MS SQL Server Driver, Lombok |
| Database | SQL Server — database `HSF302_CH4` |
| Build | Maven |

**Cấu trúc package bắt buộc** (base package `com.hsf302.ch4`):

```
com.hsf302.ch4
├── Chapter4Application.java
├── pojo/          Department.java, Student.java, Gender.java
├── dto/           DepartmentStatDTO.java, StudentSummary.java
├── repository/    DepartmentRepository.java, StudentRepository.java
├── specification/ StudentSpecs.java            (bonus)
├── service/       DepartmentService.java, DepartmentServiceImpl.java,
│                  StudentService.java, StudentServiceImpl.java
└── runner/        DataInitializer.java, ExerciseRunner.java
```

---

## 2. Mô hình dữ liệu

```
┌──────────────────────┐ 1          N ┌────────────────────────────┐
│ departments          │──────────────│ students                   │
├──────────────────────┤              ├────────────────────────────┤
│ id (PK, identity)    │              │ id (PK, identity)          │
│ code (unique)        │              │ student_code (unique)      │
│ name                 │              │ full_name                  │
└──────────────────────┘              │ email (unique, nullable)   │
                                      │ gender  (MALE/FEMALE)      │
                                      │ dob     (date)             │
                                      │ gpa     (float)            │
                                      │ active  (bit)              │
                                      │ department_id (FK, NOT NULL)│
                                      └────────────────────────────┘
```

- `Student` là **owning side** (`@ManyToOne` + `@JoinColumn(name = "department_id")`, fetch **LAZY**).
- `Department` là **inverse side** (`@OneToMany(mappedBy = "department")`), **không cascade REMOVE**.
- `Gender` là enum, lưu dạng **chuỗi**.

---

## 3. Dữ liệu mẫu (seed bởi `DataInitializer`)

**Department**

| id | code | name |
|---|---|---|
| 1 | SE | Software Engineering |
| 2 | AI | Artificial Intelligence |
| 3 | IA | Information Assurance |
| 4 | GD | Graphic Design *(chưa có sinh viên)* |

**Student**

| id | studentCode | fullName | email | gender | dob | gpa | active | dept |
|---|---|---|---|---|---|---|---|---|
| 1 | SE001 | Nguyen Van An | an.nv@fpt.edu.vn | MALE | 2005-03-15 | 3.2 | true | SE |
| 2 | SE002 | Tran Thi Binh | binh.tt@fpt.edu.vn | FEMALE | 2004-07-22 | 3.8 | true | SE |
| 3 | SE003 | Le Van Cuong | cuong.lv@fpt.edu.vn | MALE | 2003-11-05 | 2.5 | **false** | SE |
| 4 | AI001 | Pham Thi Dung | dung.pt@fpt.edu.vn | FEMALE | 2006-01-10 | 3.5 | true | AI |
| 5 | AI002 | Hoang Van Em | em.hv@gmail.com | MALE | 2002-09-30 | 2.8 | true | AI |
| 6 | AI003 | Vo Thi Hoa | hoa.vt@fpt.edu.vn | FEMALE | 2005-05-18 | 3.9 | true | AI |
| 7 | IA001 | Dang Van Giang | giang.dv@gmail.com | MALE | 2001-12-01 | 1.9 | **false** | IA |
| 8 | IA002 | Bui Thi Lan | lan.bt@fpt.edu.vn | FEMALE | 2004-02-14 | 3.1 | true | IA |
| 9 | SE004 | Nguyen Thi Mai | mai.nt@fpt.edu.vn | FEMALE | 2003-08-08 | 3.6 | true | SE |
| 10 | IA003 | Do Van Nam | *(null)* | MALE | 2005-10-20 | 2.2 | true | IA |

> Dùng `spring.jpa.hibernate.ddl-auto=create` → mỗi lần chạy app, bảng được tạo lại và dữ liệu được seed lại từ đầu.
> Các TODO chạy **theo đúng thứ tự** trong `ExerciseRunner`; Part B → D chỉ **đọc** dữ liệu, Part E mới **thay đổi** dữ liệu.

---

## 4. Yêu cầu — các TODO

**Quy ước in kết quả:** mỗi TODO in một tiêu đề `===== TODO x: ... =====`, sau đó in kết quả. `Student.toString()` **không được** truy cập `department` (để tránh `LazyInitializationException`).


### 4.0 Kiến trúc bắt buộc & tầng Service

```
ExerciseRunner ──► DepartmentService / StudentService          (interface)
                        ▲ implements
                 DepartmentServiceImpl / StudentServiceImpl       (@Service, @Transactional)
                        │
                 DepartmentRepository / StudentRepository         (Spring Data JPA)
```

**Quy định**
1. `ExerciseRunner` **chỉ inject và gọi Service interface** (`DepartmentService`, `StudentService`), **không** inject repository.
2. Mỗi TODO từ 6 trở đi làm theo thứ tự: khai báo method trong **Repository** (nếu cần) → khai báo trong **Service interface** → cài đặt trong **ServiceImpl** → gọi từ **Runner**.
3. `ServiceImpl` gắn `@Service`, `@Transactional(readOnly = true)` ở mức class; **method ghi dữ liệu** (Part E) gắn thêm `@Transactional`.
4. Inject repository bằng **constructor** (`@RequiredArgsConstructor` + field `final`).
5. Kiểm tra tham số đầu vào (range, giá trị rỗng, số âm…) đặt ở **Service**, ném `IllegalArgumentException` khi không hợp lệ.
6. `DataInitializer` (seed dữ liệu) được phép dùng repository trực tiếp.

**Service (method bắt buộc)**

| TODO | `StudentService` | `DepartmentService` |
|---|---|---|
| 6 | `long count()` · `Optional<Student> findById(Long id)` | `long count()` · `boolean existsById(Long id)` |
| 7 | `List<Student> findAllOrderByGpaDesc()` · `Page<Student> findPage(int pageIndex, int size, String sortField)` | |
| 8 | `Optional<Student> findByStudentCode(String code)` · `boolean isEmailExisted(String email)` · `long countActive()` | |
| 9 | `List<Student> searchByName(String kw)` · `List<Student> findByEmailDomain(String domain)` · `List<Student> findWithoutEmail()` | |
| 10 | `List<Student> findByGpaRange(double min, double max)` · `List<Student> findActiveByGender(Gender g)` · `List<Student> findBornAfter(LocalDate d)` | |
| 11 | `List<Student> findByDepartment(String deptCode)` · `long countByDepartment(String deptCode)` · `List<Student> findTop3ByGpa()` | `List<Department> findDepartmentsWithoutStudents()` |
| 12 | `List<Student> findGoodStudents(String deptCode, double minGpa)` | |
| 13 | `List<Student> searchByKeyword(String kw)` | |
| 14 | | `List<DepartmentStatDTO> getStatistics()` |
| 15 | `List<Student> findAboveAverageGpa()` | |
| 16 | | `Optional<Department> findByCode(String code)` · `Department getWithStudents(String code)` |
| 17 | `List<Student> findTopNInDepartment(String deptCode, int n)` | |
| 18 | `List<StudentSummary> getActiveSummaries()` | |
| 19 | `Page<Student> findActiveByDepartment(String deptCode, int pageIndex, int size)` | |
| 20 | `Student updateGpa(String code, double newGpa)` | |
| 21 | `int deactivateLowGpa(double threshold)` | |
| 22 | | `int transferStudentsAndDelete(String fromCode, String toCode)` · `List<Department> findAll()` |
| 23 | `long deleteInactiveStudents()` | |
| 24 | `List<Student> search(String kw, String deptCode, Double minGpa, Boolean active)` | |

**Validate bắt buộc ở Service:** `findPage` (pageIndex ≥ 0, size > 0) · `searchByName`/`searchByKeyword` (từ khoá rỗng → trả list rỗng) · `findByEmailDomain` (tự thêm `@` nếu thiếu) · `findByGpaRange` (min ≤ max) · `findTopNInDepartment` (n > 0) · `updateGpa` (0 ≤ GPA ≤ 4) · `transferStudentsAndDelete` (2 khoa khác nhau, khoa phải tồn tại).

### Part A — Thiết lập dự án & mapping (2.0 điểm)

| TODO | Yêu cầu |
|---|---|
| **TODO 1** | Tạo project Spring Boot, cấu hình `application.properties` kết nối SQL Server database `HSF302_CH4`, `ddl-auto=create`, bật `show-sql` + `format_sql`. |
| **TODO 2** | Tạo enum `Gender { MALE, FEMALE }` và entity `Department` (bảng `departments`): `id` identity, `code` unique not null length 10, `name` not null length 100, `List<Student> students` với `@OneToMany(mappedBy = "department")`. Viết helper `addStudent(Student s)` đồng bộ 2 chiều. **Không dùng `@Data`.** |
| **TODO 3** | Tạo entity `Student` (bảng `students`) với đủ field như mô hình; `@Enumerated(EnumType.STRING)` cho `gender`; `@ManyToOne(fetch = FetchType.LAZY, optional = false)` + `@JoinColumn(name = "department_id", nullable = false)`. Override `toString()` **không chứa** `department`. |
| **TODO 4** | Tạo `DepartmentRepository extends JpaRepository<Department, Long>` và `StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student>`. Tạo khung tầng Service: interface `DepartmentService`, `StudentService` và class `DepartmentServiceImpl`, `StudentServiceImpl` (`@Service`, `@Transactional(readOnly = true)`, inject repository qua constructor). |
| **TODO 5** | Tạo `DataInitializer implements CommandLineRunner` (`@Order(1)`) seed đúng dữ liệu ở mục 3 bằng `saveAll()`. Tạo `ExerciseRunner` (`@Order(2)`) để chạy các TODO tiếp theo; `ExerciseRunner` **chỉ inject** `DepartmentService` và `StudentService`. |

### Part B — Method có sẵn của `JpaRepository` (1.5 điểm)

| TODO | Yêu cầu | Method dùng |
|---|---|---|
| **TODO 6** | In tổng số department và student; tìm student id = 1 và id = 99 (in `"Not found"` nếu không có); kiểm tra department id = 4 có tồn tại không. | `count()`, `findById()`, `existsById()` |
| **TODO 7** | (a) In toàn bộ student sắp xếp **GPA giảm dần**. (b) Lấy **trang thứ 2** (kích thước 3) sắp xếp theo `fullName` tăng dần; in nội dung trang, `totalElements`, `totalPages`, `hasNext`. | `findAll(Sort)`, `findAll(Pageable)` |

### Part C — Derived query (keyword) (2.0 điểm)

Khai báo method trong repository **chỉ bằng tên method** (không dùng `@Query`).

| TODO | Yêu cầu |
|---|---|
| **TODO 8** | (a) Tìm student theo `studentCode` = `"AI002"` và `"XX999"` (trả `Optional`). (b) Kiểm tra email `binh.tt@fpt.edu.vn` đã tồn tại chưa. (c) Đếm số student đang active. |
| **TODO 9** | (a) Tìm student có `fullName` chứa `"nguyen"` (không phân biệt hoa thường). (b) Tìm student có email kết thúc bằng `"@gmail.com"`. (c) Tìm student **chưa có email**. |
| **TODO 10** | (a) Tìm student có GPA trong khoảng **[3.0, 3.6]**, sắp xếp GPA giảm dần. (b) Tìm student **nam** đang **active**. (c) Tìm student sinh **sau** ngày 2005-01-01. |
| **TODO 11** | (a) Tìm student thuộc department có code `"SE"`, sắp xếp theo `fullName` tăng dần *(nested property)*. (b) Đếm số student của department `"AI"`. (c) Lấy **top 3** student GPA cao nhất. (d) Tìm các department **chưa có student** nào. |

### Part D — Custom query với `@Query` (3.0 điểm)

| TODO | Yêu cầu | Kỹ thuật |
|---|---|---|
| **TODO 12** | Tìm student thuộc department `code` có GPA ≥ `minGpa`, sắp xếp GPA giảm dần. Chạy với `("SE", 3.0)`. | JPQL + named parameter |
| **TODO 13** | Tìm student có `fullName` **hoặc** `email` chứa từ khoá (không phân biệt hoa thường), sắp xếp theo tên. Chạy với `"hoa"` và `"gmail"`. | JPQL + `LIKE` + `CONCAT` + `LOWER` |
| **TODO 14** | Thống kê theo **từng** department (kể cả department không có student): `code`, `name`, số student, GPA trung bình; sắp xếp theo `code`. Trả về record `DepartmentStatDTO`. In GPA với 3 chữ số thập phân. | `LEFT JOIN` + `GROUP BY` + `COUNT/AVG` + constructor expression |
| **TODO 15** | Tìm student có GPA **lớn hơn GPA trung bình** của toàn bộ student, sắp xếp GPA giảm dần. | Subquery |
| **TODO 16** | (a) Dùng `findByCode("AI")` rồi gọi `getStudents().size()` → **bắt và in** exception xảy ra. (b) Viết query lấy department **kèm danh sách student** trong 1 câu SQL, in department và các student của nó. | `LazyInitializationException`, `JOIN FETCH` |
| **TODO 17** | Lấy **top N** student GPA cao nhất của một department bằng **native SQL** (SQL Server `TOP`). Chạy với `("SE", 2)`. | `nativeQuery = true` |
| **TODO 18** | Lấy danh sách student **active** gồm `studentCode`, `fullName`, `gpa`, **tên department**, sắp xếp theo `fullName`. Trả về interface `StudentSummary`. | Interface projection + alias |
| **TODO 19** | Phân trang student **active** của một department, sắp xếp GPA giảm dần, mỗi trang 2 phần tử. Chạy với `"SE"`, in trang 0 và trang 1. | `@Query` + `Pageable` → `Page<Student>` |

### Part E — Thay đổi dữ liệu (1.5 điểm)

Các thao tác ghi đặt trong ServiceImpl (`StudentServiceImpl`; riêng TODO 22 đặt trong `DepartmentServiceImpl`) và gắn `@Transactional` trên từng method ghi.

| TODO | Yêu cầu | Kỹ thuật |
|---|---|---|
| **TODO 20** | Cập nhật GPA của student `SE001` thành **3.4**; in lại student sau cập nhật. | `findBy...` + setter (dirty checking) / `save()` |
| **TODO 21** | Chuyển thành **inactive** tất cả student đang active có GPA < **2.5**; in số dòng bị ảnh hưởng và số student active còn lại. | `@Modifying` JPQL UPDATE |
| **TODO 22** | Chuyển **toàn bộ** student của department `IA` sang department `SE`, sau đó **xoá** department `IA`. In số student được chuyển, số student của SE, và danh sách department còn lại. | `@Modifying` UPDATE theo entity + `deleteById` trong 1 transaction |
| **TODO 23** | Xoá toàn bộ student **inactive**; in số bản ghi đã xoá, tổng student còn lại, và chạy lại thống kê của TODO 14. | Derived delete `deleteByActiveFalse()` |

### Bonus — Specification (+1.0 điểm)

| TODO | Yêu cầu |
|---|---|
| **TODO 24** | Viết `StudentSpecs` với các điều kiện **tuỳ chọn** (null = bỏ qua): `nameContains(kw)`, `inDepartment(code)`, `gpaAtLeast(min)`, `isActive(Boolean)`. Viết `search(kw, code, minGpa, active)` ghép các điều kiện, kết quả sắp xếp theo `fullName` tăng dần. Chạy (trên dữ liệu gốc — gọi **trước Part E**): `search(null, "AI", 3.0, true)` và `search("van", null, null, null)`. |

---

## 5. Kết quả mong đợi (để tự kiểm tra)

| TODO | Kết quả |
|---|---|
| 6 | 4 departments, 10 students · id=1 → *Nguyen Van An* · id=99 → *Not found* · existsById(4) → `true` |
| 7a | Hoa 3.9, Binh 3.8, Mai 3.6, Dung 3.5, An 3.2, Lan 3.1, Em 2.8, Cuong 2.5, Nam 2.2, Giang 1.9 |
| 7b | *Hoang Van Em, Le Van Cuong, Nguyen Thi Mai* · totalElements = 10 · totalPages = 4 · hasNext = true |
| 8 | AI002 → *Hoang Van Em* · XX999 → không tìm thấy · exists → `true` · active = **8** |
| 9 | (a) *Nguyen Van An, Nguyen Thi Mai* · (b) *Hoang Van Em, Dang Van Giang* · (c) *Do Van Nam* |
| 10 | (a) Mai 3.6, Dung 3.5, An 3.2, Lan 3.1 · (b) *An, Em, Nam* · (c) *An, Dung, Hoa, Nam* |
| 11 | (a) *Le Van Cuong, Nguyen Thi Mai, Nguyen Van An, Tran Thi Binh* · (b) 3 · (c) Hoa, Binh, Mai · (d) *GD – Graphic Design* |
| 12 | Binh 3.8, Mai 3.6, An 3.2 |
| 13 | `"hoa"` → *Hoang Van Em, Vo Thi Hoa* · `"gmail"` → *Dang Van Giang, Hoang Van Em* |
| 14 | AI: 3 – 3.400 · GD: 0 – null · IA: 3 – 2.400 · SE: 4 – 3.275 |
| 15 | Hoa 3.9, Binh 3.8, Mai 3.6, Dung 3.5, An 3.2, Lan 3.1 (TB = 3.05) |
| 16 | (a) `LazyInitializationException` · (b) AI – Artificial Intelligence: Dung, Em, Hoa |
| 17 | Binh 3.8, Mai 3.6 |
| 18 | 8 dòng: Bui Thi Lan (IA), Do Van Nam (IA), Hoang Van Em (AI), Nguyen Thi Mai (SE), Nguyen Van An (SE), Pham Thi Dung (AI), Tran Thi Binh (SE), Vo Thi Hoa (AI) |
| 19 | Trang 0: Binh, Mai · Trang 1: An · totalElements = 3 · totalPages = 2 |
| 24 | `(null,"AI",3.0,true)` → *Pham Thi Dung, Vo Thi Hoa* · `("van",null,null,null)` → *Dang Van Giang, Do Van Nam, Hoang Van Em, Le Van Cuong, Nguyen Van An* |
| 20 | SE001 GPA = 3.4 |
| 21 | 1 dòng bị ảnh hưởng (Do Van Nam) · active còn **7** |
| 22 | Chuyển 3 student · SE có 7 student · Department còn: SE, AI, GD |
| 23 | Xoá 3 (Cuong, Giang, Nam) · còn 7 student · Thống kê: AI 3 – 3.400 · GD 0 – null · SE 4 – 3.475 |

> Thứ tự chạy trong `ExerciseRunner`: Part B → Part C → Part D → **Bonus** → Part E.

---

## 6. Quy định commit Git

### 6.1 Yêu cầu

1. Khởi tạo Git ngay ở **TODO 1** (`git init`) và thêm `.gitignore` (bỏ qua `target/`, `.idea/`, `*.iml`, `.vscode/`…).
2. **Mỗi TODO làm xong và chạy đúng là một commit** — không gộp nhiều TODO vào một commit, không commit code đang lỗi biên dịch.
3. Commit theo **đúng thứ tự thực hiện**; lịch sử `git log` phải cho thấy tiến trình làm bài.
4. Commit message theo chuẩn **Conventional Commits** (mục 6.2), footer ghi `Refs: TODO <số>`.
5. Không commit mật khẩu thật: dùng mật khẩu chỉ dùng cho máy local, hoặc tách ra `application-local.properties` và đưa file đó vào `.gitignore`.
6. Bài làm thiếu commit hoặc commit sai chuẩn sẽ bị trừ điểm (xem mục 9).

### 6.2 Chuẩn commit message (Conventional Commits)

```
<type>(<scope>): <subject>
                                   ← dòng trống
[body — tuỳ chọn: giải thích VÌ SAO / thay đổi gì]
                                   ← dòng trống
Refs: TODO <số>
```

| Thành phần | Quy tắc |
|---|---|
| `type` | `feat` (thêm chức năng) · `fix` (sửa lỗi) · `chore` (cấu hình, setup, build) · `refactor` (sửa cấu trúc, không đổi hành vi) · `docs` (tài liệu) · `test` (kiểm thử) · `style` (format code) |
| `scope` | Phần bị ảnh hưởng: `setup`, `entity`, `repository`, `data`, `builtin`, `derived`, `query`, `spec`, `service` |
| `subject` | Tiếng Anh, **thể mệnh lệnh** (`add`, không viết `added`/`adds`), viết thường chữ đầu, **không** có dấu chấm cuối, dài tối đa ~72 ký tự |
| `body` | Tuỳ chọn, cách tiêu đề một dòng trống; giải thích lý do hoặc điểm đáng chú ý |
| `footer` | `Refs: TODO <số>` để liên kết với đề bài |

**Ví dụ đúng**
```
feat(derived): search by name, email suffix and null email

Refs: TODO 9
```
```
fix(query): load department students with join fetch

Truy cập students ngoài transaction gây LazyInitializationException,
thêm findByCodeWithStudents dùng LEFT JOIN FETCH

Refs: TODO 16
```

**Ví dụ sai**

| Message | Sai ở đâu |
|---|---|
| `update` / `done` / `fix bug` | Không có type/scope, không mô tả được thay đổi |
| `Feat: Added Student entity.` | Type viết hoa, thiếu scope, dùng thì quá khứ, có dấu chấm cuối |
| `feat(query): todo 12 13 14` | Gộp nhiều TODO, subject không mô tả nội dung |
| `feat: làm xong TODO 5` | Nên viết subject tiếng Anh, mô tả thay đổi thay vì ghi số TODO (số TODO để ở footer) |

**Lệnh commit mẫu**
```bash
git add .
git commit -m "feat(entity): add Student entity with many-to-one department" -m "Refs: TODO 3"
```
> Mỗi `-m` tạo một đoạn: `-m` thứ nhất là tiêu đề, các `-m` sau là body/footer.

### 6.3 Commit message mẫu cho từng TODO

| TODO | Commit message (tiêu đề) |
|---|---|
| 1 | `chore(setup): init spring boot project and configure sql server` |
| 2 | `feat(entity): add Gender enum and Department entity` |
| 3 | `feat(entity): add Student entity with many-to-one department` |
| 4 | `feat(repository): add repositories and service layer skeleton` |
| 5 | `feat(data): seed sample departments and students` |
| 6 | `feat(builtin): use count, findById and existsById` |
| 7 | `feat(builtin): sort and paginate students with findAll` |
| 8 | `feat(derived): find by student code, check email and count active` |
| 9 | `feat(derived): search by name, email suffix and null email` |
| 10 | `feat(derived): filter students by gpa range, gender and dob` |
| 11 | `feat(derived): query by department code, top gpa and empty dept` |
| 12 | `feat(query): find good students in department with jpql` |
| 13 | `feat(query): search students by keyword with jpql like` |
| 14 | `feat(query): add department statistics with dto projection` |
| 15 | `feat(query): find students above average gpa with subquery` |
| 16 | `fix(query): load department students with join fetch` |
| 17 | `feat(query): get top n students of department with native sql` |
| 18 | `feat(query): add active student summary interface projection` |
| 19 | `feat(query): paginate active students of department` |
| 24 | `feat(spec): add dynamic student search with specification` |
| 20 | `feat(service): update student gpa with dirty checking` |
| 21 | `feat(service): deactivate active students with low gpa` |
| 22 | `feat(service): transfer students and remove department` |
| 23 | `feat(service): delete inactive students` |

> Bonus TODO 24 được commit **trước Part E** vì trong `ExerciseRunner` nó chạy trên dữ liệu gốc. Nếu làm bonus sau cùng thì commit sau cùng cũng được.

**Kiểm tra lịch sử commit:**
```bash
git log --oneline
```
Kết quả mong đợi (mới nhất ở trên):
```
a1b2c3d feat(service): delete inactive students
...
e4f5a6b feat(entity): add Gender enum and Department entity
9f8e7d6 chore(setup): init spring boot project and configure sql server
```

---

## 7. Ràng buộc & lưu ý

- **Không** dùng `EntityManager` trực tiếp (trừ khi làm thêm ngoài yêu cầu).
- `ExerciseRunner` **không** được gọi repository trực tiếp — mọi chức năng đi qua Service.
- Part C **chỉ** dùng derived query; Part D **bắt buộc** dùng `@Query`.
- JPQL phải viết theo **tên entity/field**; native SQL viết theo **tên bảng/cột**.
- Không dùng `@Data` cho entity có quan hệ 2 chiều.
- Không dùng `CascadeType.REMOVE`/`ALL` trên `Department.students`.
- Không nối chuỗi tham số vào query — luôn dùng `:param`.

---

## 8. Checklist hoàn thành

### Part A — Setup & Mapping
- [ ] TODO 1 — Project chạy được, kết nối SQL Server `HSF302_CH4`, console hiện SQL `create table`.
- [ ] TODO 2 — `Department` có `@OneToMany(mappedBy = "department")`, `code` unique, helper `addStudent()`.
- [ ] TODO 2 — Enum `Gender` lưu dạng chuỗi (`EnumType.STRING`).
- [ ] TODO 3 — `Student` có `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "department_id")`; cột FK `department_id` được tạo.
- [ ] TODO 3 — `toString()` của entity không truy cập quan hệ; không dùng `@Data`.
- [ ] TODO 4 — 2 repository extend `JpaRepository` (Student thêm `JpaSpecificationExecutor`).
- [ ] TODO 4 — Có interface `DepartmentService`, `StudentService` và class `...ServiceImpl` gắn `@Service`, `@Transactional(readOnly = true)`, inject repository qua constructor.
- [ ] TODO 5 — Seed đủ 4 department & 10 student; `DataInitializer` chạy trước `ExerciseRunner` (`@Order`).
- [ ] TODO 5 — `ExerciseRunner` chỉ inject `DepartmentService`, `StudentService` (không có field repository).

### Part B — Built-in methods
- [ ] TODO 6 — `count()`, `findById()` xử lý `Optional` đúng (có & không có), `existsById()`.
- [ ] TODO 7 — `findAll(Sort)` GPA giảm dần; `findAll(Pageable)` trang index **1** (trang thứ 2), in đủ thông tin `Page`.

### Part C — Derived query
- [ ] TODO 8 — `findByStudentCode`, `existsByEmail`, `countByActiveTrue`.
- [ ] TODO 9 — `ContainingIgnoreCase`, `EndingWith`, `IsNull`.
- [ ] TODO 10 — `Between` + `OrderBy...Desc`, `And` + `True`, `After`.
- [ ] TODO 11 — Nested property `Department_Code`, `countBy...`, `Top3`, `IsEmpty` trên collection.

### Part D — Custom query
- [ ] TODO 12 — JPQL với `@Param`.
- [ ] TODO 13 — JPQL `LIKE` + `CONCAT` + `LOWER`, xử lý email null.
- [ ] TODO 14 — `LEFT JOIN` + `GROUP BY` + constructor expression `new ...DepartmentStatDTO(...)`; GD hiển thị 0.
- [ ] TODO 15 — Subquery `AVG`.
- [ ] TODO 16 — Tái hiện & bắt `LazyInitializationException`; sửa bằng `JOIN FETCH`.
- [ ] TODO 17 — Native query `TOP (:n)` theo tên bảng/cột.
- [ ] TODO 18 — Interface projection với alias khớp getter.
- [ ] TODO 19 — `@Query` + `Pageable`, in 2 trang.

### Part E — Modifying
- [ ] TODO 20 — Cập nhật GPA trong `@Transactional` (dirty checking hoặc `save()`).
- [ ] TODO 21 — `@Modifying(clearAutomatically = true)` UPDATE, trả về số dòng.
- [ ] TODO 22 — Chuyển student & xoá department trong **cùng 1 transaction**, không lỗi FK.
- [ ] TODO 23 — `deleteByActiveFalse()` trả về số bản ghi đã xoá; thống kê cuối đúng.

### Bonus
- [ ] TODO 24 — `StudentSpecs` với điều kiện null-safe, ghép bằng `and()`, gọi `findAll(spec)`.

### Tầng Service
- [ ] Mọi method trong bảng *Hợp đồng Service* (mục 4.0) được khai báo trong interface và cài đặt trong `...ServiceImpl`.
- [ ] Runner gọi Service cho **mọi** TODO từ 6 đến 24.
- [ ] Method ghi dữ liệu (TODO 20–23) có `@Transactional` (không `readOnly`); import `org.springframework.transaction.annotation.Transactional`.
- [ ] TODO 22 chuyển sinh viên + xoá khoa trong **một** method của `DepartmentServiceImpl`.
- [ ] Có validate đầu vào theo mục 4.0 và ném `IllegalArgumentException` khi không hợp lệ.

### Chất lượng code
- [ ] Đúng cấu trúc package; đặt tên method rõ nghĩa.
- [ ] Không có exception không mong muốn khi chạy (trừ exception chủ động bắt ở TODO 16a).
- [ ] Kết quả console khớp bảng kết quả mong đợi ở mục 5.

### Git
- [ ] Đã `git init` và có `.gitignore` (không commit `target/`, `.idea/`, `*.iml`).
- [ ] Mỗi TODO hoàn thành đúng **1 commit** (≥ 23 commit, thêm 1 nếu làm bonus), theo thứ tự thực hiện.
- [ ] Mọi commit message đúng dạng `<type>(<scope>): <subject>`: tiếng Anh, thể mệnh lệnh, viết thường, không dấu chấm cuối.
- [ ] Mỗi commit có footer `Refs: TODO <số>`.
- [ ] Không có commit chứa code lỗi biên dịch; không commit mật khẩu thật.
- [ ] `git log --oneline` khớp (về nội dung) bảng commit mẫu ở mục 6.3.

---

## 9. Thang điểm

| Phần | Điểm |
|---|---|
| Part A — Setup & mapping (TODO 1–5) | 2.0 |
| Part B — Built-in methods (TODO 6–7) | 1.5 |
| Part C — Derived query (TODO 8–11) | 2.0 |
| Part D — Custom query (TODO 12–19) | 3.0 |
| Part E — Modifying (TODO 20–23) | 1.5 |
| **Tổng** | **10.0** |
| Trừ điểm Git: thiếu commit cho một TODO / commit sai chuẩn | −0.1 mỗi lỗi (tối đa −1.0) |
| Bonus — Specification (TODO 24) | +1.0 |

**Nộp bài:** link repository (hoặc file nén có thư mục `.git`) và ảnh chụp kết quả `git log --oneline`.
