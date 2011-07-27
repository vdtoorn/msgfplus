/***************************************************************************
  * Title:          
  * Author:         Sangtae Kim
  * Last modified:  
  *
  * Copyright (c) 2008-2009 The Regents of the University of California
  * All Rights Reserved
  * See file LICENSE for details.
  ***************************************************************************/
package msutil;

import java.util.HashMap;

/**
 * This class represents an enzyme.
 * 
 * @author sangtaekim
 */
public class Enzyme {
	
	/** True if the enzyme cleaves n-terminus, false otherwise. */
	private boolean isNTerm;
	
	/** Name of the enzyme. */
	private String name;
	
	/** Amino acid residues cleaved by the enzyme. */
	private char[] residues;
	private boolean[] isResidueCleavable; 
	
	// the probability that a peptide generated by this enzyme follows the cleavage rule
	// E.g. for trypsin, probability that a peptide ends with K or R
	private float peptideCleavageEfficiency;	
	
	// the probability that a neighboring amino acid follows the enzyme rule
	// E.g. for trypsin, probability that the preceding amino acid is K or R
	private float neighboringAACleavageEfficiency;
	
	/**
	 * Instantiates a new enzyme.
	 * 
	 * @param name the name
	 * @param residues the residues cleaved by the enzyme (String)
	 * @param isNTerm N term or C term (true if it cleaves N-term)
	 */
	private Enzyme(String name, String residues, boolean isNTerm) 
	{
		this.name = name;
		this.residues = new char[residues.length()];
		this.isResidueCleavable = new boolean[128];
		for(int i=0; i<residues.length(); i++)
		{
			char residue = residues.charAt(i);
			if(!Character.isUpperCase(residue))
			{
				System.err.println("Enzyme residues must be upper cases: " + residue);
				System.exit(-1);
			}
			this.residues[i] = residue;
			isResidueCleavable[residue] = true;
		}
		this.isNTerm = isNTerm;
	}

	/**
	 * Sets the neighboring amino acid efficiency as the probability that a neighboring amino acid follows the enzyme rule
	 * @param neighboringAACleavageEfficiency neighboring amino acid effieicncy
	 * @return this object
	 */
	private void setNeighboringAAEfficiency(float neighboringAACleavageEfficiency)
	{
		this.neighboringAACleavageEfficiency = neighboringAACleavageEfficiency;
	}

	/**
	 * Gets the neighboring amino acid efficiency
	 * @return neighboring amino acid efficiency
	 */
	public float getNeighboringAACleavageEffiency() { return neighboringAACleavageEfficiency; }
	
	/**
	 * Sets the peptide cleavage efficiency as the probability that a peptide generated by this enzyme follows the cleavage rule
	 * @param peptideCleavageEfficiency peptide cleagave efficiency
	 * @return this object
	 */
	private void setPeptideCleavageEffiency(float peptideCleavageEfficiency)
	{
		this.peptideCleavageEfficiency = peptideCleavageEfficiency;
	}
	
	/**
	 * Gets the peptide efficiency.
	 * @return peptide efficiency
	 */
	public float getPeptideCleavageEfficiency()	{ return peptideCleavageEfficiency; }
	
	/**
	 * Returns the name of the enzyme.
	 * 
	 * @return the name of the enzyme.
	 */
	public String getName()		{ return name; }
	
	/**
	 * Checks if this enzyme cleaves N term.
	 * 
	 * @return true, if it cleaves N term.
	 */
	public boolean isNTerm()	{ return isNTerm; }
	
	/**
	 * Checks if this enzyme cleaves C term.
	 * 
	 * @return true, if it cleaves C term.
	 */
	public boolean isCTerm()	{ return !isNTerm; }
	
	/**
	 * Checks if the amino acid is cleavable.
	 * 
	 * @param aa the amino acid
	 * 
	 * @return true, if aa is cleavable
	 */
	public boolean isCleavable(AminoAcid aa)
	{
		for(char r : this.residues)
			if(r == aa.getUnmodResidue())
				return true;
		return false;
	}

	/**
	 * Checks if the amino acid is cleavable.
	 * 
	 * @param residue amino acid residue
	 * 
	 * @return true, if residue is cleavable
	 */
	public boolean isCleavable(char residue)
	{
		return isResidueCleavable[residue];
	}
	
	/**
	 * Checks if the peptide is cleaved by the enzyme.
	 * @param p peptide
	 * @return true if p is cleaved, false otherwise.
	 */
	public boolean isCleaved(Peptide p)
	{
		AminoAcid aa;
		if(isNTerm)
			aa = p.get(0);
		else
			aa = p.get(p.size()-1);
		return isCleavable(aa.getResidue());
	}
	
	/**
	 * Returns the number of cleavaged termini
	 * @param annotation annotation (e.g. K.DLFGEK.I)
	 * @return the number of cleavaged termini
	 */
	public int getNumCleavedTermini(String annotation, AminoAcidSet aaSet)
	{
		int nCT = 0;
		String pepStr = annotation.substring(annotation.indexOf('.')+1, annotation.lastIndexOf('.'));
		Peptide peptide = aaSet.getPeptide(pepStr);
		if(this.isCleaved(peptide))
			nCT++;
		
		AminoAcid precedingAA = aaSet.getAminoAcid(annotation.charAt(0));
		AminoAcid nextAA = aaSet.getAminoAcid(annotation.charAt(annotation.length()-1));
		if(this.isNTerm)
		{
			if(nextAA == null || this.isCleavable(nextAA))
				nCT++;
		}
		else
		{
			if(precedingAA == null || this.isCleavable(precedingAA))
				nCT++;
		}
		
		return nCT;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	/**
	 * Gets the residues.
	 * 
	 * @return the residues
	 */
	public char[] getResidues()	{ return residues; }
	
	/** The Constant TRYPSIN. */
	public static final Enzyme TRYPSIN;
	
	/** The Constant CHYMOTRYPSIN. */
	public static final Enzyme CHYMOTRYPSIN;
	
	/** The Constant LysC. */
	public static final Enzyme LysC;
	
	/** The Constant LysN. */
	public static final Enzyme LysN;
	
	/** The Constant GluC. */
	public static final Enzyme GluC;
	
	/** The Constant ArgC. */
	public static final Enzyme ArgC;
	
	/** The Constant AspN. */
	public static final Enzyme AspN;
	
	private static HashMap<String,Enzyme> registeredEnzyme;
	
	public static void register(String name, Enzyme enzyme)
	{
		registeredEnzyme.put(name, enzyme);
	}
	
	public static Enzyme getEnzymeByName(String name)
	{
		return registeredEnzyme.get(name);
	}
	
	static {
		TRYPSIN = new Enzyme("Tryp", "KR", false);
//		TRYPSIN.setNeighboringAAEfficiency(0.9148273f);
//		TRYPSIN.setPeptideCleavageEffiency(0.98173124f);
		
//		TRYPSIN.setNeighboringAAEfficiency(0.9523f);
//		TRYPSIN.setPeptideCleavageEffiency(0.9742f);

		// Modified by Sangtae to boost the performance
		TRYPSIN.setNeighboringAAEfficiency(0.99999f);
		TRYPSIN.setPeptideCleavageEffiency(0.99999f);

		CHYMOTRYPSIN = new Enzyme("CHYMOTRYPSIN", "FYWL", false);
		
		LysC = new Enzyme("LysC", "K", false);
//		LysC.setNeighboringAAEfficiency(0.79f);
//		LysC.setPeptideCleavageEffiency(0.89f);
		LysC.setNeighboringAAEfficiency(0.999f);
		LysC.setPeptideCleavageEffiency(0.999f);
		
		LysN = new Enzyme("LysN", "K", true);
		LysN.setNeighboringAAEfficiency(0.79f);
		LysN.setPeptideCleavageEffiency(0.89f);
		
		GluC = new Enzyme("GluC","E",false);
		ArgC = new Enzyme("ArgC","R",false);
		AspN = new Enzyme("AspN","D",true);
		registeredEnzyme = new HashMap<String,Enzyme>();

		register("Tryp", TRYPSIN);
		register("CHYMOTRYPSIN", CHYMOTRYPSIN);
		register("LysC", LysC);
		register("LysN", LysN);
		register("GluC", GluC);
		register("ArgC", ArgC);
		register("AspN", AspN);
	}
}
