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
