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
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.util.List;

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *DoA               20241206  KVERCO      Get record 
 *
*/

 /**
  * Get Audit Trail extension table row
 */
 public class GetFirstTrigger extends ExtendM3Transaction {
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
  
  private int XXCONO;
  
  private int noOfRows = 0;
  private String firstTrigger = "N";
  private String extapr_RGDT = "";
  private String acta = "";

  private List lstEXTWAT;
  private boolean createdBy;
   
 /*
  * Get Audit Trail extension table row
 */
  public GetFirstTrigger(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
    
  }
  
  public void main() {
    
    noOfRows = 0;
    createdBy = false;
    extapr_RGDT = "0";
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
  	pnli = mi.inData.get("PNLI") == null ? '' : mi.inData.get("PNLI").trim();
  	pnls = mi.inData.get("PNLS") == null ? '' : mi.inData.get("PNLS").trim();
  	suno = mi.inData.get("SUNO") == null ? '' : mi.inData.get("SUNO").trim();
  	sino = mi.inData.get("SINO") == null ? '' : mi.inData.get("SINO").trim();
  	inyr = mi.inData.get("INYR") == null ? '' : mi.inData.get("INYR").trim();
  	repn = mi.inData.get("REPN") == null ? '' : mi.inData.get("REPN").trim();

    XXCONO = (Integer)program.LDAZD.CONO;

    // Validate input fields for workflow 
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
    // validate input fields for Supplier Invoice 
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

    // Get the creation date from EXTAPR for PO and PR
    if (wfnm.equals("POLine_Authorisation") || wfnm.equals("PORequisiton_Authorisation")) {
      // Get the division of PO or PR 
      if (wfnm.equals("PORequisiton_Authorisation")) {
        DBAction queryMPOPLP = database.table("MPOPLP").index("00").selection("POCONO", "POPLPN", "POPLPS", "POPLP2", "POFACI").build();
        DBContainer containerMPOPLP = queryMPOPLP.getContainer();
        containerMPOPLP.set("POCONO", XXCONO);
        containerMPOPLP.set("POPLPN", puno.toInteger());
        containerMPOPLP.set("POPLPS", pnli.toInteger());
        containerMPOPLP.set("POPLP2", pnls.toInteger());
        if (queryMPOPLP.read(containerMPOPLP)) {
          divi = containerMPOPLP.get("POFACI");
        } else {
          divi = "";
        }
      }
      if (wfnm.equals("POLine_Authorisation")) {
        DBAction queryMPHEAD = database.table("MPHEAD").index("00").selection("IACONO", "IAPUNO", "IADIVI").build();
        DBContainer containerMPHEAD = queryMPHEAD.getContainer();
        containerMPHEAD.set("IACONO", XXCONO);
        containerMPHEAD.set("IAPUNO", puno);
        if (queryMPHEAD.read(containerMPHEAD)) {
          divi = containerMPHEAD.get("IADIVI");
        } else {
          divi = "";
        }
      }
      DBAction queryEXTAPR = database.table("EXTAPR").index("00").selection("EXCONO", "EXPUNO", "EXPNLI", "EXPNLS", "EXRGDT").build();
      DBContainer containerEXTAPR = queryEXTAPR.getContainer();
      containerEXTAPR.set("EXCONO", XXCONO);
      containerEXTAPR.set("EXPUNO", puno);
      containerEXTAPR.set("EXPNLI", pnli.toInteger());
      containerEXTAPR.set("EXPNLS", pnls.toInteger());
      if (queryEXTAPR.read(containerEXTAPR)) {
        extapr_RGDT = containerEXTAPR.get("EXRGDT".toString());
      } else {
        extapr_RGDT = "0";
      }
    }
    
    // Get the no of Rows and creation date for action 'Created By'
    lstEXTWAT = new ArrayList();
    
    DBAction queryEXTWAT = database.table("EXTWAT").index("00").selection("EXCONO", "EXDIVI", "EXWFNM", "EXPUNO", "EXPNLI", "EXPNLS", "EXSUNO", "EXSINO", "EXINYR", "EXREPN", "EXAPPR", "EXACTA").build();
    DBContainer containerEXTWAT = queryEXTWAT.getContainer();
    containerEXTWAT.set("EXCONO", XXCONO);
    containerEXTWAT.set("EXDIVI", divi);
    containerEXTWAT.set("EXWFNM", wfnm);
    containerEXTWAT.set("EXPUNO", puno);
    containerEXTWAT.set("EXPNLI", pnli.toInteger());
    containerEXTWAT.set("EXPNLS", pnls.toInteger());
    containerEXTWAT.set("EXSUNO", suno);
    containerEXTWAT.set("EXSINO", sino);
    containerEXTWAT.set("EXINYR", inyr.toInteger());
    containerEXTWAT.set("EXREPN", repn.toInteger());

    queryEXTWAT.readAll(containerEXTWAT, 10, 999, listEXTWAT);
    
    if (lstEXTWAT.size() > 0) {
      for (int i=0;i<lstEXTWAT.size();i++) {
        Map<String, String> record = (Map<String, String>) lstEXTWAT[i];
        noOfRows++;
        if (acta.equals("Created by")) {
          createdBy = true;
          break;
        }
      }
    }
    
    // Get current Date
    String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    if (noOfRows > 1 || createdBy == true) {
      firstTrigger = "N";
    } else {
      if (noOfRows == 0 || extapr_RGDT.equals("0") || (createdBy = false && currentDate.equals(extapr_RGDT)) ) {
        firstTrigger = "Y";
      }
    }
    mi.outData.put("FLAG", firstTrigger);
    mi.write();

  }
  
  //listEXTUMO - Callback function to return EXTUMO
  Closure<?> listEXTWAT = { DBContainer contEXTWAT ->
    String cono = contEXTWAT.get("EXCONO").toString().trim();
    String divi = contEXTWAT.get("EXDIVI").toString().trim();
    String wfnm = contEXTWAT.get("EXWFNM").toString().trim();
    String puno = contEXTWAT.get("EXPUNO").toString();
    String pnli = contEXTWAT.get("EXPNLI").toString();
    String pnls = contEXTWAT.get("EXPNLS").toString();
    String suno = contEXTWAT.get("EXSUNO").toString();
    String sino = contEXTWAT.get("EXSINO").toString();
    String inyr = contEXTWAT.get("EXINYR").toString();
    String repn = contEXTWAT.get("EXREPN").toString();
    String appr = contEXTWAT.get("EXAPPR").toString();
    String acta = contEXTWAT.get("EXACTA").toString().trim();

    Map<String,String> map = [CONO: cono, DIVI: divi, WFNM: wfnm, PUNO: puno, PNLI: pnli, PNLS: pnls, SUNO: suno, SINO: sino, INYR: inyr, REPN: repn, APPR: appr, ACTA: acta];
    lstEXTWAT.add(map);  
  }
}