/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class used for ANSI Color manipulation in a console supporting ANSI color codes */
public class AnsiColor {

    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30;40;1m";
    public static final String RED = "\u001B[31;40;1m";
    public static final String GREEN = "\u001B[32;40;1m";
    public static final String YELLOW = "\u001B[33;40;1m";
    public static final String BLUE = "\u001B[34;40;1m";
    public static final String WHITE = "\u001B[37;40;1m";
    public static final String DEFAULT = "\u001B[39;1m";


    /**
     * Parses a string with ANSI color codes based on the input
     * @param input the input string
     * @return the parsed ANSI string
     */
    public static String ParseColors(String input){
        String ret = input;
        Pattern regexChecker = Pattern.compile(":\\S+,\\S+:");
        Matcher regexMatcher = regexChecker.matcher(input);
        while(regexMatcher.find()){
            if(regexMatcher.group().length() != 0){
                String sub = regexMatcher.group().trim();
                sub = sub.replace(":", "");
                String[] colors = sub.split(",");

                ret = (colors[1].equalsIgnoreCase("N")) ?
                        ret.replace(regexMatcher.group().trim(), "\u001B[3" + getColorID(colors[0]) + ";1m")
                        :
                        ret.replace(regexMatcher.group().trim(), "\u001B[3" + getColorID(colors[0]) + ";4" + getColorID(colors[1]) + ";1m");
                ret = ret.replace("[RC]", AnsiColor.WHITE);
            }
        }
        ret = ret + AnsiColor.RESET; return ret;
    }

    /**
     * Internal function for getting a colors value
     * @param color The color as test
     * @return The colors integral value
     */
    private static int getColorID(String color){
        if(color.equalsIgnoreCase("BLACK")){
            return 0;
        }else if(color.equalsIgnoreCase("RED")){
            return 1;
        }else if(color.equalsIgnoreCase("GREEN")){
            return 2;
        }else if(color.equalsIgnoreCase("YELLOW")){
            return 3;
        }else if(color.equalsIgnoreCase("BLUE")){
            return 4;
        }else if(color.equalsIgnoreCase("WHITE")){
            return 7;
        }
        return 7;
    }
}