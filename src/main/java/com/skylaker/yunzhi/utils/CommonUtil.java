package com.skylaker.yunzhi.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class CommonUtil {
	
	private static final String INTEGER_REGX = "^[+-]?[0-9]+$";//整数
	private static final String DECIMAL_REGX = "[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+";//小数
	
	public static boolean isEmpty(String s) {
		return null == s || s.isEmpty();
	}
	
	public static boolean isNotEmpty(String s) {
		return null != s && !s.isEmpty();
	}
	
	public static boolean isEmpty(Collection<?> c) {
		return null == c || c.isEmpty();
	}
	
	public static boolean isNotEmpty(Collection<?> c) {
		return null != c && !c.isEmpty();
	}
	
	public static boolean isEmpty(Map<?, ?> c) {
		return null == c || c.isEmpty();
	}
	
	public static boolean isNotEmpty(Map<?, ?> c) {
		return null != c && !c.isEmpty();
	}

	
	/**
	 * 在一个递增的list里找出第一个大于key值的元素下标 采用二分搜索算法
	 * 
	 * @param list
	 * @param key
	 * @return
	 */
	public static <T extends Comparable<T>> int getFirstGtKeyIndex(List<T> list, T key) {
		if(isEmpty(list)) {
			return 0;
		}
		int low = 0;
		int high = list.size() - 1;
		int mid = 0;
		while(low <= high) {
			mid = (low + high) / 2;
			T cur = list.get(mid);
			if(key.compareTo(cur) < 0) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		
		return low;
	}
	
	/**
	 * 在一个递减的list里找出第一个小于key值的元素下标 采用二分搜索算法
	 * 
	 * @param list
	 * @param key
	 * @return
	 */
	public static <T extends Comparable<T>> int getFirstLtKeyIndex(List<T> list, T key) {
		if(isEmpty(list)) {
			return 0;
		}
		int low = 0;
		int high = list.size() - 1;
		int mid = 0;
		while(low <= high) {
			mid = (low + high) / 2;
			T cur = list.get(mid);
			if(key.compareTo(cur) > 0) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		
		return low;
	}
	

	/**
	 * 根据效果集合，计算出一个属性值
	 * 
	 * @param attrType

	 * @return
	 */
	public static float calcEffectBonus(int attrType, Map<Integer, Float> effects) {

		// 整数值
		Float abl = effects.get(attrType + 1);
		if (null == abl) {
			abl = 0f;
		}

		// 百分比
		Float pct = effects.get(attrType + 2);
		if (null == pct) {
			pct = 0f;
		}

		// 天然小数值
		Float notPctAbl = effects.get(attrType + 3);
		if (notPctAbl == null) {
			notPctAbl = 0f;
		}

		float value = (float) (abl * (1 + pct));
		value += notPctAbl;
		return value;
	}

	/**
	 * 效果Map转换成为属性Map
	 * 
	 * @param effects
	 * @param attr
	 * @return
	 */
	public static Map<Integer, Float> effectMap2AttrMap(Map<Integer, Float> effects, Map<Integer, Float> attr) {

		if (null == effects || effects.size() == 0) {
			return attr;
		}

		for (Map.Entry<Integer, Float> entry : effects.entrySet()) {
			int attrId = getAttrIdByEffectId(entry.getKey());
			float value = calcEffectBonus(attrId, effects);
			if (attr.containsKey(attrId)) {
				attr.put(attrId, attr.get(attrId) + value);
			} else {
				attr.put(attrId, value);
			}
		}

		return attr;
	}

	/**
	 * 根据效果id计算对应的属性id
	 * 
	 * @param effectId
	 * @return
	 */
	public static int getAttrIdByEffectId(int effectId) {
		return effectId-effectId % 10 ;
	}
	
	/**
	 * 向map中添加key-value键值对，如果重复key就合并
	 * @param base
	 * @param key
	 * @param value
	 */
	public static void addValueToMap(Map<Integer,Integer> base, int key, int value){
		if(base.containsKey(key)){
			base.put(key, base.get(key) + value);
		}else{
			base.put(key, value);
		}
	}
	
	public static boolean isNumber(String s) {
		return s.matches(INTEGER_REGX) || s.matches(DECIMAL_REGX);
	}
}
