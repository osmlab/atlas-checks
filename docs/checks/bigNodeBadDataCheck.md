# Big Node Bad Data Check

#### Description

This check will simply look for complex intersections and flag them when they have too many paths and junction edges.

#### Configuration

This check has three configurables that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"max.number.threshold": 25``` - Max number of paths in an intersection
- ```"max.number.junction.edges.threshold": 2``` - Max number of junction edges in an intersection.
- ```""highway.type": {"minimum": "toll_gantry", "maximum": "motorway}``` - Min and max highway type 

#### Live Examples

Complex intersection:
- Edge [id:157299839](https://www.openstreetmap.org/way/157299839), [id:619577664](https://www.openstreetmap.org/way/619577664), and [id:517407227](https://www.openstreetmap.org/way/517407227)
- Edge [id:517407229](https://www.openstreetmap.org/way/517407229), [id:517350955](https://www.openstreetmap.org/way/517350955), and [id:517350956](https://www.openstreetmap.org/way/517350956)
- Edge [id:668226161](https://www.openstreetmap.org/way/668226161), [id:4370531](https://www.openstreetmap.org/way/4370531), and [id:619577663](https://www.openstreetmap.org/way/619577663)
- Edge [id:517350954](https://www.openstreetmap.org/way/517350954), [id:517407228](https://www.openstreetmap.org/way/517407228), and [id:110014409](https://www.openstreetmap.org/way/110014409)

Please see the source code for BigNodeBadDataCheck here: [BigNodeBadDataCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/BigNodeBadDataCheck.java)