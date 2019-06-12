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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Controller;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.*;
import gate.lib.interaction.process.Process4StringStream;
import gate.util.Files;
import gate.util.GateRuntimeException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

@CreoleResource(
    name = "Run an annotator process as a command and interact via pipes.",
    comment = "(A short one-line description of this PR, suitable for a tooltip in the GUI)")
public class AF_Program 
        extends AbstractLanguageAnalyser
        implements ControllerAwarePR 
{

  private static final Logger LOGGER = Logger.getLogger(AF_Program.class);
  private static final long serialVersionUID = -2486627536702216228L;


  private String inputASName;
  public String getInputASName() {
    return inputASName;
  }
  @Optional
  @RunTime
  @CreoleParameter(comment = "The annotation set used for input annotations")
  public void setInputASName(String value) {
    this.inputASName = value;
  }

  private String containingAnnotationType;
  public String getContainingAnnotationType() {
    return containingAnnotationType;
  }
  @Optional
  @RunTime
  @CreoleParameter(comment = "The annotation type spanning segments to get anntoated")
  public void setContainingAnnotationType(String value) {
    this.containingAnnotationType = value;
  }
  
  private String inputAnnotationType;
  public String getInputAnnotationType() {
    return inputAnnotationType;
  }
  @Optional
  @RunTime
  @CreoleParameter(comment = "Existing annotations to use by the annotator")
  public void setInputAnnotationType(String value) {
    this.inputAnnotationType = value;
  }
  
  private String outputASName;
  public String getOutputASName() {
    return outputASName;
  }
  @Optional
  @RunTime
  @CreoleParameter(comment = "The annotation set used for output annotations")
  public void setOutputASName(String value) {
    this.outputASName = value;
  }

  private String commandLine;
  public String getCommandLine() {
    return commandLine;
  }
  @Optional
  @RunTime
  @CreoleParameter(comment = "The command to run to execute the program, can contain variables")
  public void setCommandLine(String value) {
    this.commandLine = value;
  }
  
  protected URL dataDirectory;
  @RunTime
  @CreoleParameter(comment = "The directory to use as working directory for the program")
  public void setDataDirectory(URL value) {
    dataDirectory = value;
  }
  public URL getDataDirectory() {
    return this.dataDirectory;
  }
  
  
  ObjectMapper mapper;
  Process4StringStream process;
  
  
  /**
   * Initialize this Run an annotator process as a command and interact via pipes..
   * @return this resource.
   * @throws ResourceInstantiationException if an error occurs during init.
   */
  @Override
  public Resource init() throws ResourceInstantiationException {
    LOGGER.debug("Run an annotator process as a command and interact via pipes. is initializing");

    // your initialization code here

    return this;
  }

  public void processItem(Document doc, Long fromOffset, Long toOffset, 
          AnnotationSet allanns, AnnotationSet outputAS) {
    // get the text
    int from = fromOffset.intValue();
    int to = toOffset.intValue();
    String txt = doc.getContent().toString().substring(from, to);
    AnnotationSet anns = null;
    if(anns != null) {
      anns = allanns.getContained(fromOffset, toOffset);
    }
    // convert the item to JASON, using a map representation with keys: text, anns, feats
    // where anns is a list of maps with keys from, to, features
    Map<String,Object> item = new HashMap<>();
    item.put("text", txt);
    item.put("features", doc.getFeatures());
    if(anns == null) {
      item.put("anns", null);
    } else {
      List<Map<String, Object>> annList = new ArrayList<>();
      for(Annotation ann : anns) {
        Map<String, Object> tmpmap = new HashMap<>();
        tmpmap.put("from", gate.Utils.start(ann).intValue());
        tmpmap.put("to", gate.Utils.end(ann).intValue());
        tmpmap.put("features", ann.getFeatures());
      }
      item.put("anns", annList);
    }
    String json = null;
    try {
      json = mapper.writeValueAsString(item);
    } catch (JsonProcessingException ex) {
      throw new GateRuntimeException("Could not convert information to json", ex);
    }      
    process.writeObject(json);
    String json_ret = (String)process.readObject();
    Object obj_ret = null;
    try {
      obj_ret = mapper.readValue(json_ret, Map.class);          
    } catch (IOException ex) {
      throw new GateRuntimeException("Could not interpret response json: "+json_ret, ex);
    }
    // we always expect a map as a response!
    Map<String,Object>retMap = (Map<String,Object>)obj_ret;
    // the return map should contain the following fields
    // status: must be "ok", (for now, we terminate if not)
    // features (document features to set/override)
    // anns: list of annotations to create
    String status = (String)retMap.get("status");
    if(status==null) {
      status = "UNKNOWN";
    }
    if (status.equals("ok")) {
      Map<String, Object> feats = (Map<String, Object>)retMap.get("features");
      if(feats != null) {
        for(String key : feats.keySet()) {
          doc.getFeatures().put(key, feats.get(key));
        }
      }
      List<Map<String, Object>> ret_anns = (List<Map<String, Object>>)retMap.get("anns");
      if(ret_anns != null) {
        for(Map<String, Object> ret_ann : ret_anns) {
          int from_off = (Integer)ret_ann.get("from");
          int to_off = (Integer)ret_ann.get("to");
          String anntype = (String)ret_ann.get("type");
          if(anntype==null) {
            anntype = "Annotation";
          }
          FeatureMap annfeatures = null;
          Map<String, Object> tmpfeats = (Map<String, Object>)ret_ann.get("features");
          if(tmpfeats != null) {
            annfeatures = gate.Utils.toFeatureMap(tmpfeats);
          } else {
            annfeatures = Factory.newFeatureMap();
          }
          gate.Utils.addAnn(outputAS, from_off, to_off, anntype, annfeatures);
        }
      }
    } else {
      String error = (String)retMap.get("error");
      if(error == null) {
        error = "UNKNOWN";
      }
      throw new GateRuntimeException("Non-ok status received, document="+doc.getName()+", error="+error);
    }
  }
  
  
  
  /**
   * Execute this Run an annotator process as a command and interact via pipes. over the current document.
   * @throws ExecutionException if an error occurs during processing.
   */
  @Override
  public void execute() throws ExecutionException {
    if(isInterrupted()) {
      throw new ExecutionInterruptedException("Execution of Run an annotator process as a command and interact via pipes. has been interrupted!");
    }
    interrupted = false;

    Document doc = getDocument();
    if(doc != null) {
      AnnotationSet inputAS = doc.getAnnotations(inputASName);
      AnnotationSet outputAS = doc.getAnnotations(outputASName);
      AnnotationSet inputAnns = null;
      if (getInputAnnotationType() != null && !getInputAnnotationType().isEmpty()) {
        inputAnns = inputAS.get(getInputAnnotationType());
      }

      if (getContainingAnnotationType() != null && !getContainingAnnotationType().isEmpty()) {
        // iterate over all the containing annotations and do whatever needed for those instead of the whole document
        AnnotationSet containingAnns = inputAS.get(getContainingAnnotationType());
        for (Annotation ann : containingAnns) {
          processItem(doc, gate.Utils.start(ann), gate.Utils.end(ann), inputAnns, outputAS);
        }
      } else{
        // use the whole document
        processItem(doc, 0L, doc.getContent().size(), inputAnns, outputAS);
      }
      
    }
  }

  @Override
  public void controllerExecutionStarted(Controller cntrlr) throws ExecutionException {
    mapper = new ObjectMapper();
    String cmdLine = getCommandLine();
    Map<String, String> settings = new HashMap<>();
    // create the settings specific for our run
    settings.put("datadirurl", getDataDirectory().toString());
    try {
      settings.put("datadirfile", Files.fileFromURL(getDataDirectory()).getCanonicalPath());
    } catch (IOException ex) {
      throw new GateRuntimeException("Could not get canonical name for data directory URL "+getDataDirectory(), ex);
    }
    try {
      settings.put("cwd", new File(".").getCanonicalPath());
    } catch (IOException ex) {
      throw new GateRuntimeException("Could not get canonical name for current directory", ex);
    }
    cmdLine = Utils.substituteCommand(cmdLine, settings);
    List<String> cmdList = Utils.splitCommand(cmdLine);
    Map<String,String> env = new HashMap<>();
    File dataDir = Files.fileFromURL(getDataDirectory());
    process = Process4StringStream.create(dataDir, env, cmdList);
  }

  @Override
  public void controllerExecutionFinished(Controller cntrlr) throws ExecutionException {
    process.writeObject("STOP");
    process.stop();
  }

  @Override
  public void controllerExecutionAborted(Controller cntrlr, Throwable thrwbl) throws ExecutionException {
    process.writeObject("STOP");
    process.stop();
    throw new GateRuntimeException("Got exception during processing", thrwbl);
  }

}

