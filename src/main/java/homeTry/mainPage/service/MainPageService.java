package homeTry.mainPage.service;

import homeTry.common.constants.DateTimeUtil;
import homeTry.diary.service.DiaryService;
import homeTry.exerciseList.service.ExerciseHistoryService;
import homeTry.exerciseList.service.ExerciseTimeService;
import homeTry.mainPage.dto.response.MainPageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class MainPageService {

    private static final Logger log = LoggerFactory.getLogger(MainPageService.class);

    private final DiaryService diaryService;
    private final ExerciseTimeService exerciseTimeService;
    private final ExerciseHistoryService exerciseHistoryService;

    public MainPageService(DiaryService diaryService,
            ExerciseTimeService exerciseTimeService,
            ExerciseHistoryService exerciseHistoryService) {
        this.diaryService = diaryService;
        this.exerciseTimeService = exerciseTimeService;
        this.exerciseHistoryService = exerciseHistoryService;
    }

    private static final long SLOW_THRESHOLD_MS = 500;

    @Transactional(readOnly = true)
    public MainPageResponse getMainPage(LocalDate date, Long memberId, Pageable pageable) {
        long start = System.currentTimeMillis();
        LocalDate adjustedToday = DateTimeUtil.getAdjustedCurrentDate();

        MainPageResponse response;
        long s1, s2, s3, t1, t2, t3;
        String step1Name, step2Name;

        if (adjustedToday.isEqual(date)) {
            s1 = System.currentTimeMillis();
            var exerciseTimes = exerciseTimeService.getExerciseTimesForToday(memberId);
            t1 = System.currentTimeMillis() - s1;

            s2 = System.currentTimeMillis();
            var exerciseResponses = exerciseTimeService.getExerciseResponsesForToday(memberId);
            t2 = System.currentTimeMillis() - s2;

            s3 = System.currentTimeMillis();
            var diaries = diaryService.getDiaryByDate(date, memberId, pageable);
            t3 = System.currentTimeMillis() - s3;

            step1Name = "getExerciseTimesForToday";
            step2Name = "getExerciseResponsesForToday";
            response = new MainPageResponse(exerciseTimes, exerciseResponses, diaries);
        } else {
            s1 = System.currentTimeMillis();
            var exerciseHistories = exerciseHistoryService.getExerciseHistoriesForDay(memberId, date);
            t1 = System.currentTimeMillis() - s1;

            s2 = System.currentTimeMillis();
            var exerciseResponses = exerciseHistoryService.getExerciseResponsesForDay(memberId, date);
            t2 = System.currentTimeMillis() - s2;

            s3 = System.currentTimeMillis();
            var diaries = diaryService.getDiaryByDate(date, memberId, pageable);
            t3 = System.currentTimeMillis() - s3;

            step1Name = "getExerciseHistoriesForDay";
            step2Name = "getExerciseResponsesForDay";
            response = new MainPageResponse(exerciseHistories, exerciseResponses, diaries);
        }

        long total = System.currentTimeMillis() - start;
        if (total >= SLOW_THRESHOLD_MS) {
            log.warn("[PERF][Service] SLOW getMainPage total={}ms | {}={}ms, {}={}ms, getDiaryByDate={}ms",
                    total, step1Name, t1, step2Name, t2, t3);
        }
        return response;
    }
}
