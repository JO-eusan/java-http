## 톰캣 구현하기

### 1. HTTP 서버 구현하기

- [x] 인덱스 페이지(html + css)에 접근할 수 있다.
  - http://localhost:8080/index.html
- [x] 로그인 페이지(html)을 응답할 수 있다.
  - http://localhost:8080/login?account=gugu&password=password
- [x] 쿼리 파라미터의 정보로 회원을 조회할 수 있다.
  - 콘솔 로깅 확인

### 2. 로그인 구현하기

- [x] 로그인 성공 여부에 따라 다른 페이지로 리다이렉트(status code 302) 한다.
  - 로그인 성공 시, `/index.html`
  - 로그인 실패 시, `401.html`
- [x] 회원가입 페이지(html)을 응답할 수 있다.
  - http://localhost:8080/register
  - 회원가입 완료 시 `index.html`로 리다이렉트
- [x] HTTP Request Header의 Cookie에 `JSESSIONID`가 없으면 응답 헤어데 `Set-Cookie`를 반환할 수 있다.
  - 로그인 페이지에서 로그인 후 Cookies 확인
- [x] `JSESSIONID`를 이용하여 로그인 여부 체크
  - 로그인 성공 시 Session 객체의 값으로 `User` 객체 저장
  - 이미 로그인 된 상태라면 로그인 페이지 접근 시 `index.html`로 리다이렉트 처리
