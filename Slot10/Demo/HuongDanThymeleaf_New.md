# Hướng Dẫn Thực Hành Thymeleaf trong Spring Boot

> **Yêu cầu chung:** Java 17+, Spring Boot 3.3.x, Maven, SQL Server 2019+.
>
> **Base package:** `com.example.demo` · **Database (Bài 4):** `ThymeleafDemo` · **Giao diện:** Bootstrap 5.3.3 + Bootstrap Icons 1.11.3 (dùng được **online** hoặc **offline**).

---

## Cách dùng tài liệu

Mỗi bài được chia thành các **TODO**. Mỗi TODO gồm:

1. **Yêu cầu** — cần làm gì, kết quả mong đợi.
2. **Các bước** — hướng dẫn step-by-step kèm code.
3. **✅ Checklist** — chỉ chuyển sang TODO tiếp theo khi đã tick hết.

Lỗi Thymeleaf **không** xuất hiện khi biên dịch, chỉ xuất hiện **khi trang được render** và **ở đúng nhánh `th:if` được chạy** 

## Mục lục

- [Phần 0 — Chuẩn bị project & Bootstrap online/offline](#phần-0--chuẩn-bị-project--bootstrap-onlineoffline)
- [Bài 1 — Hiển thị dữ liệu: `th:text`, `th:each`, `th:if`](#bài-1-cơ-bản--hiển-thị-dữ-liệu-thtext-theach-thif)
- [Bài 2 — Form: `th:object`, `th:field`, PRG](#bài-2-cơ-bản--form-thobject-thfield-prg)
- [Bài 3 — Layout với Fragments](#bài-3-cơ-bản--layout-với-fragments)
- [Bài 4 — CRUD với Spring Data JPA + SQL Server](#bài-4-nâng-cao--crud-với-spring-data-jpa--sql-server)
- [Bài 5 — Validation, thông báo lỗi & i18n](#bài-5-nâng-cao--validation-thông-báo-lỗi--i18n)
- [Tổng kết — Bảng Thymeleaf Expressions & lỗi chung](#tổng-kết)

---
# Phần 0 — Chuẩn bị project & Bootstrap online/offline

## TODO 0.1 — Tạo project Spring Boot

**Yêu cầu:** project Maven chạy được, mở `http://localhost:8080` thấy trang 404 Whitelabel (chưa có controller).

**Các bước:**

1. Vào https://start.spring.io (hoặc IntelliJ → New Project → Spring Boot):
   - Maven · Java · Spring Boot **4.1.x** · Java **21**
   - Group `com.example` · Artifact `demo` · Package `com.example.demo`
   - Dependencies: **Spring Web**, **Thymeleaf**, **Validation**, **Spring Boot DevTools**
   - ⚠️ **Chưa** chọn Spring Data JPA và MS SQL Server Driver 
2. `pom.xml` cần có:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>

3. Chạy class `DemoApplication`.

### ✅ Checklist TODO 0.1
- [ ] Maven reload không báo đỏ; SDK = Java 17+.
- [ ] Console có `Tomcat started on port 8080`.
- [ ] `http://localhost:8080` → Whitelabel Error Page 404 (bình thường).

---

## TODO 0.2 — Cấu trúc thư mục & `application.properties`

**Yêu cầu:** tạo sẵn các package/thư mục cho cả 5 bài.

```
src/main/
├── java/com/example/demo/
│   ├── DemoApplication.java
│   ├── config/          ← WebConfig (Bài 5)
│   ├── controller/
│   ├── model/           ← SinhVien, SanPham, KhoaHoc, NguoiDung
│   ├── repository/      ← Bài 4
│   └── service/         ← Bài 4
└── resources/
    ├── templates/
    │   ├── fragments/layout.html     ← Bài 3
    │   ├── sinhvien/  sanpham/  khoahoc/  dangky/
    │   └── trang-chu.html
    ├── static/
    │   ├── css/style.css
    │   └── vendor/                   ← Bootstrap offline (TODO 0.3)
    ├── messages.properties           ← Bài 5
    ├── messages_en.properties        ← Bài 5
    └── application.properties
```

`application.properties`:

```properties
spring.application.name=demo
server.port=8080

# Tắt cache để sửa template không cần restart
spring.thymeleaf.cache=false
```

`static/css/style.css` (CSS riêng, nạp **sau** Bootstrap):

```css
body { background-color: #f8f9fa; }
.table th { white-space: nowrap; }
```

### ✅ Checklist TODO 0.2
- [ ] Mọi package nằm **trong** `com.example.demo` (package con của class `@SpringBootApplication`).
- [ ] `templates/` và `static/` nằm trong `src/main/resources/`.
- [ ] IntelliJ `Settings → Editor → File Encodings`: Global, Project, **Properties Files** đều **UTF-8**.

---

## TODO 0.3 — Cài Bootstrap: online (CDN) hoặc offline (file trong project)

**Yêu cầu:** trang dùng được class Bootstrap (`container`, `table`, `btn`...), icon `bi-*`, và component cần JS (nút đóng alert, menu thu gọn). **Đi thi không có Internet → bắt buộc biết cách offline.**

### Cách A — Online (CDN)

Chỉ cần mạng Internet, không cần file:

```html
<!-- trong <head> -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"/>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"/>

<!-- cuối <body> -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
```

### Cách B — Offline (khuyến nghị khi đi thi)

**Bước 1 — Lấy file** (làm ở nhà, lúc có mạng), chọn 1 trong 3:

| Nguồn | Lấy những gì |
|---|---|
| File `bootstrap-offline.zip` có sẵn — chỉ cần giải nén |
| https://getbootstrap.com → *Download* → "Compiled CSS and JS" | `css/bootstrap.min.css`, `js/bootstrap.bundle.min.js` |
| https://github.com/twbs/icons/releases → `bootstrap-icons-1.11.3.zip` | `font/bootstrap-icons.min.css` **và cả thư mục** `font/fonts/` |

**Bước 2 — Đặt vào project** đúng cấu trúc:

```
src/main/resources/static/
├── css/style.css
└── vendor/
    ├── bootstrap/
    │   ├── css/bootstrap.min.css          (+ .map: tuỳ chọn)
    │   └── js/bootstrap.bundle.min.js     (+ .map: tuỳ chọn)
    └── bootstrap-icons/
        ├── bootstrap-icons.min.css
        └── fonts/
            ├── bootstrap-icons.woff2
            └── bootstrap-icons.woff
```

**Bước 3 — Tham chiếu bằng `@{...}`** (không có chữ `static`):

```html
<!-- trong <head> -->
<link rel="stylesheet" th:href="@{/vendor/bootstrap/css/bootstrap.min.css}"/>
<link rel="stylesheet" th:href="@{/vendor/bootstrap-icons/bootstrap-icons.min.css}"/>
<link rel="stylesheet" th:href="@{/css/style.css}"/>

<!-- cuối <body> -->
<script th:src="@{/vendor/bootstrap/js/bootstrap.bundle.min.js}"></script>
```

**Bước 4 — Trang kiểm tra nhanh** `static/bootstrap-test.html` (file HTML tĩnh, không qua Thymeleaf):

```html
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Test Bootstrap offline</title>
    <link rel="stylesheet" href="/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/vendor/bootstrap-icons/bootstrap-icons.min.css">
</head>
<body class="p-4">
<div class="alert alert-success alert-dismissible fade show">
    <i class="bi bi-check-circle-fill"></i>
    CSS OK nếu khung màu xanh; Icons OK nếu thấy dấu tích bên trái.
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
<p>JS OK nếu bấm ✕ thì khung biến mất.</p>
<script src="/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

### So sánh

| | Online (CDN) | Offline (static) |
|---|---|---|
| Cần Internet | Có | **Không** |
| Thay đổi khi chép project sang máy khác | Không | File đi theo project |
| Đường dẫn | `href="https://..."` | `th:href="@{/vendor/...}"` |
| Rủi ro | Mất mạng → trang "trơn", không lỗi Java | Đặt sai thư mục → 404 file CSS/font |


### ✅ Checklist TODO 0.3
- [ ] Mở `http://localhost:8080/vendor/bootstrap/css/bootstrap.min.css` → thấy nội dung CSS.
- [ ] Mở `http://localhost:8080/bootstrap-test.html` → khung xanh, có icon ✔, bấm ✕ thì khung biến mất.
- [ ] **Tắt Wi-Fi** rồi F5 → vẫn như trên.
- [ ] F12 → Network: không có dòng đỏ 404 (đặc biệt file `.woff2`); không có request tới `cdn.jsdelivr.net`.
- [ ] (Nếu không chép file `.map`) chỉ có cảnh báo *"DevTools failed to load source map"* — vô hại, bỏ qua.

### ⚠️ Lỗi thường gặp với Bootstrap

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| Trang không có style, F12 → 404 `bootstrap.min.css` | Sai thư mục, hoặc viết `@{/static/vendor/...}` | File phải ở `static/vendor/...`, URL **không** có `/static` |
| Icon hiện ô vuông / trống | Thiếu thư mục `fonts/` cạnh `bootstrap-icons.min.css` (CSS tìm font theo đường dẫn tương đối `fonts/...`) | Chép đủ thư mục `fonts/` |
| Bấm ✕ alert / nút menu không có tác dụng | Thiếu JS, hoặc dùng `bootstrap.min.js` (thiếu Popper) | Dùng `bootstrap.bundle.min.js`, đặt cuối `<body>` |
| Đã chép file nhưng vẫn 404 | IntelliJ chưa copy sang `target/classes` | **Build → Build Project (Ctrl+F9)** hoặc restart app |
| CSS riêng không có tác dụng | `style.css` nạp **trước** Bootstrap → bị Bootstrap ghi đè | Nạp `style.css` **sau cùng** |
| Đổi online ↔ offline phải sửa nhiều file | Mỗi trang tự khai báo `<link>` | Gom vào fragment `commonHead` (Bài 3) → chỉ sửa 1 chỗ |
| Dùng `href="/vendor/..."` (không `@{}`), deploy với context path `/demo` → mất CSS | Đường dẫn tuyệt đối không có context path | Dùng `th:href="@{/vendor/...}"` trong template Thymeleaf |

---

# Bài 1 (Cơ bản) — Hiển thị dữ liệu: `th:text`, `th:each`, `th:if`

**Mục tiêu:** render danh sách từ Model; dùng `th:text`, `th:each` (biến trạng thái), `th:if`/`th:unless`, utility `#lists`, `#numbers`.

| TODO | Yêu cầu | Kết quả |
|---|---|---|
| 1.1 | Model `SinhVien` (maSV, hoTen, diemTB) | Class có getter |
| 1.2 | Controller `GET /sinhvien` đưa `sinhViens`, `tieuDe` vào Model | Trả view `sinhvien/danh-sach` |
| 1.3 | Template bảng Bootstrap, cột STT, xếp loại theo điểm, thông báo khi rỗng | Trang hiển thị đúng |
| 1.4 | Kiểm thử cả trường hợp có dữ liệu **và** rỗng | Cả 2 nhánh render không lỗi |

## TODO 1.1 — Model `SinhVien`

`model/SinhVien.java`:

```java
package com.example.demo.model;

public class SinhVien {
    private String maSV;
    private String hoTen;
    private double diemTB;

    public SinhVien() {}

    public SinhVien(String maSV, String hoTen, double diemTB) {
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.diemTB = diemTB;
    }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public double getDiemTB() { return diemTB; }
    public void setDiemTB(double diemTB) { this.diemTB = diemTB; }
}
```

> ⚠️ Thymeleaf đọc dữ liệu qua **getter**: `${sv.maSV}` → gọi `getMaSV()`. Thiếu getter → lỗi khi render, **không** lỗi khi biên dịch. Getter của `maSV` phải là `getMaSV()` (IDE generate đúng); tự gõ `getMasv()` → `${sv.maSV}` sẽ lỗi.

### ✅ Checklist TODO 1.1
- [ ] Đủ getter cho 3 field (dùng IDE: *Generate → Getter and Setter*).

## TODO 1.2 — Controller

`controller/SinhVienController.java`:

```java
package com.example.demo.controller;

import com.example.demo.model.SinhVien;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class SinhVienController {

    @GetMapping("/sinhvien")
    public String danhSach(Model model) {
        List<SinhVien> danhSach = List.of(
                new SinhVien("SV001", "Nguyễn Văn An", 8.5),
                new SinhVien("SV002", "Trần Thị Bình", 6.2),
                new SinhVien("SV003", "Lê Hoàng Cường", 7.0)
        );
        model.addAttribute("sinhViens", danhSach);
        model.addAttribute("tieuDe", "Danh sách sinh viên");
        return "sinhvien/danh-sach";      // → templates/sinhvien/danh-sach.html
    }
}
```

### ✅ Checklist TODO 1.2
- [ ] `@Controller` (**không** phải `@RestController` — cái này trả về chuỗi `"sinhvien/danh-sach"` ra màn hình).
- [ ] Tên view **không** có `.html`, **không** có `/` ở đầu.
- [ ] Tên attribute (`sinhViens`, `tieuDe`) ghi lại để dùng **đúng y hệt** trong template.

## TODO 1.3 — Template `sinhvien/danh-sach.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title th:text="${tieuDe}">Danh sách</title>
    <!-- Bootstrap OFFLINE (TODO 0.3). Dùng online thì thay bằng link CDN. -->
    <link rel="stylesheet" th:href="@{/vendor/bootstrap/css/bootstrap.min.css}"/>
    <link rel="stylesheet" th:href="@{/css/style.css}"/>
</head>
<body>
<main class="container py-4">

    <h1 class="h3 mb-3" th:text="${tieuDe}">Tiêu đề</h1>

    <!-- Có dữ liệu → hiện bảng -->
    <table class="table table-bordered table-hover bg-white"
           th:unless="${#lists.isEmpty(sinhViens)}">
        <thead class="table-dark">
        <tr>
            <th>#</th>
            <th>Mã SV</th>
            <th>Họ tên</th>
            <th class="text-end">Điểm TB</th>
            <th>Xếp loại</th>
        </tr>
        </thead>
        <tbody>
        <!-- sv: phần tử hiện tại; stat: biến trạng thái (count bắt đầu từ 1) -->
        <tr th:each="sv, stat : ${sinhViens}">
            <td th:text="${stat.count}">1</td>
            <td th:text="${sv.maSV}">SV001</td>
            <td th:text="${sv.hoTen}">Nguyễn Văn A</td>
            <td class="text-end" th:text="${#numbers.formatDecimal(sv.diemTB, 1, 1)}">8.5</td>
            <td>
                <span th:if="${sv.diemTB >= 8.0}" class="badge bg-success">Giỏi</span>
                <span th:if="${sv.diemTB >= 6.5 and sv.diemTB < 8.0}" class="badge bg-primary">Khá</span>
                <span th:if="${sv.diemTB < 6.5}" class="badge bg-danger">Trung bình</span>
            </td>
        </tr>
        </tbody>
    </table>

    <!-- Rỗng → hiện thông báo -->
    <div th:if="${#lists.isEmpty(sinhViens)}" class="alert alert-info">
        Không có sinh viên nào.
    </div>

    <p class="text-muted" th:unless="${#lists.isEmpty(sinhViens)}">
        Tổng: <strong th:text="${#lists.size(sinhViens)}">0</strong> sinh viên
    </p>
</main>
</body>
</html>
```

**Giải thích:**

| Cú pháp | Ý nghĩa |
|---|---|
| `th:text="${tieuDe}"` | Thay nội dung thẻ bằng giá trị (tự **escape** HTML) |
| Chữ mẫu trong thẻ (`Tiêu đề`, `SV001`) | Chỉ để mở file HTML trực tiếp vẫn xem được bố cục (*natural template*); bị thay khi render |
| `th:each="sv, stat : ${...}"` | Lặp; `stat.index` từ **0**, `stat.count` từ **1**, có `stat.size`, `stat.first`, `stat.last`, `stat.odd` |
| `th:if` / `th:unless` | Có / không render thẻ. Cặp `th:if="X"` & `th:unless="X"` là hai nhánh đối nhau |
| `and`, `or`, `not` | Toán tử logic (không cần escape như `&&`) |
| `#lists.isEmpty`, `#lists.size` | Utility object cho List |
| `#numbers.formatDecimal(x, 1, 1)` | Ít nhất 1 chữ số phần nguyên, 1 chữ số thập phân |

> 💡 Dấu thập phân theo **locale** của request: trình duyệt tiếng Việt có thể hiển thị `8,5`. Muốn luôn là dấu chấm: `${#numbers.formatDecimal(sv.diemTB, 1, 'DEFAULT', 1, 'POINT')}`.
>
> 💡 Cách gọn hơn cho xếp loại: viết method `getXepLoai()` trong `SinhVien` rồi dùng `${sv.xepLoai}` — logic nằm ở Java, template chỉ hiển thị.

### ✅ Checklist TODO 1.3
- [ ] File đúng đường dẫn `templates/sinhvien/danh-sach.html` (đúng **hoa/thường**).
- [ ] Thẻ `<html>` có `xmlns:th="http://www.thymeleaf.org"`.
- [ ] `<meta charset="UTF-8"/>` có trong `<head>`.

## TODO 1.4 — Chạy & kiểm thử 2 nhánh

1. Mở `http://localhost:8080/sinhvien`.
2. Sửa tạm controller thành `List.of()` → F5 (DevTools tự restart).
3. Trả lại dữ liệu cũ.

### ✅ Checklist TODO 1.4
- [ ] Có dữ liệu: bảng 3 dòng, STT 1–3, tiếng Việt đúng dấu, badge Giỏi (xanh lá) / Khá (xanh dương) / Trung bình (đỏ).
- [ ] Rỗng: **không** có bảng, có khung "Không có sinh viên nào.", không lỗi.
- [ ] Ctrl+U (View Source): **không còn** thuộc tính `th:` nào.
- [ ] Tab trình duyệt hiển thị tiêu đề "Danh sách sinh viên".

## ⚠️ Lỗi thường gặp — Bài 1

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| `Error resolving template [sinhvien/danh-sach]` | Sai tên/đường dẫn file, sai hoa thường, file nằm ngoài `templates/` | Đối chiếu tên view ↔ đường dẫn file |
| `Property or field 'hoten' cannot be found on object of type 'SinhVien'` | Sai tên thuộc tính (`hoten` ≠ `hoTen`) hoặc thiếu getter | Tên = tên getter bỏ `get`, viết thường chữ đầu |
| Màn hình chỉ hiện chữ `sinhvien/danh-sach` | Dùng `@RestController` | Đổi sang `@Controller` |
| Trang hiện nguyên chữ mẫu "Tiêu đề", "SV001" | Mở file HTML trực tiếp bằng trình duyệt, hoặc thiếu `xmlns:th`/dependency thymeleaf | Truy cập qua `http://localhost:8080/...` |
| Tiếng Việt lỗi font (`Nguyá»…n`) | File không lưu UTF-8 / thiếu `<meta charset>` | TODO 0.2, TODO 1.3 |
| Nhánh rỗng chưa từng chạy → lỗi chỉ lộ ra khi dữ liệu rỗng | Thymeleaf chỉ đánh giá biểu thức ở nhánh được render | Luôn test **cả 2 nhánh** (TODO 1.4) |

---

# Bài 2 (Cơ bản) — Form: `th:object`, `th:field`, PRG

**Mục tiêu:** form nhập sản phẩm, binding vào object, xử lý POST theo mẫu **Post → Redirect → Get** (PRG) và flash message.

| TODO | Yêu cầu | Kết quả |
|---|---|---|
| 2.1 | DTO `SanPham` (ten, gia, soLuong) dùng kiểu wrapper | Ô trống → `null`, không lỗi 400 |
| 2.2 | Controller `GET /sanpham/them`, `POST /sanpham/them`, `GET /sanpham/ket-qua` | POST xong redirect |
| 2.3 | Template form Bootstrap | Binding bằng `th:object` + `th:field` |
| 2.4 | Template kết quả: flash message + danh sách | F5 không thêm trùng |

## TODO 2.1 — DTO `SanPham`

```java
package com.example.demo.model;

public class SanPham {
    private String ten;
    private Double gia;          // wrapper: ô trống → null (int/double sẽ lỗi 400)
    private Integer soLuong;

    public SanPham() {}          // BẮT BUỘC: Spring tạo object rỗng rồi set từng field

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public Double getGia() { return gia; }
    public void setGia(Double gia) { this.gia = gia; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
}
```

### ✅ Checklist TODO 2.1
- [ ] Có constructor **không tham số**.
- [ ] Có **setter** cho mọi field (binding dùng setter; thiếu setter → field luôn `null`).

## TODO 2.2 — Controller 

```java
package com.example.demo.controller;

import com.example.demo.model.SanPham;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@RequestMapping("/sanpham")
public class SanPhamController {

    // Lưu tạm trong bộ nhớ: dùng chung cho mọi người dùng, mất khi restart.
    // CopyOnWriteArrayList an toàn khi nhiều request cùng thêm.
    private final List<SanPham> danhSach = new CopyOnWriteArrayList<>();

    // GET – hiển thị form rỗng
    @GetMapping("/them")
    public String showForm(Model model) {
        model.addAttribute("sanPham", new SanPham());     // tên "sanPham" = th:object
        return "sanpham/form";
    }

    // POST – nhận dữ liệu, lưu, rồi REDIRECT
    @PostMapping("/them")
    public String xuLyForm(@ModelAttribute("sanPham") SanPham sanPham, RedirectAttributes ra) {
        danhSach.add(sanPham);
        ra.addFlashAttribute("thongBao", "Thêm sản phẩm thành công!");   // sống qua 1 lần redirect
        return "redirect:/sanpham/ket-qua";
    }

    // GET – trang kết quả
    @GetMapping("/ket-qua")
    public String ketQua(Model model) {
        model.addAttribute("danhSach", danhSach);
        return "sanpham/ket-qua";
    }
}
```


```
POST /sanpham/them ──► add ──► 302 Location: /sanpham/ket-qua ──► GET /sanpham/ket-qua (F5 an toàn)
```

### ✅ Checklist TODO 2.2
- [ ] Tên attribute ở GET (`"sanPham"`) khớp tên trong `@ModelAttribute("sanPham")` và `th:object="${sanPham}"`.
- [ ] POST trả `"redirect:/sanpham/ket-qua"` (URL), **không** phải `"sanpham/ket-qua"` (view).
- [ ] Dùng `addFlashAttribute` (không phải `addAttribute`) cho thông báo.

## TODO 2.3 — Template `sanpham/form.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title>Thêm sản phẩm</title>
    <link rel="stylesheet" th:href="@{/vendor/bootstrap/css/bootstrap.min.css}"/>
</head>
<body>
<main class="container py-4" style="max-width: 600px">
    <h2 class="h4 mb-3">Thêm sản phẩm mới</h2>

    <!--
        th:action → URL đích (tự thêm context path)
        th:object → object trong Model; bên trong dùng *{...}
        th:field  → sinh id, name, value cho input
    -->
    <form th:action="@{/sanpham/them}" th:object="${sanPham}" method="post"
          class="card card-body shadow-sm">

        <div class="mb-3">
            <label for="ten" class="form-label">Tên sản phẩm</label>
            <input type="text" class="form-control" th:field="*{ten}"
                   placeholder="Nhập tên..." required/>
        </div>

        <div class="mb-3">
            <label for="gia" class="form-label">Giá (VNĐ)</label>
            <input type="number" class="form-control" th:field="*{gia}" min="0" step="1000" required/>
        </div>

        <div class="mb-3">
            <label for="soLuong" class="form-label">Số lượng</label>
            <input type="number" class="form-control" th:field="*{soLuong}" min="0" required/>
        </div>

        <div class="d-flex gap-2">
            <button type="submit" class="btn btn-primary">Thêm</button>
            <a th:href="@{/sanpham/ket-qua}" class="btn btn-outline-secondary">Xem danh sách</a>
        </div>
    </form>
</main>
</body>
</html>
```

> `th:field="*{ten}"` sinh ra `id="ten" name="ten" value="..."` → `<label for="ten">` hoạt động. **Không** tự viết thêm `name=`/`value=`.

### ✅ Checklist TODO 2.3
- [ ] `/sanpham/them` hiển thị form Bootstrap.
- [ ] F12 → Elements: mỗi input có `id` và `name` đúng tên field.
- [ ] Mọi `th:field` nằm **bên trong** `<form th:object=...>`.

## TODO 2.4 — Template `sanpham/ket-qua.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title>Kết quả</title>
    <link rel="stylesheet" th:href="@{/vendor/bootstrap/css/bootstrap.min.css}"/>
</head>
<body>
<main class="container py-4">

    <!-- Flash message: chỉ có ngay sau redirect -->
    <div th:if="${thongBao}" class="alert alert-success" th:text="${thongBao}">Thông báo</div>

    <h3 class="h5">Danh sách hiện tại</h3>

    <ul class="list-group mb-3" th:unless="${#lists.isEmpty(danhSach)}">
        <li class="list-group-item" th:each="sp : ${danhSach}">
            <!-- |...| literal substitution: ghép chuỗi gọn hơn dùng + -->
            <span th:text="|${sp.ten} - ${#numbers.formatDecimal(sp.gia, 1, 'POINT', 0, 'COMMA')} VNĐ (SL: ${sp.soLuong})|">
                Tên - Giá (SL)
            </span>
        </li>
    </ul>
    <p th:if="${#lists.isEmpty(danhSach)}" class="text-muted">Chưa có sản phẩm.</p>

    <a th:href="@{/sanpham/them}" class="btn btn-primary">Thêm tiếp</a>
</main>
</body>
</html>
```

> `formatDecimal(sp.gia, 1, 'POINT', 0, 'COMMA')` → `1.500.000` (dấu chấm ngăn cách hàng nghìn, 0 chữ số thập phân).

### ✅ Checklist TODO 2.4
- [ ] Thêm "Bút bi", 5000, 10 → về `/sanpham/ket-qua`, có khung xanh "Thêm sản phẩm thành công!", dòng `Bút bi - 5.000 VNĐ (SL: 10)`.
- [ ] URL trên thanh địa chỉ là `/sanpham/ket-qua` (đã redirect).
- [ ] Nhấn **F5** → khung thông báo **biến mất**, danh sách **không** bị thêm trùng.
- [ ] Restart app → `/sanpham/ket-qua` hiện "Chưa có sản phẩm." (dữ liệu trong bộ nhớ).
- [ ] Mở `/sanpham/ket-qua` trực tiếp lúc chưa thêm gì → không lỗi.

## ⚠️ Lỗi thường gặp — Bài 2

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| `Neither BindingResult nor plain target object for bean name 'sanPham' available` | GET không `addAttribute("sanPham", ...)`, hoặc tên khác `th:object` | Tên phải khớp tuyệt đối |
| Trang 400 khi để trống ô số | Field kiểu `int`/`double` | Dùng `Integer`/`Double` |
| Submit xong mọi field là `null` | Thiếu setter / `name` input sai (tự viết `name=` khác tên field) | Dùng `th:field`, đủ setter |
| F5 thêm trùng sản phẩm | Không redirect sau POST | PRG (TODO 2.2) |
| Thông báo không hiện sau redirect | Dùng `model.addAttribute` hoặc `ra.addAttribute` | `ra.addFlashAttribute` |
| `Cannot process attribute th:field: no associated BindingStatus` | `th:field` đặt ngoài `th:object` | Đặt trong form có `th:object` |
| 405 Method Not Allowed | Form `method="post"` nhưng Controller chỉ có `@GetMapping` (hoặc ngược lại) | Đối chiếu method + URL |

---

# Bài 3 (Cơ bản) — Layout với Fragments

**Mục tiêu:** gom `<head>` (Bootstrap), navbar, thông báo, footer, script vào **một file fragment**; mọi trang dùng lại. Đổi Bootstrap online ↔ offline chỉ sửa **1 chỗ**.

| TODO | Yêu cầu | Kết quả |
|---|---|---|
| 3.1 | `fragments/layout.html` với 5 fragment: `commonHead(pageTitle)`, `navbar(active)`, `alerts`, `footer`, `scripts` | File fragment |
| 3.2 | Trang chủ `trang-chu.html` dùng các fragment | Có navbar + footer |
| 3.3 | `TrangChuController` `GET /` | Trang chủ hiển thị |
| 3.4 | Refactor trang Bài 1, Bài 2 sang dùng fragment | Mọi trang cùng giao diện |
| 3.5 | (Tuỳ chọn) Chuyển online/offline bằng cấu hình | Không sửa template |

## TODO 3.1 — `templates/fragments/layout.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <!-- ===== Fragment 1: head chung (có tham số pageTitle) ===== -->
    <th:block th:fragment="commonHead(pageTitle)">
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <title th:text="${pageTitle}">App</title>

        <!-- ---- Bootstrap OFFLINE (mặc định, dùng khi đi thi) ---- -->
        <link rel="stylesheet" th:href="@{/vendor/bootstrap/css/bootstrap.min.css}"/>
        <link rel="stylesheet" th:href="@{/vendor/bootstrap-icons/bootstrap-icons.min.css}"/>

        <!-- ---- Bootstrap ONLINE: bỏ comment 2 dòng dưới, comment 2 dòng trên ----
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"/>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"/>
        -->

        <link rel="stylesheet" th:href="@{/css/style.css}"/>
    </th:block>
</head>
<body>

<!-- ===== Fragment 2: navbar (tham số active để tô sáng menu) ===== -->
<nav th:fragment="navbar(active)" class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container">
        <a class="navbar-brand fw-bold" th:href="@{/}">
            <i class="bi bi-leaf me-1"></i>Thymeleaf Demo
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                data-bs-target="#mainNav" aria-controls="mainNav"
                aria-expanded="false" aria-label="Menu">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${active == 'sinhvien'} ? 'active'"
                       th:href="@{/sinhvien}">Sinh viên</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${active == 'sanpham'} ? 'active'"
                       th:href="@{/sanpham/them}">Sản phẩm</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${active == 'khoahoc'} ? 'active'"
                       th:href="@{/khoahoc}">Khóa học</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${active == 'dangky'} ? 'active'"
                       th:href="@{/dangky}">Đăng ký</a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<!-- ===== Fragment 3: thông báo flash dùng chung ===== -->
<th:block th:fragment="alerts">
    <div th:if="${successMsg}" class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle me-1"></i><span th:text="${successMsg}">OK</span>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
    </div>
    <div th:if="${errorMsg}" class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle me-1"></i><span th:text="${errorMsg}">Lỗi</span>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
    </div>
</th:block>

<!-- ===== Fragment 4: footer ===== -->
<footer th:fragment="footer" class="border-top text-center text-muted py-3 mt-5">
    <small>© 2026 Spring Boot App – Thymeleaf Demo</small>
</footer>

<!-- ===== Fragment 5: script cuối trang ===== -->
<th:block th:fragment="scripts">
    <!-- OFFLINE -->
    <script th:src="@{/vendor/bootstrap/js/bootstrap.bundle.min.js}"></script>
    <!-- ONLINE:
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    -->
</th:block>

</body>
</html>
```

**Giải thích:**

| Điểm | Ý nghĩa |
|---|---|
| `th:fragment="commonHead(pageTitle)"` | Fragment có **tham số** — trang con truyền tiêu đề riêng |
| `<th:block>` | Thẻ "ảo" của Thymeleaf, không sinh ra HTML → gom nhiều thẻ `<meta>`, `<link>` làm 1 fragment |
| `navbar(active)` | Tô sáng menu đang mở. (Thymeleaf 3.1 trong Spring Boot 3 đã **bỏ** `#request`, `#httpServletRequest` → không lấy URL hiện tại trong template được như tutorial cũ, nên truyền tham số) |
| Link trong comment HTML | Thymeleaf **không** xử lý gì trong `<!-- -->` → comment chỉ để đổi online/offline nhanh. Không lồng `<!-- -->` bên trong một comment khác |

### ✅ Checklist TODO 3.1
- [ ] File ở `templates/fragments/layout.html`.
- [ ] 5 fragment có tên: `commonHead`, `navbar`, `alerts`, `footer`, `scripts`.
- [ ] Đúng **một** trong hai khối online/offline đang hoạt động (khối kia nằm trong comment).

## TODO 3.2 — Trang chủ `templates/trang-chu.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <!-- th:replace: thẻ <th:block> này BỊ THAY bằng fragment -->
    <th:block th:replace="~{fragments/layout :: commonHead('Trang chủ')}"></th:block>
</head>
<body>

<!-- th:insert: GIỮ thẻ <div>, chèn fragment VÀO TRONG (minh hoạ khác biệt) -->
<div th:insert="~{fragments/layout :: navbar('home')}"></div>

<main class="container">
    <th:block th:replace="~{fragments/layout :: alerts}"></th:block>

    <div class="p-4 bg-white rounded shadow-sm mb-4">
        <h1 class="h3">Chào mừng đến với Spring Boot + Thymeleaf!</h1>
        <p class="text-muted mb-0">Chọn chức năng bên dưới hoặc trên menu.</p>
    </div>

    <div class="row g-3">
        <div class="col-md-3" th:each="item : ${ {'sinhvien','sanpham/them','khoahoc','dangky'} }">
            <a class="card card-body text-decoration-none h-100"
               th:href="@{'/' + ${item}}" th:text="${item}">link</a>
        </div>
    </div>
</main>

<!-- th:replace: thẻ <div> BỊ THAY hoàn toàn bằng <footer> -->
<div th:replace="~{fragments/layout :: footer}"></div>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
</body>
</html>
```

> `${ {'a','b'} }` là cú pháp **inline list** của SpEL — dùng tạm để sinh nhanh các thẻ link.

**`th:insert` vs `th:replace`** — xem bằng View Source:

```
<div th:insert="~{... :: navbar('home')}"></div>
  → <div><nav class="navbar ...">...</nav></div>      ← giữ <div> bao ngoài

<div th:replace="~{... :: footer}"></div>
  → <footer class="border-top ...">...</footer>        ← <div> biến mất
```

### ✅ Checklist TODO 3.2
- [ ] Gọi fragment có tham số **đúng số lượng** tham số: `navbar('home')`, `commonHead('Trang chủ')`.
- [ ] Tham số chuỗi có **nháy đơn** `'Trang chủ'` (thiếu nháy → bị hiểu là biến → `null`).

## TODO 3.3 — `TrangChuController`

```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrangChuController {
    @GetMapping("/")
    public String trangChu() {
        return "trang-chu";
    }
}
```

### ✅ Checklist TODO 3.3
- [ ] `http://localhost:8080/` → có navbar tối, 4 thẻ link, footer.
- [ ] Thu nhỏ cửa sổ trình duyệt → menu thu thành nút ☰; bấm nút → menu xổ ra (**JS Bootstrap hoạt động**).
- [ ] View Source: navbar nằm **trong** một `<div>` (insert); footer **không** có `<div>` bao ngoài (replace).
- [ ] View Source: chỉ có **một** `<head>`, các `<link>` Bootstrap nằm trong đó.

## TODO 3.4 — Refactor trang Bài 1 & 2 sang dùng fragment

**Yêu cầu:** mọi trang có cùng head/navbar/footer; xoá các `<link>` Bootstrap viết tay.

Mẫu chung cho **mọi** trang (ví dụ `sinhvien/danh-sach.html`):

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <!-- Tiêu đề lấy từ Model: truyền biểu thức ${...} làm tham số -->
    <th:block th:replace="~{fragments/layout :: commonHead(${tieuDe})}"></th:block>
</head>
<body>
<nav th:replace="~{fragments/layout :: navbar('sinhvien')}"></nav>

<main class="container">
    <th:block th:replace="~{fragments/layout :: alerts}"></th:block>

    <!-- ... nội dung cũ của trang (h1, table, ...) giữ nguyên ... -->
</main>

<footer th:replace="~{fragments/layout :: footer}"></footer>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
</body>
</html>
```

Áp dụng tương tự:

| Trang | `commonHead(...)` | `navbar(...)` |
|---|---|---|
| `sinhvien/danh-sach.html` | `${tieuDe}` | `'sinhvien'` |
| `sanpham/form.html` | `'Thêm sản phẩm'` | `'sanpham'` |
| `sanpham/ket-qua.html` | `'Kết quả'` | `'sanpham'` |

> Ở `ket-qua.html`, có thể đổi `thongBao` thành `successMsg` trong Controller để dùng chung fragment `alerts`.

### ✅ Checklist TODO 3.4
- [ ] Ctrl+F trong `templates/` (trừ `layout.html`): không còn `bootstrap.min.css` nào.
- [ ] Các trang `/sinhvien`, `/sanpham/them`, `/sanpham/ket-qua` có navbar, menu tương ứng được tô sáng (`active`).
- [ ] Đổi `layout.html` sang **online** → mọi trang đều dùng CDN (F12 → Network thấy `cdn.jsdelivr.net`). Đổi lại **offline**, tắt Wi-Fi → mọi trang vẫn đẹp.

## TODO 3.5 — (Tuỳ chọn) Chuyển online/offline bằng cấu hình

Không muốn comment/bỏ comment? Đọc thuộc tính cấu hình ngay trong template. Thêm vào `application.properties`:

```properties
# true = offline (static/vendor), false = online (CDN)
app.bootstrap.offline=true
```

Thay phần link trong `commonHead`:

```html
<th:block th:if="${@environment.getProperty('app.bootstrap.offline', 'true') == 'true'}">
    <link rel="stylesheet" th:href="@{/vendor/bootstrap/css/bootstrap.min.css}"/>
    <link rel="stylesheet" th:href="@{/vendor/bootstrap-icons/bootstrap-icons.min.css}"/>
</th:block>
<th:block th:unless="${@environment.getProperty('app.bootstrap.offline', 'true') == 'true'}">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"/>
</th:block>
```

(làm tương tự cho fragment `scripts`). `@environment` là cú pháp truy cập **bean Spring** trong biểu thức Thymeleaf.

### ✅ Checklist TODO 3.5
- [ ] `app.bootstrap.offline=false` → restart → F12 thấy tải từ CDN; `=true` → tải từ `/vendor/...`.

## ⚠️ Lỗi thường gặp — Bài 3

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| `Error resolving template [fragments/layout]` | Sai đường dẫn (`fragment/`, `Fragments/`), file ngoài `templates/` | `~{fragments/layout :: ...}` khớp `templates/fragments/layout.html` |
| Không báo lỗi nhưng chỗ đó trống | Sai **tên** fragment (`navBar` ≠ `navbar`) — tuỳ phiên bản có thể lỗi hoặc rỗng | Đối chiếu tên `th:fragment` |
| `Cannot resolve fragment. Signature "navbar(active)" declares parameters...` | Số tham số khi gọi khác số tham số khai báo | Gọi đủ: `navbar('home')` |
| Tiêu đề trang là chữ `null` hoặc rỗng | Truyền `commonHead(Trang chủ)` thiếu nháy, hoặc `${tieuDe}` không có trong Model | `commonHead('Trang chủ')`; kiểm tra Model |
| Có 2 thẻ `<head>`/`<body>` lồng nhau | Dùng `th:insert` cho `commonHead` đặt trên thẻ `<head>` | Dùng `<th:block th:replace>` **bên trong** `<head>` |
| Thuộc tính `class`, `th:if` trên thẻ có `th:replace` không tác dụng | Thẻ bị thay **trước** khi xử lý các thuộc tính khác | Đặt class/điều kiện trong fragment hoặc bọc thêm thẻ ngoài |
| Menu ☰ không xổ ra, nút ✕ không đóng | Quên `scripts` fragment ở cuối trang | Thêm `<th:block th:replace="~{fragments/layout :: scripts}">` |
| `${#request.requestURI}` lỗi | Thymeleaf 3.1 (Spring Boot 3) đã bỏ `#request` | Truyền tham số `active` như TODO 3.1 |

---

# Bài 4 (Nâng cao) — CRUD với Spring Data JPA + SQL Server

**Mục tiêu:** quản lý **Khóa học** lưu trong SQL Server: danh sách + tìm kiếm, thêm, sửa (dùng chung form), xoá; flash message; giao diện dùng fragment Bài 3.

| TODO | Yêu cầu | Kết quả |
|---|---|---|
| 4.1 | Tạo DB `ThymeleafDemo`, thêm dependency, cấu hình datasource | App kết nối được SQL Server |
| 4.2 | Entity `KhoaHoc` + `KhoaHocRepository` | Bảng `khoa_hoc` tự tạo |
| 4.3 | `KhoaHocService` (constructor injection, `Optional`, transaction) | Tầng nghiệp vụ |
| 4.4 | `KhoaHocController`: list/search, them, sua, luu, xoa (POST) | Đủ 5 chức năng |
| 4.5 | Template `khoahoc/danh-sach.html` | Bảng + tìm kiếm + nút |
| 4.6 | Template `khoahoc/form.html` dùng chung thêm/sửa | Hidden `id` |
| 4.7 | Kiểm thử CRUD, đối chiếu SSMS | Dữ liệu khớp |

**Luồng:**

```
GET  /khoahoc?keyword=..   → danh sách (+ tìm kiếm)
GET  /khoahoc/them         → form rỗng (id = null)
GET  /khoahoc/sua/{id}     → form điền sẵn (id có giá trị, nằm trong input hidden)
POST /khoahoc/luu          → save(): id null → INSERT, id có → UPDATE → redirect
POST /khoahoc/xoa/{id}     → xoá → redirect
```

## TODO 4.1 — Database & cấu hình

1. SSMS (đăng nhập được bằng `sa` — nếu chưa, xem `Chapter6.md` PHẦN 1: bật TCP/IP cổng 1433, Mixed Mode):

```sql
IF DB_ID(N'ThymeleafDemo') IS NULL
    CREATE DATABASE ThymeleafDemo;
GO
```

2. `pom.xml`: **bỏ comment** 2 dependency `spring-boot-starter-data-jpa` và `mssql-jdbc` (TODO 0.1) → Maven Reload.

3. Thêm vào `application.properties`:

```properties
# ===== SQL Server =====
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ThymeleafDemo;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=12345

# ===== JPA / Hibernate =====
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
# String -> NVARCHAR để lưu tiếng Việt
spring.jpa.properties.hibernate.use_nationalized_character_data=true
spring.jpa.open-in-view=false
```

| Thuộc tính | Lưu ý |
|---|---|
| `encrypt=true;trustServerCertificate=true` | Driver mới bật mã hoá mặc định; thiếu → lỗi `PKIX path building failed` |
| `ddl-auto=update` | Tạo bảng nếu chưa có, **giữ** dữ liệu; **không** sửa kiểu cột đã tồn tại |
| Không khai báo `dialect`, `driver-class-name` | Hibernate 6 / Spring Boot tự nhận diện |
| Windows Authentication | `;integratedSecurity=true` cần thêm file `mssql-jdbc_auth-*.dll` đúng phiên bản driver vào `PATH` → phức tạp, nên dùng tài khoản `sa` |

### ✅ Checklist TODO 4.1
- [ ] Console có `HikariPool-1 - Start completed.`
- [ ] Không có `Login failed for user 'sa'`, `TCP/IP connection ... failed`, `Cannot open database`.
- [ ] Các trang Bài 1–3 vẫn chạy.

## TODO 4.2 — Entity & Repository

`model/KhoaHoc.java`:

```java
package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "khoa_hoc")
public class KhoaHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_khoa_hoc", nullable = false, length = 100)
    private String tenKhoaHoc;

    @Column(name = "giang_vien", length = 100)
    private String giangVien;

    @Column(name = "so_tin_chi")
    private Integer soTinChi;              // Integer: ô trống không lỗi 400

    public KhoaHoc() {}                     // JPA bắt buộc

    public KhoaHoc(String tenKhoaHoc, String giangVien, Integer soTinChi) {
        this.tenKhoaHoc = tenKhoaHoc;
        this.giangVien = giangVien;
        this.soTinChi = soTinChi;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenKhoaHoc() { return tenKhoaHoc; }
    public void setTenKhoaHoc(String tenKhoaHoc) { this.tenKhoaHoc = tenKhoaHoc; }

    public String getGiangVien() { return giangVien; }
    public void setGiangVien(String giangVien) { this.giangVien = giangVien; }

    public Integer getSoTinChi() { return soTinChi; }
    public void setSoTinChi(Integer soTinChi) { this.soTinChi = soTinChi; }
}
```

`repository/KhoaHocRepository.java`:

```java
package com.example.demo.repository;

import com.example.demo.model.KhoaHoc;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KhoaHocRepository extends JpaRepository<KhoaHoc, Long> {
    // WHERE upper(ten_khoa_hoc) LIKE upper('%keyword%') ORDER BY ...
    List<KhoaHoc> findByTenKhoaHocContainingIgnoreCase(String keyword, Sort sort);
}
```

Dữ liệu mẫu (SSMS, nhớ tiền tố `N` cho tiếng Việt):

```sql
USE ThymeleafDemo;
INSERT INTO khoa_hoc (ten_khoa_hoc, giang_vien, so_tin_chi) VALUES
 (N'Lập trình Java',       N'Nguyễn Văn A', 3),
 (N'Spring Boot cơ bản',   N'Trần Thị B',   3),
 (N'Cơ sở dữ liệu',        N'Lê Văn C',     4);
```

### ✅ Checklist TODO 4.2
- [ ] Import `jakarta.persistence.*` (**không** `javax.*`).
- [ ] Console có `create table khoa_hoc (... ten_khoa_hoc nvarchar(100) not null ...)`.
- [ ] `SELECT * FROM khoa_hoc;` → 3 dòng, tiếng Việt đúng dấu.
- [ ] App khởi động không lỗi `No property 'tenKhoaHoc' found` (tên trong method repository = tên **field Java**).

## TODO 4.3 — Service

```java
package com.example.demo.service;

import com.example.demo.model.KhoaHoc;
import com.example.demo.repository.KhoaHocRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class KhoaHocService {

    private final KhoaHocRepository repo;

    public KhoaHocService(KhoaHocRepository repo) {      // constructor injection
        this.repo = repo;
    }

    public List<KhoaHoc> search(String keyword) {
        Sort sort = Sort.by("id");
        if (keyword == null || keyword.isBlank()) {
            return repo.findAll(sort);
        }
        return repo.findByTenKhoaHocContainingIgnoreCase(keyword.trim(), sort);
    }

    public Optional<KhoaHoc> findById(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public KhoaHoc save(KhoaHoc kh) {
        return repo.save(kh);           // id null → INSERT, id có → UPDATE
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
```

### ✅ Checklist TODO 4.3
- [ ] `@Transactional` import từ `org.springframework.transaction.annotation` (có `readOnly`).
- [ ] Không còn `orElseThrow()` không tham số (sẽ ra 500 khi id sai).

## TODO 4.4 — Controller

```java
package com.example.demo.controller;

import com.example.demo.model.KhoaHoc;
import com.example.demo.service.KhoaHocService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/khoahoc")
public class KhoaHocController {

    private final KhoaHocService service;

    public KhoaHocController(KhoaHocService service) {
        this.service = service;
    }

    // READ – danh sách + tìm kiếm
    @GetMapping
    public String danhSach(@RequestParam(value = "keyword", required = false) String keyword,
                           Model model) {
        model.addAttribute("danhSach", service.search(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "khoahoc/danh-sach";
    }

    // CREATE – form rỗng
    @GetMapping("/them")
    public String themMoi(Model model) {
        model.addAttribute("khoaHoc", new KhoaHoc());
        model.addAttribute("pageTitle", "Thêm khóa học");
        return "khoahoc/form";
    }

    // UPDATE – form điền sẵn
    @GetMapping("/sua/{id}")
    public String sua(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        return service.findById(id)
                .map(kh -> {
                    model.addAttribute("khoaHoc", kh);
                    model.addAttribute("pageTitle", "Sửa khóa học");
                    return "khoahoc/form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMsg", "Không tìm thấy khóa học ID: " + id);
                    return "redirect:/khoahoc";
                });
    }

    // SAVE – dùng chung cho thêm & sửa
    @PostMapping("/luu")
    public String luu(@ModelAttribute("khoaHoc") KhoaHoc khoaHoc, RedirectAttributes ra) {
        boolean isNew = (khoaHoc.getId() == null);
        service.save(khoaHoc);
        ra.addFlashAttribute("successMsg", isNew ? "Thêm khóa học thành công!" : "Cập nhật thành công!");
        return "redirect:/khoahoc";
    }

    // DELETE – bắt buộc POST
    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Long id, RedirectAttributes ra) {
        if (service.delete(id)) {
            ra.addFlashAttribute("successMsg", "Đã xóa khóa học!");
        } else {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy khóa học để xóa!");
        }
        return "redirect:/khoahoc";
    }
}
```

> `@PathVariable("id")`, `@RequestParam(value = "keyword")` ghi rõ tên → an toàn kể cả khi project không biên dịch với cờ `-parameters`.

### ✅ Checklist TODO 4.4
- [ ] Mọi thao tác ghi (`luu`, `xoa`) là `@PostMapping` và kết thúc bằng `redirect:`.
- [ ] Thông báo dùng tên `successMsg` / `errorMsg` → khớp fragment `alerts` (Bài 3).

## TODO 4.5 — Template `khoahoc/danh-sach.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <th:block th:replace="~{fragments/layout :: commonHead('Quản lý khóa học')}"></th:block>
</head>
<body>
<nav th:replace="~{fragments/layout :: navbar('khoahoc')}"></nav>

<main class="container">
    <th:block th:replace="~{fragments/layout :: alerts}"></th:block>

    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="h4 mb-0"><i class="bi bi-journal-bookmark me-2"></i>Quản lý khóa học</h2>
        <a th:href="@{/khoahoc/them}" class="btn btn-primary">
            <i class="bi bi-plus-lg me-1"></i>Thêm khóa học
        </a>
    </div>

    <!-- Tìm kiếm: GET, dùng name + th:value (không phải th:field) -->
    <form th:action="@{/khoahoc}" method="get" class="row g-2 mb-3">
        <div class="col-md-6">
            <input type="text" name="keyword" class="form-control"
                   th:value="${keyword}" placeholder="Tìm theo tên khóa học..."/>
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-outline-primary"><i class="bi bi-search"></i> Tìm</button>
        </div>
        <div class="col-auto" th:unless="${#strings.isEmpty(keyword)}">
            <a th:href="@{/khoahoc}" class="btn btn-outline-secondary">Xem tất cả</a>
        </div>
    </form>

    <table class="table table-hover bg-white shadow-sm">
        <thead class="table-dark">
        <tr>
            <th>ID</th><th>Tên khóa học</th><th>Giảng viên</th>
            <th class="text-center">Số tín chỉ</th><th class="text-center">Thao tác</th>
        </tr>
        </thead>
        <tbody>
        <tr th:each="kh : ${danhSach}">
            <td th:text="${kh.id}">1</td>
            <td th:text="${kh.tenKhoaHoc}">Tên</td>
            <td th:text="${kh.giangVien}">GV</td>
            <td class="text-center" th:text="${kh.soTinChi}">3</td>
            <td class="text-center">
                <!-- Link sửa với path variable -->
                <a th:href="@{/khoahoc/sua/{id}(id=${kh.id})}"
                   class="btn btn-sm btn-outline-warning" title="Sửa">
                    <i class="bi bi-pencil"></i>
                </a>
                <!-- Xoá: form POST + confirm, tên lấy từ data-* -->
                <form th:action="@{/khoahoc/xoa/{id}(id=${kh.id})}" method="post" class="d-inline"
                      th:data-name="${kh.tenKhoaHoc}"
                      onsubmit="return confirm('Xóa khóa học ' + this.dataset.name + '?')">
                    <button type="submit" class="btn btn-sm btn-outline-danger" title="Xóa">
                        <i class="bi bi-trash"></i>
                    </button>
                </form>
            </td>
        </tr>
        <tr th:if="${#lists.isEmpty(danhSach)}">
            <td colspan="5" class="text-center text-muted py-4">Không có dữ liệu</td>
        </tr>
        </tbody>
    </table>
</main>

<footer th:replace="~{fragments/layout :: footer}"></footer>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
</body>
</html>
```

> ⚠️ **Không** viết `onclick="return confirm('Xóa [[${kh.tenKhoaHoc}]]?')"` — Thymeleaf không xử lý inline trong thuộc tính thường. Cũng **không** dùng `th:onclick="'...' + ${...}"` — Thymeleaf 3.1 chỉ cho `th:on*` nhận số/boolean. Dùng `th:data-*` như trên.

### ✅ Checklist TODO 4.5
- [ ] `/khoahoc` hiển thị 3 khóa học, có navbar (menu "Khóa học" sáng), footer.
- [ ] Tìm `java` → chỉ còn "Lập trình Java"; ô tìm vẫn giữ chữ `java`; nút "Xem tất cả" xuất hiện.
- [ ] Tìm `xyz` → dòng "Không có dữ liệu".
- [ ] Bấm 🗑 → hộp thoại hiện **đúng tên** khóa học.

## TODO 4.6 — Template `khoahoc/form.html` (dùng chung thêm/sửa)

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <th:block th:replace="~{fragments/layout :: commonHead(${pageTitle})}"></th:block>
</head>
<body>
<nav th:replace="~{fragments/layout :: navbar('khoahoc')}"></nav>

<main class="container" style="max-width: 640px">
    <h2 class="h4 mb-3" th:text="${pageTitle}">Form</h2>

    <form th:action="@{/khoahoc/luu}" th:object="${khoaHoc}" method="post"
          class="card card-body shadow-sm">

        <!-- Lưu id khi sửa; khi thêm mới value rỗng → id = null → INSERT -->
        <input type="hidden" th:field="*{id}"/>

        <div class="mb-3">
            <label for="tenKhoaHoc" class="form-label">Tên khóa học <span class="text-danger">*</span></label>
            <input type="text" class="form-control" th:field="*{tenKhoaHoc}" maxlength="100" required/>
        </div>
        <div class="mb-3">
            <label for="giangVien" class="form-label">Giảng viên</label>
            <input type="text" class="form-control" th:field="*{giangVien}" maxlength="100"/>
        </div>
        <div class="mb-3">
            <label for="soTinChi" class="form-label">Số tín chỉ</label>
            <input type="number" class="form-control" th:field="*{soTinChi}" min="1" max="6"/>
        </div>

        <div class="d-flex gap-2">
            <button type="submit" class="btn btn-primary"><i class="bi bi-save me-1"></i>Lưu</button>
            <a th:href="@{/khoahoc}" class="btn btn-outline-secondary">Hủy</a>
        </div>
    </form>
</main>

<footer th:replace="~{fragments/layout :: footer}"></footer>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
</body>
</html>
```

> 💡 Hidden `id` có thể bị sửa bằng F12 → ghi đè bản ghi khác. Chấp nhận được trong bài này; cách an toàn hơn (id lấy từ URL `/{id}/edit` + DTO) xem `Chapter6.md` / `Chapter6_BTVN.md` Bài 4. Validation server-side cho form này: áp dụng kỹ thuật của Bài 5.

### ✅ Checklist TODO 4.6
- [ ] `/khoahoc/them` → tiêu đề "Thêm khóa học", form trống; F12: `<input type="hidden" id="id" name="id" value="">`.
- [ ] `/khoahoc/sua/1` → tiêu đề "Sửa khóa học", dữ liệu điền sẵn, hidden `value="1"`.

## TODO 4.7 — Kiểm thử CRUD & đối chiếu database

| # | Thao tác | Giao diện | Console / SSMS |
|---|---|---|---|
| 1 | Thêm "Thymeleaf nâng cao", "Phạm D", 2 | Flash xanh "Thêm khóa học thành công!" | `insert into khoa_hoc ...`; SSMS có dòng mới, đúng dấu |
| 2 | F5 sau khi thêm | Không thêm trùng | Số dòng không đổi |
| 3 | Sửa id=1 → tín chỉ 4 | "Cập nhật thành công!" | `update khoa_hoc set ... where id=?` |
| 4 | `/khoahoc/sua/999` | Flash đỏ "Không tìm thấy..." | Không lỗi 500 |
| 5 | Xoá id=2 (OK ở hộp thoại) | "Đã xóa khóa học!" | `delete from khoa_hoc where id=?` |
| 6 | Gõ tay `GET /khoahoc/xoa/1` trên thanh địa chỉ | Lỗi 405 (đúng — xoá chỉ nhận POST) | id=1 vẫn còn |
| 7 | Restart app | Dữ liệu còn nguyên | `ddl-auto=update` giữ dữ liệu |
| 8 | Sửa trực tiếp trong SSMS rồi F5 | Giao diện thấy giá trị mới | View đọc từ DB |

### ✅ Checklist TODO 4.7
- [ ] 8/8 kịch bản đạt.
- [ ] Bấm ✕ trên flash message → đóng được (JS offline OK).

## ⚠️ Lỗi thường gặp — Bài 4

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| `Failed to configure a DataSource: 'url' attribute is not specified` | Có `data-jpa` nhưng thiếu `spring.datasource.url` | TODO 4.1 (hoặc chưa tới Bài 4 thì comment dependency lại) |
| `The TCP/IP connection to the host localhost, port 1433 has failed` | TCP/IP chưa bật / sai cổng | SQL Server Configuration Manager → bật TCP/IP 1433 → restart service |
| `Login failed for user 'sa'` | Chưa bật Mixed Mode / sai mật khẩu / `sa` bị disable | SSMS → Properties → Security; `ALTER LOGIN sa ENABLE` |
| `PKIX path building failed` | Thiếu `trustServerCertificate=true` | Thêm vào URL |
| Tiếng Việt thành `?` trong DB | Cột `VARCHAR` / chèn SQL thiếu `N'...'` | `use_nationalized_character_data=true`, drop bảng tạo lại; dùng `N'...'` |
| Sửa mà lại **thêm mới** bản ghi | Form thiếu `<input type="hidden" th:field="*{id}">` | Thêm hidden id |
| Thêm mới mà lại **ghi đè** bản ghi | Model của form Thêm có sẵn id (dùng lại object cũ) | GET `/them` luôn `new KhoaHoc()` |
| 500 `NoSuchElementException: No value present` | `findById(id).orElseThrow()` với id không tồn tại | Dùng `Optional` + redirect kèm thông báo |
| 405 khi bấm xoá | Form xoá `method="post"` nhưng Controller `@GetMapping` | `@PostMapping("/xoa/{id}")` |
| Hộp thoại xoá hiện `[[${kh.tenKhoaHoc}]]` nguyên văn | Inline expression trong `onclick`/`onsubmit` | `th:data-name` + `this.dataset.name` |
| `No property 'ten' found for type 'KhoaHoc'` | Sai tên field trong tên method repository | Dùng đúng `TenKhoaHoc` |
| Trang trắng nửa chừng, console `LazyInitializationException` | (khi có quan hệ LAZY) view đọc dữ liệu chưa load, `open-in-view=false` | Fetch đủ trong Service (xem Chapter6_BTVN Bài 5) |

---

# Bài 5 (Nâng cao) — Validation, thông báo lỗi & i18n

**Mục tiêu:** form đăng ký có validate phía server (Bean Validation), hiển thị lỗi theo Bootstrap, đa ngôn ngữ Việt/Anh bằng `messages*.properties` và `?lang=`.

| TODO | Yêu cầu | Kết quả |
|---|---|---|
| 5.1 | Kiểm tra dependency validation | `jakarta.validation` dùng được |
| 5.2 | DTO `NguoiDung` với ràng buộc, message là **key** | `{validation.ten.notblank}` |
| 5.3 | `messages.properties` (vi) + `messages_en.properties` (en) + cấu hình `spring.messages.*` | Thông báo lấy theo ngôn ngữ |
| 5.4 | `WebConfig`: `LocaleResolver` + `LocaleChangeInterceptor` | `?lang=en` đổi ngôn ngữ, nhớ trong session |
| 5.5 | Controller `@Valid` + `BindingResult`, thành công → redirect | PRG |
| 5.6 | Template form: nhãn `#{...}`, lỗi `th:errors`, Bootstrap `is-invalid` | Lỗi hiện dưới từng ô |
| 5.7 | Trang thành công dùng message có tham số | `Xin chào {0}!` |
| 5.8 | Kiểm thử ma trận ngôn ngữ × lỗi | Mọi nhánh đúng |

## TODO 5.1 — Dependency

Đã có từ TODO 0.1:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### ✅ Checklist TODO 5.1
- [ ] Import được `jakarta.validation.constraints.NotBlank` (không phải `javax.validation`).

## TODO 5.2 — DTO `NguoiDung`

```java
package com.example.demo.model;

import jakarta.validation.constraints.*;

public class NguoiDung {

    @NotBlank(message = "{validation.ten.notblank}")
    @Size(min = 2, max = 50, message = "{validation.ten.size}")
    private String hoTen;

    @NotBlank(message = "{validation.email.notblank}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotNull(message = "{validation.tuoi.notnull}")
    @Min(value = 18, message = "{validation.tuoi.min}")
    @Max(value = 100, message = "{validation.tuoi.max}")
    private Integer tuoi;

    // Không bắt buộc: chấp nhận rỗng HOẶC đúng 10 chữ số
    @Pattern(regexp = "^$|^[0-9]{10}$", message = "{validation.sdt.pattern}")
    private String soDienThoai;

    public NguoiDung() {}

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getTuoi() { return tuoi; }
    public void setTuoi(Integer tuoi) { this.tuoi = tuoi; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
}
```

| Annotation | Bắt lỗi | Lưu ý |
|---|---|---|
| `@NotBlank` | `null`, `""`, `"   "` | Chỉ cho `String` |
| `@NotNull` | `null` | Dùng cho số (`Integer`) |
| `@Size(min, max)` | Độ dài chuỗi | Chuỗi rỗng vi phạm cả `@NotBlank` lẫn `@Size(min=2)` → hiện **2** lỗi là bình thường |
| `@Email` | Định dạng email | `""` được coi là **hợp lệ** → cần thêm `@NotBlank` |
| `@Min`, `@Max` | Giá trị số | `null` được coi là hợp lệ → cần `@NotNull` |
| `@Pattern` | Regex | `""` **không** khớp `^[0-9]{10}$` → thêm `^$\|` nếu không bắt buộc |
| `message = "{key}"` | Lấy nội dung từ `messages*.properties` theo ngôn ngữ hiện tại | Có dấu `{}`; sai key → hiện nguyên chữ `{validation...}` |

### ✅ Checklist TODO 5.2
- [ ] `tuoi` là `Integer` (không phải `int`).
- [ ] Mọi `message` có dạng `"{...}"` và key sẽ có trong file properties (TODO 5.3).

## TODO 5.3 — File messages & cấu hình

`src/main/resources/messages.properties` (mặc định — **tiếng Việt**):

```properties
# ===== Validation =====
validation.ten.notblank=Họ tên không được để trống
validation.ten.size=Họ tên phải từ 2 đến 50 ký tự
validation.email.notblank=Email không được để trống
validation.email.invalid=Email không hợp lệ
validation.tuoi.notnull=Tuổi không được để trống
validation.tuoi.min=Tuổi phải từ 18 trở lên
validation.tuoi.max=Tuổi không được vượt quá 100
validation.sdt.pattern=Số điện thoại phải gồm đúng 10 chữ số

# ===== Giao diện =====
form.title.dangky=Đăng ký tài khoản
form.label.hoten=Họ tên
form.label.email=Email
form.label.tuoi=Tuổi
form.label.sdt=Số điện thoại
form.button.dangky=Đăng ký
form.errors.summary=Vui lòng kiểm tra lại các trường sau:
form.success=Đăng ký thành công!
form.success.greeting=Xin chào {0}, thông tin của bạn đã được ghi nhận.
form.link.again=Đăng ký thêm
```

`src/main/resources/messages_en.properties`:

```properties
validation.ten.notblank=Full name is required
validation.ten.size=Full name must be 2 to 50 characters
validation.email.notblank=Email is required
validation.email.invalid=Invalid email format
validation.tuoi.notnull=Age is required
validation.tuoi.min=Must be at least 18 years old
validation.tuoi.max=Age cannot exceed 100
validation.sdt.pattern=Phone number must be exactly 10 digits

form.title.dangky=Register Account
form.label.hoten=Full name
form.label.email=Email
form.label.tuoi=Age
form.label.sdt=Phone number
form.button.dangky=Register
form.errors.summary=Please check the following fields:
form.success=Registration successful!
form.success.greeting=Hello {0}, your information has been recorded.
form.link.again=Register another
```

Thêm vào `application.properties`:

```properties
# ===== i18n =====
spring.messages.basename=messages
spring.messages.encoding=UTF-8
# Không có messages_vi → dùng messages.properties, KHÔNG rơi về ngôn ngữ của hệ điều hành
spring.messages.fallback-to-system-locale=false
```

**Vì sao không tự tạo bean `MessageSource` như bản gốc?** Spring Boot đã tự cấu hình khi thấy `messages.properties`; chỉ cần 3 dòng trên. Nếu vẫn muốn tự tạo bean thì **bắt buộc** đặt tên method là `messageSource`, gọi `setDefaultEncoding("UTF-8")` **và** `setFallbackToSystemLocale(false)`.

> ⚠️ **Bẫy `fallback-to-system-locale`:** mặc định là `true`. Khi người dùng chọn `vi`, Spring tìm `messages_vi.properties` → không có → **rơi về ngôn ngữ hệ điều hành** → máy Windows tiếng Anh sẽ lấy `messages_en.properties` → giao diện tiếng Anh dù đã chọn tiếng Việt. Trên máy bạn (Windows tiếng Việt) có thể không thấy lỗi, nhưng lên máy chấm thi thì lộ ra.
>
> ⚠️ Message có tham số `{0}` dùng `MessageFormat` → dấu nháy đơn phải viết **đôi**: `You''re welcome`. Viết `You're` → mất nháy và tham số không được thay.

### ✅ Checklist TODO 5.3
- [ ] 2 file nằm ngay trong `src/main/resources/` (không trong `templates/`).
- [ ] Hai file có **cùng tập key** (thiếu key ở file nào → ngôn ngữ đó hiện `??key_en??`).
- [ ] IntelliJ hiển thị tiếng Việt đúng khi mở file (File Encodings → Properties Files = UTF-8; **bỏ** tick *Transparent native-to-ascii conversion* nếu bị đổi thành `ọ`... — dạng `\uXXXX` vẫn chạy đúng, chỉ khó đọc).

## TODO 5.4 — `config/WebConfig.java`

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // TÊN BEAN BẮT BUỘC là "localeResolver" (DispatcherServlet tìm theo tên)
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.forLanguageTag("vi"));    // lần đầu vào: tiếng Việt
        return slr;
    }

    // Bắt tham số ?lang=en / ?lang=vi trên mọi request → lưu vào session
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        registry.addInterceptor(lci);
    }
}
```

### ✅ Checklist TODO 5.4
- [ ] Method tên đúng `localeResolver` (đặt `myLocaleResolver` → Spring bỏ qua, `?lang=` không có tác dụng).
- [ ] Class có `@Configuration` và nằm trong package con của `com.example.demo`.

## TODO 5.5 — Controller

```java
package com.example.demo.controller;

import com.example.demo.model.NguoiDung;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dangky")
public class DangKyController {

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "dangky/form";
    }

    @PostMapping
    public String xuLy(@Valid @ModelAttribute("nguoiDung") NguoiDung nguoiDung,
                       BindingResult result,              // PHẢI đứng ngay sau tham số @Valid
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "dangky/form";                         // trả VIEW → giữ dữ liệu + lỗi
        }
        ra.addFlashAttribute("hoTen", nguoiDung.getHoTen());
        return "redirect:/dangky/thanh-cong";             // PRG
    }

    @GetMapping("/thanh-cong")
    public String thanhCong() {
        return "dangky/thanh-cong";
    }
}
```

### ✅ Checklist TODO 5.5
- [ ] Thứ tự tham số: `@Valid @ModelAttribute(...) NguoiDung`, **ngay sau** là `BindingResult`.
- [ ] Có lỗi → `return "dangky/form"` (view), **không** redirect.
- [ ] Thành công → `redirect:` + flash.

## TODO 5.6 — Template `dangky/form.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:lang="${#locale.language}">
<head>
    <!-- #{...}: message theo ngôn ngữ hiện tại; truyền làm tham số fragment -->
    <th:block th:replace="~{fragments/layout :: commonHead(#{form.title.dangky})}"></th:block>
</head>
<body>
<nav th:replace="~{fragments/layout :: navbar('dangky')}"></nav>

<main class="container" style="max-width: 640px">

    <!-- Đổi ngôn ngữ -->
    <div class="text-end mb-2">
        <a th:href="@{/dangky(lang=vi)}" class="btn btn-sm"
           th:classappend="${#locale.language == 'vi'} ? 'btn-dark' : 'btn-outline-dark'">Tiếng Việt</a>
        <a th:href="@{/dangky(lang=en)}" class="btn btn-sm"
           th:classappend="${#locale.language == 'en'} ? 'btn-dark' : 'btn-outline-dark'">English</a>
    </div>

    <h2 class="h4 mb-3" th:text="#{form.title.dangky}">Đăng ký</h2>

    <form th:action="@{/dangky}" th:object="${nguoiDung}" method="post"
          class="card card-body shadow-sm" novalidate>

        <!-- Tổng hợp lỗi: #fields chỉ dùng BÊN TRONG phần tử có th:object -->
        <div th:if="${#fields.hasAnyErrors()}" class="alert alert-danger py-2">
            <strong th:text="#{form.errors.summary}">Vui lòng kiểm tra:</strong>
            <ul class="mb-0">
                <li th:each="err : ${#fields.allErrors()}" th:text="${err}">Lỗi</li>
            </ul>
        </div>

        <div class="mb-3">
            <label for="hoTen" class="form-label" th:text="#{form.label.hoten}">Họ tên</label>
            <!-- th:errorclass: thêm class khi field có lỗi -->
            <input type="text" class="form-control" th:field="*{hoTen}" th:errorclass="is-invalid"/>
            <!-- th:errors: in thông báo lỗi; chỉ render khi có lỗi -->
            <div class="invalid-feedback" th:errors="*{hoTen}">Lỗi họ tên</div>
        </div>

        <div class="mb-3">
            <label for="email" class="form-label" th:text="#{form.label.email}">Email</label>
            <input type="email" class="form-control" th:field="*{email}" th:errorclass="is-invalid"/>
            <div class="invalid-feedback" th:errors="*{email}">Lỗi email</div>
        </div>

        <div class="mb-3">
            <label for="tuoi" class="form-label" th:text="#{form.label.tuoi}">Tuổi</label>
            <input type="number" class="form-control" th:field="*{tuoi}" th:errorclass="is-invalid"/>
            <div class="invalid-feedback" th:errors="*{tuoi}">Lỗi tuổi</div>
        </div>

        <div class="mb-3">
            <label for="soDienThoai" class="form-label" th:text="#{form.label.sdt}">Số điện thoại</label>
            <!-- Cách viết của bản gốc, tương đương th:errorclass -->
            <input type="text" class="form-control" th:field="*{soDienThoai}"
                   th:classappend="${#fields.hasErrors('soDienThoai')} ? 'is-invalid'"/>
            <div class="invalid-feedback" th:errors="*{soDienThoai}">Lỗi SĐT</div>
        </div>

        <button type="submit" class="btn btn-primary" th:text="#{form.button.dangky}">Đăng ký</button>
    </form>
</main>

<footer th:replace="~{fragments/layout :: footer}"></footer>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
</body>
</html>
```

**Giải thích:**

| Cú pháp | Ý nghĩa |
|---|---|
| `#{form.label.hoten}` | **Message expression** — đọc từ `messages*.properties` theo locale hiện tại |
| `#locale.language` | Ngôn ngữ hiện tại (`vi`/`en`) — dùng tô sáng nút, đặt `lang` cho `<html>` |
| `th:errorclass="is-invalid"` | Thêm class Bootstrap khi field lỗi (phải đứng **cùng thẻ** với `th:field`) |
| `<div class="invalid-feedback" th:errors>` | Bootstrap chỉ hiện `.invalid-feedback` khi phần tử **liền trước** có `is-invalid` |
| `#fields.allErrors()` | Danh sách mọi thông báo lỗi (đã dịch theo ngôn ngữ) |
| `novalidate` | Tắt kiểm tra của trình duyệt để thấy validate **phía server** khi thực hành |

### ✅ Checklist TODO 5.6
- [ ] `/dangky` → nhãn tiếng Việt, nút "Tiếng Việt" tô đậm.
- [ ] `th:field`, `th:errors`, `#fields` đều nằm **trong** `<form th:object="${nguoiDung}">`.

## TODO 5.7 — Trang `dangky/thanh-cong.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:lang="${#locale.language}">
<head>
    <th:block th:replace="~{fragments/layout :: commonHead(#{form.success})}"></th:block>
</head>
<body>
<nav th:replace="~{fragments/layout :: navbar('dangky')}"></nav>

<main class="container" style="max-width: 640px">
    <div class="alert alert-success">
        <h2 class="h4" th:text="#{form.success}">Thành công!</h2>
        <!-- Message có tham số: #{key(${giaTri})} thay vào {0} -->
        <p class="mb-0" th:if="${hoTen}" th:text="#{form.success.greeting(${hoTen})}">Xin chào ...</p>
    </div>
    <a th:href="@{/dangky}" class="btn btn-outline-primary" th:text="#{form.link.again}">Đăng ký thêm</a>
</main>

<footer th:replace="~{fragments/layout :: footer}"></footer>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
</body>
</html>
```

### ✅ Checklist TODO 5.7
- [ ] Đăng ký hợp lệ → URL `/dangky/thanh-cong`, có "Xin chào **<tên>**, ...".
- [ ] F5 → câu chào biến mất (flash chỉ sống 1 lần), không lỗi.

## TODO 5.8 — Kiểm thử ma trận

| # | Ngôn ngữ | Thao tác | Kết quả mong đợi |
|---|---|---|---|
| 1 | vi | Mở `/dangky` | Nhãn tiếng Việt |
| 2 | vi | Submit form trống | Ô Họ tên có 2 lỗi, Email 1 lỗi, Tuổi 1 lỗi, SĐT **không** lỗi; khung tổng hợp lỗi tiếng Việt |
| 3 | vi | Tên `A`, email `abc`, tuổi `17`, SĐT `123` | 4 lỗi tương ứng; dữ liệu đã nhập **vẫn còn** |
| 4 | vi | Tuổi `101` | "Tuổi không được vượt quá 100" |
| 5 | — | Bấm "English" | Toàn bộ nhãn tiếng Anh, URL `/dangky?lang=en` |
| 6 | en | Submit form trống | Lỗi **tiếng Anh** |
| 7 | en | Sang `/sinhvien` rồi quay lại `/dangky` (không có `?lang`) | Vẫn tiếng Anh (lưu trong session) |
| 8 | en | Đăng ký hợp lệ | "Hello <tên>, ..." |
| 9 | vi | Bấm "Tiếng Việt" | Trở lại tiếng Việt |
| 10 | vi | Mở cửa sổ ẩn danh (session mới) | Mặc định tiếng Việt |

### ✅ Checklist TODO 5.8
- [ ] 10/10 kịch bản đạt.
- [ ] Không thấy chữ dạng `??form.title.dangky_vi??` hay `{validation.ten.notblank}` ở bất kỳ đâu.

## ⚠️ Lỗi thường gặp — Bài 5

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| Hiện `??form.title.dangky_vi??` | Thymeleaf không tìm thấy key: sai tên key, sai `basename`, file đặt sai chỗ | Kiểm tra key, file ở `src/main/resources/`, `spring.messages.basename=messages` |
| Lỗi validate hiện nguyên `{validation.ten.notblank}` | Key không có trong file messages | Thêm key vào **cả hai** file |
| Chọn tiếng Việt nhưng hiện tiếng Anh (chỉ trên một số máy) | `fallback-to-system-locale=true` (mặc định) + máy dùng Windows tiếng Anh | `spring.messages.fallback-to-system-locale=false` |
| Tiếng Việt trong thông báo bị lỗi (`Há» tÃªn`) | File `.properties` không đọc bằng UTF-8 | `spring.messages.encoding=UTF-8` + lưu file UTF-8 |
| `?lang=en` không có tác dụng | Thiếu interceptor, hoặc bean LocaleResolver không tên `localeResolver`, hoặc thiếu `@Configuration` | TODO 5.4 |
| Submit có lỗi → trang **400** thay vì hiện lỗi | `BindingResult` không đứng ngay sau `@Valid` | Đặt liền nhau |
| Submit có lỗi → form trống, mất lỗi | Nhánh lỗi dùng `redirect:` | Trả `"dangky/form"` |
| Không có lỗi nào dù nhập sai | Thiếu `@Valid`, hoặc import `javax.validation` | `jakarta.validation.Valid` |
| Ô SĐT để trống vẫn báo lỗi | Regex `^[0-9]{10}$` không chấp nhận rỗng | `^$\|^[0-9]{10}$` |
| Ô tuổi trống → `Failed to convert property value ... 'int'` | Dùng `int` | `Integer` + `@NotNull` |
| Thông báo lỗi có nhưng không hiện trên giao diện | `.invalid-feedback` không đứng **ngay sau** input có `is-invalid`, hoặc thiếu `th:errorclass` | Đúng cấu trúc TODO 5.6 |
| `Cannot process attribute th:errors... no associated BindingStatus` / lỗi `#fields` | Đặt ngoài `<form th:object>` | Đưa vào trong form |
| Câu chào không thay `{0}` hoặc mất dấu `'` | Nháy đơn trong message có tham số | Viết `''` |

---

# Tổng kết

## Bảng Thymeleaf Expressions

| Expression | Cú pháp | Ví dụ | Bài |
|---|---|---|---|
| Variable | `${...}` | `${sv.hoTen}` | 1 |
| Selection | `*{...}` | `*{ten}` (trong `th:object`) | 2 |
| Message | `#{...}` | `#{form.title.dangky}`, `#{form.success.greeting(${hoTen})}` | 5 |
| Link URL | `@{...}` | `@{/khoahoc/sua/{id}(id=${kh.id})}`, `@{/dangky(lang=en)}` | 2, 4, 5 |
| Fragment | `~{...}` | `~{fragments/layout :: navbar('home')}` | 3 |
| Literal substitution | `\|...\|` | `\|${sp.ten} (SL: ${sp.soLuong})\|` | 2 |
| Bean Spring | `${@bean...}` | `${@environment.getProperty('app.bootstrap.offline')}` | 3 |

| Attribute | Mục đích | Bài |
|---|---|---|
| `th:text` | Ghi text, **escape** HTML (an toàn) | 1 |
| `th:utext` | Ghi HTML thô — **không** dùng cho dữ liệu người dùng (XSS) | — |
| `th:each` | Vòng lặp (+ biến trạng thái `stat`) | 1 |
| `th:if` / `th:unless` | Điều kiện | 1 |
| `th:switch` / `th:case` | Switch-case | — |
| `th:href` / `th:src` / `th:action` | URL | 2–5 |
| `th:object` / `th:field` | Binding form | 2, 4, 5 |
| `th:errors` / `th:errorclass` | Lỗi validation | 5 |
| `th:classappend` | Thêm class có điều kiện | 3, 5 |
| `th:fragment` / `th:replace` / `th:insert` | Layout | 3 |
| `th:data-*` | Đưa dữ liệu cho JavaScript | 4 |
| `th:block` | Thẻ ảo để nhóm, không sinh HTML | 3 |

| Utility | Ví dụ |
|---|---|
| `#lists` | `#lists.isEmpty(x)`, `#lists.size(x)` |
| `#strings` | `#strings.isEmpty(keyword)` |
| `#numbers` | `#numbers.formatDecimal(x, 1, 'POINT', 0, 'COMMA')` |
| `#fields` | `#fields.hasErrors('email')`, `#fields.hasAnyErrors()`, `#fields.allErrors()` |
| `#locale` | `#locale.language` |
| `#dates` / `#temporals` | `java.util.Date` / `java.time.*` |

## Lỗi chung cần nhớ (mọi bài)

| # | Quy tắc | Vi phạm → |
|---|---|---|
| 1 | Template ở `templates/`, tên view không `.html`, đúng hoa thường | `Error resolving template` (có thể chỉ lộ khi chạy jar/Linux) |
| 2 | `${x.y}` = getter `getY()` | `Property or field 'y' cannot be found` |
| 3 | Biến có thể `null` → `th:if` hoặc `?.` | `... cannot be found on null` |
| 4 | Tên model attribute = `th:object` = `@ModelAttribute("...")` | `Neither BindingResult nor plain target object` |
| 5 | `BindingResult` ngay sau `@Valid` | Trang 400 |
| 6 | Kiểu số dùng wrapper (`Integer`, `Double`) | Trang 400 / `typeMismatch` |
| 7 | POST thành công → `redirect:` + flash | F5 gửi lại form |
| 8 | Xoá/sửa dữ liệu chỉ bằng POST | Bot/prefetch xoá nhầm |
| 9 | Không đặt `[[${...}]]` trong `onclick`/`onsubmit`; dùng `th:data-*` | Hiện nguyên văn / lỗi `th:on*` |
| 10 | Bootstrap: file trong `static/vendor`, URL `@{/vendor/...}`, giữ thư mục `fonts/`, dùng `bootstrap.bundle` | Mất style / icon / JS |
| 11 | Lỗi Thymeleaf chỉ lộ khi render đúng nhánh → test **mọi** nhánh (rỗng, có lỗi, sửa, không tìm thấy, đổi ngôn ngữ) | Lỗi "ngầm" tới lúc chấm bài mới thấy |
| 12 | Đọc lỗi: tìm `Caused by:` cuối và `(template: "..." - line X, col Y)` trong console | Mất thời gian đoán mò |
