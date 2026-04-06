package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByIdAndDeletedFalse(UUID id);

    Page<Employee> findByDeletedFalse(Pageable pageable);

    Optional<Employee> findByUserIdAndDeletedFalse(UUID userId);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.employeeStatus = :status")
    Page<Employee> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.department = :department")
    List<Employee> findByDepartment(@Param("department") String department);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.reportsToId = :managerId")
    List<Employee> findDirectReports(@Param("managerId") UUID managerId);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND " +
           "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Employee> searchEmployees(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT e.department FROM Employee e WHERE e.deleted = false AND e.department IS NOT NULL ORDER BY e.department")
    List<String> findDistinctDepartments();
}
