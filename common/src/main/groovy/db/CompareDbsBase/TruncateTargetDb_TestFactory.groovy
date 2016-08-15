package db.CompareDbsBase

import excel.ExcelObjectProvider
import org.testng.Reporter
import org.testng.annotations.Factory
import org.testng.annotations.Parameters

class TruncateTargetDb_TestFactory extends CompareS2T_TestFactoryBase {


    @Parameters(["inputFile", "schemaColumn", "atgardColumn"])
    @Factory
    public Object[] createTruncateInstances(String inputFile, String schemaColumn, String atgardColumn) {

        def targetDb = schemaColumn.toLowerCase() + "_Target"
        def system = schemaColumn[0].toUpperCase() + schemaColumn[1..-1].toLowerCase()
        def result = [];

        ExcelObjectProvider excelObjectProvider = new ExcelObjectProvider(inputFile)
        excelObjectProvider.addColumnsToRetriveFromFile(["Tabell"])
        excelObjectProvider.addColumnsCapabiliteisToRetrive("System", system)
//        excelObjectProvider.addColumnsCapabiliteisToRetrive("Tabell", "AMSULOG")
//        excelObjectProvider.addColumnsCapabiliteisToRetrive("Atgard", atgardColumn)
        def excelBodyRows = excelObjectProvider.getGdcObjects(2,1)
        excelObjectProvider.printRow(excelBodyRows, ["System", "Tabell", "Atgard"])

        Reporter.log("Number of lines read <$excelBodyRows.size>")
        excelBodyRows.unique().eachWithIndex { excelRow, index ->
            def table = excelRow["Tabell"]
            result.add(new TruncateTargetTable_Test(targetDb, excelRow["System"], table, atgardColumn))


        }
        return result;
    }

}