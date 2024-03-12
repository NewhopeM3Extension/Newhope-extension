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
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.time.ZoneId;
 
 /*
 *Modification area - M3
 *Nbr               Date       User id     Description
 *WKF009            20230609   XWZHAO      Supplier invoice approval run
 *WKF009            20240305   KVERCO      Approve invoices with status 33333 if all lines are on 33334 (workaround for M3 issue)
 *  
 */
 
/**
* EXT110 - Check for any Supplier Invoices with all lines matched and approve for payment
*/

public class EXT110 extends ExtendM3Batch {
  private final LoggerAPI logger;
  private final DatabaseAPI database;
  private final BatchAPI batch;
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  private int XXCONO;
  private int currentDate;
  private int currentTime;
  private String accountStatus;
  
  boolean allStatusesOk;
  
  private List lstToBeApproved;

  
  public EXT110(LoggerAPI logger, DatabaseAPI database, BatchAPI batch, MICallerAPI miCaller, ProgramAPI program, IonAPI ion) {
    this.logger = logger;
    this.database = database;
    this.batch = batch;
  	this.miCaller = miCaller;
  	this.program = program;
  	this.ion = ion;
  }
  
  public void main() {
    XXCONO= program.LDAZD.CONO;
    accountStatus = "";
    
    if (!batch.getReferenceId().isPresent()) {
      logger.debug("Job data for job ${batch.getJobId()} is missing");
      return;
    }
    
    // Get parameters from EXTJOB
    logger.debug("ReferenceId=" + batch.getReferenceId());
    Optional<String> data = getJobData(batch.getReferenceId().get());
    
    if (!data.isPresent()) {
      logger.debug("Job reference Id ${batch.getReferenceId().get()} is passed, but data was not found");
      return;
    }
    
    currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    
    lstToBeApproved = new ArrayList();
    
    ExpressionFactory expression = database.getExpressionFactory("FPLEDG");
    //expression = expression.eq("EPAPRV", "0");
    
    DBAction queryFPLEDG = database.table("FPLEDG").index("12").selection("EPDIVI", "EPSUNO", "EPSINO", "EPINYR", "EPAPRV").build();
    DBContainer FPLEDG = queryFPLEDG.getContainer();
    FPLEDG.set("EPCONO", XXCONO);
   
    queryFPLEDG.readAll(FPLEDG, 1, 9999, lstFPLEDG);
    
    logger.debug("lstToBeApproved.size=" + lstToBeApproved.size());
    for (int i=0;i<lstToBeApproved.size();i++) {
      Map<String, String> record = (Map<String, String>) lstToBeApproved[i];
		  String divi = record.DIVI.trim();
		  String suno = record.SUNO.trim();
		  String sino = record.SINO.trim();
		  String inyr = record.INYR.trim();
		  logger.debug("call APS110MI SINO=" + sino); 
		  
		  def  params = [ "DIVI": divi, "SUNO": suno, "SINO": sino, "INYR": inyr]; 
      def callback = {
        Map<String, String> response ->
      }
      miCaller.call("APS110MI","ApproveInvoice", params, callback);
    }
  }
  /*
	 * getJobData 
	 *
	*/
  private Optional<String> getJobData(String referenceId) {
    def queryEXTJOB = database.table("EXTJOB").index("00").selection("EXRFID", "EXJOID", "EXDATA").build();
    def EXTJOB = queryEXTJOB.createContainer();
    EXTJOB.set("EXCONO", XXCONO);
    EXTJOB.set("EXRFID", referenceId);
    if (queryEXTJOB.read(EXTJOB)) {
      return Optional.of(EXTJOB.getString("EXDATA"));
    }
    return Optional.empty();
  }
   
  /**
   * lstFPLEDG - Callback function to return FPLEDG records
   *
  */
  Closure<?> lstFPLEDG = { DBContainer FPLEDG ->
    String divi = FPLEDG.get("EPDIVI").toString().trim();
    String suno = FPLEDG.get("EPSUNO").toString().trim();
    String sino = FPLEDG.get("EPSINO").toString().trim();
    String inyr = FPLEDG.get("EPINYR").toString().trim();
    String aprv = FPLEDG.get("EPAPRV").toString().trim();
    
    allStatusesOk = true;
    DBAction queryFGINHE = database.table("FGINHE").index("00").selection("F4INS0").build();
    DBContainer FGINHE = queryFGINHE.getContainer();
    FGINHE.set("F4CONO", XXCONO);
    FGINHE.set("F4DIVI", divi);
    FGINHE.set("F4SUNO", suno);
    FGINHE.set("F4SINO", sino);
    FGINHE.set("F4INYR", inyr.toInteger());
    
    if (queryFGINHE.read(FGINHE)) {
      String ins0 = FGINHE.get("F4INS0").toString().trim();
      if (ins0 == "33334") {
        def map = [DIVI: divi, SUNO: suno, SINO: sino, INYR: inyr];
        lstToBeApproved.add(map);
      }
      // Workaround for M3 error where all lines are set to 33334 but header is stuck on 33333
      if (ins0 == "33333") {
        DBAction queryFGINLI = database.table("FGINLI").index("10").selection("F5INS5", "F5INS2","F5INS3","F5INS4","F5INS5").build();
        DBContainer FGINLI = queryFGINLI.getContainer();
        FGINLI.set("F5CONO", XXCONO);
        FGINLI.set("F5DIVI", divi);
        FGINLI.set("F5SUNO", suno);
        FGINLI.set("F5SINO", sino);
        FGINLI.set("F5INYR", inyr.toInteger());
        FGINLI.set("F5INS5", "3");
        queryFGINLI.readAll(FGINLI, 6, 1, lstFGINLI);
         //If no lines are found waiting for account correction
        if (accountStatus.isBlank()) {
          def map = [DIVI: divi, SUNO: suno, SINO: sino, INYR: inyr];
          lstToBeApproved.add(map);
        }
      }
    }
    
  }
  
  /*
   * lstFGINLI - Callback function to return FGINLI records
   *
  */
  Closure<?> lstFGINLI = { DBContainer FGINLI ->
      accountStatus = FGINLI.get("F5INS5").toString().trim();
  }
  
  
}
