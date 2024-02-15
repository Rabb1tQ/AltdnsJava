package com.rabbitq;


import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.io.file.FileReader;
import com.beust.jcommander.JCommander;
import com.rabbitq.entity.TargetOptionsEntity;

import java.io.File;
import java.util.*;


public class Altdns {

    public static void main(String[] args) {
        printBanner();

        TargetOptionsEntity targetOptionsEntity = new TargetOptionsEntity();
        JCommander commander = JCommander.newBuilder()
                .addObject(targetOptionsEntity)
                .build();
        try {
            commander.parse(args);
        } catch (Exception e){
            commander.usage();
            return;
        }
        if (targetOptionsEntity.isHelp()) {
            commander.usage();
            return;
        }
        FileReader fileReader = new FileReader(targetOptionsEntity.getWordlist());
        List<String> alterationWords = fileReader.readLines();
        insertAllIndexes(targetOptionsEntity.getInput(), targetOptionsEntity.getOutput(), alterationWords);
        insertNumberSuffixSubdomains(targetOptionsEntity.getInput(), targetOptionsEntity.getOutput());
        insertDashSubdomains(targetOptionsEntity.getInput(), targetOptionsEntity.getOutput(), alterationWords);
        joinWordsSubdomains(targetOptionsEntity.getInput(), targetOptionsEntity.getOutput(), alterationWords);
        textDuplicateRemover(targetOptionsEntity.getInput(), targetOptionsEntity.getOutput());
    }

    public static void insertNumberSuffixSubdomains(String input, String outputTmp) {
        FileReader fileReader = new FileReader(input);
        List<String> listInput = fileReader.readLines();
        FileAppender appender = new FileAppender(new File(outputTmp), 16, true);
        for (int s = 0; s < listInput.size(); s++) {
            String line = listInput.get(s);
            String[] parts = line.trim().split("\\.");
            List<String> currentSub = new ArrayList<>();
            Map<String, String> ext = new HashMap<>();
            if (parts.length > 2) {
                for (int i = 0; i < parts.length - 2; i++) {
                    currentSub.add(parts[i]);
                }
                ext.put("domain", parts[parts.length - 2]);
                ext.put("suffix", parts[parts.length - 1]);
            } else break;
            for (int word = 0; word < 10; word++) {
                for (int index = 0; index < currentSub.size(); index++) {
                    // add word-NUM
                    String originalSub = currentSub.get(index);
                    currentSub.set(index, currentSub.get(index) + "-" + word);
                    // join the array to make into actual subdomain (aa.bb.cc)
                    String actualSub = String.join(".", currentSub);
                    // save full URL as line in file
                    String fullUrl = String.format("%s.%s.%s", actualSub, ext.get("domain"), ext.get("suffix"));
                    appender.append(fullUrl);
                    currentSub.set(index, originalSub);

                    // add wordNUM
                    originalSub = currentSub.get(index);
                    currentSub.set(index, currentSub.get(index) + word);
                    // join the array to make into actual subdomain (aa.bb.cc)
                    actualSub = String.join(".", currentSub);
                    // save full URL as line in file
                    fullUrl = String.format("%s.%s.%s", actualSub, ext.get("domain"), ext.get("suffix"));
                    appender.append(fullUrl);
                    currentSub.set(index, originalSub);
                }
            }
        }
        appender.flush();
        appender.toString();
    }

    public static void insertAllIndexes(String input, String outputTmp, List<String> alterationWords) {
        try {
            FileReader fileReader = new FileReader(input);
            List<String> listInput = fileReader.readLines();
            FileAppender appender = new FileAppender(new File(outputTmp), 16, true);
            // BufferedWriter wp = new BufferedWriter(new FileWriter(outputTmp, true));
            for (int s = 0; s < listInput.size(); s++) {
                String line = listInput.get(s);
                String[] parts = line.trim().split("\\.");
                List<String> currentSub = new ArrayList<>();
                Map<String, String> ext = new HashMap<>();
                if (parts.length > 2) {
                    for (int i = 0; i < parts.length - 2; i++) {
                        currentSub.add(parts[i]);
                    }
                    ext.put("domain", parts[parts.length - 2]);
                    ext.put("suffix", parts[parts.length - 1]);
                } else break;
                for (String word : alterationWords) {
                    for (int index = 0; index < currentSub.size(); index++) {
                        currentSub.add(index, word);
                        String actualSub = String.join(".", currentSub);
                        String fullUrl = String.format("%s.%s.%s", actualSub, ext.get("domain"), ext.get("suffix"));
                        if (!actualSub.endsWith(".")) {
                            appender.append(fullUrl);
                        }
                        currentSub.remove(index);
                    }
                    currentSub.add(word);
                    String actualSub = String.join(".", currentSub);
                    String[] domainParts = actualSub.split("\\.");
                    String fullUrl = String.format("%s.%s.%s", actualSub, ext.get("domain"), ext.get("suffix"));
                    if (currentSub.get(0).length() > 0) {
                        appender.append(fullUrl);

                        // writeDomain(wp, String.format("%s.%s.%s%n", actualSub, domainParts[domainParts.length - 2], domainParts[domainParts.length - 1]));
                    }
                    currentSub.remove(currentSub.size() - 1);
                }
            }
            appender.flush();
            appender.toString();
        } catch (IORuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertDashSubdomains(String input, String outputTmp, List<String> alterationWords) {
        FileReader fileReader = new FileReader(input);
        List<String> listInput = fileReader.readLines();
        FileAppender appender = new FileAppender(new File(outputTmp), 16, true);
        for (int s = 0; s < listInput.size(); s++) {
            String line = listInput.get(s);
            String[] parts = line.trim().split("\\.");
            List<String> currentSub = new ArrayList<>();
            Map<String, String> ext = new HashMap<>();
            if (parts.length > 2) {
                for (int i = 0; i < parts.length - 2; i++) {
                    currentSub.add(parts[i]);
                }
                ext.put("domain", parts[parts.length - 2]);
                ext.put("suffix", parts[parts.length - 1]);
            } else break;
            for (String word : alterationWords) {
                for (int index = 0; index < currentSub.size(); index++) {
                    // add word- to subdomain
                    String originalSub = currentSub.get(index);
                    currentSub.set(index, currentSub.get(index) + "-" + word.trim());
                    // join the array to make into actual subdomain (aa.bb.cc)
                    String actualSub = String.join(".", currentSub);
                    // save full URL as line in file
                    String fullUrl = String.format("%s.%s.%s", actualSub, ext.get("domain"), ext.get("suffix"));
                    if (currentSub.get(0).length() > 0 && !actualSub.startsWith("-")) {
                        appender.append(fullUrl);
                    }
                    currentSub.set(index, originalSub);

                    // add -word to subdomain
                    currentSub.set(index, word.trim() + "-" + originalSub);
                    actualSub = String.join(".", currentSub);
                    // save second full URL as line in file
                    fullUrl = String.format("%s.%s.%s", actualSub, ext.get("domain"), ext.get("suffix"));
                    if (!actualSub.endsWith("-")) {
                        appender.append(fullUrl);
                    }
                    currentSub.set(index, originalSub);
                }
            }
        }
        appender.flush();
        appender.toString();

    }

    public static void joinWordsSubdomains(String input, String outputTmp, List<String> alterationWords) {
        FileReader fileReader = new FileReader(input);
        List<String> listInput = fileReader.readLines();
        FileAppender appender = new FileAppender(new File(outputTmp), 16, true);
        for (int s = 0; s < listInput.size(); s++) {
            String line = listInput.get(s);
            String[] parts = line.trim().split("\\.");
            List<String> currentSub = new ArrayList<>();
            Map<String, String> ext = new HashMap<>();
            if (parts.length > 2) {
                for (int i = 0; i < parts.length - 2; i++) {
                    currentSub.add(parts[i]);
                }
                ext.put("domain", parts[parts.length - 2]);
                ext.put("suffix", parts[parts.length - 1]);
            } else break;
            for (String word : alterationWords) {
                for (int index = 0; index < currentSub.size(); index++) {
                    // add word to subdomain
                    String originalSub = currentSub.get(index);
                    currentSub.set(index, currentSub.get(index) + word.trim());
                    // join the array to make into actual subdomain (aa.bb.cc)
                    String actualSub = String.join(".", currentSub);
                    // save full URL as line in file
                    String fullUrl = String.format("%s.%s.%s", actualSub,  ext.get("domain"), ext.get("suffix"));
                    appender.append(fullUrl);
                    currentSub.set(index, originalSub);

                    // add word to subdomain at the beginning
                    currentSub.set(index, word.trim() + originalSub);
                    actualSub = String.join(".", currentSub);
                    // save second full URL as line in file
                    fullUrl = String.format("%s.%s.%s", actualSub,  ext.get("domain"), ext.get("suffix"));
                    appender.append(fullUrl);
                    currentSub.set(index, originalSub);
                }
            }
        }
        appender.flush();
        appender.toString();
    }

    public static void textDuplicateRemover(String input, String outputTmp){
        FileReader fileReader = new FileReader(input);
        List<String> listInput = fileReader.readLines();
        FileAppender appender = new FileAppender(new File(outputTmp), 16, true);
        Set<String> lines = new HashSet<>();

        for (int s = 0; s < listInput.size(); s++) {
            String line = listInput.get(s);
            if (lines.add(line)) {
                continue;
            }
        }
        for (int i=0;i<lines.size();i++){
            appender.append(lines.toArray()[i].toString());
        }
        appender.flush();
        appender.toString();
    }
    public static void printBanner() {
        System.out.println(
                "██████╗ ██╗   ██╗    ██████╗  █████╗ ██████╗ ██████╗ ██╗████████╗ ██████╗ \n" +
                "██╔══██╗╚██╗ ██╔╝    ██╔══██╗██╔══██╗██╔══██╗██╔══██╗██║╚══██╔══╝██╔═══██╗\n" +
                "██████╔╝ ╚████╔╝     ██████╔╝███████║██████╔╝██████╔╝██║   ██║   ██║   ██║\n" +
                "██╔══██╗  ╚██╔╝      ██╔══██╗██╔══██║██╔══██╗██╔══██╗██║   ██║   ██║▄▄ ██║\n" +
                "██████╔╝   ██║       ██║  ██║██║  ██║██████╔╝██████╔╝██║   ██║   ╚██████╔╝\n" +
                "╚═════╝    ╚═╝       ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚═════╝ ╚═╝   ╚═╝    ╚══▀▀═╝ \n" +
                "                                                                          ");
    }
}