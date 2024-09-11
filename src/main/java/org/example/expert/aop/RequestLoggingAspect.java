package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
public class RequestLoggingAspect {

    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    @Pointcut("@annotation(org.example.expert.annotation.ApiRequestLogging)")
    private void requestLoggingAnnotation() {
    }

    @Around("requestLoggingAnnotation()")
    public Object adviceAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = requestAttributes.getRequest();

        long userId = (long) request.getAttribute("userId"); // API 요청 유저 ID
        long startTime = System.currentTimeMillis(); // API 요청 시간
        String requestURI = request.getRequestURI();

        try {
            return joinPoint.proceed(); // 메서드 실행
        } finally {
            log.info("요청 사용자의 ID : {}", userId);
            log.info("API 요청 시간 : {}", startTime);
            log.info("API 요청 URL : {}", requestURI);
        }


    }

}