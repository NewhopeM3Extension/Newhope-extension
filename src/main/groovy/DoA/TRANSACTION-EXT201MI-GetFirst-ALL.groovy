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
 public class GetFirst extends ExtendM3Transaction {
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
  
  private int XXCONO;
  
  private int noOfRows = 0;
  private String firstTrigger = "N";
  private String extapr_RGDT = "";
  private String acta = "";

  private List lstEXTAWA;
  private boolean createdBy;
   
 /*
  * Get Audit Trail extension table row
 */
  public GetFirst(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
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
  	pk01 = mi.inData.get("PK01") == null ? '' : mi.inData.get("PK01").trim();
    if (pk01.isEmpty()) {
      mi.error("Primary key 1 must be entered");
      return;
    }
  	pk02 = mi.inData.get("PK02") == null ? '' : mi.inData.get("PK02").trim();
    if (pk02.isEmpty()) {
      mi.error("Primary key 2 must be entered");
      return;
    }
  	pk03 = mi.inData.get("PK03") == null ? '' : mi.inData.get("PK03").trim();
    if (pk03.isEmpty()) {
      mi.error("Primary key 3 must be entered");
      return;
    }
  	pk04 = mi.inData.get("PK04") == null ? '' : mi.inData.get("PK04").trim();
    if (pk04.isEmpty()) {
      mi.error("Primary key 4 must be entered");
      return;
    }
  	pk05 = mi.inData.get("PK05") == null ? '' : mi.inData.get("PK05").trim();
  	pk06 = mi.inData.get("PK06") == null ? '' : mi.inData.get("PK06").trim();
  	pk07 = mi.inData.get("PK07") == null ? '' : mi.inData.get("PK07").trim();
  	pk08 = mi.inData.get("PK08") == null ? '' : mi.inData.get("PK08").trim();

    XXCONO = (Integer)program.LDAZD.CONO;

    // Get the creation date from EXTAPR for PO and PR
    if (pk01.equals("POLine_Authorisation") || pk01.equals("PORequisiton_Authorisation")) {
      // Get the division of PO or PR 
      if (pk01.equals("PORequisiton_Authorisation")) {
        DBAction queryMPOPLP = database.table("MPOPLP").index("00").selection("POCONO", "POPLPN", "POPLPS", "POPLP2", "POFACI").build();
        DBContainer containerMPOPLP = queryMPOPLP.getContainer();
        containerMPOPLP.set("POCONO", XXCONO);
        containerMPOPLP.set("POPLPN", pk02.toInteger());
        containerMPOPLP.set("POPLPS", pk03.toInteger());
        containerMPOPLP.set("POPLP2", pk04.toInteger());
        if (queryMPOPLP.read(containerMPOPLP)) {
          divi = containerMPOPLP.get("POFACI");
        } else {
          divi = "";
        }
      }
      if (pk01.equals("POLine_Authorisation")) {
        DBAction queryMPHEAD = database.table("MPHEAD").index("00").selection("IACONO", "IAPUNO", "IADIVI").build();
        DBContainer containerMPHEAD = queryMPHEAD.getContainer();
        containerMPHEAD.set("IACONO", XXCONO);
        containerMPHEAD.set("IAPUNO", pk02);
        if (queryMPHEAD.read(containerMPHEAD)) {
          divi = containerMPHEAD.get("IADIVI");
        } else {
          divi = "";
        }
      }
      DBAction queryEXTAPR = database.table("EXTAPR").index("00").selection("EXCONO", "EXPUNO", "EXPNLI", "EXPNLS", "EXRGDT").build();
      DBContainer containerEXTAPR = queryEXTAPR.getContainer();
      containerEXTAPR.set("EXCONO", XXCONO);
      containerEXTAPR.set("EXPUNO", pk02);
      containerEXTAPR.set("EXPNLI", pk03.toInteger());
      containerEXTAPR.set("EXPNLS", pk04.toInteger());
      if (queryEXTAPR.read(containerEXTAPR)) {
        extapr_RGDT = containerEXTAPR.get("EXRGDT".toString());
      } else {
        extapr_RGDT = "0";
      }
    }
    
    // Get the no of Rows and creation date for action 'Created By'
    lstEXTAWA = new ArrayList();
    
    DBAction queryEXTAWA = database.table("EXTAWA").index("00").selection("EXCONO", "EXDIVI", "EXPK01", "EXPK02", "EXPK03", "EXPK04", "EXPK05", "EXPK06", "EXPK07", "EXPK08", "EXAPPR", "EXACTA").build();
    DBContainer containerEXTAWA = queryEXTAWA.getContainer();
    containerEXTAWA.set("EXCONO", XXCONO);
    containerEXTAWA.set("EXDIVI", divi);
    containerEXTAWA.set("EXPK01", pk01);
    containerEXTAWA.set("EXPK02", pk02);
    containerEXTAWA.set("EXPK03", pk03);
    containerEXTAWA.set("EXPK04", pk04);
    containerEXTAWA.set("EXPK05", pk05);
    containerEXTAWA.set("EXPK06", pk06);
    containerEXTAWA.set("EXPK07", pk07);
    containerEXTAWA.set("EXPK08", pk08);

    queryEXTAWA.readAll(containerEXTAWA, 10, 999, listEXTAWA);
    
    if (lstEXTAWA.size() > 0) {
      for (int i=0;i<lstEXTAWA.size();i++) {
        Map<String, String> record = (Map<String, String>) lstEXTAWA[i];
        noOfRows++;
/*        mi.outData.put("CONO", record.CONO.trim());
        mi.outData.put("DIVI", record.DIVI.trim());
        mi.outData.put("PK01", record.PK01);
        mi.outData.put("PK02", record.PK02);
        mi.outData.put("PK03", record.PK03);
        mi.outData.put("PK04", record.PK04);
        mi.outData.put("PK05", record.PK05);
        mi.outData.put("PK06", record.PK06);
        mi.outData.put("PK07", record.PK07);
        mi.outData.put("PK08", record.PK08);
        mi.outData.put("APPR", record.APPR);
        mi.outData.put("ACTA", record.ACTA);*/
        //acta = acta + record.ACTA;
        if (record.ACTA.equals("Created by")) {
          createdBy = true;
        }
      }
    }
    
    // Get current Date
    //mi.error("No of rows " + noOfRows + " createdBy " + createdBy);
    String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    if (noOfRows > 1 || createdBy == true) {
      firstTrigger = "N";
    } else {
      if (noOfRows == 0 || extapr_RGDT.equals("0") || (createdBy = false && currentDate.equals(extapr_RGDT)) ) {
        firstTrigger = "Y";
      }
    }
    //mi.error("No or Rows " + noOfRows + " EXTAPR_RGDT " + extapr_RGDT + " current date "  + currentDate + " division " + divi + " acta " + acta + " created by " + createdBy + " firstTrigger " + firstTrigger);
    mi.outData.put("FLAG", firstTrigger);
    mi.write();

  }
  
  //listEXTUMO - Callback function to return EXTUMO
  Closure<?> listEXTAWA = { DBContainer contEXTAWA ->
    String cono = contEXTAWA.get("EXCONO").toString().trim();
    String divi = contEXTAWA.get("EXDIVI").toString().trim();
    String pk01 = contEXTAWA.get("EXPK01").toString().trim();
    String pk02 = contEXTAWA.get("EXPK02").toString();
    String pk03 = contEXTAWA.get("EXPK03").toString();
    String pk04 = contEXTAWA.get("EXPK04").toString();
    String pk05 = contEXTAWA.get("EXPK05").toString();
    String pk06 = contEXTAWA.get("EXPK06").toString();
    String pk07 = contEXTAWA.get("EXPK07").toString();
    String pk08 = contEXTAWA.get("EXPK08").toString();
    String appr = contEXTAWA.get("EXAPPR").toString();
    String acta = contEXTAWA.get("EXACTA").toString().trim();
    logger.debug(appr);
    logger.debug(acta);

    Map<String,String> map = [CONO: cono, DIVI: divi, PK01: pk01, PK02: pk02, PK03: pk03, PK04: pk04, PK05: pk05, PK06: pk06, PK07: pk07, PK08: pk08, APPR: appr, ACTA: acta];
    lstEXTAWA.add(map);  
  }
}