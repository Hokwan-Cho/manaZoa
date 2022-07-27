package com.test.java.common;

import org.apache.commons.collections.map.ListOrderedMap;

/**
 * key 값을 camelCase 로 변경한 Map으로 변경한다.
 * ex) PASSWORD => password, MEMBER_ID => memberId, member_photo_file => memberPhotoFile
 *
 */
public class DataMap extends ListOrderedMap {

	private static final long serialVersionUID = 1L;

	@Override
	public Object put(Object key, Object value) {
		String tmp = (String) key;
		if (tmp.indexOf('_') < 0 && Character.isLowerCase(tmp.charAt(0))) {
			return super.put(tmp, value);
		}
		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;
		int len = tmp.length();

		for (int i = 0; i < len; i++) {
			char currentChar = tmp.charAt(i);
			if (currentChar == '_') {
				nextUpper = true;
			} else {
				if (nextUpper) {
					result.append(Character.toUpperCase(currentChar));
					nextUpper = false;
				} else {
					result.append(Character.toLowerCase(currentChar));
				}
			}
		}
		return super.put(result.toString(), value);
	}	
	
    public DataMap clone() {
        DataMap m = new DataMap();
        m.putAll(this.getMap());
        return m;
    }

	public String getString(Object key){
		String result = "";
		if(super.get(key) != null){
			result = super.get(key).toString();
		}
		return result;
	}

	public int getInt(Object key){
		String result = "0";
		if(super.get(key) != null){
			result = super.get(key).toString();
		}
		return Integer.parseInt(result);
	}

	public long getLong(Object key){		
		return Long.parseLong(this.getString(key));
	}
}

