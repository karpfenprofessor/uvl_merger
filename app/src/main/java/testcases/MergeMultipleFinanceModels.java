package testcases;

import model.choco.Region;
import model.recreate.RecreationModel;
import util.UVLParser;
import util.Validator;
import util.Merger.MergeResult;
import util.Merger;
import util.analyse.Analyser;

public class MergeMultipleFinanceModels {

    public static void main(String[] args) {
        try {
            RecreationModel modelFinance2 = UVLParser.parseUVLFile("uvl/finance/finance_2.uvl", Region.A);
            RecreationModel modelFinance3 = UVLParser.parseUVLFile("uvl/finance/finance_3.uvl", Region.B);
            RecreationModel modelFinance4 = UVLParser.parseUVLFile("uvl/finance/finance_4.uvl", Region.C);
            RecreationModel modelFinance5 = UVLParser.parseUVLFile("uvl/finance/finance_5.uvl", Region.D);
            RecreationModel modelFinance6 = UVLParser.parseUVLFile("uvl/finance/finance_6.uvl", Region.E);
            RecreationModel modelFinance7 = UVLParser.parseUVLFile("uvl/finance/finance_7.uvl", Region.F);
            RecreationModel modelFinance8 = UVLParser.parseUVLFile("uvl/finance/finance_8.uvl", Region.G);
            RecreationModel modelFinance9 = UVLParser.parseUVLFile("uvl/finance/finance_9.uvl", Region.H);
            RecreationModel modelFinance10 = UVLParser.parseUVLFile("uvl/finance/finance_10.uvl", Region.I);

            MergeResult mergedModelOneStep = Merger.fullMerge(modelFinance9, modelFinance10);
            mergedModelOneStep.mergedStatistics().printStatistics();

            Validator.validateMerge(mergedModelOneStep.mergedModel(), modelFinance9, modelFinance10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}