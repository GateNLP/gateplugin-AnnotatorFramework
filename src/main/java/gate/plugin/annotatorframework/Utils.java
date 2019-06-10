/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.plugin.annotatorframework;

import gate.util.GateRuntimeException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Johann Petrak <johann.petrak@gmail.com>
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
   * @param command
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

