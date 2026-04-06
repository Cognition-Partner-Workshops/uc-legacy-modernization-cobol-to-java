package com.suitecrm.workflow.engine;

import com.suitecrm.workflow.entity.Workflow;
import com.suitecrm.workflow.entity.WorkflowAction;
import com.suitecrm.workflow.entity.WorkflowCondition;
import com.suitecrm.workflow.repository.WorkflowActionRepository;
import com.suitecrm.workflow.repository.WorkflowConditionRepository;
import com.suitecrm.workflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEngine {

    private final WorkflowRepository workflowRepository;
    private final WorkflowActionRepository actionRepository;
    private final WorkflowConditionRepository conditionRepository;
    private final ConditionEvaluator conditionEvaluator;
    private final ActionExecutor actionExecutor;

    @Transactional
    public void triggerWorkflows(String module, String event, Map<String, Object> record, Map<String, Object> previousRecord) {
        List<Workflow> workflows = workflowRepository.findByFlowModuleAndStatusAndDeletedFalse(module, "Active");
        for (Workflow workflow : workflows) {
            if (!shouldRun(workflow, event)) continue;
            List<WorkflowCondition> conditions = conditionRepository
                .findByWorkflowIdAndDeletedFalseOrderByConditionOrderAsc(workflow.getId());
            if (conditionEvaluator.evaluate(conditions, record, previousRecord)) {
                List<WorkflowAction> actions = actionRepository
                    .findByWorkflowIdAndDeletedFalseOrderByActionOrderAsc(workflow.getId());
                for (WorkflowAction action : actions) {
                    try {
                        actionExecutor.execute(action, record);
                    } catch (Exception e) {
                        log.error("Workflow action failed: workflow={}, action={}", workflow.getId(), action.getId(), e);
                    }
                }
                if (!Boolean.TRUE.equals(workflow.getMultipleRuns())) {
                    log.info("Workflow {} executed once, skipping further runs", workflow.getId());
                }
            }
        }
    }

    private boolean shouldRun(Workflow workflow, String event) {
        String runWhen = workflow.getRunWhen();
        if (runWhen == null || "Always".equals(runWhen)) return true;
        return runWhen.equalsIgnoreCase(event);
    }
}
