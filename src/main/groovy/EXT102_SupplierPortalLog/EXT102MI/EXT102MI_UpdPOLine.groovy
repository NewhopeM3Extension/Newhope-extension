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
*Nbr     Date      User id   Description
*EXT102  20240116  RMURRAY   Update EXT api to handle portal updates and logging
*EXT102  20240205  RMURRAY   Validation on ION API call. 
*EXT102  20240414  RMURRAY   Add in PO Line confirmation
*EXT102  20240501  RMURRAY   Prepare for submission, PRD Approval.
*EXT102	 20240514  RMURRAY	 Making updates as per review feedback.
*EXT102  20240717  RMURRAY   Updating logic, consider flag for PO approval UCA1, and Status update for previously approved items. 
*/

/*
* UpdPOLine
*/
public class UpdPOLine extends ExtendM3Transaction {

	private final MIAPI mi;
	private final DatabaseAPI database;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final MICallerAPI miCaller;
	private final IonAPI ion;

	private String puno; /*INPUT*/
	private String pnli; /*INPUT*/
	private String pnls; /*INPUT*/
	private String codt; /*INPUT*/

	private String cppr;
	private String site;
	private String pitd;
	private String pitt;
	private String tel1;
	private String emal;
	private String suno;
	private String itno;
	private String orqa;
	private String crud;
	private String chid;
	private String cppo;
	private String codo;
	private String telo;

	private String orqo;
	private String pust;
	private String pusl;
	private String currentDateTime;

	private int currentDate;
	private int currentTime;
	private int XXCONO;

	private boolean isDateParsed;

	public UpdPOLine(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller, IonAPI ion) {
		this.mi = mi;
		this.database = database;
		this.logger = logger;
		this.program = program;
		this.miCaller = miCaller; 
		this.ion = ion; 
	}

	public void main() {
		/**
		*Fetch input fields from MI
		*/
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
    codt = mi.inData.get("CODT") == null ? '' : mi.inData.get("CODT").trim(); 
		if (codt == "?") {
			codt = "";
		} 
    cppr = mi.inData.get("CPPR") == null ? '' : mi.inData.get("CPPR").trim(); 
		if (cppr == "?") {
			cppr = "";
		}     
		tel1 = mi.inData.get("TEL1") == null ? '' : mi.inData.get("TEL1").trim();
		if (tel1 == "?") {
			tel1 = "";
		}  
		emal = mi.inData.get("EMAL") == null ? '' : mi.inData.get("EMAL").trim();
		if (emal == "?") {
			emal = "";
		} 

		/*
		* Set company number
		*/
		XXCONO= program.LDAZD.CONO;
		/*
		* Set program user as chid user
		*/
		chid = program.getUser();
    /*
		* Set Date Variables with customer local time zone
		*/
		ZoneId zid = ZoneId.of("Australia/Sydney"); 
		currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
		currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
		currentDateTime =  (LocalDate.now(zid).format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).toString() + " AEST"; 

		/*
		* Perform validation on input variables
		* Note to reviewer - Validation not required on ITNO or SUNO as these fields are retrieved by the api PPS200MI_GetLine
		*/
		if (!validateInput()) {
			return;
		}

		/*
		* Retrieve purchase order line information via API PPS200MI_GetLine
		*/
    Map<String, String> paramsGetPOLine = [ "PUNO": puno,  "PNLI": pnli, "PNLS": pnls ];
    miCaller.call("PPS001MI","GetLine", paramsGetPOLine, callbackGetPOLine);
    
    if(isStringNullOrEmpty(itno) || isStringNullOrEmpty(pust) || isStringNullOrEmpty(pusl)){
      mi.error("Error, PO Line not found " + puno + " " + pnli + " " + pnls);
      return;
    }
    if(!isStringNullOrEmpty(pust) && !isStringNullOrEmpty(pusl)){
      if(pust.toInteger() > 70 || pusl.toInteger() > 35 || pusl.toInteger() < 12){
        mi.error("Error, incorrect status PUSL:" + pusl + " PUST:" + pust);
        return;
      }
    }

	  if(pusl.toInteger() == 35){
  		Map<String, String> paramsDelPOTrans = [ "PUNO": puno, "PNLI": pnli, "PUOS": pusl.trim() ];
      miCaller.call("PPS330MI","DelPOTrans", paramsDelPOTrans, {});
    };
    
    /*
     * Called ION for Update as PPS200MI through MICaller causes lock.
    */
    String url = "/M3/m3api-rest/v2/execute/PPS200MI/UpdLine";
    Map<String, String> params = [ "PUNO": puno,  "PNLI": pnli, "PNLS": pnls, "UCA1":"Y" ];
    if(!cppr.isEmpty()){
      params.put("PUPR", cppr);
    }
    if(!codt.isEmpty()){
      params.put("CODT", codt);
    }
    if(!tel1.isEmpty()){
      params.put("TEL1", tel1);
    }
    Map<String,String> headers = ["Accept": "application/json"];
    IonResponse response = ion.get(url, headers, params);
    logger.info("response ${response}");
    if(response.getError()){
      mi.error(response.getErrorMessage());
      return;
    }

    DBAction actionMPLINE = database.table("MPLINE").index("00").build();
    DBContainer MPLINE = actionMPLINE.getContainer();
    MPLINE.set("IBCONO", XXCONO);
    MPLINE.set("IBPUNO", puno);
    MPLINE.set("IBPNLI", pnli.toInteger());
    MPLINE.set("IBPNLS", pnls.toInteger());
    if (!actionMPLINE.readLock(MPLINE, updateMPLINE)){
      mi.error("Record does not exists");
      return;
    }

    Map<String, String> paramsConfirmLine = [ "PUNO": puno,  "PNLI": pnli, "PNLS": pnls, "CODT": codt];
    if(!isStringNullOrEmpty(cppr)){
      paramsConfirmLine.put("CPPR", cppr);
    }
    miCaller.call("PPS001MI","ConfirmLine", paramsConfirmLine, null);

		writeEXT();

	}
	
	
	/*
   * updateEXTMAT - Callback function to update EXTMAT table
   *
   */
	Closure<?> updateMPLINE = { LockedResult MPLINE ->
    ZoneId zid = ZoneId.of("Australia/Sydney"); 
    int currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    int counter = MPLINE.get("IBCHNO").toString().trim().toInteger();
    MPLINE.set("IBLMDT", currentDate);
    MPLINE.set("IBCHNO", counter +1);
    MPLINE.set("IBCHID", program.getUser());
    MPLINE.set("IBPUSL", "20")
    MPLINE.set("IBUCA1", "Y");  /*SET APPROVAL FLAG TO Y - HAS BEEN PREVIOUSLY APROVED*/
    MPLINE.update();
	}
	

  Closure<?> callbackGetPOLine = { Map<String,String> response -> 
    itno = response.ITNO.trim();
    suno = response.SUNO.trim();
    orqo = response.ORQA.trim();
    orqa = response.ORQA.trim();
    pust = response.PUST.trim();
    pusl = response.PUSL.trim();
    if(!isStringNullOrEmpty(cppr)){
      String cPPO = response.CPPR.trim();
      if(!isStringNullOrEmpty(cPPO) && cPPO.toDouble() != cppr.toDouble()){
        cppo = cPPO;  
      }
    }
    if(!isStringNullOrEmpty(tel1)){
      String tELO = response.TEL1.trim();
      if(!isStringNullOrEmpty(tELO) && tELO.trim() != tel1.trim()){
        telo = tELO;  
      }
    }
    if(!isStringNullOrEmpty(codt)){
      String cODT = response.CODT.trim();
      if(!isStringNullOrEmpty(cODT) && cODT.toInteger() != codt.toInteger()){
        codo = response.CODT.trim();
      }
    }
  }

	/**********
	* validateInput - Validate all the input fields
	* @return false if there is any error
	*         true if pass the validation
	*/
	private boolean validateInput(){

		/*
		* Validate key fields used for api call
		*/
		String purchaseOrderLine = isStringNullOrEmpty(puno) ? "PUNO" : isStringNullOrEmpty(pnli) ? "PNLI" : isStringNullOrEmpty(pnls) ? "PNLS" : "";
		if (!isStringNullOrEmpty(purchaseOrderLine)) {
			mi.error("${purchaseOrderLine}, not defined");
			return false;
		}

		/*
		* Validate CODT confirmed date as date MovexDate YDM8 YYYYMMDD
		*/
		if(!isStringNullOrEmpty(codt)){
			tryParseDate(codt);
		}
		
		/*
		* Validate user email not empty
		*/
		if(!isEmailFormat(emal)){
			mi.error("Invalid email address - EMAL");
			return false;
		}

		/*
		* Validation Successful
		*/
		return true;
	}

	/*
	* writeEXT
	* Create log entry in table EXTSPZ
	*/
	private void writeEXT() {
		DBAction actionEXTSPZ = database.table("EXTSPZ").build();
		DBContainer EXTSPZ = actionEXTSPZ.getContainer();
		EXTSPZ.set("EXCONO", XXCONO);
		EXTSPZ.set("EXSUNO", suno);
		EXTSPZ.set("EXITNO", itno);
		EXTSPZ.set("EXPUNO", puno);
		EXTSPZ.set("EXPNLI", pnli.toInteger());
		EXTSPZ.set("EXPNLS", pnls.toInteger());
		EXTSPZ.set("EXORQA", orqa.toDouble());
    if(!isStringNullOrEmpty(orqo)){
      EXTSPZ.set("EXORQO", orqo.toDouble());
    }
		if(!isStringNullOrEmpty(cppo)){      
			EXTSPZ.set("EXPUPR", cppr.toDouble());
			EXTSPZ.set("EXPUPO", cppo.toDouble());			
		}
		if(!isStringNullOrEmpty(codo)){
			EXTSPZ.set("EXDWDT", codt.toInteger());
			EXTSPZ.set("EXDWDO", codo.toInteger());
		}
		if(!isStringNullOrEmpty(telo)){
			EXTSPZ.set("EXTEL1", tel1);
			EXTSPZ.set("EXTELO", telo);
		}
		EXTSPZ.set("EXEMAL", emal);
		EXTSPZ.set("EXLMDT", currentDate.toInteger());
		EXTSPZ.set("EXLMTM", currentTime.toInteger());
		EXTSPZ.set("EXCHID", chid);
		EXTSPZ.set("EXCHNO", 0); 
		EXTSPZ.set("EXRGDT", currentDate);
		EXTSPZ.set("EXRGTM", currentTime);
		actionEXTSPZ.insert(EXTSPZ, recordExists);
	}
	
	/*
	* recordExists - return record already exists error message to the MI
	*/
	Closure recordExists = {
		mi.error("Record already exists");
		return;
	}

	/********
	* Helpers
	*/
	/*
	* Checks if the date is correct format for MovexDate YYYYMMDD YDM8
	*/
	private void tryParseDate(String stringToParse){
		if (!isStringNullOrEmpty(stringToParse)) {
			if(stringToParse.length() != 8){
				mi.error("Date length must be 8");
				return;
			}
			try {
				LocalDate.parse(stringToParse, DateTimeFormatter.ofPattern("yyyyMMdd"))
			} catch (DateTimeParseException e) {
				mi.error("Incorrect date format yyyyMMdd");
				return;
			}
		}
	}

	/*
	* Checks if the string is null or empty.
	*/
	private boolean isStringNullOrEmpty(String stringToValidate){
		return stringToValidate == null || stringToValidate.isEmpty();
	}
	/*
	* Checks if string is not null or empty, contains the @ symbol
	* unable to use java.util.regex, unable to use PatternSyntaxException
	*/
	private boolean isEmailFormat(String stringToValidate){
		return !isStringNullOrEmpty(stringToValidate) && stringToValidate.contains('@') && !isStringNullOrEmpty(stringToValidate.split('@')[0]) && !isStringNullOrEmpty(stringToValidate.split('@')[1]) && stringToValidate.split('@')[1].contains('.');
	}

}
