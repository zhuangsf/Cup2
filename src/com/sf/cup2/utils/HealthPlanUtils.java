package com.sf.cup2.utils;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * 饮水计划模块
 * @author soon
 *
 */
public class HealthPlanUtils {
	public final static int WEIGHT_POWER = 35;
	//性别权值：男0.05 女0.05（男女平等）
	public final static float SEXPOWER_MALE = 0.05f;
	public final static float SEXPOWER_FEMALE = 0.05f;
	//职业权值：体力职业0.2 脑力职业0.1 其他0.0 不动职业-0.1
	public final static float JOBPOWER_BODY = 0.2f;
	public final static float JOBPOWER_BRAIN = 0.1f;
	public final static float JOBPOWER_DEFAULT = 0.0f;
	public final static float JOBPOWER_HOME = -0.1f;
	public final static float OTHER_POWER = 0.005f;
	public final static int[] DEFAULT_BMI = new int[] {17,19,21};
	public final static int[] DEFAULT_WATER = new int[] {1050,1800,2100};
	
	/**
	 * 自觉确保参数符合规范，未作入参检测
	 * @param sex  male/female
	 * @param birthday  1990-01-01
	 * @param height
	 * @param weight
	 * @param job
	 * @param other
	 * @return
	 */
	public static String getSuggestPlan(String sex,String birthday,int height,int weight,String job,String other)
    {
		float waterI=planWaterI(weight);
		float waterII=planWaterII(birthday,height,weight);
		float waterIII=planWaterIII(waterI,waterII);
		
		float sexPower=Utils.SHARE_PREFERENCE_CUP_SEX_MALE.equals(sex)?SEXPOWER_MALE:SEXPOWER_FEMALE;
        float jobPower=getJobPower(job);
        float otherPower=getOtherPower(other);
		float waterFinal=planWaterFinal(waterIII,sexPower,jobPower,otherPower);
		return waterFinal+"";
    }
	
	/**
	 * 水量I（ml）=35*体重（kg）
	 * @param weight
	 * @return
	 */
	private static float planWaterI(int weight){
		float waterI = weight*WEIGHT_POWER;
		Utils.Log("planWaterI:"+waterI);
		return waterI;
	}
	
	/**
	 * 水量II（ml）=自身bmi*理想水量/理想bmi
	 * @param birthday
	 * @param height
	 * @param weight
	 * @return
	 */
	private static float planWaterII(String birthday,int height,int weight){
		String[] dateSpilt = birthday.split("-");
		Calendar c = Calendar.getInstance();//首先要获取日历对象
		int mYear = c.get(Calendar.YEAR);
		int birthYear = Integer.parseInt(dateSpilt[0]);
		int ageIndex = 0;
		if(mYear - birthYear >= 16)
		{
			ageIndex = 2;
		}
		else if(mYear - birthYear >= 10)
		{
			ageIndex = 1;
		}else
		{
			ageIndex = 0;
		}
		float waterII = getMyBmi(height,weight)*DEFAULT_WATER[ageIndex]/DEFAULT_BMI[ageIndex];;
		Utils.Log("planWaterII:"+waterII);
		return waterII;
	}
	
	/**
	 * BMI = 体重（kg） / （身高（m）×身高（m））
	 * @param height
	 * @param weight
	 * @return
	 */
	private static float getMyBmi(int height,int weight){
		float my_bmi = weight/(height/100.0f)/(height/100.0f);
		Utils.Log("getMyBmi:"+ my_bmi);
		return my_bmi;
	}
	
	/**
	 * 水量III（ml）=（水量I-水量II）*0.5+水量II
	 * @param waterI
	 * @param waterII
	 * @return
	 */
	private static float planWaterIII(float waterI,float waterII){
		float waterIII = (waterI + waterII)/2;
		Utils.Log("planWaterIII:"+waterIII);
		return waterIII;
	}
	
	/**
	 * 最终水量=水量III*(1+性别权值+职业权值+其他权值)
	 * @param waterIII
	 * @param sexPower
	 * @param jobPower
	 * @param otherPower
	 * @return
	 */
	private static float planWaterFinal(float waterIII,float sexPower,float jobPower,float otherPower){
		float waterFinal = waterIII * (1 + sexPower + jobPower + otherPower);
		Utils.Log("planWaterFinal:" + waterFinal+" power:"+(1 + sexPower + jobPower + otherPower));
		BigDecimal b = new BigDecimal(waterFinal);
		float f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).floatValue();  
		return f1;
	}
	
	/**
	 * 职业权值：体力职业0.2 脑力职业0.1 其他0.0 不动职业-0.1
	 * @param job
	 * @return
	 */
	private static float getJobPower(String job){
		float jobPower=JOBPOWER_DEFAULT;
		if(("body work").equals(job)){
			jobPower=JOBPOWER_BODY;
		}else if (("brain work").equals(job)){
			jobPower=JOBPOWER_BRAIN;
		}else if (("at home").equals(job)){
			jobPower=JOBPOWER_HOME;
		}else{
			jobPower=JOBPOWER_DEFAULT;
		}
		return jobPower;
	}
	
	//
	/**
	 * 手机号各个位数相加×0.005    0～0.045
	 * @param other    phone num
	 * @return
	 */
	private static float getOtherPower(String other){
		int i=Utils.getSumInOne(other);
		Utils.Log("getOtherPower:"+(i*OTHER_POWER));
		return i*OTHER_POWER;
	}
}
