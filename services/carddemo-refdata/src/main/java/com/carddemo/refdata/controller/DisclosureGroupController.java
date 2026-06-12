package com.carddemo.refdata.controller;

import java.util.List;

import com.carddemo.refdata.entity.DisclosureGroup;
import com.carddemo.refdata.service.DisclosureGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reference/disclosure-groups")
public class DisclosureGroupController {

    private final DisclosureGroupService service;

    public DisclosureGroupController(DisclosureGroupService service) {
        this.service = service;
    }

    @GetMapping
    public List<DisclosureGroup> list() {
        return service.findAll();
    }

    @GetMapping("/{groupId}/{typeCode}/{catCode}")
    public ResponseEntity<DisclosureGroup> get(@PathVariable String groupId,
                                               @PathVariable String typeCode,
                                               @PathVariable int catCode) {
        DisclosureGroup entity = service.findByKey(groupId, typeCode, catCode);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entity);
    }

    @PostMapping
    public ResponseEntity<DisclosureGroup> create(@Valid @RequestBody DisclosureGroup entity) {
        DisclosureGroup created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{groupId}/{typeCode}/{catCode}")
    public ResponseEntity<DisclosureGroup> update(@PathVariable String groupId,
                                                  @PathVariable String typeCode,
                                                  @PathVariable int catCode,
                                                  @Valid @RequestBody DisclosureGroup entity) {
        DisclosureGroup updated = service.update(groupId, typeCode, catCode, entity);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{groupId}/{typeCode}/{catCode}")
    public ResponseEntity<Void> delete(@PathVariable String groupId,
                                       @PathVariable String typeCode,
                                       @PathVariable int catCode) {
        if (!service.delete(groupId, typeCode, catCode)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
