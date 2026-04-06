package com.suitecrm.connector.service;

import com.suitecrm.connector.dto.*;
import com.suitecrm.connector.entity.*;
import com.suitecrm.connector.repository.*;
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
public class ConnectorService {

    private final ConnectorRepository connectorRepository;
    private final ExternalAccountRepository externalAccountRepository;
    private final OAuthKeyRepository oauthKeyRepository;
    private final ConnectorLogRepository logRepository;
    private final IntegrationMappingRepository mappingRepository;

    // Connectors
    public Page<ConnectorDto> listConnectors(Pageable pageable) {
        return connectorRepository.findByDeletedFalse(pageable).map(this::toConnectorDto);
    }

    public List<ConnectorDto> listEnabledConnectors() {
        return connectorRepository.findByIsEnabledTrueAndDeletedFalse()
                .stream().map(this::toConnectorDto).collect(Collectors.toList());
    }

    public ConnectorDto getConnector(UUID id) {
        Connector connector = connectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector not found: " + id));
        return toConnectorDto(connector);
    }

    public ConnectorDto createConnector(ConnectorCreateRequest request, UUID userId) {
        Connector connector = Connector.builder()
                .name(request.getName())
                .description(request.getDescription())
                .connectorType(request.getConnectorType())
                .sourceModule(request.getSourceModule())
                .connectorClass(request.getConnectorClass())
                .baseUrl(request.getBaseUrl())
                .apiVersion(request.getApiVersion())
                .authType(request.getAuthType())
                .configJson(request.getConfigJson())
                .fieldMapping(request.getFieldMapping())
                .createdBy(userId)
                .build();
        return toConnectorDto(connectorRepository.save(connector));
    }

    public ConnectorDto updateConnector(UUID id, ConnectorCreateRequest request) {
        Connector connector = connectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector not found: " + id));
        if (request.getName() != null) connector.setName(request.getName());
        if (request.getDescription() != null) connector.setDescription(request.getDescription());
        if (request.getBaseUrl() != null) connector.setBaseUrl(request.getBaseUrl());
        if (request.getAuthType() != null) connector.setAuthType(request.getAuthType());
        if (request.getConfigJson() != null) connector.setConfigJson(request.getConfigJson());
        if (request.getFieldMapping() != null) connector.setFieldMapping(request.getFieldMapping());
        return toConnectorDto(connectorRepository.save(connector));
    }

    public void deleteConnector(UUID id) {
        Connector connector = connectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector not found: " + id));
        connector.setDeleted(true);
        connectorRepository.save(connector);
    }

    public ConnectorDto toggleConnector(UUID id) {
        Connector connector = connectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector not found: " + id));
        connector.setIsEnabled(!connector.getIsEnabled());
        return toConnectorDto(connectorRepository.save(connector));
    }

    // External accounts
    public List<ExternalAccountDto> getExternalAccounts(UUID connectorId) {
        return externalAccountRepository.findByConnectorIdAndDeletedFalse(connectorId)
                .stream().map(this::toExternalAccountDto).collect(Collectors.toList());
    }

    public List<ExternalAccountDto> getUserExternalAccounts(UUID userId) {
        return externalAccountRepository.findByUserIdAndDeletedFalse(userId)
                .stream().map(this::toExternalAccountDto).collect(Collectors.toList());
    }

    // OAuth keys
    public List<OAuthKeyDto> listOAuthKeys() {
        return oauthKeyRepository.findByDeletedFalse()
                .stream().map(this::toOAuthKeyDto).collect(Collectors.toList());
    }

    // Logs
    public Page<ConnectorLog> getConnectorLogs(UUID connectorId, Pageable pageable) {
        return logRepository.findByConnectorIdOrderByDateEnteredDesc(connectorId, pageable);
    }

    // Mappings
    public List<IntegrationMapping> getMappings(UUID connectorId) {
        return mappingRepository.findByConnectorIdAndDeletedFalse(connectorId);
    }

    // Mappers
    private ConnectorDto toConnectorDto(Connector connector) {
        return ConnectorDto.builder()
                .id(connector.getId())
                .name(connector.getName())
                .description(connector.getDescription())
                .connectorType(connector.getConnectorType())
                .sourceModule(connector.getSourceModule())
                .baseUrl(connector.getBaseUrl())
                .apiVersion(connector.getApiVersion())
                .authType(connector.getAuthType())
                .status(connector.getStatus())
                .isEnabled(connector.getIsEnabled())
                .dateEntered(connector.getDateEntered())
                .dateModified(connector.getDateModified())
                .build();
    }

    private ExternalAccountDto toExternalAccountDto(ExternalAccount account) {
        return ExternalAccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .connectorId(account.getConnectorId())
                .userId(account.getUserId())
                .externalId(account.getExternalId())
                .application(account.getApplication())
                .dateEntered(account.getDateEntered())
                .build();
    }

    private OAuthKeyDto toOAuthKeyDto(OAuthKey key) {
        return OAuthKeyDto.builder()
                .id(key.getId())
                .name(key.getName())
                .connectorId(key.getConnectorId())
                .oauthType(key.getOauthType())
                .tokenUrl(key.getTokenUrl())
                .authorizeUrl(key.getAuthorizeUrl())
                .scope(key.getScope())
                .build();
    }
}
