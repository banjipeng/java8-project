package org.pingan.payment.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.pingan.payment.entity.GlobalCodeEnum;
import org.pingan.payment.entity.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 统一业务异常处理方法
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseResult<?> processServiceException(HttpServletResponse response, ServiceException e) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        ResponseResult<Object> result = new ResponseResult<>();
        result.setCode(e.getCode());
        result.setMessage(e.getMessage());
        log.error(e.toString() + "_" + e.getMessage(), e);
        return result;
    }

    /**
     * 统一处理参数校验错误异常1
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseResult<?> processValidException(HttpServletResponse response, MethodArgumentNotValidException e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        List<String> errorStringList = e.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        String errorMessage = String.join("; ", errorStringList);
        response.setContentType("application/json;charset=UTF-8");
        log.error(e.toString() + "_" + e.getMessage(), e);
        return ResponseResult.systemException(errorMessage, GlobalCodeEnum.GL_FAIL_9998);
    }

    /**
     * 统一处理参数校验错误异常2
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseResult<?> processValidException(HttpServletResponse response, HttpRequestMethodNotSupportedException e){
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        String[] supportedMethods = e.getSupportedMethods();
        String errorMessage = "此接口不支持" + e.getMethod();
        if (!ArrayUtils.isEmpty(supportedMethods)){
            errorMessage += " (仅支持" + String.join(",", supportedMethods) + ")";
        }
        response.setContentType("application/json;charset=UTF-8");
        log.error(e.toString() + "_" + e.getMessage(), e);
        return ResponseResult.systemException(errorMessage, GlobalCodeEnum.GL_FAIL_9996);
    }


    /**
     * 统一处理参数校验错误异常3
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ResponseResult<?> processValidException(HttpServletResponse response, BindException e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        List<String> errorStringList = e.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        String errorMessage = String.join("; ", errorStringList);
        response.setContentType("application/json;charset=UTF-8");
        log.error(e.toString() + "_" + e.getMessage(), e);
        return ResponseResult.systemException(errorMessage, GlobalCodeEnum.GL_FAIL_9998);
    }

    /**
     * 未知系统处理异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult<?> processDefaultException(HttpServletResponse response, Exception e){
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json;charset=UTF-8");
        log.error(e.toString() + "_" + e.getMessage(), e);
        return ResponseResult.systemException();
    }
}
