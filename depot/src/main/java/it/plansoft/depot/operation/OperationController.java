package it.plansoft.depot.operation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operations")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @PostMapping("/place")
    public ResponseEntity place(@RequestBody List<OperationDetailValue> details) {

        String uuid = UUID.randomUUID().toString();
        int status = operationService.place(new OperationValue(
                "api", "api@" + uuid, uuid,
                details
        ));

        return getResponseEntity(status);
    }

    private ResponseEntity getResponseEntity(int status) {
        if (status > 0) {
            return new ResponseEntity(HttpStatus.CREATED);
        } else if (status == 0) {
            return new ResponseEntity(HttpStatus.ALREADY_REPORTED);
        } else if (status == -50 ) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
    }
}

