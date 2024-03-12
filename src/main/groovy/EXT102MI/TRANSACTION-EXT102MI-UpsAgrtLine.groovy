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
* UpsAgrtLine
*/
public class UpsAgrtLine extends ExtendM3Transaction {

	private final MIAPI mi;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final IonAPI ion;

	private String suno;
	private String agnb;
	private String grpi;
	private String obv1;
	private String obv2;
	private String pupr;
	private String puprOld;
	private String fvdt;
	private String fvdtOld;
	private String uvdt;
	private String uvdtOld;
	private String emal;
  private String crud;
	private String currentDateTime;
	
	private int currentDate;
	private int currentTime;
	private int XXCONO;
	
	private boolean isDateParsed;

	public UpsAgrtLine(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
		this.mi = mi;
		this.database = database;
		this.miCaller = miCaller;
		this.logger = logger;
		this.program = program;
		this.ion = ion; 
	}

	public void main() {
    //Fetch input fields from MI

		suno = mi.inData.get("SUNO") == null ? '' : mi.inData.get("SUNO").trim();
		if (suno == "?") {
			suno = "";
		} 
    agnb = mi.inData.get("AGNB") == null ? '' : mi.inData.get("AGNB").trim();
		if (agnb == "?") {
			agnb = "";
		} 
    grpi = mi.inData.get("GRPI") == null ? '' : mi.inData.get("GRPI").trim();
		if (grpi == "?") {
			grpi = "";
		} 
    obv1 = mi.inData.get("OBV1") == null ? '' : mi.inData.get("OBV1").trim();
		if (obv1 == "?") {
			obv1 = "";
		} 
    obv2 = mi.inData.get("OBV2") == null ? '' : mi.inData.get("OBV2").trim();
		if (obv2 == "?") {
			obv2 = "";
		} 
		pupr = mi.inData.get("PUPR") == null ? '' : mi.inData.get("PUPR").trim();
		if (pupr == "?") {
			pupr = "";
		} 
		fvdt = mi.inData.get("FVDT") == null ? '' : mi.inData.get("FVDT").trim();
		if (fvdt == "?") {
			fvdt = "";
		} 
    uvdt = mi.inData.get("UVDT") == null ? '' : mi.inData.get("UVDT").trim();
		if (uvdt == "?") {
			uvdt = "";
		}  
		emal = mi.inData.get("EMAL") == null ? '' : mi.inData.get("EMAL").trim();
		if (emal == "?") {
			emal = "";
		}
    crud = mi.inData.get("CRUD") == null ? '': mi.inData.get("CRUD").trim();
    if (crud == "?") {
			crud = "";
		}

    uvdtOld = "";
    fvdtOld = "";
		puprOld = "";

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
    if(crud == "UPD"){
      url = "/M3/m3api-rest/v2/execute/PPS100MI/UpdItemSupplier";
      params = ["SUNO": suno,"AGNB": agnb,"GRPI": grpi,"OBV1": obv1,"OBV2": obv2,"FVDT": fvdt, "UVDT": uvdt]; 
    }
    if(crud == "ADD") {
      url = "/M3/m3api-rest/v2/execute/PPS040MI/AddItemSupplier";
      params = ["SUNO": suno,"AGNB": agnb,"GRPI": grpi,"OBV1": obv1,"OBV2": obv2,"FVDT": fvdt, "UVDT": uvdt, "PUPR": pupr]; 
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
    String headAgreement = suno.isEmpty() ? "SUNO" : agnb.isEmpty() ? "AGNB" : "";
    if (headAgreement.isEmpty()) {
		  mi.error("${headAgreement} not defined");
    }

    DBAction queryMPAGRH = database.table("MPAGRH").index("00").selection("AHAGNB").build();
		DBContainer MPAGRH = queryMPAGRH.getContainer();
    MPAGRH.set("AHCONO", XXCONO);
    MPAGRH.set("AHSUNO", suno);
    MPAGRH.set("AHAGNB", agnb);
    if (!queryMPAGRH.read(MPAGRH)) {
      mi.error("Agreement is invalid.");
      return false;
    }

    String lineAgreement = grpi.isEmpty() ? "GRPI" : obv1.isEmpty() ? "OBV1" : obv2.isEmpty() ? "OBV1" : fvdt.isEmpty() ? "FVDT" : uvdt.isEmpty() ? "UVDT" : "";
    if (lineAgreement.isEmpty()) {
		  mi.error("${lineAgreement} not defined");
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

    //Validate uvdt
		isDateParsed = false;
		if (!uvdt.isEmpty()) {
			if(uvdt.length() != 8 ){
				mi.error("Date length must be 8");
				return false;
			}
			try {
				isDateParsed = LocalDate.parse(uvdt, DateTimeFormatter.ofPattern("yyyyMMdd"))
			} catch (DateTimeParseException e) {
				mi.error("Incorrect date format yyyyMMdd");
				return false;
			}
		} else {
			mi.error("To date not supplied");
			return false;
		}

    DBAction queryMPAGRL = database.table("MPAGRL").index("00").selection("AIUVDT","AIFVDT","AIPUPR").build();
		DBContainer MPAGRL = queryMPAGRL.getContainer();
    MPAGRL.set("AICONO", XXCONO);
    MPAGRL.set("AISUNO", suno);
    MPAGRL.set("AIAGNB", agnb);
    MPAGRL.set("AIFVDT", fvdt);
    MPAGRL.set("AIGRPI", grpi);
    MPAGRL.set("AIOBV1", obv1);
    MPAGRL.set("AIOBV2", obv2);
    if (!queryMPAGRL.readAll(MPAGRL, 1, callbackMPAGRL)) {
      mi.error("Agreement Line is invalid.");
      return false;
    }
		
    if(!crud.isEmpty()){
      if(crud != "UPD" || crud != "ADD"){
        mi.error("CRUD input incorrect");
        return;
      }
    } else {
      mi.error("CRUD not supplied");
      return;
    }

    if(emal.isEmpty()){
      mi.error("EMAL not supplied");
      return;
    }

		//Validation Successful
		return true;
	}
  
  Closure<?> callbackMPAGRL = { DBContainer MPAGRL ->
    puprOld = MPAGRL.get("IBPUPR").toString()?.trim();
    fvdtOld = MPAGRL.get("IBFVDT").toString()?.trim();
    uvdtOld = MPAGRL.get("IBUVDT").toString()?.trim();
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
  	  EXTSPL.set("EXAGNB", agnb);
      if(!pupr.isEmpty()){
  	    EXTSPL.set("EXPUPR", pupr.toDouble());
        if(puprOld != null && !puprOld.isEmpty()) {
          EXTSPL.set("EXPUPO", puprOld.toDouble());
        }
      }
      EXTSPL.set("EXOBV1", obv1);
  	  EXTSPL.set("EXOBV2", obv2);
  	  EXTSPL.set("EXFVDT", fvdt.toInteger());
      if(fvdtOld != null && !fvdtOld.isEmpty()){
  	    EXTSPL.set("EXFVDO", fvdtOld.toInteger());
      }
      EXTSPL.set("EXUVDT", uvdt.toInteger());
      if(uvdtOld != null && !uvdtOld.isEmpty()){
  	    EXTSPL.set("EXUVDO", uvdtOld.toInteger());
      }
  	  EXTSPL.set("EXEMAL", emal);
			EXTSPL.set("EXLMDT", currentDate.toInteger());
			EXTSPL.set("EXLMTM", currentTime.toInteger());
			EXTSPL.set("EXDTXX", currentDateTime);
			EXTSPL.set("EXFLAG", 0);
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
