package org.example.library.recommendations.job;

import lombok.extern.slf4j.Slf4j;
import org.example.library.recommendations.service.GenreMappingService;
import org.example.library.recommendations.service.GlobalRebuildService;
import org.example.library.recommendations.service.RecommendationTriggerService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class RecommendationRebuildJob {

    private final RecommendationTriggerService triggerService;
    private final GenreMappingService genreMappingService;
    private final GlobalRebuildService rebuildService;
    private final RecommendationRebuildJob self;

    public RecommendationRebuildJob(RecommendationTriggerService triggerService,
                                    GenreMappingService genreMappingService,
                                    GlobalRebuildService rebuildService,
                                    @Lazy RecommendationRebuildJob self) {
        this.triggerService = triggerService;
        this.genreMappingService = genreMappingService;
        this.rebuildService = rebuildService;
        this.self = self;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        self.run();
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void run() {
        if (triggerService.shouldUpdateGenreMapping()) {
            log.info("New categories detected. Updating genre mapping...");
            genreMappingService.updateGenreMapping();
        }

        if (triggerService.shouldRebuildVocabulary()) {
            log.info("Starting full rebuild of recommendation model...");
            rebuildService.executeFullRebuild();
        } else {
            log.info("Vocabulary update not needed. Conditions not met.");
        }
    }

}
