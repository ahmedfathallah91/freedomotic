/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.hello;

/**
 *
 * @author ahmed
 */

import java.io.File;
import java.io.InputStream;
import java.util.List;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.OutputStream;

public class JenaEngine {
	static private String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/**
	 * Charger un model a partir un fichier owl
	 * 
	 * @param args
	 *            + Entree: le chemin vers le fichier owl + Sortie: l'objet
	 *            model jena
	 */
	static public Model readModel(String inputDataFile) {
		// create an empty model
		Model model = ModelFactory.createDefaultModel();
		// use the FileManager to find the input file
		InputStream in = FileManager.get().open(inputDataFile);
		if (in == null) {
			System.out.println("Ontology file: " + inputDataFile + " not found");
			return null;
		}
		// read the RDF/XML file
		model.read(in, "");
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		
		return model;
	}

	/**
	 * Faire l'inference
	 * 
	 * @param args
	 *            + Entree: l'objet model Jena avec le chemin du fichier des
	 *            regles + Sortie: l'objet model infere Jena
	 */
	static public Model readInferencedModelFromRule(Model model, String inputRuleFile) {
		InputStream in = FileManager.get().open(inputRuleFile);
		if (in == null) {
			System.out.println("Rule File: " + inputRuleFile + " not found");
			return null;
		} else {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}
                
		List rules = Rule.rulesFromURL(inputRuleFile);
		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setDerivationLogging(true);
		reasoner.setOWLTranslation(true); // not needed in RDFS case
		reasoner.setTransitiveClosureCaching(true);
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		return inf;
	}

	/**
	 * Executer une requete
	 * 
	 * @param args
	 *            + Entree: l'objet model Jena avec une chaine des caracteres
	 *            SparQL + Sortie: le resultat de la requete en String
	 */
	static public String executeQuery(Model model, String queryString) {
		Query query = QueryFactory.create(queryString);
		// No reasoning
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			// Netbeans IDE automatically overrides this toString()
			public String toString() {
				return this.string.toString();
			}
		};
		ResultSetFormatter.out(output, results, query);
		return output.toString();
	}
}
