package db.CompareDbsBase

import base.AnySqlCompareTest
import org.testng.ITestContext
import org.testng.annotations.Test
/*
 this class uses split of incoming query by ";" and will execute each sub query
 */

public class NullifyTargetTable extends AnySqlCompareTest{
    private static int row = 0
    private String targetDb;
    private String action;
    private String table;
    private String column;
    private String searchExtraCondition;
    private boolean execute;

    public NullifyTargetTable(targetDb, system, table, action, column, searchExtraCondition, boolean execute = false) {
        super.setup()
        this.targetDb = targetDb
        this.action = action
        this.table = table
        this.column = column
        this.searchExtraCondition = searchExtraCondition
        this.execute = execute

        String dbTargetOwner = settings."$targetDb".owner
    }

    @Test
    public void updateTargetTest(ITestContext testContext){
        super.setTargetSqlHelper(testContext, targetDb)
        reporterLogLn(reporterHelper.addIcons(getDbType(), getDbType(targetDb)))

        row++
        reporterLogLn("Row: <$row> Nullify Column");
        reporterLogLn("Target Db: <$targetDb> ");
        reporterLogLn("Action:    <$action> ");
        reporterLogLn("Column:\n$column\n");
        reporterLogLn("###");
        reporterLogLn("execute:    <$execute>");

        def targetSql = "UPDATE $table SET $column = null " +
                "where $column IS NOT NULL"
        if(!searchExtraCondition.isEmpty() && searchExtraCondition != "-"){
            targetSql += "\nAND $searchExtraCondition"
        }
        targetSql += "-- Execute is <$execute>"
        reporterLogLn("$targetSql")
        if(execute) {
            execute(targetDbSqlDriver, targetSql)
        }
    }

}
