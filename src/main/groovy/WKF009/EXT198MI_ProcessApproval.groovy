/***************************************************************
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
 import java.time.ZoneId;
 
/*
* Modification area - M3
* Nbr               Date      User id     Description
* WKF009            20230927  KVERCO      Supplier invoice approval run
* WKF009            20240305  KVERCO      Update to workaround 33333 status error on header when all lines are 33334
*/

/**
* ProcessApproval - Processing supplier invoices ready for approval to pay
*/
public class ProcessApproval extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  
  private int XXCONO;
  
  public ProcessApproval(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.miCaller = miCaller;
    this.logger = logger;
    this.program = program;
  }
  
  public void main() {

    XXCONO= program.LDAZD.CONO;
    
  	String referenceId = UUID.randomUUID().toString();
    setupData(referenceId);
      def params = ["JOB": "EXT110", "TX30": "Invoice Approval Run", "XCAT": "010", "SCTY": "1", "XNOW": "1", "UUID": referenceId]; // single run - now
      miCaller.call("SHS010MI", "SchedXM3Job", params, { result -> });
  }

  /**
	 * setupData  - write to EXTJOB
	 *
	 */
  private void setupData(String referenceId) {
    String data = "";
    
    int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
    DBAction actionEXTJOB = database.table("EXTJOB").build();
  	DBContainer EXTJOB = actionEXTJOB.getContainer();
  	EXTJOB.set("EXCONO", XXCONO);
  	EXTJOB.set("EXRFID", referenceId);
  	EXTJOB.set("EXDATA", data);
    EXTJOB.set("EXRGDT", currentDate);
  	EXTJOB.set("EXRGTM", currentTime);
  	EXTJOB.set("EXLMDT", currentDate);
  	EXTJOB.set("EXCHNO", 0);
  	EXTJOB.set("EXCHID", program.getUser());
    actionEXTJOB.insert(EXTJOB);
  }
}
