# 커밋 메시지 규약

[AngularJS 커밋 규약](https://docs.google.com/document/d/1QrDFcIiPjSLDn3EL15IJygNPiHORgU1_OOAqWjiDU5Y/)을 따른다.
`commit-convention.md`(영어 원문)를 한국어로 옮긴 문서다. 규칙은 원문 그대로이고, 예시만 이 프로젝트에 맞게 바꿨다.

## 왜 규약을 지키나

- 변경 내역(CHANGELOG)을 스크립트로 뽑을 수 있다.
- 포맷 정리처럼 로직이 안 바뀐 커밋을 버그 추적(`git bisect`)에서 건너뛸 수 있다.
- 기록을 볼 때 **무엇을, 어디서** 바꿨는지 첫 줄만 보고 알 수 있다.

## 형식

```text
<type>(<scope>): <subject>
<빈 줄>
<body>
<빈 줄>
<footer>
```

- 어느 줄이든 **100자를 넘지 않는다.** GitHub과 여러 Git 도구에서 읽기 쉽게 하기 위해서다.
- 첫 줄만 필수다. body와 footer는 필요할 때만 쓴다.

## 첫 줄

변경을 짧게 요약한다.

### type — 무슨 종류의 변경인가

| type | 뜻 |
| --- | --- |
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `docs` | 문서 |
| `style` | 포맷, 공백, 세미콜론 등. 로직 변경 없음 |
| `refactor` | 동작은 그대로 두고 구조만 바꿈 |
| `test` | 빠진 테스트 추가 |
| `chore` | 빌드, 설정 같은 관리 작업 |

### scope — 어디를 바꿨나

변경한 위치를 나타내는 말이면 무엇이든 된다.

이 프로젝트에서는 도메인 이름을 쓰면 된다. 예: `member`, `product`, `order`, `config`, `git`

### subject — 무엇을 했나

- **명령형 현재 시제**로 쓴다. `changed`나 `changes`가 아니라 `change`
- 첫 글자를 **대문자로 쓰지 않는다**
- 끝에 **마침표(.)를 찍지 않는다**

## body — 왜 바꿨나

- subject처럼 명령형 현재 시제로 쓴다.
- **바꾼 이유**와 **이전과 무엇이 달라졌는지**를 적는다.

참고: [365git — writing git commit messages](http://365git.tumblr.com/post/3308646748/writing-git-commit-messages),
[tbaggery — a note about git commit messages](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html)

## footer

### 호환이 깨지는 변경

기존 사용법이 더 이상 동작하지 않게 되는 변경은 footer에 반드시 적는다.
**무엇이 바뀌었는지, 왜 바꿨는지, 어떻게 옮겨야 하는지**를 함께 쓴다.

```text
BREAKING CHANGE: <무엇이 바뀌었는지>

<어떻게 옮기면 되는지>
```

### 이슈 연결

해결한 이슈는 footer에 `Closes`로 적는다.

```text
Closes #234
```

여러 개면 쉼표로 잇는다.

```text
Closes #123, #245, #992
```

## 예시

이 프로젝트의 실제 커밋과 앞으로 쓸 법한 커밋이다.

```text
chore(project): initialize Spring Boot project
```

```text
docs(git): add commit message convention
```

```text
chore(config): configure local MySQL datasource
```

```text
feat(member): add Member entity
```

body가 필요한 경우:

```text
fix(order): restore stock when order line is cancelled

Cancelling an order line only changed its status and left the stock as is.
Restore the option stock in the same transaction.
```

로직이 안 바뀐 정리는 `style`로 분리한다:

```text
style(member): fix indentation
```

## 원문에서 뺀 것

- CHANGELOG를 뽑는 `git log` 명령과 `git bisect` 명령의 예시
- AngularJS 저장소의 실제 커밋 목록과 `$browser`, `$compile` 같은 AngularJS 전용 예시
- 긴 `BREAKING CHANGE` 코드 예시

규칙 자체는 하나도 빼거나 바꾸지 않았다.