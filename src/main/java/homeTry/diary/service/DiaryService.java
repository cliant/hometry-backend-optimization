package homeTry.diary.service;

import homeTry.common.constants.DateTimeUtil;
import homeTry.diary.dto.DiaryDto;
import homeTry.diary.dto.request.DiaryRequest;
import homeTry.diary.exception.badRequestException.DiaryNotFoundException;
import homeTry.diary.model.entity.Diary;
import homeTry.diary.repository.DiaryRepository;
import homeTry.member.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DiaryService {

    private static final Logger log = LoggerFactory.getLogger(DiaryService.class);

    private final DiaryRepository diaryRepository;
    private final MemberService memberService;

    public DiaryService(DiaryRepository diaryRepository, MemberService memberService) {
        this.diaryRepository = diaryRepository;
        this.memberService = memberService;
    }

    @Transactional(readOnly = true)
    public Slice<DiaryDto> getDiaryByDate(LocalDate date, Long memberId, Pageable pageable) {

        LocalDateTime startOfDay = DateTimeUtil.getStartOfDay(date);
        LocalDateTime endOfDay = DateTimeUtil.getEndOfDay(date);

        long dbStart = System.currentTimeMillis();
        Slice<Diary> diaries = diaryRepository.findByCreatedAtBetweenAndMember(
                startOfDay, endOfDay, memberService.getMemberEntity(memberId), pageable);
        log.info("[PERF][DB] findByCreatedAtBetweenAndMember rows={} time={}ms",
                diaries.getNumberOfElements(), System.currentTimeMillis() - dbStart);

        long mapStart = System.currentTimeMillis();
        Slice<DiaryDto> result = diaries.map(DiaryDto::from);
        log.info("[PERF][Serialization] DiaryDto mapping rows={} time={}ms",
                result.getNumberOfElements(), System.currentTimeMillis() - mapStart);

        return result;
    }


    @Transactional
    public void createDiary(DiaryRequest diaryRequest, Long memberId) {

        diaryRepository.save(
                new Diary(diaryRequest.memo(),
                        memberService.getMemberEntity(memberId)));
    }

    @Transactional
    public void deleteDiary(Long diaryId) {

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException());

        diaryRepository.delete(diary);

    }

    @Transactional
    public void deleteByMember(Long memberId) {
        diaryRepository.deleteByMember(memberService.getMemberEntity(memberId));
    }
}
