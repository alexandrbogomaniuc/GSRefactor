package com.betsoft.casino.mp.clashofthegods.model;

import java.io.*;

public class CollectResultData {
    public static void main(String[] args) throws IOException {
        PrintWriter pwFastModel = new PrintWriter(new OutputStreamWriter(new
                FileOutputStream("/home/ksard/Work/prj/mp/Tests/CoG/resultsFastModel.csv"), "Cp866"));
        PrintWriter pwLongModel = new PrintWriter(new OutputStreamWriter(new
                FileOutputStream("/home/ksard/Work/prj/mp/Tests/CoG/resultsLongModel.csv"), "Cp866"));

        File file = new File("/home/ksard/Work/prj/mp/Tests/CoG/843_Results_Local_Tests/");
        File[] files = file.listFiles();
        DataInputStream dis = null;
        BufferedInputStream bis = null;

        StringBuilder resString = new StringBuilder();


        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            FileInputStream fis = new FileInputStream(file1);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            String name = file1.getName();
            boolean isFastResults  = name.contains("model_");
            resString.setLength(0);

            while (dis.available() != 0) {
                if(isFastResults) {
                    String record = dis.readLine();
                    if (record.contains("enemy:")) {
                        resString.append(record.substring("enemy:".length())).append("\t");
                    }
                    if (record.contains("coinInCents:")) {
                        resString.append(record.substring("coinInCents:".length())).append("\t");
                    }

                    if (record.contains("totalNumberShots:")) {
                        resString.append(record.substring("totalNumberShots:".length())).append("\t");
                    }

                    if (record.contains("weaponName:")) {
                        resString.append(record.substring("weaponName:".length())).append("\t");
                    }

                    if (record.contains("needPaidShots:")) {
                        resString.append(record.substring("needPaidShots:".length())).append("\t");
                    }

                    if (record.contains("needTestX2Mode:")) {
                        resString.append(record.substring("needTestX2Mode:".length())).append("\t");
                    }

                }else{

                }
            }

            if(isFastResults){
                pwFastModel.println(resString);
            }else{
                pwLongModel.println(resString);
            }
        }

        pwFastModel.flush();
        pwLongModel.flush();
        pwFastModel.close();
        pwLongModel.close();
    }
}
