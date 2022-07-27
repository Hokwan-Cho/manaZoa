package com.test.java.util;

import java.util.List;

import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

public class JsonConverter {

	/**
	 * json 문자열을 object 로 변환한다
	 * @param json
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static <T> T getObject(String json, Class<T> type) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        T result = objectMapper.readValue(json, TypeFactory.defaultInstance().constructType(type));
        
        return result;
	}
	
	/**
	 * json 문자열을 object List 로 변환한다
	 * @param json
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> getObjectList(String json, Class<T> type) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<T> result = objectMapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, type));        
        return result;
	}
	
}
