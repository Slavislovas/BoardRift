package com.socialnetwork.boardrift.util;

import com.socialnetwork.boardrift.repository.SuspensionRepository;
import com.socialnetwork.boardrift.repository.WarningRepository;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ExpiredSuspensionChecker {
    private final SuspensionRepository suspensionRepository;
    private final WarningRepository warningRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void checkAndDeleteExpiredSuspensions() {
        List<SuspensionEntity> suspensions = suspensionRepository.findAll();

        for (SuspensionEntity suspension : suspensions) {
            if (suspension.getSuspendedUntil().isEqual(LocalDate.now())) {
                suspension.getUser().getReceivedWarnings().clear();
                suspension.getUser().setSuspension(null);
                suspensionRepository.delete(suspension);
            }
        }
    }
}