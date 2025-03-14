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

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *FELIX             20250121  KVERCO      Get the supplier code from the description 
 *
*/

 /**
  * Get Supplier Group table row
 */
 public class GetSUCL extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String tx40;

  private int XXCONO;
  
    private List lstCSYTAB;
   
 /*
  * Get Supplier Group
 */
  public GetSUCL(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
    
  }
  
  public void main() {
  	tx40 = mi.inData.get("TX40") == null ? '' : mi.inData.get("TX40").trim();
    if (tx40.isEmpty()) {
      mi.error("Description must be entered");
      return;
    }

    XXCONO = (Integer)program.LDAZD.CONO;
    
    lstCSYTAB = new ArrayList();
    
    DBAction query = database.table("CSYTAB").index("30").selection("CTCONO", "CTSTCO", "CTSTKY", "CTTX40").build();
    DBContainer container = query.getContainer();
    container.set("CTCONO", XXCONO);
    container.set("CTDIVI", ' ');
    container.set("CTSTCO", 'SUCL');
    container.set("CTTX40", tx40.trim());
    query.readAll(container, 4, 999, listCSYTAB);
    
    if (lstCSYTAB.size() > 0) {
      Map<String, String> record = (Map<String, String>) lstCSYTAB[0];
      //mi.outData.put("CONO", XXCONO.toString());
      //mi.outData.put("STCO", container.get("CTSTCO").toString());
      mi.outData.put("STKY", record.STKY.trim());
      mi.outData.put("TX40", record.TX40.trim());
      mi.write();
    } else {
      mi.error("Supplier Group does not exist.");
      return;
    }
  }
  
  //listCSYTAB - Callback function to return CSYTAB
  Closure<?> listCSYTAB = { DBContainer contCSYTAB ->
    String stky = contCSYTAB.get("CTSTKY").toString().trim();
    String tx40 = contCSYTAB.get("CTTX40").toString().trim();
    Map<String,String> map = [STKY: stky, TX40: tx40];
    lstCSYTAB.add(map);
  }  
  
}
