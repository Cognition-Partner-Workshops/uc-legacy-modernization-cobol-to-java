package com.suitecrm.kb.service;

import com.suitecrm.kb.dto.*;
import com.suitecrm.kb.entity.*;
import com.suitecrm.kb.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class KBService {

    private final KBContentRepository contentRepository;
    private final KBCategoryRepository categoryRepository;
    private final KBDocumentRepository documentRepository;
    private final KBTagRepository tagRepository;
    private final KBContentTagRepository contentTagRepository;

    // Articles
    public Page<KBContentDto> listArticles(Pageable pageable) {
        return contentRepository.findByDeletedFalse(pageable).map(this::toContentDto);
    }

    public Page<KBContentDto> listArticlesByStatus(String status, Pageable pageable) {
        return contentRepository.findByStatusAndDeletedFalse(status, pageable).map(this::toContentDto);
    }

    public Page<KBContentDto> listArticlesByCategory(UUID categoryId, Pageable pageable) {
        return contentRepository.findByCategoryIdAndDeletedFalse(categoryId, pageable).map(this::toContentDto);
    }

    public KBContentDto getArticle(UUID id) {
        KBContent content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KB Article not found: " + id));
        content.setViewCount(content.getViewCount() + 1);
        contentRepository.save(content);
        KBContentDto dto = toContentDto(content);
        List<KBContentTag> tagRels = contentTagRepository.findByKbContentId(id);
        dto.setTags(tagRels.stream()
                .map(rel -> tagRepository.findById(rel.getTagId()).map(KBTag::getName).orElse(""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList()));
        return dto;
    }

    public KBContentDto createArticle(KBContentCreateRequest request, UUID userId) {
        KBContent content = KBContent.builder()
                .name(request.getName())
                .body(request.getBody())
                .summary(request.getSummary())
                .status(request.getStatus() != null ? request.getStatus() : "draft")
                .activeDate(request.getActiveDate())
                .expDate(request.getExpDate())
                .categoryId(request.getCategoryId())
                .assignedUserId(userId)
                .createdBy(userId)
                .build();
        content = contentRepository.save(content);

        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                KBTag tag = tagRepository.findByNameAndDeletedFalse(tagName)
                        .orElseGet(() -> tagRepository.save(KBTag.builder().name(tagName).build()));
                tag.setUsageCount(tag.getUsageCount() + 1);
                tagRepository.save(tag);
                contentTagRepository.save(KBContentTag.builder()
                        .kbContentId(content.getId())
                        .tagId(tag.getId())
                        .build());
            }
        }
        return toContentDto(content);
    }

    public KBContentDto updateArticle(UUID id, KBContentCreateRequest request) {
        KBContent content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KB Article not found: " + id));
        if (request.getName() != null) content.setName(request.getName());
        if (request.getBody() != null) content.setBody(request.getBody());
        if (request.getSummary() != null) content.setSummary(request.getSummary());
        if (request.getStatus() != null) content.setStatus(request.getStatus());
        if (request.getActiveDate() != null) content.setActiveDate(request.getActiveDate());
        if (request.getExpDate() != null) content.setExpDate(request.getExpDate());
        if (request.getCategoryId() != null) content.setCategoryId(request.getCategoryId());
        content.setRevision(content.getRevision() + 1);
        return toContentDto(contentRepository.save(content));
    }

    public void deleteArticle(UUID id) {
        KBContent content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KB Article not found: " + id));
        content.setDeleted(true);
        contentRepository.save(content);
    }

    public Page<KBContentDto> searchArticles(String query, Pageable pageable) {
        return contentRepository.search(query, pageable).map(this::toContentDto);
    }

    public List<KBContentDto> getMostViewed(int limit) {
        return contentRepository.findMostViewed(PageRequest.of(0, limit)).stream()
                .map(this::toContentDto).collect(Collectors.toList());
    }

    public KBContentDto rateArticle(UUID id, boolean helpful) {
        KBContent content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KB Article not found: " + id));
        if (helpful) {
            content.setHelpfulCount(content.getHelpfulCount() + 1);
        } else {
            content.setNotHelpfulCount(content.getNotHelpfulCount() + 1);
        }
        return toContentDto(contentRepository.save(content));
    }

    // Categories
    public List<KBCategoryDto> listCategories() {
        return categoryRepository.findByParentIdIsNullAndDeletedFalse().stream()
                .map(this::toCategoryDto).collect(Collectors.toList());
    }

    public KBCategoryDto getCategory(UUID id) {
        KBCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KB Category not found: " + id));
        return toCategoryDto(cat);
    }

    public KBCategoryDto createCategory(KBCategoryDto request, UUID userId) {
        KBCategory category = KBCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .displayOrder(request.getDisplayOrder())
                .isExternal(request.getIsExternal())
                .createdBy(userId)
                .build();
        return toCategoryDto(categoryRepository.save(category));
    }

    // Documents
    public List<KBDocumentDto> getDocumentsByArticle(UUID articleId) {
        return documentRepository.findByKbContentIdAndDeletedFalse(articleId).stream()
                .map(this::toDocumentDto).collect(Collectors.toList());
    }

    // Tags
    public List<String> listTags() {
        return tagRepository.findByDeletedFalse().stream()
                .map(KBTag::getName).collect(Collectors.toList());
    }

    public List<String> searchTags(String query) {
        return tagRepository.findByNameContainingIgnoreCaseAndDeletedFalse(query).stream()
                .map(KBTag::getName).collect(Collectors.toList());
    }

    private KBContentDto toContentDto(KBContent content) {
        return KBContentDto.builder()
                .id(content.getId())
                .name(content.getName())
                .body(content.getBody())
                .summary(content.getSummary())
                .status(content.getStatus())
                .revision(content.getRevision())
                .activeDate(content.getActiveDate())
                .expDate(content.getExpDate())
                .viewCount(content.getViewCount())
                .helpfulCount(content.getHelpfulCount())
                .notHelpfulCount(content.getNotHelpfulCount())
                .categoryId(content.getCategoryId())
                .assignedUserId(content.getAssignedUserId())
                .dateEntered(content.getDateEntered())
                .dateModified(content.getDateModified())
                .build();
    }

    private KBCategoryDto toCategoryDto(KBCategory cat) {
        List<KBCategoryDto> children = categoryRepository.findByParentIdAndDeletedFalse(cat.getId())
                .stream().map(this::toCategoryDto).collect(Collectors.toList());
        return KBCategoryDto.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .parentId(cat.getParentId())
                .displayOrder(cat.getDisplayOrder())
                .isExternal(cat.getIsExternal())
                .children(children)
                .build();
    }

    private KBDocumentDto toDocumentDto(KBDocument doc) {
        return KBDocumentDto.builder()
                .id(doc.getId())
                .name(doc.getName())
                .description(doc.getDescription())
                .documentType(doc.getDocumentType())
                .filename(doc.getFilename())
                .fileMimeType(doc.getFileMimeType())
                .fileUrl(doc.getFileUrl())
                .kbContentId(doc.getKbContentId())
                .categoryId(doc.getCategoryId())
                .status(doc.getStatus())
                .dateEntered(doc.getDateEntered())
                .build();
    }
}
