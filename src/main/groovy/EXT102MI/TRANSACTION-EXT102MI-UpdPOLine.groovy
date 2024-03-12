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
*EXT102            20240116  RMURRAY     Update EXT api to handle portal updates and logging
*EXT102            20240205  RMURRAY     Validation on ION API call. 
*/

/*
* UpdPOLine
*/
public class UpdPOLine extends ExtendM3Transaction {

	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final IonAPI ion;

  private String suno;
  private String itno;
	private String puno;
	private String pnli;
	private String pnls;
	private String pupr;
	private String puprOld;
	private String dwdt;
	private String dwdtOld;
	private String pitd;
	private String pitdOld;
  private String pitt;
	private String pittOld;
  private String tel1;
	private String tel1Old;
	private String emal;
	private String currentDateTime;
	
	private int currentDate;
	private int currentTime;
	private int XXCONO;
	
	private boolean isDateParsed;

	public UpdPOLine(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.logger = logger;
		this.program = program;
		this.ion = ion; 
	}

	public void main() {
    //Fetch input fields from MI
		puno = mi.inData.get("PUNO") == null ? '' : mi.inData.get("PUNO").trim();
		if (puno == "?") {
			puno = "";
		} 
		pnli = mi.inData.get("PNLI") == null ? '' : mi.inData.get("PNLI").trim();
		if (pnli == "?") {
			pnli = "";
		} 
		pnls = mi.inData.get("PNLS") == null ? '' : mi.inData.get("PNLS").trim();
		if (pnls == "?") {
			pnls = "";
		} 
    tel1 = mi.inData.get("TEL1") == null ? '' : mi.inData.get("TEL1").trim();
		if (tel1 == "?") {
			tel1 = "";
		} 
		pitd = mi.inData.get("PITD") == null ? '' : mi.inData.get("PITD").trim();
		if (pitd == "?") {
			pitd = "";
		} 
    pitt = mi.inData.get("PITT") == null ? '' : mi.inData.get("PITT").trim();
		if (pitt == "?") {
			pitt = "";
		} 
		pupr = mi.inData.get("PUPR") == null ? '' : mi.inData.get("PUPR").trim();
		if (pupr == "?") {
			pupr = "";
		}     
		dwdt = mi.inData.get("DWDT") == null ? '' : mi.inData.get("DWDT").trim();
		if (dwdt == "?") {
			dwdt = "";
		} 
    tel1 = mi.inData.get("TEL1") == null ? '' : mi.inData.get("TEL1").trim();
		if (tel1 == "?") {
			tel1 = "";
		} 
    emal = mi.inData.get("EMAL") == null ? '' : mi.inData.get("EMAL").trim();
		if (emal == "?") {
			emal = "";
		} 

    pittOld = "";
		pitdOld = "";
    puprOld = "";
		dwdtOld = "";
    tel1Old = "";
		
		//Perform validation on input
		if (!validateInput()) {
			return;
		}

    //Set Date Variables
  	ZoneId zid = ZoneId.of("Australia/Brisbane"); 
		currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
		currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    currentDateTime =  (LocalDate.now(zid).format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).toString() + " AEST"; 
	  
    String url = "";
    Map<String, String> params = new HashMap<String,String>();
    url = "/M3/m3api-rest/v2/execute/PPS200MI/UpdLine";
    params = [ "PUNO": puno,  "PNLI": pnli, "PNLS": pnls ];
    if(!pupr.isEmpty()){
      params.put("PUPR", pupr);
    }
    if(!dwdt.isEmpty()){
      params.put("DWDT", dwdt);
    }
    if(!pitd.isEmpty()){
      params.put("PITD", pitd);
    }
    if(!pitt.isEmpty()){
      params.put("PITT", pitt);
    }
    if(!tel1.isEmpty()){
      params.put("TEL1", tel1);
    }

    Map<String,String> headers = ["Accept": "application/json"];
    IonResponse response = ion.get(url, headers, params);
    if(response.getError()){
      logger.debug("Failed calling ION API ${url}, detailed error message: ${response.getErrorMessage()}");
      mi.error(response.getErrorMessage());
      return;
    }
    if (response.getStatusCode() != 200) {
      logger.debug("Expected status 200 but got ${response.getStatusCode()} instead");
      mi.error(response.getErrorMessage());
      return
    }

    writeEXT()
	}

	/*
	* validateInput - Validate all the input fields
	* @return false if there is any error
	*         true if pass the validation
	*/
	boolean validateInput(){

    //Set CONO (Company number from program)
  	XXCONO= program.LDAZD.CONO;
	  
  	//Validate SUNO
    String purchaseOrderLine = puno.isEmpty() ? "PUNO" : pnli.isEmpty() ? "PNLI" : pnls.isEmpty() ? "PNLS" : "";
    if (purchaseOrderLine.isEmpty()) {
		  mi.error("${purchaseOrderLine} not defined");
    }

    DBAction queryMPLINE = database.table("MPLINE").index("00").selection("IBSUNO","IBITNO","IBPUPR","IBDWDT","IBPITD","IBPITT","IBTEL1").build();
		DBContainer MPLINE = queryMPLINE.getContainer();
    MPLINE.set("IBCONO", XXCONO);
    MPLINE.set("IBPUNO", puno);
    MPLINE.set("IBPNLI", pnli);
    MPLINE.set("IBPNLS", pnls);
    if (!queryMPLINE.readAll(MPLINE, 1, callbackMPLINE)) {
      mi.error("PO Line is invalid");
      return false;
    } 

    //Validate DWDT
		isDateParsed = false;
		if (!dwdt.isEmpty()) {
			if(dwdt.length() != 8 ){
				mi.error("Date length must be 8 - DWDT");
				return false;
			}
			try {
				isDateParsed = LocalDate.parse(dwdt, DateTimeFormatter.ofPattern("yyyyMMdd"))
			} catch (DateTimeParseException e) {
				mi.error("Incorrect date format yyyyMMdd - DWDT");
				return false;
			}
		} 

    if(emal.isEmpty()){
      mi.error("EMAL not supplied");
      return;
    }

		//Validation Successful
		return true;
	}
/*
  * lstMPLINE - Callback function to return EXTREL
  * IBSUNO IBITNO IBTEL1 IBPITD IBPITT
  */
  Closure<?> callbackMPLINE = { DBContainer MPLINE ->
    suno = MPLINE.get("IBSUNO").toString().trim();
    itno = MPLINE.get("IBITNO").toString().trim();
    puprOld = MPLINE.get("IBPUPR").toString().trim();
    tel1Old = MPLINE.get("IBTEL1").toString().trim();
    pitdOld = MPLINE.get("IBPITD").toString().trim();
    pittOld = MPLINE.get("IBPITT").toString().trim();
    dwdtOld = MPLINE.get("IBDWDT").toString().trim();
  }

  /*
	* writeEXTMAT
	*
	*/
  private void writeEXT() {
  	  DBAction actionEXTSP0 = database.table("EXTSP0").build();
  	  DBContainer EXTSP0 = actionEXTSP0.getContainer();
  	  EXTSP0.set("EXCONO", XXCONO);
  	  EXTSP0.set("EXSUNO", suno);
  	  EXTSP0.set("EXITNO", itno);
      if(!pupr.isEmpty()){
  	    EXTSP0.set("EXPUPR", pupr.toDouble());
      }
      if(puprOld != null && !puprOld.isEmpty()){
        EXTSP0.set("EXPUPO", puprOld.toDouble());
      }
      if(!dwdt.isEmpty()){
  	    EXTSP0.set("EXDWDT", dwdt.toInteger());
      }
      if(dwdtOld != null && !dwdtOld.isEmpty()){
        EXTSP0.set("EXDWO", dwdtOld.toInteger());
      }
      if(!pitd.isEmpty()){
  	    EXTSP0.set("EXPITD", pitd);
      }
      if(pitdOld != null && !pitdOld.isEmpty()){
        EXTSP0.set("EXPITO", pitdOld);
      }
      if(!pitt.isEmpty()){
  	    EXTSP0.set("EXPITD", pitt);
      }
      if(pittOld != null && !pittOld.isEmpty()){
        EXTSP0.set("EXPITO", pittOld);
      }
      if(!tel1.isEmpty()){
        EXTSP0.set("EXTEL1", tel1);
      }
      if(tel1Old != null && !tel1Old.isEmpty()){
        EXTSP0.set("EXTELO", tel1Old);
      }
  	  EXTSP0.set("EXEMAL", emal);
			EXTSP0.set("EXLMDT", currentDate.toInteger());
			EXTSP0.set("EXLMTM", currentTime.toInteger());
			EXTSP0.set("EXDTXX", currentDateTime);
			EXTSP0.set("EXFLAG", 0);
			EXTSP0.set("EXCRUD", "UPD");
	    actionEXTSP0.insert(EXTSP0, recordExists);
   }
   /*
   * recordExists - return record already exists error message to the MI
   */
	Closure recordExists = {
		mi.error("Record already exists");
	}

  
}
