package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.Analyser;
import uvl.util.RecreationMerger;
import uvl.util.UVLParser;

public class TestAnything {

    protected final static Logger logger = LogManager.getLogger(TestAnything.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband7_realized.uvl";	 
        String filePathString2 = "uvl/smartwatch/miband8_realized.uvl";	   
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModel recModel2 = UVLParser.parseUVLFile(filePathString2, Region.B);

        System.out.println("solutions model A: " + Analyser.returnNumberOfSolutions(recModel));
        System.out.println("solutions model B: " + Analyser.returnNumberOfSolutions(recModel2));

        recModel.contextualizeAllConstraints();
        recModel2.contextualizeAllConstraints();

        System.out.println("solutions model A contextualized: " + Analyser.returnNumberOfSolutions(recModel));
        System.out.println("solutions model B contextualized: " + Analyser.returnNumberOfSolutions(recModel2));

        RecreationModel unionModel = RecreationMerger.union(recModel, recModel2);
        
        System.out.println("solutions union model: " + Analyser.returnNumberOfSolutions(unionModel));
        Analyser.printAllSolutions(unionModel);

        RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(unionModel);

        System.out.println("solutions merged model: " + Analyser.returnNumberOfSolutions(mergedModel));

        mergedModel = RecreationMerger.cleanup(mergedModel);

        System.out.println("solutions finished model: " + Analyser.returnNumberOfSolutions(mergedModel));
        Analyser.printAllSolutions(mergedModel);
    }
} 