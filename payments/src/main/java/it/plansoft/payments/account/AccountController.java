package it.plansoft.payments.account;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts/{userId}/")
public class AccountController {


    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/charge")
    public ResponseEntity addCharge(@PathVariable String userId, @RequestBody BigDecimal payment) {

        if ( BigDecimal.ZERO.compareTo(payment) >= 0) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }

        int status = accountService.addCharge(userId, payment.abs().multiply(new BigDecimal("-1.00")),
                "api", UUID.randomUUID().toString(), "api");

        return getResponseEntity(status);
    }


    @PostMapping("/deposit")
    public ResponseEntity addDeposit(@PathVariable String userId, @RequestBody BigDecimal deposit) {

        if ( BigDecimal.ZERO.compareTo(deposit) >= 0) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }

        String refId = UUID.randomUUID().toString();
        int status = accountService.addDeposit(userId, deposit,
                "api", refId, "api");

        return getResponseEntity(status);
    }

    @GetMapping("/balance")
    public ResponseEntity<Account> balance(@PathVariable String userId) {
        Optional<Account> byUserId = accountService.getByUserId(userId);
        if ( ! byUserId.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(byUserId.get(), HttpStatus.OK);
        }
    }

    private ResponseEntity getResponseEntity(int status) {
        if (status >= 0) {
            return new ResponseEntity(HttpStatus.CREATED);
        } else if (status == -1) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        } else {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
