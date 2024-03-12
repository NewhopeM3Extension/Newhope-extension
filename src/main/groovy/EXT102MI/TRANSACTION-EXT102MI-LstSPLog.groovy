/*
 ***************************************************************
 *                                                             *
 *                           NOTICE                            *
 *                                                             *
 *   THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS             *
 *   CONFIDENTIAL INFORMATION OF INFOR AND/OR ITS AFFILIATES   *
 *   OR SUBSIDIARIES AND SHALL NOT BE DISCLOSED WITHOUT PRIOR  *
 *   WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND       *
 *   ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH  *
 *   THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.            *
 *   ALL OTHER RIGHTS RESERVED.                                *
 *                                                             *
 *   (c) COPYRIGHT 2020 INFOR.  ALL RIGHTS RESERVED.           *
 *   THE WORD AND DESIGN MARKS SET FORTH HEREIN ARE            *
 *   TRADEMARKS AND/OR REGISTERED TRADEMARKS OF INFOR          *
 *   AND/OR ITS AFFILIATES AND SUBSIDIARIES. ALL RIGHTS        *
 *   RESERVED.  ALL OTHER TRADEMARKS LISTED HEREIN ARE         *
 *   THE PROPERTY OF THEIR RESPECTIVE OWNERS.                  *
 *                                                             *
 ***************************************************************
 */
 
import groovy.lang.Closure;
import groovy.json.JsonSlurper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException
import java.time.ZoneId;
 
 
 /*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *                  20231025  RUSSELM      
 *            
 */
 
  /*
  * Search item
 */
public class LstSPLog extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  private String cono;
  private String lmdt;
  private String lmtm;
  private String itno;
  private String suno;
  private String sunm;
  private String pupr;
  private String fvdt;
  private String crud;
  private String emal;
  private String dtxx;
  private String site;
  private String sitt;
  
  private int XXCONO;
  private int currentDate;

  private List lstEXTSPL;

  
  public LstSPLog(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
    this.miCaller = miCaller;
    this.logger = logger;
    this.program = program;
    this.ion = ion;
  }
  
  public void main() {
    
    //Set Date Variables
  	ZoneId zid = ZoneId.of("Australia/Brisbane"); 
		currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
		
    //Fetch input fields from MI
    lmdt = mi.inData.get("LMDT") == null ? '' : mi.inData.get("LMDT").trim();
  	if (lmdt == "?") {
  	  lmdt = "";
  	} 
  	
  	//Perform validation on input
    if (!validateInput()) {
      return;
    }

    lstEXTSPL = new ArrayList();
    
    // Search on EXTSPL by lmdt via database query.
    DBAction queryEXTSPL = database.table("EXTSPL")
      .index("10")
      .selection("EXCONO", "EXSUNO", "EXITNO", "EXPUPR", "EXFVDT", "EXEMAL", "EXLMDT","EXLMTM", "EXDTXX", "EXCRUD", "EXSITE", "EXSITT").build();
    DBContainer EXTSPL = queryEXTSPL.getContainer();
	  EXTSPL.set("EXLMDT", lmdt);
  	queryEXTSPL.readAll(EXTSPL, 999, callbackEXTSPL);


    if (lstEXTSPL.size() > 0) {
      for (int i=0;i<lstEXTSPL.size();i++) {
        Map<String, String> record = (Map<String, String>) lstEXTSPL[i];
        mi.outData.put("CONO", record.get("CONO").toString());
        mi.outData.put("SUNO", record.get("SUNO").toString());
        mi.outData.put("ITNO", record.get("ITNO").toString());
        mi.outData.put("PUPR", record.get("PUPR").toString());
        mi.outData.put("FVDT", record.get("FVDT").toString());
        mi.outData.put("EMAL", record.get("EMAL").toString());
        mi.outData.put("LMDT", record.get("LMDT").toString());
        mi.outData.put("LMTM", record.get("LMTM").toString());
        mi.outData.put("DTXX", record.get("DTXX").toString());
        mi.outData.put("CRUD", record.get("CRUD").toString());
        mi.outData.put("SITE", record.get("SITE").toString());
        mi.outData.put("SITT", record.get("SITT").toString());
        mi.write();
      }
    }
  }
  /*
   * validateInput - Validate all the input fields
   * @return false if there is any error
   *         true if pass the validation
   */
  boolean validateInput(){
  
    //Validate LMDT
		boolean isDateParsed = false;
		if (!lmdt.isEmpty()) {
			if(lmdt.length() != 8 ){
				mi.error("Date length must be 8");
				return false;
			}
			try {
				isDateParsed = LocalDate.parse(lmdt, DateTimeFormatter.ofPattern("yyyyMMdd"))
			} catch (DateTimeParseException e) {
				mi.error("Incorrect date format yyyyMMdd");
				return false;
			}
		} else {
			lmdt = currentDate;
		}
  	
    return true;
  }
  
  Closure<?> callbackEXTSPL = { DBContainer resultEXTSPL ->
    cono = resultEXTSPL.get("EXCONO").toString().trim();
    suno = resultEXTSPL.get("EXSUNO").toString().trim();
    itno = resultEXTSPL.get("EXITNO").toString().trim();
    pupr = resultEXTSPL.get("EXPUPR").toString().trim();
    fvdt = resultEXTSPL.get("EXFVDT").toString().trim();
    emal = resultEXTSPL.get("EXEMAL").toString().trim();
    lmdt = resultEXTSPL.get("EXLMDT").toString().trim();
    lmtm = resultEXTSPL.get("EXLMTM").toString().trim();
    dtxx = resultEXTSPL.get("EXDTXX").toString().trim();
    crud = resultEXTSPL.get("EXCRUD").toString().trim();
    site = resultEXTSPL.get("EXSITE").toString().trim();
    sitt = resultEXTSPL.get("EXSITT").toString().trim();
    def map = [CONO:cono, SUNO:suno, ITNO:itno, PUPR:pupr, FVDT:fvdt, EMAL:emal, LMDT:lmdt, LMTM:lmtm, DTXX:dtxx, CRUD:crud, SITE:site, SITT:sitt];
    lstEXTSPL.add(map);
  }
  
}
