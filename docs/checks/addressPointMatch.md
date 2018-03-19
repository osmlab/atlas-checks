# Address Point Match Check

#### Description

This check identifies cases of improperly tagged street names (addr:street). This check includes
cases where the street name has not been specified or if it has the incorrect street name. Due to
the fact that we cannot account for different geocoding techniques that may exist in different parts
of the world, we operate under the assumption that the nearest street to a given address location
should be the street name tagged.

#### Live Example


#### Code Review


To learn more about the code, please look at the comments in the source code for the check.
[AddressPointMatchCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/AddressPointMatchCheck.java)