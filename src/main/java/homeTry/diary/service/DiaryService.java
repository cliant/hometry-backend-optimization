package homeTry.diary.service;

import homeTry.common.cache.CacheType;
import homeTry.common.constants.DateTimeUtil;
import org.springframework.cache.annotation.Caching;
import homeTry.diary.dto.DiaryDto;
import homeTry.diary.dto.request.DiaryRequest;
import homeTry.diary.exception.badRequestException.DiaryNotFoundException;
import homeTry.diary.model.entity.Diary;
import homeTry.diary.repository.DiaryRepository;
import homeTry.member.service.MemberService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberService memberService;

    public DiaryService(DiaryRepository diaryRepository, MemberService memberService) {
        this.diaryRepository = diaryRepository;
        this.memberService = memberService;
    }

    @Caching(cacheable = {
            @Cacheable(
                    cacheNames = CacheType.Constants.DIARY_HISTORY,
                    key = "#memberId + '_' + #date + '_' + #lastId + '_' + #size",
                    condition = "#date.isBefore(T(homeTry.common.constants.DateTimeUtil).getAdjustedCurrentDate())"
            ),
            @Cacheable(
                    cacheNames = CacheType.Constants.DIARY,
                    key = "#memberId + '_' + #date + '_' + #lastId + '_' + #size",
                    condition = "!#date.isBefore(T(homeTry.common.constants.DateTimeUtil).getAdjustedCurrentDate())"
            )
    })
    @Transactional(readOnly = true)
    public Slice<DiaryDto> getDiaryByDate(LocalDate date, Long memberId, Long lastId, int size) {

        LocalDateTime startOfDay = DateTimeUtil.getStartOfDay(date);
        LocalDateTime endOfDay = DateTimeUtil.getEndOfDay(date);

        PageRequest pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id"));

        Slice<Diary> diaries;
        if (lastId == null) {
            diaries = diaryRepository.findByCreatedAtBetweenAndMember(
                    startOfDay, endOfDay, memberService.getMemberEntity(memberId), pageable);
        } else {
            diaries = diaryRepository.findByCreatedAtBetweenAndMemberAndIdGreaterThan(
                    startOfDay, endOfDay, memberService.getMemberEntity(memberId), lastId, pageable);
        }

        return diaries.map(DiaryDto::from);
    }


    @CacheEvict(cacheNames = CacheType.Constants.DIARY, allEntries = true)
    @Transactional
    public void createDiary(DiaryRequest diaryRequest, Long memberId) {

        diaryRepository.save(
                new Diary(diaryRequest.memo(),
                        memberService.getMemberEntity(memberId)));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheType.Constants.DIARY, allEntries = true),
            @CacheEvict(cacheNames = CacheType.Constants.DIARY_HISTORY, allEntries = true)
    })
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