package com.suitecrm.scheduler.service;

import com.suitecrm.scheduler.dto.*;
import com.suitecrm.scheduler.entity.*;
import com.suitecrm.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class SchedulerService {
    private final SchedulerRepository schedulerRepository;
    private final SchedulerJobRepository schedulerJobRepository;

    public Page<SchedulerDto> getAllSchedulers(Pageable pageable) {
        return schedulerRepository.findByDeletedFalse(pageable).map(this::toSchedulerDto);
    }
    public SchedulerDto getSchedulerById(UUID id) {
        return toSchedulerDto(schedulerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Scheduler not found: " + id)));
    }
    public Page<SchedulerJobDto> getAllJobs(Pageable pageable) {
        return schedulerJobRepository.findByDeletedFalse(pageable).map(this::toJobDto);
    }

    private SchedulerDto toSchedulerDto(Scheduler s) {
        return SchedulerDto.builder().id(s.getId()).name(s.getName()).job(s.getJob()).jobInterval(s.getJobInterval())
                .timeFrom(s.getTimeFrom()).timeTo(s.getTimeTo()).lastRun(s.getLastRun()).status(s.getStatus())
                .catchUp(s.getCatchUp()).dateEntered(s.getDateEntered()).dateModified(s.getDateModified()).build();
    }
    private SchedulerJobDto toJobDto(SchedulerJob j) {
        return SchedulerJobDto.builder().id(j.getId()).name(j.getName()).schedulerId(j.getSchedulerId())
                .executeTime(j.getExecuteTime()).status(j.getStatus()).resolution(j.getResolution())
                .message(j.getMessage()).target(j.getTarget()).retryCount(j.getRetryCount())
                .failureCount(j.getFailureCount()).dateEntered(j.getDateEntered()).dateModified(j.getDateModified()).build();
    }
}
