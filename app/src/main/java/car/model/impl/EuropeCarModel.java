package car.model.impl;

import car.model.base.BaseCarModel;
import car.model.base.Region;

public class EuropeCarModel extends BaseCarModel {

        public EuropeCarModel() {
                this(false, 0);
        }

        public EuropeCarModel(boolean addConstraints, int number) {
                super();
                regionModel = Region.EUROPE;

                // Initialize variables
                region = model.intVar("region", 1, 1); // Europe
                type = model.intVar("type", 0, 3); // Combi: 0, Limo: 1, City: 2, Suv: 3
                color = model.intVar("color", 0, 1); // White: 0, Black: 1
                engine = model.intVar("engine", 0, 2); // 1l: 0, 1.5l: 1, 2l: 2
                couplingdev = model.intVar("couplingdev", 0, 1); // Yes: 0, No: 1
                fuel = model.intVar("fuel", 0, 3); // Electro: 0, Diesel: 1, Gas: 2, Hybrid: 3
                service = model.intVar("service", 0, 2); // 15k: 0, 20k: 1, 25k: 2

                if (addConstraints && number == 0) {
                        addLogicalConstraints();
                } else if (addConstraints) {
                        // addRandomConstraints(number);
                }
        }

        private void addLogicalConstraints() {
                // Constraint c1ger: fuel != gas (fuel != 2)
                model.arithm(fuel, "!=", 2).post();

                // Constraint c2ger: fuel = electro → couplingdev = no
                // (fuel == 0 → couplingdev == 1)
                model.ifThen(
                                model.arithm(fuel, "=", 0),
                                model.arithm(couplingdev, "=", 1));

                // Constraint c3ger: fuel = diesel → type != city
                // (fuel == 1 → type != 2)
                model.ifThen(
                                model.arithm(fuel, "=", 1),
                                model.arithm(type, "!=", 2));
        }

}
