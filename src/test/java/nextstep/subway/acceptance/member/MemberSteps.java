package nextstep.subway.acceptance.member;

import static org.assertj.core.api.Assertions.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.auth.token.TokenRequest;
import nextstep.member.application.dto.MemberRequest;

public class MemberSteps {
    public static final String USERNAME_FIELD = "username";
    public static final String PASSWORD_FIELD = "password";

    public static String 회원_생성_하고_로그인_됨(String email, String password, int age) {
        회원_생성됨(회원_생성_요청(email, password, age));
        return 로그인_되어_있음(email, password);
    }

    public static String 로그인_되어_있음(String email, String password) {
        ExtractableResponse<Response> response = 로그인_요청(email, password);
        return response.jsonPath().getString("accessToken");
    }

    public static ExtractableResponse<Response> 로그인_요청(String email, String password) {
        TokenRequest body = new TokenRequest(email, password);

        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .when().post("/login/token")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract();
    }

    public static ExtractableResponse<Response> 회원_생성_요청(String email, String password, int age) {
        MemberRequest body = createMemberRequest(email, password, age);
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .when().post("/members")
                .then().log().all().extract();
    }

    private static MemberRequest createMemberRequest(String email, String password, int age) {
        return MemberRequest.builder()
            .email(email)
            .password(password)
            .age(age)
            .build();
    }

    public static void 회원_생성됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    public static ExtractableResponse<Response> 회원_정보_조회_요청(ExtractableResponse<Response> response) {
        String uri = response.header("Location");

        return RestAssured.given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get(uri)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_정보_수정_됨(ExtractableResponse<Response> response, String email, String password, Integer age) {
        String uri = response.header("Location");

        MemberRequest body = createMemberRequest(email, password, age);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .when().put(uri)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 회원_삭제_됨(ExtractableResponse<Response> response) {
        String uri = response.header("Location");
        return RestAssured
                .given().log().all()
                .when().delete(uri)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .extract();
    }


    public static ExtractableResponse<Response> 내_회원_정보_조회_요청(String email, String password) {
        return RestAssured
                .given().log().all()
                .auth().form(email, password, new FormAuthConfig("/login/session", USERNAME_FIELD, PASSWORD_FIELD))
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/members/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract();
    }

    public static ExtractableResponse<Response> 내_회원_정보_조회_요청(String accessToken) {
        return RestAssured.given().log().all()
                          .auth().oauth2(accessToken)
                          .accept(MediaType.APPLICATION_JSON_VALUE)
                          .when().get("/members/me")
                          .then().log().all()
                          .statusCode(HttpStatus.OK.value())
                          .extract();
    }

    public static void 회원_정보_조회됨(ExtractableResponse<Response> response, String email, int age) {
        assertThat(response.jsonPath().getString("id")).isNotNull();
        assertThat(response.jsonPath().getString("email")).isEqualTo(email);
        assertThat(response.jsonPath().getInt("age")).isEqualTo(age);
    }

    public static ExtractableResponse<Response> 내_회원_정보_수정_됨(String accessToken, String email, String password, Integer age) {
        MemberRequest body = createMemberRequest(email, password, age);

        return RestAssured.given().log().all()
                          .auth().oauth2(accessToken)
                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                          .body(body)
                          .when().put("/members/me")
                          .then().log().all()
                          .statusCode(HttpStatus.OK.value())
                          .extract();
    }

    public static ExtractableResponse<Response> 회원_탈퇴_됨(String accessToken) {
        return RestAssured.given().log().all()
                          .auth().oauth2(accessToken)
                          .when().delete("/members/me")
                          .then().log().all()
                          .statusCode(HttpStatus.NO_CONTENT.value())
                          .extract();
    }
}
