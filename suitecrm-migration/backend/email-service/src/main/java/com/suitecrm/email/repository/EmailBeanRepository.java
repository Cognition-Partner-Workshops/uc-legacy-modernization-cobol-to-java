package com.suitecrm.email.repository;

import com.suitecrm.email.entity.EmailBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailBeanRepository extends JpaRepository<EmailBean, UUID> {
    List<EmailBean> findByEmailIdAndDeletedFalse(UUID emailId);
    List<EmailBean> findByBeanIdAndBeanModuleAndDeletedFalse(UUID beanId, String beanModule);
}
