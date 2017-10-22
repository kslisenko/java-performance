package com.backend.api;

import com.backend.dto.AddMessageResponse;
import com.backend.dto.MessageDTO;
import com.backend.service.ChatDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class HTTPWebService {

    private static final Logger log = LoggerFactory.getLogger(HTTPWebService.class);

    @Autowired
    private ChatDAO dao;

    @ResponseBody
    @RequestMapping(value = "/message", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public AddMessageResponse add(@RequestBody MessageDTO message, HttpServletRequest request) {
        log("/add", request);

        boolean result = dao.add(message.getMessage());
        return new AddMessageResponse(result);
    }

    @ResponseBody
    @RequestMapping(value = "/message", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MessageDTO> getAll(HttpServletRequest request) {
        log("/get", request);
        return dao.getAll();
    }

    private void log(String methodName, HttpServletRequest request) {
        log.info("{} Dynatrace [{}], Zipkin [{}]", methodName,
                getHeaders("x-dynatrace", request), getHeaders("x-b3", request));
    }

    private String getHeaders(String prefix, HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .filter((headerName) -> headerName.startsWith(prefix))
                .map((headerName) -> headerName + "=" + request.getHeader(headerName))
                .reduce((header1, header2) -> header1 + ", " + header2).orElse("");
    }
}