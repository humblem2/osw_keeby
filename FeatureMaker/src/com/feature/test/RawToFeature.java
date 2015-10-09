package com.feature.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;





///////////////////////////////////////////////
// ==> :: 유동적인 부분 ::
//brFileName
//bwFileName
//className
///////////////////////////////////////////////


// File을 읽고/쓰기
//==> raw-data(*.txt)를 read하여, 36차원의 Feature를 만든 후 *.CSV format으로 write하는 Logic.
public class RawToFeature 
{
	/// Filed
	private static String brFileName = "C:/z.SH_CHOdata/96.raw_sensor_data/003.unseendata_of_exercisetype/04.dumbbell_10set/raw_sensor_unseendata03.txt"; // 읽어올파일 경로, 유동적! ==> 이부분은 일부러 자동화 안시킴
	private static String bwFileName = "C:/z.SH_CHOdata/96.raw_sensor_data/004.feature_of_unseendata/04.dumbbell_10set/features_unseen_03.csv"; // write할 파일 경로, 유동적 ==> 이부분은 일부러 자동화 안시킴
	private static String className = "db"; // Class = Labeling [ ==> 1,3,5,...2n-1...] / ==> 유동적
	
	public static void main(String[] args) 
	{
		///Field
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		BufferedReader brForReadCount = null;
		
		
		double[] sumArray = { 0. , 0. , 0. , 0. , 0. , 0. , 0.}; // 테이블형식의 txt파일 column별로 summation하면서 저장.
		
		double mean = 0.; // 평균
		double variance = 0.; // 분산
		double deviation = 0.; // 표준편차
		double min = 0.; // 최대값
		double max = 0.; // 최소값
		double amplitude = 0.; // 진폭
		double rms = 0.; // RMS값(Root Mean Square)
		
		System.out.println("[Start Read txt File]");
		
		
		
		// Summation 부분(for Test)
		try 
		{
			// txt파일 읽어서
			br = new BufferedReader(new FileReader(brFileName)); // path 유동적
			brForReadCount = br;
			String oneLine = null;
			
			
			while(true)
			{
				
				try 
				{
					oneLine = br.readLine(); // 한줄 읽어서
					
					if(oneLine == null)
					{
						break;
					}
					
					// 공백으로 split하여 array에 할당
					String[] tempArray = null;
					tempArray = oneLine.trim().split(" ");
					
					
					for(int i = 0 ; i < tempArray.length ; i++)
					{
						// 요소를 double로 캐스팅후 계속 더해줘
						sumArray[i] += Double.parseDouble(tempArray[i].trim());
						
					}
					
					System.out.println("[Array length] "+tempArray.length+"-------"+" [test] :: "+oneLine);
					
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				} 
				
				
			}
			
			
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		
		
		
		System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		
		
		
		
		
		
		
		
		// 각 Column들의 합 잘 됬나 보자
		for (int i = 0; i < sumArray.length; i++) 
		{
			System.out.println("["+i+"] >> "+sumArray[i]);
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////// CSV파일 만들기 /////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		try 
		{
			// txt파일 읽어서
			bw = new BufferedWriter(new FileWriter(bwFileName,true)); // path 유동적 // 파일은 이어쓰기(true)
			
			// 이제 컬럼별로 담겼어.
			List<Double[]> list = RawToFeature.columnArray(brForReadCount);
			
			// CSV파일로 출력할 컬럼명 ==> CSV파일 생성시 초기 1번 실행 후 주석처리
			
		 	String columnName = "class,meanAccX,deviationAccX,varianceAccX,minAccX,maxAccX,amplitudeAccX,rmsAccx,"
									+"meanAccY,deviationAccY,varianceAccY,minAccY,maxAccY,amplitudeAccY,rmsAccY,"
									+"meanAccZ,deviationAccZ,varianceAccZ,minAccZ,maxAccZ,amplitudeAccZ,rmsAccZ,"
									+"meanGyroX,deviationGyroX,varianceGyroX,minGyroX,maxGyroX,amplitudeGyroX,rmsGyroX,"
									+"meanGyroY,deviationGyroY,varianceGyroY,minGyroY,maxGyroY,amplitudeGyroY,rmsGyroY,"
									+"meanGyroZ,deviationGyroZ,varianceGyroZ,minGyroZ,maxGyroZ,amplitudeGyroZ,rmsGyroZ,";
			
			
			bw.append(columnName);
			bw.append("\n");
			
			
			
			// CSV파일에 출력 ==> Class명 포함하여 append ==> 클래스명 유동적
			bw.append(className+",");
			
			
			// 42차원(6 x 7) ==> Label까지 고려하면 43차원의 Feature-Data.
			// Feature ==> 평균 / 분산 / 편차 / 최대값 / 최소값 / ㅣ 최대값 - 최소값 ㅣ= 진폭 / RMS
			for (Double[] doubles : list) // 차례대로 한 Column씩 빼서
			{
				for (int i = 0; i < doubles.length; i++) 
				{
					System.out.println(doubles);
					System.out.println("**********************************************************************************");
				}
				System.out.println("---------------------------------------------------------=================================================");
				
				mean = RawToFeature.mean(doubles);
				deviation = RawToFeature.standardDeviation(doubles, 0);
				variance = RawToFeature.variance(doubles);
				min = RawToFeature.valueMinMaxAmp(doubles, 0);
				max = RawToFeature.valueMinMaxAmp(doubles, 1);
				amplitude = RawToFeature.valueMinMaxAmp(doubles, 2);
				rms = RawToFeature.valueRMS(doubles);
				
				// [debug]
				System.out.println("\n=========================================================================================================");
				System.out.println("[Feature]\n");
				System.out.println(mean);
				System.out.println(deviation);
				System.out.println(variance);
				System.out.println(min);
				System.out.println(max);
				System.out.println(amplitude);
				System.out.println(rms);
				System.out.println("\n=========================================================================================================\n");
				
				
				// CSV파일에 출력 
				
				bw.append(Double.toString(mean)).append(",").append(deviation+",").append(variance+",").append(min+",").append(max+",").append(amplitude+",").append(rms+",");
				
				//////////////////////////////
				//////////////////////////////
				//////////////////////////////
				//////////////////////////////
				//////////////////////////////
				//////////////////////////////
				//////////////////////////////
				
				
			}// end of for
			bw.append("\n");
			bw.flush();
			bw.close();
			
			System.out.println(">>>  :: [Abstraction Successfully!] :: ");
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		
		
	} // end of main() method
	
	// 산술 평균 계산 method
	public static double mean(Double[] array) 
	{  
	    double sum = 0.0;
	    
	    for (int i = 0; i < array.length; i++)
	    {
	    	sum += array[i];
	    }
	    
	    return sum / array.length;
	}
	
	// 표준-편차 계산 method
	// option이 0 == 모집단의 표준편차
	// option이 1 == 표본집단의 표준편차
	public static double standardDeviation(Double[] array, int option)
	{
		if (array.length < 2) return Double.NaN;

	    double sum = 0.0; // 편차 제곱의 총합
	    double deviation = 0.0;
	    double diff;
	    double meanValue = mean(array); // 평균(Mean) 계산

	    for (int i = 0; i < array.length; i++) 
	    {
			diff = array[i] - meanValue;
			sum += (diff * diff);
	    }
	    
	    deviation = Math.sqrt(sum / (array.length - option));

	    return deviation;
	}
	
	// 분산 계산 method
	public static double variance(Double[] array)
	{
		double deviation = standardDeviation(array, 0);
		double variance = Math.pow(deviation, 2);
		
		return variance;
	}
	
	// 최대값, 최소값 , 진폭 계산 method 
	// option이 0 ==> minimum return,
	// option이 1 ==> maximum return,
	// option이 2 ==> 최대값과 최소값의 차이(진폭=amplitude).
	public static double valueMinMaxAmp(Double[] array, int option)
	{
		Arrays.sort(array); // 오름차순sorting
		double value = 0.;
		
		if(option == 0) // option이 0이면
		{
			value = array[0]; // 최소값 return 
		}
		else if(option == 1)
		{
			value = array[array.length-1]; // 최대값 return 
		}
		else
		{
			value = Math.abs((array.length-1)-array[0]);
		}
		
		return value;
	}
	// RMS(Root Mean Square) 계산 method
	public static double valueRMS(Double[] array)
	{
		double sum = 0.0;
		double rootMean = 0.0;
		double rootMeanSquare = 0.0;
		
		// 제곱의 합
		for (int i = 0; i < array.length; i++)
	    {
	    	sum += Math.pow(array[i], 2);
	    }
		
		rootMean = sum / array.length;
		rootMeanSquare = Math.sqrt(rootMean);
		
		return rootMeanSquare;
	}
	// 컬럼들을 배열로 만드는 method
	public static List<Double[]> columnArray(BufferedReader br)
	{
		
		Double[] accX;
		Double[] accY;
		Double[] accZ;
		
		Double[] gyroX;
		Double[] gyroY;
		Double[] gyroZ;
		
		Double[] time;
		
		String oneLine = null;
		
		List<Double> accXList = new ArrayList<Double>();
		List<Double> accYList = new ArrayList<Double>();
		List<Double> accZList = new ArrayList<Double>();
		
		List<Double> gyroXList = new ArrayList<Double>();
		List<Double> gyroYList = new ArrayList<Double>();
		List<Double> gyroZList = new ArrayList<Double>();
		List<Double> timeList = new ArrayList<Double>();
		
		
		
		try 
		{
			br = new BufferedReader(new FileReader(brFileName));
		} 
		catch (FileNotFoundException e1) 
		{
			e1.printStackTrace();
		}
		
		while(true)
		{
			
			try 
			{
				
				oneLine = br.readLine(); // 한줄 읽어서
				
				if(oneLine == null)
				{
					break;
				}
				
				// 공백으로 split하여 array에 할당
				String[] tempArray = null;
				tempArray = oneLine.trim().split(" ");
				
				
				for(int i = 0 ; i < tempArray.length ; i++)
				{
					
					// 요소를 double로 캐스팅후 계속 컬럼별로 list에 추가.
					switch (i)
					{
						case 0: accXList.add(Double.parseDouble(tempArray[i])); break;
						case 1: accYList.add(Double.parseDouble(tempArray[i])); break;
						case 2: accZList.add(Double.parseDouble(tempArray[i])); break;
						case 3: gyroXList.add(Double.parseDouble(tempArray[i])); break;
						case 4: gyroYList.add(Double.parseDouble(tempArray[i])); break;
						case 5: gyroZList.add(Double.parseDouble(tempArray[i])); break;
						
						case 6: timeList.add(Double.parseDouble(tempArray[i])); break; // 시간
					}
					
				}// end of for
				
			}// end of try 
			catch (IOException e)
			{
				e.printStackTrace();
			} 
			
			
		}// end of while
		
		//System.out.println(accXList);
		
		// 리스트를 배열로 .. Converting
		accX = accXList.toArray(new Double[accXList.size()]);
		accY = accYList.toArray(new Double[accYList.size()]);
		accZ = accZList.toArray(new Double[accZList.size()]);
		gyroX = gyroXList.toArray(new Double[gyroXList.size()]);
		gyroY = gyroYList.toArray(new Double[gyroYList.size()]);
		gyroZ = gyroZList.toArray(new Double[gyroZList.size()]);
		
		
		List<Double[]> list = new ArrayList<Double[]>();
		list.add(accX);
		list.add(accY);
		list.add(accZ);
		list.add(gyroX);
		list.add(gyroY);
		list.add(gyroZ);
		
	
		return list;
	
	}
	
}
