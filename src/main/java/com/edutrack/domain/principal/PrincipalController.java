package com.edutrack.domain.principal;

import com.edutrack.domain.principal.dto.PrincipalRegistrationRequest;
import com.edutrack.domain.principal.dto.PrincipalRegistrationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PrincipalController {

    private final PrincipalService principalService;

    @PostMapping("/academy/signup")
    public ResponseEntity<PrincipalRegistrationResponse> signup(
            @Valid @RequestBody PrincipalRegistrationRequest principalRegistrationRequest){

        PrincipalRegistrationResponse response = principalService.registerAcademy(principalRegistrationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
