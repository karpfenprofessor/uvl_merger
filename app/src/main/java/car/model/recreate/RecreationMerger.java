package car.model.recreate;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import car.merge.CarChecker;
import car.model.base.Region;
import car.model.impl.MergedCarModel;
import car.model.recreate.constraints.AbstractConstraint;

public class RecreationMerger {
    
    protected final static Logger logger = LogManager.getLogger(RecreationMerger.class);

    public static RecreationModel merge(RecreationModel naBaseRecreationModel, RecreationModel euBaseRecreationModel) {
        RecreationModel mergedModel = new RecreationModel(Region.MERGED);

        mergedModel.addConstraints(naBaseRecreationModel.getConstraints());
        mergedModel.addConstraints(euBaseRecreationModel.getConstraints());

        return mergedModel;
    }

    public static RecreationModel inconsistencyCheck(RecreationModel mergedUnionModel) {
        RecreationModel mergedModel = new RecreationModel(Region.MERGED);
        RecreationModel testingModel = null;

        Iterator<AbstractConstraint> iterator = mergedUnionModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            AbstractConstraint checkConstraint = constraint.copy();
            AbstractConstraint originalConstraint = constraint.copy();
            testingModel = new RecreationModel(Region.TESTING);
            testingModel.addConstraints(mergedUnionModel.getConstraints());
            testingModel.addConstraints(mergedModel.getConstraints());

            if(isInconsistent(checkConstraint, testingModel)) {
                originalConstraint.disableContextualize();
                mergedModel.addConstraint(originalConstraint);
            } else {
                mergedModel.addConstraint(originalConstraint);
            }

            iterator.remove();
        }

        return mergedModel;
    }

    public static boolean isInconsistent(AbstractConstraint constraint, RecreationModel testingRecreationModel) {
        MergedCarModel testingModel = new MergedCarModel();

        constraint.disableContextualize();
        constraint.setNegation(Boolean.TRUE);
        testingRecreationModel.addConstraint(constraint);

        testingModel.recreateFromRegionModel(testingRecreationModel);

        if (CarChecker.checkConsistency(testingModel)) {
            return false;
        } else {
            return true;
        }
    }




        
    
}
