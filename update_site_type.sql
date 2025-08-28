-- 기존 유저들의 site_type을 CORE로 업데이트
UPDATE users 
SET site_type = 'CORE' 
WHERE site_type IS NULL;

-- 업데이트된 레코드 수 확인
SELECT COUNT(*) as updated_users 
FROM users 
WHERE site_type = 'CORE';
