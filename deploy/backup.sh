#!/bin/bash
# MySQL 컨테이너의 backend_service DB를 backups/ 폴더에 .sql 파일로 뽑는다.
# 서버에서: cd /opt/backend-service && ./backup.sh
set -eu
cd "$(dirname "$0")"
mkdir -p backups
FILE="backups/backend_service-$(date +%Y%m%d-%H%M%S).sql"
# -T: 터미널 없이 실행. 비밀번호는 컨테이너 안의 환경 변수를 그대로 쓴다.
docker compose exec -T mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction backend_service' > "$FILE"
# 30일 지난 백업은 지운다.
find backups -name '*.sql' -mtime +30 -delete
echo "백업 완료: $FILE ($(du -h "$FILE" | cut -f1))"