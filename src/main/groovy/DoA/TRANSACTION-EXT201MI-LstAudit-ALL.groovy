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
 *DoA               20250429  KVERCO      Get list of Audit record
 *
*/

 /**
  * Get Audit Trail extension table row
 */
 public class LstAudit extends ExtendM3Transaction {
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
  private String acta;
  
  private int XXCONO;
  
  private List lstEXTWAT;  
  private String ivqa;

 /*
  * Get Audit Trail extension table row
 */
  public LstAudit(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
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
  	pnli = mi.inData.get("PNLI") == null ? '' : mi.inData.get("PNLI").trim();
  	pnls = mi.inData.get("PNLS") == null ? '' : mi.inData.get("PNLS").trim();
  	suno = mi.inData.get("SUNO") == null ? '' : mi.inData.get("SUNO").trim();
  	sino = mi.inData.get("SINO") == null ? '' : mi.inData.get("SINO").trim();
  	inyr = mi.inData.get("INYR") == null ? '' : mi.inData.get("INYR").trim();
  	repn = mi.inData.get("REPN") == null ? '' : mi.inData.get("REPN").trim();
  	acta = mi.inData.get("ACTA") == null ? '' : mi.inData.get("ACTA").trim();

    XXCONO = (Integer)program.LDAZD.CONO;
    lstEXTWAT = new ArrayList();
    
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
    }    
	  if (inyr.isEmpty()) {
       inyr = "0";
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

    DBAction queryEXTWAT = database.table("EXTWAT").index("00").selection("EXCONO", "EXDIVI", "EXWFNM", "EXPUNO", "EXPNLI", "EXPNLS", "EXSUNO", "EXSINO", "EXINYR", "EXREPN", "EXLMTS",
                                                                          "EXACTA", "EXAPPR", "EXSUNM", "EXITNO", "EXPUPR", "EXORQA", "EXCUCD", "EXLNAM", "EXLAMT", "EXAPRO", "EXAPLI", "EXPURC",
                                                                          "EXAIT1", "EXAIT2", "EXAIT3", "EXAIT4", "EXAIT5", "EXAIT6", "EXAIT7", "EXCPPR", "EXCAMT", "EXCLAM", "EXCRID", "EXRESP",
                                                                          "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build();
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
      }
    } else {
      mi.error("Record does not exist in EXTWAT.");
      return;
    }
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
    String cacta = contEXTWAT.get("EXACTA").toString().trim();
    String sunm = contEXTWAT.get("EXSUNM").toString();
    String itno = contEXTWAT.get("EXITNO").toString();
    String purc = contEXTWAT.get("EXPURC").toString();
    String orqa = contEXTWAT.get("EXORQA").toString();
    String pupr = contEXTWAT.get("EXPUPR").toString();
    String cucd = contEXTWAT.get("EXCUCD").toString();
    String lamt = contEXTWAT.get("EXLAMT").toString();
    String lnam = contEXTWAT.get("EXLNAM").toString();
    String apro = contEXTWAT.get("EXAPRO").toString();
    String apli = contEXTWAT.get("EXAPLI").toString();
    String ait1 = contEXTWAT.get("EXAIT1").toString();
    String ait2 = contEXTWAT.get("EXAIT2").toString();
    String ait3 = contEXTWAT.get("EXAIT3").toString();
    String ait4 = contEXTWAT.get("EXAIT4").toString();
    String ait5 = contEXTWAT.get("EXAIT5").toString();
    String ait6 = contEXTWAT.get("EXAIT6").toString();
    String ait7 = contEXTWAT.get("EXAIT7").toString();
    String rorn = contEXTWAT.get("EXRORN").toString();
    String site = contEXTWAT.get("EXSITE").toString();
    String cppr = contEXTWAT.get("EXCPPR").toString();
    String camt = contEXTWAT.get("EXCAMT").toString();
    String clam = contEXTWAT.get("EXCLAM").toString();
    String crid = contEXTWAT.get("EXCRID").toString();
    String resp = contEXTWAT.get("EXRESP").toString();
    String lmts = contEXTWAT.get("EXLMTS").toString();
    String rgdt = contEXTWAT.get("EXRGDT").toString();
    String rgtm = contEXTWAT.get("EXRGTM").toString();
    String lmdt = contEXTWAT.get("EXLMDT").toString();
    String chid = contEXTWAT.get("EXCHID").toString();
    String chno = contEXTWAT.get("EXCHNO").toString();

    if (acta.equals(cacta) || acta == null || acta.isEmpty()) {
        mi.outData.put("CONO", cono);
        mi.outData.put("DIVI", divi);
        mi.outData.put("WFNM", wfnm);
        mi.outData.put("PUNO", puno);
        mi.outData.put("PNLI", pnli);
        mi.outData.put("PNLS", pnls);
        mi.outData.put("SUNO", suno);
        mi.outData.put("SINO", sino);
        mi.outData.put("INYR", inyr);
        mi.outData.put("REPN", repn);
        mi.outData.put("APPR", appr);
        mi.outData.put("ACTA", cacta);
        mi.outData.put("SUNM", sunm);
        mi.outData.put("ITNO", itno);
        mi.outData.put("PURC", purc);
        mi.outData.put("ORQA", orqa);
        mi.outData.put("PUPR", pupr);
        mi.outData.put("CUCD", cucd);
        mi.outData.put("LNAM", lnam);
        mi.outData.put("LAMT", lamt);
        mi.outData.put("APRO", apro);
        mi.outData.put("APLI", apli);
        mi.outData.put("AIT1", ait1);
        mi.outData.put("AIT2", ait2);
        mi.outData.put("AIT3", ait3);
        mi.outData.put("AIT4", ait4);
        mi.outData.put("AIT5", ait5);
        mi.outData.put("AIT6", ait6);
        mi.outData.put("AIT7", ait7);
        mi.outData.put("RORN", rorn);
        mi.outData.put("SITE", site);
        mi.outData.put("CPPR", cppr);
        mi.outData.put("CAMT", camt);
        mi.outData.put("CLAM", clam);
        mi.outData.put("CRID", crid);
        mi.outData.put("RESP", resp);
        mi.outData.put("LMTS", lmts);
        mi.outData.put("RGDT", rgdt);
        mi.outData.put("RGTM", rgtm);
        mi.outData.put("LMDT", lmdt);
        mi.outData.put("CHNO", chno);
        mi.outData.put("CHID", chid);

        mi.write();
    }
    
  }

}