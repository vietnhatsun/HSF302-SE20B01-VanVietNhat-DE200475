package com.hsf302.chapter6.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", "Trang hoặc tài nguyên bạn tìm kiếm không tồn tại.");
        model.addAttribute("errorDetail", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute("errorCode", 400);
        model.addAttribute("errorMessage", "Tham số đường dẫn hoặc yêu cầu không đúng định dạng (Ví dụ: ID phải là số).");
        model.addAttribute("errorDetail", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "Đã có lỗi không mong muốn xảy ra trong hệ thống.");
        model.addAttribute("errorDetail", ex.getMessage());
        return "error/500";
    }
}
