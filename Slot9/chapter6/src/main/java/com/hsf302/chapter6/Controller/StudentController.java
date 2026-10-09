package com.hsf302.chapter6.Controller;

import com.hsf302.chapter6.entity.Student;
import com.hsf302.chapter6.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
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

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** Chạy trước MỌI handler trong controller → view nào cũng có ${majors} */
    @ModelAttribute("majors")
    public List<String> majors() {
        return studentService.getMajors();
    }

    // ==================== BÀI NÂNG CAO 1, 2, 3, 4: READ ALL, SEARCH, FILTER, SORT, PAGINATION ====================

    @GetMapping
    public String list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "major", required = false) String major,
            @RequestParam(name = "minGpa", required = false) Double minGpa,
            @RequestParam(name = "maxGpa", required = false) Double maxGpa,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortField", defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {

        Page<Student> studentPage = studentService.findWithFilterAndSort(
                keyword, major, minGpa, maxGpa, page, size, sortField, sortDir);

        model.addAttribute("studentPage", studentPage);
        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedMajor", major);
        model.addAttribute("minGpa", minGpa);
        model.addAttribute("maxGpa", maxGpa);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");

        return "students/list";
    }

    // ==================== READ ONE ====================

    @GetMapping("/{id:[0-9]+}")
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
        // 1. Kiểm tra trùng email (khi email đã đúng định dạng)
        if (!bindingResult.hasFieldErrors("email")
                && studentService.isEmailTaken(student.getEmail(), null)) {
            bindingResult.rejectValue("email", "duplicate", "Email đã tồn tại");
        }
        // 2. Trả lại form nếu có lỗi
        if (bindingResult.hasErrors()) {
            return formView(model, false);
        }
        // 3. Lưu vào DB
        try {
            studentService.create(student);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("email", "duplicate", "Email đã tồn tại");
            return formView(model, false);
        }
        ra.addFlashAttribute("successMsg", "Thêm sinh viên thành công!");
        return "redirect:/students";
    }

    // ==================== UPDATE ====================

    @GetMapping("/{id:[0-9]+}/edit")
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

    @PostMapping("/{id:[0-9]+}/edit")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("student") Student student,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes ra) {
        student.setId(id);

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

    @PostMapping("/{id:[0-9]+}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        if (studentService.delete(id)) {
            ra.addFlashAttribute("successMsg", "Xóa sinh viên thành công!");
        } else {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sinh viên để xóa!");
        }
        return "redirect:/students";
    }

    // ==================== PHẦN GIÁO VIÊN KIỂM TRA TRÊN LỚP ====================

    /** 1. Trang báo cáo thống kê & quản lý nâng cao */
    @GetMapping("/stats")
    public String showStatistics(Model model) {
        model.addAttribute("stats", studentService.getMajorStatistics());
        model.addAttribute("topStudents", studentService.findTop3ByGpa());
        return "students/stats";
    }

    /** 2. Thao tác Edit nâng cao: Cộng điểm hàng loạt theo chuyên ngành */
    @PostMapping("/bonus-gpa")
    public String addBonusGpa(@RequestParam("major") String major,
                              @RequestParam(name = "bonus", defaultValue = "0.2") Double bonus,
                              RedirectAttributes ra) {
        int count = studentService.addBonusGpaForMajor(major, bonus);
        ra.addFlashAttribute("successMsg", "Đã cộng " + bonus + " GPA cho " + count + " sinh viên chuyên ngành " + major);
        return "redirect:/students/stats";
    }

    /** 3. Thao tác Delete nâng cao 1: Xóa theo chuyên ngành */
    @PostMapping("/delete-by-major")
    public String deleteByMajor(@RequestParam("major") String major, RedirectAttributes ra) {
        long count = studentService.deleteByMajor(major);
        ra.addFlashAttribute("successMsg", "Đã xóa toàn bộ " + count + " sinh viên thuộc chuyên ngành " + major);
        return "redirect:/students/stats";
    }

    /** 4. Thao tác Delete nâng cao 2: Xóa sinh viên có GPA dưới mức quy định */
    @PostMapping("/delete-low-gpa")
    public String deleteLowGpa(@RequestParam(name = "minGpa", defaultValue = "2.0") Double minGpa,
                               RedirectAttributes ra) {
        int count = studentService.deleteStudentsWithGpaLessThan(minGpa);
        ra.addFlashAttribute("successMsg", "Đã xóa " + count + " sinh viên có GPA < " + minGpa);
        return "redirect:/students/stats";
    }

    // ==================== Helper ====================

    private String formView(Model model, boolean isEdit) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("pageTitle", isEdit ? "Cập nhật sinh viên" : "Thêm sinh viên mới");
        return FORM_VIEW;
    }
}