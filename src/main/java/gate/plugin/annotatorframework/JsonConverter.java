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

import gate.Document;
import gate.DocumentExporter;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.Plugin;
import gate.util.GateException;
import gate.util.GateRuntimeException;
import java.io.ByteArrayOutputStream;


// TODO: this should probably get replaced by a proper JSON converter class
// which maybe should also be a GATE Format and have the API we need. 
// The current format JSON is too restricted to really represent GATE documents
// and does not support the API we need. 
// Another thing to look at is the delta-representation used by the Python plugin

/**
 * Small wrapper around the Format JSON exporter to simplify conversion
 * 
 * @author Johann Petrak &lt;johann.petrak@gmail.com&gt;
 */
public class JsonConverter {
  FeatureMap options;
  DocumentExporter exporter;
  public JsonConverter(FeatureMap options) {
    this.options = options;
    try {
      // make sure the plugin is registered
      Gate.getCreoleRegister().registerPlugin(    
              new Plugin.Maven("uk.ac.gate.plugins","format-json","8.6"));
    } catch (GateException ex) {
      throw new GateRuntimeException("Could not register format-json plugin", ex);
    }
    exporter = DocumentExporter.getInstance("gate.corpora.export.GATEJsonExporter");
  }
  
  public JsonConverter() {
    this(Factory.newFeatureMap());
  }
  
  
  public String doc2json(Document doc, FeatureMap options) {
    FeatureMap opts = gate.Utils.toFeatureMap(options);
    for(Object k : this.options.keySet()) {
      if (!opts.containsKey(k)) {
        opts.put(k, this.options.get(k));
      }
    }
    return doc2json_worker(doc, opts);
  }

  public String doc2json(Document doc) {
    return doc2json_worker(doc, options);
  }
  
  private String doc2json_worker(Document doc, FeatureMap opts) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      exporter.export(doc, os, opts);
      return os.toString("UTF-8");
    } catch (Exception ex) {
      throw new GateRuntimeException("Could not convert document to json: "+doc.getName(), ex);
    }    
  }
  
}
