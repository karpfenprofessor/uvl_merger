package car.model.recreate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import car.model.base.Region;

public class RecreationMerger {
    
    protected final static Logger logger = LogManager.getLogger(RecreationMerger.class);

    public static RecreationModel merge(RecreationModel naBaseRecreationModel, RecreationModel euBaseRecreationModel) {
        RecreationModel mergedModel = new RecreationModel(Region.MERGED);

        mergedModel.addConstraints(naBaseRecreationModel.getConstraints());
        mergedModel.addConstraints(euBaseRecreationModel.getConstraints());

        return mergedModel;
    }




        
    
}
