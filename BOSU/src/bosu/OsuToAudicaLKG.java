/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bosu;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.util.List;


public class OsuToAudicaLKG {

    /**
     * @param args the command line arguments
     */
    public static void main2(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<Circle2> circles = new ArrayList<Circle2>();
        
        
        String newLine = scanner.nextLine();
        while(!newLine.equals("")){
            String[] numS = newLine.split(",");
            double[] nums = new double[numS.length];
            for(int x=0; x<5/*numS.length*/; x++){
                nums[x] = Double.parseDouble(numS[x]);
            }
            Circle2 newCircle = new Circle2(nums[0],nums[1],nums[2]/(60000.0 / (128.0 * 480.0)),nums[3],nums[4]);
            if(Integer.parseInt(numS[3]) % 2 == 0){
                newCircle.isSlider = true;
                String[] newSplit = numS[5].split("\\|");
                newCircle.sliderType = newSplit[0];
                newCircle.curvePointsXs = new double[newSplit.length-1];
                newCircle.curvePointsYs = new double[newSplit.length-1];
                for(int i=0; i<newSplit.length-1; i++){
                    String[] xy = newSplit[i+1].split(":");
                    newCircle.curvePointsXs[i] = Double.parseDouble(xy[0]);
                    newCircle.curvePointsYs[i] = Double.parseDouble(xy[1]);
                }
                
                newCircle.repeat = Double.parseDouble(numS[6]);
                newCircle.pixelLength = Double.parseDouble(numS[7]);
                
            }
            circles.add(newCircle);
            
            newLine = scanner.nextLine();
        }
        
        
        System.out.println("--------");
        
        
        /*for(int x=0; x<circles.size(); x++){
            if(!circles.get(x).isSlider)
                System.out.println(circles.get(x).x + "," + circles.get(x).y + "," + circles.get(x).time + "," + circles.get(x).type + "," + circles.get(x).hitSound);
            else{
                System.out.print(circles.get(x).x + "," + circles.get(x).y + "," + circles.get(x).time + "," + circles.get(x).type + "," + circles.get(x).hitSound + "," + circles.get(x).sliderType);
                for(int i=0; i<circles.get(x).curvePointsXs.length; i++){
                    System.out.print("|" + circles.get(x).curvePointsXs[i] + ":" + circles.get(x).curvePointsYs[i]);
                }
                System.out.println("," + circles.get(x).repeat + "," + circles.get(x).pixelLength);
            }
        }*/
        System.out.println("{\r\n\"cues\":[");
        for(int i=0; i<circles.size(); i++){
            System.out.print("{\r\n\"tick\": "+ circles.get(i).time +",\r\n\"tickLength\": 120,\r\n\"pitch\": 3,\r\n\"velocity\": 127,\r\n\"gridOffset\":{\r\n\"x\": 0.0,\r\n\"y\": 0.0\r\n},\r\n\"handType\": 1,\r\n\"behavior\": 0\r\n}");
        }
        System.out.println("]}");
        
        try(FileWriter fileWriter = new FileWriter("D:\\SteamLibrary\\steamapps\\common\\Audica\\Audica_Data\\StreamingAssets\\HmxAudioAssets\\songs\\MyLove\\expert.cues")) {
            fileWriter.write("{\r\n\"cues\":[");
            for(int i=0; i<circles.size()-1; i++){
                fileWriter.write("{\r\n\"tick\": "+ circles.get(i).time +",\r\n\"tickLength\": 120,\r\n\"pitch\": "+(coordsOf((int)circles.get(i).x,(int)circles.get(i).y))+",\r\n\"velocity\": 127,\r\n\"gridOffset\":{\r\n\"x\": 0.0,\r\n\"y\": 0.0\r\n},\r\n\"handType\": 1,\r\n\"behavior\": 0\r\n},");
            }
            fileWriter.write("{\r\n\"tick\": "+ circles.get(circles.size()-1).time +",\r\n\"tickLength\": 120,\r\n\"pitch\": "+(coordsOf((int)circles.get(circles.size()-1).x,(int)circles.get(circles.size()-1).y))+",\r\n\"velocity\": 127,\r\n\"gridOffset\":{\r\n\"x\": 0.0,\r\n\"y\": 0.0\r\n},\r\n\"handType\": 1,\r\n\"behavior\": 0\r\n}");
            fileWriter.write("]}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static int coordsOf(int osuX, int osuY){
        int x = 0;
        int y = 0;
        double offsetX = 0.0;
        double offsetY = 0.0;
        
        osuX = (osuX/2)+128;
        
        if(osuX < 42){
            x = 0;
            offsetX = osuX/42.0;
        }
        else if(osuX < 85){
            x = 1;
            offsetX = (osuX-42.0)/(85.0-42.0);
        }
        else if(osuX < 127){
            x = 2;
            offsetX = (osuX-85.0)/(127.0-85.0);
        }
        else if(osuX < 170){
            x = 3;
            offsetX = (osuX-127.0)/(170.0-127.0);
        }
        else if(osuX < 212){
            x = 4;
            offsetX = (osuX-170.0)/(212.0-170.0);
        }
        else if(osuX < 255){
            x = 5;
            offsetX = (osuX-212.0)/(255.0-212.0);
        }
        else if(osuX < 297){
            x = 6;
            offsetX = (osuX-255.0)/(297.0-255.0);
        }
        else if(osuX < 340){
            x = 7;
            offsetX = (osuX-297.0)/(340.0-297.0);
        }
        else if(osuX < 382){
            x = 8;
            offsetX = (osuX-340.0)/(382.0-340.0);
        }
        else if(osuX < 425){
            x = 9;
            offsetX = (osuX-382.0)/(425.0-382.0);
        }
        else if(osuX < 469){
            x = 10;
            offsetX = (osuX-425.0)/(469.0-425.0);
        }
        else if(osuX < 512){
            x = 11;
            offsetX = (osuX-469.0)/(512.0-469.0);
        }
        
        if(osuY < 55){
            y = 6;
            offsetY = osuY/55.0;
        }
        else if(osuY < 110){
            y = 5;
            offsetY = (osuY-55.0)/(110.0-55.0);
        }
        else if(osuY < 165){
            y = 4;
            offsetY = (osuY-110.0)/(165.0-110.0);
        }
        else if(osuY < 220){
            y = 3;
            offsetY = (osuY-165.0)/(220.0-165.0);
        }
        else if(osuY < 275){
            y = 2;
            offsetY = (osuY-220.0)/(275.0-220.0);
        }
        else if(osuY < 330){
            y = 1;
            offsetY = (osuY-275.0)/(330.0-275.0);
        }
        else if(osuY < 384){
            y = 0;
            offsetY = (osuY-330.0)/(384.0-330.0);
        }
        
        return x + 12 * y;
    }
    
}


class Circle2{
    public boolean isSlider = false;
    
    public double x,y,time,type,hitSound;
    
    public String sliderType;
    public double[] curvePointsXs;
    public double[] curvePointsYs;
    public double repeat;
    public double pixelLength;
    
    public Circle2(double ix, double iy, double itime, double itype, double ihitSound){
        x=ix;
        y=iy;
        time=itime;
        type=itype;
        hitSound=ihitSound;
    }
    
    public Circle2(double ix, double iy, double itime, double itype, double ihitSound, 
            String isliderType, double[] icurvePointsXs, double[] icurvePointsYs, double irepeat, double ipixelLength){
        x=ix;
        y=iy;
        time=itime;
        type=itype;
        hitSound=ihitSound;
        
        sliderType = isliderType;
        curvePointsXs = icurvePointsXs;
        curvePointsYs = icurvePointsYs;
        repeat = irepeat;
        pixelLength = ipixelLength;
    }
}
