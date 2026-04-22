package dolpi.moxsend_engine.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourcesNotFound.class)
    public ResponseEntity<?> handle(ResourcesNotFound ex){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlegeneric(Exception ex){
        return new ResponseEntity("Something went wrong: "+ ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
