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
*EXT101_UpdPOReqTxt 20240730  RMURRAY   Changed calls from ion to use micaller. Added validation of input textblockid TXID validated TXVR as MWNO. Check if MMOMAT and MPOPLP has existing TXID, and delete after being replaced. 
*                                       Removed delete of record from EXTMAT as it no longer requires the record, and is removed prior to this step by the portal executing EXT101MI_DelWOPart.
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
  private final MICallerAPI miCaller;

  private String cono;  
  private String plpn;
  private String plps;
  private String plp2;
  private String ztxt;
  
  private String rorn;
  private String rorl;
  private String itno;
  private String tx40;
  private String faci;
  private String prno;
  private String txid;
  private String tfil;
  private String txvr;
  private String lncd;
      
  private String txidMWOMAT;
  private String txidMPOPLP;
  
  private int XXCONO;
  private int currentDate;
  private int currentTime;
  
  List<String> stringList;

	public UpdPOReqTxt(MIAPI mi, DatabaseAPI database,  LoggerAPI logger, ProgramAPI program, IonAPI ion, MICallerAPI miCaller) {
		this.mi = mi;
		this.database = database;
		this.logger = logger;
		this.program = program;
		this.ion = ion;
		this.miCaller = miCaller;
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
		
		stringList = new ArrayList<String>();
		tfil = "MSYTXH";
		
		if (!validateInput()) {
			return;
		}
		
		ZoneId zid = ZoneId.of("Australia/Sydney"); 
		currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
		currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    
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

    
    int txidMPOPLPInt = !isStringNullOrEmpty(txidMPOPLP) ? txidMPOPLP.toInteger() : 0;
    int txidMMOMATInt = !isStringNullOrEmpty(txidMWOMAT) ? txidMWOMAT.toInteger() : 0;
    
    if(txidMPOPLPInt > 0){
      
      String txvrMPOPLP = "";
      String lncdMPOPLP = "";
      Map<String,String> paramsLstTxtBlocks = ["CONO": XXCONO.toString(), "TXID": txid, "TFIL": tfil];
      miCaller.setListMaxRecords(1);
      miCaller.call("CRS980MI","LstTxtBlocks", paramsLstTxtBlocks, { Map<String, String> response ->
        txvrMPOPLP = isStringNullOrEmpty(response.TXVR) ? "" : response.TXVR.trim();
        lncdMPOPLP = isStringNullOrEmpty(response.LNCD) ? "" : response.LNCD.trim();
      });
      
      paramsLstTxtBlocks.put("LNCD",lncdMPOPLP);
      paramsLstTxtBlocks.put("TXVR", txvrMPOPLP);
      miCaller.call("CRS980MI","DltTxtBlockLins", paramsLstTxtBlocks, {});
      
    }
    
    
    if((txidMMOMATInt > 0 && txidMPOPLPInt == 0) || (txidMPOPLPInt > 0 && txidMMOMATInt > 0 && txidMMOMATInt != txidMPOPLPInt)){
      
      String txvrMMOMAT = "";
      String lncdMMOMAT = "";
      Map<String,String> paramsLstTxtBlocks = ["CONO": XXCONO.toString(), "TXID": txid, "TFIL": tfil];
      miCaller.setListMaxRecords(1);
      miCaller.call("CRS980MI","LstTxtBlocks", paramsLstTxtBlocks, { Map<String, String> response ->
        txvrMMOMAT = response.TXVR;
        lncdMMOMAT = response.LNCD;
      });

      paramsLstTxtBlocks.put("LNCD",lncdMMOMAT);
      paramsLstTxtBlocks.put("TXVR", txvrMMOMAT);
      miCaller.call("CRS980MI","DltTxtBlockLins", paramsLstTxtBlocks, {});
    }
    

	}
	
  /****
  * CALLBACKS
  */
  Closure<?> callbackMPOPLP = { LockedResult MPOPLP ->
    int chno = MPOPLP.get("POCHNO").toString().toInteger();
    String pitt = MPOPLP.get("POPITT").toString();
    MPOPLP.set("POPTXT", pitt);
    MPOPLP.set("POTXID", Long.parseLong(txid));
    MPOPLP.set("POCHID", program.getUser());
    MPOPLP.set("POCHNO", chno + 1);
    MPOPLP.set("POLMDT", currentDate);
    MPOPLP.update();    
  }
  
  Closure<?> callbackMMOMAT = { LockedResult MMOMAT ->
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
  
	Closure<?> callbackGetPlannedPO = { Map<String, String> response ->
    rorn = response.RORN.trim();
    rorl = response.RORL.trim();
    itno = response.ITNO.trim();
    faci = response.FACI.trim();
    txidMPOPLP = isStringNullOrEmpty(response.TXID) ? "" : response.TXID.trim();
  }
  
  Closure<?> callbackGetMtrl = { Map<String, String> response ->
    prno = response.PRNO;
    txidMWOMAT = isStringNullOrEmpty(response.TXID) ? "" : response.TXID.trim();
  }
  
  Closure<?> callbackLstTxtBlocks = { Map<String, String> response ->
    txvr = response.TXVR;
    lncd = response.LNCD;
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
    
    /***
    * Validate the purchase order via input variable plpn, plps, PLP2
    * are correct, and reference an actual PO proposal.
    */
    Map<String,String> paramsGetPlannedPO = ["CONO": XXCONO.toString(), "PLPN": plpn, "PLPS": plps, "PLP2": plp2];
    miCaller.setListMaxRecords(1);
    miCaller.call("PPS170MI","GetPlannedPO", paramsGetPlannedPO, callbackGetPlannedPO);
    
    /***
    * Failure if any of the following rorn, rorl, itno, faci are null
    * Unable to continue without reference work order numbers
    */
    if(isStringNullOrEmpty(rorn) || isStringNullOrEmpty(rorl) || isStringNullOrEmpty(itno) || isStringNullOrEmpty(faci)){
      mi.error("Reference not exist on requisition");
      return false;
    }
     
    /***
    * Validate the textid exists in database.
    */
    Map<String,String> paramsLstTxtBlocks = ["CONO": XXCONO.toString(), "TXID": txid, "TFIL": tfil];
    miCaller.setListMaxRecords(1);
    miCaller.call("CRS980MI","LstTxtBlocks", paramsLstTxtBlocks, callbackLstTxtBlocks);
    if(isStringNullOrEmpty(txvr) || txvr != rorn){
      mi.error("Error, text block txvr != mwno");
      return;
    }
    
    /****
    * Validate work order, retrieve faci, prno for parameter set
    * Use the retrieved values from PlannedPO api call to Validate
    * referenced order.
    */
    Map<String,String> paramsGetMtrl = [ "MWNO": rorn, "MSEQ": rorl];
    miCaller.call("MOS100MI","GetMtrl",paramsGetMtrl, callbackGetMtrl);
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
