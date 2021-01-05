package com.nettyboot.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

	private static final DateTimeFormatter Y_M_D_T_H_M_S_Z = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final DateTimeFormatter Y_M_D_H_M_S = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter Y_M_D_H_M = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final DateTimeFormatter Y_M_D = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter Y_M = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private static final DateTimeFormatter YMDHMSS_17 = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	private static final DateTimeFormatter YMDHMSS_15 = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
	private static final DateTimeFormatter YMDHMS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");

    public static String getYMDTHMSZ(){
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(Y_M_D_T_H_M_S_Z);
	}
    
    public static String getCurrentYMDHMS(){
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(Y_M_D_H_M_S);
	}
    
    public static String getCurrentYMDHMS(long millis){
		Instant instant = Instant.ofEpochMilli(millis);
		LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
		return dateTime.format(Y_M_D_H_M_S);
	}
    
    public static String getCurrentYMDHM(){
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(Y_M_D_H_M);
	}
    
    public static String getCurrentYMDHM(long millis){
		Instant instant = Instant.ofEpochMilli(millis);
		LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
		return dateTime.format(Y_M_D_H_M);
	}
 
    public static String getCurrentYMDHMSS(){
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(YMDHMSS_17);
	}
    
    public static String getCurrentYMDHMSS2(){
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(YMDHMSS_15);
	}
    
    public static String getCurrentYMDHMS2(){
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(YMDHMS);
	}
    
    public static String getTodayYMD(){
		LocalDate date = LocalDate.now();
		return date.format(Y_M_D);
	}

	public static String getTodayYMD2(){
		LocalDate date = LocalDate.now();
		return date.format(YMD);
	}

	public static String getCurrentYM(){
		LocalDate date = LocalDate.now();
		return date.format(YM);
	}

	public static int getCurrentYear(){
		LocalDate date = LocalDate.now();
		return date.getYear();
	}

	public static String getCurrentHHmm(){
		String endTime = null;
		try{
			LocalTime time = LocalTime.now();
			endTime = time.format(HH_MM);
		}catch(Exception e){
		}
		return endTime;
	}

	public static String getYestodayYMDHMS(){
		Instant instant = Instant.ofEpochMilli(System.currentTimeMillis()-86400000L);
		LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
		return dateTime.format(Y_M_D_H_M_S);
	}

	public static String getYestodayYMD(){
		LocalDate today = LocalDate.now();
		LocalDate yestoday = today.minusDays(1);
		return yestoday.format(Y_M_D);
	}

	public static String getAfterDaysYMD(int days){
		String endDate = null;
		try{
			LocalDate date1 = LocalDate.now();
			LocalDate date2 = date1.plusDays(days);
			endDate = date2.format(Y_M_D);
		}catch(Exception e){
		}
		return endDate;
	}
    
    public static String getAfterDaysYMD(int days, String startDate){
    	String endDate = null;
    	try{
			DateTimeFormatter formatter = startDate.length() == 8 ? YMD : Y_M_D;
			LocalDate date1 = LocalDate.parse(startDate, formatter);
			LocalDate date2 = date1.plusDays(days);
			endDate = date2.format(formatter);
    	}catch(Exception e){
    	}
    	return endDate;
    }
    
    public static String getAfterMonthsYMD(int months, String startDate){
    	String endDate = null;
    	try{
			DateTimeFormatter formatter = startDate.length() == 8 ? YMD : Y_M_D;
			LocalDate date1 = LocalDate.parse(startDate, formatter);
			LocalDate date2 = date1.plusMonths(months);
			endDate = date2.format(formatter);
    	}catch(Exception e){
    	}
    	return endDate;
    }

	public static String getAfterMonthsYM(int months){
		String endDate = null;
		try{
			LocalDate date1 = LocalDate.now();
			LocalDate date2 = date1.plusMonths(months);
			endDate = date2.format(Y_M);
		}catch(Exception e){
		}
		return endDate;
	}

	public static String getAfterYearYMD(int years, String startDate){
		String endDate = null;
		try{
			DateTimeFormatter formatter = startDate.length() == 8 ? YMD : Y_M_D;
			LocalDate date1 = LocalDate.parse(startDate, formatter);
			LocalDate date2 = date1.plusYears(years);
			endDate = date2.format(formatter);
		}catch(Exception e){
		}
		return endDate;
	}

	public static String getAfterMinutesYMDHMS(int minutes){
		String endTime = null;
		try{
			LocalDateTime date1 = LocalDateTime.now();
			LocalDateTime date2 = date1.plusMinutes(minutes);
			endTime = date2.format(Y_M_D_H_M_S);
		}catch(Exception e){
		}
		return endTime;
	}

	public static String getAfterHoursYMDHMS(int hours){
		String endTime = null;
		try{
			LocalDateTime date1 = LocalDateTime.now();
			LocalDateTime date2 = date1.plusMinutes(hours);
			endTime = date2.format(Y_M_D_H_M_S);
		}catch(Exception e){
		}
		return endTime;
	}

    public static long getDaysBetweenDates(String ymdhm1, String ymdhm2){
    	try{
			LocalDateTime dateTime1 = LocalDateTime.parse(ymdhm1, Y_M_D_H_M);
			LocalDateTime dateTime2 = LocalDateTime.parse(ymdhm2, Y_M_D_H_M);
    		return Math.abs((dateTime1.toEpochSecond(ZoneOffset.UTC)-dateTime2.toEpochSecond(ZoneOffset.UTC))/86400);
    	}catch(Exception e){
    	}
    	return 0;
    }

    public static int getDayOfWeek(){
    	LocalDate date = LocalDate.now();
    	return date.getDayOfWeek().getValue();
    }
    
	public static boolean checkformatYMD(String date){
    	try{
    		LocalDate.parse(date, Y_M_D);
    		return true;
		}catch (Exception e){

		}
    	return false;
	}

	public static long getSecondsFromNow(String startTime){
		long seconds = 0;
		try {
			DateTimeFormatter formatter = startTime.length() == 16 ? Y_M_D_H_M : Y_M_D_H_M_S;
			LocalDateTime dateTime1 = LocalDateTime.parse(startTime, formatter);
			LocalDateTime dateTime2 = LocalDateTime.now();
			seconds = Math.abs(dateTime1.toEpochSecond(ZoneOffset.UTC)-dateTime2.toEpochSecond(ZoneOffset.UTC));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return seconds;
	}

}
