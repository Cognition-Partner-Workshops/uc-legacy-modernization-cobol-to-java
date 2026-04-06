package com.suitecrm.auth.service;

import com.suitecrm.auth.dto.*;
import com.suitecrm.auth.entity.*;
import com.suitecrm.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AclRoleRepository aclRoleRepository;
    private final AclActionRepository aclActionRepository;
    private final SecurityGroupRepository securityGroupRepository;
    private final EmployeeRepository employeeRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final OAuthTokenRepository oAuthTokenRepository;

    // ==================== User Management ====================

    @Transactional(readOnly = true)
    public Page<UserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserDto);
    }

    @Transactional(readOnly = true)
    public UserDto getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return toUserDto(user);
    }

    // ==================== Employee Management ====================

    @Transactional(readOnly = true)
    public Page<EmployeeDto> listEmployees(Pageable pageable) {
        return employeeRepository.findByDeletedFalse(pageable).map(this::toEmployeeDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployee(UUID id) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
        return toEmployeeDto(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> searchEmployees(String query, Pageable pageable) {
        return employeeRepository.searchEmployees(query, pageable).map(this::toEmployeeDto);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getDirectReports(UUID managerId) {
        return employeeRepository.findDirectReports(managerId)
                .stream().map(this::toEmployeeDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getDepartments() {
        return employeeRepository.findDistinctDepartments();
    }

    // ==================== ACL Role Management ====================

    @Transactional(readOnly = true)
    public Page<AclRoleDto> listAclRoles(Pageable pageable) {
        return aclRoleRepository.findByDeletedFalse(pageable).map(this::toAclRoleDto);
    }

    @Transactional(readOnly = true)
    public AclRoleDto getAclRole(UUID id) {
        AclRole role = aclRoleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("ACL Role not found: " + id));
        return toAclRoleDto(role);
    }

    public AclRoleDto createAclRole(AclRoleDto request) {
        AclRole role = AclRole.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : false)
                .build();
        AclRole saved = aclRoleRepository.save(role);
        log.info("Created ACL role: {} ({})", saved.getName(), saved.getId());
        return toAclRoleDto(saved);
    }

    // ==================== ACL Action Management ====================

    @Transactional(readOnly = true)
    public List<AclActionDto> getActionsByCategory(String category) {
        return aclActionRepository.findByCategoryAndDeletedFalse(category)
                .stream().map(this::toAclActionDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getActionCategories() {
        return aclActionRepository.findDistinctCategories();
    }

    // ==================== Security Group Management ====================

    @Transactional(readOnly = true)
    public Page<SecurityGroupDto> listSecurityGroups(Pageable pageable) {
        return securityGroupRepository.findByDeletedFalse(pageable).map(this::toSecurityGroupDto);
    }

    @Transactional(readOnly = true)
    public SecurityGroupDto getSecurityGroup(UUID id) {
        SecurityGroup group = securityGroupRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Security Group not found: " + id));
        return toSecurityGroupDto(group);
    }

    public SecurityGroupDto createSecurityGroup(SecurityGroupDto request) {
        SecurityGroup group = SecurityGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .noninheritable(request.getNoninheritable() != null ? request.getNoninheritable() : false)
                .assignedUserId(request.getAssignedUserId())
                .build();
        SecurityGroup saved = securityGroupRepository.save(group);
        log.info("Created security group: {} ({})", saved.getName(), saved.getId());
        return toSecurityGroupDto(saved);
    }

    // ==================== User Preferences ====================

    @Transactional(readOnly = true)
    public List<UserPreference> getUserPreferences(UUID userId) {
        return userPreferenceRepository.findByUserIdAndDeletedFalse(userId);
    }

    public UserPreference setUserPreference(UUID userId, String key, String value) {
        UserPreference pref = userPreferenceRepository.findByUserIdAndKey(userId, key)
                .orElse(UserPreference.builder()
                        .userId(userId)
                        .preferenceKey(key)
                        .build());
        pref.setPreferenceValue(value);
        return userPreferenceRepository.save(pref);
    }

    // ==================== Mappers ====================

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(user.getStatus())
                .build();
    }

    private EmployeeDto toEmployeeDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .userId(employee.getUserId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .title(employee.getTitle())
                .department(employee.getDepartment())
                .phoneWork(employee.getPhoneWork())
                .phoneMobile(employee.getPhoneMobile())
                .email(employee.getEmail())
                .addressStreet(employee.getAddressStreet())
                .addressCity(employee.getAddressCity())
                .addressState(employee.getAddressState())
                .addressPostalCode(employee.getAddressPostalCode())
                .addressCountry(employee.getAddressCountry())
                .reportsToId(employee.getReportsToId())
                .employeeStatus(employee.getEmployeeStatus())
                .dateEntered(employee.getDateEntered())
                .dateModified(employee.getDateModified())
                .build();
    }

    private AclRoleDto toAclRoleDto(AclRole role) {
        return AclRoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isAdmin(role.getIsAdmin())
                .dateEntered(role.getDateEntered())
                .dateModified(role.getDateModified())
                .build();
    }

    private AclActionDto toAclActionDto(AclAction action) {
        return AclActionDto.builder()
                .id(action.getId())
                .name(action.getName())
                .category(action.getCategory())
                .acltype(action.getAcltype())
                .aclaccess(action.getAclaccess())
                .build();
    }

    private SecurityGroupDto toSecurityGroupDto(SecurityGroup group) {
        return SecurityGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .noninheritable(group.getNoninheritable())
                .assignedUserId(group.getAssignedUserId())
                .createdBy(group.getCreatedBy())
                .dateEntered(group.getDateEntered())
                .dateModified(group.getDateModified())
                .build();
    }
}
