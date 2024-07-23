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
 
import groovy.lang.Closure
 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException
import java.time.ZoneId;

import groovy.json.JsonSlurper;

/*
*Modification area - M3
*Nbr                Date      User id   Description
*EXT101_UpdPOReqTxt 20240723  RMURRAY   API created to update PPS180 (MPOPLP) and MOS101 (MMOMAT) TXID value with the TXID created from spares portal for text lines between 60 and 300 characters.
*/

/*
* UpdPOReqTxt
*/
public class UpdPOReqTxt extends ExtendM3Transaction {

  private final MIAPI mi;
  private final DatabaseAPI database;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;

  private String cono;  
  private String plpn;
  private String plps;
  private String plp2;
  private String lino;
  private String ztxt;
  
  private String rorn;
  private String rorl;
  private String itno;
  private String tx40;
  private String faci;
  private String prno;
  private String txid;
  
  private int XXCONO;
  private int currentDate;
  private int currentTime;
  
  List<String> stringList;

	public UpdPOReqTxt(MIAPI mi, DatabaseAPI database,  LoggerAPI logger, ProgramAPI program, IonAPI ion) {
		this.mi = mi;
		this.database = database;
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
		plpn = mi.inData.get("PLPN") == null ? '' : mi.inData.get("PLPN").trim();
		if (plpn == "?") {
			plpn = "";
		} 
		plps = mi.inData.get("PLPS") == null ? '' : mi.inData.get("PLPS").trim();
		if (plps == "?") {
			plps = "";
		} 
		plp2 = mi.inData.get("PLP2") == null ? '' : mi.inData.get("PLP2").trim();
		if (plp2 == "?") {
			plp2 = "";
		} 
		txid = mi.inData.get("TXID") == null ? '' : mi.inData.get("TXID").trim();
		if (txid == "?") {
			txid = "";
		}
    lino = mi.inData.get("LINO") == null ? '' : mi.inData.get("LINO").trim();
		if (lino == "?") {
			lino = "";
		} 
		
		stringList = new ArrayList<String>();
		
		if (!validateInput()) {
			return;
		}
		
		ZoneId zid = ZoneId.of("Australia/Sydney"); 
		currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
		currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
		
    Map<String,String> headers = ["Accept": "application/json"];
    Map<String, String> params = new HashMap<String,String>();
    
    /***
     * ASSIGN TO WORK ORDER MATERIAL VIA MOS100MI_ChgMtrl, 
     * NOTE TO REVIEWER:
     * Changed to DATABASE update, as the API was causing 
     * the material line to go to STS90 after update.
     * This is only to assign TXID to the material line.
     * Status update not required for this action.
    */
    DBAction actionMMOMAT = database.table("MMOMAT")
      .index("00")
      .build();
    DBContainer MMOMAT = actionMMOMAT.getContainer();
    MMOMAT.set("QMCONO", XXCONO.toInteger());
    MMOMAT.set("QMFACI", faci);
    MMOMAT.set("QMPRNO", prno);
    MMOMAT.set("QMMWNO", rorn);
    MMOMAT.set("QMMSEQ", rorl.toInteger());
    if (!actionMMOMAT.readLock(MMOMAT, callbackMMOMAT)){
       mi.error("Record does not exists MMOMAT " + faci + " " + prno + " " + rorn + " " + rorl);
       return;
    }
    
    /***
     * ASSIGN TO PURCHASE ORDER REQUISITION MPOPLP VIA DB UPDATE
     * NOTE TO REVIEWER:
     * Direct database update is used as there is no suitable API 
     * for PPS180. This update is to only assign TXID
    */
    DBAction actionMPOPLP = database.table("MPOPLP")
      .index("00")
      .build();
    DBContainer MPOPLP = actionMPOPLP.getContainer();
    MPOPLP.set("POCONO", XXCONO.toInteger());
    MPOPLP.set("POPLPN", plpn.toInteger());
    MPOPLP.set("POPLPS", plps.toInteger());
    MPOPLP.set("POPLP2", plp2.toInteger());
    if (!actionMPOPLP.readLock(MPOPLP, callbackMPOPLP)){
       mi.error("Record does not exists MPOPLP " + plpn + " " + plps + " " + plp2 );
       return;
    }
    
    /***
     * ASSIGN TO PURCHASE ORDER REQUISITION MPOPLP VIA DB UPDATE
    */
    DBAction actionEXTMAT = database.table("EXTMAT")
       .index("00")
       .build();
    DBContainer EXTMAT = actionEXTMAT.getContainer();
    EXTMAT.set("EXCONO", XXCONO.toInteger());
    EXTMAT.set("EXFACI", faci);
    EXTMAT.set("EXPRNO", prno);
    EXTMAT.set("EXMWNO", plpn);
    EXTMAT.set("EXLINO", lino.toInteger())
    if (!actionEXTMAT.readLock(EXTMAT, deleteEXTMAT)){
       mi.error("Record does not exists EXTMAT " + faci + " " + prno + " " + plpn + " " + lino );
       return;
    }

	}
	
  /****
  * CALLBACKS
  */
  Closure callbackMPOPLP = { LockedResult MPOPLP ->
    int chno = MPOPLP.get("POCHNO").toString().toInteger();
    String pitt = MPOPLP.get("POPITT").toString();
    MPOPLP.set("POPTXT", pitt);
    MPOPLP.set("POTXID", Long.parseLong(txid));
    MPOPLP.set("POCHID", program.getUser());
    MPOPLP.set("POCHNO", chno + 1);
    MPOPLP.set("POLMDT", currentDate);
    MPOPLP.update();    
  }
  
  Closure callbackMMOMAT = { LockedResult MMOMAT ->
    int chno = MMOMAT.get("QMCHNO").toString().toInteger();
    MMOMAT.set("QMTXID", Long.parseLong(txid));
    MMOMAT.set("QMCHID", program.getUser());
    MMOMAT.set("QMCHNO", chno + 1);
    MMOMAT.set("QMLMDT", currentDate);
    MMOMAT.update();    
  }

  /*
  * deleteEXTMAT - Callback function to update MPOPLP table
  */
  Closure deleteEXTMAT = { LockedResult EXTMAT ->
    EXTMAT.delete();
  }
  
  /***
  * validateInput - Validate all the input fields
  * @return false if there is any error
  *         true if pass the validation
  */
  boolean validateInput(){

    if (!cono.isEmpty() ){
        if (cono.isInteger()){
          XXCONO = cono.toInteger();
        } else {
          mi.error("Company " + cono + " is invalid");
          return false;
        }
    } else {
        XXCONO= program.LDAZD.CONO;
    }

    /**** 
    * VALIDATE INPUTS
    * RETRIEVE DATA FROM MPOPLP
    */
    if(isStringNullOrEmpty(txid)){
      mi.error("TXID not supplied");
      return false;
    }
    
    /*
    * Validate Textblock Exists
    */
    Map<String,String> headers = ["Accept": "application/json"];
    Map<String,String> params = new HashMap<String,String>();
    
    /***
    * Validate the purchase order via input variable plpn, plps, PLP2
    * are correct, and reference an actual PO proposal.
    */
    params = new HashMap<String,String>();
    params.put("CONO", XXCONO.toString());
    params.put("PLPN", plpn);
    params.put("PLPS", plps);
    params.put("PLP2", plp2);
    String url = "/M3/m3api-rest/v2/execute/PPS170MI/GetPlannedPO";
    IonResponse responseGetPlannedPO = ion.get(url, headers, params);
    if(responseGetPlannedPO.getError()){
        logger.debug("Failed calling ION API ${url}, detailed error message: ${responseGetPlannedPO.getErrorMessage()}");
        mi.error("calling api failed ${url} " + responseGetPlannedPO.getErrorMessage());
        return false;
    }
    /***
    * Validation on return of row.
    */
    if (responseGetPlannedPO.getStatusCode() == 200) {
        JsonSlurper jsonSlurper = new JsonSlurper();
        Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(responseGetPlannedPO.getContent());
        ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
        ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
        recordList.eachWithIndex { it, index ->
          Map<String, String> record = (Map<String, String>) it;
          rorn = record.RORN;
          rorl = record.RORL;
          itno = record.ITNO;
          faci = record.FACI;
        }
    }
    /***
    * Failure if any of the following rorn, rorl, itno, faci are null
    * Unable to continue without reference work order numbers
    */
    if(isStringNullOrEmpty(rorn) || isStringNullOrEmpty(rorl) || isStringNullOrEmpty(itno) || isStringNullOrEmpty(faci)){
        mi.error("Reference not exist on requisition");
        return false;
    }

    /****
    * Validate work order, retrieve faci, prno for parameter set
    * Use the retrieved values from PlannedPO api call to Validate
    * referenced order.
    */
    params = new HashMap<String,String>();
    params.put("MWNO", rorn);
    params.put("MSEQ", rorl);
    url = "/M3/m3api-rest/v2/execute/MOS100MI/GetMtrl";
    IonResponse responseGetMtrl = ion.get(url, headers, params);
    if(responseGetMtrl.getError()){
        logger.debug("Failed calling ION API ${url}, detailed error message: ${responseGetMtrl.getErrorMessage()}");
        mi.error("calling api failed ${url} " + responseGetMtrl.getErrorMessage());
        return false;
    }
    /***
    * Validation on return of row.
    */
    if (responseGetMtrl.getStatusCode() == 200) {
        JsonSlurper jsonSlurper = new JsonSlurper();
        Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(responseGetMtrl.getContent());
        ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
        ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
        recordList.eachWithIndex { it, index ->
          Map<String, String> record = (Map<String, String>) it;
          prno = record.PRNO;
        }
    }
    /***
    * Failure if  prno not returned from API. 
    * PRNO is part of the index, thus mandatory.
    */
    if(isStringNullOrEmpty(prno)){
        mi.error("Unable to retrieve prno " + rorn + " " + rorl);
        return false;
    }
      
    return true;
  }
    
  /***********
  * HELPERS 
	* Checks if the string is null or empty.
	*/
	private boolean isStringNullOrEmpty(String stringToValidate){
		return stringToValidate == null || stringToValidate.isEmpty();
	}

}
