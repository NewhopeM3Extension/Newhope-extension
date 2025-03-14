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
 *Nbr               Date      User id     Description
 *NHG_Phase_2       20240612  KVERCO      Workflow action Audit Trail
 *
 */

/**
* - Add Audit Trail extension table row
*/
public class Add extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String divi;
  private String pk01;
  private String pk02;
  private String pk03;
  private String pk04;
  private String pk05;
  private String pk06;
  private String pk07;
  private String pk08;
  private String appr;
  private String suno;
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
  private String puno = "";
  private String pnli = "";
  private String pnls = "";
  private String plpn = "";
  private String plps = "";
  private String plp2 = "";
  

  private boolean found;
  

  private int XXCONO;
 
 /*
  * Add Workflow Action Audit Trail extension table row
 */
  public Add(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
	  
  }
  
  public void main() {
    
  	divi = mi.inData.get("DIVI") == null ? '' : mi.inData.get("DIVI").trim();
  	if (divi == "?") {
  	  divi = "";
  	} 
  	pk01 = mi.inData.get("PK01") == null ? '' : mi.inData.get("PK01").trim();
  	if (pk01 == "?") {
  	  pk01 = "";
  	} 
  	pk02 = mi.inData.get("PK02") == null ? '' : mi.inData.get("PK02").trim();
  	if (pk02 == "?") {
  	  pk02 = " ";
  	} 
  	pk03 = mi.inData.get("PK03") == null ? '' : mi.inData.get("PK03").trim();
  	if (pk03 == "?") {
  	  pk03 = "0";
  	} 
  	pk04 = mi.inData.get("PK04") == null ? '' : mi.inData.get("PK04").trim();
  	if (pk04 == "?") {
  	  pk04 = "0";
  	} 
  	pk05 = mi.inData.get("PK05") == null ? '' : mi.inData.get("PK05").trim();
  	if (pk05 == "?") {
  	  pk05 = "";
  	} 
  	pk06 = mi.inData.get("PK06") == null ? '' : mi.inData.get("PK06").trim();
  	if (pk06 == "?") {
  	  pk06 = "";
  	} 
  	pk07 = mi.inData.get("PK07") == null ? '' : mi.inData.get("PK07").trim();
  	if (pk07 == "?") {
  	  pk07 = "";
  	} 
  	pk08 = mi.inData.get("PK08") == null ? '' : mi.inData.get("PK08").trim();
  	if (pk08 == "?") {
  	  pk08 = "";
  	} 

		XXCONO = (Integer)program.LDAZD.CONO;

    // Validate input fields  	
    
    // Validate input fields for Supplier Invoice
		if (pk01.isEmpty()) {
      mi.error("Primary key 1 Workflow name must be entered");
      return;
    }
    if (pk01.equals("Invoice_No_PO_Approval")) {
		   if (pk02.isEmpty()) {
         mi.error("Primary key 2 Supplier no must be entered");
         return;
       }
		   if (pk03.isEmpty()) {
         mi.error("Primary key 3 Supplier Invoice no must be entered");
         return;
       }
		   if (pk04.isEmpty()) {
         mi.error("Primary key 4 Invoice year must be entered");
         return;
       }
    }

    // validate input fields for Supplier Invoice and Purchase Order Line
    if (pk01.equals("SupplierInvoiceLine_Approval_Non_Matched") || pk01.equals("SupplierInvoiceLine_POReceipt") || pk01.equals("POLine_Authorisation")) {
		   if (pk02.isEmpty()) {
          mi.error("Primary key 2 PO no must be entered");
          return;
       }
		   if (pk03.isEmpty()) {
          mi.error("Primary key 3 PO line no must be entered");
          return;
       }
		   if (pk04.isEmpty()) {
          mi.error("Primary key 4 PO sub line no must be entered");
          return;
		   }      
       if (pk01.equals("SupplierInvoiceLine_Approval_Non_Matched") || pk01.equals("SupplierInvoiceLine_POReceipt")) {
		      if (pk05.isEmpty()) {
            mi.error("Primary key 5 Supplier no must be entered");
            return;
          }
		      if (pk06.isEmpty()) {
            mi.error("Primary key 6 Supplier Invoice no must be entered");
            return;
          }
		      if (pk07.isEmpty()) {
            mi.error("Primary key 7 Invoice year must be entered");
            return;
          }
		      if (pk08.isEmpty()) {
            mi.error("Primary key 8 Receive no must be entered");
            return;
		      }
       }
       puno = pk02;
       pnli = pk03;
       pnls = pk04;
    }
    
    // validate input fielsd for Purchase Requisition
    if (pk01.equals("PORequisiton_Authorisation")) {
		   if (pk02.isEmpty()) {
          mi.error("Primary key 2 PR no must be entered");
          return;
       }
		   if (pk03.isEmpty()) {
          mi.error("Primary key 3 PR sub line no must be entered");
          return;
       }
		   if (pk04.isEmpty()) {
          mi.error("Primary key 3 PR sub line no 2 must be entered");
          return;
       }
       plpn = pk02;
       plps = pk03;
       plp2 = pk04;
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
  	suno = mi.inData.get("SUNO") == null ? '' : mi.inData.get("SUNO").trim();
  	if (suno == "?") {
  	  suno = "";
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


		XXCONO = (Integer)program.LDAZD.CONO;

    // Validate input fields  	
		if (pk01.isEmpty()) {
      mi.error("Primary key 1 must be entered");
      return;
    }
		if (pk02.isEmpty()) {
      mi.error("Primary key 2 must be entered");
      return;
    }
		if (pk03.isEmpty()) {
      mi.error("Primary key 3 must be entered");
      return;
    }

    /* validate asts */
    if (!pk01.equals("POLine_Authorisation") && !pk01.equals("PORequisiton_Authorisation") && !pk01.equals("SupplierInvoiceLine_Approval_Non_Matched") && !pk01.equals("SupplierInvoiceLine_POReceipt") && !pk01.equals("Invoice_No_PO_Approval")) {
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
    if (!puno.isEmpty()) {
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
  	
    writeEXTAWA();
  }
  /**
  * writeEXTAWA - Write Purchase Authorisation extension table EXTAPR
  *
  */
  def writeEXTAWA() {
  	ZoneId zid = ZoneId.of("Australia/Brisbane"); 
    LocalDateTime currentDateTimeNow = LocalDateTime.now(zid);
    int currentDate = currentDateTimeNow.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    int currentTime = Integer.valueOf(currentDateTimeNow.format(DateTimeFormatter.ofPattern("HHmmss")));
    String timestamp = currentDateTimeNow.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

	  DBAction actionEXTAWA = database.table("EXTAWA").build();
  	DBContainer EXTAWA = actionEXTAWA.getContainer();
  	EXTAWA.set("EXCONO", XXCONO);
  	EXTAWA.set("EXDIVI", divi);
  	EXTAWA.set("EXPK01", pk01);
  	EXTAWA.set("EXPK02", pk02);
  	EXTAWA.set("EXPK03", pk03);
  	EXTAWA.set("EXPK04", pk04);
  	EXTAWA.set("EXPK05", pk05);
  	EXTAWA.set("EXPK06", pk06);
  	EXTAWA.set("EXPK07", pk07);
  	EXTAWA.set("EXPK08", pk08);
  	EXTAWA.set("EXAPPR", appr);
  	EXTAWA.set("EXACTA", acta);
  	EXTAWA.set("EXLMTS", timestamp);
  	EXTAWA.set("EXRGDT", currentDate);
  	EXTAWA.set("EXRGTM", currentTime);
  	EXTAWA.set("EXLMDT", currentDate);
  	EXTAWA.set("EXCHNO", 0);
  	EXTAWA.set("EXCHID", program.getUser());
  	if (!suno.isEmpty()) {
  	  EXTAWA.set("EXSUNO", suno);
  	}
  	if (!sunm.isEmpty()) {
  	  EXTAWA.set("EXSUNM", sunm);
  	}
  	if (!itno.isEmpty()) {
  	  EXTAWA.set("EXITNO", itno);
  	}
  	if (!cucd.isEmpty()) {
  	  EXTAWA.set("EXCUCD", cucd);
  	}
  	if (!pupr.isEmpty()) {
  	  EXTAWA.set("EXPUPR", Double.parseDouble(pupr));
  	}
  	if (!lnam.isEmpty()) {
  	  EXTAWA.set("EXLNAM", Double.parseDouble(lnam));
  	}
  	if (!lamt.isEmpty()) {
  	  EXTAWA.set("EXLAMT", Double.parseDouble(lamt));
  	}
  	if (!apro.isEmpty()) {
  	  EXTAWA.set("EXAPRO", apro);
  	}
  	if (!apli.isEmpty()) {
  	  EXTAWA.set("EXAPLI", Double.parseDouble(apli));
  	}
  	if (!purc.isEmpty()) {
  	  EXTAWA.set("EXPURC", purc);
  	}
  	if (!ait1.isEmpty()) {
  	  EXTAWA.set("EXAIT1", ait1);
  	}
  	if (!ait2.isEmpty()) {
  	  EXTAWA.set("EXAIT2", ait2);
  	}
  	if (!ait3.isEmpty()) {
  	  EXTAWA.set("EXAIT3", ait3);
  	}
  	if (!ait4.isEmpty()) {
  	  EXTAWA.set("EXAIT4", ait4);
  	}
  	if (!ait5.isEmpty()) {
  	  EXTAWA.set("EXAIT5", ait5);
  	}
  	if (!ait6.isEmpty()) {
  	  EXTAWA.set("EXAIT6", ait6);
  	}
  	if (!ait7.isEmpty()) {
  	  EXTAWA.set("EXAIT7", ait7);
  	}
  	if (!orqa.isEmpty()) {
  	  EXTAWA.set("EXORQA", Double.parseDouble(orqa));
  	}  	
  	if (!rorn.isEmpty()) {
  	  EXTAWA.set("EXRORN", rorn);
  	}
  	if (!puno.isEmpty()) {
  	  EXTAWA.set("EXPUNO", puno);
  	}
  	if (!pnli.isEmpty()) {
  	  EXTAWA.set("EXPNLI", pnli.toInteger());
  	}
  	if (!pnls.isEmpty()) {
  	  EXTAWA.set("EXPNLS", pnls.toInteger());
  	}
  	if (!plpn.isEmpty()) {
  	  EXTAWA.set("EXPLPN", plpn.toInteger());
  	}
  	if (!plps.isEmpty()) {
  	  EXTAWA.set("EXPLPS", plps.toInteger());
  	}
  	if (!plp2.isEmpty()) {
  	  EXTAWA.set("EXPLP2", plp2.toInteger());
  	}
    actionEXTAWA.insert(EXTAWA, recordExists);
	}
  /**
   * recordExists - return record already exists error message to the MI
   *
  */
  Closure recordExists = {
	  mi.error("Record already exists");
  }
  
}
