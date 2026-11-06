# Hướng Dẫn Kiểm Thử & Checklist Nộp Bài (TODO 6)

## 1. Kịch bản kiểm thử bằng tay (Manual Test Checklist)
Khởi động ứng dụng (chạy `Se20B01De200475SdmPeApplication` hoặc `mvn spring-boot:run`), truy cập `http://localhost:8080/`:

| STT | Thao tác | Kết quả kỳ vọng | Câu hỏi đề |
|---|---|---|---|
| 1 | Truy cập `http://localhost:8080/` | Danh sách đủ thiết bị, sắp xếp theo Ngày lắp đặt giảm dần, cột Category hiển thị Tên (không phải ID) | Q1.1 |
| 2 | Tìm kiếm từ khóa (ví dụ "LG" hoặc "lg") | Tìm kiếm wildcard không phân biệt hoa thường, kết quả vẫn sắp xếp ngày giảm dần | Q1.2 |
| 3 | Tìm kiếm chuỗi không tồn tại ("xyzabc") | Hiển thị thông báo "No records found", không văng lỗi 500 | Q1.2 |
| 4 | Nhấn nút "Add New" | Mở form Add New Device, tất cả các ô nhập đều trống | Q1.3, Q3.1 |
| 5 | Nhấn "Save" khi form đang trống | Hiển thị thông báo lỗi required tương ứng ngay cạnh từng field | Q3.2a |
| 6 | Device Code nhập sai định dạng (ví dụ "abc123") | Báo lỗi định dạng 3 chữ hoa + 3 số (ví dụ DVC101) | Q3.2a |
| 7 | Device Code đã tồn tại trong DB | Báo lỗi "Device Code already exists" | Q3.2a |
| 8 | Nhập Price = 0 hoặc âm | Báo lỗi "Price must be a number greater than 0" | Q3.2a |
| 9 | Nhập Warranty = 0 hoặc > 60 | Báo lỗi "Warranty must be a positive integer between 1 and 60" | Q3.2a |
| 10 | Nhập Installation Date ngày quá khứ / hôm nay | Báo lỗi "Installation date must be a future date" | Q3.2a |
| 11 | Kiểm tra dropdown Category | Hiển thị đủ các danh mục lấy động từ database | Q5 |
| 12 | Nhập toàn bộ dữ liệu hợp lệ và nhấn "Save" | Hiển thị "Created new device successfully" và form được reset trống | Q3.2b, c |
| 13 | Nhấn "Back" quay lại trang danh sách | Xuất hiện thiết bị vừa thêm mới trên danh sách | Q3.3 |
| 14 | Nhấn "View" trên 1 dòng | Hiển thị chi tiết toàn bộ field read-only, có nút Back | Q4 |
| 15 | Nhấn "Delete" -> chọn Cancel | Hộp thoại đóng, thiết bị không bị xóa | Q2.3 |
| 16 | Nhấn "Delete" thiết bị Active (`status = 'AC'`) | Hiển thị thông báo lỗi "Cannot delete an active device", không xóa | Q2.2 |
| 17 | Nhấn "Delete" thiết bị Inactive/Draft -> chọn Yes | Báo "Deleted successfully", thiết bị bị xóa khỏi danh sách và DB | Q2.2 |

---

## 2. Checklist "Điểm 0" trước khi nộp bài
- [x] Package gốc đúng `com.de200475.mvc` (đã xóa triệt để package mẫu thừa `com.se123456`).
- [x] Không sử dụng Lombok hay thư viện lạ ngoài phạm vi đề thi cho phép.
- [x] Tên bảng (`categories`, `devices`) và tên cột khớp 100% với script SQL.
- [x] `application.properties`: `ddl-auto=none`, cổng 8080, context-path `/`.
- [x] Build và repackage thành công `SDM.jar`.

---

## 3. Hướng dẫn nộp bài lên SEB
1. Dừng ứng dụng đang chạy.
2. Chạy lệnh dọn dẹp thư mục target:
   ```powershell
   .\mvnw.cmd clean
   ```
3. Chuột phải vào thư mục `SE20B01_DE200475_SDM_PE` -> **Send to** -> **Compressed (zipped) folder**.
4. Mở file zip kiểm tra: đảm bảo thấy file `pom.xml` và thư mục `src` nằm ngay trong thư mục gốc của file zip.
5. Nộp file zip lên SEB và kiểm tra xác nhận nộp thành công.
