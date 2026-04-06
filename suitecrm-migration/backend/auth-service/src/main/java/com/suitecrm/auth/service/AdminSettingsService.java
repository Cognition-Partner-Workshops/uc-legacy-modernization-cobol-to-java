package com.suitecrm.auth.service;

import com.suitecrm.auth.entity.AdminSetting;
import com.suitecrm.auth.repository.AdminSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSettingsService {

    private final AdminSettingRepository adminSettingRepository;

    public Optional<String> getSetting(String category, String name) {
        return adminSettingRepository.findByCategoryAndName(category, name)
            .map(AdminSetting::getValue);
    }

    public List<AdminSetting> getCategory(String category) {
        return adminSettingRepository.findByCategory(category);
    }

    @Transactional
    public AdminSetting setSetting(String category, String name, String value) {
        AdminSetting setting = adminSettingRepository.findByCategoryAndName(category, name)
            .orElse(AdminSetting.builder().category(category).name(name).build());
        setting.setValue(value);
        return adminSettingRepository.save(setting);
    }
}
