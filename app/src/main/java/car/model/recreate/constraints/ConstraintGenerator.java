package car.model.recreate.constraints;

import org.chocosolver.solver.search.strategy.selectors.variables.Random;

public class ConstraintGenerator {
    
    public void createRandomConstraints(int numberOfConstraints) {
        createRandomConstraints(numberOfConstraints, Boolean.TRUE, Boolean.TRUE);
    }

    public void createRandomConstraints(int numberOfConstraints, boolean onlyImplication, boolean restricted,
            int seed) {
        random = new Random(seed);
        createRandomConstraints(numberOfConstraints, Boolean.TRUE, Boolean.TRUE);
    }

    public void createRandomConstraints(int numberOfConstraints, boolean onlyImplication, boolean restricted) {
        int numberOfSolutions = solveAndReturnNumberOfSolutions();
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
            numberOfSolutions = solveAndReturnNumberOfSolutions();
            float coefficient = (float) numberOfSolutions / oldNumberOfSolutions;
            if (restricted && coefficient <= 0.7) {
                constraints.remove(constraint);
                i--;
                numberOfSolutions = oldNumberOfSolutions;
            }

            i = (i < 0) ? i = 0 : i;
        }

        logger.debug("[random] created " + constraints.size() + " constraints in " + region.printRegion());
    }

    private SimpleConstraint createRandomSimpleConstraint(String alreadyInUse) {
        List<String> variablesList = new ArrayList<>(
                Arrays.asList("type", "color", "engine", "couplingdev", "fuel", "service"));
        String[] operators = { "=", "!=", ">", "<", "<=", ">=" };

        if (alreadyInUse != null) {
            variablesList.remove(alreadyInUse);
        }

        String variable = variablesList.get(random.nextInt(variablesList.size()));
        String operator = operators[random.nextInt(operators.length)];
        Integer value = getRandomValueForVariable(variable, operator);

        return new SimpleConstraint(variable, operator, value);
    }

    private Integer getRandomValueForVariable(String variable, String operator) {
        if (operator.contains(">")) {
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
                    return random.nextInt(1, 4); // 0-3
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

    public void createLogicalNorthAmericaConstraints(int constraints) {
    }

    public void createLogicalEuropeConstraints(int constraints) {
    }

    public void createSharedConstraints(int number) {
        List<ImplicationConstraint> allConstraints = new ArrayList<>();
        SimpleConstraint c0shared_1 = new SimpleConstraint("type", "=", 0);
        SimpleConstraint c0shared_2 = new SimpleConstraint("engine", "!=", 1);
        ImplicationConstraint c0shared = new ImplicationConstraint(c0shared_1, c0shared_2);

        SimpleConstraint c1shared_1 = new SimpleConstraint("fuel", "=", 3);
        SimpleConstraint c1shared_2 = new SimpleConstraint("type", "<=", 3);
        ImplicationConstraint c1shared = new ImplicationConstraint(c1shared_1, c1shared_2);

        SimpleConstraint c2shared_1 = new SimpleConstraint("couplingdev", "!=", 1);
        SimpleConstraint c2shared_2 = new SimpleConstraint("engine", ">=", 0);
        ImplicationConstraint c2shared = new ImplicationConstraint(c2shared_1, c2shared_2);

        SimpleConstraint c3shared_1 = new SimpleConstraint("couplingdev", "=", 0);
        SimpleConstraint c3shared_2 = new SimpleConstraint("service", ">=", 0);
        ImplicationConstraint c3shared = new ImplicationConstraint(c3shared_1, c3shared_2);

        SimpleConstraint c4shared_1 = new SimpleConstraint("couplingdev", "<=", 1);
        SimpleConstraint c4shared_2 = new SimpleConstraint("color", ">=", 0);
        ImplicationConstraint c4shared = new ImplicationConstraint(c4shared_1, c4shared_2);

        SimpleConstraint c5shared_1 = new SimpleConstraint("fuel", "<", 1);
        SimpleConstraint c5shared_2 = new SimpleConstraint("type", ">", 1);
        ImplicationConstraint c5shared = new ImplicationConstraint(c5shared_1, c5shared_2);

        SimpleConstraint c6shared_1 = new SimpleConstraint("engine", ">", 0);
        SimpleConstraint c6shared_2 = new SimpleConstraint("color", ">=", 0);
        ImplicationConstraint c6shared = new ImplicationConstraint(c6shared_1, c6shared_2);

        SimpleConstraint c7shared_1 = new SimpleConstraint("type", "=", 0);
        SimpleConstraint c7shared_2 = new SimpleConstraint("couplingdev", ">=", 0);
        ImplicationConstraint c7shared = new ImplicationConstraint(c7shared_1, c7shared_2);

        SimpleConstraint c8shared_1 = new SimpleConstraint("type", "=", 2);
        SimpleConstraint c8shared_2 = new SimpleConstraint("service", "<", 2);
        ImplicationConstraint c8shared = new ImplicationConstraint(c8shared_1, c8shared_2);

        SimpleConstraint c9shared_1 = new SimpleConstraint("service", "<", 2);
        SimpleConstraint c9shared_2 = new SimpleConstraint("couplingdev", ">=", 0);
        ImplicationConstraint c9shared = new ImplicationConstraint(c9shared_1, c9shared_2);

        SimpleConstraint c10shared_1 = new SimpleConstraint("type", "=", 2);
        SimpleConstraint c10shared_2 = new SimpleConstraint("service", ">=", 0);
        ImplicationConstraint c10shared = new ImplicationConstraint(c10shared_1, c10shared_2);

        SimpleConstraint c11shared_1 = new SimpleConstraint("fuel", ">", 0);
        SimpleConstraint c11shared_2 = new SimpleConstraint("couplingdev", ">=", 0);
        ImplicationConstraint c11shared = new ImplicationConstraint(c11shared_1, c11shared_2);

        SimpleConstraint c12shared_1 = new SimpleConstraint("engine", "<", 1);
        SimpleConstraint c12shared_2 = new SimpleConstraint("fuel", ">=", 1);
        ImplicationConstraint c12shared = new ImplicationConstraint(c12shared_1, c12shared_2);

        SimpleConstraint c13shared_1 = new SimpleConstraint("engine", "<", 2);
        SimpleConstraint c13shared_2 = new SimpleConstraint("couplingdev", "<=", 1);
        ImplicationConstraint c13shared = new ImplicationConstraint(c13shared_1, c13shared_2);

        SimpleConstraint c14shared_1 = new SimpleConstraint("couplingdev", "=", 0);
        SimpleConstraint c14shared_2 = new SimpleConstraint("color", ">=", 0);
        ImplicationConstraint c14shared = new ImplicationConstraint(c14shared_1, c14shared_2);

        SimpleConstraint c15shared_1 = new SimpleConstraint("couplingdev", "<", 1);
        SimpleConstraint c15shared_2 = new SimpleConstraint("type", ">", 0);
        ImplicationConstraint c15shared = new ImplicationConstraint(c15shared_1, c15shared_2);

        SimpleConstraint c16shared_1 = new SimpleConstraint("type", ">=", 2);
        SimpleConstraint c16shared_2 = new SimpleConstraint("color", ">=", 0);
        ImplicationConstraint c16shared = new ImplicationConstraint(c16shared_1, c16shared_2);

        SimpleConstraint c17shared_1 = new SimpleConstraint("service", ">=", 0);
        SimpleConstraint c17shared_2 = new SimpleConstraint("type", "!=", 0);
        ImplicationConstraint c17shared = new ImplicationConstraint(c17shared_1, c17shared_2);

        SimpleConstraint c18shared_1 = new SimpleConstraint("couplingdev", "<=", 1);
        SimpleConstraint c18shared_2 = new SimpleConstraint("service", ">=", 0);
        ImplicationConstraint c18shared = new ImplicationConstraint(c18shared_1, c18shared_2);

        SimpleConstraint c19shared_1 = new SimpleConstraint("type", "<", 1);
        SimpleConstraint c19shared_2 = new SimpleConstraint("service", "<=", 1);
        ImplicationConstraint c19shared = new ImplicationConstraint(c19shared_1, c19shared_2);

        SimpleConstraint c20shared_1 = new SimpleConstraint("type", "=", 0);
        SimpleConstraint c20shared_2 = new SimpleConstraint("fuel", "!=", 2);
        ImplicationConstraint c20shared = new ImplicationConstraint(c20shared_1, c20shared_2);

        SimpleConstraint c21shared_1 = new SimpleConstraint("type", "<", 1);
        SimpleConstraint c21shared_2 = new SimpleConstraint("service", ">=", 0);
        ImplicationConstraint c21shared = new ImplicationConstraint(c21shared_1, c21shared_2);

        SimpleConstraint c22shared_1 = new SimpleConstraint("type", "<=", 3);
        SimpleConstraint c22shared_2 = new SimpleConstraint("engine", "<=", 2);
        ImplicationConstraint c22shared = new ImplicationConstraint(c22shared_1, c22shared_2);

        SimpleConstraint c23shared_1 = new SimpleConstraint("type", "<", 3);
        SimpleConstraint c23shared_2 = new SimpleConstraint("couplingdev", "<=", 1);
        ImplicationConstraint c23shared = new ImplicationConstraint(c23shared_1, c23shared_2);

        SimpleConstraint c24shared_1 = new SimpleConstraint("engine", ">", 0);
        SimpleConstraint c24shared_2 = new SimpleConstraint("color", "<=", 1);
        ImplicationConstraint c24shared = new ImplicationConstraint(c24shared_1, c24shared_2);

        SimpleConstraint c25shared_1 = new SimpleConstraint("couplingdev", "=", 0);
        SimpleConstraint c25shared_2 = new SimpleConstraint("engine", "<=", 2);
        ImplicationConstraint c25shared = new ImplicationConstraint(c25shared_1, c25shared_2);

        SimpleConstraint c26shared_1 = new SimpleConstraint("service", ">=", 1);
        SimpleConstraint c26shared_2 = new SimpleConstraint("fuel", ">=", 1);
        ImplicationConstraint c26shared = new ImplicationConstraint(c26shared_1, c26shared_2);

        SimpleConstraint c27shared_1 = new SimpleConstraint("fuel", ">=", 2);
        SimpleConstraint c27shared_2 = new SimpleConstraint("couplingdev", ">=", 0);
        ImplicationConstraint c27shared = new ImplicationConstraint(c27shared_1, c27shared_2);

        SimpleConstraint c28shared_1 = new SimpleConstraint("service", ">=", 0);
        SimpleConstraint c28shared_2 = new SimpleConstraint("color", "<=", 1);
        ImplicationConstraint c28shared = new ImplicationConstraint(c28shared_1, c28shared_2);

        SimpleConstraint c29shared_1 = new SimpleConstraint("engine", "<", 2);
        SimpleConstraint c29shared_2 = new SimpleConstraint("type", ">", 0);
        ImplicationConstraint c29shared = new ImplicationConstraint(c29shared_1, c29shared_2);

        SimpleConstraint c30shared_1 = new SimpleConstraint("service", ">", 0);
        SimpleConstraint c30shared_2 = new SimpleConstraint("engine", ">=", 0);
        ImplicationConstraint c30shared = new ImplicationConstraint(c30shared_1, c30shared_2);

        SimpleConstraint c31shared_1 = new SimpleConstraint("couplingdev", "!=", 1);
        SimpleConstraint c31shared_2 = new SimpleConstraint("color", ">=", 0);
        ImplicationConstraint c31shared = new ImplicationConstraint(c31shared_1, c31shared_2);

        SimpleConstraint c32shared_1 = new SimpleConstraint("engine", ">=", 1);
        SimpleConstraint c32shared_2 = new SimpleConstraint("type", ">", 0);
        ImplicationConstraint c32shared = new ImplicationConstraint(c32shared_1, c32shared_2);

        SimpleConstraint c33shared_1 = new SimpleConstraint("service", ">", 1);
        SimpleConstraint c33shared_2 = new SimpleConstraint("type", ">", 1);
        ImplicationConstraint c33shared = new ImplicationConstraint(c33shared_1, c33shared_2);

        SimpleConstraint c34shared_1 = new SimpleConstraint("service", "<", 2);
        SimpleConstraint c34shared_2 = new SimpleConstraint("fuel", ">", 0);
        ImplicationConstraint c34shared = new ImplicationConstraint(c34shared_1, c34shared_2);

        SimpleConstraint c35shared_1 = new SimpleConstraint("couplingdev", "!=", 0);
        SimpleConstraint c35shared_2 = new SimpleConstraint("color", ">=", 0);
        ImplicationConstraint c35shared = new ImplicationConstraint(c35shared_1, c35shared_2);

        SimpleConstraint c36shared_1 = new SimpleConstraint("color", "!=", 0);
        SimpleConstraint c36shared_2 = new SimpleConstraint("engine", "<=", 2);
        ImplicationConstraint c36shared = new ImplicationConstraint(c36shared_1, c36shared_2);

        SimpleConstraint c37shared_1 = new SimpleConstraint("engine", ">", 1);
        SimpleConstraint c37shared_2 = new SimpleConstraint("color", "<=", 1);
        ImplicationConstraint c37shared = new ImplicationConstraint(c37shared_1, c37shared_2);

        SimpleConstraint c38shared_1 = new SimpleConstraint("type", "<=", 2);
        SimpleConstraint c38shared_2 = new SimpleConstraint("color", ">=", 0);
        ImplicationConstraint c38shared = new ImplicationConstraint(c38shared_1, c38shared_2);

        SimpleConstraint c39shared_1 = new SimpleConstraint("color", "=", 0);
        SimpleConstraint c39shared_2 = new SimpleConstraint("couplingdev", "<=", 1);
        ImplicationConstraint c39shared = new ImplicationConstraint(c39shared_1, c39shared_2);

        SimpleConstraint c40shared_1 = new SimpleConstraint("engine", "!=", 1);
        SimpleConstraint c40shared_2 = new SimpleConstraint("type", ">", 0);
        ImplicationConstraint c40shared = new ImplicationConstraint(c40shared_1, c40shared_2);

        SimpleConstraint c41shared_1 = new SimpleConstraint("service", "<=", 1);
        SimpleConstraint c41shared_2 = new SimpleConstraint("engine", "<=", 2);
        ImplicationConstraint c41shared = new ImplicationConstraint(c41shared_1, c41shared_2);

        SimpleConstraint c42shared_1 = new SimpleConstraint("fuel", ">", 2);
        SimpleConstraint c42shared_2 = new SimpleConstraint("type", ">", 1);
        ImplicationConstraint c42shared = new ImplicationConstraint(c42shared_1, c42shared_2);

        SimpleConstraint c43shared_1 = new SimpleConstraint("type", "=", 0);
        SimpleConstraint c43shared_2 = new SimpleConstraint("service", "=", 1);
        ImplicationConstraint c43shared = new ImplicationConstraint(c43shared_1, c43shared_2);

        SimpleConstraint c44shared_1 = new SimpleConstraint("color", ">=", 0);
        SimpleConstraint c44shared_2 = new SimpleConstraint("service", "<=", 2);
        ImplicationConstraint c44shared = new ImplicationConstraint(c44shared_1, c44shared_2);

        SimpleConstraint c45shared_1 = new SimpleConstraint("engine", "=", 1);
        SimpleConstraint c45shared_2 = new SimpleConstraint("service", "<=", 1);
        ImplicationConstraint c45shared = new ImplicationConstraint(c45shared_1, c45shared_2);

        SimpleConstraint c46shared_1 = new SimpleConstraint("couplingdev", ">", 0);
        SimpleConstraint c46shared_2 = new SimpleConstraint("type", ">=", 2);
        ImplicationConstraint c46shared = new ImplicationConstraint(c46shared_1, c46shared_2);

        SimpleConstraint c47shared_1 = new SimpleConstraint("type", "=", 0);
        SimpleConstraint c47shared_2 = new SimpleConstraint("couplingdev", "<", 1);
        ImplicationConstraint c47shared = new ImplicationConstraint(c47shared_1, c47shared_2);

        SimpleConstraint c48shared_1 = new SimpleConstraint("couplingdev", ">=", 0);
        SimpleConstraint c48shared_2 = new SimpleConstraint("service", ">=", 0);
        ImplicationConstraint c48shared = new ImplicationConstraint(c48shared_1, c48shared_2);

        SimpleConstraint c49shared_1 = new SimpleConstraint("type", "<=", 2);
        SimpleConstraint c49shared_2 = new SimpleConstraint("service", "<", 2);
        ImplicationConstraint c49shared = new ImplicationConstraint(c49shared_1, c49shared_2);

        allConstraints.add(c0shared);
        allConstraints.add(c1shared);
        allConstraints.add(c2shared);
        allConstraints.add(c3shared);
        allConstraints.add(c4shared);
        allConstraints.add(c5shared);
        allConstraints.add(c6shared);
        allConstraints.add(c7shared);
        allConstraints.add(c8shared);
        allConstraints.add(c9shared);
        allConstraints.add(c10shared);
        allConstraints.add(c11shared);
        allConstraints.add(c12shared);
        allConstraints.add(c13shared);
        allConstraints.add(c14shared);
        allConstraints.add(c15shared);
        allConstraints.add(c16shared);
        allConstraints.add(c17shared);
        allConstraints.add(c18shared);
        allConstraints.add(c19shared);
        allConstraints.add(c20shared);
        allConstraints.add(c21shared);
        allConstraints.add(c22shared);
        allConstraints.add(c23shared);
        allConstraints.add(c24shared);
        allConstraints.add(c25shared);
        allConstraints.add(c26shared);
        allConstraints.add(c27shared);
        allConstraints.add(c28shared);
        allConstraints.add(c29shared);
        allConstraints.add(c30shared);
        allConstraints.add(c31shared);
        allConstraints.add(c32shared);
        allConstraints.add(c33shared);
        allConstraints.add(c34shared);
        allConstraints.add(c35shared);
        allConstraints.add(c36shared);
        allConstraints.add(c37shared);
        allConstraints.add(c38shared);
        allConstraints.add(c39shared);
        allConstraints.add(c40shared);
        allConstraints.add(c41shared);
        allConstraints.add(c42shared);
        allConstraints.add(c43shared);
        allConstraints.add(c44shared);
        allConstraints.add(c45shared);
        allConstraints.add(c46shared);
        allConstraints.add(c47shared);
        allConstraints.add(c48shared);
        allConstraints.add(c49shared);

        for (int i = 0; i < number && i < allConstraints.size(); i++) {
            addConstraint(allConstraints.get(i));
        }
    }

    public void createPaperNorthAmericaConstraints() {
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

    public void createPaperEuropeConstraints() {
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
}
