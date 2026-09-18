package fu.de200475;

import fu.de200475.dao.DepartmentDAO;
import fu.de200475.pojo.Department;
import fu.de200475.pojo.Employee;
import fu.de200475.pojo.Gender;
import fu.de200475.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DepartmentDAO departmentDAO = new DepartmentDAO();

        Department it = new Department("Marketing", "Ha Noi");
        Employee e1 = new Employee("aa.nguyen@company.com", "Nguyen Van A", Gender.MALE,
                new BigDecimal("15000000"), LocalDate.of(2022, 1, 10));
        Employee e2 = new Employee("bb.tran@company.com", "Tran Thi B", Gender.FEMALE,
                new BigDecimal("18000000"), LocalDate.of(2021, 6, 1));
        Employee e3 = new Employee("cc.le@company.com", "Le Van C", Gender.OTHER,
                new BigDecimal("12000000"), LocalDate.of(2023, 3, 15));

        it.addEmployee(e1);
        it.addEmployee(e2);
        it.addEmployee(e3);

        departmentDAO.save(it);
        System.out.println("Da luu Department, id = " + it.getId());

        Department found = departmentDAO.findByIdWithEmployees(it.getId());
        System.out.println("Phong ban: " + found.getName());
        for (Employee e : found.getEmployees()) {
            System.out.println("  - " + e);
        }

        System.out.println("\n========== TODO 2.8: TAI HIEN N+1 QUERY PROBLEM ==========");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Department> departments = em.createQuery("SELECT d FROM Department d", Department.class).getResultList();
            System.out.println("So luong Department tim thay: " + departments.size());

            for (Department dept : departments) {
                System.out.println("Phong ban: " + dept.getName());
                for (Employee emp : dept.getEmployees()) {
                    System.out.println("  --> Nhan vien: " + emp.getFullName() + " | Luong: " + emp.getSalary());
                }
            }
        } finally {
            em.close();
        }

        System.out.println("\n========== TODO 2.9: FIX N+1 BANG JOIN FETCH ==========");
        List<Department> departmentsWithEmployees = departmentDAO.findAllWithEmployees();
        System.out.println("So luong Department tim thay (chi voi 1 query): " + departmentsWithEmployees.size());

        for (Department dept : departmentsWithEmployees) {
            System.out.println("Phong ban: " + dept.getName());
            for (Employee emp : dept.getEmployees()) {
                System.out.println("  --> Nhan vien: " + emp.getFullName() + " | Luong: " + emp.getSalary());
            }
        }

        JPAUtil.close();
    }
}