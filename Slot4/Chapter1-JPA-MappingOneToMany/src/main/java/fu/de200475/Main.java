package fu.de200475;

import fu.de200475.dao.DepartmentDAO;
import fu.de200475.dao.EmployeeDAO;
import fu.de200475.dao.ProjectDAO;
import fu.de200475.pojo.Department;
import fu.de200475.pojo.Employee;
import fu.de200475.pojo.Gender;
import fu.de200475.pojo.Project;
import fu.de200475.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Don dep data demo cu neu co de chay lai chuong trinh khong bi trung Unique Key
        cleanDemoData();

        DepartmentDAO departmentDAO = new DepartmentDAO();
        EmployeeDAO employeeDAO = new EmployeeDAO();
        ProjectDAO projectDAO = new ProjectDAO();

        // Lay phong ban co san hoac tao moi phong ban mac dinh
        Department dept = departmentDAO.findAll().stream().findFirst().orElse(null);
        if (dept == null) {
            dept = new Department("Software Engineering", "Building Alpha");
            departmentDAO.save(dept);
        }

        // TODO 5.7: Main demo - tao 3 Employee, 2 Project, phan cong cheo va in ra
        System.out.println("========== TODO 5.7: DEMO TAO PROJECT VA EMPLOYEE ==========");

        // 1. Tao 2 Project
        Project prjA = new Project("PRJ001", "E-Commerce System", new BigDecimal("100000000"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        Project prjB = new Project("PRJ002", "Mobile Banking App", new BigDecimal("250000000"), LocalDate.of(2024, 3, 1), null);

        projectDAO.save(prjA);
        projectDAO.save(prjB);
        System.out.println("Da tao 2 Project thanh cong: ID_A = " + prjA.getId() + ", ID_B = " + prjB.getId());

        // 2. Tao 3 Employee (day du thong tin salary, hireDate, gender, active, department)
        Employee e1 = new Employee("an.nguyen@fpt.edu.vn", "Nguyen Van An", Gender.MALE,
                new BigDecimal("15000000"), LocalDate.of(2022, 1, 10), true);
        e1.setDepartment(dept);

        Employee e2 = new Employee("binh.tran@fpt.edu.vn", "Tran Thi Binh", Gender.FEMALE,
                new BigDecimal("20000000"), LocalDate.of(2021, 5, 15), true);
        e2.setDepartment(dept);

        Employee e3 = new Employee("cuong.le@fpt.edu.vn", "Le Van Cuong", Gender.OTHER,
                new BigDecimal("18000000"), LocalDate.of(2023, 2, 20), true);
        e3.setDepartment(dept);

        employeeDAO.save(e1);
        employeeDAO.save(e2);
        employeeDAO.save(e3);
        System.out.println("Da tao 3 Employee thanh cong: ID_1 = " + e1.getId() + ", ID_2 = " + e2.getId() + ", ID_3 = " + e3.getId());

        // 3. Phan cong cheo qua EmployeeDAO: NV1 -> A+B, NV2 -> B, NV3 -> A
        System.out.println("\n========== PHAN CONG CHEO NHAN VIEN VAO PROJECT ==========");
        employeeDAO.assignEmployeeToProject(e1.getId(), prjA.getId());
        employeeDAO.assignEmployeeToProject(e1.getId(), prjB.getId());
        employeeDAO.assignEmployeeToProject(e2.getId(), prjB.getId());
        employeeDAO.assignEmployeeToProject(e3.getId(), prjA.getId());
        System.out.println("Phan cong thanh cong!");

        // 4. Luu va in ra danh sach project cua tung nhan vien
        System.out.println("\n========== DANH SACH PROJECT CUA TUNG NHAN VIEN ==========");
        List<Employee> employees = employeeDAO.findAllWithProjects();
        for (Employee emp : employees) {
            System.out.println("\nNhan vien: " + emp.getFullName() + " (" + emp.getEmail() + ")");
            System.out.println("  - So luong project tham gia: " + emp.getProjects().size());
            for (Project p : emp.getProjects()) {
                System.out.println("    + [" + p.getProjectCode() + "] " + p.getProjectName() + " | Ngan sach: " + p.getBudget());
            }
        }

        JPAUtil.close();
    }

    private static void cleanDemoData() {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Xoa lien ket trong bang phu employee_project truoc
            em.createNativeQuery("DELETE FROM employee_project").executeUpdate();
            // Xoa nhan vien demo
            em.createQuery("DELETE FROM Employee e WHERE e.email IN ('an.nguyen@fpt.edu.vn', 'binh.tran@fpt.edu.vn', 'cuong.le@fpt.edu.vn')").executeUpdate();
            // Xoa project demo
            em.createQuery("DELETE FROM Project p WHERE p.projectCode IN ('PRJ001', 'PRJ002')").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        } finally {
            em.close();
        }
    }
}