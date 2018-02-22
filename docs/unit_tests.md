# Writing Unit Tests for Altas Checks

It is important to make sure your code works as expected as well as making sure that any changes you make don't break existing changes. A good approach to developing integrity checks is using a [Test Driven Development](https://en.wikipedia.org/wiki/Test-driven_development) based approach. In a nutshell design your unit tests first and then build your check making sure that the tests that you originally built succeeds. 

### Example Unit Test

For real examples you can look a couple that have already been built for the currently implemented checks:

- [SinkIslandCheckTest](../src/test/java/org/openstreetmap/atlas/checks/validation/linear/edges/SinkIslandCheckTest.java) and [SinkIslandCheckTestRule](../src/test/java/org/openstreetmap/atlas/checks/validation/linear/edges/SinkIslandCheckTestRule.java)
- [AbbreviatedNameCheckTest](../src/test/java/org/openstreetmap/atlas/checks/validation/tag/AbbreviatedNameCheckTest.java) and [AbbreviatedNameCheckTestRule](../src/test/java/org/openstreetmap/atlas/checks/validation/tag/AbbreviatedNameCheckTestRule.java)

As you see in the above their are two files associated with the unit tests. The first file are the unit tests themselves, the second file is, for lack of a better term, test Atlas files. Below we will first describe the structure of a unit test to be used to validate your Atlas Check and then describe how the data file is built and how to generate test atlas'.

### Our First Unit Test

```java
public class MyUnitTest {
    // For now we will assume that there is a class called MyUnitTestRule with a test atlas inside called "testAtlas"
    @Rule
    public MyUnitTestRule setup = new MyUnitTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testMyUnitTest()
    {
        // MyCheck would be replaced with the name of the check that you are testing
        // The configuration is optional, and only required if you have specific configuration for the check that you want to test or is required for the check itself
        this.verifier.actual(this.setup.testAtlas(),
                new MyCheck(ConfigurationResolver.inlineConfiguration("{\"key\":\"value\"}")));
        // There are various helper functions described below, this one simply checks if the result of the check on the test atlas produces at least 1 flag.
        this.verifier.verifyNotEmpty();
        
        // Another common scenario would be to loop through the results and check something on it.
        this.verifier.verify(flag -> {
            // Check the flag object for some expected values
        });
    }   
}
```

A basic Atlas-Checks unit test contains 3 things:
1. A TestRule class that is essentially an atlas or multiple atlas to test against.
2. A `ConsumerBasedExpectedCheckVerifier` instance that will execute the check you are testing against the test atlas.
3. A series of unit tests using the JUnit framework.

### The `ConsumerBasedExpectedCheckVerifier`

The ConsumerBasedExpectedCheckVerifier is a class that will run your check against a test atlas. The test atlas are often very small atlas objects that contain specific data to test against. This verifier has too main types of functions

1. `actual(Atlas, BaseCheck)` - The actual method establishes what data to run against (ie. what atlas file) and what check to run against the provided test atlas.
2. verify functions - The verify functions will be used to verify the results of the running the check over the test atlas file. Below are some useful functions
    
    - `verifyNotEmpty()` - Verifies that at least 1 flag was produced by the check
    - `verifyEmpty()` - Verifies that no flags were produced by the check
    - `verifyExpectedSize(int)` - Verifies that a specific number of flags were produced by the check
    - `verify(Consumer<List<CheckFlag>>)` - A custom verifier allowing the developer to loop through each individual flag that is produced and verify it in any way that the developer wishes.
    
 ### The `TestRule`
 
 As mentioned previously the TestRule is a class that is associated with the Unit test class and contains all the test data, which would essentially be an Atlas in some form or another. An Atlas can be created in 2 primary ways:
 
 - Inline - An inline atlas uses tags to build it within the code. The code below will create an inline atlas that contains 3 nodes and 2 edges. You can also use the inline @tags to build an Atlas containing `lines`, `points` or `relations`. An example of this can be found in the [FloatingEdgeCheckTestRule](../src/test/java/org/openstreetmap/atlas/checks/validation/linear/edges/FloatingEdgeCheckTestRule.java)
 ```java
@TestAtlas(nodes = { @Node(coordinates = @Loc(value = "37.32544,-122.033948")),
            @Node(coordinates = @Loc(value = "37.33531,-122.009566")),
            @Node(coordinates = @Loc(value = "37.3314171,-122.0304871")) }, edges = {
                    @Edge(coordinates = { @Loc(value = "37.32544,-122.033948"), @Loc(value = "37.33531,-122.009566") }, tags = {
                            "highway=SECONDARY" }),
                    @Edge(coordinates = { @Loc(value = "37.33531,-122.009566"), @Loc(value = "37.3314171,-122.0304871") }, tags = {
                            "highway=SECONDARY" }) })
```
- Atlas Text Files - It is fairly easy to produce very small test atlas text files which that can be stored in resources much like the [poolsize.altas](../src/test/resources/org/openstreetmap/atlas/checks/validation/areas/poolsize.atlas). And generating this files are fairly straight forward, and can be done by following these steps:
1. Generate a new Configuration in Intellij Idea IDE. (These general steps should work in any IDE, and the concept should be able to be transferred over to anything)
2. Choose Application and input the following parameters in the dialog to the right.
    - Name: AtlasTestGenerator
    - Main Class: org.openstreetmap.atlas.utilities.runtime.FlexibleCommand
    - Program Arguments: atlas-with-this-entity<br/>
                         -input=`[Input location for atlas file]`<br/>
                         -osmid=`[OSM ID of primary feature]`<br/>
                         -expand=`[Amount you want to expand around feature]`<br/>
                         -text-output=`[Output location for resultant atlas text file]`<br/>
    - Working directory: [Root directory of Atlas-Checks project]                     
    - Use classpath of module: atlas-checks_main
3. Run new AtlasTestGenerator configuration in Intellij.

This will produce a text atlas file in the location provided. You can then move the resultant file to the resources directory, much like the [poolsize.atlas](../src/test/resources/org/openstreetmap/atlas/checks/validation/areas/poolsize.atlas), Or you could set the output location to the resources directory to begin with. It is important to note that these files should be small as they would be checked into the repository as part of the code. There are options in the Atlas-Generator to zip up the results, but generally it is preferable to be able to see the contents of the atlas file. Once you have the text atlas file you can insert it into your test rule with the following code.
```java
@TestAtlas(loadFromTextResource = "test.atlas")
private Atlas testAtlas;

public Atlas getTestAtlas()
{
    return this.testAtlas;
}
```
The first line is the @tag that will load the text resource and instantiate the Atlas object below it. Then include a basic getter for the Atlas so that your unit test can use the test atlas. After completing all this you are ready to run your first unit test.
