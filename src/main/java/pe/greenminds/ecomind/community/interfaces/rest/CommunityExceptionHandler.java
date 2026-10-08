package pe.greenminds.ecomind.community.interfaces.rest;

import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
@RestControllerAdvice(basePackageClasses=CommunityController.class) public class CommunityExceptionHandler{
 @ExceptionHandler(IllegalArgumentException.class)public ResponseEntity<ErrorResource> invalid(IllegalArgumentException e){return ResponseEntity.badRequest().body(new ErrorResource("COMMUNITY_REQUEST_INVALID",e.getMessage(),null));}
 @ExceptionHandler(IllegalStateException.class)public ResponseEntity<ErrorResource> conflict(IllegalStateException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResource("COMMUNITY_STATE_CONFLICT",e.getMessage(),null));}
 @ExceptionHandler(SecurityException.class)public ResponseEntity<ErrorResource> forbidden(SecurityException e){return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResource("COMMUNITY_ACCESS_FORBIDDEN",e.getMessage(),null));}
}
