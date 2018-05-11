package model; 
/**
 * 
 * @author Di Paolo, Ovidi, Venditti
 * @version 1.0
 *
 */

import vo.UUID;
import controller.SessionDataHandler;


public class Page {
	
	//variabili istanza
	private UUID id;
	private SessionDataHandler session;
	private Integer pageNumber;
	private PageScan scan;
	private PageTranscription transcription;
	
	//costruttore
	//TODO
	public Page() {
		
	}
	
	
	//metodi get e set
	
	public UUID getID() {
		return this.id;
	}
	
	public Integer getPageNumber() {
		return this.pageNumber;
	}
	
	public PageScan getScan() {
		return this.scan;
	}
	
	public PageTranscription getTranscription() {
		return this.transcription;
	}
	
	public void setID(UUID id) {
		this.id=id;
	}
	
	public void setPageNumber(int n) {
		this.pageNumber=n;
	}
	
	public void setScan(PageScan ps) {
		this.scan=ps;
	}
	
	public void setTranscription(PageTranscription pt) {
		this.transcription=pt;
	}
	
}