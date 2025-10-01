# Consistency-Based Feature Model Merger

This project implements a **consistency-based merging algorithm** for feature models with regional variability, based on the approach described in the paper:

> *Mathias Uta, Alexander Felfernig, Gottfried Schenner, Johannes Spöcklberger (2019):  
"Consistency-based Merging of Variability Models"*  
([PDF](https://ceur-ws.org/Vol-2467/paper-02.pdf))

The goal is to **merge two or more feature models** from the same domain, each tailored to different regions or product variants, into a **single, consistent and semantics-preserving feature model**.

---

## 🚀 Quick Start

### Prerequisites
- **Java 21** or higher
- **Gradle** (included via wrapper)


### Basic Usage
```java
// Parse two UVL models
RecreationModel modelA = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
RecreationModel modelB = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

// Perform full merge
RecreationModel mergedModel = Merger.fullMerge(modelA, modelB).mergedModel();

// Validate the merge
Validator.validateMerge(mergedModel, modelA, modelB);
```

---

## 🧠 What it does

The merger implements a **three-phase algorithm**:

### 1. **Union Phase**
- Combines features and constraints from all input models
- Creates unified region structures and root feature hierarchies
- Handles feature splitting for the same features in different regions under different parents
- Preserves contextualization information

### 2. **Inconsistency Check Phase**
- Analyzes each contextualized cross tree constraint for consistency
- Decontextualizes constraints that would create inconsistencies

### 3. **Cleanup Phase**
- Removes redundant constraints that don't affect the solution space
- Uses constraint negation to identify removable constraints
- Ensures the final model is minimal while preserving semantics

---

## 📁 Project Structure

```
app/src/main/java/
├── model/                    # Data structures
│   ├── choco/               # Choco Solver integration
│   └── recreate/            # Feature model representation
├── util/                    # Core utilities
│   ├── analyse/             # Analysis and statistics
│   ├── helper/              # Merge helper functions
│   ├── Merger.java          # Main merging engine
│   ├── UVLParser.java       # UVL file parser
│   └── Validator.java       # Merged model validation
└── testcases/               # Example applications
    └── merge_analysis/      # Batch analysis tools

app/src/main/resources/uvl/  # Test models
├── automotive/             # real world models
├── busybox/                # real world models
├── finance/                # real world models
├── smartwatch/             # real world models
├── paper_test_models/      # Feature Model examples from the Paper
└── testcases/              # Synthetic test cases
```

---

## 🔧 Key Components

### Core Classes
- **`Merger`** - Main merging algorithm implementation
- **`UVLParser`** - Parses UVL files into internal representation
- **`Validator`** - Validates merge correctness and semantic preservation
- **`Analyser`** - Provides analysis and statistics functionality

### Model Representations
- **`RecreationModel`** - Internal feature model representation
- **`ChocoModel`** - Constraint satisfaction model for solving
- **`Feature`** - Individual boolean feature
- **`AbstractConstraint`** - Base class for all constraint types

---

## 📦 Dependencies & Tools

- **[Choco Solver 4.10.18](https://choco-solver.org/)** – Constraint satisfaction solving
- **[UVL Parser 0.3](https://github.com/Universal-Variability-Language/uvl-parser)** – UVL file parsing
- **Java 21** – Implementation language
- **Gradle 8.0+** – Build system and dependency management
- **JUnit 5** – Testing framework
- **Log4J2** – Logging and output
- **Lombok** – Code generation
- **SonarQube** – Code quality analysis (coverage disabled)

---

## 🔬 Research Foundation

- **Paper:** "Consistency-based Merging of Variability Models"  
  ([PDF](https://ceur-ws.org/Vol-2467/paper-02.pdf))
- **UVL Language Specification:**  
  [Universal Variability Language (UVL)](https://github.com/Universal-Variability-Language)

---

## 🚀 Future Improvements

- **Multi-Region Support** - Handle more than two regions simultaneously
- **Performance Optimization** - Better Translation of Constraints in Java choco
