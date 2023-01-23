/*
 * Copyright (c) 2019- The University Of Sheffield.
 *
 * This file is part of gateplugin-AnnotatorFramework
 * (see https://github.com/GateNLP/gateplugin-AnnotatorFramework).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
package gate.plugin.annotatorframework;

import gate.util.GateRuntimeException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Johann Petrak &lt;johann.petrak@gmail.com&gt;
 */
public class Utils {
  
  /**
   * Return the operating system type of the system we are running under.
   * @return the operating system type, LINUXLIKE or WINDOWSLIKE
   */
  public static OsType getOsType() {
    boolean linuxLike = System.getProperty("file.separator").equals("/");
    boolean windowsLike = System.getProperty("file.separator").equals("\\");
    if(linuxLike) {
      return OsType.LINUXLIKE;
    } else if(windowsLike) {
      return OsType.WINDOWSLIKE;
    } else {
      throw new GateRuntimeException("It appears this OS is not supported");
    }    
  }
  
  /**
   * Get the sheel command file extension for the operating system.
   * @return either .sh or .cmd
   */
  public static String getShellExtension() {
    return getOsType() == OsType.LINUXLIKE ? ".sh" : ".cmd";
  }
  
  /**
   * Known types of operating system
   */
  public static enum OsType {
    WINDOWSLIKE,
    LINUXLIKE
  }
  
  /**
   * Split a command string into the command and parameters
   * 
   * @param command the command to split
   * @return a list containing the command and all parameters
   */
  public static List<String> splitCommand(String command) {
    return Arrays.asList(command.trim().split("\\s+"));
  }
 
  /**
   * Substitute variables in the command string.
   * 
   * @param command with variables 
   * @param settings a map with settings to use
   * @return command with variables replaced with their actual values
   */
  public static String substituteCommand(String command, Map<String,String> settings) {
    command = gate.Utils.replaceVariablesInString(command, System.getProperties(), settings);
    return command;
  }
  
  
}


