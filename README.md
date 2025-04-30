# Consistency-Based Feature Model Merger

This project implements a **consistency-based merging algorithm** for feature models with regional variability, based on the approach described in the paper:

> *Mathias Uta, Alexander Felfernig, Gottfried Schenner, Johannes Spöcklberger (2019):  
"Consistency-based Merging of Variability Models"*  
([PDF]([https://arxiv.org/pdf/1910.13234.pdf](https://ceur-ws.org/Vol-2467/paper-02.pdf)))

The goal is to **merge two or more feature models** from the same domain, each tailored to different regions or product variants, into a **single, consistent and semantics-preserving feature model**.

---

## 🧠 What it does

- Parses two feature models in [UVL format](https://github.com/Universal-Variability-Language/uvl-models)
- Converts the models into a unified internal Java `FeatureModel` structure
- Contextualizes all constraints with region identifiers
- Unifies the feature trees, adding structural region-awareness
- Adds custom constraints to bind region-specific features
- Performs **inconsistency checking** to decontextualize constraints when safe
- Runs **redundancy cleanup** to remove unnecessary cross-tree constraints
- Outputs a **merged model** with correct semantics and solution space

---

## 📦 Used Libraries and Tools

- **[Choco Solver](https://choco-solver.org/)** – for all constraint satisfaction checks
- **ANTLR + UVL Parser** – for parsing `.uvl` files and generating the feature tree
- **Java 17** – for implementation
- **Gradle** – for project setup and dependency management
- **SLF4J / Logback** – for logging

---

## 🔬 Based On

- **Paper:** "Consistency-based Merging of Variability Models"  
  ([PDF](https://arxiv.org/pdf/1910.13234.pdf))
- **UVL Language Specification:**  
  [Universal Variability Language (UVL)](https://github.com/Universal-Variability-Language)

---

## 🚀 Future Improvements

- Export merged model back to UVL
- Visualization of the merged feature model
