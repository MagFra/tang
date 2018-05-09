package db.CompareDbsBase

import base.AnySqlCompareTest
import org.apache.log4j.Logger
import org.testng.ITestContext
import org.testng.annotations.Test

import static dtos.base.Constants.dbRunTypeFirstRow

public class VerifyMaskedTargetColumn_Test extends AnySqlCompareTest{
    private final static Logger log = Logger.getLogger("VMT   ")
    private static int row = 0
    private String targetDb;
    private String sourceDb;
    private String system;
    private String sourceTargetSql;
    private String targetDbOwner;
    def table
    def column
    def actionColumn
    def searchCriteria
    def searchExtraCondition
    def numberOfLinesInSqlCompare = 101
    public VerifyMaskedTargetColumn_Test(ITestContext testContext, targetDb, sourceDb, system, table, column, actionColumn, searchCriteria = "", searchExtraCondition = "") {
        super.setup()
        this.targetDb = targetDb
        this.sourceDb = sourceDb
        this.system = system.toLowerCase()
        this.table = table.toLowerCase()
        this.column = column.toLowerCase()
        this.actionColumn = actionColumn
        this.searchCriteria = searchCriteria
        this.searchExtraCondition = searchExtraCondition
        targetDbOwner = settings."$targetDb".owner
        super.setSourceSqlHelper(testContext, sourceDb)
        super.setTargetSqlHelper(testContext, targetDb)
        if(settings["numberOfLinesInSqlCompare"] != "" && settings["numberOfLinesInSqlCompare"].size() != 0 ){
            numberOfLinesInSqlCompare = settings["numberOfLinesInSqlCompare"]
        }

        log.info("sourceTargetSql <$sourceTargetSql>")
    }

    @Test
    public void verifyMaskedTargetTest(ITestContext testContext){
        def tmpColumn = column
        reporterLogLn(reporterHelper.addIcons(getDbType(), getDbType(sourceDb), getDbType(targetDb)))
        row++
        reporterLogLn("Row: <$row> Verify <$actionColumn> TABLE/COLUMN ");
        reporterLogLn("Source Db: <$sourceDb> ");
        reporterLogLn("Target Db: <$targetDb> ");
        reporterLogLn("Tmp Table: <$table> ");
        //reporterLogLn("column: <$column> ");


        def checkColumnType = "SELECT data_type FROM USER_TAB_COLS WHERE lower(table_name) = '$table' AND lower(column_name) = '$column'"
        reporterLogLn("Sql to check column type:\n$checkColumnType\n")
        def checkColumnTypeResult = getDbResult(targetDbSqlDriver, checkColumnType, dbRunTypeFirstRow)



        def TARGET_TABLE_QUERY_SQLSERVER = "SELECT DISTINCT Table_name FROM Information_schema.columns WHERE table_name = '%s'" //Todo: change this  sqlserver sql and check in
        if(checkColumnTypeResult[0] == "CLOB" ){
            reporterLogLn("Clob: <$column> ");
            tmpColumn = "to_char( $column)"
            reporterLogLn("checkColumnType:\n$checkColumnType\n")
            reporterLogLn("Column <$table> <$column> is xLOB type<$checkColumnTypeResult> ==> <$tmpColumn>")
        }
        reporterLogLn("tmpColumn: <$tmpColumn> ");

        def TARGET_TABLE_QUERY_ORACLE = "SELECT $tmpColumn FROM $table\n" +
                " WHERE NOT $tmpColumn IS NULL\n" +
                " AND ROWNUM < 21\n"

        if (searchCriteria != "") {
            def numberOfLinesInSqlCompareTemp = numberOfLinesInSqlCompare
            if(numberOfLinesInSqlCompare.class.equals(String)){
                numberOfLinesInSqlCompareTemp  = Integer.parseInt(numberOfLinesInSqlCompare) + 1000
            }else {
                numberOfLinesInSqlCompareTemp = numberOfLinesInSqlCompare + 1000
            }

            sourceTargetSql = "-- Verify search criteria and masked column<$searchCriteria, $tmpColumn> in table <$table> in target<$targetDb> against source<$sourceDb>\n"
                sourceTargetSql += "SELECT $searchCriteria, $tmpColumn FROM $table\n" +
                        " WHERE NOT $tmpColumn IS NULL\n" +
                        " AND REPLACE($tmpColumn, ' ' , '') != ''\n" +
                        " AND $searchCriteria BETWEEN (SELECT MAX($searchCriteria)- $numberOfLinesInSqlCompare FROM $table where NOT $tmpColumn IS NULL) AND (SELECT MAX($searchCriteria) FROM $table where NOT $tmpColumn IS NULL)\n" +
                    " AND ROWNUM < $numberOfLinesInSqlCompareTemp\n"
        }else{
            sourceTargetSql = "-- Verify masked column<$tmpColumn> in table <$table> in target<$targetDb> against source<$sourceDb>\n"
            sourceTargetSql += "SELECT $tmpColumn FROM $table\n" +
                    " WHERE NOT $tmpColumn IS NULL\n" +
                    " AND ROWNUM < $numberOfLinesInSqlCompare\n"
        }
        if( searchExtraCondition != ""){
            sourceTargetSql += "\nAND $searchExtraCondition\n"
        }
        sourceTargetSql += "ORDER BY 1\n"

        if(getDbType(targetDb).equals("sqlserver")){//Todo: fix this code for sqlserver
//            sourceTargetSql = "-- Verify masked column<$column> in table <$table> in system <$system> \n"
//            sourceTargetSql = String.format(TARGET_TABLE_QUERY_SQLSERVER, table)
        }
        log.info("sourceTargetSql:\n$sourceTargetSql\n")
        reporterLogLn("TargetSql:\n$sourceTargetSql\n")
        reporterLogLn("#########")

        def sourceDbResult = getSourceDbRowsResult(sourceTargetSql)
        def targetDbResult = getTargetDbRowsResult(sourceTargetSql)

        boolean sameData = false
        if(sourceDbResult != null && targetDbResult != null ) {
            if(targetDbResult.size().equals(0)){
                reporterLogLn("Target size is zero")

                sameData = false
            }else {
                if (checkColumnTypeResult[0] == "CLOB") {
                    sameData = (targetDbResult.collect {}.toString() == sourceDbResult.collect {}.toString())
                } else {
                   sameData = (targetDbResult == sourceDbResult)
                }
            }
            int index = 0
            if (sameData){
                def maxRows= settings.maxDiffsToShow
                def count = targetDbResult.size()
                if (count > maxRows ){
                    targetDbResult = targetDbResult[0..maxRows-1]
                    reporterLogLn("Showing max rows: <$maxRows>")
                }else{
                    reporterLogLn("Showing max rows: <$count>")
                }

                targetDbResult.each {
                    reporterLogLn(sourceDbResult[index].toString() + " == " + targetDbResult[index].toString())
                    index++
                }
            }
        }
        tangAssert.assertTrue(!sameData, "Table/Column <$table/$column> should be masked", "Table/Column seems to be unmasked ");

    }

}
