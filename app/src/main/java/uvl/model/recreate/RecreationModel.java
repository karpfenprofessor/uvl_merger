package uvl.model.recreate;

import car.model.base.BaseCarModel;
import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.MergedCarModel;
import car.model.impl.NorthAmericaCarModel;
import uvl.model.recreate.constraints.AbstractConstraint;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class RecreationModel {
    protected final Logger logger;
    private List<AbstractConstraint> constraints;
    
    private Region region;


    public RecreationModel(Region region) {
        this.constraints = new ArrayList<>();
        this.region = region;
        logger = LogManager.getLogger(this.getClass());
    }

    public RecreationModel(Region region, int seed) {
        this.constraints = new ArrayList<>();
        this.region = region;
        logger = LogManager.getLogger(this.getClass());
    }

    public void printConstraints() {
        logger.debug("[print] start printing constraints of recreation model: " + region.printRegion());
        int cnt = 0;
        for (AbstractConstraint c : constraints) {
            logger.debug("  [" + cnt + "]: " + c.toString());
            cnt++;
        }
        logger.debug("[print] finished printing constraints of recreation model: " + region.printRegion());
    }

    public void contextualizeAllConstraints() {
        logger.debug("[contextualize] " + constraints.size() + " constraints in region " + getRegion().printRegion());
        for (AbstractConstraint constraint : constraints) {
            constraint.setContextualize(region.ordinal());
        }
    }

    public void addConstraint(AbstractConstraint c) {
        constraints.add(c);
    }

    public void addConstraints(List<AbstractConstraint> c) {
        constraints.addAll(c);
    }

    public void addNegation(AbstractConstraint c) {
        c.setNegation(Boolean.TRUE);
        constraints.add(c);
    }

    public float analyseContextualizationShare() {
        long contextualizedSize = 0;
        long constraintsSize = 0;

        constraintsSize = constraints.size();
        contextualizedSize = constraints.stream().filter(c -> c.isContextualized()).count();

        float ratio = (float) contextualizedSize / constraintsSize;

        logger.debug("[analyse] " + region.printRegion() + " has " + constraintsSize + " constraints, "
                + contextualizedSize + " are contextualized constraints, ratio: " + ratio);

        return ratio;
    }

    public void solveAndPrintNumberOfSolutions() {
        createChocoModel(this).solveAndPrintNumberOfSolutions();
    }

    public int solveAndReturnNumberOfSolutions() {
        return createChocoModel(this).solveAndReturnNumberOfSolutions();
    }

    public long solveAndReturnAverageSolutionTime(int runs) {
        return createChocoModel(this).solveXNumberOfTimes(100);
    }

    private BaseCarModel createChocoModel(RecreationModel model) {
        BaseCarModel chocoModel = null;
        if (region == Region.EUROPE)
            chocoModel = new EuropeCarModel();

        if (region == Region.NORTH_AMERICA)
            chocoModel = new NorthAmericaCarModel();

        if (region == Region.MERGED || region == Region.TESTING || region == Region.UNION || chocoModel == null)
            chocoModel = new MergedCarModel();

        //chocoModel.recreateFromRegionModel(model);
        return chocoModel;
    }

    public List<AbstractConstraint> getConstraints() {
        return constraints;
    }

    public Region getRegion() {
        return region;
    }
}
