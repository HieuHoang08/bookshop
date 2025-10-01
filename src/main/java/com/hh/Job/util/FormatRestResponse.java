package com.hh.Job.util;


import com.hh.Job.domain.response.RestResponse;
import com.hh.Job.util.annotation.APImessage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

            HttpServletResponse httpServletResponse = ((ServletServerHttpResponse) response).getServletResponse();
            int statusCode = httpServletResponse.getStatus();
            // Bạn có thể xử lý statusCode ở đây nếu cần

        RestResponse<Object> restResponse = new RestResponse<Object>();
        restResponse.setStatusCode(statusCode);

        if(body instanceof String|| body instanceof Resource ){
            return body;
        }

        if(statusCode >= 400){
            return body;
        }else{
            restResponse.setData(body);
            APImessage message = returnType.getMethodAnnotation(APImessage.class);
            restResponse.setMessage(message != null ? message.value() :  "success");
        }
        return restResponse;
    }
}
