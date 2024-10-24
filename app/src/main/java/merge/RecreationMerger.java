package merge;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import car.model.base.Region;
import car.model.impl.MergedCarModel;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;

public class RecreationMerger {
    
    protected final static Logger logger = LogManager.getLogger(RecreationMerger.class);

    public static RecreationModel fullMerge(RecreationModel model1, RecreationModel model2, boolean monitorTime) {
        model1.contextualizeAllConstraints();
        model2.contextualizeAllConstraints();
        RecreationModel unionModel = merge(model1, model2);
        RecreationModel returnModel = null;
        

        returnModel = inconsistencyCheck(unionModel, monitorTime);
        cleanup(returnModel, monitorTime);

        returnModel.benchmark.numberOfConstraintsUnion = model1.getConstraints().size() + model2.getConstraints().size();
        returnModel.benchmark.averageSolutionTimeUnion = unionModel.solveAndReturnAverageSolutionTime(100);
        returnModel.benchmark.contextualizationShare = returnModel.analyseContextualizationShare();
        returnModel.benchmark.averageSolutionTimeMerged = returnModel.solveAndReturnAverageSolutionTime(100);
        returnModel.benchmark.numberOfConstraintsMerged = returnModel.getConstraints().size();

        logger.debug("[merge] finished merge with " + returnModel.getConstraints().size() + " constraints\n");
        return returnModel;
    }

    public static RecreationModel merge(RecreationModel naBaseRecreationModel, RecreationModel euBaseRecreationModel) {
        RecreationModel mergedModel = new RecreationModel(Region.UNION);

        mergedModel.addConstraints(naBaseRecreationModel.getConstraints());
        mergedModel.addConstraints(euBaseRecreationModel.getConstraints());

        logger.debug("[merge] " + naBaseRecreationModel.getConstraints().size() + " constraints from " + naBaseRecreationModel.getRegion().printRegion() + " and " + euBaseRecreationModel.getConstraints().size() + " constraints from " + euBaseRecreationModel.getRegion());

        return mergedModel;
    }

    public static RecreationModel inconsistencyCheck(RecreationModel mergedUnionModel) {
        return inconsistencyCheck(mergedUnionModel, Boolean.FALSE);
    }

    public static RecreationModel inconsistencyCheck(RecreationModel mergedUnionModel, boolean monitorTime) {
        logger.debug("[inconsistency] check with " + mergedUnionModel.getConstraints().size() + " constraints");
        RecreationModel mergedModel = new RecreationModel(Region.MERGED);
        RecreationModel testingModel = null;
        int cnt = 0;

        Iterator<AbstractConstraint> iterator = mergedUnionModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            AbstractConstraint checkConstraint = constraint.copy();
            AbstractConstraint originalConstraint = constraint.copy();
            testingModel = new RecreationModel(Region.TESTING);
            testingModel.addConstraints(mergedUnionModel.getConstraints());
            testingModel.addConstraints(mergedModel.getConstraints());
            mergedModel.benchmark.numberOfChecks++;

            if(isInconsistent(checkConstraint, testingModel, monitorTime, mergedModel)) {
                originalConstraint.disableContextualize();
                mergedModel.addConstraint(originalConstraint);
                logger.info("  [incon_yes] constraint decontextualized: " + originalConstraint.toString());
                cnt++;
            } else {
                mergedModel.addConstraint(originalConstraint);
                //logger.info("  [incon_no] constraint is fine: " + originalConstraint.toString());
            }

            iterator.remove();
        }

        logger.debug("[inconsistency] finished, decontextualized " + cnt + " constraints");
        return mergedModel;
    }

    public static RecreationModel cleanup(RecreationModel mergedModel) {
        return cleanup(mergedModel, Boolean.FALSE);
    }

    public static RecreationModel cleanup(RecreationModel mergedModel, boolean monitorTime) {
        logger.debug("[cleanup] with " + mergedModel.getConstraints().size() + " constraints");
        int cnt = 0;
        Iterator<AbstractConstraint> iterator = mergedModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            constraint.setNegation(Boolean.TRUE);
            mergedModel.benchmark.numberOfChecks++;

            if(isInconsistent(mergedModel, monitorTime)) {
                iterator.remove();
                logger.info("  [incon_yes] constraint removed: " + constraint.toString());
                cnt++;
            } else {
                constraint.setNegation(Boolean.FALSE);
            }
        }

        logger.debug("[cleanup] finished, removed " + cnt + " constraints");
        return mergedModel;
    }

    

    private static boolean isInconsistent(AbstractConstraint constraint, RecreationModel testingRecreationModel, boolean monitorTime, RecreationModel mergedModel) {
        MergedCarModel testingModel = new MergedCarModel();

        constraint.disableContextualize();
        constraint.setNegation(Boolean.TRUE);
        testingRecreationModel.addConstraint(constraint);

        testingModel.recreateFromRegionModel(testingRecreationModel);

        if (CarChecker.checkConsistency(testingModel, mergedModel)) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean isInconsistent(RecreationModel testingRecreationModel, boolean monitorTime) {
        MergedCarModel testingModel = new MergedCarModel();
        testingModel.recreateFromRegionModel(testingRecreationModel);

        if (CarChecker.checkConsistency(testingModel, testingRecreationModel)) {
            return false;
        } else {
            return true;
        }
    }




        
    
}
