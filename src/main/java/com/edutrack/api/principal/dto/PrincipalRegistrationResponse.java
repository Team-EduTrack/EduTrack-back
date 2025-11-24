package com.edutrack.api.principal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrincipalRegistrationResponse {

    private final Long id;
    private final String academyName;
    private final String academyCode;

}
