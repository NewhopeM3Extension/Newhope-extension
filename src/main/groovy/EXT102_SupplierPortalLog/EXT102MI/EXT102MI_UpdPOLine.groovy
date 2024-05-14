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
*/

/*
* UpdPOLine
*/
public class UpdPOLine extends ExtendM3Transaction {

	private final MIAPI mi;
	private final DatabaseAPI database;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final IonAPI ion;

	private String puno;
	private String pnli;
	private String pnls;
	private String pupr;
	private String dwdt;
	private String pitd;
	private String pitt;
	private String tel1;
	private String emal;
	private String suno;
	private String itno;
	private String orqa;
	private String crud;
	private String chid;
	private String pupo;
	private String dwdo;
	private String pito;
	private String pit2;
	private String telo;
	private String orqo;
	private String currentDateTime;

	private int currentDate;
	private int currentTime;
	private int XXCONO;

	private boolean isDateParsed;

	public UpdPOLine(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
		this.mi = mi;
		this.database = database;
		this.logger = logger;
		this.program = program;
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
		pit2 = mi.inData.get("PIT2") == null ? '' : mi.inData.get("PIT2").trim();
		if (pit2 == "?") {
			pit2 = "";
		} 
		pito = mi.inData.get("PITO") == null ? '' : mi.inData.get("PITO").trim();
		if (pito == "?") {
			pito = "";
		} 
		pupo = mi.inData.get("PUPO") == null ? '' : mi.inData.get("PUPO").trim();
		if (pupo == "?") {
			pupo = "";
		}     
		dwdo = mi.inData.get("DWDO") == null ? '' : mi.inData.get("DWDO").trim();
		if (dwdo == "?") {
			dwdo = "";
		} 
		telo = mi.inData.get("TELO") == null ? '' : mi.inData.get("TELO").trim();
		if (telo == "?") {
			telo = "";
		} 
		crud = mi.inData.get("CRUD") == null ? '' : mi.inData.get("CRUD").trim();
		if (crud == "?") {
			crud = "";
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
		* Perform validation on input variables
		* Note to reviewer - Validation not required on ITNO or SUNO as these fields are retrieved by the api PPS200MI_GetLine
		*/
		if (!validateInput()) {
			return;
		}

		/*
		* Set Date Variables with customer local time zone
		*/
		ZoneId zid = ZoneId.of("Australia/Brisbane"); 
		currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
		currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
		currentDateTime =  (LocalDate.now(zid).format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).toString() + " AEST"; 

		/*
		* Retrieve purchase order line information via API PPS200MI_GetLine
		*/
		Map<String, String> paramsPOLine = [ "CONO": XXCONO.toString(), "PUNO": puno, "PNLI": pnli, "PNLS": pnls ];
		String urlPOLine = "/M3/m3api-rest/v2/execute/PPS200MI/GetLine";

		Map<String,String> headersPOLine = ["Accept": "application/json"];
		IonResponse responsePOLine = ion.get(urlPOLine, headersPOLine, paramsPOLine);

		if(responsePOLine.getError()){
			logger.debug("Failed calling ION API ${urlPOLine}, detailed error message: ${responsePOLine.getErrorMessage()}");
			mi.error("calling api failed ${urlPOLine} " + responsePOLine.getErrorMessage());
			return;
		}

		if (responsePOLine.getStatusCode() != 200) {
			mi.error("Incorrect status, no payload");
			return;
		} 
			
		/*
		* If payload is for PO Confirmation "CNF"
		*/
		if(crud == "CNF"){

			JsonSlurper jsonSlurper = new JsonSlurper();
			Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(responsePOLine.getContent());
			ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
			ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
			recordList.eachWithIndex { it, index ->
				Map<String, String> recordMPLINE = (Map<String, String>) it;
				if (recordMPLINE.SUNO != null) {
					itno = recordMPLINE.ITNO;
					suno = recordMPLINE.SUNO;
					orqo = recordMPLINE.ORQA;
					orqa = recordMPLINE.ORQA;
					if(!isStringNullOrEmpty(pupr) && isStringNullOrEmpty(pupo)){
						pupo = recordMPLINE.PUPR;  
					}
					if(!isStringNullOrEmpty(dwdt) && isStringNullOrEmpty(dwdo)){
						dwdo = recordMPLINE.DWDT;
					}
				}
			}
		
			/*
			* Perform PO Line Confirmation via API PPS100MI_ConfirmLine
			*/
			Map<String, String> params = [ "PUNO": puno,  "PNLI": pnli, "PNLS": pnls ];
			String urlConfirmLine = "/M3/m3api-rest/v2/execute/PPS001MI/ConfirmLine";

			if(!isStringNullOrEmpty(pupr)){
				params.put("PUPR", pupr);
			}
			if(!isStringNullOrEmpty(dwdt)){
				params.put("DWDT", dwdt);
			}
			Map<String,String> headers = ["Accept": "application/json"];
			IonResponse response = ion.get(urlConfirmLine, headers, params);
			if(response.getError()){
				logger.debug("Failed calling ION API ${urlConfirmLine}, detailed error message: ${response.getErrorMessage()}");
				mi.error("calling api failed ${urlConfirmLine} " + response.getErrorMessage());
				return;
			}
      
    	} else {
			/*
			* If payload is not for PO Line Confirmation, Assumed as a PO Line Update.
			*/
			JsonSlurper jsonSlurper = new JsonSlurper();
			Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(responsePOLine.getContent());
			ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
			ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
			recordList.eachWithIndex { it, index ->
				Map<String, String> recordMPLINE = (Map<String, String>) it;
				if (recordMPLINE.SUNO != null) {
					itno = recordMPLINE.ITNO;
					suno = recordMPLINE.SUNO;
					orqo = recordMPLINE.ORQA;
					orqa = recordMPLINE.ORQA;
					if(isStringNullOrEmpty(pupo)){
						pupo = recordMPLINE.PUPR;  
					}
					if(isStringNullOrEmpty(telo)){
						telo = recordMPLINE.TEL1;  
					}
					if(isStringNullOrEmpty(pito)){
						pito = recordMPLINE.PITD;
					}
					if(isStringNullOrEmpty(pit2)){
						pit2 = recordMPLINE.PITT;
					}
					if(isStringNullOrEmpty(dwdo)){
						dwdo = recordMPLINE.DWDT;
					}
				}
			}
		}
		
		/*
		* Perform Update on PO Line via API PPS200MI_UpdLine
		*/
		Map<String, String> params = new HashMap<String,String>();
		String urlUpdLine = "/M3/m3api-rest/v2/execute/PPS200MI/UpdLine";
		params = [ "PUNO": puno,  "PNLI": pnli, "PNLS": pnls ];
		if(!isStringNullOrEmpty(pupr)){
			params.put("PUPR", pupr);
		}
		if(!isStringNullOrEmpty(dwdt)){
			params.put("DWDT", dwdt);
		}
		if(!isStringNullOrEmpty(pitd)){
			params.put("PITD", pitd);
		}
		if(!isStringNullOrEmpty(pitt)){
			params.put("PITT", pitt);
		}
		if(!isStringNullOrEmpty(tel1)){
			params.put("TEL1", tel1);
		}
		Map<String,String> headers = ["Accept": "application/json"];
		IonResponse response = ion.get(urlUpdLine, headers, params);
		if(response.getError()){
			logger.debug("Failed calling ION API ${urlUpdLine}, detailed error message: ${response.getErrorMessage()}");
			mi.error("calling api failed ${urlUpdLine} " + response.getErrorMessage());
			return;
		}
		
		/*
		* Write log values to extension table EXTSPZ
		*/
		writeEXT();
  	  
	}

	/*
	* validateInput - Validate all the input fields
	* @return false if there is any error
	*         true if pass the validation
	*/
	private boolean validateInput(){


		/*
		* CRUD field must not be null or empty
		*/
		if(isStringNullOrEmpty(crud)){
			logger.debug("mi.error: CRUD value is null or empty");
			mi.error("Crud value not specified")
			return false;
		} 

		/*
		* CRUD must be UPD or CNF
		*/
		if(crud != "UPD" && crud != "CNF"){
			logger.debug("mi.error: CRUD input is incorrect " + crud);
			mi.error("CRUD must be UPD or CNF");
			return false;
		}

		/*
		* Validate key fields used for api call
		*/
		String purchaseOrderLine = isStringNullOrEmpty(puno) ? "PUNO" : isStringNullOrEmpty(pnli) ? "PNLI" : isStringNullOrEmpty(pnls) ? "PNLS" : "";
		if (!isStringNullOrEmpty(purchaseOrderLine)) {
			mi.error("${purchaseOrderLine}, not defined");
			return false;
		}

		/*
		* Validate dwdt as date MovexDate YDM8 YYYYMMDD
		*/
		if(crud == "CNF") {
			/*Validate dwdt as date MovexDate*/
			if(!isStringNullOrEmpty(dwdt)){
				tryParseDate(dwdt);
			}
			/*Validate dwdo as date MovexDate*/
			if(!isStringNullOrEmpty(dwdo)){
				tryParseDate(dwdo);
			}
		} else {
			tryParseDate(dwdt);
			/*Validate dwdo as date MovexDate*/
			if(!isStringNullOrEmpty(dwdo)){
				tryParseDate(dwdo);
			}
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
		EXTSPZ.set("EXORQO", orqo.toDouble());
		if(!isStringNullOrEmpty(pupr)){
			EXTSPZ.set("EXPUPR", pupr.toDouble());
			if(!isStringNullOrEmpty(pupo)){
				EXTSPZ.set("EXPUPO", pupo.toDouble());
			}
		}
		if(!isStringNullOrEmpty(dwdt)){
			EXTSPZ.set("EXDWDT", dwdt.toInteger());
			if(!isStringNullOrEmpty(dwdo)){ 
				EXTSPZ.set("EXDWDO", dwdo.toInteger());
			}
		}
		if(!isStringNullOrEmpty(pitd)){
			EXTSPZ.set("EXPITD", pitd);
			if(isStringNullOrEmpty(pito)){
				EXTSPZ.set("EXPITO", pito);
			}
		}
		if(!isStringNullOrEmpty(pitt)){
			EXTSPZ.set("EXPITT", pitt);
			if(!isStringNullOrEmpty(pit2)){
				EXTSPZ.set("EXPIT2", pit2);
			}
		}
		if(!isStringNullOrEmpty(tel1)){
			EXTSPZ.set("EXTEL1", tel1);
			if(!isStringNullOrEmpty(telo)){
				EXTSPZ.set("EXTELO", telo);
			}
		}
		EXTSPZ.set("EXEMAL", emal);
		EXTSPZ.set("EXLMDT", currentDate.toInteger());
		EXTSPZ.set("EXLMTM", currentTime.toInteger());
		EXTSPZ.set("EXDTXX", currentDateTime);
		EXTSPZ.set("EXCRUD", crud); 
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
	}

	/********
	* Helpers
	*/
	/*
	* Checks if the date is correct format for MovexDate YYYYMMDD YDM8
	*/
	private void tryParseDate(String stringToParse){
		if (isStringNullOrEmpty(stringToParse)) {
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
