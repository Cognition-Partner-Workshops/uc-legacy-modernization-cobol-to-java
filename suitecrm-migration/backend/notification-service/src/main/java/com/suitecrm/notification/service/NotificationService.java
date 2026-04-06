package com.suitecrm.notification.service;

import com.suitecrm.notification.dto.*;
import com.suitecrm.notification.entity.*;
import com.suitecrm.notification.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class NotificationService {
    private final AlertRepository alertRepository;
    private final FavoriteRepository favoriteRepository;

    public Page<AlertDto> getAlertsByUser(UUID userId, Pageable pageable) {
        return alertRepository.findByAssignedUserIdAndDeletedFalse(userId, pageable).map(this::toAlertDto);
    }
    public List<AlertDto> getUnreadAlerts(UUID userId) {
        return alertRepository.findByAssignedUserIdAndIsReadFalseAndDeletedFalse(userId).stream().map(this::toAlertDto).toList();
    }
    @Transactional
    public void markAlertRead(UUID id) {
        Alert alert = alertRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));
        alert.setIsRead(true);
        alertRepository.save(alert);
    }
    public List<FavoriteDto> getFavorites(UUID userId) {
        return favoriteRepository.findByAssignedUserIdAndDeletedFalse(userId).stream().map(this::toFavoriteDto).toList();
    }
    @Transactional
    public FavoriteDto addFavorite(UUID userId, String moduleName, UUID recordId) {
        Favorite fav = Favorite.builder().assignedUserId(userId).moduleName(moduleName).recordId(recordId).build();
        return toFavoriteDto(favoriteRepository.save(fav));
    }

    private AlertDto toAlertDto(Alert a) {
        return AlertDto.builder().id(a.getId()).name(a.getName()).alertType(a.getAlertType())
                .urlRedirect(a.getUrlRedirect()).targetModule(a.getTargetModule()).description(a.getDescription())
                .isRead(a.getIsRead()).assignedUserId(a.getAssignedUserId())
                .dateEntered(a.getDateEntered()).dateModified(a.getDateModified()).build();
    }
    private FavoriteDto toFavoriteDto(Favorite f) {
        return FavoriteDto.builder().id(f.getId()).moduleName(f.getModuleName())
                .recordId(f.getRecordId()).assignedUserId(f.getAssignedUserId()).dateEntered(f.getDateEntered()).build();
    }
}
