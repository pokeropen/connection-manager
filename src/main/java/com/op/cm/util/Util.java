package com.op.cm.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.op.cm.api.ICommunicationService;
import com.op.cm.services.CommunicationService;

/**
 * Created by jalagari on 14/01/20.
 */
public class Util {

    private static ICommunicationService communicationService = new CommunicationService();
    private static ObjectMapper objectMapper = new ObjectMapper();
    {

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static ICommunicationService getCommunicationService() {
        return communicationService;
    }
}
