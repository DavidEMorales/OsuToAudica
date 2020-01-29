/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bosu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.text.DecimalFormat;
import javax.swing.*;


public class OsuToAudica {
    
    public static void main1(String[] args) {
        OsuToAudica ota = new OsuToAudica();
    }
    
    public static int width=300;
    public static int height=240;
    public JFrame frame;
    public Random randy=new Random();
    
    
    public OsuToAudica(){
        
        frame = new JFrame("Osu! to Audica Map Converter");
        frame.setSize(width,height);//Toolkit.getDefaultToolkit().getScreenSize());//width, height);
        frame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - width/2, Toolkit.getDefaultToolkit().getScreenSize().height/2 - height/2);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel();
        
        
        JLabel pathIntroText = new JLabel(".osu file location:");
        JTextField pathText = new JTextField(10);
        
        JLabel nameIntroText = new JLabel(".osu file name:");
        JTextField nameText = new JTextField(10);
        
        JLabel path2IntroText = new JLabel(".audica file creation location:");
        JTextField path2Text = new JTextField(10);
        
        JLabel bpmIntroText = new JLabel("Song bpm:");
        JTextField bpmText = new JTextField(10);
        
        JLabel offsetIntroText = new JLabel("Time offset (in ms):");
        JTextField offsetText = new JTextField(10);
        
        String[] optionStrings = {"No Sliders/Sustains", "Only Sustains", "Basic Sliders", "Interpolate Sliders"};
        JComboBox optionList = new JComboBox(optionStrings);
        optionList.setSelectedIndex(0);
        
        String[] handOptionStrings = {"Same Hand", "Alternate Hands"};
        JComboBox handOptionList = new JComboBox(handOptionStrings);
        handOptionList.setSelectedIndex(0);
        
        
        JButton convertButton = new JButton("Convert");
        convertButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!pathText.getText().equals("")){
                    convert(pathText.getText(), nameText.getText(), path2Text.getText(), Double.parseDouble(bpmText.getText()), optionList.getSelectedIndex(), handOptionList.getSelectedIndex(), Double.parseDouble(offsetText.getText()));
                }
            } 
        });
        
        
        panel.add(pathIntroText);
        panel.add(pathText);
        panel.add(nameIntroText);
        panel.add(nameText);
        panel.add(path2IntroText);
        panel.add(path2Text);
        panel.add(bpmIntroText);
        panel.add(bpmText);
        panel.add(offsetIntroText);
        panel.add(offsetText);
        panel.add(optionList);
        panel.add(handOptionList);
        panel.add(convertButton);
        
        frame.add(panel);
        frame.setVisible(true);
    }
    
    
    
    public static void convert(String OGfilepath, String OGname, String filepath, double bpm, int option, int handOption, double offset){
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<Circle> circles = new ArrayList<Circle>();
        double sliderMultiplier = 1.4;
        try{
            BufferedReader br = new BufferedReader(new FileReader(OGfilepath+"\\"+OGname));


            String newLine = br.readLine();
            while(!newLine.equals("[HitObjects]")){
                newLine = br.readLine();
                //System.out.println(newLine);
                
                if(newLine.startsWith("SliderMultiplier:"))
                    sliderMultiplier = Double.parseDouble(newLine.split(":")[1]);
            }
            newLine = br.readLine();
            while(!newLine.equals("")){
                System.out.println(newLine);
                String[] numS = newLine.split(",");
                double[] nums = new double[numS.length];
                for(int x=0; x<5/*numS.length*/; x++){
                    nums[x] = Double.parseDouble(numS[x]);
                }
                Circle newCircle = new Circle(nums[0],nums[1],(nums[2] + offset)/(60000.0 / (bpm * 480.0)),nums[3],nums[4]);
                int circleType = Integer.parseInt(numS[3]);
                if(option == 0){
                    //make everything a circle
                    circles.add(newCircle);
                }
                else if((circleType-4)%8 == 0){
                    //spinner
                    circles.add(newCircle);
                }
                else if(circleType % 2 == 0){
                    if(option == 1){
                        //slider into sustain
                        newCircle.isSustain = true;
                        newCircle.repeat = Double.parseDouble(numS[6]);
                        newCircle.pixelLength = Double.parseDouble(numS[7]);
                        
                        
                        double beatDuration = 60000.0 / (480.0 *(60.0 / bpm));
                        double sliderDuration = (newCircle.pixelLength / (100.0 * sliderMultiplier)) * beatDuration * newCircle.repeat;

                        newCircle.sustainLength = sliderDuration;
                    }
                    else{
                        //slider into slider
                        newCircle.isSlider = true;
                        String[] newSplit = numS[5].split("\\|");
                        newCircle.sliderType = newSplit[0];

                        newCircle.curvePointsXs = new ArrayList<Double>();
                        newCircle.curvePointsYs = new ArrayList<Double>();
                        for(int i=0; i<newSplit.length-1; i++){
                            String[] xy = newSplit[i+1].split(":");
                            if(i>0 && newCircle.curvePointsXs.get(newCircle.curvePointsXs.size()-1) == Double.parseDouble(xy[0]) && newCircle.curvePointsYs.get(newCircle.curvePointsYs.size()-1) == Double.parseDouble(xy[1])){
                                //skip adding this one if it's a duplicate
                            }
                            else{
                                newCircle.curvePointsXs.add(Double.parseDouble(xy[0]));
                                newCircle.curvePointsYs.add(Double.parseDouble(xy[1]));
                            }

                        }

                        newCircle.repeat = Double.parseDouble(numS[6]);
                        newCircle.pixelLength = Double.parseDouble(numS[7]);


                        //lengthen short line sliders
                        double pathLength = 0.0;
                        double lastX = newCircle.x;
                        double lastY = newCircle.y;
                        for(int a = 0; a<newCircle.curvePointsXs.size(); a++){
                            pathLength += Math.sqrt(Math.pow(newCircle.curvePointsXs.get(a) - lastX,2) + Math.pow(newCircle.curvePointsYs.get(a) - lastY,2));
                            lastX = newCircle.curvePointsXs.get(a);
                            lastY = newCircle.curvePointsYs.get(a);
                        }
                        if(/*newCircle.sliderType.equals("L") && */pathLength < newCircle.pixelLength){
                            System.out.println("Extending...");
                            double diffX = newCircle.curvePointsXs.get(newCircle.curvePointsXs.size()-1) - newCircle.x;
                            double diffY = newCircle.curvePointsYs.get(newCircle.curvePointsXs.size()-1) - newCircle.y;
                            double lenDiff = newCircle.pixelLength - pathLength;

                            double maxLenX = (newCircle.pixelLength/pathLength)*diffX;
                            double currX = newCircle.curvePointsXs.get(newCircle.curvePointsXs.size()-1);
                            double currY = newCircle.curvePointsYs.get(newCircle.curvePointsYs.size()-1);
                            while(currX < maxLenX - diffX){
                                newCircle.curvePointsXs.add(currX + diffX);
                                newCircle.curvePointsYs.add(currY + diffY);
                                currX += diffX;
                                currY += diffY;
                            }
                            if(currX < maxLenX - 40){
                                newCircle.curvePointsXs.add((newCircle.pixelLength/pathLength)*diffX);
                                newCircle.curvePointsYs.add((newCircle.pixelLength/pathLength)*diffY);
                            }

                        }

                        if(option == 3){
                            //interpolate to make more notes
                            lastX = newCircle.x;
                            lastY = newCircle.y;
                            System.out.println(pathLength + "    " + newCircle.pixelLength);
                            for(int a = 0; a<newCircle.curvePointsXs.size(); a++){
                                System.out.println("dist: " + Math.sqrt(Math.pow(newCircle.curvePointsXs.get(a) - lastX,2) + Math.pow(newCircle.curvePointsYs.get(a) - lastY,2)));
                                if(Math.sqrt(Math.pow(newCircle.curvePointsXs.get(a) - lastX,2) + Math.pow(newCircle.curvePointsYs.get(a) - lastY,2)) > 40){
                                    System.out.println("Interpolating...");
                                    lastX = (newCircle.curvePointsXs.get(a)+lastX)/2.0;
                                    lastY = (newCircle.curvePointsYs.get(a)+lastY)/2.0;
                                    newCircle.curvePointsXs.add(a, lastX);
                                    newCircle.curvePointsYs.add(a, lastY);
                                    a++;
                                }
                            }
                        }

                        //do slider repeats
                        int dir = -1;
                        int count = (int)newCircle.repeat-1;
                        int initSize = newCircle.curvePointsXs.size();
                        double offsetPos = 20;
                        while(count > 0){
                            if(dir == -1){
                                for(int a = initSize-1; a>=0; a--){
                                    newCircle.curvePointsXs.add(newCircle.curvePointsXs.get(a) + offsetPos);
                                    newCircle.curvePointsYs.add(newCircle.curvePointsYs.get(a) - offsetPos);
                                }
                                newCircle.curvePointsXs.add(newCircle.x + offsetPos);
                                newCircle.curvePointsYs.add(newCircle.y - offsetPos);

                                dir = 1;
                                offsetPos = 0;
                            }
                            else if(dir == 1){
                                for(int a = 0; a<initSize; a++){
                                    newCircle.curvePointsXs.add(newCircle.curvePointsXs.get(a) + offsetPos);
                                    newCircle.curvePointsYs.add(newCircle.curvePointsYs.get(a) - offsetPos);
                                }
                                newCircle.curvePointsXs.add(newCircle.curvePointsXs.get(initSize-1) + offsetPos);
                                newCircle.curvePointsYs.add(newCircle.curvePointsXs.get(initSize-1) - offsetPos);

                                dir = -1;
                                offsetPos = 20;
                            }

                            count--;
                        }
                    
                    }
                }
                circles.add(newCircle);

                newLine = br.readLine();
            }
            br.close();
        }
        catch(Exception e){
            
        }
        
        System.out.println("--------");
        
        for(int i=0; i<circles.size()-1; i++){
            if(i<circles.size()-4&& 
                    Math.sqrt(Math.pow((int)circles.get(i).x - (int)circles.get(i+1).x,2) + Math.pow((int)circles.get(i).y - (int)circles.get(i+1).y,2)) < 40 && 
                    Math.sqrt(Math.pow((int)circles.get(i+1).x - (int)circles.get(i+2).x,2) + Math.pow((int)circles.get(i+1).y - (int)circles.get(i+2).y,2)) < 40 && 
                    Math.sqrt(Math.pow((int)circles.get(i+2).x - (int)circles.get(i+3).x,2) + Math.pow((int)circles.get(i+2).y - (int)circles.get(i+3).y,2)) < 40){
                int currC = i;
                circles.get(currC).isSlider = true;
                circles.get(currC).sliderType = "P";
                circles.get(currC).repeat = 1;
                circles.get(currC).curvePointsXs = new ArrayList<Double>();
                circles.get(currC).curvePointsYs = new ArrayList<Double>();
                while(i < circles.size()-2 && 
                        Math.sqrt(Math.pow((int)circles.get(i).x - (int)circles.get(i+1).x,2) + Math.pow((int)circles.get(i).y - (int)circles.get(i+1).y,2)) < 50){
                    circles.get(i).curvePointsXs.add(circles.get(i+1).x);
                    circles.get(i).curvePointsYs.add(circles.get(i+1).y);
                    circles.remove(i+1);
                }
                circles.get(i).pixelLength = Math.sqrt(Math.pow((int)circles.get(i).x - circles.get(i).curvePointsXs.get(circles.get(i).curvePointsXs.size()-1).intValue(),2) + Math.pow((int)circles.get(i).y - circles.get(i).curvePointsYs.get(circles.get(i).curvePointsXs.size()-1).intValue(),2));
            }
        }
        
        
        
        
        int handType = 2;
        try(FileWriter fileWriter = new FileWriter(filepath+"\\song.cues")) {
            fileWriter.write("{\r\n\"cues\":[");
            for(int i=0; i<circles.size()-1; i++){
                if(i>0 && Math.sqrt(Math.pow((int)circles.get(i).x - (int)circles.get(i-1).x,2) + Math.pow((int)circles.get(i).y - (int)circles.get(i-1).y,2)) >= 40){
                    if(handOption == 1){
                        if(handType == 1) handType = 2;
                        else handType = 1;
                    }
                    
                }
                
                Coords coords = coordsOf((int)circles.get(i).x,(int)circles.get(i).y);
                if(!circles.get(i).isSlider && !circles.get(i).isSustain){
                    fileWriter.write("{\r\n\"tick\": "+ (circles.get(i).time) +
                            ",\r\n\"tickLength\": 120,\r\n\"pitch\": "+coords.audicaCoord+
                            ",\r\n\"velocity\": "+circles.get(i).getAudicaHitsound()+",\r\n\"gridOffset\":{\r\n\"x\": "+coords.offsetX+
                            ",\r\n\"y\": "+coords.offsetY+"\r\n},\r\n\"handType\": "+handType+",\r\n\"behavior\": 0\r\n},");
                }
                else if(circles.get(i).isSustain){
                    fileWriter.write("{\r\n\"tick\": "+ (circles.get(i).time) +
                            ",\r\n\"tickLength\": "+circles.get(i).sustainLength+",\r\n\"pitch\": "+coords.audicaCoord+
                            ",\r\n\"velocity\": "+circles.get(i).getAudicaHitsound()+",\r\n\"gridOffset\":{\r\n\"x\": "+coords.offsetX+
                            ",\r\n\"y\": "+coords.offsetY+"\r\n},\r\n\"handType\": "+handType+",\r\n\"behavior\": 3\r\n},");
                }
                else{
                    double beatDuration = 60000.0 / (480.0 *(60.0 / bpm));
                    double sliderDuration = (circles.get(i).pixelLength / (100.0 * sliderMultiplier)) * beatDuration * circles.get(i).repeat;
                    
                        
                    
                    if(circles.get(i).pixelLength < 30){
                        //condense slider into a hold
                        fileWriter.write("{\r\n\"tick\": "+ (circles.get(i).time) +
                            ",\r\n\"tickLength\": 120,\r\n\"pitch\": "+coords.audicaCoord+
                            ",\r\n\"velocity\": "+circles.get(i).getAudicaHitsound()+",\r\n\"gridOffset\":{\r\n\"x\": "+coords.offsetX+
                            ",\r\n\"y\": "+coords.offsetY+"\r\n},\r\n\"handType\": "+handType+",\r\n\"behavior\": 0\r\n},");
                    }
                    else{
                        //make a slider
                        double numOfNotes = (double)(circles.get(i).curvePointsXs.size()+1);
                        double noteDuration = sliderDuration / numOfNotes;
                        
                        fileWriter.write("{\r\n\"tick\": "+ (circles.get(i).time) +
                            ",\r\n\"tickLength\": "+noteDuration+",\r\n\"pitch\": "+coords.audicaCoord+
                            ",\r\n\"velocity\": 1,\r\n\"gridOffset\":{\r\n\"x\": "+coords.offsetX+
                            ",\r\n\"y\": "+coords.offsetY+"\r\n},\r\n\"handType\": "+handType+",\r\n\"behavior\": 4\r\n},");

                        Coords coordsOfFollower = coordsOf((int)((circles.get(i).x + circles.get(i).curvePointsXs.get(0).intValue())/2.0),(int)((circles.get(i).y + circles.get(i).curvePointsYs.get(0).intValue())/2.0));
                        if(option==2){fileWriter.write("{\r\n\"tick\": "+ (circles.get(i).time + noteDuration) +
                            ",\r\n\"tickLength\": "+noteDuration+",\r\n\"pitch\": "+coordsOfFollower.audicaCoord+
                            ",\r\n\"velocity\": 2,\r\n\"gridOffset\":{\r\n\"x\": "+coordsOfFollower.offsetX+
                            ",\r\n\"y\": "+coordsOfFollower.offsetY+"\r\n},\r\n\"handType\": "+handType+",\r\n\"behavior\": 5\r\n},");}

                        for(int a = 0; a<circles.get(i).curvePointsXs.size()-1; a++){

                            coordsOfFollower = coordsOf(circles.get(i).curvePointsXs.get(a).intValue(),(int)circles.get(i).curvePointsYs.get(a).intValue());
                            fileWriter.write("{\r\n\"tick\": "+ (circles.get(i).time + (2*(a+1))*noteDuration) +
                                ",\r\n\"tickLength\": "+noteDuration+",\r\n\"pitch\": "+coordsOfFollower.audicaCoord+
                                ",\r\n\"velocity\": 2,\r\n\"gridOffset\":{\r\n\"x\": "+coordsOfFollower.offsetX+
                                ",\r\n\"y\": "+coordsOfFollower.offsetY+"\r\n},\r\n\"handType\": "+handType+",\r\n\"behavior\": 5\r\n},");

                            if(option==2){coordsOfFollower = coordsOf((int)((circles.get(i).curvePointsXs.get(a).intValue() + circles.get(i).curvePointsXs.get(a+1).intValue())/2.0),(int)((circles.get(i).curvePointsYs.get(a).intValue() + circles.get(i).curvePointsYs.get(a+1).intValue())/2.0));
                            fileWriter.write("{\r\n\"tick\": "+ (circles.get(i).time + (2*(a+1)+1)*noteDuration) +
                                ",\r\n\"tickLength\": "+noteDuration+",\r\n\"pitch\": "+coordsOfFollower.audicaCoord+
                                ",\r\n\"velocity\": 2,\r\n\"gridOffset\":{\r\n\"x\": "+coordsOfFollower.offsetX+
                                ",\r\n\"y\": "+coordsOfFollower.offsetY+"\r\n},\r\n\"handType\": "+handType+",\r\n\"behavior\": 5\r\n},");}

                        }
                        
                        coordsOfFollower = coordsOf(circles.get(i).curvePointsXs.get(circles.get(i).curvePointsXs.size()-1).intValue(),(int)circles.get(i).curvePointsYs.get(circles.get(i).curvePointsXs.size()-1).intValue());
                        fileWriter.write("{\r\n\"tick\": "+ (circles.get(i).time + (2*(circles.get(i).curvePointsXs.size()))*noteDuration) +
                            ",\r\n\"tickLength\": "+noteDuration+",\r\n\"pitch\": "+coordsOfFollower.audicaCoord+
                            ",\r\n\"velocity\": 2,\r\n\"gridOffset\":{\r\n\"x\": "+coordsOfFollower.offsetX+
                            ",\r\n\"y\": "+coordsOfFollower.offsetY+"\r\n},\r\n\"handType\": "+handType+",\r\n\"behavior\": 5\r\n},");

                        
                    }
                }
            }
            Coords coords = coordsOf((int)circles.get(circles.size()-1).x,(int)circles.get(circles.size()-1).y);
            fileWriter.write("{\r\n\"tick\": "+ (circles.get(circles.size()-1).time) +
                    ",\r\n\"tickLength\": 120,\r\n\"pitch\": "+coords.audicaCoord+
                    ",\r\n\"velocity\": 20,\r\n\"gridOffset\":{\r\n\"x\": "+coords.offsetX+
                    ",\r\n\"y\": "+coords.offsetY+"\r\n},\r\n\"handType\": "+(handType == 1 ? 2 : 1)+",\r\n\"behavior\": 0\r\n}");
            fileWriter.write("]}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
        
    }
    
    public static Coords coordsOf(int osuX, int osuY){
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
        
        Coords coords = new Coords();
        coords.x = x;
        coords.y = y;
        coords.audicaCoord = x + 12 * y;
        coords.offsetX = offsetX;
        coords.offsetY = -offsetY;
        
        return coords;
    }
    
}

class Coords{
    public int x;
    public int y;
    public int audicaCoord;
    public double offsetX;
    public double offsetY;
}

class Circle{
    public boolean isSlider = false;
    public boolean isSustain = false;
    
    public double x,y,time,type,hitSound;
    
    public String sliderType;
    public ArrayList<Double> curvePointsXs;
    public ArrayList<Double> curvePointsYs;
    public double repeat;
    public double pixelLength;
    
    public double sustainLength = 120.0;
    
    public Circle(double ix, double iy, double itime, double itype, double ihitSound){
        x=ix;
        y=iy;
        time=itime;
        type=itype;
        hitSound=ihitSound;
    }
    
    public Circle(double ix, double iy, double itime, double itype, double ihitSound, 
            String isliderType, ArrayList<Double> icurvePointsXs, ArrayList<Double> icurvePointsYs, double irepeat, double ipixelLength){
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
    
    public int getAudicaHitsound(){
        switch((int)hitSound){
                case 1: return 20;
                case 2: return 60;
                case 3: return 60;
                case 4: return 3;
                case 5: return 3;
                case 6: return 3;
                case 7: return 3;
                case 8: return 127;
                case 9: return 127;
                case 10: return 127;
                case 11: return 127;
                case 12: return 127;
                case 13: return 127;
                case 14: return 127;
                case 15: return 127;
                default: return 20;
        }
    }
    
}
