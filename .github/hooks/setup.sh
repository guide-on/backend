#!/bin/bash

ln -sf ../../.github/hooks/commit-msg .git/hooks/commit-msg
chmod +x .github/hooks/commit-msg
git config commit.template .github/COMMIT_TEMPLATE.txt

echo "✅ commit-msg hook & 템플릿 설정 완료!"