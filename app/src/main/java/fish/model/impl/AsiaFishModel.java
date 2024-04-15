package fish.model.impl;

import fish.model.base.BaseModel;
import fish.model.base.Region;

public class AsiaFishModel extends BaseModel {

        public AsiaFishModel() {
                this(false, 0);
        }

        public AsiaFishModel(boolean addConstraints, int number) {
                super();
                regionModel = Region.ASIA;

                // Initialize variables
                region = model.intVar("region", 2, 2); // Asia
                habitat = model.intVar("habitat", 0, 1); // Freshwater: 0, Saltwater: 1
                size = model.intVar("size", 0, 2); // S: 0, M: 1, L: 2
                diet = model.intVar("diet", 0, 2); // Herbivore: 0, Omnivore: 1, Carnivore: 2
                fishFamily = model.intVar("fishFamily", 0, 3); // Cyprinidae: 0, Scombridae: 1, Lutjanidae: 2,
                                                               // Cichlidae: 3
                fishSpecies = model.intVar("fishSpecies", 0, 7); // Grass Carp: 0, Silver Carp: 1, Yellowfin Tuna: 2,
                                                                 // Skipjack
                                                                 // Tuna: 3, Red Snapper: 4, Flame Snapper: 5, Tilapia:
                                                                 // 6,
                                                                 // Discus: 7

                if (addConstraints && number == 0) {
                        System.out.println("START|ADD-LOGICAL|-> " + regionModel.printRegion()
                                        + " | CONSTRAINTS: " + model.getNbCstrs());
                        addLogicalConstraints();
                        System.out.println("END  |ADD-LOGICAL|-> " + regionModel.printRegion()
                                        + " | CONSTRAINTS: " + model.getNbCstrs());
                } else if (addConstraints) {
                        addRandomConstraints(number);
                }

                System.out.println("CREATED Model " + regionModel.printRegion() + " with " + model.getNbCstrs()
                                + " constraints.");
        }

        private void addLogicalConstraints() {
                // Constraint 1: Cyprinidae such as Grass Carp are found in freshwater
                model.ifThen(model.arithm(fishFamily, "=", 0),
                                model.arithm(habitat, "=", 0));

                // Constraint 2: Scombridae, including Yellowfin Tuna, are found in saltwater
                model.ifThen(model.arithm(fishFamily, "=", 1),
                                model.arithm(habitat, "=", 1));

                // Constraint 3: Lutjanidae like Red Snapper are found in saltwater
                model.ifThen(model.arithm(fishFamily, "=", 2),
                                model.arithm(habitat, "=", 1));

                // Constraint 4: Cichlidae, including Tilapia, are primarily freshwater fish
                model.ifThen(model.arithm(fishFamily, "=", 3),
                                model.arithm(habitat, "=", 0));

                // Constraint 5: Large fish such as Yellowfin Tuna are found in saltwater
                model.ifThen(model.arithm(fishSpecies, "=", 2),
                                model.arithm(habitat, "=", 1));

                // Constraint 6: Silver Carp, a Cyprinidae, is generally large
                model.ifThen(model.arithm(fishSpecies, "=", 1),
                                model.arithm(size, "=", 2));

                // Constraint 7: Grass Carp are generally herbivores
                model.ifThen(model.arithm(fishSpecies, "=", 0),
                                model.arithm(diet, "=", 0));

                // Constraint 8: Carnivorous diet common in saltwater species like Red Snapper
                model.ifThen(model.arithm(fishSpecies, "=", 4),
                                model.arithm(diet, "=", 2));

                // Constraint 9: Medium-sized fish like Tilapia are primarily in freshwater
                model.ifThen(model.arithm(fishSpecies, "=", 6),
                                model.and(model.arithm(size, "=", 1), model.arithm(habitat, "=", 0)));

                // Constraint 10: Discus, a smaller Cichlidae, is found in freshwater
                model.ifThen(model.arithm(fishSpecies, "=", 7),
                                model.and(model.arithm(size, "=", 0), model.arithm(habitat, "=", 0)));

                // Constraint 11: Scombridae species are generally large
                model.ifThen(model.arithm(fishFamily, "=", 1),
                                model.arithm(size, "=", 2));

                // Constraint 12: Small to medium-sized Cichlidae are omnivorous
                model.ifThen(model.arithm(fishFamily, "=", 3),
                                model.arithm(diet, "=", 1));

                // Constraint 13: Large and medium-sized fish are more common in saltwater
                model.ifThen(model.arithm(size, ">=", 1),
                                model.arithm(habitat, "=", 1));

                // Constraint 14: Smaller fish in freshwater are typically herbivorous
                model.ifThen(model.arithm(size, "=", 0),
                                model.and(model.arithm(habitat, "=", 0), model.arithm(diet, "=", 0)));

                // Constraint 15: Omnivores are common among Cyprinidae
                model.ifThen(model.arithm(fishFamily, "=", 0),
                                model.arithm(diet, "=", 1));

                // Constraint 16: Saltwater habitats favor carnivorous species like Flame
                // Snapper
                model.ifThen(model.arithm(fishSpecies, "=", 5),
                                model.and(model.arithm(habitat, "=", 1), model.arithm(diet, "=", 2)));

                // Constraint 17: Large saltwater fish are typically carnivorous
                model.ifThen(model.arithm(size, "=", 2),
                                model.and(model.arithm(habitat, "=", 1), model.arithm(diet, "=", 2)));

                // Constraint 18: Freshwater habitats predominantly host small to medium-sized
                // fish
                model.ifThen(model.arithm(habitat, "=", 0),
                                model.or(model.arithm(size, "=", 0), model.arithm(size, "=", 1)));

                // Constraint 19: Large freshwater fish are not herbivores
                model.ifThen(model.and(model.arithm(size, "=", 2), model.arithm(habitat, "=", 0)),
                                model.arithm(diet, "!=", 0));

                // Constraint 20: Omnivorous diet is prevalent in diverse habitats
                model.ifThen(model.arithm(diet, "=", 1),
                                model.or(model.arithm(habitat, "=", 0), model.arithm(habitat, "=", 1)));

                // Constraint 21: Large carnivorous fish like Skipjack Tuna are found in
                // saltwater
                model.ifThen(model.arithm(fishSpecies, "=", 3),
                                model.arithm(habitat, "=", 1));

                // Constraint 22: Medium and large fish in freshwater are generally omnivorous
                model.ifThen(model.or(model.arithm(size, "=", 1), model.arithm(size, "=", 2)),
                                model.and(model.arithm(diet, "=", 1), model.arithm(habitat, "=", 0)));

                // Constraint 23: Freshwater environments do not host large carnivorous fish
                model.ifThen(model.and(model.arithm(size, "=", 2), model.arithm(habitat, "=", 0)),
                                model.arithm(diet, "!=", 2));

                // Constraint 24: Small fish in saltwater environments are primarily herbivorous
                model.ifThen(model.and(model.arithm(size, "=", 0), model.arithm(habitat, "=", 1)),
                                model.arithm(diet, "=", 0));

                // Constraint 25: Medium-sized herbivores like Tilapia are common in freshwater
                model.ifThen(model.and(model.arithm(fishSpecies, "=", 6), model.arithm(size, "=", 1)),
                                model.and(model.arithm(habitat, "=", 0), model.arithm(diet, "=", 0)));

                // Constraint 26: Small fish in freshwater are typically omnivorous
                model.ifThen(model.and(model.arithm(size, "=", 0), model.arithm(habitat, "=", 0)),
                                model.arithm(diet, "=", 1));

                // Constraint 27: Saltwater fish families like Scombridae are generally not
                // herbivores
                model.ifThen(model.arithm(fishFamily, "=", 1),
                                model.arithm(diet, "!=", 0));

                // Constraint 28: All Cichlidae, regardless of size, found in freshwater
                model.ifThen(model.arithm(fishFamily, "=", 3),
                                model.arithm(habitat, "=", 0));

                // Constraint 29: Small freshwater fish are not carnivorous
                model.ifThen(model.and(model.arithm(size, "=", 0), model.arithm(habitat, "=", 0)),
                                model.arithm(diet, "!=", 2));

                // Constraint 30: Large fish are generally carnivorous in saltwater
                model.ifThen(model.and(model.arithm(size, "=", 2), model.arithm(habitat, "=", 1)),
                                model.arithm(diet, "=", 2));

                // Constraint 31: Saltwater habitats host primarily medium and large fish
                model.ifThen(model.arithm(habitat, "=", 1),
                                model.or(model.arithm(size, "=", 1), model.arithm(size, "=", 2)));

                // Constraint 32: Freshwater herbivores include fish like Silver Carp
                model.ifThen(model.arithm(fishSpecies, "=", 1),
                                model.and(model.arithm(diet, "=", 0), model.arithm(habitat, "=", 0)));

                // Constraint 33: Omnivores are found across all size categories in freshwater
                model.ifThen(model.arithm(habitat, "=", 0),
                                model.or(model.arithm(size, "=", 0),
                                                model.or(model.arithm(size, "=", 1), model.arithm(size, "=", 2))));

                // Constraint 34: No small carnivorous fish in freshwater
                model.ifThen(model.and(model.arithm(size, "=", 0), model.arithm(habitat, "=", 0)),
                                model.arithm(diet, "!=", 2));

                // Constraint 35: Large omnivorous fish are common in freshwater
                model.ifThen(model.and(model.arithm(size, "=", 2), model.arithm(habitat, "=", 0)),
                                model.arithm(diet, "=", 1));

                // Constraint 36: Small and medium fish in saltwater are rarely carnivorous
                model.ifThen(
                                model.and(model.arithm(habitat, "=", 1),
                                                model.or(model.arithm(size, "=", 0), model.arithm(size, "=", 1))),
                                model.arithm(diet, "!=", 2));

                // Constraint 37: Medium-sized fish in saltwater are typically omnivorous
                model.ifThen(model.and(model.arithm(size, "=", 1), model.arithm(habitat, "=", 1)),
                                model.arithm(diet, "=", 1));

                // Constraint 38: Freshwater environments predominantly host herbivorous species
                model.ifThen(model.arithm(habitat, "=", 0),
                                model.arithm(diet, "=", 0));

                // Constraint 39: Medium and large fish are not herbivores in saltwater
                model.ifThen(
                                model.and(model.arithm(habitat, "=", 1),
                                                model.or(model.arithm(size, "=", 1), model.arithm(size, "=", 2))),
                                model.arithm(diet, "!=", 0));

                // Constraint 40: Large carnivorous fish like Yellowfin Tuna are exclusive to
                // saltwater
                model.ifThen(model.and(model.arithm(fishSpecies, "=", 2), model.arithm(size, "=", 2)),
                                model.and(model.arithm(habitat, "=", 1), model.arithm(diet, "=", 2)));
        }

        @Override
        public String getHabitat(int value) {
                return value == 0 ? "Freshwater" : "Saltwater";
        }

        @Override
        public String getSize(int value) {
                switch (value) {
                        case 0:
                                return "S";
                        case 1:
                                return "M";
                        case 2:
                                return "L";
                        default:
                                return "Unknown";
                }
        }

        @Override
        public String getDiet(int value) {
                switch (value) {
                        case 0:
                                return "Herbivore";
                        case 1:
                                return "Omnivore";
                        case 2:
                                return "Carnivore";
                        default:
                                return "Unknown";
                }
        }

        @Override
        public String getFishFamily(int value) {
                switch (value) {
                        case 0:
                                return "Cyprinidae";
                        case 1:
                                return "Scombridae";
                        case 2:
                                return "Lutjanidae";
                        case 3:
                                return "Cichlidae";
                        default:
                                return "Unknown";
                }
        }

        @Override
        public String getFishSpecies(int value) {
                switch (value) {
                        case 0:
                                return "Grass Carp";
                        case 1:
                                return "Silver Carp";
                        case 2:
                                return "Yellowfin Tuna";
                        case 3:
                                return "Skipjack Tuna";
                        case 4:
                                return "Red Snapper";
                        case 5:
                                return "Flame Snapper";
                        case 6:
                                return "Tilapia";
                        case 7:
                                return "Discus";
                        default:
                                return "Unknown";
                }
        }
}
