SET SESSION cte_max_recursion_depth = 100001;

-- 멤버 1000명 삽입 (테이블이 비어있을 때만)
INSERT INTO member (email, nickname, kakao_member_id, kakao_access_token, exercise_attendance_date, role, is_inactive, created_at, updated_at)
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 1000
)
SELECT
    CONCAT('user', n, '@test.com'),
    CONCAT('user', n),
    NULL,
    NULL,
    0,
    'USER',
    false,
    NOW(),
    NOW()
FROM nums
WHERE NOT EXISTS (SELECT 1 FROM member LIMIT 1);

-- 일기 50000개 삽입 (실제 존재하는 member_id 참조, 테이블이 비어있을 때만)
INSERT INTO diary (memo, member_id, created_at, updated_at)
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 50000
),
member_list AS (
    SELECT id, (ROW_NUMBER() OVER (ORDER BY id) - 1) AS rn FROM member
),
member_count AS (
    SELECT COUNT(*) AS cnt FROM member
)
SELECT
    CONCAT('테스트 일기 ', nums.n),
    member_list.id,
    NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
    NOW()
FROM nums
JOIN member_count ON TRUE
JOIN member_list ON member_list.rn = nums.n % member_count.cnt
WHERE NOT EXISTS (SELECT 1 FROM diary LIMIT 1);

-- =====================================================================
-- Worst-case: member_id 1024 일기 대량 삽입
-- =====================================================================

-- 2026-04-23 당일 10000건: 특정 날짜 조회 worst-case
INSERT INTO diary (memo, member_id, created_at, updated_at)
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 10000
)
SELECT
    CONCAT('부하테스트 당일 일기 ', nums.n),
    1024,
    TIMESTAMP('2026-04-23') + INTERVAL FLOOR(RAND() * 86400) SECOND,
    NOW()
FROM nums
WHERE NOT EXISTS (
    SELECT 1 FROM diary WHERE member_id = 1024 AND DATE(created_at) = '2026-04-23' LIMIT 1
);

-- 과거 1년치 40000건: member 1024 전체 row 수 증가로 풀스캔 부하 극대화
INSERT INTO diary (memo, member_id, created_at, updated_at)
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 40000
)
SELECT
    CONCAT('부하테스트 과거 일기 ', nums.n),
    1024,
    TIMESTAMP('2026-04-22') - INTERVAL FLOOR(RAND() * 364) DAY + INTERVAL FLOOR(RAND() * 86400) SECOND,
    NOW()
FROM nums
WHERE NOT EXISTS (
    SELECT 1 FROM diary WHERE member_id = 1024 AND DATE(created_at) < '2026-04-23' LIMIT 1
);