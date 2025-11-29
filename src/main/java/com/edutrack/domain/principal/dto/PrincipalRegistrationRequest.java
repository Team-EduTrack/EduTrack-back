package com.edutrack.domain.principal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrincipalRegistrationRequest {

    @NotBlank(message = "원장 이름은 필수입니다.")
    @Pattern(regexp = "^[가-힣]+$", message = "이름은 한글로 공백 없이 입력해주세요.")
    private String principalName;

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 5, max = 15, message = "아이디는 5~15자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문/숫자만 가능합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$",
            message = "비밀번호는 최소 8자리, 숫자, 대소문자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordConfirm;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010[0-9]{8}$", message = "전화번호는 - 없이 숫자만 입력해주세요.")
    private String phone;


    @Email(message = "유효하지 않은 이메일 형식입니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "학원 이름은 필수입니다.")
    private String academyName;
}
