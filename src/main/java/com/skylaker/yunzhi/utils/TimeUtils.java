package com.skylaker.yunzhi.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



/**
 * 一些日期,时间相关的判断方法
 *
 */
public class TimeUtils {
	
	public static final long SECOND_MILLS = 1000L;
	
	public static final long MINUTE_MILLS = SECOND_MILLS * 60;
	
	public static final long HOUR_MILLS = MINUTE_MILLS * 60;
	
	public static final long DAY_MILLS = HOUR_MILLS * 24;

	public static final long WEEK_MILLS = DAY_MILLS * 7;
	
	public static final long TIME_ZONE_OFFSET = Calendar.getInstance().getTimeZone().getRawOffset();  // 时区导致的差值
	
	public static final long ADD_MILLS = DAY_MILLS * 3;  // Java时间的第一天（1970.1.1）是周四，这个是把一周的前三天补全的时间（以周一为第一天）
	
	private static final ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>();
	
	private static ThreadLocal<SimpleDateFormat> simpleDateFormat1 = new ThreadLocal<SimpleDateFormat>();
	
	/**
	 * 将time偏移offsetTime毫秒再计算与1970-01-01 00:00:00相差的天数
	 */
	public static int getOffsetDay(long time, long offsetTime) {
		
		return Conv.toInt((time + offsetTime + TIME_ZONE_OFFSET) / DAY_MILLS);
	}
	
	public static long getCurrentHour(long time) {
		return (time + TIME_ZONE_OFFSET) / HOUR_MILLS;
	}
	
	public static int getCurrentDay(long time) {

		return Conv.toInt((time + TIME_ZONE_OFFSET) / DAY_MILLS);
	}

	public static int getCurrentWeek(long time) {

		return Conv.toInt((time + TIME_ZONE_OFFSET + ADD_MILLS) / WEEK_MILLS) + 1;
	}
	
	/**
	 * @return 获取偏移后的时间
	 * @param time	当前时间
	 * @param n				清数据的时间点
	 * @param before		0点是否前移
	 * */
	public static long getOffSetTime(long time, int n, boolean before){
		long offsetTime = 0;
		if(before) {
			n = (0 == n) ? 24 : n;
			offsetTime = (24 - n) * HOUR_MILLS;  // offsetTime > 0
		} else {
			offsetTime = -n * HOUR_MILLS;  //offsetTime < 0
		}
		
		long offSetTime = time + offsetTime;
		if(offSetTime < 0){
			offSetTime = 0;
		}
		return offSetTime;
	}
	
	/**
	 * 每周一n点刷新的判断
	 */
	public static boolean checkPassWeek(long time, int n, boolean before) {
		
		long nowOffsetTime = TimeUtils.getOffSetTime(WorldTime.getInstance().getTimeInMillis(), n, before);
		long lastAcceptOffsetTime = TimeUtils.getOffSetTime(time, n, before);
		return !TimeUtils.inTheSameWeek(nowOffsetTime, lastAcceptOffsetTime);
	}
	
	/**
	 * 是否可以做N点清除数据的操作
	 * 
	 * a.N == 0 算法1 
	 * b.N != 0 && before == true 算法2
	 * c.N != 0 && before == false  算法3
	 * 
	 * 算法1：判断是否同一天即可
	 * 
	 * 算法2(N点之后算在下一天即0点往前移)：
	 * 情况1：上次清的时间和当前时间在同一天
	 * 			a.当前时间在[0,N)(不清)
	 * 			b.当前时间在[N,24],上次清时间在[N,24] (不清)
	 * 			c.当前时间在[N,24],上次清时间在[0,N) (清)
	 * 情况2：上次清的时间是当前时间的前一天
	 * 			a.上次清时间在[0,N) (清)
	 * 			b.上次清时间在[N,24],当前时间在[N,24] (清)
	 * 			c.上次清时间在[N,24],当前时间在[0,N) (不清)
	 * 情况3：上次清的时间在当前时间的前一天之前 (清)
	 * 
	 * 算法3(N点之前算在前一天即0点往后移)
	 * 情况1：上次清的时间和当前时间在同一天
	 * 			a.上次清时间在[N,24](不清)
	 * 			b.上次清时间在[0,N),当前时间[0,N](不清)
	 * 			c.上次清时间在[0,N),当前时间在[N,24](清)
	 * 情况2：上次清的时间是当前时间的前一天
	 * 			a.当前时间在[N,24](清)
	 * 			b.当前时间在[0,N),上次清时间在[0,N)(清)
	 * 			c.当前时间在[0,N),上次清时间在[n,24](不清)
	 * 情况3：上次清的时间在当前时间的前一天之前 (清)
	 * 
	 * 综合上述分析可看出算法2和算法3的情况1、情况3一样
	 * 
	 * @param lastClearTime	上次清数据的时间
	 * @param n				清数据的时间点
	 * @param before		0点是否前移
	 * */
	public static boolean canClearAtN(long lastClearTime, int n, boolean before) {
		
		if(0 == lastClearTime) {
			return true;
		}
		
		long offsetTime = 0;
		if(before) {
			n = (0 == n) ? 24 : n;
			offsetTime = (24 - n) * HOUR_MILLS;  // offsetTime > 0
		} else {
			offsetTime = -n * HOUR_MILLS;  //offsetTime < 0
		}
		
		long now = WorldTime.getInstance().getTimeInMillis();
		return getOffsetDay(now, offsetTime) != getOffsetDay(lastClearTime, offsetTime);
		
		/*
		if(0 == n) {//算法1
			return !DateValidate.inTheSameDay(now, lastClearTime);
		}
		
		int lastDay = getCurrentDay(lastClearTime);
		int curDay = getCurrentDay(now);
		int lastHour = getHourOfDay(lastClearTime);
		int curHour = getHourOfDay(now);
		
		if(curDay - lastDay > 1) {//情况3
			return true;
		} else if(curDay - lastDay == 0) {//情况1
			if(curHour >= n && lastHour < n) {
				return true;//1c
			}
		} else {//情况2
			if(before) {//算法2
				if(lastHour < n) {
					return true;//22a
				}
				if(curHour >= n) {
					return true;//22b
				}
			} else {//算法3
				if(curHour >= n) {
					return true;//32a
				}
				if(lastHour < n) {
					return true;//32b
				}
			}
		}
		
		return false;
		*/
	}
	
	public static boolean inTheSameHour(long firstT, long secondT) {
		return getCurrentHour(firstT) == getCurrentHour(secondT);
	}
	
	public static boolean inTheSameDay(long firstT, long secondT) {
		
		if (getCurrentDay(firstT) == getCurrentDay(secondT))
			return true;
		
		return false;
	}
	
	public static boolean inTheSameWeek(long firstT, long secondT) {
		
		if (getCurrentWeek(firstT) == getCurrentWeek(secondT))
			return true;
		
		return false;
	}
	
	// 获取某天的第一秒 add by lc
	public static long getDayFirstSecond(long time) {
		return time - (time + TIME_ZONE_OFFSET) % DAY_MILLS;
	}
	
	// 获取某天的最后一秒
	public static long getDayLastSecond(long time) {
		return getDayFirstSecond(time) + DAY_MILLS;
	}
	
	// 获取某周的第一秒（以 周一算） add by lc
	public static long getWeekFirstSecond(long time) {
		return time - (time + TIME_ZONE_OFFSET + ADD_MILLS) % WEEK_MILLS;
	}
	
	//获取时间是周几
	public static int getCurrentWeekDay(long time) {
		
		return (int)((time - getWeekFirstSecond(time)) / DAY_MILLS + 1);
	}
	
	// 获取该月的第一秒
	public static long getMonthFirstSecond(long time) {
		
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	// 获取该月的最后一秒
	public static long getMonthLastSecond(long time) {
		
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	public static int getMonth_of_year(long time){
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal.get(Calendar.MONTH) + 1;
	}
	
	public static int getDay_of_month(long time){
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal.get(Calendar.DAY_OF_MONTH);
	}
	
	public static long getMonthFirstSecondOffset(long time, int n) {
		
		long firstTime = getMonthFirstSecond(time);
		long offsetMills = n * HOUR_MILLS;
		if(time - firstTime <= offsetMills)  // time是这个月的第一天前n个小时 应该算在上个月
			time -= offsetMills;
		return getMonthFirstSecond(time) + offsetMills;
	}
	
	public static long getMonthLastSecondOffset(long time, int n) {
		
		long firstTime = getMonthFirstSecond(time);
		long offsetMills = n * HOUR_MILLS;
		if(time - firstTime <= offsetMills)  // time是这个月的第一天前n个小时 应该算在上个月
			time -= offsetMills;
		return getMonthLastSecond(time) + offsetMills;
	}
	
	public static boolean inTheSameMonth(long firstT, long secondT) {
		
		final Calendar cal1 = Calendar.getInstance();
		final Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(firstT);
		cal2.setTimeInMillis(secondT);
		
		if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
			return true;
		
		return false;
	}
	
	public static String formatTimeByType(long time, String format){
		
		String myFormat = null == format ? "yyyy年M月d日    HH:mm:ss" : format;
		SimpleDateFormat fomat = new SimpleDateFormat(myFormat);
		return fomat.format(time);
	}
	
	public static boolean inTheSameYear(long firstT, long secondT){
		final Calendar cal1 = Calendar.getInstance();
		final Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(firstT);
		cal2.setTimeInMillis(secondT);
		
		if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
			return true;
		return false;
	}
	
	public static int getHourOfDay(long time) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}
	
	/**
	 * 获取当前月一共多少天
	 * */
	public static int getMaxDayOfMonth(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 判断当前时间是不是在给定的时间段之间     必须是当天的
	 * end > begin  [0:00  ~ 24:00]
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isInTimeSubsection(String begin, String end){
		try{
			begin = begin.replace("：", ":");
			end = end.replace("：", ":");
			
			String[] beginArray = begin.split(":");
			String[] endArray = end.split(":");
			if(beginArray.length<2 || endArray.length <2){
				throw new RuntimeException("时间段格式错误：");
			}
			
			int beginHour = Integer.parseInt(beginArray[0]);
			int beginMin = Integer.parseInt(beginArray[1]);
			int endHour = Integer.parseInt(endArray[0]);
			int endMin = Integer.parseInt(endArray[1]);			
			
			if(beginHour>endHour ||
					beginHour<0 || beginHour>24 ||
					endHour<0 || endHour > 24){
				throw new RuntimeException("结束时间段 应当大于开始时间段");
			}
			
			Calendar calendar = Calendar.getInstance(); 
			calendar.clear(Calendar.HOUR_OF_DAY);   
			calendar.clear(Calendar.MINUTE);
			calendar.set(Calendar.HOUR_OF_DAY, beginHour);   
			calendar.set(Calendar.MINUTE, beginMin);
			long startTime = calendar.getTimeInMillis();
			
			calendar.clear(Calendar.HOUR_OF_DAY);   
			calendar.clear(Calendar.MINUTE);
			calendar.set(Calendar.HOUR_OF_DAY, endHour);   
			calendar.set(Calendar.MINUTE, endMin);
			long endTime = calendar.getTimeInMillis();
			long now = System.currentTimeMillis();
			if(startTime <=now  && endTime>=now){
				return true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 判断当前时间是不是在给定的时间段之间     必须是当天的
	 * end > begin  [0:00:00  ~ 24:00:00]
	 * @param begin
	 * @param end
	 * @param isSecond 重载用无实际意义
	 * @return
	 */
	public static boolean isBetweenTime(String begin, String end){
		try{
			begin = begin.replace("：", ":");
			end = end.replace("：", ":");
			
			String[] beginArray = begin.split(":");
			String[] endArray = end.split(":");
			if(beginArray.length<3 || endArray.length <3){
				throw new RuntimeException("时间段格式错误：");
			}
			
			int beginHour = Integer.parseInt(beginArray[0]);
			int beginMin = Integer.parseInt(beginArray[1]);
			int beginSecond = Integer.parseInt(beginArray[2]);
			int endHour = Integer.parseInt(endArray[0]);
			int endMin = Integer.parseInt(endArray[1]);			
			int endSecond = Integer.parseInt(endArray[2]);			
			
			if(beginHour>endHour ||
					beginHour<0 || beginHour>24 ||
					endHour<0 || endHour > 24){
				throw new RuntimeException("结束时间段 应当大于开始时间段");
			}
			
			Calendar calendar = Calendar.getInstance(); 
			calendar.clear(Calendar.HOUR_OF_DAY);   
			calendar.clear(Calendar.MINUTE);
			calendar.clear(Calendar.SECOND);
			calendar.set(Calendar.HOUR_OF_DAY, beginHour);   
			calendar.set(Calendar.MINUTE, beginMin);
			calendar.set(Calendar.SECOND, beginSecond);
			long startTime = calendar.getTimeInMillis();
			
			calendar.clear(Calendar.HOUR_OF_DAY);   
			calendar.clear(Calendar.MINUTE);
			calendar.clear(Calendar.SECOND);
			calendar.set(Calendar.HOUR_OF_DAY, endHour);   
			calendar.set(Calendar.MINUTE, endMin);
			calendar.set(Calendar.SECOND, endSecond);
			long endTime = calendar.getTimeInMillis();
			long now = System.currentTimeMillis();
			if(startTime <=now  && endTime>=now){
				return true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 传入一个开始时间和结束时间，判断当前时间是否在之间
	 * @param startTime 开始时间 =null("")表示没有开始时间
	 * @param endTime 结束时间 =null("")表示没有结束时间
	 * @return
	 */
	public static boolean isRunning(String startTime, String endTime) {
		long now = WorldTime.getInstance().getTimeInMillis();
		try {
			if(CommonUtil.isEmpty(startTime)) {//没有开始时间
				if(CommonUtil.isEmpty(endTime)) {//没有结束时间
					return true;
				} else if(now <= parseDate(endTime)) {//在结束时间之前
					return true;
				} else {//在结束时间之后
					return false;
				}
			} else {//有开始时间
				if(CommonUtil.isEmpty(endTime)) {//没有结束时间
					if(now >= parseDate(startTime)) {//在开始时间之后
						return true;
					} else {//在开始时间之前
						return false;
					}
				} else {//有结束时间
					if(now < parseDate(startTime)
							|| now > parseDate(endTime)) {
						return false;
					} else {
						return true;
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static String getTodayStr(long cur) {
		SimpleDateFormat format = simpleDateFormat1.get();
		if (null == format) {
			format = new SimpleDateFormat("yyyy-MM-dd");
			simpleDateFormat1.set(format);
		}
		return format.format(new Date(cur));
	}
	
	public static long parseDate(String str) {
		SimpleDateFormat format = simpleDateFormat.get();
		if (null == format) {
			format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			simpleDateFormat.set(format);
		}
		try {
			str = str.replace("：", ":");
			return format.parse(str).getTime();
		} catch (Exception e) {
			throw new RuntimeException("时间格式错误：" + str, e);
		}
	}
	
	/**
	 * 返回2个日期之间的月数
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getMonthSpace(long time1, long time2){
		Calendar calendar1 = Calendar.getInstance(); 
		Calendar calendar2 = Calendar.getInstance(); 
		calendar1.setTimeInMillis(time1);
		calendar2.setTimeInMillis(time2);
		
		int year = calendar1.get(Calendar.YEAR)-calendar2.get(Calendar.YEAR);
		int mounth = calendar1.get(Calendar.MONTH) - calendar2.get(Calendar.MONTH);
		
		int result = year *12 + mounth;
		return Math.abs(result);
	}
	
	
	/**
	 * 获取2个日期之间的天数
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getDaysBetween(long time1, long time2){
		
		int subDays =  Conv.toInt((time2 - time1) / DAY_MILLS);
		return Math.abs(subDays);
	}
	
	/**
	 * 获取2个日期之间的时间数 向上取整
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getHoursBetween(long time1, long time2) {
		int hours = 0;
		long diff = time2 - time1;
		if(diff <= TimeUtils.HOUR_MILLS) {
			hours = 1;
			return hours;
		} 
		
		long diffhour = diff / TimeUtils.HOUR_MILLS;
		if(diff % TimeUtils.HOUR_MILLS > 0) {
			hours += 1;	
		}
		hours += diffhour;
		
		return hours;
	}
	
	/**
	 * 获取一天内时间段的描述
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 每日12:00到14:00
	 */
	public static String getDayTimePeriodString(long start, long end) {
		
		if(start > end)
			return null;
		
		if(end >  TimeUtils.DAY_MILLS)
			return null;
		
		return "每日" + getClockTime(start) + "到" + getClockTime(end);
	}
	
	/**
	 * 获取一周内时间段的描述
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 如：周二12:00到周三14:00
	 */
	public static String getWeekPeriodTimeString(long start, long end) {
		
		if(start > end)
			return null;
		
		if(end >  TimeUtils.WEEK_MILLS)
			return null;
		
		String startStr = getWeekClockTime(start);
		String endStr = getWeekClockTime(end);
		
		if(startStr.substring(0, 2).equals(endStr.substring(0,2)))
			endStr = endStr.substring(2);  // 如果是同一天，省略后一个周X的描述
		
		return startStr+"到"+endStr;
	}
	
	/**
	 * 从时间的毫秒值到时钟值，精确到分
	 * @param time  时间的毫秒值
	 * @return 如：23:05 
	 */
	public static String getClockTime(long time) {
		
		if(time >= TimeUtils.DAY_MILLS)
			return null;
		
		long hourNum = time / TimeUtils.HOUR_MILLS;
		long hourRet = time % TimeUtils.HOUR_MILLS;
		long minuteNum = hourRet / TimeUtils.MINUTE_MILLS;
		
		return String.format("%02d:%02d", hourNum, minuteNum);
	}
	
	/**
	 * 从毫秒值到周几时钟值，精确到分
	 * @param time
	 * @return 如：周三23:05 
	 */
	public static String getWeekClockTime(long time) {
		
		if(time >= TimeUtils.WEEK_MILLS)
			return null;
		
		int daynum = (int)(time /  TimeUtils.DAY_MILLS);
		long dayret = time %  TimeUtils.DAY_MILLS;
		
		String clocktime = getClockTime(dayret);
		switch(daynum) {
		case 0:
			return "周一"+clocktime;
			
		case 1:
			return "周二"+clocktime;
			
		case 2:
			return "周三"+clocktime;
			
		case 3:
			return "周四"+clocktime;
			
		case 4:
			return "周五"+clocktime;
			
		case 5:
			return "周六"+clocktime;
			
		case 6:
			return "周日"+clocktime;
		}
		
		return null;
	}
	
	/**
	 * 获得一段时间的短描述，如: X天X小时X分钟，高级的单位没有时则不存在，例如小于1天则变成X小时X分钟
	 * 
	 * @return
	 */
	public static String getPeriodShortFormat(long period) {
		
		long daynum = period /  TimeUtils.DAY_MILLS;
		long dayret = period %  TimeUtils.DAY_MILLS;
		
		long hournum = dayret / TimeUtils.HOUR_MILLS;
		long hourret = dayret % TimeUtils.HOUR_MILLS;
		
		long minutenum = hourret / TimeUtils.MINUTE_MILLS;
		
		if(daynum > 0)
			return daynum + "天" + hournum + "小时" + minutenum + "分钟";
		else if(hournum > 0)
			return hournum + "小时" + minutenum + "分钟";
		else if(minutenum > 0)
			return minutenum + "分钟";
		else
			return "1分钟";
	}
	
	/**
	 * 根据策划配置制定时间，判断当前时间是否跨周
	 * configTime为距离每日0点的小时数
	 * 当跨周时返回false
	 * */
	public static boolean isBetweenWeek(long time, int configTime) {
		
		long configMillsTime = configTime * HOUR_MILLS;
		int curWeek = getCurrentWeekByConfig(configTime);
		int week2 = Conv.toInt((time + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / WEEK_MILLS) + 1;
		return curWeek == week2; 
	}
	
	/**
	 * 根据策划配置的每周N点为一周的起点，获取当前为一周的第几天
	 * configTime == 1表示周一的1点为本周的第一天。
	 * */
	public static int getWeekDayByConfig(int configTime) {
		
		long configMillsTime = configTime * HOUR_MILLS;
		int days = (int)(((WorldTime.getInstance().getTimeInMillis() + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / DAY_MILLS) + 1);
		return days % 7;
	}
	/**
	 * 根据策划配置的每周N点为一周的起点，获取time为一周的第几天
	 * @param time
	 * @param configTime == 1表示周一的1点为本周的第一天。
	 * @return
	 */
	public static int getWeekDayByConfig(long time, int configTime) {
		
		long configMillsTime = configTime * HOUR_MILLS;
		int days = (int)(((time + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / DAY_MILLS) + 1);
		return days % 7;
	}
	
	/**
	 * 根据策划配置，获取当前时间的周数。
	 * @param configTime  每天零点的偏移小时数
	 * */
	public static int getCurrentWeekByConfig(int configTime){
		
		long configMillsTime = configTime * HOUR_MILLS;
		int weekDay = (int)(((WorldTime.getInstance().getTimeInMillis() + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / WEEK_MILLS) + 1);
		return weekDay;
	}
	
	/**
	 * 根据策划配置，获取time时间的周数。
	 * 
	 * @param time
	 * @param configTime
	 * @return
	 */
	public static int getCurrentWeekByConfig(long time, int configTime) {

		long configMillsTime = configTime * HOUR_MILLS;
		int weekDay = (int) (((time + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / WEEK_MILLS) + 1);
		return weekDay;
	}
	
	/**
	 * 判断当前时间与给定时间的大小，单位为毫秒
	 * 给定时间大于当前时间，表示超前
	 * */
	public static boolean isHead(long compareTime){
		long now = WorldTime.getInstance().getTimeInMillis();
		return compareTime > now;
	}
	
	/**
	 * 根据配置获取当前的天数
	 * configTime == 8表示早上8点为一天的开始时间
	 * */
	public static int getCurrentDayByConfig(int configTime) {
		int configMillsTime = configTime*60*60*1000;
		long now = WorldTime.getInstance().getTimeInMillis();
		return (int) ((now + TIME_ZONE_OFFSET - configMillsTime) / DAY_MILLS);
	}
	
	/**
	 * 根据配置时间获取当前时间的毫秒值
	 * @param time
	 * @return
	 */
	public static long getCurrentMillisByConfig(String time) {
		return parseDate(getCurrentTimeStringByConfig(time));
	}
	
	/**
	 * 根据配置表获取时间的字符串
	 * @param time
	 * @return
	 */
	public static String getCurrentTimeStringByConfig(String time) {
		String currentDay = getTodayStr(WorldTime.getInstance().getTimeInMillis());
		String retTimeStr = currentDay + " " +  time;
		return retTimeStr;
	}
	
	
	/**
	 * 指定日期d加上天数
	 * @param d
	 * @param day
	 * @return
	 */
	public static Date addDate(Date d, long day) {
		long time = d.getTime();
		day = day * DAY_MILLS;
		time += day;
		return new Date(time);
	}
	
	/**
	 * 天数转换为毫秒数
	 * @param day 天数
	 * @return 转换后的毫秒数
	 */
	public static long getMillisByDay(int day) {
		return TimeUtils.DAY_MILLS * day;
	}
	
}
