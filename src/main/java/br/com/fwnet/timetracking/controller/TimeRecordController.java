package br.com.fwnet.timetracking.controller;

import br.com.fwnet.timetracking.dto.request.CreateTimeRecordRequest;
import br.com.fwnet.timetracking.dto.response.AdminTimeRecordResponse;
import br.com.fwnet.timetracking.dto.response.TimeRecordResponse;
import br.com.fwnet.timetracking.service.TimeRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/time-records")
@SecurityRequirement(name = "bearerAuth")
public class TimeRecordController {

    private final TimeRecordService timeRecordService;

    public TimeRecordController(TimeRecordService timeRecordService) {
        this.timeRecordService = timeRecordService;
    }

    @PostMapping
    public ResponseEntity<TimeRecordResponse> create(
            Principal principal,
            @Valid @RequestBody CreateTimeRecordRequest request
    ) {
        TimeRecordResponse response =
                timeRecordService.create(
                        principal.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TimeRecordResponse>> getHistory(
            Principal principal
    ) {
        List<TimeRecordResponse> response =
                timeRecordService.getHistory(
                        principal.getName()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<AdminTimeRecordResponse>> getAdminHistory() {
        List<AdminTimeRecordResponse> response =
                timeRecordService.getAdminHistory();

        return ResponseEntity.ok(response);
    }
}