
INSERT INTO member (
    email,
    nickname,
    kakao_member_id,
    kakao_access_token,
    exercise_attendance_date,
    role,
    is_inactive,
    created_at,
    updated_at
)
SELECT
    CONCAT('user', t.n, '@test.com'),
    CONCAT('user', t.n),
    NULL,
    NULL,
    0,
    'USER',
    false,
    NOW(),
    NOW()
FROM (
    SELECT @rownum := @rownum + 1 AS n
    FROM information_schema.tables, (SELECT @rownum := 0) r
    LIMIT 1000
) t;

INSERT INTO diary (
    memo,
    member_id,
    created_at,
    updated_at
)
SELECT
    CONCAT('테스트 일기 ', t.n),
    FLOOR(1 + (RAND() * 1000)),
    NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
    NOW()
FROM (
    SELECT @rownum := @rownum + 1 AS n
    FROM information_schema.tables a,
         information_schema.tables b,
         (SELECT @rownum := 0) r
    LIMIT 50000
) t;