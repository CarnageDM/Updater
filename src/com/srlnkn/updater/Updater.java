package com.srlnkn.updater;

import com.srlnkn.updater.utils.Configs;
import com.srlnkn.updater.utils.JarUtils;
import com.srlnkn.updater.utils.Utils;
import org.objectweb.asm.tree.ClassNode;


import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Author : NKN & JJ & Krazy Meerkat
 */
public class Updater {

    public Integer CustomRevison = 0; //This must be left at 0 when not in use.

    public Updater(){
        try {
            String rsLink = "http://oldschool11.runescape.com/";
            String pageSource = Utils.getPage(rsLink);
            Pattern archiveRegex = Pattern.compile("archive=(.*) ");
            Matcher archiveMatcher = archiveRegex.matcher(pageSource);
            Pattern codeRegex = Pattern.compile("code=(.*) ");
            Matcher codeMatcher = codeRegex.matcher(pageSource);
            if (archiveMatcher.find() && codeMatcher.find()) {
                String jarLink = rsLink + archiveMatcher.group(1);
                //System.out.println("Jar location: " + jarLink);
                String codeName = codeMatcher.group(1).replaceAll(".class", "");
                //System.out.println("Code name: " + codeName);
                //System.out.println("\nLoading parameters...");
                Pattern paramRegex = Pattern.compile("<param name=\"([^\\s]+)\"\\s+value=\"([^>]*)\">");
                Matcher paramMatcher = paramRegex.matcher(pageSource);
                while (paramMatcher.find()) {
                    String key = paramMatcher.group(1);
                    String value = paramMatcher.group(2);
                    //System.out.printf("%-20s %s", key, value + "\n");
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put(key, value);
                }
                File dir = new File(Configs.HOME);
                if(!dir.exists())
                    dir.mkdir();
                File tempCachedClient = new File(Configs.HOME, "client.jar");
                if (CustomRevison > 0) {
                    tempCachedClient = new File(Configs.HOME, "client"+CustomRevison+".jar");
                }
                Boolean Downloaded = false;
                if ((!tempCachedClient.exists()) && (CustomRevison < 1)) {
                    Downloaded = true;
                    System.out.println("\n//Downloading Initial Client");
                    Utils.downloadFile(jarLink, tempCachedClient);
                }
                HashMap<String, ClassNode> tempClassMap = JarUtils.parseJar(new JarFile(tempCachedClient));
                File cachedClient = new File(Configs.HOME, "client"+JarUtils.getRevision(tempClassMap.get("client"))+".jar");
                if((JarUtils.isUpdated(tempClassMap.get("client"), jarLink)) && (CustomRevison < 1)){
                    System.out.println("\n//Downloading Client "+JarUtils.getRevision(tempClassMap.get("client")));
                    cachedClient = new File(Configs.HOME, "client"+JarUtils.getRevision(tempClassMap.get("client"))+".jar");
                    Utils.downloadFile(jarLink, cachedClient);
                    Utils.copyFileUsingFileChannels(cachedClient, tempCachedClient);
                } else if (Downloaded) {
                    System.out.println("\n//Using Client "+JarUtils.getRevision(tempClassMap.get("client")));
                    cachedClient = new File(Configs.HOME, "client"+JarUtils.getRevision(tempClassMap.get("client"))+".jar");
                    Utils.copyFileUsingFileChannels(tempCachedClient, cachedClient);
                } else if (CustomRevison > 0) {
                    System.out.println("\n//Using Client "+CustomRevison);
                    cachedClient = new File(Configs.HOME, "client"+CustomRevison+".jar");
                } else {
                    System.out.println("\n//Using Client "+JarUtils.getRevision(tempClassMap.get("client")));
                    cachedClient = new File(Configs.HOME, "client"+JarUtils.getRevision(tempClassMap.get("client"))+".jar");
                }

                if (cachedClient.exists()) { //Only continue if the final client exists
                    HashMap<String, ClassNode> ClassMap = JarUtils.parseJar(new JarFile(cachedClient));

                    System.out.println("{*");
                    System.out.println("**  SRL's Un-Named Updater");
                    System.out.println("**    Developed by");
                    System.out.println("**      NKN, Krazy_Meerkat and JJ.");
                    System.out.println("*}");
                    System.out.println(" ");
                    System.out.println("const");
                    if (CustomRevison > 0) {
                        System.out.println(" ReflectionRevision = '"+CustomRevison+"';");
                    } else {
                        System.out.println(" ReflectionRevision = '"+JarUtils.getRevision(ClassMap.get("client"))+"';");
                    }
                    System.out.println(" ");

                } else {
                    if (CustomRevison > 0) { //Most likely reason for not finding our Client
                        System.out.println(" Couldn't find client"+CustomRevison+".jar in directory AppData/Roaming/SRLUpdater/");
                    } else {
                        System.out.println(" Couldn't find client"+JarUtils.getRevision(tempClassMap.get("client"))+".jar in directory AppData/Roaming/SRLUpdater/");
                    }
                }

            }



        } catch (Exception e) {
            System.out.println("Error constructing client");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading... please check your internet connection.", "Error loading..", JOptionPane.ERROR_MESSAGE);
        }
    }
}
