package com.orange.signsatwork.biz.task;

import com.orange.signsatwork.biz.domain.PasswordResetToken;
import com.orange.signsatwork.biz.persistence.model.PasswordResetTokenDB;
import com.orange.signsatwork.biz.persistence.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TokensPurgeTask {


    @Autowired
    PasswordResetTokenRepository passwordTokenRepository;

    @Scheduled(cron = "${purge.cron.expression}")
    public void purgeExpired() {

       Date now = Date.from(Instant.now());

       List<PasswordResetTokenDB> passwordResetTokenDBList = passwordTokenRepository.selectAllExpiredSince(now);

       for(PasswordResetTokenDB passwordResetTokenDB:passwordResetTokenDBList) {
         passwordTokenRepository.delete(passwordResetTokenDB.getId());
       }

    }
}
