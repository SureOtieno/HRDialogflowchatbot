package com.example.dialogflow.utils;


import com.example.dialogflow.utils.constants.Constants;
import com.example.dialogflow.dto.Pagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GenericUtils {

    public static ResponseEntity<Object> noRecordsFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(com.example.tendawaks.utils.GenericResponse.GenericResponseData.builder()
                        .status(com.example.tendawaks.utils.ResponseStatus.FAILED.getStatus())
                        .data(new ArrayList<>())
                        .message(Constants.NO_RECORD_FOUND)
                        .msgDeveloper(Constants.NO_RECORD_FOUND)
                        .build());
    }

    public static Map<String, Object> getRecordsResponse(Map<String, String> getParams, Pagination pagination, List<?> ls)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(Constants.PAGE, pagination.getPage());
        map.put(Constants.PAGESIZE, pagination.getPageSize());
        map.put(Constants.TOTALPAGES, pagination.getTotalPages());
        map.put(Constants.TOTALCOUNT, pagination.getTotalCount());
        map.put("data", ls);

        Map responsesLs = new HashMap<>();
        responsesLs.put("data", ls);

        return getParams.containsKey(Constants.PAGE) ? map : responsesLs;
    }
}
