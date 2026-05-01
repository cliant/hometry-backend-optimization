package homeTry.mainPage.service;

import homeTry.common.constants.DateTimeUtil;
import homeTry.diary.service.DiaryService;
import homeTry.exerciseList.service.ExerciseHistoryService;
import homeTry.exerciseList.service.ExerciseTimeService;
import homeTry.mainPage.dto.response.MainPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class MainPageService {

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

    @Transactional(readOnly = true)
    public MainPageResponse getMainPage(LocalDate date, Long memberId, Long lastId, int size) {
        LocalDate adjustedToday = DateTimeUtil.getAdjustedCurrentDate();
        if (adjustedToday.isEqual(date)) {
            return getTodayMainPageResponse(memberId, date, lastId, size);
        }
        return getHistoricalMainPageResponse(memberId, date, lastId, size);
    }

    private MainPageResponse getTodayMainPageResponse(Long memberId, LocalDate date, Long lastId, int size) {
        var exerciseTimes = exerciseTimeService.getExerciseTimesForToday(memberId);
        var exerciseResponses = exerciseTimeService.getExerciseResponsesForToday(memberId);
        var diaries = diaryService.getDiaryByDate(date, memberId, lastId, size);
        return new MainPageResponse(exerciseTimes, exerciseResponses, diaries);
    }

    private MainPageResponse getHistoricalMainPageResponse(Long memberId, LocalDate date, Long lastId, int size) {
        var exerciseHistories = exerciseHistoryService.getExerciseHistoriesForDay(memberId, date);
        var exerciseResponses = exerciseHistoryService.getExerciseResponsesForDay(memberId, date);
        var diaries = diaryService.getDiaryByDate(date, memberId, lastId, size);
        return new MainPageResponse(exerciseHistories, exerciseResponses, diaries);
    }
}