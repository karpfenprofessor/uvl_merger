package car.model.recreate;

import car.merge.CarChecker;
import car.model.base.BaseCarModel;
import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.MergedCarModel;
import car.model.impl.NorthAmericaCarModel;
import car.model.recreate.constraints.AbstractConstraint;
import car.model.recreate.constraints.ImplicationConstraint;
import car.model.recreate.constraints.SimpleConstraint;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class RecreationModel {
    private List<AbstractConstraint> constraints;
    private Region region;

    protected final Logger logger;
    private Random random;

    public RecreationModel(Region region) {
        this.constraints = new ArrayList<>();
        this.region = region;
        logger = LogManager.getLogger(this.getClass());
        random = new Random(1234);
    }

    public RecreationModel(Region region, Integer seed) {
        this.constraints = new ArrayList<>();
        this.region = region;
        logger = LogManager.getLogger(this.getClass());
        random = new Random(seed);
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

    public List<AbstractConstraint> getConstraints() {
        return constraints;
    }

    public Region getRegion() {
        return region;
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

    public void analyseModel() {
        long contextualizedSize = 0;
        long constraintsSize = 0;

        constraintsSize = constraints.size();
        contextualizedSize = constraints.stream().filter(c -> c.isContextualized()).count();

        logger.debug("[analyse] model " + region.printRegion() + " has " + constraintsSize + " constraints, "
                + contextualizedSize + " are contextualized constraints");
    }

    public void contextualizeAllConstraints() {
        logger.debug("[contextualize] region " + getRegion().printRegion());
        for (AbstractConstraint constraint : constraints) {
            constraint.setContextualize(region.ordinal());
        }
    }

    public void createRandomConstraints(int numberOfConstraints) {
        createRandomConstraints(numberOfConstraints, Boolean.TRUE, Boolean.TRUE);
    }

    public void createRandomConstraints(int numberOfConstraints, boolean onlyImplication, boolean restricted) {
        int numberOfSolutions = solveWithNumberOfSolutions();
        int oldNumberOfSolutions = 0;

        for (int i = 0; i <= numberOfConstraints; i++) {
            boolean isImplicationConstraint = onlyImplication ? onlyImplication : random.nextDouble() < 0.66;
            AbstractConstraint constraint = null;
            if (isImplicationConstraint) {
                SimpleConstraint antecedent = createRandomSimpleConstraint(null);
                SimpleConstraint consequent = createRandomSimpleConstraint(antecedent.getVariable());
                constraint = new ImplicationConstraint(antecedent, consequent);
            } else {
                constraint = createRandomSimpleConstraint(null);
            }

            if (constraints.contains(constraint)) {
                i--;
                logger.info("[rand] constraint already in model " + region.printRegion());
                continue;
            }

            constraints.add(constraint);

            oldNumberOfSolutions = numberOfSolutions;
            numberOfSolutions = solveWithNumberOfSolutions();
            float coefficient = (float) numberOfSolutions / oldNumberOfSolutions;
            if (restricted && coefficient <= 0.80) {
                constraints.remove(constraint);
                i--;
                numberOfSolutions = oldNumberOfSolutions;
            }

            i = (i < 0) ? i = 0:i;
        }

        logger.debug("[random] created " + constraints.size() + " constraints in " + region.printRegion());
    }

    private SimpleConstraint createRandomSimpleConstraint(String alreadyInUse) {
        List<String> variablesList = new ArrayList<>(
                Arrays.asList("type", "color", "engine", "couplingdev", "fuel", "service"));
        String[] operators = { "=", "!=", ">", "<" };

        if (alreadyInUse != null) {
            variablesList.remove(alreadyInUse);
        }

        String variable = variablesList.get(random.nextInt(variablesList.size()));
        String operator = operators[random.nextInt(operators.length)];
        Integer value = getRandomValueForVariable(variable, operator);

        return new SimpleConstraint(variable, operator, value);
    }

    private Integer getRandomValueForVariable(String variable, String operator) {
        if(operator.contains(">")) {
            switch (variable) {
                case "type":
                    return random.nextInt(3); // 0-3
                case "color":
                    return random.nextInt(1); // 0-1
                case "engine":
                    return random.nextInt(2); // 0-2
                case "couplingdev":
                    return random.nextInt(1); // 0-1
                case "fuel":
                    return random.nextInt(3); // 0-3
                case "service":
                    return random.nextInt(2); // 0-2
                default:
                    throw new IllegalArgumentException("Unknown variable: " + variable);
            }
        } else if (operator.contains("<")) {
            switch (variable) {
                case "type":
                    return random.nextInt(1,4); // 0-3
                case "color":
                    return random.nextInt(1, 2); // 0-1
                case "engine":
                    return random.nextInt(1, 3); // 0-2
                case "couplingdev":
                    return random.nextInt(1, 2); // 0-1
                case "fuel":
                    return random.nextInt(1, 4); // 0-3
                case "service":
                    return random.nextInt(1, 3); // 0-2
                default:
                    throw new IllegalArgumentException("Unknown variable: " + variable);
            }
        } else {
            switch (variable) {
                case "type":
                    return random.nextInt(4); // 0-3
                case "color":
                    return random.nextInt(2); // 0-1
                case "engine":
                    return random.nextInt(3); // 0-2
                case "couplingdev":
                    return random.nextInt(2); // 0-1
                case "fuel":
                    return random.nextInt(4); // 0-3
                case "service":
                    return random.nextInt(3); // 0-2
                default:
                    throw new IllegalArgumentException("Unknown variable: " + variable);
            }
        }
    }

    public void createLogicalNorthAmericaConstraints() {
        SimpleConstraint c1us = new SimpleConstraint("fuel", "!=", 3);

        SimpleConstraint c2us_1 = new SimpleConstraint("fuel", "=", 0);
        SimpleConstraint c2us_2 = new SimpleConstraint("couplingdev", "=", 1);
        ImplicationConstraint c2us = new ImplicationConstraint(c2us_1, c2us_2);

        SimpleConstraint c3us_1 = new SimpleConstraint("fuel", "=", 1);
        SimpleConstraint c3us_2 = new SimpleConstraint("color", "=", 1);
        ImplicationConstraint c3us = new ImplicationConstraint(c3us_1, c3us_2);

        addConstraint(c1us);
        addConstraint(c2us);
        addConstraint(c3us);
    }

    public void createLogicalEuropeConstraints() {
        SimpleConstraint c1eu = new SimpleConstraint("fuel", "!=", 2);

        SimpleConstraint c2eu_1 = new SimpleConstraint("fuel", "=", 0);
        SimpleConstraint c2eu_2 = new SimpleConstraint("couplingdev", "=", 1);
        ImplicationConstraint c2eu = new ImplicationConstraint(c2eu_1, c2eu_2);

        SimpleConstraint c3eu_1 = new SimpleConstraint("fuel", "=", 1);
        SimpleConstraint c3eu_2 = new SimpleConstraint("type", "!=", 2);
        ImplicationConstraint c3eu = new ImplicationConstraint(c3eu_1, c3eu_2);

        addConstraint(c1eu);
        addConstraint(c2eu);
        addConstraint(c3eu);
    }

    public void solveAndPrintNumberOfSolutions() {
        BaseCarModel model = null;
        if (region == Region.EUROPE)
            model = new EuropeCarModel();

        if (region == Region.NORTH_AMERICA)
            model = new NorthAmericaCarModel();

        if (region == Region.MERGED)
            model = new MergedCarModel();

        model.recreateFromRegionModel(this);
        model.solveAndPrintNumberOfSolutions();
    }

    public int solveWithNumberOfSolutions() {
        BaseCarModel model = null;
        if (region == Region.EUROPE)
            model = new EuropeCarModel();

        if (region == Region.NORTH_AMERICA)
            model = new NorthAmericaCarModel();

        if (region == Region.MERGED)
            model = new MergedCarModel();

        model.recreateFromRegionModel(this);
        return model.solveWithNumberOfSolutions();
    }
}
