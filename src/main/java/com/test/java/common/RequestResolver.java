package com.test.java.common;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;


/**
 * String controller 사용, pageIndex(defalut=1), itemCountePerPage(default=10) 설정
 * 로그인 사용자인경우 registId,updateId 자동으로 추가함
 * @see com.softone.web.TwsMapArgumentResolver
 *
 */
public class RequestResolver {

	DataMap map = new DataMap();

    public Object get(String key){
    	if(this.containsKey(key)){
    		return map.get(key);
    	}else{
    		return null;
    	}

    }

    public void put(String key, Object value){
        map.put(key, value);
    }

    public Object remove(String key){
        return map.remove(key);
    }

    public boolean containsKey(String key){
        return map.containsKey(key);
    }

    public boolean containsValue(Object value){
        return map.containsValue(value);
    }

    public void clear(){
        map.clear();
    }

    public Set<Entry<String, Object>> entrySet(){
        return map.entrySet();
    }

    public Set<String> keySet(){
        return map.keySet();
    }

    public boolean isEmpty(){
        return map.isEmpty();
    }

    public void putAll(DataMap m){
        map.putAll(m);
    }

    public DataMap getMap(){
        return map;
    }

    public String getString(Object key){    	
    	
    	if(map.get(key) == null || "".equals(map.get(key))) {
    		return "";
    	}else {
    		return map.getString(key);
    	}
	}

	public int getInt(Object key){
		String result = this.getString(key);
		return Integer.parseInt(result);
	}

	public int getInt(Object key, int defaultValue){
		String result = this.getString(key);
		if(result.equals(""))
			result = "0";
		return Integer.parseInt(result);
	}

	public long getLong(Object key){
		String result = this.getString(key);
		return Long.parseLong(result);
	}

	public long getLong(Object key, long defaultValue){
		String result = this.getString(key);
		if(result.equals(""))
			result = "0";
		return Long.parseLong(result);
	}

	public void putTotalCount(List<DataMap> list, String attributeName){
		long totalCount = 0l;
		if(list != null && list.size() > 0 ){
			totalCount = ((DataMap)list.get(0)).getLong("totalCount");
		}
		this.map.put(attributeName, totalCount);
	}

	public void putTotalCount(List<DataMap> list){
		this.putTotalCount(list, "totalCount");
	}


	public void putPageCount(List<DataMap> list, int itemCountPerPage){
		long totalCount = 0l;
		if(list != null && list.size() > 0 ){
			totalCount = ((DataMap)list.get(0)).getLong("totalCount");
		}
		int pageCount = (int) (Math.ceil((double) totalCount / (double) itemCountPerPage));
		this.map.put("pageCount", pageCount);
	}
	
}
