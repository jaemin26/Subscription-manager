package com.subscription.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 * 
 * 전역 예외 처리 핸들러
 * 
 * 수학적 구조:
 * - 예외 발생 → GlobalExceptionHandler
 *          → HTTP 상태 코드 변환
 *          → 에러 응답 반환
 * 
 * 호출 경로: Controller에서 예외 발생
     *          → @ExceptionHandler 메서드 호출
     *          → ResponseEntity<ErrorResponse> 반환
     * 
 * 규칙:
 * - @RestControllerAdvice로 모든 Controller의 예외 처리
 * - 각 예외 타입별로 적절한 HTTP 상태 코드 반환
 * - 에러 메시지를 JSON 형식으로 반환
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * IllegalArgumentException 처리
     * 
     * 수학적 구조:
     * - 입력: IllegalArgumentException (예: 리소스를 찾을 수 없음)
     * - 출력: ResponseEntity<Map<String, String>> (에러 메시지)
     * - HTTP 상태 코드: 404 Not Found 또는 400 Bad Request
     * 
     * 호출 경로: Service에서 IllegalArgumentException 발생
     *          → GlobalExceptionHandler.handleIllegalArgumentException()
     *          → ResponseEntity.badRequest() 반환
     * 
     * @param ex IllegalArgumentException
     * @return ResponseEntity<Map<String, String>> 에러 응답 (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * MethodArgumentNotValidException 처리 (@Valid 검증 실패)
     * 
     * 수학적 구조:
     * - 입력: MethodArgumentNotValidException (검증 실패)
     * - 출력: ResponseEntity<Map<String, Object>> (필드별 에러 메시지)
     * - HTTP 상태 코드: 400 Bad Request
     * 
     * 호출 경로: @Valid 검증 실패
     *          → GlobalExceptionHandler.handleValidationExceptions()
     *          → ResponseEntity.badRequest() 반환
     * 
     * @param ex MethodArgumentNotValidException
     * @return ResponseEntity<Map<String, Object>> 검증 에러 응답 (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", "입력값 검증에 실패했습니다.");
        errorResponse.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 일반 예외 처리 (기타 예외)
     * 
     * 수학적 구조:
     * - 입력: Exception (예상치 못한 예외)
     * - 출력: ResponseEntity<Map<String, String>> (에러 메시지)
     * - HTTP 상태 코드: 500 Internal Server Error
     * 
     * 호출 경로: 예상치 못한 예외 발생
     *          → GlobalExceptionHandler.handleGenericException()
     *          → ResponseEntity.internalServerError() 반환
     * 
     * @param ex Exception
     * @return ResponseEntity<Map<String, String>> 에러 응답 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "서버에서 오류가 발생했습니다.");
        // 프로덕션 환경에서는 상세 에러 메시지를 숨기는 것이 좋습니다
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

