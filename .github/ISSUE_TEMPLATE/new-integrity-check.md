---
name: New Integrity Check
about: Suggest a new integrity check
title: "[New Check]"
labels: new integrity check
assignees: ''

---

**Description**
Short description of the check. Very high level and in plain terms. Include references to existing documentation if applicable. 

**Requirements**
This section should specify the requirements for an Atlas object (or OSM feature) to be flagged by this check. This section is mainly for devs to get a sense of feature candidates for the source code.
1. Check Candidate (example)
 - Is an `Edge`|`Point`|`Relation`|`Node`|`Area`|`Line`|
 - Has `highway` tag, values include `trunk_link`, `trunk`, `motorway_link`

**Use Cases**
This section summarizes the various use cases for this check and provides potential enhancement suggestions if needed. The uses cases should act as a test in a way, such that when the analysis/enhancement is completed, these types of use cases should be found or eliminated.

For each use case, please add a screenshot of the feature & openstreetmap.org link (or other sources). If necessary, edit the screenshot to better illustrate the use case.

Example:

*Case 1: [Descriptive but concise title.] (Location)*
 - Summary/Explanation of what is being flagged and why
 - OSM Link

Add supporting  image here

**Further investigation**
Provide any additional information found that is worth mentioning (e.g. Further explanation of false positives or false negatives).

**Supported regions**
Specify which countries/regions this check is applicable to. List ISO codes. "All countries" works all well.

**What's not supported (optional):**
This section explains what elements are not supported by this check, whether or not these elements should be included/addressed in the future, and if these cases are better handled in another (existing or new) check.

**References**
Include a short description of the reference (e.g. osm wiki links, check source code, etc.) and provide a link.
