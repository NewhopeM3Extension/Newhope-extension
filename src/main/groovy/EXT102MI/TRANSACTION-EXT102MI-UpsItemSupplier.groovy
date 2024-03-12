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
* UpsItemSupplier
*/
public class UpsItemSupplier extends ExtendM3Transaction {

	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final IonAPI ion;

	private String cono;
	private String itno;
	private String suno;
	private String pupr;
	private String puprOld;
	private String fvdt;
	private String fvdtOld;
	private String site;
	private String siteOld;
	private String sitt;
  private String sittOld;
	private String lclv;
	private String ctxt;
  private String ctxtOld;
	private String emal;
  private String crud;
	private String currentDateTime;
		
	private int currentDate;
	private int currentTime;
	private int XXCONO;
	
	private boolean isDateParsed;

	public UpsItemSupplier(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.logger = logger;
		this.program = program;
		this.ion = ion; 
	}

	public void main() {
    //Fetch input fields from MI
		cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();
		if (cono == "?") {
			cono = "";
		} 
		itno = mi.inData.get("ITNO") == null ? '' : mi.inData.get("ITNO").trim();
		if (itno == "?") {
			itno = "";
		} 
		suno = mi.inData.get("SUNO") == null ? '' : mi.inData.get("SUNO").trim();
		if (suno == "?") {
			suno = "";
		} 
		pupr = mi.inData.get("PUPR") == null ? '' : mi.inData.get("PUPR").trim();
		if (pupr == "?") {
			pupr = "";
		} 
		fvdt = mi.inData.get("FVDT") == null ? '' : mi.inData.get("FVDT").trim();
		if (fvdt == "?") {
			fvdt = "";
		} 
    site = mi.inData.get("SITE") == null ? '' : mi.inData.get("SITE").trim();
		if (site == "?") {
			site = "";
		} 
		sitt = mi.inData.get("SITT") == null ? '' : mi.inData.get("SITT").trim();
		if (sitt == "?") {
			sitt = "";
		} 
    lclv = mi.inData.get("LCLV") == null ? '' : mi.inData.get("LCLV").trim();
		if (lclv == "?") {
			lclv = "";
		}
    ctxt = mi.inData.get("CTXT") == null ? '' : mi.inData.get("CTXT").trim();
		if (ctxt == "?") {
			ctxt = "";
		} 
		emal = mi.inData.get("EMAL") == null ? '' : mi.inData.get("EMAL").trim();
		if (emal == "?") {
			emal = "";
		}
    crud = mi.inData.get("CRUD") == null ? '': mi.inData.get("CRUD").trim();
    if (crud == "?") {
			crud = "";
		}
		
    siteOld = "";
    sittOld = "";
    ctxtOld = "";
    puprOld = "";
    fvdtOld = "";

		//Perform validation on input
		if (!validateInput()) {
			return;
		}

    //Set Date Variables
  	ZoneId zid = ZoneId.of("Australia/Brisbane"); 
		currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
		currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    currentDateTime =  (LocalDate.now(zid).format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).toString() + " AEST"; 
	  
    Map<String, String> params = ["ITNO": itno,"SUNO": suno,"PUPR": pupr,"FVDT": fvdt,"CTXT": ctxt,"LCLV": lclv, "UCA4":currentDateTime ]; 
    Map<String,String> headers = ["Accept": "application/json"];
    
    String url = "";
    if(crud == "UPD"){
      url = "/M3/m3api-rest/v2/execute/PPS040MI/UpdItemSupplier";
    }
    if(crud == "ADD") {
      params.put("QUCL","NA"); 
      url = "/M3/m3api-rest/v2/execute/PPS040MI/AddItemSupplier";
    }

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
    if (!cono.isEmpty()){
  	  if (cono.isInteger()){
    		XXCONO= cono.toInteger();
    	  } else {
    		mi.error("Company " + cono + " is invalid");
    		return false;
  	  }
  	} else {
  	  XXCONO= program.LDAZD.CONO;
  	}
	  
  	//Validate SUNO
    if (!suno.isEmpty()) {
			DBAction queryCIDMAS = database.table("CIDMAS").index("00").selection("IDSUNO").build();
			DBContainer CIDMAS = queryCIDMAS.getContainer();
			CIDMAS.set("IDCONO", XXCONO);
			CIDMAS.set("IDSUNO", suno);
			if (!queryCIDMAS.read(CIDMAS)) {
				mi.error("Supplier is invalid.");
				return false;
			}
		} else {
		  mi.error("Supplier not defined")
		}
		
		//Validate PUPR
		if (!pupr.isEmpty() && !pupr.isNumber()) {
      mi.error("Purchase price not valid number");
      return false;
		}

    //Validate FVDT
		isDateParsed = false;
		if (!fvdt.isEmpty()) {
			if(fvdt.length() != 8 ){
				mi.error("Date length must be 8");
				return false;
			}
			try {
				isDateParsed = LocalDate.parse(fvdt, DateTimeFormatter.ofPattern("yyyyMMdd"))
			} catch (DateTimeParseException e) {
				mi.error("Incorrect date format yyyyMMdd");
				return false;
			}
		} else {
			mi.error("From date not supplied");
			return false;
		}

    DBAction queryMITVEN = database.table("MITVEN").index("00").selection("IFCONO", "IFSUNO", "IFITNO", "IFPUPR", "IFFVDT", "IFSITE", "IFSITT").build();
		DBContainer MITVEN = queryMITVEN.getContainer();
    MITVEN.set("IFCONO", XXCONO);
    MITVEN.set("IFITNO", itno);
    MITVEN.set("IFSUNO", suno);
    if (!queryMITVEN.readAll(MITVEN, 1, callbackMITVEN)) {
      mi.error("Item supplier is invalid.");
      return false;
    }
		
    if(!crud.isEmpty()){
      if(crud != "UPD" && crud != "ADD"){
        mi.error("CRUD input incorrect");
        return;
      }
    } else {
      mi.error("CRUD not supplied");
      return;
    }

		//Validation Successful
		return true;
	}

  Closure<?> callbackMITVEN = { DBContainer MITVEN ->
    puprOld = MITVEN.get("IFPUPR").toString()?.trim();
    fvdtOld = MITVEN.get("IFFVDT").toString()?.trim();
    siteOld = MITVEN.get("IFSITE").toString()?.trim();
    sittOld = MITVEN.get("IFSITT").toString()?.trim();
  }
   /*
	* writeEXTMAT
	*
	*/
  private void writeEXT() {
  	  DBAction actionEXTSPL = database.table("EXTSP0").build();
  	  DBContainer EXTSPL = actionEXTSPL.getContainer();
  	  EXTSPL.set("EXCONO", XXCONO);
  	  EXTSPL.set("EXSUNO", suno);
  	  EXTSPL.set("EXITNO", itno);
      if(!pupr.isEmpty()){
  	    EXTSPL.set("EXPUPR", pupr.toDouble());
      }
      if(puprOld != null && !puprOld.isEmpty()){
        EXTSPL.set("EXPUPO", puprOld.toDouble());
      }
      if(fvdtOld != null && !fvdtOld.isEmpty()){
        EXTSPL.set("EXFVDO", fvdtOld.toInteger());
      }
      if(siteOld != null && !siteOld.isEmpty()){
        EXTSPL.set("EXSITO", siteOld);
      }
      if(sittOld != null && !sittOld.isEmpty()){
        EXTSPL.set("EXSIT2", sittOld);
      }
  	  EXTSPL.set("EXFVDT", fvdt.toInteger());
  	  EXTSPL.set("EXEMAL", emal);
			EXTSPL.set("EXLMDT", currentDate.toInteger());
			EXTSPL.set("EXLMTM", currentTime.toInteger());
			EXTSPL.set("EXDTXX", currentDateTime);
			EXTSPL.set("EXFLAG", 0);
			EXTSPL.set("EXSITE", site);
			EXTSPL.set("EXSITT", sitt);
			EXTSPL.set("EXCRUD", crud);
	    actionEXTSPL.insert(EXTSPL, recordExists);
   }
   /*
   * recordExists - return record already exists error message to the MI
   */
	Closure recordExists = {
		mi.error("Record already exists");
	}
}
