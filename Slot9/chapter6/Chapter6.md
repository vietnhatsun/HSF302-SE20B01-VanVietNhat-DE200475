# Chapter 6 — CRUD Student với Spring MVC + Thymeleaf + SQL Server

> **Mục tiêu:** Nâng cấp project mẫu "CRUD Student với Thymeleaf" (Chapter 5 — Phần 3) từ **lưu dữ liệu trong bộ nhớ (ArrayList)** sang **lưu vào SQL Server** bằng **Spring Data JPA**, giữ nguyên giao diện và luồng CRUD.
>
> **Base package:** `com.hsf302.chapter6` · **Database:** SQL Server `HSF302_CH6` · **Stack:** Spring Boot 3.3.x, Java 17, Spring MVC, Thymeleaf, Spring Data JPA (Hibernate 6), Bean Validation, `mssql-jdbc`.
>
> Mỗi bước có **✅ Checklist kiểm tra** — chỉ chuyển sang bước tiếp theo khi đã tick hết checklist của bước hiện tại.

---

## Mục lục

- [PHẦN 0 — Tổng quan thay đổi](#phần-0--tổng-quan-thay-đổi)
- [PHẦN 1 — Chuẩn bị SQL Server](#phần-1--chuẩn-bị-sql-server)
- [PHẦN 2 — Xây dựng project step-by-step](#phần-2--xây-dựng-project-step-by-step)
  - [Bước 1: Tạo project & `pom.xml`](#bước-1-tạo-project--pomxml)
  - [Bước 2: Cấu trúc thư mục](#bước-2-cấu-trúc-thư-mục)
  - [Bước 3: `application.properties`](#bước-3-applicationproperties)
  - [Bước 4: Main Application](#bước-4-main-application--studentmanagementapplicationjava)
  - [Bước 5: Entity `Student`](#bước-5-entity--studentjava)
  - [Bước 6: Repository](#bước-6-repository--studentrepositoryjava)
  - [Bước 7: Service](#bước-7-service--studentservice--studentserviceimpl)
  - [Bước 8: Dữ liệu mẫu](#bước-8-dữ-liệu-mẫu--datainitializerjava)
  - [Bước 9: Controller](#bước-9-controller--studentcontrollerjava)
  - [Bước 10: HomeController](#bước-10-home-redirect--homecontrollerjava)
  - [Bước 11: CSS + Layout](#bước-11-css--layout--fragmentslayouthtml)
  - [Bước 12: `students/list.html`](#bước-12-template--studentslisthtml)
  - [Bước 13: `students/form.html`](#bước-13-template--studentsformhtml)
  - [Bước 14: `students/detail.html`](#bước-14-template--studentsdetailhtml)
  - [Bước 15: Kiểm thử tổng thể](#bước-15-kiểm-thử-tổng-thể)
- [PHẦN 3 — Luồng xử lý CRUD (MVC + JPA + SQL)](#phần-3--luồng-xử-lý-crud-mvc--jpa--sql)
- [PHẦN 4 — Lỗi thường gặp](#phần-4--lỗi-thường-gặp)
- [PHẦN 5 — Điểm cần nhớ & bài tập mở rộng](#phần-5--điểm-cần-nhớ--bài-tập-mở-rộng)

---

# PHẦN 0 — Tổng quan thay đổi

## 0.1 Kiến trúc trước và sau

```
Chapter 5 (in-memory)                    Chapter 6 (SQL Server)
─────────────────────                    ──────────────────────
Browser                                  Browser
  │                                        │
StudentController                        StudentController          (gần như giữ nguyên)
  │                                        │
StudentService (ArrayList)               StudentService (interface)
                                           │
                                         StudentServiceImpl         (@Transactional)
                                           │
                                         StudentRepository          (Spring Data JPA)
                                           │
                                         Hibernate → mssql-jdbc
                                           │
                                         SQL Server: HSF302_CH6.dbo.students
```

> Ý nghĩa quan trọng của MVC: **chỉ thay tầng Model (Service/Repository)**, Controller và View gần như **không đổi**. Đây là minh chứng cho *separation of concerns*.

## 0.2 Bảng thay đổi so với Chapter 5

| # | Chapter 5 | Chapter 6 | Lý do |
|---|---|---|---|
| 1 | `ArrayList` + `AtomicLong` trong `StudentService` | `StudentRepository extends JpaRepository` + SQL Server | Dữ liệu tồn tại sau khi tắt ứng dụng |
| 2 | `Student` là POJO | `Student` là `@Entity` → bảng `students` | ORM |
| 3 | `id` tự tăng bằng `AtomicLong` | `@GeneratedValue(strategy = IDENTITY)` | SQL Server tự sinh `IDENTITY(1,1)` |
| 4 | `int age`, `double gpa` | `Integer age`, `Double gpa` + `@NotNull` | Ô trống → báo "không được để trống" thay vì lỗi `typeMismatch` khó hiểu |
| 5 | Không kiểm tra trùng email | Kiểm tra trùng email (Service) + `UNIQUE` ở DB | Toàn vẹn dữ liệu |
| 6 | Service là 1 class | `StudentService` (interface) + `StudentServiceImpl` | Đúng chuẩn phân tầng, dễ mock khi test |
| 7 | Dữ liệu mẫu trong constructor Service | `DataInitializer` (`CommandLineRunner`) chỉ seed khi bảng rỗng | Không bị nhân đôi dữ liệu mỗi lần chạy |
| 8 | `update()` thay cả object trong list | Load entity → copy field → **dirty checking** tự `UPDATE` | Cách cập nhật chuẩn của JPA |
| 9 | Layout dùng `th:replace="~{fragments/layout :: layout}"` nhưng `layout.html` **không có** `th:fragment="layout"` → **lỗi template** | Layout **có tham số**: `th:fragment="layout(title, content)"` | Sửa lỗi của bản gốc |
| 10 | `confirm(... /*[[${student.name}]]*/ ...)` trong thuộc tính `onsubmit` → **không được Thymeleaf xử lý** | `th:data-name` + `this.dataset.name` | Sửa lỗi của bản gốc |
| 11 | `majors` được add lặp lại ở 4 method | `@ModelAttribute("majors")` method-level | Bớt lặp code |
| 12 | Flash message chỉ có ở `list.html` | Đặt trong layout → mọi trang đều hiển thị | Tái sử dụng |
| 13 | CSS inline trong layout | `static/css/style.css` | Đúng cấu trúc thư mục đã khai báo |
| 14 | — | `student.setId(null)` khi Create | Chống người dùng gửi kèm `id` để ghi đè bản ghi khác |

---

# PHẦN 1 — Chuẩn bị SQL Server

> Làm **một lần** cho máy của bạn. Nếu đã làm ở Chapter 4/5 (database `HSF302_CH4`, `HSF302_CH5` chạy được) thì chỉ cần thực hiện mục **1.4**.

## 1.1 Bật TCP/IP, cổng 1433

JDBC kết nối qua **TCP/IP**, trong khi SSMS có thể kết nối qua Shared Memory → SSMS vào được **không** có nghĩa là Java vào được.

1. Mở **SQL Server Configuration Manager** (gõ `SQLServerManager16.msc` / `SQLServerManager15.msc` trong Run nếu không tìm thấy).
2. **SQL Server Network Configuration → Protocols for MSSQLSERVER** (hoặc `SQLEXPRESS`) → **TCP/IP → Enabled**.
3. Double-click **TCP/IP → tab IP Addresses → kéo xuống IPAll**:
   - `TCP Dynamic Ports` = *(để trống)*
   - `TCP Port` = `1433`
4. **SQL Server Services → SQL Server (MSSQLSERVER) → Restart**.

## 1.2 Bật SQL Server Authentication & tài khoản `sa`

1. SSMS → chuột phải server → **Properties → Security** → chọn **SQL Server and Windows Authentication mode** → OK → **Restart service**.
2. Chạy (đăng nhập bằng Windows Authentication):

```sql
ALTER LOGIN sa ENABLE;
ALTER LOGIN sa WITH PASSWORD = '12345', CHECK_POLICY = OFF;
```

> `CHECK_POLICY = OFF` vì mật khẩu `12345` không đạt chính sách độ phức tạp của Windows. Chỉ dùng cho máy học tập.

## 1.3 Kiểm tra cổng

PowerShell:
```powershell
Test-NetConnection localhost -Port 1433
```
Kết quả cần có: `TcpTestSucceeded : True`.

## 1.4 Tạo database `HSF302_CH6`

> Hibernate **chỉ tạo bảng**, **không tạo database**. Database phải tồn tại trước.

```sql
IF DB_ID(N'HSF302_CH6') IS NULL
    CREATE DATABASE HSF302_CH6;
GO
```

*(Tuỳ chọn)* Nếu bạn muốn tự tạo bảng thay vì để Hibernate tạo (khi đó dùng `ddl-auto=validate`):

```sql
USE HSF302_CH6;
GO
CREATE TABLE students (
    id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    name   NVARCHAR(50)  NOT NULL,
    email  NVARCHAR(100) NOT NULL CONSTRAINT UK_students_email UNIQUE,
    age    INT           NOT NULL,
    major  NVARCHAR(20)  NOT NULL,
    gpa    FLOAT         NOT NULL
);
```

### ✅ Checklist PHẦN 1
- [ ] Đăng nhập SSMS bằng **SQL Server Authentication**, user `sa`, password `12345` thành công.
- [ ] `Test-NetConnection localhost -Port 1433` → `TcpTestSucceeded : True`.
- [ ] Trong SSMS, mục **Databases** có `HSF302_CH6`.
- [ ] Ghi lại tên instance: mặc định (`MSSQLSERVER`) hay `SQLEXPRESS` (dùng ở Bước 3).

---

# PHẦN 2 — Xây dựng project step-by-step

## Bước 1: Tạo project & `pom.xml`

**Cách 1 — Spring Initializr (https://start.spring.io):**
- Project: **Maven** · Language: **Java** · Spring Boot: **3.3.x** · Java: **17**
- Group: `com.hsf302` · Artifact: `chapter6` · Package name: `com.hsf302.chapter6`
- Dependencies: **Spring Web**, **Thymeleaf**, **Validation**, **Spring Data JPA**, **MS SQL Server Driver**, **Spring Boot DevTools**

**Cách 2 — `pom.xml` thủ công:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

    <groupId>com.hsf302</groupId>
    <artifactId>chapter6</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>student-management</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Web MVC + Tomcat nhúng -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- Bean Validation (Hibernate Validator) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MỚI: Spring Data JPA (Hibernate 6 + HikariCP) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- MỚI: JDBC driver SQL Server (version do Spring Boot quản lý) -->
        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>mssql-jdbc</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- DevTools: tự restart khi sửa code -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Giải thích các dependency mới:**

| Dependency | Mang theo gì |
|---|---|
| `spring-boot-starter-data-jpa` | Hibernate 6 (JPA provider), Spring Data JPA (`JpaRepository`), HikariCP (connection pool), quản lý transaction |
| `mssql-jdbc` (`runtime`) | Driver JDBC của Microsoft; `runtime` vì code không import trực tiếp class của driver |

> Bản gốc có Lombok (tùy chọn). Bản này **viết getter/setter thủ công** để tránh lỗi cấu hình annotation processor. Nếu dùng Lombok cho entity, **không dùng `@Data`** (sinh `equals/hashCode/toString` trên mọi field → lỗi với quan hệ JPA); dùng `@Getter @Setter @NoArgsConstructor`.

### ✅ Checklist Bước 1
- [ ] Project mở được trong IntelliJ, Maven reload không báo đỏ.
- [ ] Tab **Maven → Dependencies** có `spring-boot-starter-data-jpa` và `mssql-jdbc`.
- [ ] `Project Structure → SDK` = **Java 17** (hoặc cao hơn).
- [ ] Chưa chạy ứng dụng ở bước này (chưa có cấu hình datasource → sẽ lỗi, là bình thường).

---

## Bước 2: Cấu trúc thư mục

```
src/
├── main/
│   ├── java/com/hsf302/chapter6/
│   │   ├── StudentManagementApplication.java
│   │   ├── config/
│   │   │   └── DataInitializer.java          ← MỚI: seed dữ liệu mẫu
│   │   ├── entity/
│   │   │   └── Student.java                  ← ĐỔI: model → entity (@Entity)
│   │   ├── repository/
│   │   │   └── StudentRepository.java        ← MỚI
│   │   ├── service/
│   │   │   ├── StudentService.java           ← ĐỔI: thành interface
│   │   │   └── impl/
│   │   │       └── StudentServiceImpl.java   ← MỚI
│   │   └── controller/
│   │       ├── HomeController.java
│   │       └── StudentController.java
│   └── resources/
│       ├── templates/
│       │   ├── fragments/
│       │   │   └── layout.html
│       │   └── students/
│       │       ├── list.html
│       │       ├── form.html
│       │       └── detail.html
│       ├── static/
│       │   └── css/
│       │       └── style.css
│       └── application.properties
```

> ⚠️ Mọi package (`entity`, `repository`, `service`, `controller`, `config`) phải là **package con** của package chứa class `@SpringBootApplication` (`com.hsf302.chapter6`). Nằm ngoài → Spring không quét thấy → lỗi `No qualifying bean` hoặc 404.

### ✅ Checklist Bước 2
- [ ] Đã tạo đủ các package và thư mục như trên (file có thể chưa có nội dung).
- [ ] `templates/` và `static/` nằm trong `src/main/resources/` (không phải `src/main/java/`).

---

## Bước 3: `application.properties`

```properties
# ===== Application =====
spring.application.name=student-management
server.port=8080

# ===== DataSource: SQL Server =====
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=HSF302_CH6;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=12345

# ===== JPA / Hibernate =====
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
# Map mọi String -> NVARCHAR để lưu được tiếng Việt
spring.jpa.properties.hibernate.use_nationalized_character_data=true
# Tắt Open Session In View: mọi truy cập DB phải xong trong Service
spring.jpa.open-in-view=false

# ===== Thymeleaf =====
spring.thymeleaf.cache=false

# ===== Logging =====
logging.level.com.hsf302=DEBUG
```

**Giải thích:**

| Thuộc tính | Ý nghĩa / lưu ý |
|---|---|
| `jdbc:sqlserver://localhost:1433;databaseName=...` | Cú pháp URL của SQL Server dùng **dấu `;`** ngăn cách tham số (khác MySQL dùng `?` và `&`) |
| `encrypt=true;trustServerCertificate=true` | Driver `mssql-jdbc` 10+ **bật mã hoá mặc định**; SQL Server local dùng chứng chỉ tự ký → phải tin tưởng chứng chỉ, nếu không sẽ lỗi `PKIX path building failed` |
| `ddl-auto=update` | Tạo bảng nếu chưa có, **giữ dữ liệu** giữa các lần chạy → phù hợp CRUD web. Xem bảng so sánh bên dưới |
| `show-sql` + `format_sql` | In câu SQL Hibernate sinh ra → đối chiếu với thao tác trên giao diện |
| `use_nationalized_character_data=true` | Mặc định Hibernate map `String` → `VARCHAR` → tiếng Việt bị lưu thành `?`. Thuộc tính này map sang **`NVARCHAR`** |
| `open-in-view=false` | Spring Boot mặc định bật OSIV và in cảnh báo. Tắt đi để buộc truy cập DB trong tầng Service (thói quen tốt khi có quan hệ lazy ở các chương sau) |
| Không cần `hibernate.dialect` | Hibernate 6 tự nhận diện `SQLServerDialect` từ kết nối |

**`ddl-auto` — chọn giá trị nào?**

| Giá trị | Hành vi | Dùng khi |
|---|---|---|
| `create` | DROP + CREATE bảng mỗi lần chạy → **mất dữ liệu** | Bài tập cần dữ liệu giống nhau mỗi lần chạy (Chapter 4) |
| `create-drop` | Như `create`, DROP thêm khi tắt app | Test |
| **`update`** | Tạo bảng/cột còn thiếu, **không xoá**, **không sửa** kiểu cột đã có | **Bài này** — CRUD web cần dữ liệu tồn tại |
| `validate` | Chỉ kiểm tra entity khớp bảng, sai → không khởi động | Khi tự tạo bảng bằng script |
| `none` | Không làm gì | Production |

**Dùng instance `SQLEXPRESS`?** Thay URL bằng một trong hai cách:
```properties
# Cách 1: theo tên instance (cần dịch vụ SQL Server Browser đang chạy)
spring.datasource.url=jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=HSF302_CH6;encrypt=true;trustServerCertificate=true
# Cách 2: đặt cổng tĩnh 1433 cho SQLEXPRESS (PHẦN 1.1) rồi giữ nguyên URL localhost:1433
```

> 💡 Không muốn đưa mật khẩu lên Git: tạo `application-local.properties` (thêm vào `.gitignore`) chứa `spring.datasource.password=...`, và thêm `spring.profiles.active=local` vào `application.properties`.

### ✅ Checklist Bước 3
- [ ] `databaseName` đúng `HSF302_CH6` (đã tạo ở PHẦN 1.4).
- [ ] username/password khớp với tài khoản đăng nhập SSMS thành công.
- [ ] Có `trustServerCertificate=true`.
- [ ] Có `use_nationalized_character_data=true`.
- [ ] File được lưu với encoding **UTF-8**.

---

## Bước 4: Main Application — `StudentManagementApplication.java`

```java
package com.hsf302.chapter6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudentManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
    }
}
```

**🔎 Checkpoint kết nối DB:** chạy ứng dụng ngay lúc này (chưa có entity/controller).

### ✅ Checklist Bước 4
- [ ] Console có dòng `HikariPool-1 - Start completed.` → **kết nối SQL Server thành công**.
- [ ] Console có dòng `Tomcat started on port 8080`.
- [ ] Không có `Login failed for user 'sa'`, `TCP/IP connection ... has failed`, `Cannot open database` (nếu có → xem [PHẦN 4](#phần-4--lỗi-thường-gặp)).
- [ ] Mở `http://localhost:8080` → trang **Whitelabel Error Page 404** (bình thường, chưa có controller).

---

## Bước 5: Entity — `Student.java`

```java
package com.hsf302.chapter6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 50, message = "Tên phải từ 2 đến 50 ký tự")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @NotNull(message = "Tuổi không được để trống")
    @Min(value = 18, message = "Tuổi tối thiểu là 18")
    @Max(value = 30, message = "Tuổi tối đa là 30")
    @Column(name = "age", nullable = false)
    private Integer age;

    @NotBlank(message = "Chuyên ngành không được để trống")
    @Column(name = "major", nullable = false, length = 20)
    private String major;

    @NotNull(message = "GPA không được để trống")
    @DecimalMin(value = "0.0", message = "GPA tối thiểu là 0.0")
    @DecimalMax(value = "4.0", message = "GPA tối đa là 4.0")
    @Column(name = "gpa", nullable = false)
    private Double gpa;

    // ========== Constructors ==========

    /** JPA bắt buộc có constructor không tham số */
    public Student() {}

    /** Dùng cho seed data — không có id vì DB tự sinh */
    public Student(String name, String email, Integer age, String major, Double gpa) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.major = major;
        this.gpa = gpa;
    }

    // ========== Getters & Setters ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
```

**Giải thích:**

| Annotation | Tầng | Tác dụng |
|---|---|---|
| `@Entity`, `@Table(name="students")` | JPA | Class ↔ bảng `students` |
| `@Id` + `@GeneratedValue(IDENTITY)` | JPA | Cột `id BIGINT IDENTITY(1,1)` — SQL Server tự tăng. Với IDENTITY, Hibernate phải `INSERT` **ngay khi `save()`** để lấy id |
| `@Column(nullable=false, length=50)` | JPA | Ràng buộc ở **DB** (`NOT NULL`, `NVARCHAR(50)`) — chỉ có tác dụng khi Hibernate tạo bảng |
| `@Column(unique=true)` | JPA | Tạo `UNIQUE` constraint cho email |
| `@NotBlank`, `@Size`, `@Min`... | Bean Validation | Ràng buộc ở **form** (Controller, `@Valid`) — trước khi chạm DB |

> 💡 **Hai lớp bảo vệ:** Bean Validation chặn dữ liệu sai từ form và báo lỗi thân thiện; ràng buộc DB (`NOT NULL`, `UNIQUE`) là lớp cuối cùng nếu dữ liệu vào bằng đường khác. Nên khai báo **cả hai**, và cho độ dài khớp nhau (`@Size(max=50)` ↔ `length=50`).

> ⚠️ **Vì sao `Integer`/`Double` thay vì `int`/`double`?** Khi người dùng để trống ô tuổi, chuỗi `""` không chuyển được sang `int` → lỗi `typeMismatch` với thông báo tiếng Anh dài. Với `Integer`, `""` → `null` → `@NotNull` báo "Tuổi không được để trống".

> ⚠️ Bind form trực tiếp vào entity là chấp nhận được cho bài lab 1 bảng. Ở dự án thật (và ở Chapter 6 BTVN), nên dùng **DTO** (`StudentForm`) để tránh người dùng gửi thêm field không mong muốn (*mass assignment*).

### ✅ Checklist Bước 5
- [ ] Import đúng `jakarta.persistence.*` và `jakarta.validation.constraints.*` (**không** phải `javax.*`).
- [ ] Có constructor **không tham số**.
- [ ] Chạy lại app → console có câu lệnh `create table students (...)` với các cột `nvarchar`.
- [ ] SSMS: `HSF302_CH6 → Tables → dbo.students` tồn tại (F5 để refresh).
- [ ] Chạy kiểm tra cấu trúc bảng:
  ```sql
  USE HSF302_CH6;
  SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'students';
  ```
  → `name`, `email`, `major` có kiểu **`nvarchar`**; cả 6 cột đều có `IS_NULLABLE = NO`.

> Nếu bảng `students` đã tồn tại từ lần chạy trước với kiểu `varchar` → `ddl-auto=update` **không sửa kiểu cột**. Xoá bảng (`DROP TABLE students;`) rồi chạy lại.

---

## Bước 6: Repository — `StudentRepository.java`

```java
package com.hsf302.chapter6.repository;

import com.hsf302.chapter6.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /** Email đã tồn tại? (dùng khi thêm mới) */
    boolean existsByEmailIgnoreCase(String email);

    /** Email đã được sinh viên KHÁC dùng? (dùng khi cập nhật) */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
```

**Giải thích:**
- `JpaRepository<Student, Long>` cung cấp sẵn: `findAll`, `findAll(Sort)`, `findById`, `save`, `deleteById`, `existsById`, `count`...
- Hai method `existsBy...` là **derived query**: Spring Data đọc tên method và sinh JPQL, ví dụ:
  `existsByEmailIgnoreCaseAndIdNot` → `select count(*) > 0 from Student s where upper(s.email) = upper(?1) and s.id <> ?2`.
- Không cần viết class implementation — Spring tạo proxy lúc khởi động. `@Repository` là tuỳ chọn với interface kế thừa `JpaRepository`.

### ✅ Checklist Bước 6
- [ ] Kiểu khoá trong `JpaRepository<Student, Long>` khớp kiểu của `@Id` (`Long`).
- [ ] Tên field trong method khớp tên field Java (`email`, `id`), **không** phải tên cột.
- [ ] Chạy app không lỗi `No property 'xxx' found for type 'Student'` (lỗi này = sai tên trong derived query).

---

## Bước 7: Service — `StudentService` + `StudentServiceImpl`

### 7.1 Interface `StudentService.java`

```java
package com.hsf302.chapter6.service;

import com.hsf302.chapter6.entity.Student;

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

    List<String> getMajors();
}
```

### 7.2 Implementation `StudentServiceImpl.java`

```java
package com.hsf302.chapter6.service.impl;

import com.hsf302.chapter6.entity.Student;
import com.hsf302.chapter6.repository.StudentRepository;
import com.hsf302.chapter6.service.StudentService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)          // mặc định: mọi method chỉ đọc
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    @Transactional                      // ghi dữ liệu → bỏ readOnly
    public Student create(Student student) {
        student.setId(null);            // luôn INSERT, không bao giờ ghi đè bản ghi cũ
        return studentRepository.save(student);
    }

    @Override
    @Transactional
    public boolean update(Long id, Student data) {
        return studentRepository.findById(id)
                .map(existing -> {
                    existing.setName(data.getName());
                    existing.setEmail(data.getEmail());
                    existing.setAge(data.getAge());
                    existing.setMajor(data.getMajor());
                    existing.setGpa(data.getGpa());
                    // Không cần gọi save(): entity đang "managed",
                    // Hibernate tự sinh UPDATE khi transaction commit (dirty checking)
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!studentRepository.existsById(id)) {
            return false;
        }
        studentRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean isEmailTaken(String email, Long excludeId) {
        if (email == null || email.isBlank()) return false;
        return excludeId == null
                ? studentRepository.existsByEmailIgnoreCase(email.trim())
                : studentRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), excludeId);
    }

    @Override
    public List<String> getMajors() {
        return List.of("CNTT", "KTPM", "HTTT", "ATTT", "MMT");
    }
}
```

**Giải thích các điểm quan trọng:**

| Điểm | Giải thích |
|---|---|
| `@Transactional(readOnly = true)` trên class | Các method đọc chạy trong transaction chỉ đọc (Hibernate bỏ qua dirty checking → nhanh hơn) |
| `@Transactional` trên method ghi | Ghi đè cấu hình class → transaction đọc/ghi. Lỗi → **rollback** toàn bộ |
| `create()` gán `id = null` | `save()` của Spring Data: id `null` → `persist` (INSERT); id có giá trị → `merge` (UPDATE nếu tồn tại). Gán null để form Create không bao giờ ghi đè bản ghi khác |
| `update()` load rồi set field | Cách chuẩn: chỉ cập nhật field cho phép; entity managed + transaction commit → `UPDATE students SET ... WHERE id=?` |
| `delete()` kiểm tra `existsById` | `deleteById` của Spring Boot 3 **không ném lỗi** khi id không tồn tại → kiểm tra trước để báo "Không tìm thấy" |
| Controller chỉ biết `StudentService` (interface) | Có thể thay implementation (in-memory ↔ JPA) mà không sửa Controller |

### ✅ Checklist Bước 7
- [ ] `@Service` đặt trên **class implementation** (`StudentServiceImpl`), không đặt trên interface.
- [ ] Import `org.springframework.transaction.annotation.Transactional` (không phải `jakarta.transaction.Transactional` — cái này không có `readOnly`).
- [ ] Các method `create`, `update`, `delete` có `@Transactional` (không readOnly).
- [ ] Chạy app không lỗi `No qualifying bean of type 'StudentService'`.

---

## Bước 8: Dữ liệu mẫu — `DataInitializer.java`

```java
package com.hsf302.chapter6.config;

import com.hsf302.chapter6.entity.Student;
import com.hsf302.chapter6.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final StudentRepository studentRepository;

    public DataInitializer(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() > 0) {
            log.info("Bảng students đã có dữ liệu → bỏ qua seed");
            return;
        }
        studentRepository.saveAll(List.of(
                new Student("Nguyễn Văn An",  "an@fpt.edu.vn",    20, "CNTT", 3.5),
                new Student("Trần Thị Bình",  "binh@fpt.edu.vn",  21, "KTPM", 3.2),
                new Student("Lê Minh Cường",  "cuong@fpt.edu.vn", 19, "ATTT", 3.8),
                new Student("Phạm Thị Dung",  "dung@fpt.edu.vn",  22, "HTTT", 2.9)
        ));
        log.info("Đã seed {} sinh viên", studentRepository.count());
    }
}
```

**Giải thích:**
- `CommandLineRunner.run()` chạy **một lần** sau khi ApplicationContext khởi động xong.
- Kiểm tra `count() > 0` để không chèn trùng (với `ddl-auto=update` dữ liệu được giữ lại giữa các lần chạy; nếu không kiểm tra, lần chạy thứ 2 sẽ lỗi trùng email do `UNIQUE`).
- Thay cho việc seed trong constructor của Service như bản gốc — constructor không nên truy cập DB.

> Nếu tự chèn dữ liệu bằng SQL, chuỗi tiếng Việt phải có tiền tố **`N`**: `INSERT INTO students(name, ...) VALUES (N'Nguyễn Văn An', ...)`. Thiếu `N` → lưu thành `Nguy?n V?n An`.

### ✅ Checklist Bước 8
- [ ] Chạy app lần 1 → log `Đã seed 4 sinh viên`, console có 4 câu `insert into students ...`.
- [ ] SSMS: `SELECT * FROM students;` → 4 dòng, **tiếng Việt hiển thị đúng dấu**, id = 1..4.
- [ ] Tắt và chạy app lần 2 → log `Bảng students đã có dữ liệu → bỏ qua seed`, vẫn 4 dòng (không nhân đôi).

---

## Bước 9: Controller — `StudentController.java`

```java
package com.hsf302.chapter6.controller;

import com.hsf302.chapter6.entity.Student;
import com.hsf302.chapter6.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    private static final String FORM_VIEW = "students/form";

    private final StudentService studentService;

    // Constructor injection (recommended) — 1 constructor nên không cần @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** Chạy trước MỌI handler trong controller → view nào cũng có ${majors} */
    @ModelAttribute("majors")
    public List<String> majors() {
        return studentService.getMajors();
    }

    // ==================== READ ALL ====================

    @GetMapping
    public String list(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "students/list";
    }

    // ==================== READ ONE ====================

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        return studentService.findById(id)
                .map(student -> {
                    model.addAttribute("student", student);
                    return "students/detail";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMsg", "Không tìm thấy sinh viên ID: " + id);
                    return "redirect:/students";
                });
    }

    // ==================== CREATE ====================

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("student", new Student());
        return formView(model, false);
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("student") Student student,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes ra) {
        // 1. Kiểm tra nghiệp vụ: email trùng (chỉ khi email đã hợp lệ về định dạng)
        if (!bindingResult.hasFieldErrors("email")
                && studentService.isEmailTaken(student.getEmail(), null)) {
            bindingResult.rejectValue("email", "duplicate", "Email đã tồn tại");
        }
        // 2. Có lỗi → quay lại form (KHÔNG redirect để giữ dữ liệu + lỗi)
        if (bindingResult.hasErrors()) {
            return formView(model, false);
        }
        // 3. Lưu DB — vẫn bắt lỗi UNIQUE phòng trường hợp 2 người submit cùng lúc
        try {
            studentService.create(student);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("email", "duplicate", "Email đã tồn tại");
            return formView(model, false);
        }
        ra.addFlashAttribute("successMsg", "Thêm sinh viên thành công!");
        return "redirect:/students";                      // PRG pattern
    }

    // ==================== UPDATE ====================

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        return studentService.findById(id)
                .map(student -> {
                    model.addAttribute("student", student);
                    return formView(model, true);
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMsg", "Không tìm thấy sinh viên ID: " + id);
                    return "redirect:/students";
                });
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("student") Student student,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes ra) {
        student.setId(id);   // form không gửi id → gắn từ URL để khi trả lỗi, form action vẫn đúng

        if (!bindingResult.hasFieldErrors("email")
                && studentService.isEmailTaken(student.getEmail(), id)) {
            bindingResult.rejectValue("email", "duplicate", "Email đã được sinh viên khác sử dụng");
        }
        if (bindingResult.hasErrors()) {
            return formView(model, true);
        }
        try {
            if (studentService.update(id, student)) {
                ra.addFlashAttribute("successMsg", "Cập nhật thành công!");
            } else {
                ra.addFlashAttribute("errorMsg", "Không tìm thấy sinh viên ID: " + id);
            }
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("email", "duplicate", "Email đã được sinh viên khác sử dụng");
            return formView(model, true);
        }
        return "redirect:/students";
    }

    // ==================== DELETE ====================

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        if (studentService.delete(id)) {
            ra.addFlashAttribute("successMsg", "Xóa sinh viên thành công!");
        } else {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sinh viên để xóa!");
        }
        return "redirect:/students";
    }

    // ==================== Helper ====================

    private String formView(Model model, boolean isEdit) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("pageTitle", isEdit ? "Cập nhật sinh viên" : "Thêm sinh viên mới");
        return FORM_VIEW;
    }
}
```

**Thay đổi so với bản Chapter 5 & lý do:**

| Thay đổi | Lý do |
|---|---|
| `@ModelAttribute("majors")` method-level | Bản gốc add `majors` ở 4 chỗ; quên 1 chỗ (vd. nhánh lỗi validate) → dropdown rỗng |
| `formView(model, isEdit)` | Gom `isEdit` + `pageTitle` vào 1 chỗ |
| Kiểm tra trùng email + `rejectValue` | Lỗi nghiệp vụ hiển thị ngay dưới ô email giống lỗi validate |
| `catch (DataIntegrityViolationException)` | Spring dịch lỗi `UNIQUE` của SQL Server thành exception này. Không bắt → trang lỗi 500 |
| `@PathVariable("id")` ghi rõ tên | An toàn kể cả khi project không biên dịch với cờ `-parameters` |
| `student.setId(id)` trong `update` | Form không có ô `id`; nếu trả về form khi lỗi mà `student.id = null` → `th:action` sinh URL sai |

> **Lưu ý về DELETE:** HTML form chỉ hỗ trợ GET/POST → dùng `POST /students/{id}/delete`. **Không** dùng link `GET` để xoá (trình duyệt/bot có thể prefetch link và xoá nhầm).

### ✅ Checklist Bước 9
- [ ] `@Controller` (**không** phải `@RestController`).
- [ ] Trong cả `create` và `update`: thứ tự tham số là `@Valid @ModelAttribute("student") Student student, BindingResult bindingResult` — **liền nhau**.
- [ ] Tên model attribute `"student"` khớp `th:object="${student}"` ở form.
- [ ] Nhánh thành công trả `"redirect:/students"`; nhánh lỗi trả `"students/form"`.
- [ ] Import `org.springframework.dao.DataIntegrityViolationException`.

---

## Bước 10: Home redirect — `HomeController.java`

```java
package com.hsf302.chapter6.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/students";
    }
}
```

### ✅ Checklist Bước 10
- [ ] Truy cập `http://localhost:8080/` → URL trên trình duyệt đổi thành `/students` (chưa có template thì sẽ lỗi template — bình thường ở bước này).

---

## Bước 11: CSS + Layout — `fragments/layout.html`

### 11.1 `static/css/style.css`

```css
body { background-color: #f8f9fa; }
.navbar-brand { font-weight: bold; font-size: 1.3rem; }
.table th { background-color: #343a40; color: #fff; }
.gpa-high { color: #28a745; font-weight: bold; }
.gpa-mid  { color: #fd7e14; }
.gpa-low  { color: #dc3545; }
```

### 11.2 `templates/fragments/layout.html` — layout có tham số

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org"
      th:fragment="layout(title, content)">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <!-- Tiêu đề do trang con truyền vào -->
    <title th:replace="${title}">Student Manager</title>

    <!-- Bootstrap 5 + Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
    <!-- CSS riêng (static/css/style.css) -->
    <link th:href="@{/css/style.css}" rel="stylesheet"/>
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container">
        <a class="navbar-brand" th:href="@{/}">
            <i class="bi bi-mortarboard-fill me-2"></i>Student Manager
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link text-white" th:href="@{/students}">
                <i class="bi bi-people me-1"></i>Sinh viên
            </a>
            <a class="nav-link text-white" th:href="@{/students/create}">
                <i class="bi bi-plus-circle me-1"></i>Thêm mới
            </a>
        </div>
    </div>
</nav>

<main class="container">

    <!-- Flash messages: dùng chung cho mọi trang -->
    <div th:if="${successMsg}" class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle me-2"></i><span th:text="${successMsg}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <div th:if="${errorMsg}" class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle me-2"></i><span th:text="${errorMsg}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <!-- Nội dung do trang con truyền vào -->
    <div th:replace="${content}">Nội dung trang</div>
</main>

<footer class="text-center text-muted py-4 mt-5 border-top">
    <small>HSF302 &copy; 2026 — Spring MVC + Thymeleaf + SQL Server</small>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

**Cơ chế layout có tham số (sửa lỗi bản gốc):**

```
students/list.html                              fragments/layout.html
<html th:replace="~{fragments/layout ::         <html th:fragment="layout(title, content)">
        layout(~{::title}, ~{::section})}">       <title th:replace="${title}">  ← <title> của list
  <title>Danh sách sinh viên</title>              ...
  <section> ... nội dung ... </section>          <div th:replace="${content}"> ← <section> của list
```

- `~{::title}` / `~{::section}`: chọn thẻ `<title>` / `<section>` **trong chính trang con**, truyền sang layout.
- Toàn bộ `<html>` của trang con bị **thay** bằng layout đã chèn nội dung.
- Mỗi trang con chỉ được có **một** thẻ `<section>` ở cấp nội dung chính (selector `::section` lấy **tất cả** thẻ `section`).

> Bản gốc dùng `th:replace="~{fragments/layout :: layout}"` nhưng `layout.html` không khai báo fragment `layout`, và `~{::content}` bên trong layout lại tìm trong chính file layout → lỗi `TemplateInputException`/trang trống. Cách trên dùng Thymeleaf thuần, **không cần** thư viện Layout Dialect.

### ✅ Checklist Bước 11
- [ ] `style.css` nằm ở `src/main/resources/static/css/style.css`.
- [ ] Mở trực tiếp `http://localhost:8080/css/style.css` → thấy nội dung CSS.
- [ ] `layout.html` có `th:fragment="layout(title, content)"` trên thẻ `<html>`.

---

## Bước 12: Template — `students/list.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{fragments/layout :: layout(~{::title}, ~{::section})}">
<head>
    <title>Danh sách sinh viên</title>
</head>
<body>
<section>

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-people-fill me-2"></i>Danh sách sinh viên</h2>
        <a th:href="@{/students/create}" class="btn btn-primary">
            <i class="bi bi-plus-lg me-1"></i>Thêm sinh viên
        </a>
    </div>

    <div class="card shadow-sm">
        <div class="card-body p-0">

            <!-- Trường hợp rỗng -->
            <div th:if="${#lists.isEmpty(students)}" class="text-center p-5 text-muted">
                <i class="bi bi-inbox display-4"></i>
                <p class="mt-2">Chưa có sinh viên nào.</p>
                <a th:href="@{/students/create}" class="btn btn-outline-primary">Thêm ngay</a>
            </div>

            <!-- Bảng dữ liệu -->
            <table th:unless="${#lists.isEmpty(students)}" class="table table-hover mb-0">
                <thead>
                <tr>
                    <th>#</th>
                    <th>ID</th>
                    <th>Họ tên</th>
                    <th>Email</th>
                    <th>Tuổi</th>
                    <th>Chuyên ngành</th>
                    <th>GPA</th>
                    <th class="text-center">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <tr th:each="student, stat : ${students}">
                    <td th:text="${stat.count}">1</td>
                    <td th:text="${student.id}">1</td>
                    <td>
                        <a th:href="@{/students/{id}(id=${student.id})}"
                           th:text="${student.name}"
                           class="text-decoration-none fw-semibold">Tên</a>
                    </td>
                    <td th:text="${student.email}">email</td>
                    <td th:text="${student.age}">20</td>
                    <td><span class="badge bg-secondary" th:text="${student.major}">CNTT</span></td>
                    <td>
                        <span th:classappend="${student.gpa >= 3.5} ? 'gpa-high' :
                                              (${student.gpa >= 2.5} ? 'gpa-mid' : 'gpa-low')"
                              th:text="${#numbers.formatDecimal(student.gpa, 1, 1)}">3.5</span>
                    </td>
                    <td class="text-center">
                        <a th:href="@{/students/{id}(id=${student.id})}"
                           class="btn btn-sm btn-outline-info me-1" title="Chi tiết">
                            <i class="bi bi-eye"></i>
                        </a>
                        <a th:href="@{/students/{id}/edit(id=${student.id})}"
                           class="btn btn-sm btn-outline-warning me-1" title="Sửa">
                            <i class="bi bi-pencil"></i>
                        </a>
                        <form th:action="@{/students/{id}/delete(id=${student.id})}"
                              method="post" class="d-inline"
                              th:data-name="${student.name}"
                              onsubmit="return confirm('Xóa sinh viên ' + this.dataset.name + '?')">
                            <button type="submit" class="btn btn-sm btn-outline-danger" title="Xóa">
                                <i class="bi bi-trash"></i>
                            </button>
                        </form>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div th:unless="${#lists.isEmpty(students)}" class="text-muted mt-2">
        <small>Tổng: <strong th:text="${#lists.size(students)}">0</strong> sinh viên</small>
    </div>

</section>
</body>
</html>
```

**Ghi chú:**
- Thêm cột **ID** để dễ đối chiếu với `SELECT * FROM students` (cột `#` là số thứ tự hiển thị, **không phải** id — sau khi xoá, id có "lỗ hổng", ví dụ 1, 2, 4).
- Flash message đã chuyển sang layout → không lặp lại ở đây.
- `th:data-name` sinh `data-name="Nguyễn Văn An"` (được escape an toàn), JavaScript đọc bằng `this.dataset.name`.

### ✅ Checklist Bước 12
- [ ] `http://localhost:8080/students` hiển thị 4 sinh viên seed, có navbar + footer (layout hoạt động).
- [ ] Cột ID khớp với `SELECT id, name FROM students ORDER BY id;`.
- [ ] Tiếng Việt hiển thị đúng dấu.
- [ ] GPA có màu: ≥ 3.5 xanh, ≥ 2.5 cam, < 2.5 đỏ.
- [ ] Bấm nút xoá → hộp thoại hiện **đúng tên** sinh viên → bấm **Cancel** (chưa xoá).
- [ ] View Source (Ctrl+U): **không còn** thuộc tính `th:*` nào trong HTML.

---

## Bước 13: Template — `students/form.html`

Form dùng chung cho Create và Edit (phân biệt qua `isEdit`).

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{fragments/layout :: layout(~{::title}, ~{::section})}">
<head>
    <title th:text="${pageTitle}">Form sinh viên</title>
</head>
<body>
<section>

    <div class="row justify-content-center">
        <div class="col-md-7">

            <nav aria-label="breadcrumb" class="mb-3">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a th:href="@{/students}">Sinh viên</a></li>
                    <li class="breadcrumb-item active" th:text="${pageTitle}">Form</li>
                </ol>
            </nav>

            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0">
                        <i class="bi me-2" th:classappend="${isEdit} ? 'bi-person-check' : 'bi-person-plus'"></i>
                        <span th:text="${pageTitle}">Form</span>
                    </h5>
                </div>
                <div class="card-body">

                    <form th:action="${isEdit} ? @{/students/{id}/edit(id=${student.id})} : @{/students/create}"
                          th:object="${student}"
                          method="post"
                          novalidate>

                        <!-- Lỗi tổng hợp: #fields chỉ dùng được BÊN TRONG phần tử có th:object -->
                        <div th:if="${#fields.hasAnyErrors()}" class="alert alert-warning py-2">
                            Vui lòng kiểm tra lại các trường được đánh dấu đỏ.
                        </div>

                        <!-- Họ tên -->
                        <div class="mb-3">
                            <label for="name" class="form-label fw-semibold">
                                Họ tên <span class="text-danger">*</span>
                            </label>
                            <input type="text" class="form-control"
                                   th:field="*{name}"
                                   th:errorclass="is-invalid"
                                   placeholder="Nhập họ và tên"/>
                            <div class="invalid-feedback" th:errors="*{name}">Lỗi tên</div>
                        </div>

                        <!-- Email -->
                        <div class="mb-3">
                            <label for="email" class="form-label fw-semibold">
                                Email <span class="text-danger">*</span>
                            </label>
                            <input type="email" class="form-control"
                                   th:field="*{email}"
                                   th:errorclass="is-invalid"
                                   placeholder="example@fpt.edu.vn"/>
                            <div class="invalid-feedback" th:errors="*{email}">Lỗi email</div>
                        </div>

                        <!-- Tuổi -->
                        <div class="mb-3">
                            <label for="age" class="form-label fw-semibold">
                                Tuổi <span class="text-danger">*</span>
                            </label>
                            <input type="number" class="form-control"
                                   th:field="*{age}"
                                   th:errorclass="is-invalid"
                                   min="18" max="30"/>
                            <div class="invalid-feedback" th:errors="*{age}">Lỗi tuổi</div>
                        </div>

                        <!-- Chuyên ngành -->
                        <div class="mb-3">
                            <label for="major" class="form-label fw-semibold">
                                Chuyên ngành <span class="text-danger">*</span>
                            </label>
                            <select class="form-select" th:field="*{major}" th:errorclass="is-invalid">
                                <option value="">-- Chọn chuyên ngành --</option>
                                <option th:each="m : ${majors}" th:value="${m}" th:text="${m}"></option>
                            </select>
                            <div class="invalid-feedback" th:errors="*{major}">Lỗi chuyên ngành</div>
                        </div>

                        <!-- GPA -->
                        <div class="mb-4">
                            <label for="gpa" class="form-label fw-semibold">
                                GPA <span class="text-danger">*</span>
                            </label>
                            <input type="number" class="form-control"
                                   th:field="*{gpa}"
                                   th:errorclass="is-invalid"
                                   step="0.1" min="0" max="4"
                                   placeholder="0.0 - 4.0"/>
                            <div class="form-text">Thang điểm 4.0</div>
                            <div class="invalid-feedback" th:errors="*{gpa}">Lỗi GPA</div>
                        </div>

                        <div class="d-flex gap-2">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-save me-1"></i>
                                <span th:text="${isEdit} ? 'Cập nhật' : 'Thêm mới'">Lưu</span>
                            </button>
                            <a th:href="@{/students}" class="btn btn-outline-secondary">
                                <i class="bi bi-x-lg me-1"></i>Hủy
                            </a>
                        </div>
                    </form>
                </div>
            </div>

        </div>
    </div>

</section>
</body>
</html>
```

**Ghi chú:**
- `th:errorclass="is-invalid"`: tự thêm class khi field có lỗi — gọn hơn `th:classappend="${#fields.hasErrors('x')} ? 'is-invalid'"` của bản gốc (cả hai đều đúng).
- `th:errors` chỉ render khi field có lỗi → không cần `th:if`.
- `novalidate`: tắt validate của trình duyệt để **thấy được validate phía server** khi thực hành. Bỏ `novalidate` khi triển khai thật (validate 2 phía).
- `th:field="*{age}"` sinh cả `id="age"` và `name="age"` → `<label for="age">` hoạt động.
- Thêm dấu `*` cho GPA vì entity đã bắt buộc `@NotNull`.

### ✅ Checklist Bước 13
- [ ] `/students/create` hiển thị form trống, dropdown có 5 chuyên ngành.
- [ ] Bấm **Thêm mới** khi để trống tất cả → mỗi ô viền đỏ + thông báo **tiếng Việt** dưới ô (kể cả Tuổi, GPA).
- [ ] Nhập tên `A`, email `abc`, tuổi `17`, chọn chuyên ngành, GPA `5` → hiển thị đúng 4 lỗi (tên, email, tuổi, GPA); dữ liệu đã nhập **vẫn còn**.
- [ ] Để trống tên → hiện **2** thông báo ("không được để trống" + "từ 2 đến 50 ký tự") vì chuỗi rỗng vi phạm cả `@NotBlank` và `@Size` — đây là hành vi đúng.
- [ ] Nhập email `an@fpt.edu.vn` (đã tồn tại) → lỗi **"Email đã tồn tại"** dưới ô email; DB **không** có thêm dòng.
- [ ] Nhập hợp lệ → quay về danh sách + thông báo xanh "Thêm sinh viên thành công!"; bản ghi mới xuất hiện cuối bảng.
- [ ] SSMS: `SELECT * FROM students ORDER BY id DESC;` → thấy bản ghi vừa thêm, tiếng Việt đúng.
- [ ] Console có câu `insert into students (age,email,gpa,major,name) values (?,?,?,?,?)`.
- [ ] Nhấn **F5** ở trang danh sách sau khi thêm → **không** phát sinh bản ghi trùng (PRG).
- [ ] `/students/1/edit` → form điền sẵn dữ liệu, tiêu đề "Cập nhật sinh viên", nút "Cập nhật".
- [ ] Sửa tên → Cập nhật → console có `update students set ... where id=?`; SSMS thấy giá trị mới.
- [ ] Sửa sinh viên 1 **giữ nguyên email của chính mình** → cập nhật **thành công** (không báo trùng).
- [ ] Sửa sinh viên 1 sang email `binh@fpt.edu.vn` → lỗi "Email đã được sinh viên khác sử dụng".
- [ ] Ở form Edit, để trống tên → bấm Cập nhật → quay lại form, URL action vẫn là `/students/1/edit` (xem bằng F12).

---

## Bước 14: Template — `students/detail.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{fragments/layout :: layout(~{::title}, ~{::section})}">
<head>
    <title>Chi tiết sinh viên</title>
</head>
<body>
<section>

    <div class="row justify-content-center">
        <div class="col-md-7">

            <nav aria-label="breadcrumb" class="mb-3">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a th:href="@{/students}">Sinh viên</a></li>
                    <li class="breadcrumb-item active" th:text="${student.name}">Tên</li>
                </ol>
            </nav>

            <div class="card shadow-sm">
                <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">
                        <i class="bi bi-person-circle me-2"></i>
                        <span th:text="${student.name}">Tên sinh viên</span>
                    </h5>
                    <span class="badge bg-light text-dark" th:text="'ID: ' + ${student.id}">ID</span>
                </div>
                <div class="card-body">
                    <dl class="row mb-0">
                        <dt class="col-sm-4">Họ tên</dt>
                        <dd class="col-sm-8" th:text="${student.name}">Tên</dd>

                        <dt class="col-sm-4">Email</dt>
                        <dd class="col-sm-8">
                            <a th:href="'mailto:' + ${student.email}" th:text="${student.email}">email</a>
                        </dd>

                        <dt class="col-sm-4">Tuổi</dt>
                        <dd class="col-sm-8" th:text="${student.age} + ' tuổi'">20 tuổi</dd>

                        <dt class="col-sm-4">Chuyên ngành</dt>
                        <dd class="col-sm-8">
                            <span class="badge bg-primary" th:text="${student.major}">CNTT</span>
                        </dd>

                        <dt class="col-sm-4">GPA</dt>
                        <dd class="col-sm-8">
                            <span th:classappend="${student.gpa >= 3.5} ? 'text-success fw-bold' :
                                                  (${student.gpa >= 2.5} ? 'text-warning fw-bold' : 'text-danger fw-bold')"
                                  th:text="${#numbers.formatDecimal(student.gpa, 1, 1)}">3.5</span>
                            <small class="text-muted ms-1">/ 4.0</small>
                        </dd>
                    </dl>
                </div>
                <div class="card-footer d-flex gap-2">
                    <a th:href="@{/students/{id}/edit(id=${student.id})}" class="btn btn-warning">
                        <i class="bi bi-pencil me-1"></i>Sửa
                    </a>
                    <!-- Sửa lỗi bản gốc: dùng data-name thay cho inline /*[[...]]*/ trong thuộc tính -->
                    <form th:action="@{/students/{id}/delete(id=${student.id})}"
                          method="post"
                          th:data-name="${student.name}"
                          onsubmit="return confirm('Xóa sinh viên ' + this.dataset.name + '?')">
                        <button type="submit" class="btn btn-danger">
                            <i class="bi bi-trash me-1"></i>Xóa
                        </button>
                    </form>
                    <a th:href="@{/students}" class="btn btn-outline-secondary ms-auto">
                        <i class="bi bi-arrow-left me-1"></i>Quay lại
                    </a>
                </div>
            </div>

        </div>
    </div>

</section>
</body>
</html>
```

### ✅ Checklist Bước 14
- [ ] Bấm tên sinh viên ở danh sách → trang chi tiết đúng dữ liệu trong DB.
- [ ] `/students/999` (id không tồn tại) → quay về danh sách + thông báo đỏ "Không tìm thấy sinh viên ID: 999".
- [ ] Bấm **Xóa** → hộp thoại có **tên sinh viên** → OK → quay về danh sách + "Xóa sinh viên thành công!".
- [ ] Console có `delete from students where id=?`; SSMS không còn dòng đó.
- [ ] Nhấn nút Back của trình duyệt về trang chi tiết đã xoá rồi bấm Xóa lần nữa → "Không tìm thấy sinh viên để xóa!" (không lỗi 500).

---

## Bước 15: Kiểm thử tổng thể

### 15.1 Chạy ứng dụng
```bash
mvn spring-boot:run
# hoặc trong IDE: Run StudentManagementApplication
```

### 15.2 URL kiểm tra

| URL | Method | Chức năng | SQL kỳ vọng trên console |
|---|---|---|---|
| `/` | GET | Redirect → `/students` | — |
| `/students` | GET | Danh sách | `select ... from students order by id` |
| `/students/create` | GET | Form thêm mới | — |
| `/students/create` | POST | Thêm mới | `select count(*)...` (check email) → `insert into students ...` |
| `/students/1` | GET | Chi tiết ID=1 | `select ... where id=?` |
| `/students/1/edit` | GET | Form sửa ID=1 | `select ... where id=?` |
| `/students/1/edit` | POST | Cập nhật ID=1 | `select ...` → `update students set ... where id=?` |
| `/students/1/delete` | POST | Xoá ID=1 | `select count(*)...` → `select ...` → `delete from students where id=?` |

### 15.3 Kịch bản kiểm thử (đối chiếu giao diện ↔ database)

| # | Kịch bản | Kết quả trên giao diện | Kiểm tra trong SSMS |
|---|---|---|---|
| 1 | Mở `/students` lần đầu | 4 sinh viên seed | `SELECT COUNT(*) FROM students` = 4 |
| 2 | Thêm `Võ Thị Én`, `en@fpt.edu.vn`, 20, MMT, 3.0 | Thông báo xanh, xuất hiện cuối bảng | Có dòng mới, `name` = `Võ Thị Én` (đúng dấu) |
| 3 | Submit form trống | Cả 5 ô báo lỗi tiếng Việt, không chuyển trang | Số dòng không đổi |
| 4 | Thêm với email `AN@fpt.edu.vn` (khác hoa thường) | "Email đã tồn tại" | Số dòng không đổi |
| 5 | F5 sau khi thêm thành công | Không có bản ghi trùng | Số dòng không đổi |
| 6 | Sửa SV id=2, GPA → 3.9 | "Cập nhật thành công!", GPA màu xanh | `SELECT gpa FROM students WHERE id=2` = 3.9 |
| 7 | Sửa SV id=2 sang email của id=3 | Lỗi dưới ô email | Email id=2 không đổi |
| 8 | Xoá SV id=4 | "Xóa sinh viên thành công!" | Không còn id=4 |
| 9 | Mở `/students/4` sau khi xoá | Đỏ: "Không tìm thấy..." | — |
| 10 | **Tắt ứng dụng, chạy lại** | Dữ liệu sau các thao tác trên **vẫn còn** | Log "bỏ qua seed" |
| 11 | Thêm SV mới sau khi đã xoá id=4 | Id mới là 6 (không dùng lại 4) | `IDENTITY` không tái sử dụng id |
| 12 | Sửa trực tiếp trong SSMS: `UPDATE students SET name = N'Test SSMS' WHERE id = 1` → F5 trang | Danh sách hiện "Test SSMS" | Chứng minh view đọc từ DB, không phải bộ nhớ |

### ✅ Checklist Bước 15 (tổng)
- [ ] 12/12 kịch bản đạt.
- [ ] Không có trang **Whitelabel Error Page** trong toàn bộ thao tác bình thường.
- [ ] Console không có `WARN ... spring.jpa.open-in-view is enabled by default` (đã tắt).
- [ ] Tiếng Việt đúng ở cả giao diện **và** SSMS.

---

# PHẦN 3 — Luồng xử lý CRUD (MVC + JPA + SQL)

### Create

```
GET  /students/create
  → showCreateForm() → model: {student: new Student(), majors, isEdit=false, pageTitle}
  → render students/form.html

POST /students/create
  → DataBinder: name, email, age, major, gpa → Student (id = null)
  → @Valid → BindingResult
  → isEmailTaken(email, null) → SELECT COUNT(*) ... WHERE UPPER(email)=UPPER(?)
  → Có lỗi?   → return "students/form" (giữ dữ liệu, hiện lỗi)
  → Không lỗi → StudentServiceImpl.create()  [BEGIN TRANSACTION]
                → repository.save() → INSERT INTO students ... (IDENTITY sinh id)
                [COMMIT]
              → flash "Thêm thành công" → redirect:/students (302)
GET  /students → SELECT ... ORDER BY id → list.html (hiện flash)
```

### Read

```
GET /students       → findAll(Sort id ASC) → SELECT * FROM students ORDER BY id
GET /students/{id}  → findById(id)         → SELECT ... WHERE id=?
                      → có: detail.html | không: redirect + flash lỗi
```

### Update

```
GET  /students/{id}/edit → findById → form.html (isEdit=true, dữ liệu điền sẵn)

POST /students/{id}/edit
  → bind form → Student tạm (không phải entity managed), setId(id)
  → @Valid + isEmailTaken(email, id) → ... WHERE email=? AND id<>?
  → Lỗi → form.html
  → OK  → update(id, data) [BEGIN]
            findById(id)            → SELECT ... WHERE id=?   (entity managed)
            existing.setXxx(...)    → chưa có SQL
          [COMMIT] → dirty checking → UPDATE students SET ... WHERE id=?
        → redirect:/students + flash
```

### Delete

```
POST /students/{id}/delete (confirm JS)
  → delete(id) [BEGIN]
       existsById → SELECT COUNT(*) ... WHERE id=?
       deleteById → SELECT ... WHERE id=?  →  DELETE FROM students WHERE id=?
     [COMMIT]
  → redirect:/students + flash
```

> `deleteById` của Spring Data **load entity trước rồi mới xoá** (để chạy các callback/cascade của JPA) → thấy 1 câu `SELECT` trước `DELETE` là bình thường.

---

# PHẦN 4 — Lỗi thường gặp

### 4.1 Lỗi kết nối SQL Server

| Thông báo lỗi (console) | Nguyên nhân | Cách sửa |
|---|---|---|
| `The TCP/IP connection to the host localhost, port 1433 has failed` | TCP/IP chưa bật, cổng khác 1433, service chưa chạy | PHẦN 1.1 + restart service; kiểm tra `Test-NetConnection` |
| `Login failed for user 'sa'` | Chưa bật Mixed Mode, `sa` bị disable, sai mật khẩu | PHẦN 1.2 |
| `Cannot open database "HSF302_CH6" requested by the login` | Chưa tạo database / sai tên | PHẦN 1.4 |
| `PKIX path building failed` / `encrypt` / SSL | Thiếu `trustServerCertificate=true` | Thêm vào URL |
| `Failed to configure a DataSource: 'url' attribute is not specified` | Sai/thiếu `spring.datasource.url`, file properties không nằm trong `resources` | Kiểm tra tên thuộc tính, vị trí file |
| `Cannot load driver class: com.microsoft.sqlserver.jdbc.SQLServerDriver` | Thiếu dependency `mssql-jdbc` / chưa reload Maven | Thêm dependency, Maven → Reload |
| Kết nối được SSMS nhưng Java không vào được | SSMS dùng Shared Memory/Named Pipes; Java dùng TCP | Bật TCP/IP |
| Dùng `SQLEXPRESS` và lỗi kết nối | Instance có cổng động | Dùng `instanceName=SQLEXPRESS` + bật **SQL Server Browser**, hoặc đặt cổng tĩnh 1433 |

### 4.2 Lỗi JPA / dữ liệu

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| Tiếng Việt thành `?` trong DB | Cột `VARCHAR` | `use_nationalized_character_data=true`; `DROP TABLE students` để Hibernate tạo lại; chèn tay dùng `N'...'` |
| Đổi `length`/kiểu trong entity nhưng bảng không đổi | `ddl-auto=update` không sửa cột đã có | Drop bảng, hoặc dùng `create` 1 lần rồi đổi lại `update` |
| Dữ liệu seed bị nhân đôi / lỗi trùng email khi khởi động | Seed không kiểm tra `count()` | Thêm `if (count() > 0) return;` |
| Mất hết dữ liệu mỗi lần chạy | `ddl-auto=create` | Đổi sang `update` |
| Trang 500 `DataIntegrityViolationException ... UNIQUE KEY` | Không kiểm tra/bắt lỗi trùng email | Bước 9: `isEmailTaken` + `catch` |
| `No property 'mail' found for type 'Student'` | Sai tên field trong derived query | Đặt đúng tên field Java |
| `No qualifying bean of type 'StudentService'` | Thiếu `@Service` trên impl / package ngoài vùng quét | Bước 2, Bước 7 |
| Thêm mới lại **ghi đè** một sinh viên cũ | Form gửi kèm `id` → `save()` thực hiện merge | `student.setId(null)` trong `create` |
| `LazyInitializationException` (khi có quan hệ ở chương sau) | `open-in-view=false` và view truy cập quan hệ lazy | Fetch đủ dữ liệu trong Service (JOIN FETCH / DTO) |

### 4.3 Lỗi MVC / Thymeleaf

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| `TemplateInputException: Error resolving template [fragments/layout]` | Sai đường dẫn fragment hoặc layout thiếu `th:fragment` | Bước 11 |
| Trang chỉ có nội dung, không có navbar | Template con không dùng `th:replace` layout | Kiểm tra thẻ `<html>` của trang con |
| `Neither BindingResult nor plain target object for bean name 'student'` | GET form chưa `addAttribute("student", ...)` / tên không khớp | Bước 9 |
| Ô tuổi trống → lỗi `Failed to convert property value of type String to int` | Dùng `int` | Dùng `Integer` + `@NotNull` |
| Dropdown chuyên ngành rỗng khi validate lỗi | Không add lại `majors` | `@ModelAttribute("majors")` method-level |
| Trang 400 khi submit form có lỗi | `BindingResult` không đứng ngay sau `@Valid` | Đặt liền nhau |
| `/students/abc` → trang 400 | `abc` không chuyển được sang `Long` | Bình thường; có thể giới hạn `@GetMapping("/{id:\\d+}")` để trả 404 |
| Mất CSS | Sai vị trí `static/css`, dùng `href` không có `@{}` | Bước 11 |

---

# PHẦN 5 — Điểm cần nhớ & bài tập mở rộng

### Spring MVC
- Controller **không biết** dữ liệu đến từ ArrayList hay SQL Server → đổi tầng lưu trữ không phải sửa Controller/View.
- `@Valid` + `BindingResult` liền nhau; lỗi → trả view form; thành công → `redirect:` (PRG).
- Lỗi nghiệp vụ (trùng email) → `bindingResult.rejectValue(field, code, message)` để hiển thị như lỗi validate.
- `@ModelAttribute` method-level cho dữ liệu dùng chung (dropdown).

### Spring Data JPA + SQL Server
- Database phải tạo trước; Hibernate chỉ tạo bảng.
- URL SQL Server: `jdbc:sqlserver://host:1433;databaseName=...;encrypt=true;trustServerCertificate=true`.
- Tiếng Việt: `NVARCHAR` (`use_nationalized_character_data=true`) và `N'...'` khi viết SQL tay.
- `IDENTITY` → id do DB sinh, không tái sử dụng sau khi xoá.
- `@Transactional` ở **Service**: đọc dùng `readOnly = true`, ghi dùng mặc định.
- Update chuẩn: **load → set field → commit** (dirty checking).
- Ràng buộc 2 lớp: Bean Validation (form) + constraint DB (`NOT NULL`, `UNIQUE`).

### Thymeleaf
- Layout có tham số: `th:fragment="layout(title, content)"` + `th:replace="~{fragments/layout :: layout(~{::title}, ~{::section})}"`.
- `th:errorclass="is-invalid"` + `th:errors="*{field}"` để hiện lỗi theo Bootstrap.
- Không đặt biểu thức inline `[[...]]` trong thuộc tính sự kiện (`onsubmit`, `onclick`) — dùng `th:data-*`.

### Bài tập mở rộng (tự làm)
1. **Tìm kiếm:** thêm ô tìm theo tên/email trên `list.html` — `GET /students?keyword=an`, repository `findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String, String, Sort)`.
2. **Phân trang:** đổi `findAll()` sang `Page<Student> findAll(Pageable)`, `GET /students?page=0&size=5`, hiển thị thanh phân trang với `th:each="i : ${#numbers.sequence(0, page.totalPages - 1)}"`.
3. **Sắp xếp:** click tiêu đề cột → `?sort=gpa,desc`.
4. **Tách DTO:** tạo `StudentForm` cho form, map sang entity trong Service.
5. **Bảng `majors`:** chuyển chuyên ngành thành entity `Major` (`@ManyToOne` từ `Student`) — chuẩn bị cho bài Department–Student.
6. **Trang lỗi:** tạo `templates/error/404.html` và `templates/error/500.html` dùng chung layout.
