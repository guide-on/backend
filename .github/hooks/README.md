# Git Commit Hook 안내

이 프로젝트는 커밋 메시지 컨벤션을 자동으로 검사합니다.  
형식에 맞지 않으면 커밋이 거부됩니다.

## 커밋 메시지 형식

> <타입> : <제목>
> <br><br> 예시) feat: 로그인 기능 추가

- 제목은 50자 이내
- 마침표 금지
- 타입 예시: feat, fix, docs, style, refactor, test, chore

---

## 설정 방법 (최초 1회)

```bash
ln -s ../../.github/hooks/commit-msg .git/hooks/commit-msg
chmod +x .github/hooks/commit-msg
```
=> setup.sh 파일로 만들어둠, 아래의 코드를 터미널에서 실행

```터미널에서 실행
bash .github/hooks/setup.sh
```

---
## commit 메시지 작성시 참고
### 1. 에디터 설정
```터미널에서 실행
#  에디터를 nano로 변경

git config --global core.editor "nano"
```

### 2. 내용 작성
```터미널에서 실행
#  예시

feat: 회원가입 API 추가

기본 회원가입 기능 구현 및 유효성 검사 추가

Closes #14

```

### 3. 저장하고 종료
```
- 저장 : Ctrl + O
- 종료 : Ctrl + X
```