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
 import java.time.ZoneId;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;


/*
 *Modification area - M3
 *Nbr                Date      User id     Description
 *NHG_Phase_2        20240612  KVERCO   Workflow action Audit Trail
 *NHG_SupplierPortal 20250314  KVERCO   Add new column CPPR, CNAM, CLAM, SITE, RESP, CRID
 *
 */

/**
* - Add Audit Trail extension table row
*/
public class AddAudit extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String divi;
  private String wfnm;
  private String puno;
  private String pnli;
  private String pnls;
  private String suno;
  private String sino;
  private String inyr;
  private String repn;
  private String appr;
  private String sunm;
  private String pupr;
  private String lnam;
  private String lamt;
  private String cucd;
  private String itno;
  private String acta;
  private String apro;
  private String apli;
  private String purc;
  private String ait1;
  private String ait2;
  private String ait3;
  private String ait4;
  private String ait5;
  private String ait6;
  private String ait7;
  private String orqa;
  private String rorn;
  private String plpn = "";
  private String plps = "";
  private String plp2 = "";
  private String cppr = "";
  private String site = "";
  private String camt = "";
  private String clam = "";
  private String resp = "";
  private String crid = "";

  

  private boolean found;
  

  private int XXCONO;
 
 /*
  * Add Workflow Action Audit Trail extension table row
 */
  public AddAudit(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
	  
  }
  
  public void main() {
    
  	divi = mi.inData.get("DIVI") == null ? '' : mi.inData.get("DIVI").trim();
    if (divi.isEmpty()) {
      mi.error("Division must be entered");
      return;
    }
  	wfnm = mi.inData.get("WFNM") == null ? '' : mi.inData.get("WFNM").trim();
    if (wfnm.isEmpty()) {
      mi.error("Workflow name must be entered");
      return;
    }
  	puno = mi.inData.get("PUNO") == null ? '' : mi.inData.get("PUNO").trim();
  	if (puno == "?") {
  	  puno = " ";
  	} 
  	pnli = mi.inData.get("PNLI") == null ? '' : mi.inData.get("PNLI").trim();
  	if (pnli == "?") {
  	  pnli = " ";
  	} 
  	pnls = mi.inData.get("PNLS") == null ? '' : mi.inData.get("PNLS").trim();
  	if (pnls == "?") {
  	  pnls = " ";
  	} 
  	suno = mi.inData.get("SUNO") == null ? '' : mi.inData.get("SUNO").trim();
  	if (suno == "?") {
  	  suno = "";
  	} 
  	sino = mi.inData.get("SINO") == null ? '' : mi.inData.get("SINO").trim();
  	if (sino == "?") {
  	  sino = "";
  	} 
  	inyr = mi.inData.get("INYR") == null ? '' : mi.inData.get("INYR").trim();
  	if (inyr == "?") {
  	  inyr = " ";
  	} 
  	repn = mi.inData.get("REPN") == null ? '0' : mi.inData.get("REPN").trim();
  	if (repn == "?") {
  	  repn = " ";
  	} 

		XXCONO = (Integer)program.LDAZD.CONO;

    // Validate input fields  	
    
    // Validate input fields for Supplier Invoice
		if (wfnm.isEmpty()) {
      mi.error("Workflow name must be entered");
      return;
    }

    // validate input fields for Supplier Invoice and Purchase Order Line
    if (wfnm.equals("SupplierInvoiceLine_Approval_Non_Matched") || wfnm.equals("SupplierInvoiceLine_POReceipt") || wfnm.equals("POLine_Authorisation")) {
		   if (puno.isEmpty()) {
          mi.error("PO number must be entered");
          return;
       }
		   if (pnli.isEmpty()) {
          mi.error("PO line number must be entered");
          return;
       }
		   if (pnls.isEmpty()) {
          mi.error("PO sub line no must be entered");
          return;
		   }      
    }
    if (wfnm.equals("SupplierInvoiceLine_Approval_Non_Matched") || wfnm.equals("SupplierInvoiceLine_POReceipt") || wfnm.equals("Invoice_No_PO_Approval")) {
       if (suno.isEmpty()) {
          mi.error("Supplier no must be entered");
          return;
        }
		    if (sino.isEmpty()) {
          mi.error("Supplier Invoice no must be entered");
          return;
        }
		    if (inyr.isEmpty()) {
          mi.error("Invoice year must be entered");
          return;
        }
    }

    // validate input fielsd for Purchase Requisition
    if (wfnm.equals("PORequisiton_Authorisation")) {
		   if (puno.isEmpty()) {
          mi.error("PR no must be entered");
          return;
       }
		   if (pnli.isEmpty()) {
          mi.error("PR sub line no must be entered");
          return;
       }
		   if (pnls.isEmpty()) {
          mi.error("PR sub line no 2 must be entered");
          return;
       }
       plpn = puno;
       plps = pnli;
       plp2 = pnls;
       inyr = "0";
       repn = "0";       
    }
    
    // set inyr, repn to 0 for POLine_Authorisation
    if (wfnm.equals("POLine_Authorisation")) {
       inyr = "0";
       repn = "0";
    }
    // set pnli, pnls to 0 for Invoice No PO Invoice_No_PO_Approval
    if (wfnm.equals("Invoice_No_PO_Approval")) {
       pnli = "0";
       pnls = "0";
       repn = "0";
    }
    
    if (repn.isEmpty()) {
      repn = "0";
    }
    
  	appr = mi.inData.get("APPR") == null ? '' : mi.inData.get("APPR").trim();
  	if (appr == "?") {
  	  appr = "";
  	} 
  	acta = mi.inData.get("ACTA") == null ? '' : mi.inData.get("ACTA").trim();
		if (acta.isEmpty()) {
      mi.error("Workflow Action must be entered");
      return;
    }
  	sunm = mi.inData.get("SUNM") == null ? '' : mi.inData.get("SUNM").trim();
  	if (sunm == "?") {
  	  sunm = "";
  	}
  	itno = mi.inData.get("ITNO") == null ? '' : mi.inData.get("ITNO").trim();
  	if (itno == "?") {
  	  itno = "";
  	}
  	cucd = mi.inData.get("CUCD") == null ? '' : mi.inData.get("CUCD").trim();
  	if (cucd == "?") {
  	  cucd = "";
  	}
  	pupr = mi.inData.get("PUPR") == null ? '' : mi.inData.get("PUPR").trim();
  	if (pupr == "?") {
  	  pupr = "0";
  	}
  	lnam = mi.inData.get("LNAM") == null ? '' : mi.inData.get("LNAM").trim();
  	if (lnam == "?") {
  	  lnam = "0";
  	}
  	lamt = mi.inData.get("LAMT") == null ? '' : mi.inData.get("LAMT").trim();
  	if (lamt == "?") {
  	  lamt = "0";
  	}
  	apro = mi.inData.get("APRO") == null ? '' : mi.inData.get("APRO").trim();
  	if (apro == "?") {
  	  apro = "";
  	}
  	apli = mi.inData.get("APLI") == null ? '' : mi.inData.get("APLI").trim();
  	if (apli == "?") {
  	  apli = "0";
  	}
  	purc = mi.inData.get("PURC") == null ? '' : mi.inData.get("PURC").trim();
  	if (purc == "?") {
  	  purc = "";
  	}
  	ait1 = mi.inData.get("AIT1") == null ? '' : mi.inData.get("AIT1").trim();
  	if (ait1 == "?" || ait1.equals("N/A")) {
  	  ait1 = "";
  	}
  	ait2 = mi.inData.get("AIT2") == null ? '' : mi.inData.get("AIT2").trim();
  	if (ait2 == "?"  || ait2.equals("N/A")) {
  	  ait2 = "";
  	}
  	ait3 = mi.inData.get("AIT3") == null ? '' : mi.inData.get("AIT3").trim();
  	if (ait3 == "?" || ait3.equals("N/A")) {
  	  ait3 = "";
  	}
  	ait4 = mi.inData.get("AIT4") == null ? '' : mi.inData.get("AIT4").trim();
  	if (ait4 == "?" || ait4.equals("N/A")) {
  	  ait4 = "";
  	}
  	ait5 = mi.inData.get("AIT5") == null ? '' : mi.inData.get("AIT5").trim();
  	if (ait5 == "?" || ait5.equals("N/A")) {
  	  ait5 = "";
  	}
  	ait6 = mi.inData.get("AIT6") == null ? '' : mi.inData.get("AIT6").trim();
  	if (ait6 == "?" || ait6.equals("N/A")) {
  	  ait6 = "";
  	}
  	ait7 = mi.inData.get("AIT7") == null ? '' : mi.inData.get("AIT7").trim();
  	if (ait7 == "?" || ait7.equals("N/A")) {
  	  ait7 = "";
  	}
  	orqa = mi.inData.get("ORQA") == null ? '' : mi.inData.get("ORQA").trim();
  	if (orqa == "?") {
  	  orqa = "0";
  	}
  	rorn = mi.inData.get("RORN") == null ? '' : mi.inData.get("RORN").trim();
  	if (rorn == "?" || rorn.equals("N/A")) {
  	  rorn = "";
  	}
  	cppr = mi.inData.get("CPPR") == null ? '0' : mi.inData.get("CPPR").trim();
  	if (cppr == "?") {
  	  cppr = "0";
  	}
  	camt = mi.inData.get("CAMT") == null ? '0' : mi.inData.get("CAMT").trim();
  	if (camt == "?") {
  	  camt = "0";
  	}
  	site = mi.inData.get("SITE") == null ? '' : mi.inData.get("SITE").trim();
  	if (site == "?" || site.equals("N/A")) {
  	  site = "";
  	}
  	resp = mi.inData.get("RESP") == null ? '' : mi.inData.get("RESP").trim();
  	if (resp == "?") {
  	  resp = "";
  	}
  	crid = mi.inData.get("CRID") == null ? '' : mi.inData.get("CRID").trim();
  	if (crid == "?") {
  	  crid = "";
  	}


		XXCONO = (Integer)program.LDAZD.CONO;

    /* validate wfnm */
    if (!wfnm.equals("POLine_Authorisation") && !wfnm.equals("PORequisiton_Authorisation") && !wfnm.equals("SupplierInvoiceLine_Approval_Non_Matched") && !wfnm.equals("SupplierInvoiceLine_POReceipt") && !wfnm.equals("Invoice_No_PO_Approval")) {
      mi.error("Invalid workflow name");
      return;
    }

    // - validate approver
    if (!appr.isEmpty()) {
      DBAction queryCMNUSR = database.table("CMNUSR").index("00").selection("JUUSID").build();
      DBContainer CMNUSR = queryCMNUSR.getContainer();
      CMNUSR.set("JUCONO", 0);
      CMNUSR.set("JUDIVI", "");
      CMNUSR.set("JUUSID", appr);
      if (!queryCMNUSR.read(CMNUSR)) {
        mi.error("Approver is invalid.");
        return;
      }
    }

    // - validate division
    if (!divi.isEmpty()) {
      DBAction queryCMNDIV = database.table("CMNDIV").index("00").selection("CCDIVI").build();
      DBContainer CMNDIV = queryCMNDIV.getContainer();
      CMNDIV.set("CCCONO", XXCONO);
      CMNDIV.set("CCDIVI", divi);
      if (!queryCMNDIV.read(CMNDIV)) {
        mi.error("Division is invalid.");
        return;
      }
    }

    // - validate supplier
    if (!suno.isEmpty()) {
      DBAction queryCIDMAS = database.table("CIDMAS").index("00").selection("IDSUNO").build();
      DBContainer CIDMAS = queryCIDMAS.getContainer();
      CIDMAS.set("IDCONO", XXCONO);
      CIDMAS.set("IDSUNO", suno);
      if (!queryCIDMAS.read(CIDMAS)) {
        mi.error("Supplier is invalid.");
        return;
      }
    }

    // - validate item no
    if (!itno.isEmpty()) {
      DBAction queryMITMAS = database.table("MITMAS").index("00").selection("MMITNO").build();
      DBContainer MITMAS = queryMITMAS.getContainer();
      MITMAS.set("MMCONO", XXCONO);
      MITMAS.set("MMITNO", itno);
      if (!queryMITMAS.read(MITMAS)) {
        mi.error("Item no is invalid.");
        return;
      }
    }

    // - validate currency
    if (!cucd.isEmpty()) {
      DBAction queryCSYTAB = database.table("CSYTAB").index("00").selection("CTSTKY").build();
      DBContainer CSYTAB = queryCSYTAB.getContainer();
      CSYTAB.set("CTCONO", XXCONO);
      CSYTAB.set("CTDIVI", "");
      CSYTAB.set("CTSTCO", "CUCD");
      CSYTAB.set("CTSTKY", cucd);
      CSYTAB.set("CTLNCD", "");
      if (!queryCSYTAB.read(CSYTAB)) {
        mi.error("Currency is invalid.");
        return;
      }
    }

    // - validate AIT1 Account
    if (!ait1.isEmpty()) {
      DBAction queryFCHACC = database.table("FCHACC").index("00").selection("EAAITM").build();
      DBContainer FCHACC = queryFCHACC.getContainer();
      FCHACC.set("EACONO", XXCONO);
      FCHACC.set("EADIVI", "");
      FCHACC.set("EAAITP", 1);
      FCHACC.set("EAAITM", ait1);
      if (!queryFCHACC.read(FCHACC)) {
        FCHACC.set("EADIVI", divi);
        if (!queryFCHACC.read(FCHACC)) {
          mi.error("Account is invalid.");
          return;
        }
      }
    }
    
    // - validate AIT2 Account
    if (!ait2.isEmpty()) {
      DBAction queryFCHACC = database.table("FCHACC").index("00").selection("EAAITM").build();
      DBContainer FCHACC = queryFCHACC.getContainer();
      FCHACC.set("EACONO", XXCONO);
      FCHACC.set("EADIVI", "");
      FCHACC.set("EAAITP", 2);
      FCHACC.set("EAAITM", ait2);
      if (!queryFCHACC.read(FCHACC)) {
        FCHACC.set("EADIVI", divi);
        if (!queryFCHACC.read(FCHACC)) {
          mi.error("Cost centre is invalid.");
          return;
        }
      }
    }

    // - validate AIT3 Account
    if (!ait3.isEmpty()) {
      DBAction queryFCHACC = database.table("FCHACC").index("00").selection("EAAITM").build();
      DBContainer FCHACC = queryFCHACC.getContainer();
      FCHACC.set("EACONO", XXCONO);
      FCHACC.set("EADIVI", "");
      FCHACC.set("EAAITP", 3);
      FCHACC.set("EAAITM", ait3);
      if (!queryFCHACC.read(FCHACC)) {
        FCHACC.set("EADIVI", divi);
        if (!queryFCHACC.read(FCHACC)) {
          mi.error("Unit is invalid.");
          return;
        }
      }
    }

    // - validate AIT4 Account
    if (!ait4.isEmpty()) {
      DBAction queryFCHACC = database.table("FCHACC").index("00").selection("EAAITM").build();
      DBContainer FCHACC = queryFCHACC.getContainer();
      FCHACC.set("EACONO", XXCONO);
      FCHACC.set("EADIVI", "");
      FCHACC.set("EAAITP", 4);
      FCHACC.set("EAAITM", ait4);
      if (!queryFCHACC.read(FCHACC)) {
        FCHACC.set("EADIVI", divi);
        if (!queryFCHACC.read(FCHACC)) {
          mi.error("Component is invalid.");
          return;
        }
      }
    }

    // - validate AIT5 Account
    if (!ait5.isEmpty()) {
      DBAction queryFCHACC = database.table("FCHACC").index("00").selection("EAAITM").build();
      DBContainer FCHACC = queryFCHACC.getContainer();
      FCHACC.set("EACONO", XXCONO);
      FCHACC.set("EADIVI", "");
      FCHACC.set("EAAITP", 5);
      FCHACC.set("EAAITM", ait5);
      if (!queryFCHACC.read(FCHACC)) {
        FCHACC.set("EADIVI", divi);
        if (!queryFCHACC.read(FCHACC)) {
          mi.error("Sub Account is invalid.");
          return;
        }
      }
    }

    // - validate AIT6 Account
    if (!ait6.isEmpty()) {
      DBAction queryFCHACC = database.table("FCHACC").index("00").selection("EAAITM").build();
      DBContainer FCHACC = queryFCHACC.getContainer();
      FCHACC.set("EACONO", XXCONO);
      FCHACC.set("EADIVI", "");
      FCHACC.set("EAAITP", 6);
      FCHACC.set("EAAITM", ait6);
      if (!queryFCHACC.read(FCHACC)) {
        FCHACC.set("EADIVI", divi);
        if (!queryFCHACC.read(FCHACC)) {
          mi.error("Project is invalid.");
          return;
        }
      }
    }

    // - validate AIT7 Account
    if (!ait7.isEmpty()) {
      DBAction queryFCHACC = database.table("FCHACC").index("00").selection("EAAITM").build();
      DBContainer FCHACC = queryFCHACC.getContainer();
      FCHACC.set("EACONO", XXCONO);
      FCHACC.set("EADIVI", "");
      FCHACC.set("EAAITP", 7);
      FCHACC.set("EAAITM", ait7);
      if (!queryFCHACC.read(FCHACC)) {
        FCHACC.set("EADIVI", divi);
        if (!queryFCHACC.read(FCHACC)) {
          mi.error("Element is invalid.");
          return;
        }
      }
    }

    // - validate requisitioner
    if (!purc.isEmpty()) {
      DBAction queryCMNUSR = database.table("CMNUSR").index("00").selection("JUUSID").build();
      DBContainer CMNUSR = queryCMNUSR.getContainer();
      CMNUSR.set("JUCONO", 0);
      CMNUSR.set("JUDIVI", "");
      CMNUSR.set("JUUSID", purc);
      if (!queryCMNUSR.read(CMNUSR)) {
        mi.error("Requisitioner is invalid.");
        return;
      }
    }
    
    // - validate planned PO 
    if (!plpn.isEmpty()) {
      DBAction queryMPOPLP = database.table("MPOPLP").index("00").selection("POCONO", "POPLPN", "POPLPS", "POPLP2").build();
      DBContainer MPOPLP = queryMPOPLP.getContainer();
      MPOPLP.set("POCONO", XXCONO);
      MPOPLP.set("POPLPN", plpn.toInteger());
      MPOPLP.set("POPLPS", plps.toInteger());
      MPOPLP.set("POPLP2", plp2.toInteger());
      if (!queryMPOPLP.read(MPOPLP)) {
        mi.error("Proposal number invalid");
        return;
      }    
    }

    // - validate puno
    if (!puno.isEmpty() && !wfnm.equals("PORequisiton_Authorisation")) {
       DBAction queryMPLINE = database.table("MPLINE").index("00").selection("IBPUNO", "IBPNLI", "IBPNLS", "IBPLPN", "IBPLPS").build();
       DBContainer MPLINE = queryMPLINE.getContainer();
       MPLINE.set("IBCONO", XXCONO);
       MPLINE.set("IBPUNO", puno);
       MPLINE.set("IBPNLI", pnli.toInteger());
       MPLINE.set("IBPNLS", pnls.toInteger());
       if (!queryMPLINE.read(MPLINE)) {    
        mi.error("PO line is invalid.");
        return;
      } else {
        plpn = MPLINE.get("IBPLPN").toString().trim();
        plps = MPLINE.get("IBPLPS").toString().trim();
        plp2 = "0";
      }
    }
    
    
    // - validate responsible
    if (!resp.isEmpty()) {
      DBAction queryCMNUSR = database.table("CMNUSR").index("00").selection("JUUSID").build();
      DBContainer CMNUSR = queryCMNUSR.getContainer();
      CMNUSR.set("JUCONO", 0);
      CMNUSR.set("JUDIVI", "");
      CMNUSR.set("JUUSID", resp);
      if (!queryCMNUSR.read(CMNUSR)) {
        mi.error("User Responsible is invalid.");
        return;
      }
    }
  	

    // - validate created by
    if (!crid.isEmpty()) {
      DBAction queryCMNUSR = database.table("CMNUSR").index("00").selection("JUUSID").build();
      DBContainer CMNUSR = queryCMNUSR.getContainer();
      CMNUSR.set("JUCONO", 0);
      CMNUSR.set("JUDIVI", "");
      CMNUSR.set("JUUSID", crid);
      if (!queryCMNUSR.read(CMNUSR)) {
        mi.error("Created by is invalid.");
        return;
      }
    }  	
    writeEXTWAT();
  }
  /**
  * writeEXTWAT - Write Purchase Authorisation extension table EXTAPR
  *
  */
  private void writeEXTWAT() {
  	ZoneId zid = ZoneId.of("Australia/Brisbane"); 
    LocalDateTime currentDateTimeNow = LocalDateTime.now(zid);
    int currentDate = currentDateTimeNow.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    int currentTime = Integer.valueOf(currentDateTimeNow.format(DateTimeFormatter.ofPattern("HHmmss")));
    String timestamp = currentDateTimeNow.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

	  DBAction actionEXTWAT = database.table("EXTWAT").build();
  	DBContainer EXTWAT = actionEXTWAT.getContainer();
  	EXTWAT.set("EXCONO", XXCONO);
  	EXTWAT.set("EXDIVI", divi);
  	EXTWAT.set("EXWFNM", wfnm);
  	if (!puno.isEmpty()) {
  	  EXTWAT.set("EXPUNO", puno);
  	}
  	if (!pnli.isEmpty()) {
  	  EXTWAT.set("EXPNLI", pnli.toInteger());
  	}
  	if (!pnls.isEmpty()) {
  	  EXTWAT.set("EXPNLS", pnls.toInteger());
  	}
  	if (!suno.isEmpty()) {
  	  EXTWAT.set("EXSUNO", suno);
  	}
  	if (!sino.isEmpty()) {
  	  EXTWAT.set("EXSINO", sino);
  	}
  	if (!inyr.isEmpty()) {
    	EXTWAT.set("EXINYR", inyr.toInteger());
  	}
  	if (!repn.isEmpty()) {
    	EXTWAT.set("EXREPN", repn.toInteger());
  	}
  	if (!appr.isEmpty()) {
  	  EXTWAT.set("EXAPPR", appr);
  	}
  	EXTWAT.set("EXACTA", acta);
  	EXTWAT.set("EXLMTS", timestamp);
  	EXTWAT.set("EXRGDT", currentDate);
  	EXTWAT.set("EXRGTM", currentTime);
  	EXTWAT.set("EXLMDT", currentDate);
  	EXTWAT.set("EXCHNO", 0);
  	EXTWAT.set("EXCHID", program.getUser());
  	if (!sunm.isEmpty()) {
  	  EXTWAT.set("EXSUNM", sunm);
  	}
  	if (!itno.isEmpty()) {
  	  EXTWAT.set("EXITNO", itno);
  	}
  	if (!cucd.isEmpty()) {
  	  EXTWAT.set("EXCUCD", cucd);
  	}
  	if (!pupr.isEmpty()) {
  	  EXTWAT.set("EXPUPR", Double.parseDouble(pupr));
  	}
  	if (!lnam.isEmpty()) {
  	  EXTWAT.set("EXLNAM", Double.parseDouble(lnam));
  	}
  	if (!lamt.isEmpty()) {
  	  EXTWAT.set("EXLAMT", Double.parseDouble(lamt));
  	}
  	if (!apro.isEmpty()) {
  	  EXTWAT.set("EXAPRO", apro);
  	}
  	if (!apli.isEmpty()) {
  	  EXTWAT.set("EXAPLI", Double.parseDouble(apli));
  	}
  	if (!purc.isEmpty()) {
  	  EXTWAT.set("EXPURC", purc);
  	}
  	if (!ait1.isEmpty()) {
  	  EXTWAT.set("EXAIT1", ait1);
  	}
  	if (!ait2.isEmpty()) {
  	  EXTWAT.set("EXAIT2", ait2);
  	}
  	if (!ait3.isEmpty()) {
  	  EXTWAT.set("EXAIT3", ait3);
  	}
  	if (!ait4.isEmpty()) {
  	  EXTWAT.set("EXAIT4", ait4);
  	}
  	if (!ait5.isEmpty()) {
  	  EXTWAT.set("EXAIT5", ait5);
  	}
  	if (!ait6.isEmpty()) {
  	  EXTWAT.set("EXAIT6", ait6);
  	}
  	if (!ait7.isEmpty()) {
  	  EXTWAT.set("EXAIT7", ait7);
  	}
  	if (!orqa.isEmpty()) {
  	  EXTWAT.set("EXORQA", Double.parseDouble(orqa));
  	}  	
  	if (!rorn.isEmpty()) {
  	  EXTWAT.set("EXRORN", rorn);
  	}
  	if (!plpn.isEmpty()) {
  	  EXTWAT.set("EXPLPN", plpn.toInteger());
  	}
  	if (!plps.isEmpty()) {
  	  EXTWAT.set("EXPLPS", plps.toInteger());
  	}
  	if (!plp2.isEmpty()) {
  	  EXTWAT.set("EXPLP2", plp2.toInteger());
  	}
  	if (!cppr.isEmpty()) {
  	  EXTWAT.set("EXCPPR", Double.parseDouble(cppr));
  	}  	
  	if (!camt.isEmpty()) {
  	  EXTWAT.set("EXCAMT", Double.parseDouble(camt));
  	}  	
  	if (!clam.isEmpty()) {
  	  EXTWAT.set("EXCLAM", Double.parseDouble(clam));
  	}  	
  	if (!site.isEmpty()) {
  	  EXTWAT.set("EXSITE", site);
  	}  	
  	if (!resp.isEmpty()) {
  	  EXTWAT.set("EXRESP", resp);
  	}  	
  	if (!crid.isEmpty()) {
  	  EXTWAT.set("EXCRID", crid);
  	}  	
    actionEXTWAT.insert(EXTWAT, recordExists);
	}
  /**
   * recordExists - return record already exists error message to the MI
   *
  */
  Closure recordExists = {
	  mi.error("Record already exists");
  }
  
}